/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

export default class LicenseManagerService {

    constructor($log, restServiceHandler, $rootScope) {
        this.log = $log;
        this.restService = restServiceHandler;
        this.rootScope = $rootScope;
        this.log.debug('licenseManagerService Instanced');
    }

    getLicenseList(callback) {
        this.restService.licenseManagerServiceHandler().getLicenseList((data) => {
            return callback(data);
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
            this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Activate License was successfully.'});
            return callback(data);
        });
    }

    /**
     * Make the request to Import the license, if fails, throws an exception visible for the user to take action
     * @param license
     * @param callback
     */
    importLicense(license, callback) {
        this.restService.licenseManagerServiceHandler().requestImport(license, (data) => {
            //if(data.applied) {
                this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully Imported'});
            /*} else {
                this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was successfully applied'});
            }*/
            return callback(data);
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

