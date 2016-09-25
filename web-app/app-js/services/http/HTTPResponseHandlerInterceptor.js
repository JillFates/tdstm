/**
 * Created by Jorge Morayta on 12/11/15.
 * This method is called right after $http receives the response from the backend,
 * so you can modify the response and make other actions. This function receives a response object as a parameter
 * and has to return a response object or a promise. The response object includes
 * the request configuration, headers, status and data that returned from the backend.
 * Returning an invalid response object or promise that will be rejected, will make the $http call to fail.
 */

'use strict';

import HTTPInterceptorInterface from './HTTPInterceptorInterface.js';

export default class HTTPResponseHandlerInterceptor extends /*implements*/ HTTPInterceptorInterface {
    constructor($log, $q, rx) {
        super('response');
        this.log = $log;
        this.q = $q;
        this.defer = this.q.defer();
        this.log.debug('HTTPResponseHandlerInterceptor instanced');
    }

    response(response) {
        // do something on success

        response.config.responseTimestamp = new Date().getTime();

        this.defer.notify(response);
        return response || this.q.when(response);
    }

    listenResponse() {
        return this.defer.promise;
    }
}

