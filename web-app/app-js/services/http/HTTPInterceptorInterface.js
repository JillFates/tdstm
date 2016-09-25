/**
 * Created by Jorge Morayta on 12/22/2015.
 * ES6 Interceptor calls inner methods in a global scope, then the "this" is being lost
 * in the definition of the Class for interceptors only
 * This is a interface that take care of the issue.
 */


export default /* interface*/ class HttpInterceptor {
    constructor(methodToBind) {
        // If not method to bind, we assume our interceptor is using all the inner functions
        if(!methodToBind) {
            ['request', 'requestError', 'response', 'responseError']
                .forEach((method) => {
                    if(this[method]) {
                        this[method] = this[method].bind(this);
                    }
                });
        } else {
            // methodToBind reference to a single child class
            this[methodToBind] = this[methodToBind].bind(this);
        }

    }
}
