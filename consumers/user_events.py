# ms-notifications/consumers/user_events.py
from services.email_service import send_email
from services.sms_service import send_sms
from db import save_notification_log  # 👈 función que guarde en notification_logs

def handle_user_event(event: dict):
    channel = event.get("channel")
    to = event.get("to")
    body = event.get("body", "")
    subject = event.get("subject", "")

    try:
        if channel == "EMAIL":
            send_email(to, subject, body)
            save_notification_log(channel, to, event, "SUCCESS")

        elif channel == "SMS":
            if not to or to.strip() == "":
                print("⚠️ Número de teléfono no válido, SMS no enviado.")
                save_notification_log(channel, to, event, "FAILED", "Número inválido")
                return

            send_sms(to, body)
            save_notification_log(channel, to, event, "SUCCESS")

        else:
            raise ValueError(f"⚠️ Canal no soportado: {channel}")

    except Exception as e:
        print(f"❌ Error en envío {channel} a {to}: {e}")
        save_notification_log(channel, to, event, "FAILED", str(e))

