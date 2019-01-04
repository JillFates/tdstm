/**
 * Created by Jorge Morayta on 09/25/2016.
 */
'use strict';

export default class LicenseManagerList {

    constructor($log, $state, licenseManagerService, $uibModal) {
        this.log = $log;
        this.state = $state;
        this.licenseGrid = {};
        this.licenseGridOptions = {};
        this.licenseManagerService = licenseManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        //this.getLicenseList();
        this.log.debug('LicenseManagerList Instanced');
        this.openLastImportedLicenseId = 0;
    }


    getDataSource() {
        this.licenseGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseManagerList.onRequestImportLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Import License Request</button> <div ng-click="licenseManagerList.reloadLicenseManagerList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5,
                pageSize: 20
            },
            columns: [
                {field: 'id', hidden: true },
                {field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseManagerDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' },
                {field: 'owner.name', title: 'Owner'},
                {field: 'websitename', title: 'Website Name'},
                {field: 'client.name', title: 'Client'},
                {field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>'},
                {field: 'email', title: 'Contact Email'},
                {field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>'},
                {field: 'type.name', title: 'Type',  template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#'},
                {field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>'},
                {field: 'method.max', title: 'Server/Tokens'},
                {field: 'activationDate', title: 'Inception', type: 'date', format : '{0:dd/MMM/yyyy}', template: '{{ dataItem.activationDate | convertDateIntoTimeZone }}' },
                {field: 'expirationDate', title: 'Expiration', type: 'date', format : '{0:dd/MMM/yyyy}', template: '{{ dataItem.expirationDate | convertDateIntoTimeZone }}' },
                {field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>'},
                {field:'gracePeriodDays', hidden: true}
            ],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        this.licenseManagerService.getLicenseList((data) => {
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
                    if(this.openLastImportedLicenseId !== 0 && this.licenseGrid.dataSource._data) {
                        var newLicenseCreated = this.licenseGrid.dataSource._data.find((license) => {
                            return license.id === this.openLastImportedLicenseId;
                        });

                        this.openLastImportedLicenseId = 0;

                        if(newLicenseCreated) {
                            this.onLicenseManagerDetails(newLicenseCreated);
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
     * The user Import a new License
     */
    onRequestImportLicense() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseManager/requestImport/RequestImport.html',
            controller: 'RequestImport as requestImport',
            size: 'md'
        });

        modalInstance.result.then((licenseImported) => {
            this.openLastImportedLicenseId = licenseImported.id; // take this param from the last imported license, of course
            this.reloadLicenseManagerList();
        });
    }

    /**
     * After clicking on edit, we redirect the user to the Edition screen instead of open a dialog
     * du the size of the inputs
     */
    onLicenseManagerDetails(license) {
        this.log.info('Open Details for: ', license);
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseManager/detail/LicenseManagerDetail.html',
            controller: 'LicenseManagerDetail as licenseManagerDetail',
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

        modalInstance.result.then(() => {
            this.reloadLicenseManagerList();
        }, () => {
            this.log.info('Request Canceled.');
        });
    }


    reloadLicenseManagerList() {
        if(this.licenseGrid.dataSource) {
            this.licenseGrid.dataSource.read();
        }
    }


}