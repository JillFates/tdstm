/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

export default class ManuallyRequest {

    constructor($log, licenseManagerService, $uibModalInstance, params) {
        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = params.license;
    }

    /**
     * Execute and validate the Key is correct
     */
    emailRequest() {
        this.licenseManagerService.emailRequest(this.licenseModel, (data) => {
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