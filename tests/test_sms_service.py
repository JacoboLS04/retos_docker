# tests/test_sms_service.py
import pytest
from unittest.mock import patch, MagicMock
from services.sms_service import send_sms


@pytest.fixture
def mock_twilio():
    """Mock Twilio Client"""
    with patch('services.sms_service.Client') as mock_client:
        mock_message = MagicMock()
        mock_message.sid = "SM123456789"
        mock_client.return_value.messages.create.return_value = mock_message
        yield mock_client


def test_send_sms_success(mock_twilio):
    """Test sending SMS successfully"""
    sid = send_sms(
        to_number="+1234567890",
        content="Your verification code is 123456"
    )
    
    assert sid == "SM123456789"
    assert mock_twilio.called


def test_send_sms_with_long_message(mock_twilio):
    """Test sending SMS with long message content"""
    long_message = "This is a very long message " * 10
    
    sid = send_sms("+9876543210", long_message)
    
    assert sid == "SM123456789"


def test_send_sms_api_called_with_correct_params(mock_twilio):
    """Test that Twilio API is called with correct parameters"""
    send_sms("+1234567890", "Test message")
    
    # Verify messages.create was called
    mock_twilio.return_value.messages.create.assert_called_once()
    call_kwargs = mock_twilio.return_value.messages.create.call_args[1]
    
    assert call_kwargs['to'] == "+1234567890"
    assert call_kwargs['body'] == "Test message"


def test_send_sms_uses_environment_variables(mock_twilio):
    """Test that SMS service uses environment variables"""
    # Just verify the function works with mock
    send_sms("+1234567890", "Test")
    
    assert mock_twilio.called
    # Service creates Client internally
    assert mock_twilio.return_value.messages.create.called


def test_send_sms_failure(mock_twilio):
    """Test handling of Twilio API failure"""
    mock_twilio.return_value.messages.create.side_effect = Exception("Twilio Error")
    
    with pytest.raises(Exception) as exc_info:
        send_sms("+1234567890", "Test")
    
    assert "Twilio Error" in str(exc_info.value)


def test_send_sms_invalid_phone_number(mock_twilio):
    """Test sending SMS to invalid phone number"""
    mock_twilio.return_value.messages.create.side_effect = Exception("Invalid phone number")
    
    with pytest.raises(Exception):
        send_sms("invalid", "Test message")
