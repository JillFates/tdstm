/**
 * Created by Jorge Morayta on 12/08/15.
 * It abstract each one of the existing call to the API, it should only contains the call functions and reference
 * to the callback, no logic at all.
 *
 */


'use strict';

import RequestHandler from './RequestHandler.js';

export default class RestServiceHandler {
    constructor($log, $http, $resource, rx) {
        this.rx = rx;
        this.log = $log;
        this.http = $http;
        this.resource = $resource;
        this.prepareHeaders();
        this.log.debug('RestService Loaded');
        this.postReq = {
            method: 'POST',
            url: '',
            headers: {
                'Content-Type': 'application/json;charset=UTF-8'
            },
            data: []
        };
    }

    prepareHeaders() {
        this.http.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
    }

    TaskServiceHandler() {
        return {
            getFeeds: (callback) => {
                return this.subscribeRequest(this.http.get('test/mockupData/TaskManager/taskManagerList.json'), callback);
            }
        };
    }

    licenseManagerServiceHandler() {
        return {
            getLicenseList: (data, callback) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/cookbook/recipe/list?archived=n&context=All&rand=oDFqLTpbZRj38AW'), callback);
            },
            getLicense: (callback) => { // Mockup Data for testing see url
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../test/mockupData/LicenseManager/licenseManagerList.json'), callback);
            },
            createNewLicenseRequest: (data, callback) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseManager/licenseManagerList.json', data), callback);
            }
        };
    }

    noticeManagerServiceHandler() {
        return {
            getNoticeList: (onSuccess) => { // real ws example
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/notices'), onSuccess);
            },
            getNoticeMockUp: (onSuccess) => { // Mockup Data for testing see url
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../test/mockupData/NoticeManager/noticeManagerList.json'), onSuccess);
            },
            createNotice: (data, onSuccess, onError) => {
                this.postReq.url =  '../ws/notices';
                this.postReq.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.postReq), onSuccess, onError);
            },
            editNotice: (data, onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/NoticeManager/noticeManagerList.json', data), onSuccess);
            }
        };
    }

}

