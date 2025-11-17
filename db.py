# ms-notifications/db.py
import os
import json
import psycopg2
from psycopg2.extras import Json, RealDictCursor

DB_HOST = os.getenv("DB_HOST", "postgres")
DB_PORT = int(os.getenv("DB_PORT", 5432))
DB_NAME = os.getenv("DB_NAME", "notifications_db")
DB_USER = os.getenv("DB_USER", "notifications_user")
DB_PASS = os.getenv("DB_PASS", "notifications_pass")

_conn = None

def get_conn():
    global _conn
    if _conn is None:
        _conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        _conn.autocommit = True
    return _conn

def save_notification_log(channel: str, recipient: dict, payload: dict, status: str, error: str | None = None):
    conn = get_conn()
    cur = conn.cursor()
    cur.execute(
        """
        INSERT INTO notification_logs (channel, recipient, payload, status, error)
        VALUES (%s, %s, %s, %s, %s)
        """,
        (
            channel,
            Json(recipient),
            Json(payload),
            status,
            error
        )
    )
    cur.close()
