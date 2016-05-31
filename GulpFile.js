'use strict';

var autoprefixer = require('gulp-autoprefixer'),
    gulp = require('gulp'),
    gulpif = require('gulp-if'),
    sass = require('gulp-sass'),
    sourcemaps = require('gulp-sourcemaps');



/**
 * What this task do is to compile the SASS file and generate a map that can be view from moder browser
 * style.css is being created, if prodEnv is true, then minified the css for use on prodEnv.
 * from command line: gulp sass-compiler --PROD
 */
gulp.task('sass-compiler', function () {
    return gulp.src('web-app/css/style.sass')
        .pipe(sourcemaps.init())
        .pipe(sass({errLogToConsole: true}))
        .pipe(autoprefixer({browsers: ['last 2 version'], cascade: false}))
        .pipe(sourcemaps.write())
        .pipe(gulp.dest('web-app/css'));
});

/**
 * Execute watch task to compile css automatically on Development mode
 * from command line: gulp sass:watch
 * it will run until stop, searching for changes on any SASS file, compiles and ready to use
 */
gulp.task('sass:watch', function () {
    return gulp.watch('web-app/css/**/*.sass', ['sass-compiler']);
});

