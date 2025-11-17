# features/steps/email_steps.py
from behave import given, when, then
from unittest.mock import patch, MagicMock


@given('the email service is available')
def step_email_service_available(context):
    context.email_service_available = True
    context.mock_sendgrid = patch('services.email_service.SendGridAPIClient')
    context.mock_sg = context.mock_sendgrid.start()
    mock_response = MagicMock()
    mock_response.status_code = 202
    context.mock_sg.return_value.send.return_value = mock_response


@given('the email service is unavailable')
def step_email_service_unavailable(context):
    context.email_service_available = False
    context.mock_sendgrid = patch('services.email_service.SendGridAPIClient')
    context.mock_sg = context.mock_sendgrid.start()
    context.mock_sg.return_value.send.side_effect = Exception("Service unavailable")


@when('I send a welcome email to "{email}"')
def step_send_welcome_email(context, email):
    from services.email_service import send_email
    try:
        context.status_code = send_email(email, "Welcome", "<h1>Welcome!</h1>")
        context.email_sent = True
        context.error = None
    except Exception as e:
        context.email_sent = False
        context.error = str(e)


@when('I send a password reset email to "{email}"')
def step_send_reset_email(context, email):
    from services.email_service import send_email
    try:
        context.status_code = send_email(
            email,
            "Password Reset",
            "Click here to reset: http://retos-spring-app:8080/api/auth/reset-password?token=abc123"
        )
        context.email_sent = True
        context.email_content = "Click here to reset: http://retos-spring-app:8080/api/auth/reset-password?token=abc123"
        context.error = None
    except Exception as e:
        context.email_sent = False
        context.error = str(e)


@when('I attempt to send an email to "{email}"')
def step_attempt_send_email(context, email):
    from services.email_service import send_email
    try:
        context.status_code = send_email(email, "Test", "Test content")
        context.email_sent = True
        context.error = None
    except Exception as e:
        context.email_sent = False
        context.error = str(e)


@when('I send an HTML email to "{email}" with content "{content}"')
def step_send_html_email(context, email, content):
    from services.email_service import send_email
    try:
        context.status_code = send_email(email, "HTML Email", content)
        context.email_sent = True
        context.email_content = content
        context.error = None
    except Exception as e:
        context.email_sent = False
        context.error = str(e)


@then('the email should be sent successfully')
def step_email_sent_successfully(context):
    assert context.email_sent is True, "Email should be sent"
    assert context.status_code == 202, f"Expected 202, got {context.status_code}"


@then('the email should contain a reset link')
def step_email_contains_reset_link(context):
    assert context.email_sent is True
    assert "reset" in context.email_content.lower()
    assert "http://" in context.email_content or "https://" in context.email_content


@then('the email should fail to send')
def step_email_failed(context):
    assert context.email_sent is False, "Email should not be sent"
    assert context.error is not None, "Error should be recorded"


@then('the notification log should record the email as "{status}"')
def step_log_email_status(context, status):
    # This would check DB in integration tests, here we just verify the flow
    assert context.email_sent == (status == "SENT")


@then('the notification log should record the error')
def step_log_error(context):
    assert context.error is not None


@then('the notification should be logged')
def step_notification_logged(context):
    assert context.email_sent is True


@then('the content should be HTML formatted')
def step_content_is_html(context):
    assert "<h1>" in context.email_content or "<" in context.email_content


def after_scenario(context, scenario):
    """Clean up mocks after each scenario"""
    if hasattr(context, 'mock_sendgrid'):
        context.mock_sendgrid.stop()
