var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap', 'modNgBlur', 'ui.codemirror']);

app.config(['$logProvider', function($logProvider) {  
   //$logProvider.debugEnabled(false);  
}]);

app.controller('CookbookRecipeEditor', function($scope, $rootScope, $http, $resource, $timeout, $modal, $log, $location, $anchorScroll) {
    
    // Resource Calls
    var Recipes = $resource('/tdstm/ws/cookbook/recipe/list:archived&:context', {archived:'@archived', context: '@context'}),
        Recipe = $resource('/tdstm/ws/cookbook/recipe/:recipeId/:versionId', {recipeId:'@recipeId', versionId:'@versionId'});

    $http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";

    var restCalls = $resource(
        '/tdstm/ws/cookbook/:section/:details/:moreDetails',
        {
            section: "@section",
            details: "@details",
            moreDetails: "@moreDetails"
        },
        {
            getListOfRecipes: {
                method: "GET",
                params: {
                    section: "recipe",
                    details: "list"
                }
            },
            getARecipeVersion: {
                method: "GET",
                params: {
                    section: "recipe"
                }
            },
            createRecipe: {
                method: "POST",
                params: {
                    section: "recipe"
                }
            },
            saveWIP: {
                method: "POST",
                params: {
                    section: "recipe"
                }
            },
            release: {
                method: "POST",
                params: {
                    section: "recipe",
                    details: "release"
                }
            },
            revert: {
                method: "POST",
                params: {
                    section: "revert"
                }
            },
            validate: {
                method: "POST",
                params: {
                    section: "validate"
                }
            }
        }
    ); 

    // Default data to get recipes
    $scope.context = 'All';
    $scope.archived = 'n';

    // Method to Get the list of Recipes.
    var listRecipes = function(){
        $scope.recipes = restCalls.getListOfRecipes({archived: $scope.archived, context: $scope.context}, function(){
            $log.info('Success on getting Recipes');
            $log.info($scope.recipes.data.list);
            $scope.totalItems = $scope.recipes.data.list.length;
            $scope.gridData = ($scope.totalItems) ? $scope.recipes.data.list : [{'message': 'No results found', 'context': 'none'}]; 
            $scope.colDefinition = ($scope.totalItems) ? $scope.colDef : $scope.colDefNoData;
        }, function(){
            $log.warn('Error on getting Recipes');
            $scope.alerts.addAlert({type: 'danger', msg: 'Error: Could not get the list of Recipes'});
        });
    }

    // This should be called whenever we need to update the recipes Grid
    $scope.changeRecipeList = function(){
        listRecipes();
    }

    // Initial call to get the list of Recipes
    listRecipes();


    // Main tabset actives
    $scope.activeTabs = {
        taskGeneration : true,
        history : false,
        editor : false,
        versions : false
    };

    // Editor - Buttons statuses
    $scope.btnsStatuses = {
        saveWIP : false,
        release : false,
        cancel : false,
        discardWIP : false,
        validateSyntax : false,
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
    var col = {index: 0}
    var actionsTemplate = '<div class="gridIcon"><a href="" class="actions edit" title="Edit" ng-click="gridActions(row, 0)"><span class="glyphicon glyphicon-pencil"></span> <a href="" class="actions revert" title="Revert" ng-click="gridActions(row, 1)"><span class="glyphicon glyphicon-arrow-left"></span> <a href="" class="actions archive" title="Archive" ng-click="gridActions(row, 2)"><span class="glyphicon glyphicon-folder-close"></span> <a href="" class="actions remove" title="Remove" ng-click="gridActions(row, 3)"><span class="glyphicon glyphicon-trash"></span></a></div>'
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
                $scope.currentSelectedRecipe = angular.copy(rowItem.entity);

                // This hack is to avoid changeRecipe() to be executed many times. This is a common issue on the ng-grid for the afterSelectionChange event.
                $timeout.cancel(lastLoop);
                lastLoop = $timeout(function(){
                    $scope.changeRecipe();
                }, 50)
                
            }
        }
    };

    $scope.$on('ngGridEventData', function(){
        if(currentSelectedRow.rowIndex){
            $scope.gridOptions.selectRow(currentSelectedRow.rowIndex, true);
        }else{
            $scope.gridOptions.selectRow(0, true);
        }
    });
    //------------------------------------------

    // Updates all the content below the Recipes list with data from a selected recipe.
    // Added the Save WIP method here.
    var rec;
    $scope.changeRecipe = function(){
        if($scope.gridOptions.selectedItems[0] && $scope.totalItems){
            var item = $scope.gridOptions.selectedItems[0];
            
            rec = restCalls.getARecipeVersion({details:item.recipeId, moreDetails:''}, function(){
                
                // This is the selected recipe data.
                $scope.selectedRecipe = (rec.data) ? rec.data : null;

                // A deep copy of the selected Recipe data. It won't change when editing.
                $scope.originalDataRecipe = angular.copy($scope.selectedRecipe);

                $log.info('Success on getting selected recipe');
                $log.info(rec.data);

            }, function(){
                $log.warn('The selected recipe has no version yet. Creating empty recipe..');
                $scope.selectedRecipe = {
                    "name": $scope.currentSelectedRecipe.name,
                    "description": $scope.currentSelectedRecipe.description,
                    "context": $scope.currentSelectedRecipe.context,
                    "createdBy":"",
                    "lastUpdated":"",
                    "versionNumber": $scope.currentSelectedRecipe.versionNumber,
                    "hasWIP": $scope.currentSelectedRecipe.hasWIP,
                    "sourceCode":"",
                    "changelog":"",
                    "clonedFrom":""
                }
                $scope.originalDataRecipe = angular.copy($scope.selectedRecipe);
                $log.warn('$scope.originalDataRecipe');
                $log.warn($scope.originalDataRecipe);
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
        if (JSON.stringify(newValue) === JSON.stringify(oldValue) || !oldValue || newValue.name == "") {
            $scope.editingRecipe = false;
            $log.log('not editable')
            return;
        }
        $scope.editingRecipe = true;
        $log.log('editable')
    }, true);

    // Actions for the main Grid
    $scope.gridActions = function(row, ind){
        var action = ['edit', 'revert', 'archive', 'delete'],
            actions = {
                edit : function(){
                    $scope.activeTabs.editor = true;
                    $timeout(function(){
                        $location.hash('mainTabset');
                        $anchorScroll();
                    }, 100)
                },
                revert : function(){
                    $log.info('Revert code');
                },

                archive : function(){
                    $log.info('Archive code');
                },

                delete : function(){
                    $log.info('Delete code');
                }
            };

        actions[action[ind]]();
    }

    // Save WIP Functionality
    $scope.saveWIP = function(){
        var dataToSend = $.param($scope.selectedRecipe),
            selectedId = $scope.selectedRecipe.recipeId,
            selectedVersion = $scope.selectedRecipe.versionNumber;
        restCalls.saveWIP({details:selectedId, moreDetailss:selectedVersion}, dataToSend, function(){
            $log.info('Success on Saving WIP');
            $scope.alerts.addAlert({type: 'success', msg: 'WIP Saved', closeIn: 1500});
            listRecipes();
        }, function(){
            $log.warn('Error on Saving WIP');
            $scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to save recipe version'});
        });
    }

    // Release Version Functionality
    $scope.releaseVersion = function(){
        var dataToSend = $.param($scope.selectedRecipe),
            selectedId = $scope.selectedRecipe.recipeId,
            confirmation=confirm("Only publish recipes if it is ready for use by all users. Press Okay to publish otherwise press cancel");
            if (confirmation==true){
                $log.log('okey');
                restCalls.release({moreDetails:selectedId}, dataToSend, function(){
                    $log.info('Success on Releasing');
                    $scope.alerts.addAlert({type: 'success', msg: 'Version Released', closeIn: 1500});
                    listRecipes();
                }, function(){
                    $log.warn('Error on Saving WIP');
                    if($scope.selectedRecipe.hasWIP){
                        $scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to release version'});    
                    }else{
                        $scope.alerts.addAlert({type: 'danger', msg: 'Error: You can only release recipes saved as WIP'});
                    }
                    
                });
            }else{
                $log.log('no, please');
            }
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

                    $scope.alerts.addAlert({type: 'success', msg: 'Saved', closeIn: 1500});
                    //$scope.alerts.removeAlertAfter(1500);

                }).error(function(){
                    $log.warn('Recipe updating error');
                    $scope.alerts.addAlert({type: 'danger', msg: 'Error: Could not save the recipe'});
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
        var outsideScope = angular.element(document.getElementById('cookbookRecipesEditor')).scope();

        $scope.modalBtns = {};

        $scope.modalContextSelector = "";

        $scope.newRecipe = {
            name: '',
            description: '',
            context: $scope.modalContextSelector
        }

        $scope.modalBtns.save = function () {
            $modalInstance.close(save());
            $log.log('creating recipe');
        };

        $scope.modalBtns.cancel = function () {
            $modalInstance.dismiss('cancel');
            $log.log('cancel create recipe');
        };

        var save = function(){
            var dataToSend = $.param($scope.newRecipe);
            restCalls.createRecipe(dataToSend, function(){
                $log.info('Recipe created');
                outsideScope.alerts.addAlert({type: 'success', msg: 'Recipe Created', closeIn: 1500});
                listRecipes();
            }, function(){
                $log.warn('Error when creating recipe');
                outsideScope.alerts.addAlert({type: 'danger', msg: 'Saved'});
            });
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
        var confirmation=confirm("You are about to cancel the changes for recipe: " + currentSelectedRow.entity.name + ". You want to proceed?");
        if (confirmation==true){
            $log.log('Yes, cancel');
            $scope.selectedRecipe = angular.copy($scope.originalDataRecipe);
            return true;
        }else{
            $log.log('No, please');
            return false;
        }
    }

    // Alerts Stuff
    $scope.alerts = {};
    
    $scope.alerts.list = [];
    
    $scope.alerts.addAlert = function(obj) {
        if(obj.closeIn){
            $scope.alerts.removeAlertAfter(obj.closeIn);
        }

        $scope.alerts.list.push({type: obj.type, msg: obj.msg, hidden: false});
    };
    
    $scope.alerts.closeAlert = function(index) {
        index = (index) ? index : $scope.alerts.list.length-1;
        $scope.alerts.list[index].hidden = true;
        $timeout(function(){
            $scope.alerts.list.splice(index, 1);
        }, 500);
    };

    $scope.alerts.removeAlertAfter = function(time) {
        time = (time) ? time : 1000;
        $timeout(function(){
            $scope.alerts.closeAlert();
        }, time);
    }
    //--------------

});

angular.module('modNgBlur', [])
.directive('ngBlur', function () {
    return function (scope, elem, attrs) {
        elem.bind('blur', function () {
            scope.$apply(attrs.ngBlur);
        });
    };
});