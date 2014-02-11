var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap']);
app.controller('CookbookRecipeEditor', function($scope, $http, $resource) {
    
    $scope.contexts = ['All', 'Event', 'Bundle', 'Application'];
    
    $scope.context = 'All';

    var contextLastSelected = 0,
        archivedSelected = false;

    var Recipes = $resource('/tdstm/ws/cookbook/recipe/list?archived=:archived&context=:context&project=:project', {archived:'@archived', context: '@context', project: '@project'});

    // Initial call to the $resource method
    $scope.recipes = Recipes.get({archived: 'n', context: 'All', project: 'master'}, function(){
        seeChanges();
    });

    // This should be called whenever we need to update the recipes Grid
    $scope.change = function(){
        $scope.recipes = Recipes.get({archived: $scope.archived || 'n', context: $scope.context || 'All', project: 'master'}, function(){
            if($scope.workInProgress){
                var confitmation=confirm("Recipe " + currentSelectedRow.entity.name + " has unsaved changes. Press Okay to continue and loose those changes otherwise press Cancel");
                if (confitmation==true){
                    seeChanges();
                    contextLastSelected = $('#contextSelector').val();
                    archivedSelected = $('#viewArchived').val()
                }else{
                    var s = setTimeout(function(){
                        $('#contextSelector').val(contextLastSelected)
                        $scope.context = $('#contextSelector option:eq('+contextLastSelected+')').text();
                        $('#viewArchived').val(archivedSelected)
                        document.getElementById("viewArchived").checked = archivedSelected;
                        console.log($scope.context);
                    }, 300)
                }
            }else{
                seeChanges();
                contextLastSelected = document.getElementById("contextSelector").selectedIndex;
                archivedSelected = document.getElementById("viewArchived").checked;
            }
        });
    }

    var seeChanges = function(){
        //console.log($scope.recipes.data.list);
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

    
    // ng-grid stuff
    var editBtnTemplate = '<div class="gridIcon"><a href="#" class="actions edit"><span class="glyphicon glyphicon-edit"></span></a></div>'
    var deleteBtnTemplate = '<div class="gridIcon"><a href="#" class="actions delete"><span class="glyphicon glyphicon-remove"></span></a></div>'

    $scope.colDef = [
        {field:'name', displayName:'Recipe', enableCellEdit: true, width: '***'},
        {field:'description', displayName:'Description', enableCellEdit: true, width: '******'},
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

    $scope.mySelection = [];
    var currentSelectedRow = {};

    $scope.workInProgress = false;

    $scope.gridOptions = {
        data: 'gridData',
        multiSelect: false,
        selectedItems: $scope.mySelection,
        columnDefs: 'colDefinition',
        beforeSelectionChange: function(rowItem){
            console.log('$scope.workInProgress: '+$scope.workInProgress);
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
                    
                    var confitmation=confirm("Recipe " + currentSelectedRow.entity.name + " has unsaved changes. Press Okay to continue and loose those changes otherwise press Cancel");
                    if (confitmation==true){
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
        /*console.log(rid);
        console.log($scope.selectedRecipe);*/
        $http.post('/tdstm/ws/cookbook/recipe/'+rid+'/'+vid, $scope.selectedRecipe).
        success(function(data, status) {
            console.log('success');
            console.log(status);
            console.log(data);
        }).
        error(function(data, status) {
            console.log('error');
            console.log(status);
            console.log(data);
        });
    }

    $scope.$watch('selectedRecipe', function(newValue, oldValue) {
        console.log('d');
        if (newValue === oldValue || !oldValue || (newValue.recipeId != oldValue.recipeId)) {
            $scope.workInProgress = false;
            return;
        }
        console.log('Work in progress');
        $scope.workInProgress = true;
    }, true);

    $scope.$watch('workInProgress', function(newValue, oldValue){
        if (!newValue){
            disableElements();
        }else{
            enableDisabledElements();
        }
    })

    // Initially we should disable the buttons:
    disableElements()

    var enableDisabledElements = function(){
        console.log($('.btns .btn-group > button'));
        $('.btns .btn-group > button').prop('disabled', false);
        $('.btns button:submit').prop('disabled', false);
    }

    function disableElements(){
        console.log($('.btns .btn-group > button'));
        $('.btns .btn-group > button').prop('disabled', true);
        $('.btns button:submit').prop('disabled', true);
    }
});