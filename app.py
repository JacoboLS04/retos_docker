# ms-notifications/app.py
import pika, os, time, json
import threading
from datetime import datetime, timezone
from consumers.user_events import handle_user_event
from db import save_notification_log

# HTTP server
from flask import Flask, jsonify


# ----- Configuration -----
RABBIT_HOST = os.getenv("RABBIT_HOST", "rabbitmq")
QUEUE_NAME = os.getenv("NOTIFICATION_EVENTS_QUEUE", "notification.events.queue")
RETRIES = int(os.getenv("NOTIFICATION_SEND_RETRIES", 3))
SERVICE_VERSION = os.getenv("SERVICE_VERSION", "0.1.0")


# ----- Uptime tracking -----
START_TIME = datetime.now(timezone.utc)


def iso_start_time():
    return START_TIME.isoformat()


def uptime_seconds():
    return int((datetime.now(timezone.utc) - START_TIME).total_seconds())


def human_readable_uptime():
    secs = uptime_seconds()
    hours, remainder = divmod(secs, 3600)
    minutes, seconds = divmod(remainder, 60)
    return f"{hours:02}:{minutes:02}:{seconds:02}"


# ----- Health helpers -----
def readiness_check():
    # Aquí puede añadirse lógica (DB reachable, config loaded, etc.)
    return {
        "name": "Readiness check",
        "status": "UP",
        "data": {"from": iso_start_time(), "status": "READY"},
    }


def liveness_check():
    # Liveness simple: el proceso está vivo
    return {
        "name": "Liveness check",
        "status": "UP",
        "data": {"from": iso_start_time(), "status": "ALIVE"},
    }


# ----- Flask app (HTTP) -----
http_app = Flask(__name__)


@http_app.route("/health", methods=["GET"])
def health():
    checks = [readiness_check(), liveness_check()]
    overall = "UP" if all(c["status"] == "UP" for c in checks) else "DOWN"
    payload = {
        "status": overall,
        "version": SERVICE_VERSION,
        "uptime": human_readable_uptime(),
        "checks": checks,
    }
    return jsonify(payload), 200 if overall == "UP" else 503


@http_app.route("/health/ready", methods=["GET"])
def health_ready():
    check = readiness_check()
    payload = {
        "status": check["status"],
        "version": SERVICE_VERSION,
        "uptime": human_readable_uptime(),
        "checks": [check],
    }
    return jsonify(payload), 200 if check["status"] == "UP" else 503


@http_app.route("/health/live", methods=["GET"])
def health_live():
    check = liveness_check()
    payload = {
        "status": check["status"],
        "version": SERVICE_VERSION,
        "uptime": human_readable_uptime(),
        "checks": [check],
    }
    return jsonify(payload), 200 if check["status"] == "UP" else 503


# ----- RabbitMQ consumer logic (unchanged) -----
def connect_rabbitmq():
    retries = 10
    while retries > 0:
        try:
            connection = pika.BlockingConnection(
                pika.ConnectionParameters(host=RABBIT_HOST, port=5672)
            )
            print("✅ Connected to RabbitMQ")
            return connection
        except pika.exceptions.AMQPConnectionError:
            print("⏳ RabbitMQ not ready, retrying...")
            retries -= 1
            time.sleep(5)
    raise Exception("❌ Could not connect to RabbitMQ")


def callback(ch, method, properties, body):
    print(f"[Rabbit] Received raw: {body}")
    try:
        event = json.loads(body)
    except Exception as e:
        print(f"[Rabbit] Invalid JSON: {e} -> acking and discarding")
        ch.basic_ack(delivery_tag=method.delivery_tag)
        return

    # hacemos el intento de envío y guardamos resultado por canal
    try:
        # handle_user_event debe lanzar excepción si falla
        handle_user_event(event)
        save_notification_log(
            channel=event.get("channel", "UNKNOWN"),
            recipient={
                "email": event.get("to") if event.get("channel") == "EMAIL" else None,
                "phone": event.get("to") if event.get("channel") == "SMS" else None,
            },
            payload=event,
            status="SENT",
            error=None,
        )
        ch.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        print(f"[Rabbit] Error handling event: {e}")
        # Guardar fallo en DB
        try:
            save_notification_log(
                channel=event.get("channel", "UNKNOWN"),
                recipient={
                    "email": event.get("to") if event.get("channel") == "EMAIL" else None,
                    "phone": event.get("to") if event.get("channel") == "SMS" else None,
                },
                payload=event,
                status="FAILED",
                error=str(e),
            )
        except Exception as db_e:
            print(f"[DB] Error saving failure log: {db_e}")

        # Decisión: no requeue infinito. Aquí descartamos el mensaje (ack) o podrías requeue n veces:
        ch.basic_ack(delivery_tag=method.delivery_tag)


def run_consumer():
    connection = connect_rabbitmq()
    channel = connection.channel()
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
    # usaremos ack manual (auto_ack=False)
    channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback, auto_ack=False)
    print(f"[Rabbit] Waiting for messages in {QUEUE_NAME}...")
    channel.start_consuming()


def run_http():
    # Ejecutar flask en 0.0.0.0:8080 en un hilo separado
    http_app.run(host="0.0.0.0", port=int(os.getenv("PORT", 8080)), debug=False, use_reloader=False)


def main():
    # Start HTTP server in a background thread
    t = threading.Thread(target=run_http, daemon=True)
    t.start()

    # Run consumer in main thread (blocking)
    run_consumer()


if __name__ == "__main__":
    main()
