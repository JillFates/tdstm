var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap']);
app.controller('CookbookRecipeEditor', function($scope, $http, $resource) {
    
    $scope.contexts = ['All', 'Event', 'Bundle', 'Application'];
    
    $scope.context = 'All';

    var Recipes = $resource('/tdstm/ws/cookbook/recipe/list?archived=:archived&context=:context&project=:project', {archived:'@archived', context: '@context', project: '@project'});

    // Initial call to the $resource method
    $scope.recipes = Recipes.get({archived: 'n', context: 'All', project: 'master'}, function(){
        $scope.totalItems = $scope.recipes.data.list.length;
        $scope.gridData = ($scope.totalItems) ? $scope.recipes.data.list : [{'message': 'No results found'}];
    });

    // This should be called whenever we need to update the recipes Grid
    $scope.change = function(){
        $scope.recipes = Recipes.get({archived: $scope.archived || 'n', context: $scope.context || 'All', project: 'master'}, function(){
            $scope.totalItems = $scope.recipes.data.list.length;
            $scope.gridData = ($scope.totalItems) ? $scope.recipes.data.list : [{'message': 'No results found'}];
            $scope.switchGridIfNecessary();
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

    
    // ng-grid stuff
    var editBtnTemplate = '<div class="gridIcon"><a href="#" class="actions edit"><span class="glyphicon glyphicon-edit"></span></a></div>'
    var deleteBtnTemplate = '<div class="gridIcon"><a href="#" class="actions delete"><span class="glyphicon glyphicon-remove"></span></a></div>'

    $scope.colDef = [
        {field:'name', displayName:'Recipe', enableCellEdit: false, width: '***'},
        {field:'description', displayName:'Description', enableCellEdit: false, width: '******'},
        {field:'context', displayName:'Context', enableCellEdit: false, width: '**'},
        {field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '***'},
        {field:'lastUpdated', displayName:'Last', enableCellEdit: false, width: '****'},
        {field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false, width: '**'},
        {field:'hasWIP', displayName:'WIP', cellClass: 'text-center', enableCellEdit: false, width: '*'},
        {field:'edit', displayName:'Edit', cellClass: 'text-center', enableCellEdit: false, width: '*', sortable: false, cellTemplate: editBtnTemplate},
        {field:'delete', displayName:'Delete', cellClass: 'text-center', enableCellEdit: false, width: '*', sortable: false, cellTemplate: deleteBtnTemplate}
    ];

    $scope.colDefNoData = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
    $scope.colDefinition = $scope.colDef;

    $scope.switchGridIfNecessary = function(){
        $scope.colDefinition = ($scope.totalItems) ? $scope.colDef : $scope.colDefNoData;
    }

    $scope.mySelections = [{'name': 'a'}];

    //$scope.change1 = function(){
    //    console.log('sas');
    //    if($scope.gridOptions.selectedItems[0]){
    //        var item = $scope.gridOptions.selectedItems[0];
    //        console.log(item);
    //        $scope.recipeId = item.recipeId;
    //        $scope.requestedRecipe = Recipe.get({recipeId:item.recipeId});
    //        tabs[1].active = true
    //    }
    //}

    var Recipe = $resource('/tdstm/ws/cookbook/recipe/:recipeId', {recipeId:'@recipeId'});

    $scope.gridOptions = {
        data: 'gridData',
        multiSelect: false,
        selectedItems: $scope.mySelections,
        columnDefs: 'colDefinition'
        //afterSelectionChange: function(){
        //    $scope.change1();
        //}
    };
    //------------------------------------------
});