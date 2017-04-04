module.exports = function (config) {
  var appSrcBase = 'web-app/app-js/';       // app source TS files
  var appAssets = 'web-app/app-js/';

  config.set({
    basePath: '',
    frameworks: ['browserify', 'jasmine'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-junit-reporter'),
      require('karma-browserify')
    ],
    client: {
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    customLaunchers: {
      // From the CLI. Not used here but interesting
      // chrome setup for travis CI using chromium
      Chrome_travis_ci: {
        base: 'Chrome',
        flags: ['--no-sandbox']
      }
    },
    files: [
      // Polyfills
      'node_modules/core-js/client/shim.js',
      // zone.js
      'node_modules/zone.js/dist/zone.js',
      'node_modules/zone.js/dist/long-stack-trace-zone.js',
      'node_modules/zone.js/dist/proxy.js',
      'node_modules/zone.js/dist/sync-test.js',
      'node_modules/zone.js/dist/jasmine-patch.js',
      'node_modules/zone.js/dist/async-test.js',
      'node_modules/zone.js/dist/fake-async-test.js',

      { pattern: appSrcBase + '**/*.html', included: false, watched: true },
      { pattern: appSrcBase + '**/*.css', included: false, watched: true },

      { pattern: './web-app/app-js/test.ts', watched: false }
    ],
    preprocessors: {
      './web-app/app-js/test.ts': ['browserify']
    },
    browserify: {
      debug: false,
      plugin: ['tsify']
    },
    mime: {
      'text/x-typescript': ['ts', 'tsx']
    },
    proxies: {
      '/base/web-app/specs-dist/modules/tds/web-app/app-js/': "/base/web-app/app-js/",//sweet!!
      '/tds/web-app/app-js/': "/base/web-app/app-js/",
      '/base/web-app/specs-dist/modules/noticeManager/tds/web-app/app-js/': "/base/web-app/app-js/"
    },
    reporters: ['progress', 'kjhtml'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['Chrome'],
    singleRun: false
  });
};