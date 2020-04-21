/**
 * Created by Jorge Morayta on 13/09/2018.
 */

const path = require('path');
const webpack = require('webpack');
const CopyPlugin = require('copy-webpack-plugin');
let AngularCompilerPlugin = require( "@ngtools/webpack" ).AngularCompilerPlugin;
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
				{test: /(\.ngfactory\.js|\.ngstyle\.js|\.ts)$/, loader: "@ngtools/webpack"},
				{test: /\.html$/i, loader: 'html-loader'},
				{test: /\.ts$/, enforce: 'pre', loader: 'tslint-loader'},
				// Ignore warnings about System.import in Angular
				{test: /[\/\\]@angular[\/\\].+\.js$/, parser: {system: true}},
			]
		},
		optimization: {
			splitChunks: {
				cacheGroups: {
					commons: {
						test: /[\\/]node_modules[\\/]/,
						name: "vendor",
						chunks: "all"
					}
				}
			}
		},
		plugins: [
			new CopyPlugin([
				{ from: 'web-app/assets/modules/clarity/js/clr-icons.min.js', to: 'clr-icons.min.js'},
				{ from: 'web-app/assets/modules/webcomponents/js/custom-elements.min.js', to: 'custom-elements.min.js'}
			]),
			new webpack.DefinePlugin({
				NODE_ENV: '"development"'
			}),
			new webpack.SourceMapDevToolPlugin({
				filename: '[file].map',
				exclude: ['vendor.js', 'polyfills.js']
			}),
			new webpack.SourceMapDevToolPlugin({
				filename: '[file].map',
				include: ['custom-elements.min.js']
			}),
			new AngularCompilerPlugin({
				tsConfigPath: 'tsconfig.json',
				entryModule: 'web-app/app-js/app/tds-app.module#TDSAppModule',
				sourceMap: true,
				skipCodeGeneration: true
			})
			// Uncomment if you want to take a peek to the structure of dependencies
			// new BundleAnalyzerPlugin()
		],
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