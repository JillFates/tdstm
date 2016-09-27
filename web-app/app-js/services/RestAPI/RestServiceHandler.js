/**
 * Created by Jorge Morayta on 12/08/15.
 * It abstract each one of the existing call to the API, it should only contains the call functions and reference
 * to the callback, no logic at all.
 *
 */


'use strict';

import RequestHandler from './RequestHandler.js';

export default class RestServiceHandler{
    constructor($log, $http, $resource, rx) {
        this.rx = rx;
        this.log = $log;
        this.http = $http;
        this.resource = $resource;
        this.prepareHeaders();
        this.log.debug('RestService Loaded');
    }

    prepareHeaders() {
        this.http.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
    }

    ResourceServiceHandler() {
        return {
            getSVG: (iconName) => {
                return this.subscribeRequest(this.http.get('images/svg/' + iconName + '.svg'));
            }
        };
    }

    TaskServiceHandler() {
        return {
            getFeeds: (callback) => {
                return this.subscribeRequest(this.http.get('test/mockupData/TaskManager/taskManagerList.json'), callback);
            }
        };
    }

    LicenseManagerServiceHandler() {
        return {
            getLicenseList: (data, callback) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/cookbook/recipe/list?archived=n&context=All&rand=oDFqLTpbZRj38AW'), callback);
            },
            getLicense: (callback) => { // Mockup Data for testing see url
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../test/mockupData/LicenseManager/licenseManagerList.json'), callback);
            }
        };
    }

}

