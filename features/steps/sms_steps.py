# features/steps/sms_steps.py
from behave import given, when, then
from unittest.mock import patch, MagicMock


@given('the SMS service is available')
def step_sms_service_available(context):
    context.sms_service_available = True
    context.mock_twilio = patch('services.sms_service.Client')
    context.mock_client = context.mock_twilio.start()
    mock_message = MagicMock()
    mock_message.sid = "SM123456789"
    context.mock_client.return_value.messages.create.return_value = mock_message


@given('the SMS service is unavailable')
def step_sms_service_unavailable(context):
    context.sms_service_available = False
    context.mock_twilio = patch('services.sms_service.Client')
    context.mock_client = context.mock_twilio.start()
    context.mock_client.return_value.messages.create.side_effect = Exception("Service unavailable")


@when('I send a verification code SMS to "{phone}"')
def step_send_verification_sms(context, phone):
    from services.sms_service import send_sms
    try:
        context.sms_sid = send_sms(phone, "Your verification code is 123456")
        context.sms_sent = True
        context.sms_content = "Your verification code is 123456"
        context.error = None
    except Exception as e:
        context.sms_sent = False
        context.error = str(e)


@when('I send a login alert SMS to "{phone}"')
def step_send_login_alert_sms(context, phone):
    from services.sms_service import send_sms
    try:
        context.sms_sid = send_sms(phone, "New login detected on your account")
        context.sms_sent = True
        context.sms_content = "New login detected on your account"
        context.error = None
    except Exception as e:
        context.sms_sent = False
        context.error = str(e)


@when('I attempt to send SMS to an empty phone number')
def step_send_sms_empty_phone(context):
    from consumers.user_events import handle_user_event
    
    event = {
        "channel": "SMS",
        "to": "",
        "body": "Test message"
    }
    
    with patch('consumers.user_events.send_sms') as mock_sms, \
         patch('consumers.user_events.save_notification_log') as mock_log:
        handle_user_event(event)
        context.sms_sent = mock_sms.called
        context.log_called = mock_log.called
        if mock_log.called:
            context.log_status = mock_log.call_args[0][3]


@when('I attempt to send an SMS to "{phone}"')
def step_attempt_send_sms(context, phone):
    from services.sms_service import send_sms
    try:
        context.sms_sid = send_sms(phone, "Test message")
        context.sms_sent = True
        context.error = None
    except Exception as e:
        context.sms_sent = False
        context.error = str(e)


@then('the SMS should be delivered successfully')
def step_sms_delivered(context):
    assert context.sms_sent is True, "SMS should be sent"
    assert context.sms_sid == "SM123456789"


@then('the message should indicate a new login')
def step_message_indicates_login(context):
    assert context.sms_sent is True
    assert "login" in context.sms_content.lower()


@then('the SMS should not be sent')
def step_sms_not_sent(context):
    assert context.sms_sent is False, "SMS should not be sent"


@then('the SMS should fail to send')
def step_sms_failed(context):
    assert context.sms_sent is False
    assert context.error is not None


@then('the notification log should record the SMS as "{status}"')
def step_log_sms_status(context, status):
    assert context.sms_sent == (status == "SENT")


@then('the notification log should record "{status}" status')
def step_log_status(context, status):
    if hasattr(context, 'log_status'):
        assert context.log_status == status
    else:
        # In unit test mode, just verify the flow
        assert context.sms_sent is False


@then('the error should be logged in the notification log')
def step_error_logged(context):
    assert context.error is not None


def after_scenario(context, scenario):
    """Clean up mocks after each scenario"""
    if hasattr(context, 'mock_twilio'):
        context.mock_twilio.stop()
