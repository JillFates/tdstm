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
        //this.getLicenseList();
        this.log.debug('LicenseManagerList Instanced');
    }

    getDataSource() {
        this.licenseGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseList.onRequestNewLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Import License Request</button> <div onclick="loadGridBundleList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5
            },
            columns: [
                {field: 'licenseId', hidden: true },
                {field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseList.onLicenseDetails(this)"><span class="glyphicon glyphicon-edit"></span></button>' },
                {field: 'principal', title: 'Principal'},
                {field: 'client', title: 'Client'},
                {field: 'project', title: 'Project'},
                {field: 'contact_email', title: 'Contact Email'},
                {field: 'status', title: 'Status'},
                {field: 'type', title: 'Type'},
                {field: 'method.name', title: 'Method'},
                {field: 'method.id', hidden: true},
                {field: 'servers_tokens', title: 'Server/Tokens'},
                {field: 'inception', title: 'Inception'},
                {field: 'expiration', title: 'Expiration'},
                {field: 'environment', title: 'Env.'}
            ],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        /*this.licenseManagerService.testService((data) => {*/
                            var data = [
                                {
                                    licenseId: 1,
                                    keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                    action: '',
                                    client: 'n/a',
                                    project: 'n/a',
                                    contact_email: 'west.coast@xyyy.com',
                                    status: 'Active',
                                    type: 'Multi-Project',
                                    method:  {
                                        id: 1,
                                        name: 'Server'
                                    },
                                    servers_tokens: '8000',
                                    inception: '2016-09-15',
                                    expiration: '2016-12-01',
                                    environment: 'Production',
                                    specialInstructions: 'Help, Help, Help',
                                    applied: false,
                                    replaced: {
                                        date: new Date(),
                                        serverUrl: 'http:blablaba.com',
                                        name: 'aasdas54-5asd4a5sd-asd45a4sd'
                                    },
                                    encryptedDetail: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618'
                                }
                            ];
                            e.success(data);
                       /* });*/
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
            templateUrl: '../app-js/modules/licenseAdmin/request/RequestLicense.html',
            controller: 'RequestLicense as requestLicense',
            size: 'md',
            draggable: true,
            resolve: {
                params: function () {
                    return { id: 50, name: 'Acme, Inc.', email: 'acme@inc.com' };
                }
            }
        });

        modalInstance.result.then((license) => {
            this.log.info('New License Created: ', license);
            this.onNewLicenseCreated(license);
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

        }, () => {
            this.log.info('Request Canceled.');
        });
    }

    onNewLicenseCreated() {
        this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseAdmin/created/CreatedLicense.html',
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