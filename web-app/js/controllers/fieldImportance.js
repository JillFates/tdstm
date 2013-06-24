//-----------------------------Angular.js Implementation .--------------------//

var app = angular.module("MyApp", []);

//factory which return Fields,phases and importance
app.factory('fieldFactory',function($http){
	return{
		getFields : function() {
			return $http({
				url: contextPath+"/project/getAssetFields",
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
		getImportance : function() {
			return $http({
				url: contextPath+"/project/getImportance",
				method: 'GET'
			})
		}
	}
});

app.controller('assetFieldImportanceCtrl', function ($scope,$http,fieldFactory) {
	$scope.fields = [];
	$scope.importance = [];
	fieldFactory.getFields().success(function(data){
		$scope.fields=data;
	});
	fieldFactory.getImportance().success(function(data){
		$scope.importance = data;
	});
	$scope.phases=fieldFactory.getPhases();
	$scope.data = ['C','I','N','H'];
	//initializing section
	$scope.section = {'AssetEntity':'h','Application':'h','Database':'h','Files':'h'};
	//initializing notes to display styling notes div.
	$scope.notes=[{'id':'C','field':'Name','type':'ServerX05','imp':'C-Critical'},
	              {'id':'I','field':'Type','type':'Server','imp':'I-Ignore'},
	              {'id':'N','field':'Manufacturer','type':'HP','imp':'N-Normal'},
	              {'id':'H','field':'Model','type':'Dell','imp':'H-Hidden'}];
	
	$scope.toggleSection = function( s ) {
		$scope.section[s] = $scope.section[s] == 'h' ? 's' : 'h';
		//for time being used javaScript to show/hide styling div.
		$(".stylingNote").show();
		var imglength=$('.dgImages:visible').length;
		if($scope.section[s] == 'h' && imglength==3)
			$(".stylingNote").hide();
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
	$scope.assignData = function(type,value,field,phase) {
		$scope.importance[type][field]['phase'][phase] ='C'
			$scope.setImportance(type,field, phase, value);
	};

	$scope.setImportance = function (type,field, phase, value) {
		$scope.importance[type][field]['phase'][phase] = value;
	};
	$scope.updateAsset = function (type) {
		$http({
			url : contextPath+"/project/updateFieldImportance",
			method: "POST",
			data:{'jsonString':$scope.importance[type], 'entityType':type}
		}).success (function(resp) {
			console.log(resp);
		}).error(function(resp, status, headers, config) {
			alert("An Unexpected error while showing the asset fields.")
		});
	}
	
	$scope.cancelAsset= function (type) {
		$http({
			url : contextPath+"/project/cancelImportance",
			method: "POST",
			data:{'entityType':type}
		}).success (function(resp) {
			$scope.importance[type]=resp;
		}).error(function(resp, status, headers, config) {
			alert("An Unexpected error while showing the asset fields.")
		});
	}
	
	$scope.retriveDefaultImp = function (type){
		$http({
			url : contextPath+"/project/retriveDefaultImportance",
			method: "POST",
			data:{'entityType':type}
		}).success (function(resp) {
			$scope.importance[type]=resp;
		}).error(function(resp, status, headers, config) {
			alert("An Unexpected error while showing the asset fields.")
		});
	}
});