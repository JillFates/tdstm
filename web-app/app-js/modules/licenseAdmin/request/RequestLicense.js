/**
 * Created by Jorge Morayta on 09/26/2016.
 * Create a new Request to get a License
 */

'use strict';

export default class RequestLicense {

    /**
     * Initialize all the properties
     * @param $log
     * @param licenseAdminService
     * @param $uibModalInstance
     */
    constructor($log, licenseAdminService, $uibModalInstance) {
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;

        // Defined the Environment Select
        this.environmentDataSource = [];
        // Define the Project Select
        this.selectProject = {};
        this.selectProjectListOptions = [];

        this.getEnvironmentDataSource();
        this.getProjectDataSource();

        // Create the Model for the New License
        this.newLicenseModel = {
            email: '',
            environmentId: 0,
            projectId: 0,
            clientName: '',
            requestNote: ''
        }
    }

    /**
     * Populate the Environment dropdown values
     */
    getEnvironmentDataSource() {
        this.licenseAdminService.getEnvironmentDataSource((data)=>{
            this.environmentDataSource = data;
            this.newLicenseModel.environmentId = data[0].id;
        });
    }

    /**
     * Populate the Project dropdown values
     */
    getProjectDataSource() {
        this.selectProjectListOptions = {
            dataSource: {
                transport: {
                    read: (e) => {
                        this.licenseAdminService.getProjectDataSource((data) => {
                            this.newLicenseModel.projectId = data[0].id;
                            return e.success(data);
                        })
                    }
                }
            },
            dataTextField: 'name',
            dataValueField: 'id',
            valuePrimitive: true,
            select: ((e) => {
                // On Project Change, select the Client Name
                var item = this.selectProject.dataItem(e.item);
                this.newLicenseModel.clientName = item.client.name;
            })
        };
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