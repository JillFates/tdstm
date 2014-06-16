/************************
 * MODULE: Assets
 ************************/

/**
 * Create namespaces
 */
tds.assets = tds.assets || {};
tds.assets.controller = tds.assets.controller || {};


/*****************************************
 * Assets module configuration
 */
tds.assets.module = angular.module('tdsAssets', ['tdsCore', 'tdsComments']);


/*****************************************
 * Controller for assets
 */
tds.assets.controller.MainController = function(scope) {};

tds.assets.controller.MainController.$inject = ['$scope'];


/***************************
 * COMPATIBILITY FUNCTIONS
 ***************************/

function getAssetsList(value, type) {
	angular.element($('#modalDialogComment')).scope().getAssetsList(value, type);
}