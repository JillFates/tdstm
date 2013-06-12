//-----------------------------Angular.js Implementation .--------------------//

var app = angular.module("MyApp", []);

//factory which return assetFields and phases
app.factory('fieldFactory',function($http){
	return{
		getFields : function(type) {
			return $http({
				url: contextPath+"/project/getAssetFields",
				params:{'entityType':type},
				method: 'GET'
			})
		},
		getPhases : function() {
			var	phases =[{'id':'D','label':'Discovery'},
			   	         {'id':'V','label':'Validated'},
			   	         {'id':'R','label':'DependencyReview'},
			   	         {'id':'S','label':'DependencyScan'},
			   	         {'id':'B','label':'BundleReady'}];
			return phases;
		},
		getImportance : function(type) {
			return $http({
				url: contextPath+"/project/getImportance",
				params:{'entityType':type},
				method: 'GET'
			})
		}
	}
});

app.controller('assetFieldImportanceCtrl', function ($scope,$http,fieldFactory) {
	//initializing	importance(for time being I just hard coded) 							
	$scope.phases=fieldFactory.getPhases();
	$scope.data = ['C','I','N','H'];
	//initializing section
	$scope.section = {'AssetEntity':'h','Application':'h','Database':'h','Files':'h'};

	$scope.toggleSection = function( s ) {
		$scope.importance = [];
		$scope.fields = [];
		$scope.section[s] = $scope.section[s] == 'h' ? 's' : 'h';
		if($scope.section[s] == 's'){
			fieldFactory.getFields(s).success(function(data){
				$scope.fields=data;
			});
			fieldFactory.getImportance(s).success(function(data){
				$scope.importance=data;
			});
		}
	}

	$scope.showSection = function( s ) {
		return $scope.section[s] == 's'|| $scope.section[s] == 'e';
	}

	$scope.editMode = function (s) {
		return $scope.section[s] == 'e'
	}

	$scope.toggleEditMode = function (s) {
		$scope.section[s] = $scope.section[s] == 'e' ? 's' : 'e'
	}
	$scope.assignData = function(value,field,phase) {
		console.log(field+"_"+phase+"_"+value);
		$scope.importance[field]['phase'][phase] ='C'
			$scope.setImportance(field, phase, value);
	};

	$scope.setImportance = function (field, phase, value) {
		$scope.importance[field]['phase'][phase] = value;
	};
	$scope.updateAsset = function (type) {
		$http({
			url : contextPath+"/project/updateFieldImportance",
			method: "POST",
			data:{'jsonString':$scope.importance, 'entityType':type}
		}).success (function(resp) {
			console.log(resp);
		}).error(function(resp, status, headers, config) {
			alert("An Unexpected error while showing the asset fields.")
		});
	}
});