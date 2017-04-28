/**
 * Created by Jorge Morayta on 11/23/2015.
 * The Karma Config contains the description to be used to run all the test available using Jasmine for TDSTM.
 */

module.exports = function (config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',

        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['browserify', 'jasmine'],


        // list of files / patterns to load in the browser
        files: [
            './node_modules/phantomjs-polyfill/bind-polyfill.js', // Support to bind on PhantomJS, will be at 2.0
            './web-app/app-js/config/App.js',
            './web-app/app-js/vendors/jquery/dist/jquery.min.js',
            './web-app/test/Hook/i18.base.spec.js', // Support to avoid Angular Js Translate issues
            './web-app/test/mockupData/**/*.json',
            './web-app/test/spec/**/*.js',
        ],

        /*
         reporters: ['progress', 'html'],

         htmlReporter: {
         outputFile: './web-app/test/units.html',

         pageTitle: 'Unit Tests Result',
         subPageTitle: 'TDSTM Execution Result test'
         },*/

        reporters: ['dots', 'junit'],

        junitReporter: {
            outputFile: '/web-app/test/test-results.xml'
        },

        // list of files to exclude
        exclude: [],

        client: {
            captureConsole: true,
            mocha: {
                bail: true
            }
        },
        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            './web-app/app-js/!(vendors)/**/*.js': ['browserify'],
            './web-app/test/**/*.js': ['browserify']
        },

        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['PhantomJS'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true,

        // Concurrency level
        // how many browser should be started simultanous
        concurrency: Infinity,

        browserify: {
            debug: true,
            transform: ['babelify', 'stringify']
        }
    })
}
