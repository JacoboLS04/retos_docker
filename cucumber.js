module.exports = {
  default: {
    requireModule: ['ts-node/register'],
    require: ['features/support/**/*.ts'],
    format: [
      'progress-bar',
      'html:reports/cucumber-report.html',
      'json:reports/cucumber-report.json',
      'node_modules/allure-cucumberjs'
    ],
    formatOptions: {
      snippetInterface: 'async-await',
      resultsDir: 'allure-results'
    },
    publishQuiet: true
  }
};
