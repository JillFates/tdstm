/**
 * Created by Jorge Morayta on 09/25/2016.
 */
'use strict';

export default class LicenseManagerController {

    constructor($log, licenseManagerService, $uibModal) {
        this.log = $log;
        this.licenseGridOptions = {};
        this.licenseManagerService = licenseManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        this.getLicenseList();
        this.log.debug('LicenseManagerController Instanced');
    }

    getDataSource() {
        this.licenseGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseManager.requestNewLicenseManager()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Request New License</button> <div onclick="loadGridBundleList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5
            },
            columns: [
                {field: 'action', title: 'Action'},
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
    requestNewLicenseManager() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseManager/requestLicense/RequestLicenseView.html',
            controller: 'RequestLicenseController as requestLicense',
            size: 'md',
            resolve: {
                items: function () {
                    return ['1','a2','gg'];
                }
            }
        });

        modalInstance.result.then((selectedItem) => {
            this.debug(selectedItem);
        }, () => {
            this.log.info('Modal dismissed at: ' + new Date());
        });
    }

    getLicenseList() {
        this.licenseManagerService.getLicenseList((data) => {
            this.log.info(data);
        });
    }


}