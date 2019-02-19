/**
 * Created by Jorge Morayta on 13/09/2018.
 */

const path = require('path');
const webpack = require('webpack');

let BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin; // Peek into dependencies

module.exports = function (env) {

	console.log('Development Environment');

	return {
		mode: 'development',
		devtool: false,
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
				{test: /\.tsx?$/, loader: 'ts-loader'},
				{test: /\.(ts|js)$/, loaders: ['angular-router-loader']},
				{test: /\.ts$/, enforce: 'pre', loader: 'tslint-loader'},
				// Ignore warnings about System.import in Angular
				{test: /[\/\\]@angular[\/\\].+\.js$/, parser: {system: true}},
			]
		},
		plugins: [
			new webpack.DefinePlugin({
				NODE_ENV: '"development"'
			}),
			new webpack.SourceMapDevToolPlugin({
				filename: '[name].js.map',
				exclude: ['vendor.js', 'polyfills.js']
			}),
			new webpack.ContextReplacementPlugin(
				/\@angular(\\|\/)core(\\|\/)fesm5/,
				path.resolve(__dirname, "app-js")
			)
			// Uncomment if you want to take a peek to the structure of dependencies
			// new BundleAnalyzerPlugin()
		],
		optimization: {
			splitChunks: {
				automaticNameDelimiter: '-',
				cacheGroups: {
					commons: {
						test: /[\\/]node_modules[\\/]/,
						name: "vendor",
						chunks: 'all'
					}
				}
			}
		},
		cache: true,
		context: __dirname,
		watch: true,
		watchOptions: {
			ignored: /node_modules/,
			aggregateTimeout: 300,
			poll: 1000
		}
	}
};