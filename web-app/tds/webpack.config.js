/**
 * Created by Jorge Morayta on 6/30/2017.
 */
const webpack = require('webpack'); //to access built-in plugins
const path = require('path');
const pkg = require('./package.json');  //loads npm config file

module.exports = {
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
            {test: /\.ts$/, use: 'ts-loader'}
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
        new webpack.optimize.CommonsChunkPlugin({
            name: ['app', 'vendor']
        }),
        new webpack.optimize.UglifyJsPlugin({
            comments: false,
            sourceMap: false
        }),
        new webpack.ContextReplacementPlugin( //https://github.com/angular/angular/issues/11580
            /angular(\\|\/)core(\\|\/)@angular/,
            path.resolve(__dirname, '../src')
        )
    ],
    context: __dirname
};