/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

export default class LicenseDetail {

    constructor($log, licenseManagerService, $uibModalInstance, params) {
        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;
        this.LicenseModel = { }
    }


    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}