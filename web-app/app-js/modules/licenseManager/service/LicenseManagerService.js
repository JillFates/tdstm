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


    getProjectDataSource(onSuccess) {
        this.restService.licenseManagerServiceHandler().getProjectDataSource((data) => {
            return onSuccess(data.data);
        });
    }

    getEnvironmentDataSource(onSuccess) {
        this.restService.licenseManagerServiceHandler().getEnvironmentDataSource((data) => {
            return onSuccess(data.data);
        });
    }

    getKeyCode(licenseId, onSuccess) {
        this.restService.licenseManagerServiceHandler().getKeyCode(licenseId, (data) => {
            return onSuccess(data.data);
        });
    }

    getTimeZoneConfiguration(onSuccess) {
        this.restService.commonServiceHandler().getTimeZoneConfiguration((data) => {
            return onSuccess(data.data);
        });
    }

    manuallyRequest(license, onSuccess) {
        this.restService.licenseManagerServiceHandler().manuallyRequest(license.id, (data) => {

            if (data.status === this.statusSuccess) {
                this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Email License was successfully' });
            } else {
                this.rootScope.$emit('broadcast-msg', { type: 'warning', text: data.data });
                return onSuccess({ success: false });
            }

            return onSuccess(data);
        });
    }

    /**
     * Save the License
     */
    saveLicense(license, onSuccess) {

        var licenseModified = {
            environment: license.environment,
            method: {
                name: license.method.name
            },
            activationDate: license.initDate ? moment(license.initDate).format('YYYY-MM-DD') : '',
            expirationDate: license.endDate ? moment(license.endDate).format('YYYY-MM-DD') : '',
            status: license.status,
            project: {
                id: (license.project.id !== 'all') ? parseInt(license.project.id) : license.project.id,  // We pass 'all' when is multiproject
                name: license.project.name
            },
            bannerMessage: license.bannerMessage,
            gracePeriodDays: license.gracePeriodDays,
            websitename: license.websiteName,
            hostName: license.hostName
        };
        if (license.method.name !== 'CUSTOM') {
            licenseModified.method.max = parseInt(license.method.max);
        }

        this.restService.licenseManagerServiceHandler().saveLicense(license.id, licenseModified, (data) => {
            return onSuccess(data);
        });
    }
    /**
     * Does the activation of the current license if this is not active
     * @param license
     * @param callback
     */
    activateLicense(license, callback) {
        this.restService.licenseManagerServiceHandler().activateLicense(license.id, (data) => {
            if (data.status === this.statusSuccess) {
                this.rootScope.$emit('broadcast-msg', {
                    type: 'info',
                    text: 'The license was activated and the license was emailed.'
                });
                return callback(data);
            } else {
                this.rootScope.$emit('broadcast-msg', {
                    type: 'warning',
                    text: data.data
                });
                return callback();
            }
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
            if (data.status === this.statusSuccess) {
                this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully Imported' });
            } else {
                this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was not applied. Review the provided License Key is correct.' });
                return onError({ success: false });
            }
            return onSuccess(data);
        });
    }

    revokeLicense(license, onSuccess) {
        this.restService.licenseManagerServiceHandler().revokeLicense(license, (data) => {
            return onSuccess(data);
        });
    }

    deleteLicense(license, onSuccess) {
        this.restService.licenseManagerServiceHandler().deleteLicense(license, (data) => {
            return onSuccess(data);
        });
    }

    getActivityLog(license, onSuccess) {
        this.restService.licenseManagerServiceHandler().getActivityLog(license.id, (data) => {
            return onSuccess(data);
        });
    }

    /**
     * Create a New License passing params
     * @param newLicense
     * @param callback
     */
    createNewLicenseRequest(newLicense, callback) {
        this.restService.licenseManagerServiceHandler().createNewLicenseRequest(newLicense, (data) => {
            return callback(data);
        });
    }
}

