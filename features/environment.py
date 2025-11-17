# features/environment.py
"""Behave environment configuration with Allure support"""
import os
from allure_behave.hooks import allure_report


def before_all(context):
    """Setup before all tests"""
    # Initialize Allure report
    context.allure_report = allure_report
    pass

def after_all(context):
    """Cleanup after all tests"""
    pass

def before_scenario(context, scenario):
    """Setup before each scenario"""
    context.email_sent = False
    context.sms_sent = False
    context.error = None

def after_scenario(context, scenario):
    """Cleanup after each scenario"""
    # Clean up any mocks
    if hasattr(context, 'mock_sendgrid') and context.mock_sendgrid:
        try:
            context.mock_sendgrid.stop()
        except:
            pass
    
    if hasattr(context, 'mock_twilio') and context.mock_twilio:
        try:
            context.mock_twilio.stop()
        except:
            pass
