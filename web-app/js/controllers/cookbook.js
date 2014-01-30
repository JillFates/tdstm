var app = angular.module('cookbookRecipes', ['ngGrid']);
app.controller('CookbookRecipeEditor', function($scope, $http) {
	$http.get("../cookbook/taskList").success( function( data ) {
        $scope.recipes = data
    });

    $scope.gridOptions = { data: 'recipes' };
});