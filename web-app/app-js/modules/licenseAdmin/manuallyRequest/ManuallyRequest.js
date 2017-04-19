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
        this.licenseModel = {
            id:  params.license.id,
            email: params.license.email,
            toEmail: params.license.toEmail,
            encryptedDetail: ''
        };

        // Get the hash code using the id.
        this.getHashCode();
    }


    getHashCode() {
        this.licenseAdminService.getHashCode(this.licenseModel.id, (data) => {
            this.licenseModel.encryptedDetail = data;
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