var nodeResolve = require('resolve');

module.exports = function (config) {
  var appSrcBase = 'web-app/app-js/';       // app source TS files
  var appAssets = 'web-app/app-js/';

  config.set({
    basePath: '',
    frameworks: ['browserify', 'jasmine'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-phantomjs-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-junit-reporter'),
      require('karma-browserify'),
      require('karma-mocha-reporter')
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
      // External resources
      { pattern: 'node_modules/tinymce/skins/lightgray/**/*.*', included: false, watched: false },
      { pattern: 'node_modules/jquery/dist/jquery.min.js', included: true },
      { pattern: 'node_modules/tinymce/tinymce.js', included: true },
      { pattern: 'node_modules/tinymce/themes/modern/theme.min.js', included: true },
      // { pattern: 'node_modules/jzip/', included: true, watched: false },
      { pattern: appSrcBase + '**/*.html', included: false, watched: true },
      { pattern: appSrcBase + '**/*.css', included: false, watched: true },

      { pattern: './web-app/app-js/test.ts', watched: false }
    ],
    preprocessors: {
      './web-app/app-js/test.ts': ['browserify']
    },
    browserify: {
      debug: false,
      configure: function (bundle) {
        bundle.once('prebundle', function () {
          bundle.require(nodeResolve.sync('@progress/kendo-ooxml'), { expose: '@progress/kendo-ooxml' });
        });
      },
      plugin: ['tsify']
    },
    mime: {
      'text/x-typescript': ['ts', 'tsx']
    },
    proxies: {
      '/dist/js/vendors/tinymce/lightgray/': '/base/node_modules/tinymce/skins/lightgray/',
      '/base/web-app/specs-dist/modules/tds/web-app/app-js/': "/base/web-app/app-js/",//sweet!!
      '/tds/web-app/app-js/': "/base/web-app/app-js/",
      '/base/web-app/specs-dist/modules/noticeManager/tds/web-app/app-js/': "/base/web-app/app-js/"
    },
    phantomjsLauncher: {
      // Have phantomjs exit if a ResourceError is encountered (useful if karma exits without killing phantom) 
      exitOnResourceError: true
    },
    reporters: ['mocha', 'kjhtml'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['Chrome'],
    singleRun: false
  });
};