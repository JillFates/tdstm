/**
 * Created by Jorge Morayta on 09/25/2016.
 */
'use strict';

export default class LicenseList {

    constructor($log, $state, licenseManagerService, $uibModal) {
        this.log = $log;
        this.state = $state;
        this.licenseGridOptions = {};
        this.licenseManagerService = licenseManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        this.getLicenseList();
        this.log.debug('LicenseList Instanced');
    }

    getDataSource() {
        this.licenseGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseList.onRequestNewLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Request New License</button> <div onclick="loadGridBundleList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5
            },
            columns: [
                {field: 'licenseId', hidden: true },
                {field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseList.onLicenseDetails(#=licenseId#)"><span class="glyphicon glyphicon-edit"></span></button>' },
                {field: 'client', title: 'Client'},
                {field: 'project', title: 'Project'},
                {field: 'contact_email', title: 'Contact Email'},
                {field: 'status', title: 'Status'},
                {field: 'type', title: 'Type'},
                {field: 'method', title: 'Method'},
                {field: 'servers_tokens', title: 'Server/Tokens'},
                {field: 'inception', title: 'Inception'},
                {field: 'expiration', title: 'Expiration'},
                {field: 'environment', title: 'Env.'}
            ],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        this.licenseManagerService.testService((data) => {
                            e.success(data);
                        });
                    }
                }
            }
        };
    }

    /**
     * Open a dialog with the Basic Form to request a New License
     */
    onRequestNewLicense() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseManager/request/RequestLicense.html',
            controller: 'RequestLicense as requestLicense',
            size: 'md',
            resolve: {
                params: function () {
                    return { id: 50, name: 'Acme, Inc.', email: 'acme@inc.com' };
                }
            }
        });

        modalInstance.result.then((license) => {
            this.log.info('New License Created: ', license);
            this.onNewLicenseCreated();
        }, () => {
            this.log.info('Request Canceled.');
        });
    }

    /**
     * After clicking on edit, we redirect the user to the Edition screen instead of open a dialog
     * du the size of the inputs
     */
    onLicenseDetails(licenseId) {
        this.log.info('Open Details for: ', licenseId);
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseManager/detail/LicenseDetail.html',
            controller: 'LicenseDetail as licenseDetail',
            size: 'lg',
            resolve: {
                params: function () {
                    return { licenseId: licenseId };
                }
            }
        });

        modalInstance.result.then(() => {

        }, () => {
            this.log.info('Request Canceled.');
        });
    }

    onNewLicenseCreated() {
        this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseManager/created/CreatedLicense.html',
            size: 'md',
            controller: 'CreatedLicense as createdLicense',
            resolve: {
                params: function () {
                    return { id: 50, name: 'Acme, Inc.', email: 'acme@inc.com'  };
                }
            }
        });
    }

    getLicenseList() {
        this.licenseManagerService.getLicenseList((data) => {
            this.log.info(data);
        });
    }


}