/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

export default class RequestLicenseController {

    constructor($log, licenseManagerService, $uibModalInstance) {
        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.getEnvironmentDataSource();
        this.getProjectDataSource();
    }

    /**
     * Populate the Environment dropdown values
     */
    getEnvironmentDataSource() {
        this.environmentDataSource = [
            {id: 1, name: 'Production'},
            {id: 2, name: 'Demo'}
        ];
    }

    /**
     * Populate the Project dropdown values
     */
    getProjectDataSource() {
        this.projectDataSource = [
            {id: 1, name: 'n/a'},
            {id: 2, name: 'DR Relo'}
        ];
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}