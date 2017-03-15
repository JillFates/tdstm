/************************
 * MODULE: Manufacturer
 ************************/

/**
 * Create namespaces
 */
tds.manufacturers = tds.manufacturers || {};
tds.manufacturers.controller = tds.manufacturers.controller || {};
tds.manufacturers.service = tds.manufacturers.service || {};


/*****************************************
 * Controller for manufacturers
 */
tds.manufacturers.controller.MainController = function(scope, modal, utils) {

	this.showMergeDialog = function(manufacturerId, manufacturerName) {
		modal.open({
			templateUrl: utils.url.applyRootPath('/components/manufacturer/select-manufacturer-to-merge.html'),
			controller: tds.manufacturers.controller.SelectManufacturerToMergeDialogController,
			scope: scope,
			backdrop : 'static',
			windowClass: 'modal-comment-task-list',
			resolve: {
				currentManufacturerId: function() {
					return manufacturerId;
				},
				currentManufacturerName: function() {
					return manufacturerName;
				}				
			}
		});
	}

};

tds.manufacturers.controller.MainController.$inject = ['$scope', '$modal', 'utils'];


/*****************************************
 * Controller use to select a manufacturer
 */
tds.manufacturers.controller.SelectManufacturerToMergeDialogController = function($scope, $modalInstance, $log, $timeout, utils, manufacturerService, currentManufacturerId, currentManufacturerName, alerts) {

	$scope.manufacturers = [];
	$scope.currentManufacturerId = currentManufacturerId;

	$modalInstance.opened.then(function(modalReady) {
		$scope.$broadcast("popupOpened");
	});

	$scope.removePopupOpenedListener = $scope.$on('popupOpened', function(evt) {
		$timeout(function() {
			$scope.removePopupOpenedListener();
			manufacturerService.getManufacturersToMerge(currentManufacturerId).then(
				function(data) {
					$scope.manufacturers = data
				},
				function(data) {
					alerts.showGenericMsg();
				}
			);
		}, 50);
	});

	$scope.close = function() {
		if (!$scope.closed) {
			$scope.$close('close');
			$scope.closed = true;
		}
	};

	$scope.listFilter = function (item) { 
		return (item.id != currentManufacturerId);
	};	

	$scope.doManufacturerMerge = function(manufacturerToMergeInto) {
		if (confirm("Do you want to merge '" + currentManufacturerName + "' into '" + manufacturerToMergeInto.name + "'")) {
			manufacturerService.mergeManufacturers(manufacturerToMergeInto.id, currentManufacturerId).then(
				function(data) {
					alert("Success: '" + currentManufacturerName + "' merged into '" + manufacturerToMergeInto.name + "'");
					location.reload();
				},
				function(data) {
					alert("Fail: can't merge manufacturers." );
					$scope.close();
				}
			);
		}
	};

};

/************************
 * SERVICES
 ************************/

/**
 * Factory used to interact with the manufacturers services
 */
tds.manufacturers.service.ManufacturerService = function(utils, http, q) {

	http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";

	var getManufacturers = function() {
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/manufacturer/retrieveManufacturersListAsJSON?assetType=all&includeAlias=true')).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var mergeManufacturers = function(mId, mFromId) {
		var params = $.param({
			'id': mId,
			'fromId': mFromId
		});
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/ws/manufacturer/merge'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

    var getManufacturersToMerge = function(mFromId) {
        var deferred = q.defer();
        http.post(utils.url.applyRootPath('/manufacturer/retrieveManufacturersListToMergeAsJSON?fromId=' + mFromId)).
        success(function(data, status, headers, config) {
            deferred.resolve(data);
        }).
        error(function(data, status, headers, config) {
            deferred.reject(data);
        });
        return deferred.promise;
    };

	return {
		getManufacturers: getManufacturers,
		mergeManufacturers: mergeManufacturers,
        getManufacturersToMerge: getManufacturersToMerge
	};

};


/*****************************************
 * Manufacturer module configuration
 */
tds.manufacturers.module = angular.module('tdsManufacturers', ['tdsCore']);

tds.manufacturers.module.factory('manufacturerService', ['utils', '$http', '$q', tds.manufacturers.service.ManufacturerService]);


/***************************
 * COMPATIBILITY FUNCTIONS
 ***************************/

function showMergeDialog(manufacturerId, manufacturerName) {
	try {
		var objDom = $('[ng-app]');
		var s = angular.element(objDom).scope()
		s.manufacturers.showMergeDialog(manufacturerId, manufacturerName)
	} catch(e) { // It's being used in a two apps page
		if($('#manufacturersScopeId').length > 0) {
			var objDom = $('#manufacturersScopeId');
			var s = angular.element(objDom).scope()
			s.manufacturers.showMergeDialog(manufacturerId, manufacturerName)
		}
	}
}