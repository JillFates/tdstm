/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

export default class LicenseManagerService {

    constructor($log, restServiceHandler, $rootScope) {
        this.log = $log;
        this.restService = restServiceHandler;
        this.rootScope = $rootScope;
        this.log.debug('LicenseManagerService Instanced');
    }

    testService(callback) {
        this.restService.licenseManagerServiceHandler().getLicense((data) => {
            return callback(data);
        });
    }

    resubmitLicenseRequest(license, callback) {
        this.restService.licenseManagerServiceHandler().resubmitLicenseRequest(license, (data) => {
            this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Request License was successfully.'});
            return callback(data);
        });
    }

    emailRequest(license, callback) {
        this.restService.licenseManagerServiceHandler().emailRequest(license, (data) => {
            this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Request License was successfully.'});
            return callback(data);
        });
    }

    getLicenseList(callback) {
        this.restService.licenseManagerServiceHandler().getLicenseList((data) => {
            return callback(data);
        });
    }

    applyLicense(license, callback) {
        this.restService.licenseManagerServiceHandler().applyLicense(license, (data) => {
            //if(data.applied) {
                this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully applied'});
            /*} else {
                this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was successfully applied'});
            }*/
            return callback(data);
        });
    }

    deleteLicense(license, callback) {
        this.restService.licenseManagerServiceHandler().deleteLicense(license, (data) => {
            return callback(data);
        });
    }

    /**
     * Create a New License passing params
     * @param newLicense
     * @param callback
     */
    createNewLicenseRequest(newLicense, callback){
        this.restService.licenseManagerServiceHandler().createNewLicenseRequest(newLicense, (data) => {
            return callback(data);
        });
    }
}

