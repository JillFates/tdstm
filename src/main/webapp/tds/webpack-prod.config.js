/**
 * Created by Jorge Morayta on 12/18/2018.
 */

const path = require('path');
const webpack = require('webpack');
let AngularCompilerPlugin = require( "@ngtools/webpack" ).AngularCompilerPlugin;

module.exports = function (env) {

	console.log('Production Environment');

	return {
		mode: 'production',
		entry: {
			app: './web-app/app-js/main.ts',
			polyfills: './web-app/app-js/polyfills.ts',
		},
		output: {
			path: path.resolve(__dirname, './web-app/dist/'),
			filename: '[name].js',
			chunkFilename: '[name].js',
			publicPath: '../tds/web-app/dist/'
		},
		resolve: {
			extensions: [".ts", ".tsx", ".js"]
		},
		module: {
			rules: [
				{test: /(\.ngfactory\.js|\.ngstyle\.js|\.ts)$/, loader: "@ngtools/webpack"},
				{test: /\.html$/i, loader: 'html-loader'},
				{test: /\.ts$/, enforce: 'pre', loader: 'tslint-loader'},
				// Ignore warnings about System.import in Angular
				{test: /[\/\\]@angular[\/\\].+\.js$/, parser: {system: true}},
			]
		},
		plugins: [
			new webpack.DefinePlugin({
				NODE_ENV: '"production"'
			}),
			new AngularCompilerPlugin({
				tsConfigPath: 'tsconfig.json',
				entryModule: 'web-app/app-js/app/tds-app.module#TDSAppModule',
				sourceMap: false
			})
		],
		cache: true,
		context: __dirname
	}
};