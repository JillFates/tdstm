/**
 * Created by Jorge Morayta on 6/30/2017.
 */

const webpack = require('webpack'); //to access built-in plugins
const path = require('path');
const pkg = require('./package.json');  //loads npm config file
let BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = function(env, argv) {

    let devEnv = (env !== 'prod');

    console.log('Production Environment: ' + (!devEnv));

	return {
		entry: {
			app: './web-app/app-js/main.ts',
			vendor: Object.keys(pkg.dependencies) //get npm vendors deps from config
		},
		output: {
			path: path.resolve(__dirname, './web-app/dist/'),
			filename: '[name].js'
		},
		module: {
			rules: [
				{test: /\.ts$/, use: 'ts-loader'},
				{test: /\.ts$/, enforce: 'pre', loader: 'tslint-loader'}
			]
		},
		resolve: {
			extensions: ['.ts', '.tsx', '.js', '.jsx'],
			modules: [
				"node_modules",
				path.resolve(__dirname, "app-js")
			]
		},
		plugins: [
			new webpack.SourceMapDevToolPlugin({
				filename: '[name].js.map',
				exclude: ['vendor.js']
			}),
			new webpack.optimize.CommonsChunkPlugin({
				name: ['app', 'vendor']
			}),
			new webpack.optimize.UglifyJsPlugin({
				comments: false,
				sourceMap: devEnv,
				compress: {
					warnings: false, // Suppress uglification warnings
					pure_getters: true,
					unsafe: true,
					unsafe_comps: true,
					screw_ie8: true
				},
				exclude: [/\.min\.js$/gi] // skip pre-minified libs
			}),
			new webpack.ContextReplacementPlugin( //https://github.com/angular/angular/issues/11580
				/angular(\\|\/)core(\\|\/)@angular/,
				path.resolve(__dirname, "app-js")
			),
			//new BundleAnalyzerPlugin()
		],
		context: __dirname,
		watch: devEnv,
		watchOptions: {
			aggregateTimeout: 300,
			poll: 1000
		}
	}
};