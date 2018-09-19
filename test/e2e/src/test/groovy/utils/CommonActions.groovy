package utils

import org.apache.commons.lang3.RandomStringUtils
/*
* Note: Page including common methods or actions to perform in the system. For example: select options from select.
* */

class CommonActions {

    /*
    * Note: returns a random option from select dropdown
    * Parameter: options must be a selector containing option tags in dropdown
    * */
    static getSelectRandomOption(options){
        // removing <select value=0> label used to notify user to select a value
        def finalOptions = options.remove(0)
        getRandomOption(finalOptions)
    }

    /*
   * Note: returns a random option from given options list
   * Parameter: options must be a list
   * */
    static getRandomOption(options) {
        def random = new Random()
        options.getAt(random.nextInt(options.size()))
    }

    /*
    * Note: returns random list of elements based on the count passed from a given elements list
    * Parameter: options must be a list
    * Parameter: count is number of elements to be returned, if not set it returns a random number of elements
    * based on the given list of elements.
     */
    static getRandomOptions(options, count) {
        def randomSelectionCount = count != null ? count : getRandomNumberFromList(options)
        def option = getRandomOption options
        def finalOptionsList = []
        finalOptionsList.add option // first addition
        def found
        while (finalOptionsList.size() != randomSelectionCount) { // iterate adding to get random quantity size
            found = false
            option = getRandomOption options
            found = finalOptionsList.contains option
            if (!found) {
                finalOptionsList.add option
            }
        }
        finalOptionsList
    }

    /*
    * Note: returns random number from given list of elements
    * Parameter: elementsList must be a list
     */
    static getRandomNumberFromList(elementsList) {
        new Random().nextInt(elementsList.size()) + 1 // prevent getting zero
    }

    /*
    * Note: cleans checkboxes checked from a given elements list
    * Parameter: checkboxes must be a list
     */
    static uncheckCheckboxes(checkboxes) {
        checkboxes?.each { checkbox ->
            checkbox.click()
        }
    }

    static convertRgbToHex(int r, int g, int b) {
        "#" + toHexValue(r) + toHexValue(g) + toHexValue(b)
    }

    static toHexValue(int number) {
        def builder = new StringBuilder(Integer.toHexString(number & 0xff))
        while (builder.length() < 2) {
            builder.append("0")
        }
        builder.toString()
    }

    static getRandomString(Integer charNumbers = randomCharNumbers){
        RandomStringUtils.randomAlphanumeric(charNumbers)
    }

    static randomCharNumbers = 5
}