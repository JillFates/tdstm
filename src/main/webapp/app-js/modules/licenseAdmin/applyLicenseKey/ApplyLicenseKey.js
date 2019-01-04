/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

import FormValidator from '../../utils/form/FormValidator.js';

export default class ApplyLicenseKey extends FormValidator{

    constructor($log, $scope, licenseAdminService, $uibModal, $uibModalInstance, params) {
        super($log, $scope, $uibModal, $uibModalInstance)
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;

        this.licenseModel = {
            id: params.license.id,
            key: params.license.key
        }
        ;
        this.saveForm(this.licenseModel);
    }

    /**
     * Execute and validate the Key is correct
     */
    applyKey() {
        if(this.isDirty()) {
            this.licenseAdminService.applyLicense(this.licenseModel, (data) => {
                this.uibModalInstance.close(data);
            }, (data)=> {
                this.uibModalInstance.close(data);
            });
        }
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}