/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

import FormValidator from '../../utils/form/FormValidator.js';

export default class LicenseManagerDetail extends FormValidator{

    constructor($log, $scope, licenseManagerService, userPreferencesService, $uibModal, $uibModalInstance, params, timeZoneConfiguration) {
        super($log, $scope, $uibModal, $uibModalInstance);
        this.scope = $scope;
        this.licenseManagerService = licenseManagerService;
        this.userPreferencesService = userPreferencesService;
        this.uibModalInstance = $uibModalInstance;
        this.uibModal =$uibModal;
        this.log = $log;

        this.editMode = false;

        this.timeZoneConfiguration = timeZoneConfiguration;

        this.licenseModel = {
            id: params.license.id,
            ownerName: params.license.owner.name,
            email: params.license.email,
            project: {
                id: params.license.project.id,
                name: params.license.project.name,
            },
            clientId: params.license.client.id,
            clientName: params.license.client.name,
            status: params.license.status,
            method: {
                name: params.license.method.name,
                max: params.license.method.max,
            },
            environment: params.license.environment,
            requestDate: params.license.requestDate,
            initDate: (params.license.activationDate !== null)? angular.copy(params.license.activationDate) : '',
            endDate: (params.license.expirationDate !== null)? angular.copy(params.license.expirationDate) : '',
            specialInstructions: params.license.requestNote,
            websiteName: params.license.websitename,

            bannerMessage: params.license.bannerMessage,
            requestedId: params.license.requestedId,
            replaced: params.license.replaced,
            replacedId: params.license.replacedId,
            hostName: params.license.hostName,
            hash: params.license.id,
            gracePeriodDays: params.license.gracePeriodDays,

            applied: params.license.applied,
            keyId: params.license.keyId
        };

        this.licenseKey = 'Licenses has not been issued';

        // Defined the Environment Select
        this.selectEnvironment = {};
        this.selectEnvironmentListOptions = [];
        this.getEnvironmentDataSource();

        // Defined the Status Select List
        this.selectStatus = [];

        // Init the two Kendo Dates for Init and EndDate
        this.initDate = {};
        this.initDateOptions = {
            format: this.userPreferencesService.getConvertedDateFormatToKendoDate(),
            open: ((e) => {
                this.onChangeInitDate();
            }),
            change: ((e) => {
                this.onChangeInitDate();
            })
        };

        this.endDate = {};
        this.endDateOptions = {
            format: this.userPreferencesService.getConvertedDateFormatToKendoDate(),
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
     * Controls what buttons to show
     */
    prepareControlActionButtons() {
        this.pendingLicense = this.licenseModel.status === 'PENDING' && !this.editMode;
        this.expiredOrTerminated = (this.licenseModel.status === 'EXPIRED' || this.licenseModel.status === 'TERMINATED');
        this.activeShowMode = this.licenseModel.status === 'ACTIVE' && !this.expiredOrTerminated && !this.editMode;
    }

    prepareMethodOptions() {
        this.methodOptions = [
            {
                name: 'MAX_SERVERS',
                text: 'Servers',
                max: 0
            },
            {
                name: 'TOKEN',
                text: 'Tokens',
                max: 0
            },
            {
                name: 'CUSTOM',
                text: 'Custom'
            }
        ]
    }

    prepareLicenseKey() {
        if(this.licenseModel.status === 'ACTIVE') {
            this.licenseManagerService.getKeyCode(this.licenseModel.id, (data) => {
                if(data) {
                    this.licenseKey = data;
                    window.TDSTM.safeApply(this.scope);
                }
            });
        }
    }

    prepareActivityList() {

        this.activityGrid = {};
        this.activityGridOptions = {
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5,
                pageSize: 20
            },
            columns: [
                {field: 'dateCreated', title: 'Date', width:160, type: 'date', format : '{0:dd/MMM/yyyy h:mm:ss tt}', template: '{{ dataItem.dateCreated | convertDateTimeIntoTimeZone }}' },
                {field: 'author.personName', title: 'Whom',  width:160},
                {field: 'changes', title: 'Action', template: '<table class="inner-activity_table"><tbody><tr><td></td><td class="col-action_td"><span class="glyphicon glyphicon-minus" aria-hidden="true"></span></td><td class="col-action_td"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></td></tr>#for(var i = 0; i < data.changes.length; i++){#<tr><td style="font-weight: bold;">#=data.changes[i].field# </td><td class="col-value_td"><span class="activity-list-old-val" style="color:darkred; font-weight: bold;">{{ \'#=data.changes[i].oldValue#\' | convertDateIntoTimeZone }}</span></td><td class="col-value_td"><span class="activity-list-new-val" style="color: green; font-weight: bold;">{{ \'#=data.changes[i].newValue#\' | convertDateIntoTimeZone }}</td></tr>#}#</tbody></table>'},
            ],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        this.licenseManagerService.getActivityLog(this.licenseModel, (data) => {
                            e.success(data.data);
                        });
                    }
                },
                sort: {
                    field: 'dateCreated',
                    dir: 'asc'
                }
            },
            scrollable: true
        };
    }

    /**
     * If by some reason the License was not applied at first time, this will do a request for it
     */
    activateLicense() {
        this.licenseManagerService.activateLicense(this.licenseModel, (data) => {
            if (data) {
                this.licenseModel.status = 'ACTIVE';
                this.saveForm(this.licenseModel);
                this.prepareControlActionButtons();
                this.prepareLicenseKey();
                this.reloadRequired = true;
                this.reloadLicenseManagerList();
            }
        });
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

    deleteLicense() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
            controller: 'DialogAction as dialogAction',
            size: 'sm',
            resolve: {
                params: () => {
                    return { title: 'Confirmation Required', message: 'You are about to delete the selected license. Are you sure? Click Confirm to delete otherwise press Cancel.'};
                }
            }
        });

        modalInstance.result.then(() => {
            this.licenseManagerService.deleteLicense(this.licenseModel, (data) => {
                this.uibModalInstance.close(data);
            });
        });
    }


    /**
     * If by some reason the License was not applied at first time, this will do a request for it
     */
    manuallyRequest() {
        this.licenseManagerService.manuallyRequest(this.licenseModel, (data) => {});
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
            this.$log.warn('Invalid Number Exception', model);
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
                this.reloadLicenseManagerList();
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
                            if(!this.licenseModel.environment) {
                                this.licenseModel.environment = data[0];
                            }

                            this.saveForm(this.licenseModel);
                            return e.success(data);
                        })
                    }
                }
            },
            valueTemplate: '<span style="text-transform: capitalize;">#=((data)? data.toLowerCase(): "" )#</span>',
            template: '<span style="text-transform: capitalize;">#=((data)? data.toLowerCase(): "" )#</span>',
            valuePrimitive: true
        };
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
     * Depending the number of fields and type of field, the reset can't be on the FormValidor, at least not now
     */
    onResetForm() {
        this.resetDropDown(this.selectEnvironment, this.licenseModel.environment);
        this.onChangeInitDate();
        this.onChangeEndDate();

        this.editMode = false;
        this.prepareControlActionButtons();
    }

    /**
     * Manual reload after a change has been performed to the License
     */
    reloadLicenseManagerList() {
        if(this.activityGrid.dataSource) {
            this.activityGrid.dataSource.read();
        }
    }

}