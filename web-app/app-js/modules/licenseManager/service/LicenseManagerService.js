/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

export default class LicenseManagerService {

    constructor($log, restServiceHandler, $rootScope) {
        this.log = $log;
        this.restService = restServiceHandler;
        this.rootScope = $rootScope;
        this.statusSuccess = 'success';
        this.log.debug('licenseManagerService Instanced');
    }

    getLicenseList(onSuccess) {
        this.restService.licenseManagerServiceHandler().getLicenseList((data) => {
            return onSuccess(data.data);
        });
    }

    /**
     * Save the License
     */
    saveLicense(license, callback) {
        this.restService.licenseManagerServiceHandler().saveLicense(license, (data) => {
            return callback(data);
        });
    }
    /**
     * Does the activation of the current license if this is not active
     * @param license
     * @param callback
     */
    activateLicense(license, callback) {
        this.restService.licenseManagerServiceHandler().activateLicense(license, (data) => {
            this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'The license was activated and the license was emailed.'});
            return callback(data);
        });
    }

    /**
     * Make the request to Import the license, if fails, throws an exception visible for the user to take action
     * @param license
     * @param callback
     */
    importLicense(license, onSuccess, onError) {
        var hash = {
            data: license.hash
        };

        this.restService.licenseManagerServiceHandler().requestImport(hash, (data) => {
            if(data.status === this.statusSuccess) {
                this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully Imported'});
            } else {
                this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was not applied. Review the provided License Key is correct.'});
                return onError({ success: false});
            }
            return onSuccess(data);
        });
    }

    revokeLicense(license, callback) {
        this.restService.licenseManagerServiceHandler().revokeLicense(license, (data) => {
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

