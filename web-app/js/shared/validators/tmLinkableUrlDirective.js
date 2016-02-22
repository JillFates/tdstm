/*
app.directive('ensureUnique', ['$http', function($http) {
  return {
    require: 'ngModel',
    link: function(scope, ele, attrs, c) {
      scope.$watch(attrs.ngModel, function() {
        $http({
          method: 'POST',
          url: '/api/check/' + attrs.ensureUnique,
          data: {'field': attrs.ensureUnique}
        }).success(function(data, status, headers, cfg) {
          c.$setValidity('unique', data.isUnique);
        }).error(function(data, status, headers, cfg) {
          c.$setValidity('unique', false);
        });
      });
    }
  }
}]);
*/

tds.comments.directive.TmLinkableUrl = function($http) {
  return {
            template: '<input id=\"instructionsLinkId\" placeholder=\"Enter URL or Label|URL\" ng-model=\"ac.instructionsLink\" ng-maxlength=\"255\"></input>',
            restrict: 'E',
            link: function (scope, element, attrs, ngModel) {
              /*attrs.$observe('ngModel', function(value){ 
                scope.$watch(value,function(newValue){ 
                    console.log(newValue);  // el nuevo valor, recuerdo que cuando se genera, es un nuevo valor
                });
              });*/
            },
            controller: function () {

            }
        };
   }
tds.comments.module.directive('tmLinkableUrl', ['$http', tds.comments.directive.TmLinkableUrl]);