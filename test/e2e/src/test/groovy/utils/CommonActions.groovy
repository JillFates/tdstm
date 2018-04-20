package utils

/*
* Note: Page including common methods or actions to perform in the system. For example: select options from select.
* */

class CommonActions {

    /*
    * Note: returns a random option from select dropdown
    * Parameter: options must be a selector containing option tags in dropdown
    * */
    def getSelectRandomOption(options){
        // removing <select value=0> label used to notify user to select a value
        def finalOptions = options.remove(0)
        getRandomOption(finalOptions)
    }

    /*
   * Note: returns a random option from given options list
   * Parameter: options must be a list
   * */
    def getRandomOption(options) {
        def random = new Random()
        options.getAt(random.nextInt(options.size()))
    }
}