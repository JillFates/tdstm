/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

export default class RequestLicense {

    constructor($log, licenseAdminService, $uibModalInstance, params) {
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;
        this.getEnvironmentDataSource();
        this.getProjectDataSource();
        this.newLicenseModel = {
            contactEmail: '',
            environmentId: 0,
            projectId: 0,
            client: params,
            specialInstructions: ''
        }
    }

    /**
     * Populate the Environment dropdown values
     */
    getEnvironmentDataSource() {
        this.environmentDataSource = [
            {environmentId: 1, name: 'Production'},
            {environmentId: 2, name: 'Demo'}
        ];
    }

    /**
     * Populate the Project dropdown values
     */
    getProjectDataSource() {
        this.projectDataSource = [
            {projectId: 1, name: 'Multi-Project'},
            {projectId: 2, name: 'DR Relo'}
        ];
    }

    /**
     * Execute the Service call to generate a new License request
     */
    saveLicenseRequest() {
        this.log.info('New License Requested: ', this.newLicenseModel);
        this.licenseAdminService.createNewLicenseRequest(this.newLicenseModel, (data) => {
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