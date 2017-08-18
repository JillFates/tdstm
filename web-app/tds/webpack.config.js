/**
 * Created by jorge morayta on 6/30/2017.
 */
const webpack = require('webpack'); //to access built-in plugins
const path = require('path');

module.exports = {
    entry: './web-app/app-js/main.ts',

    output: {
        path: path.resolve(__dirname, './web-app/dist/'),
        filename: 'app.js'
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
        new webpack.optimize.UglifyJsPlugin({
            sourceMap: true
        })
    ],
    context: __dirname
};