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

tds.comments.directive.TmLinkableUrl = function($http, utils) {
  return {
            template: '<input id=\"instructionsLinkId\" ng-blur=\"validateInstructionsLink()\" placeholder=\"Enter URL or Label|URL\" ng-model=\"ac.instructionsLink\" ng-maxlength=\"255\"></input>',
            restrict: 'E',
            link: function (scope, element, attrs, ngModel) {
              /*attrs.$observe('ngModel', function(value){ 
                scope.$watch(value,function(newValue){ 
                    console.log(newValue);  // el nuevo valor, recuerdo que cuando se genera, es un nuevo valor
                });
              });*/
            },
            controller: function ($scope) {
              $scope.validateInstructionsLink = function(){
                var ilValue = $scope.ac.instructionsLink
                var jsonData = {"linkableUrl": ilValue}
                var config = {headers: {'Content-Type': 'application/json; charset=utf-8'}}
                if(ilValue.length > 1){
                  $("#saveAndCloseBId").attr('disabled', true)
                  $http.post(utils.url.applyRootPath("/common/tmLinkableUrl"), jsonData, config)
                    .success(function(data, status, headers, config) {
                      if(data.status == "error" && data.errors){
                        // Do nothing (Service Results displays the error message to the user).
                      }else{
                        $("#saveAndCloseBId").removeAttr('disabled')
                      }
                    })
                    .error(function(data, status, headers, config) {
                      alert("There's been an error validating the Linkable Url.")
                  });  
                }else{
                  $("#saveAndCloseBId").removeAttr('disabled')
                }
                

              }
            }
        };
   }
tds.comments.module.directive('tmLinkableUrl', ['$http', 'utils',  tds.comments.directive.TmLinkableUrl]);