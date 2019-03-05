/**
 * Created by Jorge Morayta on 2/3/2017.
 */

let _ = require('lodash');
let autoPreFixer = require('gulp-autoprefixer');
let gulp = require('gulp');
let sass = require('gulp-sass');
let shell = require('gulp-shell');

gulp.task('sass-main-style', function () {
	return gulp.src('../../../../grails-app/assets/stylesheets/css/tds-style.sass')
		.pipe(sass({errLogToConsole: true}))
		.pipe(autoPreFixer({browsers: ['last 2 version'], cascade: false}))
		.pipe(gulp.dest('../../../../grails-app/assets/stylesheets/css'));
});

gulp.task('sass-manager-compiler', function () {
	return gulp.src('../../../../grails-app/assets/stylesheets/css/managerStyle.sass')
		.pipe(sass({errLogToConsole: true}))
		.pipe(autoPreFixer({browsers: ['last 2 version'], cascade: false}))
		.pipe(gulp.dest('../../../../grails-app/assets/stylesheets/css'));
});

gulp.task('sass-angular-compiler', function () {
	return gulp.src('./web-app/css/tds-style.sass')
		.pipe(sass({errLogToConsole: true}))
		.pipe(autoPreFixer({browsers: ['last 2 version'], cascade: false}))
		.pipe(gulp.dest('./web-app/css'));
});

gulp.task('sass-angular-manager-compiler', function () {
	return gulp.src('./web-app/css/managerStyle.scss')
		.pipe(sass({errLogToConsole: true}))
		.pipe(autoPreFixer({browsers: ['last 2 version'], cascade: false}))
		.pipe(gulp.dest('./web-app/css'));
});

gulp.task('build-test', shell.task(['karma start karma.conf.js']));
gulp.task('build-test-report', shell.task(['karma start karma.production.conf.js']));
gulp.task('build-dev', shell.task(['npm run build-dev']));
gulp.task('build-prod', shell.task(['npm run build-prod']));