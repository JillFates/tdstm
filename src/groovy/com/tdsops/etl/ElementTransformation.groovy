package com.tdsops.etl

class ElementTransformation extends ETLTransformation {

    static left = new ElementTransformation(closure: { Element element, Integer amount ->
        element.value = element.value.take(amount)
    })

    static right = new ElementTransformation(closure: { Element element, Integer amount ->
        element.value = element.value.reverse().take(amount).reverse()
    })

    static uppercase = new ElementTransformation(closure: { it.value = it.value.toUpperCase() })

    static lowercase = new ElementTransformation(closure: { it.value = it.value.toLowerCase() })

    static transformationsMap = [
            left     : left,
            right    : right,
            uppercase: uppercase,
            lowercase: lowercase
    ]
}
