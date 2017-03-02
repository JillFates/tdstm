/**
 * Created by Jorge Morayta on 2/3/2017.
 */

var argv = require('yargs').argv,            // Arguments from command prompt
    browserify = require("browserify"),
    buffer = require('vinyl-buffer'),
    gulp = require('gulp'),
    gulpif = require('gulp-if'),
    source = require('vinyl-source-stream'),
    shell = require('gulp-shell'),
    tsify = require("tsify"),
    tslint = require("gulp-tslint"),
    uglify = require('gulp-uglify'),
    watchify = require('watchify');

/* Command line arg, e.g.: gulp --PROD */
var prodEnv = argv.PROD === true;

gulp.task('build', ['build-app', 'build-vendor']);

/**
 * Compiles the main source from main.ts and their dependencies
 * The source mapping is added by default, unless is compiled in PROD mode.
 */
gulp.task('build-app', function () {

    return browserify({
        entries: ['web-app/app-js/main.ts'],
        debug: !prodEnv // SourceMapping
    })
        .plugin("tsify")
        .bundle()
        .pipe(source('app.js'))
        .pipe(gulpif(prodEnv, buffer()))
        .pipe(gulpif(prodEnv, uglify()))
        .pipe(gulp.dest('./web-app/dist/'));

});

/**
 * Watch process only listen to main.ts and its dependencies
 * it's necessary to build-vendor if something more was added.
 */
gulp.task('watch-build-app', function () {

    var b = browserify({
        entries: ['./web-app/app-js/main.ts'],
        debug: !prodEnv // SourceMapping
    });

    var watcher = watchify(b, {
        poll: true
    });

    var updateStart = Date.now();
    return watcher
        .plugin("tsify")
        .on('update', function(){
            var updateStart = Date.now();
            console.log('Building app.js');
            watcher.plugin("tsify")
                .bundle()
                .on("error", function (error) {
                    onBuildError(error);
                    this.emit("end");
                })
                .pipe(source('app.js'))
                .pipe(gulpif(prodEnv, buffer()))
                .pipe(gulpif(prodEnv, uglify()))
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
 * vendor.ts is where all third parties should rely on
 */
gulp.task('build-vendor', function () {

    return browserify({
        entries: ['./web-app/app-js/vendor.ts'],
        debug: false // SourceMapping
    })
        .plugin("tsify")
        .bundle()
        .pipe(source('vendor.js'))
        .pipe(gulpif(prodEnv, buffer()))
        .pipe(gulpif(prodEnv, uglify()))
        .pipe(gulp.dest('./web-app/dist/'));
});

/**
 * Helper method to catch compilation errors
 * @param error\
 */
var onBuildError = function (error) {
    console.error('Error generating the app.js file by', error.message);

};

gulp.task('typescript-compile', shell.task(['tsc -p web-app']));
gulp.task('build-test',['typescript-compile'], shell.task(['karma start karma.conf.js']));
gulp.task('build-test-report',['typescript-compile'], shell.task(['karma start karma.production.conf.js']));