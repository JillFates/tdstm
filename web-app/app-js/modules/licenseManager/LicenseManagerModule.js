/**
 * Created by Jorge Morayta on 09/25/2016.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';

import LicenseManagerController from './LicenseManagerController.js';


var LicenceManagerModule = angular.module('TDSTM.LicenseManagerModule', [uiRouter]).config(['$stateProvider',  '$translatePartialLoaderProvider',
    function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('licenseManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderContainer.html',
        controller: 'HeaderController as header'
    };

    $stateProvider
        .state('licenseManager', {
            data: {page: {title: 'License Manager', instruction: '', menu: ['LICENSE_MANAGER']}},
            url: '/license/manager',
            views: {
                'headerView@': header,
                'bodyView@': {
                    templateUrl: '../app-js/modules/licenseManager/LicenseManagerContainer.html',
                    controller: 'LicenseManagerController as licenseManager'
                }
            }
        });
}]);

// Controllers
LicenceManagerModule.controller('LicenseManagerController', ['$log', LicenseManagerController]);


export default LicenceManagerModule;