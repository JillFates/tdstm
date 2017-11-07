package com.tdsops.etl

import com.tdssrc.grails.StringUtil

class Transformation {

    Element element

    Transformation (Element element) {
        this.element = element
    }

    Transformation middle (int take, int position) {
        int start = (position - 1)
        int to = (start + take - 1)
        this.element.value = this.element.value[start..to]
        this
    }

    Transformation translate (def map) {
        Map dictionary = map['with']
        if (dictionary.containsKey(element.value)) {
            element.value = dictionary[element.value]
        }
        this
    }
    /**
     * Replace all of the escape characters
     * (CR|LF|TAB|Backspace|FormFeed|single/double quote) with plus( + )
     * and replaces any non-printable, control and special unicode character
     * with a tilda ( ~ ).
     *
     * The method will also remove any leading and trailing whitespaces
     * @return
     */
    Transformation sanitize () {
        element.value = StringUtil.sanitizeAndStripSpaces(element.value)
        this
    }

    Transformation trim () {
        this.element.value = this.element.value.trim()
        this
    }

    Transformation first (String content) {
        this.element.value = this.element.value.replaceFirst(content, '')
        this
    }

    Transformation all (String content) {
        this.element.value = this.element.value.replaceAll(content, '')
        this
    }

    Transformation last (String content) {
        this.element.value = this.element.value.reverse().replaceFirst(content, '').reverse()
        this
    }

    Transformation uppercase () {
        this.element.value = this.element.value.toUpperCase()
        this
    }

    Transformation lowercase () {
        this.element.value = this.element.value.toLowerCase()
        this
    }

    Transformation left (Integer amount) {
        element.value = element.value.take(amount)
        this
    }

    Transformation right (Integer amount) {
        element.value = element.value.reverse().take(amount).reverse()
        this
    }
}
