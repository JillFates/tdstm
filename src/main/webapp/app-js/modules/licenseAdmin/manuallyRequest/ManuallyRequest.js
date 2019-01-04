/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

export default class ManuallyRequest {

    constructor($log, $scope, licenseAdminService, $uibModalInstance, params) {
        this.log = $log;
        this.scope = $scope;
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseEmailModel = {
            id:  params.license.id
        };

        // Get the hash code using the id.
        this.getEmailContent();
    }


    getEmailContent() {
        this.licenseAdminService.getEmailContent(this.licenseEmailModel.id, (data) => {
            this.licenseEmailModel = data;
            window.TDSTM.safeApply(this.scope);
        });
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}