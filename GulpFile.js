'use strict';

var _ = require('lodash'),
    argv = require('yargs').argv,
    autoprefixer = require('gulp-autoprefixer'),
    babelify = require('babelify'),
    browserify = require('browserify'),
    buffer = require('vinyl-buffer'),
    concat = require('gulp-concat'),
    del = require('del'),
    fs = require('fs'),
    gulp = require('gulp'),
    gulpFilter = require('gulp-filter'),
    gulpif = require('gulp-if'),
    jshint = require('gulp-jshint'),
    nodeResolve = require('resolve'),
    replace = require('gulp-replace'),
    rename = require('gulp-rename'),
    sass = require('gulp-sass'),
    source = require('vinyl-source-stream'),
    sourcemaps = require('gulp-sourcemaps'),
    uglify = require('gulp-uglify'),
    watchify = require('watchify');

/* Command line arg, e.g.: gulp --PROD */
var prodEnv = argv.PROD === true;
//prodEnv = true;
console.log('Production environment = ' + prodEnv);

var taskPath = "./gulp-tasks";

/**
 * Main Build Process
 * It will always run as PROD mode enabled, it also don't execute any Watchify task and the result file is minified
 */
gulp.task('build', ['build-app', 'build-vendor']);

gulp.task('build-app', function () {
    var generatedFile = 'web-app/dist/App.js',
        browserifyProcesor;

    if (fs.existsSync(generatedFile)) {
        console.error('Clean old App.js compiled file.');
        fs.unlink(generatedFile);
    }

    validateJS('web-app/app-js/**/*.js');

    console.error('Compiling App.js file.');

    browserifyProcesor = browserify({
        entries: ['./web-app/app-js/main.js'],
        debug: !prodEnv, // SourceMapping
    }).transform(babelify.configure());

    // Exclude all NPM Package from the build
    getNPMPackageIds().forEach(function (id) {
        browserifyProcesor.external(id);
    });

    var stream = browserifyProcesor.bundle().on("error", function (error) {
            onBuildError(error, generatedFile, prodEnv);
            this.emit("end");
        })
        .pipe(source('App.js'))
        .pipe(gulpif(prodEnv, buffer()))
        .pipe(gulpif(prodEnv, uglify()))
        .pipe(gulp.dest('./web-app/dist/'));

    return stream;

});

gulp.task('build-vendor', function () {
    var generatedFile = 'web-app/dist/Vendors.js',
        b;

    if (fs.existsSync(generatedFile)) {
        console.error('Clean old Vendors.js compiled file.');
        fs.unlink(generatedFile);
    }

    console.error('Compiling Vendors.js file.');

    b = browserify({
        debug: false, // SourceMapping
    });

    // Include all NPM Package from the build
    getNPMPackageIds().forEach(function (id) {
        b.require(nodeResolve.sync(id), {expose: id});
    });

    var stream = b.bundle().on("error", function (error) {
            onBuildError(error, generatedFile, prodEnv);
            this.emit("end");
        })
        .pipe(source('Vendors.js'))
        .pipe(gulpif(true, buffer()))
        .pipe(gulpif(true, uglify()))
        .pipe(gulp.dest('./web-app/dist/'));

    return stream;
});


/**
 * Main Watchify Build Process
 * To run on Prod use gulp build --PROD otherwhise will run on DevMode by default
 * PROD mode will remove the sourcemapping
 */
gulp.task('watch-build', function () {
    var generatedFile = 'web-app/dist/App.js',
        b = browserify({
            entries: ['./web-app/app-js/main.js'],
            debug: true,
        })
            .transform(babelify.configure());

    validateJS('web-app/app-js/**/*.js');

    // Exclude all NPM Package from the build
    getNPMPackageIds().forEach(function (id) {
        b.external(id);
    })

    var watcher = watchify(b, {
        poll: true
    });

    return watcher
    // wait until any change is triggered to recreate the App.js file
        .on('update', function () {
            validateJS('web-app/app-js/**/*.js');
            var updateStart = Date.now();
            console.log('Building ./web-app/dist/App.js');
            watcher.bundle()
                .on("error", function (error) {
                    onBuildError(error, generatedFile);
                    this.emit("end");
                })
                .pipe(source('App.js'))
                .pipe(gulp.dest('./web-app/dist/'));
            console.log('Build success! in ', (Date.now() - updateStart) + 'ms');
        })
        .bundle()
        .on("error", function (error) {
            onBuildError(error, generatedFile);
            this.emit("end");
        })
        .pipe(source('App.js'))
        .pipe(gulp.dest('./web-app/dist/'));
});

/**
 * Verified the quality of the code after and report any error found on it
 */
gulp.task('js-hint-tool', function (cb) {
    validateJS('web-app/app-js/**/*.js');
});

/**
 * What this task do is to compile the SASS file and generate a map that can be view from moder browser
 * style.css is being created, if prodEnv is true, then minify the css for use on prodEnv.
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

gulp.task('sass-compiler-manager', function () {
    return gulp.src('web-app/css/managerStyle.sass')
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
    return gulp.watch('web-app/css/**/*.sass', ['sass-compiler', 'sass-compiler-manager']);
});

/**
 * Runs JSHint code checker tool on the given source.
 *
 * @param src specifying which files to check
 * @return {*}
 */
function validateJS(src) {
    console.log('Running Quality Code checker for ' + src);
    var filter = gulpFilter(['**/*.*', '!**/vendors/**']);
    return gulp.src([src], {base: 'web-app'})
        .pipe(filter)
        .pipe(jshint())
        .pipe(jshint.reporter('default', {verbose: true}))
        .pipe(jshint.reporter('fail')).on('error', function (error) {
            console.log(error.toString());
        })
        .pipe(filter.restore());
}

/**
 * Helper method to catch compilation errors
 * @param error
 * @param generatedFile
 */
var onBuildError = function (error, generatedFile, prodEnv) {
    if (fs.existsSync(generatedFile)) {
        console.error('Removing ' + generatedFile + ' file.');
        fs.unlink(generatedFile);
    }
    console.error('Error generating the App.js file', error.message);

};


/**
 * Read the package.json + node_modules to match the Dependecy section
 * @returns {Array}
 */
function getNPMPackageIds() {
    // read package.json and get dependencies' package ids
    var packageManifest = {};
    try {
        packageManifest = require('./package.json');
    } catch (e) {
        // does not have a package.json manifest
    }
    return _.keys(packageManifest.dependencies) || [];
}


/**
 * Add any Sub Task Process
 */

require(taskPath + '/es6-compile')();