/**
 * Created by Jorge Morayta on 06/10/2016.
 * Contains a compilation process for ES6 using Babel (see typescript-compile.js for typescript)
 */

// Let's always ensure we are using specific require modules per task

var gulp = require("gulp"),
    sourcemaps = require("gulp-sourcemaps"),
    babel = require("gulp-babel"),
    concat = require("gulp-concat");

module.exports = function () {

    /**
     * Compile the ES6 classes and generate a sourcecode map for debugging helps
     */
    gulp.task('build-es6-generator-dependency-analyzer', function () {
        var initialPath = "web-app/js/generator";

        return gulp.src(initialPath+"/source/**/*.js")
            .pipe(sourcemaps.init())
            .pipe(babel())
            .pipe(concat("generator.js"))
            .pipe(sourcemaps.write("."))
            .pipe(gulp.dest(initialPath));
    });
}

