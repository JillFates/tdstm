/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

import FormValidator from '../../utils/form/FormValidator.js';

export default class LicenseManagerDetail extends FormValidator{

    constructor($log, $scope, licenseManagerService, $uibModal, $uibModalInstance, params) {
        super($log, $scope, $uibModal, $uibModalInstance);
        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.uibModal =$uibModal;
        this.log = $log;

        this.editMode = false;

        this.licenseModel = {
            id: params.license.id,
            ownerName: params.license.owner.name,
            email: params.license.email,
            projectId: params.license.project.id,
            clientId: params.license.client.id,
            clientName: params.license.client.name,
            statusId: params.license.status.id,
            method: {
                id: params.license.method.id,
                name: params.license.method.name,
                max: params.license.method.max,
            },
            environment: { id: params.license.environment.id },
            requestDate: params.license.requestDate,
            initDate: (params.license.activationDate !== null)? angular.copy(params.license.activationDate) : '',
            endDate: (params.license.expirationDate !== null)? angular.copy(params.license.expirationDate) : '',
            specialInstructions: params.license.requestNote,
            websiteName: params.license.websitename,

            bannerMessage: params.license.bannerMessage,
            requestedId: params.license.requestedId,
            replaced: params.license.replaced,
            replacedId: params.license.replacedId,
            activityList: params.license.activityList,
            hostName: params.license.hostName,
            hash: params.license.id,
            gracePeriodDays: params.license.gracePeriodDays,

            applied: params.license.applied,
            keyId: params.license.keyId
        };

        // Creates the Project Select List
        // Define the Project Select
        this.selectProject = {};
        this.selectProjectListOptions = [];
        this.getProjectDataSource();

        // Defined the Environment Select
        this.selectEnvironment = {};
        this.selectEnvironmentListOptions = [];
        this.getEnvironmentDataSource();

        // Defined the Status Select List
        this.selectStatus = [];
        this.getStatusDataSource();

        // Init the two Kendo Dates for Init and EndDate
        this.initDate = {};
        this.initDateOptions = {
            format: 'yyyy/MM/dd',
            open: ((e) => {
                this.onChangeInitDate();
            }),
            change: ((e) => {
                this.onChangeInitDate();
            })
        };

        this.endDate = {};
        this.endDateOptions = {
            format: 'yyyy/MM/dd',
            open: ((e) => {
                this.onChangeEndDate();
            }),
            change: ((e) => {
                this.onChangeEndDate();
            })
        };


        this.prepareMethodOptions();
        this.prepareLicenseKey();
        this.prepareActivityList();

        this.prepareControlActionButtons();

    }

    /**
     * Populate the Project dropdown values
     */
    getProjectDataSource() {
        this.selectProjectListOptions = {
            dataSource: {
                transport: {
                    read: (e) => {
                        this.licenseManagerService.getProjectDataSource((data) => {
                            if(!this.licenseModel.projectId) {
                                this.licenseModel.projectId = data[0].id;
                            }

                            this.saveForm(this.licenseModel);
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
                this.licenseModel.clientName = item.client.name;
            })
        };
    }

    /**
     * Controls what buttons to show
     */
    prepareControlActionButtons() {
        this.pendingLicense = this.licenseModel.statusId === 4 && !this.editMode;
        this.expiredOrTerminated = (this.licenseModel.statusId === 2 || this.licenseModel.statusId === 3);
        this.activeShowMode = this.licenseModel.statusId === 1 && !this.expiredOrTerminated && !this.editMode;
    }

    prepareMethodOptions() {
        this.methodOptions = [
            {
                id: 1,
                name: 'Servers',
                max: 0
            },
            {
                id: 2,
                name: 'Tokens',
                max: 0
            },
            {
                id: 3,
                name: 'Custom'
            }
        ]
    }

    prepareLicenseKey() {
        this.licenseManagerService.getKeyCode(this.licenseModel.id, (data) => {
            this.licenseKey = '-----BEGIN LICENSE REQUEST-----\n' + data + '\n-----END LICENSE REQUEST-----';
        });
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
    validateIntegerOnly(e,model){
        try {
            var newVal= parseInt(model);
            if(!isNaN(newVal)) {
                model = newVal;
            } else {
                model = 0;
            }
            if(e && e.currentTarget) {
                e.currentTarget.value = model;
            }
        } catch(e) {
            this.$log.warn('Invalid Number Expception', model);
        }
    }

    /**
     * Save current changes
     */
    saveLicense() {
        if(this.isDirty()) {
            this.editMode = false;
            this.prepareControlActionButtons();
            this.licenseManagerService.saveLicense(this.licenseModel, (data) => {
                this.reloadRequired = true;
                this.saveForm(this.licenseModel);
                this.log.info('License Saved');
            });
        } else {
            this.editMode = false;
            this.prepareControlActionButtons()
        }
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
    getEnvironmentDataSource() {
        this.selectEnvironmentListOptions = {
            dataSource: {
                transport: {
                    read: (e) => {
                        this.licenseManagerService.getEnvironmentDataSource((data) => {
                            if(!this.licenseModel.environmentId) {
                                this.licenseModel.environmentId = data[0].id;
                            }
                            return e.success(data);
                        })
                    }
                }
            },
            dataTextField: 'name',
            dataValueField: 'id',
            valuePrimitive: true
        };
    }

    /**
     * Populate values
     */
    getStatusDataSource() {
        this.selectStatusListOptions = {
            dataSource: [
                {id: 1, name: 'Active'},
                {id: 2, name: 'Expired'},
                {id: 3, name: 'Terminated'},
                {id: 4, name: 'Pending'}
            ],
            dataTextField: 'name',
            dataValueField: 'id',
            valuePrimitive: true
        }
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

            if(endDate) {
                if(this.initDate.value() > this.endDate.value()) {
                    endDate = new Date(endDate);
                    endDate.setDate(startDate.getDate());
                    this.licenseModel.endDate = endDate;
                }
            }
        }
    }

    onChangeEndDate(){
        var endDate = this.endDate.value(),
            startDate = this.initDate.value();

        if (endDate) {
            endDate = new Date(endDate);
            endDate.setDate(endDate.getDate());
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
            this.resetForm(()=> {
                this.onResetForm();
            });
        } else if(this.reloadRequired){
            this.uibModalInstance.close({});
        } else {
            this.uibModalInstance.dismiss('cancel');
        }
    }

    /**
     * Depeding the number of fields and type of field, the reset can't be on the FormValidor, at least not now
     */
    onResetForm() {
        // Reset Project Selector
        this.resetDropDown(this.selectProject, this.licenseModel.projectId);
        this.resetDropDown(this.selectStatus, this.licenseModel.statusId);
        this.resetDropDown(this.selectEnvironment, this.licenseModel.environment.id);
        this.onChangeInitDate();
        this.onChangeEndDate();

        this.editMode = false;
        this.prepareControlActionButtons();
    }

}