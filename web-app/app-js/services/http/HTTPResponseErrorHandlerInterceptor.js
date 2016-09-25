/**
 * Created by Jorge Morayta on 12/11/15.
 * If backend call fails or it might be rejected by a request interceptor or by a previous response interceptor;
 * In those cases, response error interceptor can help us to recover the backend call.
 */

'use strict';

import HTTPInterceptorInterface from './HTTPInterceptorInterface.js';

export default class HTTPResponseErrorHandlerInterceptor extends /*implements*/ HTTPInterceptorInterface {
    constructor($log, $q, rx) {
        super('responseError');
        this.log = $log;
        this.q = $q;
        this.defer = this.q.defer();
        this.log.debug('HTTPResponseErrorHandlerInterceptor instanced');
    }

    responseError(rejection) {
        // do something on error
        //if (canRecover(rejection)) {
        //    return responseOrNewPromise
        // }

        this.defer.notify(rejection);
        return this.q.reject(rejection);
    }

    listenError() {
        return this.defer.promise;
    }

}
