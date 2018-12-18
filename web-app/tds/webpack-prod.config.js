/**
 * Created by Jorge Morayta on 12/18/2018.
 */

const path = require('path');
const webpack = require('webpack');

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
				{test: /\.tsx?$/, loader: 'ts-loader'},
				{test: /\.(ts|js)$/, loaders: ['angular-router-loader']},
				{test: /\.ts$/, enforce: 'pre', loader: 'tslint-loader'},
				// Ignore warnings about System.import in Angular
				{test: /[\/\\]@angular[\/\\].+\.js$/, parser: {system: true}},
			]
		},
		plugins: [
			new webpack.DefinePlugin({
				NODE_ENV: '"production"'
			}),
			new webpack.ContextReplacementPlugin(
				/\@angular(\\|\/)core(\\|\/)esm5/,
				path.resolve(__dirname, "app-js")
			)
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
		context: __dirname
	}
};