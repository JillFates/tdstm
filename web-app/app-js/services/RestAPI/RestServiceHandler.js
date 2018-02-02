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

    commonServiceHandler() {
        return {
            getTimeZoneConfiguration: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/user/preferences/CURR_DT_FORMAT,CURR_TZ'), onSuccess);
            }
        }
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
                this.req.url = '../ws/license/request';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            applyLicense: (licenseId, data, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url = '../ws/license/' + licenseId + '/load';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            getHashCode: (licenseId, onSuccess, onError) => {
                this.req.method = 'GET';
                this.req.url = '../ws/license/' + licenseId + '/hash';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            getEmailContent: (licenseId, onSuccess, onError) => {
                this.req.method = 'GET';
                this.req.url = '../ws/license/' + licenseId + '/email/request';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            //--------------------------------------------
            resubmitLicenseRequest: (licenseId, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url = '../ws/license/' + licenseId + '/email/request';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            emailRequest: (data, callback) => {
                this.req.method = 'POST';
                this.req.url = '../ws/???';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
            },
            deleteLicense: (data, onSuccess, onError) => {
                this.req.method = 'DELETE';
                this.req.url = '../ws/license/' + data.id;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            }
        };
    }

    licenseManagerServiceHandler() {
        return {
            requestImport: (data, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url = '../ws/manager/license/request';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            getLicenseList: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/manager/license'), onSuccess);
            },
            getProjectDataSource: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/license/project'), onSuccess);
            },
            getEnvironmentDataSource: (onSuccess) => {
                return new RequestHandler(this.rx).subscribeRequest(this.http.get('../ws/license/environment'), onSuccess);
            },
            getKeyCode: (licenseId, onSuccess, onError) => {
                this.req.method = 'GET';
                this.req.url = '../ws/manager/license/' + licenseId + '/key';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            saveLicense: (licenseId, licenseModified, onSuccess, onError) => {
                this.req.method = 'PUT';
                this.req.url = '../ws/manager/license/' + licenseId;
                this.req.data = licenseModified;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            deleteLicense: (data, onSuccess, onError) => {
                this.req.method = 'DELETE';
                this.req.url = '../ws/manager/license/' + data.id + '/delete';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            revokeLicense: (data, onSuccess, onError) => {
                this.req.method = 'DELETE';
                this.req.url = '../ws/manager/license/' + data.id;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            activateLicense: (licenseId, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url = '../ws/manager/license/' + licenseId + '/activate';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            getActivityLog: (licenseId, onSuccess, onError) => {
                this.req.method = 'GET';
                this.req.url = '../ws/manager/license/' + licenseId + '/activitylog';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            manuallyRequest: (licenseId, onSuccess, onError) => {
                this.req.method = 'POST';
                this.req.url = '../ws/manager/license/' + licenseId + '/email/send';
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
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
                this.req.url = '../ws/notices';
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            editNotice: (data, onSuccess, onError) => {
                this.req.method = 'PUT';
                this.req.url = '../ws/notices/' + data.id;
                this.req.data = data;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            },
            deleteNotice: (data, onSuccess, onError) => {
                this.req.method = 'DELETE';
                this.req.url = '../ws/notices/' + data.id;
                return new RequestHandler(this.rx).subscribeRequest(this.http(this.req), onSuccess, onError);
            }
        };
    }

}

