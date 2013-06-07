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
	    template: "<div class='pickbox'  ng-repeat='datum in data' ng-click='toggle(datum)'>{{datum}} </div>"
	}
});

app.controller('assetFieldImportanceCtrl', function ($scope,$http,fieldFactory) {
	$scope.phases=fieldFactory.getPhases();
	$scope.data = ['C','I','N','H'];
	
	//initializing section
	$scope.section = {'AssetEntity':'h','Application':'h','Database':'h','Files':'h'};
	
	$scope.toggleSection = function( s ) {
		$scope.fields = [];
        $scope.section[s] = $scope.section[s] == 'h' ? 's' : 'h';
        if($scope.section[s] == 's'){
        	fieldFactory.getFields(s).success(function(data){
        		$scope.fields=data;
        	});
        };
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