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
        this.req = {
            method: '',
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

    licenseAdminServiceHandler() {
        return {
            getLicenseList: (data, onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/license'), onSuccess);
            },
            getLicense: (callback) => { // Mockup Data for testing see url
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../test/mockupData/LicenseAdmin/licenseAdminList.json'), callback);
            },
            createNewLicenseRequest: (data, callback) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/???';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
            },
            applyLicense:  (data, callback) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/???';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
            },
            resubmitLicenseRequest: (data, callback) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/???';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
            },
            emailRequest: (data, callback) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/???';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
            },
            deleteLicense: (data, onSuccess, onError) => {
                this.req.method = 'DELETE';
                this.req.url =  '../ws/license/'+data.id;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
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
                this.req.method = 'POST';
                this.req.url =  '../ws/notices';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            editNotice: (data, onSuccess, onError) => {
                this.req.method = 'PUT';
                this.req.url =  '../ws/notices/'+data.id;
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            deleteNotice: (data, onSuccess, onError) => {
                this.req.method = 'DELETE';
                this.req.url =  '../ws/notices/'+data.id;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            }
        };
    }

}

