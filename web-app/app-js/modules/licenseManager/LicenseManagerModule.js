/**
 * Created by Jorge Morayta on 09/25/2016.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';

import LicenseManagerController from './main/LicenseManagerController.js';
import LicenseManagerService from './service/LicenseManagerService.js';
import RequestLicenseController from './requestLicense/RequestLicenseController.js';


var LicenceManagerModule = angular.module('TDSTM.LicenseManagerModule', [uiRouter]).config(['$stateProvider',  '$translatePartialLoaderProvider',
    function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('licenseManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider
        .state('licenseManager', {
            data: {page: {title: 'License Manager', instruction: '', menu: ['ADMINISTER_LICENSES']}},
            url: '/license/manager',
            views: {
                'headerView@': header,
                'bodyView@': {
                    templateUrl: '../app-js/modules/licenseManager/main/LicenseManagerView.html',
                    controller: 'LicenseManagerController as licenseManager'
                }
            }
        });
}]);

// Services
LicenceManagerModule.service('LicenseManagerService', ['$log', 'RestServiceHandler', LicenseManagerService]);

// Controllers
LicenceManagerModule.controller('LicenseManagerController', ['$log', 'LicenseManagerService', '$uibModal', LicenseManagerController]);

// Modal - Controllers
LicenceManagerModule.controller('RequestLicenseController', ['$log', 'LicenseManagerService', '$uibModalInstance', RequestLicenseController]);


export default LicenceManagerModule;