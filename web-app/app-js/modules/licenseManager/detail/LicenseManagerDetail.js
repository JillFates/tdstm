/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

export default class LicenseManagerDetail {

    constructor($log, licenseManagerService, $uibModal, $uibModalInstance, params) {
        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.uibModal =$uibModal;
        this.log = $log;

        this.editMode = false;
        this.licenseModel = {
            principalId: (params.license.principal)? params.license.principal.id : {},
            email: params.license.email,
            projectId: params.license.project.id,
            clientId: params.license.client.id,
            statusId: params.license.status.id,
            method: {
                id: params.license.method.id,
                name: params.license.method.name,
                quantity: params.license.method.quantity
            },
            environmentId: params.license.environment.id,
            requested: params.license.requested,
            initDate: params.license.initDate,
            endDate: params.license.endDate,
            specialInstructions: params.license.specialInstructions,
            bannerMessage: params.license.bannerMessage,
            requestedId: params.license.requestedId,
            replaced: params.license.replaced,
            replacedId: params.license.replacedId,
            licenseKey: params.license.licenseKey,
            activityList: params.license.activityList,
            hostName: params.license.hostName,
            websiteName: params.license.websiteName,
            hash: params.license.hash,

            applied: params.license.applied,
            keyId: params.license.keyId
        };

        // Creates the Kendo Project Select List
        this.selectProject = {};
        this.selectProjectListOptions = {
            dataSource: this.getProjectsDataSource(),
            optionLabel: 'Select a Project',
            dataTextField: 'name',
            dataValueField: 'id',
            valuePrimitive: true,
            select: ((e) => {
                var item = this.selectProject.dataItem(e.item);
                this.onChangeProject(item);
            })
        };

        // Init the two Kendo Dates for Init and EndDate
        this.initDate = {};
        this.initDateOptions = {
            format: 'yyyy/MM/dd',
            max: this.licenseModel.endDate,
            change: ((e) => {
                this.onChangeInitDate();
            })
        };

        this.endDate = {};
        this.endDateOptions = {
            format: 'yyyy/MM/dd',
            min: this.licenseModel.initDate,
            change: ((e) => {
                this.onChangeEndDate();
            })
        };

        this.prepareControlActionButtons();

        this.getPrincipalDataSource();
        this.getEnvironmentDataSource();
        this.getClientDataSource();
        this.getStatusDataSource();

        this.prepareMethodOptions();
        this.prepareActivityList();


    }

    /**
     * Controls what buttons to show
     */
    prepareControlActionButtons() {
        this.pendingLicense = this.licenseModel.statusId === 2 && !this.editMode;
        this.expiredOrTerminated = (this.licenseModel.statusId === 3 || this.licenseModel.statusId === 4);
        this.activeShowMode = this.licenseModel.statusId === 1 && !this.expiredOrTerminated && !this.editMode;
    }

    prepareMethodOptions() {
        this.methodOptions = [
            {
                id: 1,
                name: 'Servers',
                quantity: 8000
            },
            {
                id: 2,
                name: 'Tokens',
                quantity: 40000
            },
            {
                id: 3,
                name: 'Custom'
            }
        ]
    }

    prepareActivityList() {
        this.activityGrid = {};
        this.activityGridOptions = {
            columns: [
                {field: 'date', title: 'Date'},
                {field: 'whom', title: 'Whom'},
                {field: 'action', title: 'Action'}
            ],
            dataSource: this.licenseModel.activityList,
            scrollable: true
        };
    }

    /**
     * The user apply and server should validate the key is correct
     */
    applyLicenseKey() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseManager/applyLicenseKey/ApplyLicenseKey.html',
            controller: 'ApplyLicenseKey as applyLicenseKey',
            size: 'md',
            resolve: {
                params: () => {
                    return { license: this.licenseModel };
                }
            }
        });

        modalInstance.result.then(() => {
            this.licenseModel.applied = true;
        });
    }

    /**
     * If by some reason the License was not applied at first time, this will do a request for it
     */
    activateLicense() {
        this.licenseManagerService.activateLicense(this.licenseModel, (data) => {});
    }

    revokeLicense() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
            controller: 'DialogAction as dialogAction',
            size: 'sm',
            resolve: {
                params: () => {
                    return { title: 'Confirmation Required', message: 'Are you sure you want to revoke it? This action cannot be undone.'};
                }
            }
        });

        modalInstance.result.then(() => {
            this.licenseManagerService.revokeLicense(this.licenseModel, (data) => {
                this.uibModalInstance.close(data);
            });
        });
    }

    /**
     * Validate the input on Server or Tokens is only integer only
     * This will be converted in a more complex directive later
     * TODO: Convert into a directive
     */
    validateIntegerOnly(e){
        try {
            var newVal= parseInt(this.licenseModel.method.quantity);
            if(!isNaN(newVal)) {
                this.licenseModel.method.quantity = newVal;
            } else {
                this.licenseModel.method.quantity = 0;
            }

            if(e && e.currentTarget && e.currentTarget.value) {
                e.currentTarget.value = this.licenseModel.method.quantity;
            }
        } catch(e) {
            this.$log.warn('Invalid Number Expception', this.licenseModel.method.quantity);
        }
    }

    /**
     * Save current changes
     */
    saveLicense() {
        this.licenseManagerService.saveLicense(this.licenseModel, (data) => {
            this.uibModalInstance.close(data);
            this.log.info('License Saved');
        });
    }

    /**
     * Change the status to Edit
     */
    modifyLicense() {
        this.editMode = true;
        this.prepareControlActionButtons();
    }

    /**
     * Populate values
     */
    getPrincipalDataSource() {
        this.principalDataSource = [
            {id: 1, name: 'EMC'},
            {id: 2, name: 'IBM'}
        ];
    }

    /**
     * Populate values
     */
    getEnvironmentDataSource() {
        this.environmentDataSource = [
            {id: 1, name: 'Production'},
            {id: 2, name: 'Other'}
        ];
    }

    /**
     * Populate values
     */
    getProjectsDataSource() {
        return  [
            {id: 1, name: 'n/a'},
            {id: 2, name: 'Bank East'}
        ];
    }

    /**
     * Populate values
     */
    getClientDataSource() {
        this.clientsDataSource = [
            {id: 1, name: 'n/a'},
            {id: 2, name: 'Gold Bank'}
        ];
    }

    /**
     * Populate values
     */
    getStatusDataSource() {
        this.statusDataSource = [
            {id: 1, name: 'Active'},
            {id: 2, name: 'Pending'},
            {id: 3, name: 'Expired'},
            {id: 4, name: 'Terminated'}
        ];
    }

    /**
     * A new Project has been selected, that means we need to reload the next project section
     * @param item
     */
    onChangeProject(item) {
        this.log.info('On change Project', item);
    }

    onChangeInitDate() {
        var startDate = this.initDate.value(),
            endDate = this.endDate.value();

        if (startDate) {
            startDate = new Date(startDate);
            startDate.setDate(startDate.getDate());
            this.endDate.min(startDate);
        } else if (endDate) {
            this.initDate.max(new Date(endDate));
        } else {
            endDate = new Date();
            this.initDate.initDate.max(endDate);
            this.endDate.min(endDate);
        }
    }

    onChangeEndDate(){
        var endDate = this.endDate.value(),
            startDate = this.initDate.value();

        if (endDate) {
            endDate = new Date(endDate);
            endDate.setDate(endDate.getDate());
            this.initDate.max(endDate);
        } else if (startDate) {
            this.endDate.min(new Date(startDate));
        } else {
            endDate = new Date();
            this.initDate.max(endDate);
            this.endDate.min(endDate);
        }
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        if(this.editMode) {
            this.editMode = false;
            this.prepareControlActionButtons();
        } else {
            this.uibModalInstance.dismiss('cancel');
        }
    }

}