/**
 * Created by Jorge Morayta on 12/08/15.
 * It abstract each one of the existing call to the API, it should only contains the call functions and reference
 * to the callback, no logic at all.
 *
 */


'use strict';

import RequestHandler from './RequestHandler.js';

export default class RestServiceHandler extends RequestHandler {
    constructor($log, $http, $resource, rx) {
        super(rx);
        this.log = $log;
        this.http = $http;
        this.resource = $resource;
        this.restPath = 'webresources/';

        this.log.debug('RestService Loaded');
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

}

