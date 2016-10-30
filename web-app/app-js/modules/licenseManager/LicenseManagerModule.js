/**
 * Created by Jorge Morayta on 09/25/2016.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';

import LicenseList from './list/LicenseList.js';
import LicenseManagerService from './service/LicenseManagerService.js';
import RequestLicense from './request/RequestLicense.js';
import CreatedLicense from './created/CreatedLicense.js';
import ApplyLicenseKey from './applyLicenseKey/ApplyLicenseKey.js';
import ManuallyRequest from './manuallyRequest/ManuallyRequest.js';
import LicenseDetail from './detail/LicenseDetail.js';


var LicenceManagerModule = angular.module('TDSTM.LicenseManagerModule', [uiRouter]).config(['$stateProvider',  '$translatePartialLoaderProvider',
    function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('licenseManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider
        .state('licenseList', {
            data: {page: {title: 'Administer Licenses', instruction: '', menu: ['ADMIN', 'LICENSE', 'LIST']}},
            url: '/license/list',
            views: {
                'headerView@': header,
                'bodyView@': {
                    templateUrl: '../app-js/modules/licenseManager/list/LicenseList.html',
                    controller: 'LicenseList as licenseList'
                }
            }
        });
}]);

// Services
LicenceManagerModule.service('LicenseManagerService', ['$log', 'RestServiceHandler', '$rootScope', LicenseManagerService]);

// Controllers
LicenceManagerModule.controller('LicenseList', ['$log', '$state', 'LicenseManagerService', '$uibModal', LicenseList]);

// Modal - Controllers
LicenceManagerModule.controller('RequestLicense', ['$log', 'LicenseManagerService', '$uibModalInstance', 'params', RequestLicense]);
LicenceManagerModule.controller('CreatedLicense', ['$log', '$uibModalInstance', 'params', CreatedLicense]);
LicenceManagerModule.controller('ApplyLicenseKey', ['$log', 'LicenseManagerService', '$uibModalInstance', '$rootScope', 'params', ApplyLicenseKey]);
LicenceManagerModule.controller('ManuallyRequest', ['$log', 'LicenseManagerService', '$uibModalInstance', 'params', ManuallyRequest]);
LicenceManagerModule.controller('LicenseDetail', ['$log', 'LicenseManagerService', '$uibModal', '$uibModalInstance', 'params', LicenseDetail]);


export default LicenceManagerModule;