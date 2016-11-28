/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

export default class LicenseAdminService {

    constructor($log, restServiceHandler, $rootScope) {
        this.log = $log;
        this.restService = restServiceHandler;
        this.rootScope = $rootScope;
        this.log.debug('licenseAdminService Instanced');
    }

    getLicenseList(callback) {
        this.restService.licenseAdminServiceHandler().getLicenseList((data) => {
            return callback(data);
        });
    }

    getEnvironmentDataSource(onSuccess) {
        this.restService.licenseAdminServiceHandler().getEnvironmentDataSource((data) => {
            return onSuccess(data);
        });
    }

    getProjectDataSource(onSuccess) {
        this.restService.licenseAdminServiceHandler().getProjectDataSource((data) => {
            return onSuccess(data.data);
        });
    }

    /**
     * Create a New License passing params
     * @param newLicense
     * @param callback
     */
    createNewLicenseRequest(newLicense, onSuccess){
        this.restService.licenseAdminServiceHandler().createNewLicenseRequest(newLicense, (data) => {
            return onSuccess(data);
        });
    }

    resubmitLicenseRequest(license, callback) {
        this.restService.licenseAdminServiceHandler().resubmitLicenseRequest(license, (data) => {
            this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Request License was successfully.'});
            return callback(data);
        });
    }

    emailRequest(license, callback) {
        this.restService.licenseAdminServiceHandler().emailRequest(license, (data) => {
            this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Request License was successfully.'});
            return callback(data);
        });
    }

    applyLicense(license, callback) {
        this.restService.licenseAdminServiceHandler().applyLicense(license, (data) => {
            //if(data.applied) {
                this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully applied'});
            /*} else {
                this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was successfully applied'});
            }*/
            return callback(data);
        });
    }

    deleteLicense(license, callback) {
        this.restService.licenseAdminServiceHandler().deleteLicense(license, (data) => {
            return callback(data);
        });
    }
}

