/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

import FormValidator from '../../utils/form/FormValidator.js';

export default class RequestImport extends FormValidator{

    constructor($log, $scope, licenseManagerService, $uibModal, $uibModalInstance) {
        super($log, $scope, $uibModal, $uibModalInstance);

        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = {
            license: ''
        };

        this.saveForm(this.licenseModel);
    }

    /**
     * Execute and validate the Key is correct
     */
    onImportLicense() {
        if(this.isDirty()) {
            this.licenseManagerService.importLicense(this.licenseModel, (data) => {
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