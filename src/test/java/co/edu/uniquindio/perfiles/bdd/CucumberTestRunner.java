package co.edu.uniquindio.perfiles.bdd;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "co.edu.uniquindio.perfiles.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:build/reports/cucumber/cucumber-report.html, json:build/reports/cucumber/cucumber-report.json, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class CucumberTestRunner {
    // This class runs all Cucumber feature files with Allure reporting
}
