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
                {field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseManagerDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' },
                {field: 'principal.name', title: 'Principal'},
                {field: 'client.name', title: 'Client'},
                {field: 'project.name', title: 'Project'},
                {field: 'contact_email', title: 'Contact Email'},
                {field: 'status.type', title: 'Status'},
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
                        /*this.licenseManagerService.getLicenseList((data) => {*/
                            var data = [
                                {
                                    licenseId: 1,
                                    keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                    action: '',
                                    principal:  {
                                        id: 1,
                                        name: 'EMC'
                                    },
                                    client: {
                                        id: 1,
                                        name: 'n/a',
                                    },
                                    project: {
                                        id: 1,
                                        name: 'n/a'
                                    },
                                    contact_email: 'west.coast@xyyy.com',
                                    status: {
                                        id: 1,
                                        type: 'Active'
                                    },
                                    type: 'Multi-Project',
                                    method:  {
                                        id: 1,
                                        name: 'Server',
                                        quantity: 4000
                                    },
                                    initDate: '2016-09-15',
                                    endDate: '2016-09-18',
                                    requested: '2016-12-08',
                                    environment: {
                                        id: 1,
                                        name: 'Production'
                                    },
                                    specialInstructions: 'Help, Help, Help',
                                    bannerMessage: 'This application should only be used for training purpose',
                                    requestedId: '5646546546546545-asdasdasd54asd-asdas6asdasd',
                                    applied: false,
                                    replaced: [ {id: 1, status: '2016-05-08' + ' http:blablaba.com ' +' aasdas54-5asd4a5sd-asd45a4sd '}, {id: 2, status: '2016-05-10' + ' http:blablaba.com ' +' aasdas54-5asd4a5sd-asd45a4sd ' }],
                                    replacedId: 1,
                                    licenseKey: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618',
                                    activityList: [
                                        { date: '2016-05-08', whom: 'Harry Legs', action: 'Requested'},
                                        { date: '2016-05-10', whom: 'Robin Banks', action: 'Imported'},
                                        { date: '2016-05-11', whom: 'Ben D Ovar', action: 'Activated'},
                                        { date: '2016-05-14', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-18', whom: 'Ben D Ovar', action: 'Unlocked License to modify'},
                                        { date: '2016-05-20', whom: 'Ben D Ovar', action: 'Modified License detail'},
                                        { date: '2016-05-21', whom: 'Ben D Ovar', action: 'Activated'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'}
                                    ],
                                    hostName: 'tm-acme-bigmove-.somedomaing.local',
                                    websiteName: 'tranman.somecorp.com',
                                    hash: 'kaskldsaldkjasda5s4a65sda65sd4a65sd46a5sd4as65d'
                                },
                                {
                                    licenseId: 2,
                                    keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                    action: '',
                                    principal:  {
                                        id: 2,
                                        name: 'IBM'
                                    },
                                    client: {
                                        id: 2,
                                        name: 'Gold Bank',
                                    },
                                    project: {
                                        id: 2,
                                        name: 'Bank East'
                                    },
                                    contact_email: 'west.coast@xyyy.com',
                                    status: {
                                        id: 2,
                                        type: 'Pending'
                                    },
                                    type: 'Project',
                                    method:  {
                                        id: 2,
                                        name: 'Token',
                                        quantity: 5000
                                    },
                                    initDate: '2016-09-15',
                                    endDate: '2016-09-18',
                                    requested: '2016-12-08',
                                    environment: {
                                        id: 1,
                                        name: 'Production'
                                    },
                                    specialInstructions: 'Help, Help, Help',
                                    requestedId: '5646546546546545-asdasdasd54asd-asdas6asdasd',
                                    applied: false,
                                    bannerMessage: 'This application should only be used for training purpose',
                                    replaced: [ {id: 1, status: '2016-05-08' + ' http:blablaba.com ' +' aasdas54-5asd4a5sd-asd45a4sd '}, {id: 2, status: '2016-05-10' + ' http:blablaba.com ' +' aasdas54-5asd4a5sd-asd45a4sd ' }],
                                    replacedId: 1,
                                    licenseKey: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618',
                                    activityList: [
                                        { date: '2016-05-08', whom: 'Harry Legs', action: 'Requested'},
                                        { date: '2016-05-10', whom: 'Robin Banks', action: 'Imported'},
                                        { date: '2016-05-11', whom: 'Ben D Ovar', action: 'Activated'},
                                        { date: '2016-05-14', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-18', whom: 'Ben D Ovar', action: 'Unlocked License to modify'},
                                        { date: '2016-05-20', whom: 'Ben D Ovar', action: 'Modified License detail'},
                                        { date: '2016-05-21', whom: 'Ben D Ovar', action: 'Activated'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'},
                                        { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License'}
                                    ],
                                    hostName: 'tm-acme-bigmove-.somedomaing.local',
                                    websiteName: 'tranman.somecorp.com',
                                    hash: 'kaskldsaldkjasda5s4a65sda65sd4a65sd46a5sd4as65d'
                                }
                            ];
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

                        this.openLastImportedLicenseId = 0;

                        if(newLicenseCreated) {
                            this.onLicenseManagerDetails(newLicenseCreated);
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