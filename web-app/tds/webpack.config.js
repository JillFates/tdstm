/**
 * Created by Jorge Morayta on 6/30/2017.
 */

const webpack = require('webpack'); //to access built-in plugins
const path = require('path');
const pkg = require('./package.json');  //loads npm config file
let BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = function (env, argv) {

	let devEnv = (env !== 'prod');

	console.log('Production Environment: ' + (!devEnv));

	return {
		mode: 'development',
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
				{test: /\.tsx?$/, loader: 'ts-loader'},
				{test: /\.ts$/, enforce: 'pre', loader: 'tslint-loader'}
			]
		},
		resolve: {
			extensions: ['.ts', '.tsx', '.js'],
			unsafeCache: true
		},
		plugins: [
			new webpack.SourceMapDevToolPlugin({
				filename: '[name].js.map'
			}),
			//new BundleAnalyzerPlugin()
		],
		optimization: {
			splitChunks: {
				name: true
			}
		},
		cache: true,
		context: __dirname,
		watch: devEnv,
		watchOptions: {
			ignored: /node_modules/,
			aggregateTimeout: 300,
			poll: 1000
		}
	}
};