/**
 * Created by Jorge Morayta on 2/3/2017.
 */

let _ = require('lodash');
let autoPreFixer = require('gulp-autoprefixer');
let gulp = require('gulp');
let sass = require('gulp-sass');
let sourceMaps = require('gulp-sourcemaps');
let shell = require('gulp-shell');

/**
 * What this task do is to compile the SASS file and generate a map that can be view from modern browser
 */
gulp.task('sass-compiler', function () {
	return gulp.src('./web-app/css/style.sass')
		.pipe(sass({errLogToConsole: true}))
		.pipe(autoPreFixer({browsers: ['last 2 version'], cascade: false}))
		.pipe(gulp.dest('./web-app/css'));
});

gulp.task('sass-compiler-manager', function () {
	return gulp.src('./web-app/css/managerStyle.sass')
		.pipe(sass({errLogToConsole: true}))
		.pipe(autoPreFixer({browsers: ['last 2 version'], cascade: false}))
		.pipe(gulp.dest('./web-app/css'));
});

/**
 * Execute watch task to compile css automatically on Development mode
 * from command line: gulp sass:watch
 * it will run until stop, searching for changes on any SASS file, compiles and ready to use
 */
gulp.task('sass:watch', function () {
	return gulp.watch('./web-app/css/**/*.sass', function () {
		gulp.src('./web-app/css/style.sass')
			.pipe(sourceMaps.init())
			.pipe(sass({errLogToConsole: true}))
			.pipe(autoPreFixer({browsers: ['last 2 version'], cascade: false}))
			.pipe(sourceMaps.write())
			.pipe(gulp.dest('./web-app/css'))
	});
});

gulp.task('build-test', shell.task(['karma start karma.conf.js']));
gulp.task('build-test-report', shell.task(['karma start karma.production.conf.js']));
gulp.task('build-dev', shell.task(['npm run build-dev']));
gulp.task('build-prod', shell.task(['npm run build-prod']));