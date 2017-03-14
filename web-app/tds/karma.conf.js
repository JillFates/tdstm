module.exports = function(config) {

    var appBase    = 'web-app/specs-dist/';       // transpiled app JS and map files
    var appSrcBase = 'web-app/app-js/';       // app source TS files
    var appAssets  = 'web-app/app-js/'; // component assets fetched by Angular's compiler

    // Testing helpers (optional) are conventionally in a folder called `testing`
    var testingBase    = 'testing/'; // transpiled test JS and map files
    var testingSrcBase = 'testing/'; // test source TS files

    config.set({
        basePath: '',
        frameworks: ['jasmine'],

        plugins: [
            require('karma-jasmine'),
            require('karma-chrome-launcher'),
            require('karma-jasmine-html-reporter'),
            require('karma-junit-reporter')
        ],

        client: {
            builtPaths: [appBase, testingBase], // add more spec base paths as needed
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
            // System.js for module loading
            'node_modules/systemjs/dist/system.src.js',

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
            
            // { pattern: 'node_modules/@progress/kendo-angular-grid/dist/npm/**/*.js', included: false, watched: false },

            // RxJs
            { pattern: 'node_modules/rxjs/**/*.js', included: false, watched: false },
            { pattern: 'node_modules/rxjs/**/*.js.map', included: false, watched: false },

            // Paths loaded via module imports:
            // Angular itself
            { pattern: 'node_modules/@angular/**/*.js', included: false, watched: false },
            { pattern: 'node_modules/@angular/**/*.js.map', included: false, watched: false },

            { pattern: 'systemjs.config.js', included: false, watched: false },
            { pattern: 'systemjs.config.extras.js', included: false, watched: false },
            'karma-test-shim.js', // optionally extend SystemJS mapping e.g., with barrels

            // transpiled application & spec code paths loaded via module imports
            { pattern: appBase + '**/*.js', included: false, watched: true },
            { pattern: testingBase + '**/*.js', included: false, watched: true },


            // Asset (HTML & CSS) paths loaded via Angular's component compiler
            // (these paths need to be rewritten, see proxies section)
            { pattern: appSrcBase + '**/*.html', included: false, watched: true },
            { pattern: appSrcBase + '**/*.css', included: false, watched: true },

            // Paths for debugging with source maps in dev tools
            { pattern: appSrcBase + '**/*.ts', included: false, watched: false },
            { pattern: appBase + '**/*.js.map', included: false, watched: false },
            { pattern: testingSrcBase + '**/*.ts', included: false, watched: false },
            { pattern: testingBase + '**/*.js.map', included: false, watched: false},

            'node_modules/@progress/kendo-angular-grid/dist/cdn/js/kendo-angular-grid.js',
            'node_modules/@progress/kendo-angular-intl/dist/cdn/js/kendo-angular-intl.js',
            'node_modules/@progress/kendo-angular-l10n/dist/cdn/js/kendo-angular-l10n.js',
            'node_modules/@ng-bootstrap/ng-bootstrap/bundles/ng-bootstrap.js'
        ],

        // Proxied base paths for loading assets
        proxies: {
            // required for component assets fetched by Angular's compiler
            //"/web-app/": appAssets,
           // '/base/web-app/app-js/modules/games/games-list/games-list.component.html':"/base/web-app/specs-dist/modules/games/games-list/games-list.component.html",
            //'/base/web-app/specs-dist/':"/base/web-app/app-js/",//root development
            '/base/web-app/specs-dist/modules/tds/web-app/app-js/':"/base/web-app/app-js/",//sweet!!
            '/tds/web-app/app-js/':"/base/web-app/app-js/",
            '/base/web-app/specs-dist/modules/noticeManager/tds/web-app/app-js/':"/base/web-app/app-js/",
            '/base/web-app/specs-dist/modules/games/tds/web-app/app-js/':"/base/web-app/app-js/"
        },

        exclude: [],
        preprocessors: {},


        reporters: ['progress', 'kjhtml'],
        singleRun: false,

        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,
        browsers: ['Chrome'],

    })
}
