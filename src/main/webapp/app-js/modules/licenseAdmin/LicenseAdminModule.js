/**
 * Created by Jorge Morayta on 09/25/2016.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';

import LicenseAdminList from './list/LicenseAdminList.js';
import LicenseAdminService from './service/LicenseAdminService.js';
import RequestLicense from './request/RequestLicense.js';
import CreatedLicense from './created/CreatedLicense.js';
import ApplyLicenseKey from './applyLicenseKey/ApplyLicenseKey.js';
import ManuallyRequest from './manuallyRequest/ManuallyRequest.js';
import LicenseDetail from './detail/LicenseDetail.js';


var LicenseAdminModule = angular.module('TDSTM.LicenseAdminModule', [uiRouter]).config(['$stateProvider',  '$translatePartialLoaderProvider', '$locationProvider',
		function ($stateProvider, $translatePartialLoaderProvider, $locationProvider) {

		$translatePartialLoaderProvider.addPart('licenseAdmin');

		// Define a generic header for the entire module, or it can be changed for each instance.
		var header = {
				templateUrl: '../app-js/modules/header/HeaderView.html',
				controller: 'HeaderController as header'
		};

		$stateProvider
				.state('licenseAdminList', {
						data: {page: {title: 'Administer Licenses', instruction: '', menu: ['Admin', 'License', 'List']}},
						url: '/license/admin/list',
						views: {
								'headerView@': header,
								'bodyView@': {
										templateUrl: '../app-js/modules/licenseAdmin/list/LicenseAdminList.html',
										controller: 'LicenseAdminList as licenseAdminList'
								}
						}
				});
}]);

// Services
LicenseAdminModule.service('LicenseAdminService', ['$log', 'RestServiceHandler', '$rootScope', LicenseAdminService]);

// Controllers
LicenseAdminModule.controller('LicenseAdminList', ['$log', '$state', 'LicenseAdminService', '$uibModal', LicenseAdminList]);

// Modal - Controllers
LicenseAdminModule.controller('RequestLicense', ['$log', '$scope', 'LicenseAdminService', '$uibModal', '$uibModalInstance', RequestLicense]);
LicenseAdminModule.controller('CreatedLicense', ['$log', '$uibModalInstance', 'params', CreatedLicense]);
LicenseAdminModule.controller('ApplyLicenseKey', ['$log', '$scope', 'LicenseAdminService', '$uibModal', '$uibModalInstance', 'params', ApplyLicenseKey]);
LicenseAdminModule.controller('ManuallyRequest', ['$log', '$scope', 'LicenseAdminService', '$uibModalInstance', 'params', ManuallyRequest]);
LicenseAdminModule.controller('LicenseDetail', ['$log', 'LicenseAdminService', '$uibModal', '$uibModalInstance', 'params', LicenseDetail]);

/*
 * Filter to URL Encode text for the 'mailto'
 */
LicenseAdminModule.filter('escapeURLEncoding', function () {
	return function (text) {
		if(text){
			text = encodeURI(text);
		}
		return text;
	}
});

export default LicenseAdminModule;