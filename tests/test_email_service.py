# tests/test_email_service.py
import pytest
from unittest.mock import patch, MagicMock
from services.email_service import send_email


@pytest.fixture
def mock_sendgrid():
    """Mock SendGrid API client"""
    with patch('services.email_service.SendGridAPIClient') as mock_sg:
        mock_response = MagicMock()
        mock_response.status_code = 202
        mock_sg.return_value.send.return_value = mock_response
        yield mock_sg


def test_send_email_success(mock_sendgrid):
    """Test sending email successfully"""
    status = send_email(
        to_email="recipient@example.com",
        subject="Test Subject",
        content="<p>Test content</p>"
    )
    
    assert status == 202
    assert mock_sendgrid.called


def test_send_email_with_html_content(mock_sendgrid):
    """Test sending email with HTML content"""
    html_content = "<html><body><h1>Hello World</h1></body></html>"
    
    status = send_email(
        to_email="test@example.com",
        subject="HTML Email",
        content=html_content
    )
    
    assert status == 202


def test_send_email_api_called_with_correct_params(mock_sendgrid):
    """Test that SendGrid API is called with correct parameters"""
    with patch('services.email_service.Mail') as mock_mail:
        send_email(
            to_email="user@example.com",
            subject="Welcome",
            content="Welcome to our platform"
        )
        
        # Verify Mail was created with correct params
        mock_mail.assert_called_once()
        call_kwargs = mock_mail.call_args[1]
        assert call_kwargs['to_emails'] == "user@example.com"
        assert call_kwargs['subject'] == "Welcome"
        assert call_kwargs['html_content'] == "Welcome to our platform"


@patch.dict('os.environ', {'SENDGRID_API_KEY': 'test_key', 'SENDER_EMAIL': 'sender@test.com'})
def test_send_email_uses_environment_variables(mock_sendgrid):
    """Test that email service uses environment variables"""
    send_email("to@example.com", "Subject", "Content")
    
    assert mock_sendgrid.called


def test_send_email_failure(mock_sendgrid):
    """Test handling of SendGrid API failure"""
    mock_sendgrid.return_value.send.side_effect = Exception("API Error")
    
    with pytest.raises(Exception) as exc_info:
        send_email("test@example.com", "Subject", "Content")
    
    assert "API Error" in str(exc_info.value)
