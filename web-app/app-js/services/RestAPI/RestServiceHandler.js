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
            getLicense: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/licenses'), onSuccess);
            },
            getEnvironmentDataSource: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/license/environment'), onSuccess);
            },
            getProjectDataSource: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/license/project'), onSuccess);
            },
            getLicenseList: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/license'), onSuccess);
            },
            createNewLicenseRequest: (data, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/license/request';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            applyLicense:  (licenseId, data, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/license/' + licenseId + '/load';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            getHashCode:  (licenseId,onSuccess, onError) => {
                this.req.method = 'GET';
                this.req.url =  '../ws/license/' + licenseId + '/hash';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            //--------------------------------------------
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

    licenseManagerServiceHandler() {
        return {
            requestImport:  (data, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/manager/license/request';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            getLicenseList: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/manager/license'), onSuccess);
            },
            saveLicense: (data, callback) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/???';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
            },
            revokeLicense: (data, onSuccess, onError) => {
                this.req.method = 'DELETE';
                this.req.url =  '../ws/license/'+data.id;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            activateLicense: (data, callback) => {
                this.req.method = 'POST';
                this.req.url =  '../ws/???';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
            }
        };
    }

    noticeManagerServiceHandler() {
        return {
            getNoticeList: (onSuccess) => { // real ws example
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/notices'), onSuccess);
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

