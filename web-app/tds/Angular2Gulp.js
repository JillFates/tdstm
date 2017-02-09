/**
 * Created by Jorge Morayta on 2/3/2017.
 */

var gulp = require('gulp'),
    browserify = require("browserify"),
    tsify = require("tsify"),
    source = require('vinyl-source-stream'),
    shell = require('gulp-shell');


gulp.task('build', ['build-app', 'build-vendor']);

gulp.task('build-app', function () {

    return browserify()
        .add("web-app/app-js/main.ts")
        .plugin("tsify")
        .bundle()
        .pipe(source('app.js'))
        .pipe(gulp.dest('./web-app/dist/'));

});

gulp.task('build-vendor', function () {

    return browserify()
        .add("web-app/app-js/vendor.ts")
        .plugin("tsify")
        .bundle()
        .pipe(source('vendor.js'))
        .pipe(gulp.dest('./web-app/dist/'));

});

gulp.task('typescript-compile', shell.task(['tsc -p web-app']));

gulp.task('build-test',['typescript-compile'], shell.task(['karma start karma.conf.js']));