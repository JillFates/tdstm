/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

export default class LicenseManagerService {

    constructor($log, RestServiceHandler) {
        this.log = $log;
        this.restService = RestServiceHandler;

        this.log.debug('LicenseManagerService Instanced');
    }

    testService(callback) {
        this.restService.LicenseManagerServiceHandler().getLicense((data) => {
            return callback(data);
        });
    }

    getLicenseList(callback) {
        this.restService.LicenseManagerServiceHandler().getLicenseList((data) => {
            return callback(data);
        });
    }
}

