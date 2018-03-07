/**
 * Created by Jorge Morayta on 6/30/2017.
 */

const webpack = require('webpack'); //to access built-in plugins
const path = require('path');
const pkg = require('./package.json');  //loads npm config file
const helpers = require('./server-utils/helpers');
// let BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin; // Peek into dependencies

module.exports = function (env) {

	let devEnv = (env !== 'prod');

	console.log('Production Environment: ' + (!devEnv));

	return {
		mode: 'production',
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
			new webpack.ContextReplacementPlugin(
				/angular(\\|\/)core(\\|\/)(@angular|esm5)/,
				path.resolve(__dirname, "app-js")
			)
			// Uncomment if you want to take a peek to the structure of dependencies
			// new BundleAnalyzerPlugin()
		],
		optimization: {
			splitChunks: {
				name: true,
				cacheGroups: {
					commons: {
						test: /[\\/]node_modules[\\/]/,
						name: "vendor",
						chunks: "all"
					}
				}
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