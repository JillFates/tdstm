/**
 * Created by Jorge Morayta on 2/3/2017.
 */

var _ = require('lodash'),
    argv = require('yargs').argv,
    autoprefixer = require('gulp-autoprefixer'),
    browserify = require("browserify"),
    buffer = require('vinyl-buffer'),
    del = require('del'),
    fs = require('fs'),
    gulp = require('gulp'),
    gulpif = require('gulp-if'),
    gulpFilter = require('gulp-filter'),
    nodeResolve = require('resolve'),
    sass = require('gulp-sass'),
    source = require('vinyl-source-stream'),
    sourcemaps = require('gulp-sourcemaps'),
    shell = require('gulp-shell'),
    tsify = require("tsify"),
    tslint = require("gulp-tslint"),
    uglify = require('gulp-uglify'),
    watchify = require('watchify');

/* Command line arg, e.g.: gulp --PROD */
var prodEnv = argv.PROD === true;
//prodEnv = true;
console.log('Production environment = ' + prodEnv);

gulp.task('build', ['build-app', 'build-vendor']);

/**
 * Compiles the main source from main.ts and their dependencies
 * The source mapping is added by default, unless is compiled in PROD mode.
 */
gulp.task('build-app', function () {

    var generatedFile = 'web-app/dist/app.js';
    var browserifyProcesor;

    if (fs.existsSync(generatedFile)) {
        console.log('Cleaning previous compiled app.js file.');
        fs.unlink(generatedFile);
    }

    validateJS('web-app/app-js/**/*.ts');

    console.log('Compiling app.js file.');

    browserifyProcesor = browserify({
        entries: ['web-app/app-js/main.ts'],
        debug: !prodEnv, // SourceMapping
    }).plugin("tsify");

    // Exclude all NPM Package from the build
    getNPMPackageIds().forEach(function (id) {
        // Does not exclude
        if (id !== 'rxjs' && id !== '@angular/http') {
            browserifyProcesor.external(id);
        }
    });

    return browserifyProcesor.bundle()
        .pipe(source('app.js'))
        .pipe(buffer())
        .pipe(uglify())
        .pipe(gulp.dest('./web-app/dist/'));

});

/**
 * Watch process only listen to main.ts and its dependencies
 * it's necessary to build-vendor if something more was added.
 */
gulp.task('watch-build-app', function () {
    var generatedFile = 'web-app/dist/app.js';
    var browserifyProcesor;

    var browserifyProcesor = browserify({
        entries: ['./web-app/app-js/main.ts'],
        debug: true // SourceMapping
    });

    validateJS('web-app/app-js/**/*.ts');

    // Exclude all NPM Package from the build
    getNPMPackageIds().forEach(function (id) {
        // Does not exclude
        if (id !== 'rxjs' && id !== '@angular/http') {
            browserifyProcesor.external(id);
        }
    });

    var watcher = watchify(browserifyProcesor, {
        poll: true
    });

    var updateStart = Date.now();
    return watcher
        .plugin("tsify")
        .on('update', function () {
            var updateStart = Date.now();
            console.log('Building app.js');
            watcher.plugin("tsify")
                .bundle()
                .on("error", function (error) {
                    onBuildError(error);
                    this.emit("end");
                })
                .pipe(source('app.js'))
                .pipe(buffer())
                .pipe(gulp.dest('./web-app/dist/'));
            console.log('Build success! in ', (Date.now() - updateStart) + 'ms');
        })
        .bundle()
        .on('error', function (error) {
            onBuildError(error);
            this.emit("end")
        });

});

/**
 * What this task do is to compile the SASS file and generate a map that can be view from moder browser
 * style.css is being created, if prodEnv is true, then minify the css for use on prodEnv.
 * from command line: gulp sass-compiler --PROD
 */
gulp.task('sass-compiler', function () {
    return gulp.src('./web-app/css/style.sass')
        .pipe(sourcemaps.init())
        .pipe(sass({ errLogToConsole: true }))
        .pipe(autoprefixer({ browsers: ['last 2 version'], cascade: false }))
        .pipe(sourcemaps.write())
        .pipe(gulp.dest('./web-app/css'));
});

/**
 * Execute watch task to compile css automatically on Development mode
 * from command line: gulp sass:watch
 * it will run until stop, searching for changes on any SASS file, compiles and ready to use
 */
gulp.task('sass:watch', function () {
    return gulp.watch('./web-app/css/**/*.sass', ['sass-compiler']);
});

/**
 * vendor.ts is where all third parties should rely on
 */
gulp.task('build-vendor', function () {

    var generatedFile = 'web-app/dist/vendor.js';
    var browserifyProcesor;

    if (fs.existsSync(generatedFile)) {
        console.log('Cleaning previous compiled vendor.js file.');
        fs.unlink(generatedFile);
    }

    console.log('Compiling vendor.js file.');

    browserifyProcesor = browserify({
        debug: false, // SourceMapping
    });

    // Include all NPM Package from the build
    getNPMPackageIds().forEach(function (id) {
        // Does not include
        if (id !== 'zone.js' && id !== 'core-js' && id !== 'rxjs') {
            browserifyProcesor.require(nodeResolve.sync(id), { expose: id });
        }
    });

    return browserifyProcesor.bundle()
        .pipe(source('vendor.js'))
        .pipe(buffer())
        .pipe(uglify())
        .pipe(gulp.dest('./web-app/dist/'));
});

/**
 * Helper method to catch compilation errors
 * @param error\
 */
var onBuildError = function (error) {
    console.error('Error generating the app.js file by', error.message);
};

/**
 * Read the package.json + node_modules to match the Dependency section
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
};

/**
 * Runs JSHint code checker tool on the given source.
 *
 * @param src specifying which files to check
 * @return {*}
 */
function validateJS(src) {
    console.log('Running Quality Code Checker for ' + src);
    return gulp.src(src)
        .pipe(tslint({
            formatter: "verbose"
        }))
        .pipe(tslint.report())
};

gulp.task('build-test', shell.task(['karma start karma.conf.js']));
gulp.task('build-test-report', shell.task(['karma start karma.production.conf.js']));

gulp.task('tiny-mce-files', function () {
    gulp.src(['node_modules/tinymce/tinymce.min.js',
        'node_modules/tinymce/themes/modern/theme.min.js',
        'node_modules/tinymce/skins/**'])
        .pipe(gulp.dest('../dist/js/vendors/tinymce'));
});