var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap']);
app.controller('CookbookRecipeEditor', function($scope, $http, $resource) {
    
    $scope.contexts = ['All', 'Event', 'Bundle', 'Application'];
    
    $scope.context = $scope.contexts[1];

    var Recipes = $resource('/tdstm/ws/cookbook/recipe/list?archived=:archived&context=:context&project=:project', {archived:'@archived', context: '@context', project: '@project'});
    
    // Initial call to the $resource method
    $scope.recipes = Recipes.get({archived: 'n', context: 'Event', project: 'master'}, function(){
        $scope.totalItems = $scope.recipes.data.list.length;
    });

    // This should be called whenever we need to update the recipes Grid
    $scope.change = function(){
        $scope.recipes = Recipes.get({archived: $scope.archived || 'n', context: $scope.context || 'All', project: 'master'}, function(){
            $scope.totalItems = $scope.recipes.data.list.length;
        });
    }

    // Pagination Stuff
    $scope.totalItems = 4;
    $scope.currentPage = 1;
    $scope.maxSize = 5;

    $scope.setPage = function (pageNo) {
        $scope.currentPage = pageNo;
    };

    $scope.bigTotalItems = 175;
    $scope.bigCurrentPage = 1;
    //----------------------------

    var editBtnTemplate = '<div class="gridIcon"><a href="#" class="actions edit"><span class="glyphicon glyphicon-edit"></span></a></div>'
    var deleteBtnTemplate = '<div class="gridIcon"><a href="#" class="actions delete"><span class="glyphicon glyphicon-remove"></span></a></div>'

    $scope.gridOptions = {
    	data: 'recipes.data.list',
    	multiSelect: false,
    	columnDefs: [
    		{field:'name', displayName:'Recipe', enableCellEdit: false, width: '***'},
        	{field:'description', displayName:'Description', enableCellEdit: false, width: '******'},
        	{field:'context', displayName:'Context', enableCellEdit: false, width: '**'},
        	{field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '***'},
        	{field:'lastUpdated', displayName:'Last', enableCellEdit: false, width: '****'},
        	{field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false, width: '**'},
        	{field:'hasWIP', displayName:'WIP', cellClass: 'text-center', enableCellEdit: false, width: '*'},
        	{field:'edit', displayName:'Edit', cellClass: 'text-center', enableCellEdit: false, width: '*', sortable: false, cellTemplate: editBtnTemplate},
        	{field:'delete', displayName:'Delete', cellClass: 'text-center', enableCellEdit: false, width: '*', sortable: false, cellTemplate: deleteBtnTemplate}
        ]
    };
});

var PaginationDemoCtrl = function ($scope) {
    $scope.totalItems = 64;
    $scope.currentPage = 4;
    $scope.maxSize = 5;

    $scope.setPage = function (pageNo) {
        $scope.currentPage = pageNo;
    };

    $scope.bigTotalItems = 175;
    $scope.bigCurrentPage = 1;
};