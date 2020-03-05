package com.tdsops.etl

import grails.util.Pair

/**
 * Class for managing ETL Global transformations.
 * They can be activated by an ETL command:
 * <pre>
 *     sanitize on
 *     sanitize off
 *
 *     trim on // Default for any ETL script
 *     trim off
 *
 *     replace 'Inc', 'Incorporated'
 * </pre>
 * @see ETLProcessor#sanitize(com.tdsops.etl.ETLProcessor.ReservedWord)
 * @see ETLProcessor#trim(com.tdsops.etl.ETLProcessor.ReservedWord)
 * @see ETLProcessor#replace(java.lang.String, java.lang.String)
 */
class ETLGlobalTransformation {

    Boolean trimmer
    Boolean sanitizer
    List<Pair<String, String>> replacements = []

    ETLGlobalTransformation() {
        trimmer = true
        sanitizer = true
    }

    /**
     * Apply all global transformation for a particular instance of {@link Element}.
     * If {@link Element#value} is a String object,
     * all the available global transformations are going to be applied
     * on {@link Element#value}.
     *
     * @param element an instance of {@link Element}
     */
    void applyAll(Element element) {

        if (element.value instanceof CharSequence) {

            if (trimmer) element.trim()
            if (sanitizer) element.sanitize()
            for (Pair<String, String> replacer in replacements) {
                element.replace(replacer.aValue, replacer.bValue)
            }
        }
    }
    /**
     * Add a new Replacer object in current {@link ETLGlobalTransformation} instance.
     *
     * @param regex
     * @param replacement
     */
    void addReplacer(String regex, String replacement) {
        replacements.add(new Pair<String, String>(regex, replacement))
    }
}
