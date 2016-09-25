/**
 * Created by Jorge Morayta on 12/22/2015.
 * Use this module to modify anything related to the Headers and Request
 */

'use strict';

import angular  from 'angular';
import HTTPRequestHandlerInterceptor from './HTTPRequestHandlerInterceptor.js';
import HTTPRequestErrorHandlerInterceptor from './HTTPRequestErrorHandlerInterceptor.js';
import HTTPResponseErrorHandlerInterceptor from './HTTPResponseErrorHandlerInterceptor.js';
import HTTPResponseHandlerInterceptor from './HTTPResponseHandlerInterceptor.js';


var HTTPModule = angular.module('TDSTM.HTTPModule', ['ngResource']).config(['$httpProvider', function($httpProvider){

    //initialize get if not there
    if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {};
    }

    //Disable IE ajax request caching
    $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul 1997 05:00:00 GMT';
    // extra
    $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
    $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';


    // Injects our Interceptors for Request
    $httpProvider.interceptors.push('HTTPRequestHandlerInterceptor');
    $httpProvider.interceptors.push('HTTPRequestErrorHandlerInterceptor');
    // Injects our Interceptors for Response
    $httpProvider.interceptors.push('HTTPResponseHandlerInterceptor');
    $httpProvider.interceptors.push('HTTPResponseErrorHandlerInterceptor');


}]);

HTTPModule.service('HTTPRequestHandlerInterceptor', ['$log', '$q', 'rx', HTTPRequestHandlerInterceptor]);
HTTPModule.service('HTTPRequestErrorHandlerInterceptor', ['$log', '$q', 'rx', HTTPRequestErrorHandlerInterceptor]);
HTTPModule.service('HTTPResponseHandlerInterceptor', ['$log', '$q', 'rx', HTTPResponseHandlerInterceptor]);
HTTPModule.service('HTTPResponseErrorHandlerInterceptor', ['$log', '$q', 'rx', HTTPResponseErrorHandlerInterceptor]);

export default HTTPModule;