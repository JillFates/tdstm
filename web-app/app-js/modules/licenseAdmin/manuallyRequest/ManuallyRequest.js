/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

export default class ManuallyRequest {

    constructor($log, licenseAdminService, $uibModalInstance, params) {
        this.log = $log;
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = params.license;
        this.licenseModel.encryptedDetail = '';

        // Init
        this.getHashCode();
    }


    getHashCode() {
        this.licenseAdminService.getHashCode(this.licenseModel.id, (data) => {
            this.licenseModel.encryptedDetail = '-----BEGIN HASH-----\n' + data + '\n-----END HASH-----';
        });
    }

    /**
     * Execute and validate the Key is correct
     */
    emailRequest() {
        this.licenseAdminService.emailRequest(this.licenseModel, (data) => {
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