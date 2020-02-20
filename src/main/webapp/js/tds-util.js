var TDSUtilFunctions = (function ($) {
    var public = {};

    /**
     * retry n times the function until the fulfillCondition is met and the callback is executed
     * @param maxTries Number of intents to try
     * @param milliseconds Gap between tries in mls
     * @param fulfillCondition Function that determines if the condition to execute the callback is met
     * @param callback Function that is executed if the fulfill condition is met
     */
    public.retryFunction = function (maxTries, milliseconds, fulfillCondition, callback ) {
        var counter = 0;
        var interval = setInterval(function () {
            counter += 1;
            try {
                if (fulfillCondition()) {
                    callback();
                    clearInterval(interval);
                } else if (counter === maxTries) {
                    clearInterval(interval);
                }
            } catch(error) {
               if (counter === maxTries)  {
                   clearInterval(interval);
               }
            }
        }, milliseconds);
    };

    return public;

})((jQuery));
