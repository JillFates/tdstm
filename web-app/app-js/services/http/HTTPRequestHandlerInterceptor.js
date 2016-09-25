/**
 * Created by Jorge Morayta on 12/11/15.
 * It implement an abstract call to HTTP Interceptors to manage only request
 */

'use strict';

import HTTPInterceptorInterface from './HTTPInterceptorInterface.js';

export default class HTTPRequestHandlerInterceptor extends /*implements*/ HTTPInterceptorInterface {

    constructor($log, $q, rx) {
        super('request');
        this.log = $log;
        this.q = $q;
        this.defer = this.q.defer();
        this.log.debug('HTTPRequestHandlerInterceptor instanced');
    }

    request(config) {
        // We can add headers if on the incoming request made it we have the token inside
        // defined by some conditions
        //config.headers['x-session-token'] = my.token;

        config.requestTimestamp = new Date().getTime();

        this.defer.notify(config);

        return config || this.q.when(config);
    }

    listenRequest() {
        return this.defer.promise;
    }

}
