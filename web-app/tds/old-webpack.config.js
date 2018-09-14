/**
 * Created by Jorge Morayta on 6/30/2017.
 */

const webpack = require('webpack'); //to access built-in plugins
const path = require('path');
let BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin; // Peek into dependencies

module.exports = function (env) {

	let devEnv = (env !== 'prod');

	console.log('Production Environment: ' + (!devEnv));

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
			publicPath: '../'
		},
		module: {
			rules: [
				{test: /\.tsx?$/, loader: 'ts-loader'},
				{test: /\.ts$/, enforce: 'pre', loader: 'tslint-loader'},
				// Ignore warnings about System.import in Angular
				{test: /[\/\\]@angular[\/\\].+\.js$/, parser: {system: true}},
			]
		},
		resolve: {
			extensions: ['.ts', '.tsx', '.js'],
			unsafeCache: true
		},
		plugins: [
			new webpack.DefinePlugin({
				'process.env.NODE_ENV': '"production"'
			}),
			new webpack.SourceMapDevToolPlugin({
				filename: '[name].js.map'
			}),
			new webpack.ContextReplacementPlugin(
				/\@angular(\\|\/)core(\\|\/)esm5/,
				path.resolve(__dirname, "app-js")
			),
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
						chunks: 'all',
						test(module, chunks) {
							const name = module.nameForCondition && module.nameForCondition();
							return chunks.some(chunk => {
								return (chunk.name === 'app' || chunk.name === 'polyfills') && /[\\/]node_modules[\\/]/.test(name);
							});
						}
					},
					codemirror: {
						name: 'codemirror',
						chunks: chunk => chunk.name == 'codemirror',
						priority: 1,
						enforce: true,
						test(module, chunks) {
							return chunks.some((chunk) =>  {
								return chunk.name === 'codemirror'
							});
						}
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