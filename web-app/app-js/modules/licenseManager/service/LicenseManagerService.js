/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

export default class LicenseManagerService {

    constructor($log, restServiceHandler) {
        this.log = $log;
        this.restService = restServiceHandler;

        this.log.debug('LicenseManagerService Instanced');
    }

    testService(callback) {
        this.restService.licenseManagerServiceHandler().getLicense((data) => {
            return callback(data);
        });
    }

    getLicenseList(callback) {
        this.restService.licenseManagerServiceHandler().getLicenseList((data) => {
            return callback(data);
        });
    }

    createNewLicenseRequest(newLicense, callback){
        // Process New License data if necessary (add, remove, etc)
        this.restService.licenseManagerServiceHandler().createNewLicenseRequest(newLicense, (data) => {
            return callback(data);
        });
    }
}

