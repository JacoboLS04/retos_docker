# features/steps/health_steps.py
from behave import when, then
from app import http_app


@when('I request the service health status')
def step_request_health(context):
    with http_app.test_client() as client:
        context.response = client.get('/health')
        context.response_data = context.response.get_json()


@when('I check the readiness endpoint')
def step_check_readiness(context):
    with http_app.test_client() as client:
        context.response = client.get('/health/ready')
        context.response_data = context.response.get_json()


@when('I check the liveness endpoint')
def step_check_liveness(context):
    with http_app.test_client() as client:
        context.response = client.get('/health/live')
        context.response_data = context.response.get_json()


@then('the health status should be "{status}"')
def step_health_status(context, status):
    assert context.response_data['status'] == status, \
        f"Expected {status}, got {context.response_data['status']}"


@then('the readiness status should be "{status}"')
def step_readiness_status(context, status):
    assert context.response_data['status'] == status


@then('the liveness status should be "{status}"')
def step_liveness_status(context, status):
    assert context.response_data['status'] == status


@then('the response should include readiness check')
def step_includes_readiness(context):
    checks = context.response_data.get('checks', [])
    readiness_checks = [c for c in checks if 'Readiness' in c['name']]
    assert len(readiness_checks) > 0, "Readiness check not found"


@then('the response should include liveness check')
def step_includes_liveness(context):
    checks = context.response_data.get('checks', [])
    liveness_checks = [c for c in checks if 'Liveness' in c['name']]
    assert len(liveness_checks) > 0, "Liveness check not found"


@then('the response should include uptime information')
def step_includes_uptime(context):
    assert 'uptime' in context.response_data, "Uptime not found in response"


@then('the response should confirm the service is alive')
def step_confirms_alive(context):
    checks = context.response_data.get('checks', [])
    assert len(checks) > 0
    assert checks[0]['data']['status'] == 'ALIVE'


@then('the response should include version number')
def step_includes_version(context):
    assert 'version' in context.response_data, "Version not found in response"
    assert isinstance(context.response_data['version'], str)


@then('the response should include uptime in human readable format')
def step_uptime_human_readable(context):
    uptime = context.response_data.get('uptime')
    assert uptime is not None
    # Should be in HH:MM:SS format
    assert ':' in uptime, "Uptime should contain colons"
