/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

export default class RequestImport {

    constructor($log, licenseManagerService, $uibModalInstance) {
        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = {
            license: ''
        };
    }

    /**
     * Execute and validate the Key is correct
     */
    onImportLicense() {
        this.licenseManagerService.importLicense(this.licenseModel, (data) => {
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