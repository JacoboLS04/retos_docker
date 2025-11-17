# ms-notifications/services/email_service.py
import os
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail

SENDGRID_API_KEY = os.getenv("SENDGRID_API_KEY")
SENDER_EMAIL = os.getenv("SENDER_EMAIL", "londgav01@gmail.com")

def send_email(to_email: str, subject: str, content: str):
    message = Mail(
        from_email=SENDER_EMAIL,
        to_emails=to_email,
        subject=subject,
        html_content=content
    )
    sg = SendGridAPIClient(SENDGRID_API_KEY)
    response = sg.send(message)
    print(f"[EMAIL] Sent to {to_email}, status={response.status_code}")
    return response.status_code
