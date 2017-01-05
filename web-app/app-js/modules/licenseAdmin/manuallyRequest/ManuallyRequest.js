/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

export default class ManuallyRequest {

    constructor($log, licenseAdminService, $uibModalInstance, params) {
        this.log = $log;
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = {
            id:  params.license.id,
            email: params.license.email,
            encryptedDetail: ''
        };

        // Get the hash code using the id.
        this.getHashCode();
    }


    getHashCode() {
        this.licenseAdminService.getHashCode(this.licenseModel.id, (data) => {
            this.licenseModel.encryptedDetail = '-----BEGIN LICENSE REQUEST-----\n' + data + '\n-----END LICENSE REQUEST-----';
        });
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}