/************************
 * MODULE: Assets
 ************************/

/**
 * Create namespaces
 */
tds.assets = tds.assets || {};
tds.assets.controller = tds.assets.controller || {};
tds.assets.service = tds.assets.service || {};


/*****************************************
 * Assets module configuration
 */
tds.assets.module = angular.module('tdsAssets', ['tdsCore', 'tdsComments']);


/*****************************************
 * Controller for assets
 */
tds.assets.controller.MainController = function($rootScope, $scope, $q, assetService) {

	//assetScope is used after it creates dynamic dom elements to $compile
	$rootScope.assetScope = $scope;

	/**
	 * Load the current set of Asset for the current view
	 */
	$scope.loadAssetTags = function(assetId) {
		if(assetId) {
			$scope.internalAsset =  {
				// Server Selected Tag
				assetSelector:{
					operator: "ANY",
					tag: []
				},
				// Current Selected Tags
				selectedAssetSelector:{}
			};

			// Get the List of Current Tags for this Asset
			assetService.getAssetTagsForAsset(assetId).then(
				function (response) {
					if (response && response.data && response.data.length >= 1) {
						response.data.forEach(function(assetTag) {
							var assetTagId = assetTag.id;
							var tagId = assetTag.tagId;
							assetTag.id = tagId;
							assetTag.assetTagId = assetTagId;
							assetTag.label = assetTag.name;
						});
						$scope.internalAsset.assetSelector.tag = response.data;
					}
					// Create the component
					recompileAssetDOM('tmAssetTagSelector');
				}
			);
		}
	};


	/**
	 * Asset Edit Tags
	 * Perform update and delete of tags
	 */
	$scope.onSubmitAssetTags = function (assetId) {

		var diffNewSelection = $scope.internalAsset.selectedAssetSelector.tag.filter((r) => !$scope.internalAsset.assetSelector.tag.find((l) => r.id === l.id));
		let diffDeletedSelection = $scope.internalAsset.assetSelector.tag.filter((r) => !$scope.internalAsset.selectedAssetSelector.tag.find((l) => r.id === l.id));

		// Partially Remove Prototype
		if(window.Prototype) {
			delete Object.prototype.toJSON;
			delete Array.prototype.toJSON;
			delete Hash.prototype.toJSON;
			delete String.prototype.toJSON;
		}

		// To Delete
		var deletedTagAssetIds = [];
		diffDeletedSelection.each( function(a){
			deletedTagAssetIds.push(parseInt(a.assetTagId));
		});
		// To Add
		var newTagAssetIds = [];
		diffNewSelection.each( function(a){
			newTagAssetIds.push(parseInt(a.id));
		});

		if (deletedTagAssetIds.length > 0) {
			assetService.deleteAssetTagsForAsset({ ids: deletedTagAssetIds }).then(function(){
				if (newTagAssetIds.length > 0) {
					assetService.createAssetTagsForAsset({tagIds: newTagAssetIds, assetId: assetId});
				}
			});
		} else if (newTagAssetIds.length > 0) {
			assetService.createAssetTagsForAsset({tagIds: newTagAssetIds, assetId: assetId});
		}


	};
};

tds.assets.controller.MainController.$inject = ['$rootScope', '$scope', '$q', 'assetService'];

/************************
 * SERVICES
 ************************/

/**
 * Factory used to interact with the assets/asset services
 */
tds.assets.service.AssetService = function (utils, $http, $q) {

	$http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";

	var getAssetTagsForAsset = function(assetId) {
		var deferred = $q.defer();
		$http({
			method: 'GET',
			url: utils.url.applyRootPath('/ws/tag/asset/' + assetId),
			headers: { "Content-Type": "application/json"}
		}).
		success(function (data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function (data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var deleteAssetTagsForAsset = function(tagAssetIds) {
		var deferred = $q.defer();
		$http({
			method: 'DELETE',
			url: utils.url.applyRootPath('/ws/tag/asset/'),
			data: JSON.stringify(tagAssetIds),
			headers: { "Content-Type": "application/json"}
		}).
		success(function (data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function (data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var createAssetTagsForAsset = function(tagIdsToAsset) {
		var deferred = $q.defer();
		$http({
			method: 'POST',
			url: utils.url.applyRootPath('/ws/tag/asset/'),
			data: JSON.stringify(tagIdsToAsset),
			headers: { "Content-Type": "application/json"}
		}).
		success(function (data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function (data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	return {
		getAssetTagsForAsset: getAssetTagsForAsset,
		deleteAssetTagsForAsset: deleteAssetTagsForAsset,
		createAssetTagsForAsset: createAssetTagsForAsset
	};

};

tds.assets.module.factory('assetService', ['utils', '$http', '$q', tds.assets.service.AssetService]);


/***************************
 * COMPATIBILITY FUNCTIONS
 ***************************/

function getAssetsList(value, type) {
	angular.element($('#modalDialogComment')).scope().getAssetsList(value, type);
}

/**
 * Function used to recompile the dynamic code generated
 */
function recompileAssetDOM(elementId, compileScope) {
	var objDom = $('[ng-app]');
	var injector = angular.element(objDom).injector();
	if (injector) {
		injector.invoke(function ($rootScope, $compile) {
			var htmlElement = $('#' + elementId);
			if (compileScope)
				$compile(htmlElement)(compileScope);
			else
				$compile(htmlElement)($rootScope.assetScope);
		});
	} else {
		location.reload();
		console.log("We were not able to recompile");
	}
}

/**
 * Get the current Angular $scope Context
 * Being used for pages outside Angular
 */
function getCurrentAngularContext() {
	var objDom = $('[ng-app]');
	return angular.element(objDom).scope();
}