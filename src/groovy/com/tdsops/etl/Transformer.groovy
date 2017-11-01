package com.tdsops.etl

class Transformer {

    Closure closure

    void apply(Transformation transformation) {
        closure(transformation)
    }

    static Trimmer = { Transformation transformation ->
        transformation.trim()
    }

    static Sanitizer = { Transformation transformation ->
        transformation.sanitize()
    }
}
