/**
 * Created by Jorge Morayta on 12/11/15.
 * It implement an abstract call to HTTP Interceptors to manage error handler
 * Sometimes a request can't be sent or it is rejected by an interceptor.
 * Request error interceptor captures requests that have been canceled by a previous request interceptor.
 * It can be used in order to recover the request and sometimes undo things that have been set up before a request,
 * like removing overlays and loading indicators, enabling buttons and fields and so on.
 */


'use strict';

import HTTPInterceptorInterface from './HTTPInterceptorInterface.js';

export default class HTTPRequestErrorHandlerInterceptor extends /*implements*/ HTTPInterceptorInterface {
    constructor($log, $q, rx) {
        super('requestError');
        this.log = $log;
        this.q = $q;
        this.defer = this.q.defer();
        this.log.debug('HTTPRequestErrorHandlerInterceptor instanced');
    }

    requestError(rejection) {
        // do something on error
        // do something on error
        //if (canRecover(rejection)) {
        //    return responseOrNewPromise
        //}
        this.defer.notify(rejection);

        return this.q.reject(rejection);
    }

    listenError() {
        return this.defer.promise;
    }

}
