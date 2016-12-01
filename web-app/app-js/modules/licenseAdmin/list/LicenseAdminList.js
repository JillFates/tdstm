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

        this.getDataSource();
        this.log.debug('LicenseAdminList Instanced');
    }

    getDataSource() {
        this.licenseGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseAdminList.onRequestNewLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Request New License</button> <div ng-click="licenseAdminList.reloadLicenseAdminList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5
            },
            columns: [
                {field: 'licenseId', hidden: true },
                {field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' },
                {field: 'client.name', title: 'Client'},
                {field: 'project.name', title: 'Project'},
                {field: 'email', title: 'Contact Email'},
                {field: 'status.name', title: 'Status'},
                {field: 'type.name', title: 'Type'},
                {field: 'method.name', title: 'Method'},
                {field: 'method.id', hidden: true},
                {field: 'serversTokens', title: 'Server/Tokens', template: '#:maxServers#'},
                {field: 'requestDate', title: 'Inception', type: 'date', format : '{0:dd/MMM/yyyy}' },
                {field: 'expirationDate', title: 'Expiration', type: 'date', format : '{0:dd/MMM/yyyy}' },
                {field: 'environment.name', title: 'Env.'}
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
                    field: 'title',
                    dir: 'asc'
                }
            },
            sortable: true
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
            size: 'md',
            draggable: true
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
                    var dataItem = license && license.dataItem;
                    return { license: dataItem };
                }
            }
        });

        modalInstance.result.then(() => {
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