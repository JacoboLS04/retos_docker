# tests/test_user_events.py
import pytest
from unittest.mock import patch, MagicMock
from consumers.user_events import handle_user_event


@pytest.fixture
def mock_services():
    """Mock email and SMS services"""
    with patch('consumers.user_events.send_email') as mock_email, \
         patch('consumers.user_events.send_sms') as mock_sms, \
         patch('consumers.user_events.save_notification_log') as mock_log:
        yield mock_email, mock_sms, mock_log


def test_handle_email_event(mock_services):
    """Test handling EMAIL event"""
    mock_email, mock_sms, mock_log = mock_services
    
    event = {
        "channel": "EMAIL",
        "to": "user@example.com",
        "subject": "Welcome",
        "body": "<h1>Welcome to our platform</h1>"
    }
    
    handle_user_event(event)
    
    # Verify email was sent
    mock_email.assert_called_once_with(
        "user@example.com",
        "Welcome",
        "<h1>Welcome to our platform</h1>"
    )
    
    # Verify SMS was not sent
    mock_sms.assert_not_called()
    
    # Verify log was saved
    assert mock_log.called


def test_handle_sms_event(mock_services):
    """Test handling SMS event"""
    mock_email, mock_sms, mock_log = mock_services
    
    event = {
        "channel": "SMS",
        "to": "+1234567890",
        "body": "Your verification code is 123456"
    }
    
    handle_user_event(event)
    
    # Verify SMS was sent
    mock_sms.assert_called_once_with(
        "+1234567890",
        "Your verification code is 123456"
    )
    
    # Verify email was not sent
    mock_email.assert_not_called()
    
    # Verify log was saved
    assert mock_log.called


def test_handle_sms_event_with_empty_phone(mock_services):
    """Test handling SMS event with empty phone number"""
    mock_email, mock_sms, mock_log = mock_services
    
    event = {
        "channel": "SMS",
        "to": "",
        "body": "Test message"
    }
    
    handle_user_event(event)
    
    # Verify SMS was not sent
    mock_sms.assert_not_called()
    
    # Verify log was saved with FAILED status
    mock_log.assert_called_once()
    call_args = mock_log.call_args
    assert call_args[0][2] == event  # payload
    assert call_args[0][3] == "FAILED"  # status


def test_handle_sms_event_with_whitespace_phone(mock_services):
    """Test handling SMS event with whitespace phone number"""
    mock_email, mock_sms, mock_log = mock_services
    
    event = {
        "channel": "SMS",
        "to": "   ",
        "body": "Test"
    }
    
    handle_user_event(event)
    
    mock_sms.assert_not_called()
    assert mock_log.called


def test_handle_unknown_channel(mock_services):
    """Test handling event with unknown channel"""
    mock_email, mock_sms, mock_log = mock_services
    
    event = {
        "channel": "PUSH",
        "to": "user123",
        "body": "Test notification"
    }
    
    handle_user_event(event)
    
    # Neither service should be called
    mock_email.assert_not_called()
    mock_sms.assert_not_called()
    
    # Log should be called with FAILED status
    assert mock_log.called


def test_handle_email_event_failure(mock_services):
    """Test handling EMAIL event when send_email fails"""
    mock_email, mock_sms, mock_log = mock_services
    mock_email.side_effect = Exception("SendGrid API Error")
    
    event = {
        "channel": "EMAIL",
        "to": "user@example.com",
        "subject": "Test",
        "body": "Test"
    }
    
    handle_user_event(event)
    
    # Verify log was called with FAILED status
    assert mock_log.called
    call_args = mock_log.call_args
    assert call_args[0][3] == "FAILED"
    assert "SendGrid API Error" in call_args[0][4]


def test_handle_sms_event_failure(mock_services):
    """Test handling SMS event when send_sms fails"""
    mock_email, mock_sms, mock_log = mock_services
    mock_sms.side_effect = Exception("Twilio API Error")
    
    event = {
        "channel": "SMS",
        "to": "+1234567890",
        "body": "Test"
    }
    
    handle_user_event(event)
    
    # Verify log was called with FAILED status
    assert mock_log.called
    call_args = mock_log.call_args
    assert call_args[0][3] == "FAILED"
    assert "Twilio API Error" in call_args[0][4]


def test_handle_event_missing_channel(mock_services):
    """Test handling event without channel field"""
    mock_email, mock_sms, mock_log = mock_services
    
    event = {
        "to": "test@example.com",
        "body": "Test"
    }
    
    handle_user_event(event)
    
    # Neither service should succeed
    mock_email.assert_not_called()
    mock_sms.assert_not_called()
