package com.tdsops.etl

class Transformer extends Expando {

    Element element

    Transformer (Element element) {
        this.element = element
    }

    /**
     *
     *
     * @param methodName
     * @param args
     */
    def methodMissing (String methodName, args) {
        if (methodName in ['first', 'last']) {
            this."$methodName"(args)
        }
    }

    def propertyMissing (String name) {
        if (name in ['lowercase', 'uppercase', 'trim']) {
            this."$name"()
        }
    }

    def middle (int take, int position) {
        int start = (position - 1)
        int to = (start + take - 1)
        this.element.value = this.element.value[start..to]
    }

    def translate (def map) {
        Map dictionary = map['with']
        if (dictionary.containsKey(element.value)) {
            element.value = dictionary[element.value]
        }
    }

    def trim () {
        this.element.value = this.element.value.trim()
    }

    def first (String content) {
        this.element.value = this.element.value.replaceFirst(content, '')
    }

    def all (String content) {
        this.element.value = this.element.value.replaceAll(content, '')
    }

    def last (String content) {
        this.element.value = this.element.value.reverse().replaceFirst(content, '').reverse()
    }

    def uppercase () {
        this.element.value = this.element.value.toUpperCase()
    }

    def lowercase () {
        this.element.value = this.element.value.toLowerCase()
    }

    def left (Integer amount) {
        element.value = element.value.take(amount)
    }

    def right (Integer amount) {
        element.value = element.value.reverse().take(amount).reverse()
    }
}
