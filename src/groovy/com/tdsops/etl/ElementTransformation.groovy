package com.tdsops.etl

class ElementTransformation extends ETLTransformation {

    static left = new ElementTransformation(closure: { Element element, Integer amount ->
        element.value = element.value.take(amount)
    })

    static right = new ElementTransformation(closure: { Element element, Integer amount ->
        element.value = element.value.reverse().take(amount).reverse()
    })

    static uppercase = new ElementTransformation(closure: { Element element ->
        element.value = element.value.toUpperCase()
    })

    static lowercase = new ElementTransformation(closure: { Element element ->
        element.value = element.value.toLowerCase()
    })

    static middle = new MiddleTransformation()


    static strip = new ETLTransformation() {

        @Override
        def apply (Element element) {
            [
                    'first': { String str ->
                        element.value = element.value.replaceFirst(str, '')
                        element
                    },
                    'last' : { String str ->
                        element.value = element.value.reverse().replaceFirst(str, '').reverse()
                        element
                    },
                    'all'  : { String str ->
                        element.value = element.value.replaceAll(str, '')
                        element
                    }
            ]
        }
    }

    static transformationsMap = [
            left     : left,
            right    : right,
            middle   : middle,
            uppercase: uppercase,
            lowercase: lowercase,
            strip    : strip
    ]
}

class MiddleTransformation extends ETLTransformation {

    private Element element
    private int amount
    /**
     *
     * Applies a transformation on an Element modifying its current value
     *
     * @param element and ETL Processor element to be modified
     */
    @Override
    MiddleTransformation apply (Element anElement) {
        this.element = anElement
        this
    }

    MiddleTransformation take (int amount) {
        this.amount = amount
        this
    }

    Element from (int position) {
        int start = (position - 1)
        int to = (start + amount - 1)
        this.element.value = this.element.value[start..to]
        this.element
    }
}
