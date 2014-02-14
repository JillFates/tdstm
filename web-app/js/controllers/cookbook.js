var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap', 'modNgBlur']);
app.controller('CookbookRecipeEditor', function($scope, $http, $resource, $timeout) {
    
    $scope.contexts = ['All', 'Event', 'Bundle', 'Application'];
    
    $scope.context = 'All';

    var contextLastSelected = 0,
        archivedSelected = false;

    var Recipes = $resource('/tdstm/ws/cookbook/recipe/list?archived=:archived&context=:context&project=:project', {archived:'@archived', context: '@context', project: '@project'});

    // Initial call to the $resource method
    $scope.recipes = Recipes.get({archived: 'n', context: 'All', project: 'master'}, function(){
        seeChanges();
        console.log($scope.recipes);
    });
    console.log($scope.recipes);

    $scope.singleRecipe = {
        name: null,
        description: null
    };

    // This should be called whenever we need to update the recipes Grid
    $scope.change = function(){
        $scope.recipes = Recipes.get({archived: $scope.archived || 'n', context: $scope.context || 'All', project: 'master'}, function(){
            seeChanges();
        });
    }

    var seeChanges = function(){
        $scope.totalItems = $scope.recipes.data.list.length;
        $scope.gridData = ($scope.totalItems) ? $scope.recipes.data.list : [{'message': 'No results found', 'context': 'none'}]; 
        $scope.colDefinition = ($scope.totalItems) ? $scope.colDef : $scope.colDefNoData;
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

    var col = {index: 0}
    // ng-grid stuff
    var actionsTemplate = '<div class="gridIcon"><a href="#" class="actions edit" title="Edit" ng-click="actionsEdit()"><span class="glyphicon glyphicon-pencil"></span> <a href="#" class="actions edit" title="Revert" ng-click="actionRevert()"><span class="glyphicon glyphicon-arrow-left"></span> <a href="#" class="actions edit" title="Archive" ng-click="actionArchive()"><span class="glyphicon glyphicon-folder-close"></span> <a href="#" class="actions edit" title="Remove" ng-click="actionDelete()"><span class="glyphicon glyphicon-trash"></span></a></div>'
    $scope.edittableField = '<input ng-class="colt' + col.index + '" ng-input="COL_FIELD" ng-model="COL_FIELD" ng-blur="updateEntity(row)" />'; //value="{{row.getProperty(col.field)}}" ng-blur="saveRecipeChanges()"
    $scope.colDef = [
        {field:'name', displayName:'Recipe', enableCellEdit: true, enableCellEditOnFocus: false, width: '***', editableCellTemplate: $scope.edittableField},
        {field:'description', displayName:'Description', enableCellEdit: true, enableCellEditOnFocus: false, width: '******', editableCellTemplate: $scope.edittableField},
        {field:'context', displayName:'Context', enableCellEdit: false, width: '**'},
        {field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '***'},
        {field:'lastUpdated', displayName:'Last', enableCellEdit: false, width: '****'},
        {field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false, width: '**'},
        {field:'hasWIP', displayName:'WIP', cellClass: 'text-center', enableCellEdit: false, width: '*'},
        {field:'', displayName:'Actions', cellClass: 'text-center', enableCellEdit: false, width: '**', sortable: false, cellTemplate: actionsTemplate}
    ];

    $scope.colDefNoData = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
    $scope.colDefinition = $scope.colDef;

    $scope.switchGridIfNecessary = function(){
        $scope.colDefinition = ($scope.totalItems) ? $scope.colDef : $scope.colDefNoData;
    }

    $scope.mySelection = [];
    var currentSelectedRow = {};

    $scope.workInProgress = false;

    $scope.gridOptions = {
        data: 'gridData',
        multiSelect: false,
        selectedItems: $scope.mySelection,
        columnDefs: 'colDefinition',
        enableCellEditOnFocus: false,
        enableCellEdit: true,
        beforeSelectionChange: function(rowItem){
            if($scope.workInProgress){
                
                /*$('#unsavedChangesModal').modal('show');
                $('#unsavedChangesModal .btn.okey').on('click', function(e){
                    e.preventDefault();
                    $('#unsavedChangesModal').modal('hide');
                    console.log('cancel');
                    return true;
                })
                $('#unsavedChangesModal .btn.cancel, #unsavedChangesModal .close').on('click', function(e){
                    e.preventDefault();
                    $('#unsavedChangesModal').modal('hide');
                    console.log('no, please');
                    return false;
                })*/
                
                var confirmation=confirm("Recipe " + currentSelectedRow.entity.name + " has unsaved changes. Press Okay to continue and loose those changes otherwise press Cancel");
                if (confirmation==true){
                    console.log('cancel');
                    return true;
                }else{
                    console.log('no, please');
                    return false;
                }
            }else{
                return true;
            }
        },
        afterSelectionChange: function(rowItem){
            if(rowItem != currentSelectedRow){
                currentSelectedRow = rowItem;
                $scope.changeRecipe();
                $scope.currentSelectedRecipe = {
                    name: rowItem.entity.name,
                    description: rowItem.entity.description
                }
            }
        }
    };

    $scope.$on('ngGridEventData', function(){
        $scope.gridOptions.selectRow(0, true);
    });
    //------------------------------------------
    
    var Recipe = $resource('/tdstm/ws/cookbook/recipe/:recipeId', {recipeId:'@recipeId'});

    $scope.changeRecipe = function(){
        if($scope.gridOptions.selectedItems[0] && $scope.totalItems){
            var item = $scope.gridOptions.selectedItems[0];
            $scope.recipeId = item.recipeId;
            var rec = Recipe.get({recipeId:item.recipeId}, function(){
                $scope.selectedRecipe = rec.data;
            });
            if($scope.selectedRecipe){
                $scope.selectedRecipe.name = currentSelectedRow.entity.name;
                console.log($scope.selectedRecipe);
            }
        }else{
            $scope.selectedRecipe = {
                "name":"",
                "description":"",
                "context":"",
                "createdBy":"",
                "lastUpdated":"",
                "versionNumber":"",
                "hasWIP":"",
                "sourceCode":"",
                "changelog":"",
                "clonedFrom":""
            }
        }
    }

    /*$scope.recipeActions = {
        saveWIP: '',
        release: '',
        revert: ''
    };*/



    $scope.saveWIP = function(){
        var rid = $scope.selectedRecipe.recipeId;
        var vid = $scope.selectedRecipe.versionNumber;

        $http.post('/tdstm/ws/cookbook/recipe/'+rid+'/'+vid, $scope.selectedRecipe).
            success(function(data, status) {
                console.log('success');
                $scope.workInProgress = false;
            }).
            error(function(data, status) {
                console.log('error');
            });
    }

    $scope.$watch('selectedRecipe', function(newValue, oldValue) {
        if (newValue === oldValue || !oldValue || (newValue.recipeId != oldValue.recipeId)) {
            $scope.workInProgress = false;
            disableElements();
            return;
        }
        $scope.workInProgress = true;
        enableDisabledElements();
    }, true);

    // Initially we should disable the buttons:
    disableElements()

    var enableDisabledElements = function(){
        console.log($('.btns .btn-group > button'));
        $('.btns .btn-group > button').prop('disabled', false);
        $('.btns button:submit').prop('disabled', false);
    }

    function disableElements(){
        $('.btns .btn-group > button').prop('disabled', true);
        $('.btns button:submit').prop('disabled', true);
    }

    $scope.actionsEdit = function(){
        //console.log(this.row.entity);
    }

    // Update recipe. After editing. 
    $scope.updateEntity = function(row) {
        
        var recipeToUpdate = {
                name : $scope.gridData[row.rowIndex].name,
                description: $scope.gridData[row.rowIndex].description
            },
            rid = $scope.gridData[row.rowIndex].recipeId;

        console.log(recipeToUpdate.name + ' - ' + $scope.currentSelectedRecipe.name + ' ... ' + recipeToUpdate.description  + ' - ' +  $scope.currentSelectedRecipe.description);
        console.log(row);
        if(!$scope.save) {
            $scope.save = { promise: null, pending: false, row: null };
        }
        
        $scope.save.row = row.rowIndex;
        if(!$scope.save.pending && (recipeToUpdate.name != $scope.currentSelectedRecipe.name || recipeToUpdate.description != $scope.currentSelectedRecipe.description)) {
            $scope.save.pending = true;
            $scope.save.promise = $timeout(function(){
                $http.put('/tdstm/ws/cookbook/recipe/'+rid, recipeToUpdate).success(function(){
                    
                    console.log("Here you'd save your record to the server, we're updating row: " 
                        + $scope.save.row + " to be: " 
                        + recipeToUpdate.name + "," 
                        + recipeToUpdate.description);

                    $scope.save.pending = false;

                    $scope.currentSelectedRecipe.name = recipeToUpdate.name;
                    $scope.currentSelectedRecipe.description = recipeToUpdate.description;

                    $('.alert.saved').fadeIn(200).delay(500).fadeOut();
                }).error(function(){
                    console.log('error when trying to update the recipe');
                });
            }, 500);
        }  
    };

});

angular.module('modNgBlur', [])
.directive('ngBlur', function () {
    return function (scope, elem, attrs) {
        elem.bind('blur', function () {
            scope.$apply(attrs.ngBlur);
        });
    };
});