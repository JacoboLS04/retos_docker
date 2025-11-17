module.exports = {
  default: {
    requireModule: ['ts-node/register'],
    require: ['features/support/**/*.ts'],
    format: [
      'progress-bar',
      'html:reports/cucumber-report.html',
      'json:reports/cucumber-report.json',
      'allure-cucumberjs'
    ],
    formatOptions: {
      snippetInterface: 'async-await',
      allureReport: {
        resultsDir: 'allure-results'
      }
    },
    publishQuiet: true
  }
};
