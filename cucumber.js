module.exports = {
  default: {
    require: ['features/step_definitions/**/*.js'],
    format: [
      'progress-bar',
      'html:reports/cucumber-report.html',
      'json:reports/cucumber-report.json'
    ],
    publishQuiet: true
  }
};
