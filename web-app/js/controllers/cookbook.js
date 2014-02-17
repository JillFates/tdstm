var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap', 'modNgBlur']);

app.config(['$logProvider', function($logProvider) {  
   //$logProvider.debugEnabled(false);  
}]);

app.controller('CookbookRecipeEditor', function($scope, $http, $resource, $timeout, $modal, $log) {
    
    // Resource Calls
    var Recipes = $resource('/tdstm/ws/cookbook/recipe/list?:archived&:context&:project', {archived:'@archived', context: '@context', project: '@project'}),
        Recipe = $resource('/tdstm/ws/cookbook/recipe/:recipeId/:versionId', {recipeId:'@recipeId', versionId:'@versionId'});

    // Default data to get recipes
    $scope.context = 'All';
    $scope.archived = 'n';
    $scope.project = 'master';

    // Method to Get the list of Recipes.
    var listRecipes = function(){
        $scope.recipes = Recipes.get({archived: 'archived=' + $scope.archived, context: 'context=' + $scope.context, project: 'project=' + $scope.project}, function(){
            $log.info('Success on getting Recipes');
            $scope.totalItems = $scope.recipes.data.list.length;
            $scope.gridData = ($scope.totalItems) ? $scope.recipes.data.list : [{'message': 'No results found', 'context': 'none'}]; 
            $scope.colDefinition = ($scope.totalItems) ? $scope.colDef : $scope.colDefNoData;
        }, function(){
            $log.warn('Error on getting Recipes');
        });
    }

    // This should be called whenever we need to update the recipes Grid
    $scope.changeRecipeList = function(){
        listRecipes();
    }

    // Initial call to get the list of Recipes
    listRecipes();

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
    var col = {index: 0}
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


    $scope.mySelection = [];
    var currentSelectedRow = {};

    $scope.editingRecipe = false;
    var lastLoop;
    $scope.gridOptions = {
        data: 'gridData',
        multiSelect: false,
        selectedItems: $scope.mySelection,
        columnDefs: 'colDefinition',
        enableCellEditOnFocus: false,
        enableCellEdit: true,
        beforeSelectionChange: function(rowItem){
            if($scope.editingRecipe){
                
                /*$('#unsavedChangesModal').modal('show');
                $('#unsavedChangesModal .btn.okey').on('click', function(e){
                    e.preventDefault();
                    $('#unsavedChangesModal').modal('hide');
                    $log.log('cancel');
                    return true;
                })
                $('#unsavedChangesModal .btn.cancel, #unsavedChangesModal .close').on('click', function(e){
                    e.preventDefault();
                    $('#unsavedChangesModal').modal('hide');
                    $log.log('no, please');
                    return false;
                })*/
                
                var confirmation=confirm("Recipe " + currentSelectedRow.entity.name + " has unsaved changes. Press Okay to continue and loose those changes otherwise press Cancel");
                if (confirmation==true){
                    $log.log('cancel');
                    return true;
                }else{
                    $log.log('no, please');
                    return false;
                }
            }else{
                return true;
            }
        },
        afterSelectionChange: function(rowItem){
            if(rowItem != currentSelectedRow){
                currentSelectedRow = rowItem;
                $scope.currentSelectedRecipe = {
                    name: rowItem.entity.name,
                    description: rowItem.entity.description
                }

                // This hack is to avoid changeRecipe() to be executed many times. This is a common issue on the ng-grid for the afterSelectionChange event.
                $timeout.cancel(lastLoop);
                lastLoop = $timeout(function(){
                    $scope.changeRecipe();
                }, 50)
                
            }
        }
    };

    $scope.$on('ngGridEventData', function(){
        $scope.gridOptions.selectRow(0, true);
    });
    //------------------------------------------

    // Updates all the content below the Recipes list with data from a selected recipe.
    // Added the Save WIP method here.
    var rec;
    $scope.changeRecipe = function(){
        if($scope.gridOptions.selectedItems[0] && $scope.totalItems){
            var item = $scope.gridOptions.selectedItems[0];
            
            rec = Recipe.get({recipeId:item.recipeId, versionId:''}, function(){
                
                // This is the selected recipe data.
                $scope.selectedRecipe = (rec.data) ? rec.data : null;

                // A deep copy of the selected Recipe data. It won't change when editing.
                $scope.originalDataRecipe = angular.copy($scope.selectedRecipe);

                $log.info('Success on getting selected recipe');
                
                $scope.saveWIP = function(){
                    rec.$save(rec.data, function(){
                        $log.info('Success on Saving WIP');
                    }, function(){
                        $log.warn('Error on Saving WIP');
                    });
                }

            }, function(){
                $log.warn('Error on getting selected recipe');
            });
            
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

    // Watch changes at the selected Recipe.
    $scope.$watch('selectedRecipe', function(newValue, oldValue) {
        oldValue = angular.copy($scope.originalDataRecipe);
        if (JSON.stringify(newValue) === JSON.stringify(oldValue) || !oldValue) {
            $scope.editingRecipe = false;
            return;
        }
        $scope.editingRecipe = true;
    }, true);

    $scope.actionsEdit = function(){
        //$log.log(this.row.entity);
    }

    // Update recipe. After editing. 
    $scope.updateEntity = function(row) {
        
        var recipeToUpdate = {
                name : $scope.gridData[row.rowIndex].name,
                description: $scope.gridData[row.rowIndex].description
            },
            rid = $scope.gridData[row.rowIndex].recipeId;

        if(!$scope.save) {
            $scope.save = { promise: null, pending: false, row: null };
        }
        
        $scope.save.row = row.rowIndex;
        if(!$scope.save.pending && (recipeToUpdate.name != $scope.currentSelectedRecipe.name || recipeToUpdate.description != $scope.currentSelectedRecipe.description)) {
            $scope.save.pending = true;
            $scope.save.promise = $timeout(function(){
                $http.put('/tdstm/ws/cookbook/recipe/'+rid, recipeToUpdate).success(function(){
                    
                    $log.info("Recipe updated: " 
                        + recipeToUpdate.name + "," 
                        + recipeToUpdate.description);

                    $scope.save.pending = false;

                    $scope.currentSelectedRecipe.name = recipeToUpdate.name;
                    $scope.currentSelectedRecipe.description = recipeToUpdate.description;

                    $('.alert.saved').fadeIn(200).delay(500).fadeOut();
                }).error(function(){
                    $log.warn('Recipe updating error');
                });
            }, 500);
        }  
    };

    // Create Recipe Modal - Open Modal
    $scope.openCreateModal = function () {

        var modalInstance = $modal.open({
            templateUrl: 'createRecipeModal',
            controller: ModalInstanceCtrl
        });
    };
    // Create Recipe Modal - Once Opened
    var ModalInstanceCtrl = function ($scope, $modalInstance) {
        $scope.modalBtns = {};

        $scope.modalContextSelector = "Event";

        $scope.newRecipe = {
            name: '',
            description: '',
            context: $scope.modalContextSelector,
            projectType: 'Master'
        }

        $log.log($scope.modalContextSelector);

        $scope.modalBtns.save = function () {
            $modalInstance.close(save());
            $log.log('creating recipe');
        };

        $scope.modalBtns.cancel = function () {
            $modalInstance.dismiss('cancel');
            $log.log('cancel create recipe');
        };

        var save = function(){
            $http.post('/tdstm/ws/cookbook/recipe', $scope.newRecipe).
                success(function(){
                    $log.info('Recipe created');
                }).
                error(function(){
                    $log.warn('Error when creating recipe');
                })
        }
    };
    //-----------------------------------------------------

    // New recipe Validation
    $scope.tmpRecipe = {};

    $scope.update = function(newRecipe) {
        $scope.tmpRecipe = angular.copy(newRecipe);
    };

    $scope.isUnchanged = function(newRecipe) {
        return angular.equals(newRecipe, $scope.tmpRecipe);
    };
    //-----------------------

    // Cancel changes
    $scope.cancelChanges = function(){
        $scope.selectedRecipe = angular.copy($scope.originalDataRecipe);
    }

});

angular.module('modNgBlur', [])
.directive('ngBlur', function () {
    return function (scope, elem, attrs) {
        elem.bind('blur', function () {
            scope.$apply(attrs.ngBlur);
        });
    };
});