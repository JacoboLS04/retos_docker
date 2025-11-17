# tests/test_app.py
import pytest
from unittest.mock import patch, MagicMock
from app import http_app, readiness_check, liveness_check


@pytest.fixture
def client():
    """Create Flask test client"""
    http_app.config['TESTING'] = True
    with http_app.test_client() as client:
        yield client


def test_health_endpoint_up(client):
    """Test /health endpoint returns UP status"""
    response = client.get('/health')
    
    assert response.status_code == 200
    data = response.get_json()
    
    assert data['status'] == 'UP'
    assert 'version' in data
    assert 'uptime' in data
    assert 'checks' in data
    assert len(data['checks']) == 2


def test_health_ready_endpoint(client):
    """Test /health/ready endpoint"""
    response = client.get('/health/ready')
    
    assert response.status_code == 200
    data = response.get_json()
    
    assert data['status'] == 'UP'
    assert len(data['checks']) == 1
    assert data['checks'][0]['name'] == 'Readiness check'


def test_health_live_endpoint(client):
    """Test /health/live endpoint"""
    response = client.get('/health/live')
    
    assert response.status_code == 200
    data = response.get_json()
    
    assert data['status'] == 'UP'
    assert len(data['checks']) == 1
    assert data['checks'][0]['name'] == 'Liveness check'


def test_readiness_check_function():
    """Test readiness_check helper function"""
    result = readiness_check()
    
    assert result['name'] == 'Readiness check'
    assert result['status'] == 'UP'
    assert 'data' in result
    assert result['data']['status'] == 'READY'


def test_liveness_check_function():
    """Test liveness_check helper function"""
    result = liveness_check()
    
    assert result['name'] == 'Liveness check'
    assert result['status'] == 'UP'
    assert 'data' in result
    assert result['data']['status'] == 'ALIVE'


def test_health_endpoint_includes_version(client):
    """Test that health endpoint includes service version"""
    response = client.get('/health')
    data = response.get_json()
    
    assert 'version' in data
    # Version should be a string
    assert isinstance(data['version'], str)


def test_health_endpoint_includes_uptime(client):
    """Test that health endpoint includes uptime"""
    response = client.get('/health')
    data = response.get_json()
    
    assert 'uptime' in data
    # Uptime should be in HH:MM:SS format
    assert ':' in data['uptime']


def test_health_ready_includes_version_and_uptime(client):
    """Test that ready endpoint includes version and uptime"""
    response = client.get('/health/ready')
    data = response.get_json()
    
    assert 'version' in data
    assert 'uptime' in data


def test_health_live_includes_version_and_uptime(client):
    """Test that live endpoint includes version and uptime"""
    response = client.get('/health/live')
    data = response.get_json()
    
    assert 'version' in data
    assert 'uptime' in data
