/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

export default class ApplyLicenseKey {

    constructor($log, licenseAdminService, $uibModalInstance, params) {
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = params.license;
    }

    /**
     * Execute and validate the Key is correct
     */
    applyKey() {
        this.licenseAdminService.applyLicense(this.licenseModel, (data) => {
            this.uibModalInstance.close(data);
        }, (data)=> {
            this.uibModalInstance.close(data);
        });
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}