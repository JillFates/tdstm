/**
 * Created by Jorge Morayta on 09/25/2016.
 */
'use strict';

export default class LicenseAdminList {

    constructor($log, $state, licenseAdminService, $uibModal) {
        this.log = $log;
        this.state = $state;
        this.licenseGrid = {};
        this.licenseGridOptions = {};
        this.licenseAdminService = licenseAdminService;
        this.uibModal = $uibModal;
        this.openLastLicenseId = 0;

        this.getDataSource();
        this.log.debug('LicenseAdminList Instanced');
    }

    getDataSource() {
        this.licenseGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseAdminList.onRequestNewLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Request New License</button> <div ng-click="licenseAdminList.reloadLicenseAdminList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5,
                pageSize: 20
            },
            columns: [
                {field: 'licenseId', hidden: true },
                {field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' },
                {field: 'client.name', title: 'Client'},
                {field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>'},
                {field: 'email', title: 'Contact Email'},
                {field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>'},
                {field: 'type.name', title: 'Type',  template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#'},
                {field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>'},
                {field: 'method.max', title: 'Server/Tokens'},
                {field: 'activationDate', title: 'Inception', type: 'date', format : '{0:dd/MMM/yyyy}', template: '{{ dataItem.activationDate | convertDateIntoTimeZone }}' },
                {field: 'expirationDate', title: 'Expiration', type: 'date', format : '{0:dd/MMM/yyyy}', template: '{{ dataItem.expirationDate | convertDateIntoTimeZone }}' },
                {field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>'}
            ],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        this.licenseAdminService.getLicenseList((data) => {
                           e.success(data);
                       });
                    }
                },
                sort: {
                    field: 'project.name',
                    dir: 'asc'
                },
                change:  (e) => {
                    // We are coming from a new imported request license
                    if(this.openLastLicenseId !== 0 && this.licenseGrid.dataSource._data) {
                        var lastLicense = this.licenseGrid.dataSource._data.find((license) => {
                            return license.id === this.openLastLicenseId;
                        });

                        this.openLastLicenseId = 0;

                        if(lastLicense) {
                            this.onLicenseDetails(lastLicense);
                        }
                    }
                }
            },
            sortable: true,
            filterable: {
                extra: false
            }
        };
    }

    /**
     * Open a dialog with the Basic Form to request a New License
     */
    onRequestNewLicense() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseAdmin/request/RequestLicense.html',
            controller: 'RequestLicense as requestLicense',
            size: 'md'
        });

        modalInstance.result.then((license) => {
            this.log.info('New License Created: ', license);
            this.onNewLicenseCreated(license);
            this.reloadLicenseAdminList();
        }, () => {
            this.log.info('Request Canceled.');
        });
    }

    /**
     * After clicking on edit, we redirect the user to the Edition screen instead of open a dialog
     * du the size of the inputs
     */
    onLicenseDetails(license) {
        this.log.info('Open Details for: ', license);
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseAdmin/detail/LicenseDetail.html',
            controller: 'LicenseDetail as licenseDetail',
            size: 'lg',
            resolve: {
                params: function () {
                    var dataItem = {};
                    if(license && license.dataItem) {
                        dataItem = license.dataItem;
                    } else {
                        dataItem = license;
                    }
                    return { license: dataItem };
                }
            }
        });

        modalInstance.result.then((data) => {
            this.openLastLicenseId = 0;
            if(data.updated) {
                this.openLastLicenseId = data.id; // take this param from the last imported license, of course
            }

            this.reloadLicenseAdminList();
        }, () => {
            this.log.info('Request Canceled.');
        });
    }

    onNewLicenseCreated(license) {
        this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseAdmin/created/CreatedLicense.html',
            size: 'md',
            controller: 'CreatedLicense as createdLicense',
            resolve: {
                params: function () {
                    return { email: license.email  };
                }
            }
        });
    }

    reloadLicenseAdminList() {
        if(this.licenseGrid.dataSource) {
            this.licenseGrid.dataSource.read();
        }
    }

}