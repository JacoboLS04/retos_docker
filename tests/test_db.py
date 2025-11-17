# tests/test_db.py
import pytest
from unittest.mock import patch, MagicMock
from db import save_notification_log


@pytest.fixture
def mock_db_connection():
    """Mock database connection"""
    with patch('db.get_conn') as mock_conn:
        mock_cursor = MagicMock()
        mock_conn.return_value.cursor.return_value = mock_cursor
        yield mock_conn, mock_cursor


def test_save_notification_log_success(mock_db_connection):
    """Test saving notification log with successful status"""
    mock_conn, mock_cursor = mock_db_connection
    
    save_notification_log(
        channel="EMAIL",
        recipient={"email": "test@example.com"},
        payload={"subject": "Test", "body": "Test content"},
        status="SENT",
        error=None
    )
    
    # Verify execute was called
    assert mock_cursor.execute.called
    # Verify close was called
    assert mock_cursor.close.called


def test_save_notification_log_with_error(mock_db_connection):
    """Test saving notification log with error status"""
    mock_conn, mock_cursor = mock_db_connection
    
    save_notification_log(
        channel="SMS",
        recipient={"phone": "+1234567890"},
        payload={"body": "Test SMS"},
        status="FAILED",
        error="Network timeout"
    )
    
    assert mock_cursor.execute.called
    call_args = mock_cursor.execute.call_args
    
    # Verify the error is passed to the query
    assert "Network timeout" in str(call_args)


def test_save_notification_log_email_channel(mock_db_connection):
    """Test saving email notification log"""
    mock_conn, mock_cursor = mock_db_connection
    
    save_notification_log(
        channel="EMAIL",
        recipient={"email": "user@example.com"},
        payload={"subject": "Welcome", "body": "<h1>Welcome!</h1>"},
        status="SENT",
        error=None
    )
    
    assert mock_cursor.execute.called
    assert mock_cursor.close.called


def test_save_notification_log_sms_channel(mock_db_connection):
    """Test saving SMS notification log"""
    mock_conn, mock_cursor = mock_db_connection
    
    save_notification_log(
        channel="SMS",
        recipient={"phone": "+9876543210"},
        payload={"body": "Your verification code is 123456"},
        status="SENT",
        error=None
    )
    
    assert mock_cursor.execute.called
    assert mock_cursor.close.called
