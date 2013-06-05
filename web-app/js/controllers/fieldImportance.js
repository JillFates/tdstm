  //-----------------------------Angular.js Implementation .--------------------//

var app = angular.module("MyApp", []);

app.directive('editImportance', [function () {
    var options = ['C', 'I', 'N', 'H'];
    var tmpl=''
    for (var i=0;i<options.length;i++){
    		tmpl += '<div class="pickbox" ng-click="assignData(' + options[i] + ')">' +options[i]+ '</div>';
    }
    return {
        restrict: 'CA',
        replace: false,
        transclude: false,
        scope: {
            field: '=field',
            phase: '=phase',
            selected: '=selected'
        },
        template: tmpl
        //tried with link: function but giving undefined value
        /*,link: function (scope, elem, attrs) {
        	scope.assignData=function(value){
       	     console.log(value);
        }
        }*/
}
}]);

app.controller('assetFieldImportanceCtrl', function ($scope,$http) {
	
	//initializing phase
	$scope.phases = [
	                 {
	                	 id: 'D',
	                	 label: 'Discovery'
	                 }, {
	                	 id: 'V',
	                	 label: 'Validation'
	                 },{
	                	 id: 'R',
	                	 label: 'DependencyReview'
	                 },{
	                	 id: 'S',
	                	 label: 'DependencyScan'
	                 },{
	                	 id: 'B',
	                	 label: 'BundleReady'
	                 }];
	//initializing importance
	$scope.importance = {
	        name: {
	            phase: {
	                'D': 'I',
	                'V': 'C'
	            }
	        },
	        location: {
	            phase: {
	                'D': 'N',
	                'V': 'I'
	            }
	        },
	        sme1: {
	            phase: {
	                'D': 'N',
	                'V': 'N'
	            }
	        },
	        sme2: {
	            phase: {
	                'D': 'N',
	                'V': 'N'
	            }
	        }
	    };
	//initializing section
	$scope.section = { 
					'AssetEntity':'h', 
					'Application':'h', 
					'Database':'h', 
					'Files':'h' 
				};
    
    $scope.toggleSection = function( s ) {
        $scope.section[s] = $scope.section[s] == 'h' ? 's' : 'h'
       if($scope.fields == undefined)
        $scope.getAssetFields(s);
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
    //get assetFields from server and updated to $scope.fields
	$scope.getAssetFields = function(type) {
		$http({
		url : contextPath+"/project/getAssetFields",
		params:{'entityType':type},
		async: true,
		method: "GET"
		}).success (function(resp) {
			$scope.fields =resp
		});
	}
});