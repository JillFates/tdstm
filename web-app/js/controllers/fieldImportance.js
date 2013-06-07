  //-----------------------------Angular.js Implementation .--------------------//

var app = angular.module("MyApp", []);

//factory which return assetFields and phases
app.factory('fieldFactory',function($http){
	return{
		getFields : function() {
	        return $http({
	            url: contextPath+"/project/getAssetFields",
	            params:{'entityType':'AssetEntity'},
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
	    }
	 }
});

app.directive("importanceDiv", function() {
	return {
	    restrict: 'E',
	    scope: { data: '=',
	    		 field: '=field',
	    		 phase: '=phase'
	    	},
	    controller: function($scope) {
	        $scope.toggle = function(id) {
	          alert("Importance="+id);
	        };
	      },
	    template: "<div class='pickbox' ng-repeat='datum in data' ng-click='toggle(datum)'>{{datum}} </div>"
	}
});

//show template directive
app.directive("assetentityshow", function() {
	return {
	    restrict: 'CA',
	    scope: {},
	    templateUrl:contextPath+'/project/showImportance.gsp',
	    controller: function ($scope, $http, $attrs,fieldFactory) {
	    	$scope.fields = [];
	    	fieldFactory.getFields().success(function(data){
	    	   $scope.fields=data;
	    	   });
	    	$scope.phases=fieldFactory.getPhases();
	    }
	}
});

//edit template directive
app.directive("assetentityedit", function() {
	return {
	    restrict: 'CA',
	    scope: {},
	    templateUrl:contextPath+'/project/editImportance.gsp',
	    controller: function ($scope, $http, $attrs,fieldFactory) {
	    	$scope.fields = [];
	    	fieldFactory.getFields().success(function(data){
	    	   $scope.fields=data;
	    	   });
	    	$scope.phases=fieldFactory.getPhases();
	    }
	}
});

app.controller('editMain', function($scope) {
	$scope.data = ['C','I','N','H'];
});

app.controller('assetFieldImportanceCtrl', function ($scope,$http) {
	$scope.importance= [];
	//initializing section
	$scope.section = {'AssetEntity':'h','Application':'h','Database':'h','Files':'h'};
	
	$scope.toggleSection = function( s ) {
        $scope.section[s] = $scope.section[s] == 'h' ? 's' : 'h';
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
    
    $scope.setImportance = function (field, phase, value) {
        $scope.importance[field]['phase'][phase] = value;
    }	
});