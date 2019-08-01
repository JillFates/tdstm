let webpackConfig = require('./webpack-prod.config')();
let webpack = require('webpack'); //to access built-in plugins
let path = require('path');
const AngularCompilerPlugin = require('@ngtools/webpack').AngularCompilerPlugin;

module.exports = function (config) {
    var appSrcBase = 'web-app/app-js/';       // app source TS files

    config.set({
        basePath: '',
        frameworks: ['jasmine'],
		files: [
			{pattern: 'node_modules/jquery/dist/jquery.min.js', included: true},
			{pattern: appSrcBase + '**/*.html'},
			{pattern: appSrcBase + 'main.spec.ts'}
		],
		plugins: [
			'karma-jasmine',
			'@angular-devkit/build-angular/plugins/karma',
			'karma-webpack',
			'karma-junit-reporter',
			'karma-phantomjs-launcher'
		],
		webpack: {
        	mode: 'production',
			module: webpackConfig.module,
			resolve: webpackConfig.resolve,
			plugins: [
				new webpack.ContextReplacementPlugin( //https://github.com/angular/angular/issues/11580
					/angular(\\|\/)core(\\|\/)@angular/,
					path.resolve(__dirname, "app-js")
				),
				new AngularCompilerPlugin({
					tsConfigPath: 'tsconfig.json',
					entryModule: 'web-app/app-js/app/tds-app.module#TDSAppModule',
					sourceMap: false,
					skipCodeGeneration: true
				})
			]
		},
		preprocessors: {
			'./web-app/app-js/main.spec.ts': ['webpack']
		},
		client: {
			captureConsole: true
		},
		mime: {'text/x-typescript': ['ts', 'tsx']},
		proxies: {
			'/base/web-app/specs-dist/modules/tds/web-app/app-js/': "/base/web-app/app-js/",//sweet!!
			'/tds/web-app/app-js/': "/base/web-app/app-js/"
		},
        reporters: ['dots', 'junit'],
        junitReporter: {
            outputDir: 'web-app/test/',
            outputFile: 'test-results.xml',
            useBrowserName: false
        },
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
		browsers: ['PhantomJS'],
        singleRun: true,
		jasmineNodeOpts: {
			defaultTimeoutInterval: 2500000
		},
    });
};
