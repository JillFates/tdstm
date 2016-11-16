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
                buttonCount: 5
            },
            columns: [
                {field: 'licenseId', hidden: true },
                {field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseDetails(this)"><span class="glyphicon glyphicon-edit"></span></button>' },
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
                                    principal: 'EMC',
                                    client: 'n/a',
                                    project: 'n/a',
                                    contact_email: 'west.coast@xyyy.com',
                                    status: 'Active',
                                    type: 'Multi-Project',
                                    method:  {
                                        id: 1,
                                        name: 'Server'
                                    },
                                    servers_tokens: '5000',
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
                                },
                                {
                                    licenseId: 2,
                                    keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                    action: '',
                                    principal: 'IBM',
                                    client: 'n/a',
                                    project: 'n/a',
                                    contact_email: 'west.coast@xyyy.com',
                                    status: 'Pending',
                                    type: 'Project',
                                    method:  {
                                        id: 1,
                                        name: 'Token'
                                    },
                                    servers_tokens: '40000',
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
                            this.licenseGridOptions.dataSource.data = data;
                            e.success(data);
                       /* });*/
                    }
                },
                change:  (e) => {
                    // We are coming from a new imported request license
                    if(this.openLastImportedLicenseId !== 0 && this.licenseGrid.dataSource._data) {
                        var newLicenseCreated = this.licenseGrid.dataSource._data.find((license) => {
                            return license.licenseId === this.openLastImportedLicenseId;
                        });

                        if(newLicenseCreated) {
                            this.onLicenseDetails(newLicenseCreated);
                        }
                    }
                }
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

        modalInstance.result.then((data) => {
            this.openLastImportedLicenseId = 1; // take this param from the last imported license, of course
            this.reloadLicenseManagerList();
        });
    }

    /**
     * After clicking on edit, we redirect the user to the Edition screen instead of open a dialog
     * du the size of the inputs
     */
    onLicenseDetails(license) {
        this.log.info('Open Details for: ', license);
        /*var modalInstance = this.uibModal.open({
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
        });*/
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

    reloadLicenseManagerList() {
        if(this.licenseGrid.dataSource) {
            this.licenseGrid.dataSource.read();
        }
    }


}