let webpackConfig = require('./webpack-dev.config')();
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
			{pattern: appSrcBase + 'main.spec.ts'},
		],
		plugins: [
			'karma-jasmine',
			'karma-html-reporter',
			'karma-jasmine-html-reporter',
			'karma-mocha-reporter',
			'karma-chrome-launcher',
			'@angular-devkit/build-angular/plugins/karma',
			'karma-webpack'
		],
		webpack: {
			mode: webpackConfig.mode,
			module: webpackConfig.module,
			resolve: webpackConfig.resolve,
			watchOptions: webpackConfig.watchOptions,
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
				}),
				new webpack.DefinePlugin({
					NODE_ENV: '"development"'
				}),
			]
		},
		preprocessors: {
			'./web-app/app-js/main.spec.ts': ['webpack']
		},
		client: {
			clearContext: false
		},
		customLaunchers: {
			Chrome_travis_ci: {
				base: 'Chrome',
				flags: ['--no-sandbox']
			}
		},
		mime: {'text/x-typescript': ['ts', 'tsx']},
		proxies: {
			'/base/web-app/specs-dist/modules/tds/web-app/app-js/': "/base/web-app/app-js/",//sweet!!
			'/tds/web-app/app-js/': "/base/web-app/app-js/"
		},
		reporters: ['kjhtml', 'html', 'mocha'],
		port: 9876,
		colors: true,
		logLevel: config.LOG_INFO,
		autoWatch: true,
		browsers: ['Chrome'],
		singleRun: false,
		jasmineNodeOpts: {
			defaultTimeoutInterval: 2500000
		},
	});
};
