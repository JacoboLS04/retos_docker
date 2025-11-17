# ms-notifications/services/sms_service.py
import os
from twilio.rest import Client

TWILIO_SID = os.getenv("TWILIO_ACCOUNT_SID")
TWILIO_AUTH = os.getenv("TWILIO_AUTH_TOKEN")
TWILIO_PHONE = os.getenv("TWILIO_PHONE")

def send_sms(to_number: str, content: str):
    client = Client(TWILIO_SID, TWILIO_AUTH)
    print("hola esoty utilizando servicios de sms")
    message = client.messages.create(
        body=content,
        from_=TWILIO_PHONE,
        to=to_number
    )
    print(f"[SMS] Sent to {to_number}, sid={message.sid}")
    return message.sid
