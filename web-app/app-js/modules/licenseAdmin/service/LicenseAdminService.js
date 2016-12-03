/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

export default class LicenseAdminService {

    constructor($log, restServiceHandler, $rootScope) {
        this.log = $log;
        this.restService = restServiceHandler;
        this.rootScope = $rootScope;
        this.statusSuccess = 'success';
        this.log.debug('licenseAdminService Instanced');
    }

    getLicenseList(onSuccess) {
        this.restService.licenseAdminServiceHandler().getLicenseList((data) => {
            return onSuccess(data.data);
        });
    }

    getEnvironmentDataSource(onSuccess) {
        this.restService.licenseAdminServiceHandler().getEnvironmentDataSource((data) => {
            return onSuccess(data.data);
        });
    }

    getProjectDataSource(onSuccess) {
        this.restService.licenseAdminServiceHandler().getProjectDataSource((data) => {
            return onSuccess(data.data);
        });
    }

    getHashCode(licenseId, onSuccess) {
        this.restService.licenseAdminServiceHandler().getHashCode(licenseId, (data) => {
            return onSuccess(data.data);
        });
    }

    /**
     * Create a New License passing params
     * @param newLicense
     * @param callback
     */
    createNewLicenseRequest(newLicense, onSuccess){
        newLicense.environmentId = parseInt(newLicense.environmentId);
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

    /**
     *  Apply The License
     * @param license
     * @param onSuccess
     */
    applyLicense(license, onSuccess, onError) {

        var hash =  {
            hash: license.key
        };

        this.restService.licenseAdminServiceHandler().applyLicense(license.id, hash, (data) => {
            if(data.status === this.statusSuccess) {
                this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully applied'});
            } else {
                this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was not applied'});
                return onError({ success: false});
            }

            return onSuccess({ success: true});

        });
    }

    deleteLicense(license, callback) {
        this.restService.licenseAdminServiceHandler().deleteLicense(license, (data) => {
            return callback(data);
        });
    }
}

