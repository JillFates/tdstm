/**
 * Created by Jorge Morayta on 09/25/2016.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';

import LicenseManagerList from './list/LicenseManagerList.js';
import LicenseManagerService from './service/LicenseManagerService.js';
import RequestImport from './requestImport/RequestImport.js';
import LicenseManagerDetail from './detail/LicenseManagerDetail.js';


var LicenseManagerModule = angular.module('TDSTM.LicenseManagerModule', [uiRouter]).config(['$stateProvider',  '$translatePartialLoaderProvider',
    function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('licenseManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider
        .state('licenseManagerList', {
            data: {page: {title: 'Licensing Manager', instruction: '', menu: ['Manager', 'License', 'List']}},
            url: '/license/manager/list',
            views: {
                'headerView@': header,
                'bodyView@': {
                    templateUrl: '../app-js/modules/licenseManager/list/LicenseManagerList.html',
                    controller: 'LicenseManagerList as licenseManagerList'
                }
            }
        });
}]);

// Services
LicenseManagerModule.service('LicenseManagerService', ['$log', 'RestServiceHandler', '$rootScope', LicenseManagerService]);


// Controllers
LicenseManagerModule.controller('LicenseManagerList', ['$log', '$state', 'LicenseManagerService', '$uibModal', LicenseManagerList]);

// Modal - Controllers
LicenseManagerModule.controller('RequestImport', ['$log', '$scope', 'LicenseManagerService', '$uibModal', '$uibModalInstance', RequestImport]);
LicenseManagerModule.controller('LicenseManagerDetail', ['$log', '$scope', 'LicenseManagerService', 'UserPreferencesService', '$uibModal', '$uibModalInstance', 'params', LicenseManagerDetail]);


export default LicenseManagerModule;