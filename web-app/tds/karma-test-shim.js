/**
 * Created by aaferreira on 09/02/2017.
 */
// /*global jasmine, __karma__, window*/
Error.stackTraceLimit = 0; // "No stacktrace"" is usually best for app testing.

// Uncomment to get full stacktrace output. Sometimes helpful, usually not.
// Error.stackTraceLimit = Infinity; //

jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000;

// builtPaths: root paths for output ("built") files
// get from karma.config.js, then prefix with '/base/' (default is 'app/')
var builtPaths = (__karma__.config.builtPaths || ['app/'])
    .map(function (p) { return '/base/' + p; });

__karma__.loaded = function () { };

function isJsFile(path) {
    return path.slice(-3) == '.js';
}

function isSpecFile(path) {
    return /\.spec\.(.*\.)?js$/.test(path);
}

// Is a "built" file if is JavaScript file in one of the "built" folders
function isBuiltFile(path) {
    return isJsFile(path) &&
        builtPaths.reduce(function (keep, bp) {
            return keep || (path.substr(0, bp.length) === bp);
        }, false);
}

var allSpecFiles = Object.keys(window.__karma__.files)
    .filter(isSpecFile)
    .filter(isBuiltFile);

function onlyAppFiles(filePath) {
    return /\/base\/dist\/(?!.*\.spec\.js$).*\.js$/.test(filePath);
}

function createPathRecords(pathsMapping, appPath) {
    // creates local module name mapping to global path with karma's fingerprint in path, e.g.:
    // './vg-player/vg-player':
    // '/base/dist/vg-player/vg-player.js?f4523daf879cfb7310ef6242682ccf10b2041b3e'
    var pathParts = appPath.split('/');
    var moduleName = './' + pathParts.slice(Math.max(pathParts.length - 2, 1)).join('/');
    moduleName = moduleName.replace(/\.js$/, '');
    pathsMapping[moduleName] = appPath + '?' + window.__karma__.files[appPath];
    return pathsMapping;
}

System.config({
    baseURL: 'base',
    // Extend usual application package list with test folder
    packages: { 'testing': { main: 'index.js', defaultExtension: 'js' } },

    // Assume npm: is set in `paths` in systemjs.config
    // Map the angular testing umd bundles
    map: {
        '@angular/core/testing': 'npm:@angular/core/bundles/core-testing.umd.js',
        '@angular/common/testing': 'npm:@angular/common/bundles/common-testing.umd.js',
        '@angular/compiler/testing': 'npm:@angular/compiler/bundles/compiler-testing.umd.js',
        '@angular/platform-browser/testing': 'npm:@angular/platform-browser/bundles/platform-browser-testing.umd.js',
        '@angular/platform-browser-dynamic/testing': 'npm:@angular/platform-browser-dynamic/bundles/platform-browser-dynamic-testing.umd.js',
        '@angular/http/testing': 'npm:@angular/http/bundles/http-testing.umd.js',
        '@angular/router/testing': 'npm:@angular/router/bundles/router-testing.umd.js',
        '@angular/forms/testing': 'npm:@angular/forms/bundles/forms-testing.umd.js',
        '@progress/kendo-angular-grid': 'npm:@progress/kendo-angular-grid/dist/cdn/js/kendo-angular-grid.js',
        '@progress/kendo-angular-intl':'npm:@progress/kendo-angular-intl/dist/cdn/js/kendo-angular-intl.js',
        '@progress/kendo-angular-l10n':'npm:@progress/kendo-angular-l10n/dist/cdn/js/kendo-angular-l10n.js',
        '@ng-bootstrap/ng-bootstrap': 'npm:@ng-bootstrap/ng-bootstrap/bundles/ng-bootstrap.js'

    },
});

System.import('systemjs.config.js')
    .then(importSystemJsExtras)
    .then(initTestBed)
    .then(initTesting);

/** Optional SystemJS configuration extras. Keep going w/o it */
function importSystemJsExtras() {
    return System.import('systemjs.config.extras.js')
        .catch(function (reason) {
            console.log(
                'Warning: System.import could not load the optional "systemjs.config.extras.js". Did you omit it by accident? Continuing without it.'
            );
            console.log(reason);
        });
}

function initTestBed() {
    return Promise.all([
        System.import('@angular/core/testing'),
        System.import('@angular/platform-browser-dynamic/testing')
    ])

        .then(function (providers) {
            var coreTesting = providers[0];
            var browserTesting = providers[1];

            coreTesting.TestBed.initTestEnvironment(
                browserTesting.BrowserDynamicTestingModule,
                browserTesting.platformBrowserDynamicTesting());
        })
}

// Import all spec files and start karma
function initTesting() {
    return Promise.all(
        allSpecFiles.map(function (moduleName) {
            return System.import(moduleName);
        })
    )
        .then(__karma__.start, __karma__.error);
}
