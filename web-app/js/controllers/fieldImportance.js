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
	$scope.help = [];
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
	
	//This code is used to Toggle Legend div.
	$scope.legend ='h'
	$scope.showLegend = function( ) {
		return $scope.legend == 's';
	}
	$scope.toggleLegend = function( s ) {
		$scope.legend = $scope.legend == 'h' ? 's' : 'h';
	}
	
	//initializing notes to display styling notes div.
	$scope.notes = [
		{'id':'C','field':'Name','type':'svoradb01','imp':'C)ritical'},
	    {'id':'I','field':'Type','type':'Server','imp':'I)mportant'},
	    {'id':'N','field':'Manufacturer','type':'HP','imp':'N)ormal'},
	    {'id':'H','field':'*Custom ONLY*','type':'','imp':'H)idden'}
	];
	
	$scope.toggleSection = function( s ) {
		$scope.section[s] = $scope.section[s] == 'h' ? 's' : 'h';
		//for help text to initialize.
		if($scope.section[s] == 's')
			$scope.helpSection(s)
		//for time being used javaScript to show/hide styling div.
		$(".legend").show();
		var imglength=$('.dgImages:visible').length;
		if ($scope.section[s] == 'h' && imglength==3)
			$(".legend").hide();
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
	//Constant consists of all customs as list
	var FIELD_LIST = [];
	for(i=1;i<25;i++){
		FIELD_LIST.push("custom"+i);
	}
	$scope.assignData = function(type,value,field,phase) {
		//checking condition that for customs1.. 24 FI='H' must be same for all validation types.
			if(FIELD_LIST.indexOf(field) != -1 && value=='H'){
				$scope.phases.each(function(p){
				$scope.setImportance(type,field, p.id, value);
				});
			}else if(value!= 'H'){ //checking condition that hidden will only work for customs1.. 24
				$scope.setImportance(type,field, phase, value);
				//checking condition to remove FI i.e Hidden for custom fields when FI is selected as C or I or N.
					$scope.phases.each(function(p){
						if(p.id!=phase && $scope.importance[type][field]['phase'][p.id]=='H')
						$scope.setImportance(type,field, p.id, 'N');
					});
			}else{
				$scope.setImportance(type,field, phase, 'N');
			}
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
	
	$scope.helpSection = function (type){
		$http({
			url : contextPath+"/common/tooltips",
			method: "POST",
			data:{'entityType':type}
		}).success (function(resp) {
			$scope.help=resp;
		}).error(function(resp, status, headers, config) {
			alert("An Unexpected error while showing the asset fields.")
		});
	}
	$scope.updateHelp = function (type) {
		$http({
			url : contextPath+"/common/tooltipsUpdate",
			method: "POST",
			data:{'jsonString':$scope.help[type], 'entityType':type}
		}).success (function(resp) {
			console.log(resp);
		}).error(function(resp, status, headers, config) {
			alert("An Unexpected error while showing the asset fields.")
		});
	}
});