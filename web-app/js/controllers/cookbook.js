var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap', 'modNgBlur', 
	 'ui.codemirror']);

app.config(['$logProvider', function($logProvider) {  
   //$logProvider.debugEnabled(false);  
}]);

app.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('servicesInterceptor');
}]);

app.controller('CookbookRecipeEditor', function($scope, $rootScope, $http, $resource, $timeout, $modal, 
	$log, $location, $anchorScroll, $sce) {
	
	// All Vars used
	var restCalls, listRecipes, columnSel, actionsTemplate, updateBtns, lastLoop, lastLoopData, confirmation, 
	confirmation, rowToShow, ModalInstanceCtrl, checkLoginStatus;

	var layoutPluginGroups = new ngGridLayoutPlugin();
	var layoutPluginTasks = new ngGridLayoutPlugin();
	var layoutPluginVersions = new ngGridLayoutPlugin();

	$http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
	// Resource Calls

	var startingFolder = 'tdstm'

	var restMethodDefinitions = {
			archive: {
				method: "POST",
				params: {
					domain: "cookbook",
					section: "recipe",
					details: "archive"
				}
			},
			unarchive: {
				method: "POST",
				params: {
					domain: "cookbook",
					section: "recipe",
					details: "unarchive"
				}
			},
			getListOfRecipes: {
				method: "GET",
				params: {
					domain: "cookbook",
					section: "recipe",
					details: "list"
				}
			},
			getARecipeVersion: {
				method: "GET",
				params: {
					domain: "cookbook",
					section: "recipe"
				}
			},
			createRecipe: {
				method: "POST",
				params: {
					domain: "cookbook",
					section: "recipe"
				}
			},
			saveWIP: {
				method: "POST",
				params: {
					domain: "cookbook",
					section: "recipe"
				}
			},
			discardWIP: {
				method: "DELETE",
				params: {
					domain: "cookbook",
					section: "recipe"
				}
			},
			release: {
				method: "POST",
				params: {
					domain: "cookbook",
					section: "recipe",
					details: "release"
				}
			},
			revert: {
				method: "POST",
				params: {
					domain: "cookbook",
					section: "recipe",
					details: "revert"
				}
			},
			validate: {
				method: "POST",
				params: {
					domain: "cookbook",
					section: "recipe",
					details: "validateSyntax"
				}
			},
			putInRecipe: {
				method: "PUT",
				params: {
					domain: "cookbook",
					section: "recipe"
				}
			},
			getEventsAndBundles: {
				method: "GET",
				params: {
					section: "event",
					details: "listEventsAndBundles"
				}
			},
			getListBundles: {
				method: "GET",
				params: {
					domain: "event",
					section: "listBundles"
				}
			},
			getListInBundle: {
				method: "GET",
				params: {
					domain: "application",
					section: "listInBundle"
				}
			},
			getUsrPreferences: {
				method: "GET",
				params: {
					domain: "user",
					section: "preferences"
				}
			},
			generateTask: {
				method: "POST",
				params: {
					domain: "task",
					section: "generateTasks"
				}
			},
			getTaskBatchInfo: {
				method: "GET",
				params: {
					domain: "task",
					section: "findTaskBatchByRecipeAndContext"
				}
			},
			getListTaskBatches: {
				method: "GET",
				params: {
					domain: "task",
					section: "listTaskBatches"
				}
			},
			getTaskBatch: {
				method: "GET",
				params: {
					domain: "task",
					section: "taskBatch"
				}
			},
			publishTaskBatch: {
				method: "POST",
				params: {
					domain: "task",
					details: "publish"
				}
			},
			unpublishTaskBatch: {
				method: "POST",
				params: {
					domain: "task",
					details: "unpublish"
				}
			},
			deleteTaskBatch: {
				method: "DELETE",
				params: {
					domain: "task"
				}
			},
			getGroups: {
				method: "GET",
				params: {
					domain: "cookbook",
					section: "groups"
				}
			},
			getVersions: {
				method: "GET",
				params: {
					domain: "cookbook",
					section: "recipeVersion",
					details: "list"
				}
			},
			getUserProjects: {
				method: "GET",
				params: {
					domain: "project",
					section: "userProjects"
				}
			}
	};
	
	restCalls = $resource(
		'/'+startingFolder+'/ws/:domain/:section/:details/:moreDetails',
		{
			domain: "@domain",
			section: "@section",
			details: "@details",
			moreDetails: "@moreDetails"
		},
		restMethodDefinitions
	);

	// Default data to get recipes
	$scope.context = 'All';
	$scope.archived = 'n';

	rowToShow = null;

	// Method to Get the list of Recipes.
	listRecipes = function(ind){
		$scope.recipes = restCalls.getListOfRecipes({archived: $scope.archived, context: $scope.context}, 
			function(data){
			$log.info('Success on getting Recipes');
			if(data.data){
				$scope.totalItems = data.data.list.length;
				$scope.gridData = ($scope.totalItems) ? data.data.list : 
					[{'message': 'No results found', 'context': 'none'}]; 
				$scope.colDefinition = ($scope.totalItems) ? $scope.colDef : $scope.colDefNoData;
				if(ind){
					rowToShow = ind;
				}
			}else{
				$log.warn('Moved Temporarily');
				location.reload();
			}
		}, function(data){
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

	$scope.activeSubTabs = {
		tasks: {
			summary: true,
			exceptions: false,
			info: false
		},
		history: {
			actions: true,
			tasks: false,
			logs: false
		},
		editor: {
			logs: true,
			groups: false,
			syntaxErrors: false
		}
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

	$scope.testingBoolean = true;

	// ng-grid stuff

	$scope.preventSelection = false;

	$scope.executeUpdate = true;

	columnSel = {index: 0},
	actionsTemplate =   '<div class="gridIcon">'+
	'<a href="" class="actions edit" title="Edit" ng-click="gridActions(row, 0)">'+
	'<span class="glyphicon glyphicon-pencil"></span>'+
	'</a>'+
	'<a href="" class="actions revert" ng-class="{ disabled: gridData[row.rowIndex].versionNumber < 1 }"'+
		'title="Revert" ng-click="gridActions(row, 1)">'+
	'<span class="glyphicon glyphicon-arrow-left"></span>'+
	'</a>'+
	'<a href="" class="actions archive" title="Archive" ng-click="gridActions(row, 2)"'+
		'ng-hide="archived == \'y\'">'+
	'<span class="glyphicon glyphicon-folder-close"></span>'+
	'</a>'+
	'<a href="" class="actions unarchive" title="UnArchive" ng-click="gridActions(row, 4)"'+
		'ng-hide="archived == \'n\'">'+
	'<span class="glyphicon glyphicon-folder-open"></span>'+
	'</a>'+
	'<a href="" class="actions remove" title="Remove" ng-click="gridActions(row, 3)">'+
	'<span class="glyphicon glyphicon-trash"></span>'+
	'</a>'+
	'</div>';
	$scope.edittableField = '<input class="ngGridCellEdit" ng-class="colt' + columnSel.index + 
		'" ng-input="COL_FIELD" ng-model="COL_FIELD" ng-keydown="keyPressed($event, row, col)" />';
	$scope.colDef = [
	{field:'name', displayName:'Recipe', enableCellEdit: true, enableCellEditOnFocus: false, width: '***', 
		editableCellTemplate: $scope.edittableField},
	{field:'description', displayName:'Description', enableCellEdit: true, enableCellEditOnFocus: false, 
		width: '******', editableCellTemplate: $scope.edittableField},
	{field:'context', displayName:'Context', enableCellEdit: false, width: '**'},
	{field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '***'},
	{field:'lastUpdated', displayName:'Last Updated', enableCellEdit: false, width: '****'},
	{field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false, 
		width: '**'},
	{field:'hasWIP', displayName:'WIP', cellClass: 'text-center', enableCellEdit: false, width: '*'},
	{field:'', displayName:'Actions', cellClass: 'text-center', enableCellEdit: false, width: '**', 
		sortable: false, cellTemplate: actionsTemplate}
	];

	$scope.colDefNoData = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
	$scope.colDefinition = $scope.colDef;

	$scope.currentSelectedRow = {};
	
	$scope.editingRecipe = false;

	$scope.enabledGridSelection = true;

	$scope.gridOptions = {
		data: 'gridData',
		multiSelect: false,
		selectedItems: [],
		columnDefs: 'colDefinition',
		enableCellEditOnFocus: false,
		enableCellEdit: true,
		beforeSelectionChange: function(rowItem){
			if (rowItem.rowIndex == $scope.currentSelectedRow.rowIndex) {
				if(!$scope.preventSelection){
					return true;
				}else{
					return false;
				}
			}
			if($scope.editingRecipe){                
				confirmation = confirm("Recipe " + $scope.currentSelectedRow.entity.name + 
					" has unsaved changes."+ 
					"Press Okay to continue and loose those changes otherwise press Cancel");
				if (confirmation == true){
					return true;
				}else{
					return false;
				}
			}else{
				if(!$scope.preventSelection){
					return true;
				}else{
					return false;
				}
			}
		},
		afterSelectionChange: function(rowItem){
			if(rowItem.rowIndex != $scope.currentSelectedRow.rowIndex && $scope.enabledGridSelection){
				$scope.currentSelectedRow = rowItem;
				// This hack is to avoid changeRecipe() to be executed many times. 
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				$timeout.cancel(lastLoop);
				lastLoop = $timeout(function(){
					if(rowItem.entity.name){
						$log.info('change recipe');
						$scope.currentSelectedRecipe = angular.copy(rowItem.entity);
						$scope.changeRecipe();
					}else{
						$scope.currentSelectedRecipe = null;
						$scope.selectedRecipe = {
							"name": "",
							"description": "",
							"context": "",
							"createdBy":"",
							"lastUpdated":"",
							"versionNumber": "",
							"hasWIP": "",
							"sourceCode":"",
							"changelog":"",
							"clonedFrom":""
						}
					}
				}, 50)
				
			}
		}
	};

	$scope.$on('ngGridEventData', function(){
		var row = 0;
		$timeout.cancel(lastLoopData);
		lastLoopData = $timeout(function(){
			if(typeof(rowToShow) == 'number'){
				row = ($scope.gridData.length == 1) ? 0 : rowToShow;
				rowToShow = null;
				$scope.gridOptions.selectRow(row, true);
			}else if(typeof($scope.currentSelectedRow.rowIndex) == 'number'){
				row = $scope.currentSelectedRow.rowIndex;
				$scope.gridOptions.selectRow(row, true);
			}
			
		}, 100);
	});

	$scope.$on('ngGridEventEndCellEdit', function(evt){
		var row=$scope.currentSelectedRow;
		if (!row) {
	        return true;
	    }
		$scope.updateEntity(row);
	});
	
	$scope.keyPressed = function(ev, row, col) {
		var charCode = ev.which || ev.keyCode
		if (charCode==13 || charCode==9){
			ev.stopPropagation();
			$scope.$broadcast('ngGridEventEndCellEdit');
			//$scope.executeUpdate = true;
			//$('.gridStyle input:visible').blur();
		}else if(charCode==27){
			//$scope.executeUpdate = false;
			//$('.gridStyle input:not(:visible)').focus();
		}
	}
	//------------------------------------------

	var fillDefault = function(){
		el = {
			"recipeId": $scope.currentSelectedRecipe.recipeId,
			"name": $scope.currentSelectedRecipe.name,
			"description": $scope.currentSelectedRecipe.description,
			"createdBy": null,
			"lastUpdated": "",
			"versionNumber": $scope.currentSelectedRecipe.versionNumber,
			"hasWIP": $scope.currentSelectedRecipe.hasWIP,
			"sourceCode": "",
			"changelog": "",
			"clonedFrom": ""
		}

		return el;
	}

	$scope.wipConfig = [];

	// Updates all the content below the Recipes list with data from a selected recipe.
	$scope.changeRecipe = function(){
		var item = $scope.gridOptions.selectedItems[0],
			ind = $scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId;
		$log.info(item);
		if(!$scope.wipConfig[ind]){
			$scope.wipConfig[ind] = {
				opt: (item.versionNumber > 0) ? 'release' : 'wip',
				justReleased: false
			}   
		}

		// recipeVersion will be 0 to get WIP or '' to get the latest version.
		var recipeVersion = ($scope.wipConfig[ind] && 
			$scope.wipConfig[ind].opt == 'wip') ? 0 : '';

		if(item && $scope.totalItems){
			if(item.hasWIP || item.versionNumber > 0){
				
				$scope.selectedRVersion = null;
				$scope.selectedRWip = null;

				// This sets the new 2 variables for wip and release
				var fillTheVars = function(){
					
					if(!item.hasWIP && item.versionNumber > 0){
						$scope.wipConfig[ind].opt = 'release';
					}

					$scope.selectedRecipe = ($scope.wipConfig[ind].opt == 'release') ?
						angular.copy($scope.selectedRVersion) : angular.copy($scope.selectedRWip);

					if(!$scope.selectedRWip){
						$scope.selectedRWip = ($scope.selectedRVersion) ? $scope.selectedRVersion : 
						fillDefault();
					}

					// A deep copy of the selected Recipe data. It won't change when editing.
					$scope.originalDataRecipe = angular.copy($scope.selectedRWip);

					// This is a good moment to call the get list task batches service
					$scope.tasks.getListTaskBatches({recipeId: $scope.selectedRecipe.recipeId, limitDays: 30})
					$scope.versions.getVersions({recipeId: $scope.selectedRecipe.recipeId});

				}

				// Only call getWipRecipe if there is the recipe has WIP
				var callGetWipService = function(){
					restCalls.getARecipeVersion({details:item.recipeId, moreDetails: 0}, function(data){
						// This is the selected recipe data.
						$scope.selectedRWip = (data.data) ? data.data : null;
						fillTheVars();
						$log.info('Success on getting selected wip recipe');
					}, function(){
						$scope.selectedRWip = fillDefault();
						fillTheVars();
						$log.info('No records found for selected wip Recipe');
					});
				}

				restCalls.getARecipeVersion({details:item.recipeId}, function(data){
					$scope.selectedRVersion = (data.data) ? data.data : null;
					if($scope.selectedRVersion.hasWIP){
						// Only call getWipRecipe if there is the recipe has WIP
						callGetWipService();
					}else{
						$scope.selectedRWip = angular.copy($scope.selectedRVersion);
						fillTheVars();
					}
					
					$log.info('Success on getting selected released recipe');
					$log.info(data.data);
				}, function(){
					$scope.selectedRVersion = fillDefault();
					$log.info('No records found for selected released Recipe');
				});

			}else if($scope.wipConfig[ind].opt == 'wip' && !item.hasWIP){
				/*if($scope.originalDataRecipe){*/
				    $scope.selectedRecipe = fillDefault();
				    $scope.selectedRWip = angular.copy($scope.selectedRecipe);
					$scope.originalDataRecipe = angular.copy($scope.selectedRecipe);
				/*}*/
			}else{
				$log.info('The selected recipe has no version yet or has no WIP yet.'+
					' Creating empty recipe..');
			    $scope.selectedRecipe = fillDefault();
			    $scope.selectedRWip = angular.copy($scope.selectedRecipe);
				$scope.originalDataRecipe = angular.copy($scope.selectedRecipe);
			}

			//Reset the selects in task generation part.
			$scope.tasks.resetSelects();

			$scope.currentSyntaxValidation = '';
		}else{
			$log.warn('no results found for the selected recipe');
		}
	}

	// Switching RELEASE or WIP radio buttons functionality. 
	$scope.switchWipRelease = function(par){
		$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].opt = par;
		if(par == 'wip' && !$scope.selectedRecipe.hasWIP){
			//$scope.selectedRWip.changelog = '';
		}

		if(par == 'release'){
			//$scope.selectedRWip = angular.copy($scope.selectedRecipe)
		}else if($scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].
			justReleased){
			$scope.selectedRWip.changelog = '';
			$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].
				justReleased = false;
		}

		$scope.selectedRecipe = (par == 'release') ? angular.copy($scope.selectedRVersion) : 
			angular.copy($scope.selectedRWip);
	}

	// Watch changes at the WIP RecipeVersion.
	$scope.$watch('selectedRWip', function(newValue, oldValue) {
		oldValue = angular.copy($scope.originalDataRecipe);
		if (JSON.stringify(newValue) === JSON.stringify(oldValue) || !oldValue || !newValue || ((newValue != null) && newValue.name == "")) {
			$scope.editingRecipe = false;
			return;
		}
		$scope.editingRecipe = true;
	}, true);

	// Watch changes on changelog
	$scope.$watch('selectedRecipe', function(newValue, oldValue) {
		if (($scope.gridData != null) && ($scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId] != null) &&
			($scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].opt == 'wip')) {
			$scope.selectedRWip.changelog = $scope.selectedRecipe.changelog;
		}
	}, true);

	// Watch changes on originalDataRecipe
	$scope.$watch('originalDataRecipe', function(newValue, oldValue) {
		$scope.editingRecipe = false;
	}, true);

	// Watch changes on originalDataRecipe
	$scope.$watch('currentSelectedRecipe', function(newValue, oldValue) {
		if (newValue && $scope.selectedRWip && $scope.selectedRVersion) {
			$scope.selectedRWip.name = newValue.name;
			$scope.selectedRWip.description = newValue.description;
			$scope.selectedRVersion.name = newValue.name;
			$scope.selectedRVersion.description = newValue.description;		
		}
	}, true);

	// Actions for the main Grid
	$scope.gridActions = function(row, ind){

		if(ind != 0){
			$scope.preventSelection = true;
		}
		
		if(ind == 1 && $scope.gridData[row.rowIndex].versionNumber < 1){
			return false;
		}

		var action = ['edit', 'revert', 'archive', 'delete', 'unarchive'],
		actions = {
			edit : function(){
				$timeout(function(){
					$scope.preventSelection = false;
					$scope.activeTabs.editor = true;
					$location.hash('mainTabset');
					$anchorScroll();
				}, 100)
			},
			revert : function(){
				$scope.preventSelection = false;
				$scope.activeTabs.history = true;
				$timeout(function(){
					$location.hash('mainTabset');
					$anchorScroll();
				}, 100);
			},

			archive : function(){
				$log.info('Archive code');
				var selectedId = row.entity.recipeId;
				restCalls.archive({moreDetails:selectedId}, function(){
					$log.info('Success on archiving Recipe');
					$scope.alerts.addAlert({type: 'success', msg: 'Recipe Archived', closeIn: 1500});
					listRecipes();
					$scope.preventSelection = false;
				}, function(){
					$log.warn('Error on archiving Recipe');
					$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to Archive Recipe'});
					$scope.preventSelection = false;
				});
			},

			unarchive : function(){
				$log.info('Archive code');
				var selectedId = row.entity.recipeId;
				restCalls.unarchive({moreDetails:selectedId}, function(){
					$log.info('Success on unarchiving Recipe');
					$scope.alerts.addAlert({type: 'success', msg: 'Recipe UnArchived', closeIn: 1500});
					listRecipes();
					$scope.preventSelection = false;
				}, function(){
					$log.warn('Error on unarchiving Recipe');
					$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to UnArchive Recipe'});
					$scope.preventSelection = false;
				});
			},

			delete : function(){
				confirmation = confirm("Delete Recipe \""+row.entity.name+"\"?");
				if (confirmation == true){
					var selectedId = row.entity.recipeId;
					restCalls.discardWIP({details:selectedId}, function(){
						$log.info('Success on removing Recipe');
						$scope.alerts.addAlert({type: 'success', msg: 'Recipe Removed', closeIn: 1500});
						listRecipes();
						$scope.preventSelection = false;
						if(row.rowIndex == $scope.currentSelectedRow.rowIndex){
							rowToShow = 0;
						}
					}, function(){
						$log.warn('Error on removing Recipe');
						$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to Remove Recipe'});
						$scope.preventSelection = false;
					});
				}
			}
		};

		actions[action[ind]]();
	}

	// This boolean is for to differentiate when the click at the grid was in an action or not. 
	// If it was in an action it shouldn't select that row, specially for delete action. 
	//Otherwise the grid tries to select a non existing row.
	
	// Editor Actions -----------
	$scope.editorActions = {
		// Save WIP
		saveWIP : function(){
			var tmpObj = angular.copy($scope.selectedRWip),
			selectedId = $scope.selectedRWip.recipeId,
			selectedVersion = $scope.selectedRWip.versionNumber;
			dataToSend = $.param(tmpObj)
			restCalls.saveWIP({details:selectedId}, dataToSend, function(){
				$log.info('Success on Saving WIP');
				$scope.alerts.addAlert({type: 'success', msg: 'WIP Saved', closeIn: 1500});
				$scope.originalDataRecipe = angular.copy($scope.selectedRWip);
				$scope.switchWipRelease('wip');
				listRecipes();
			}, function(){
				$log.warn('Error on Saving WIP');
				$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to save WIP'});
			});
		},
		// Release
		releaseVersion : function(){
			var dataToSend = $.param($scope.selectedRecipe),
			selectedId = $scope.selectedRecipe.recipeId,
			confirmation = confirm("Only publish recipes if it is ready for use by all users."+
				"Press Okay to publish otherwise press cancel");
			if (confirmation == true){
				restCalls.release({moreDetails:selectedId}, dataToSend, function(){
					$log.info('Success on Releasing');
					$scope.alerts.addAlert({type: 'success', msg: 'Version Released', closeIn: 1500});
					$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].opt = 
						'release';
					$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].
						justReleased = true;
					listRecipes();
				}, function(){
					$log.warn('Error on Saving WIP');
					if($scope.selectedRecipe.hasWIP){
						$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to release version'});    
					}else{
						$scope.alerts.addAlert({type: 'danger', msg: 'Error: You can only release recipes'+
							' saved as WIP'});
					}
					
				});
			}
		},
		// Cancel
		cancelChanges : function(){
			var confirmation = confirm("You are about to cancel the changes for recipe: " +
				$scope.currentSelectedRow.entity.name + ". You want to proceed?");
			if (confirmation == true){
				$scope.selectedRWip = angular.copy($scope.originalDataRecipe);
				$scope.selectedRecipe = angular.copy($scope.originalDataRecipe);
				return true;
			}else{
				return false;
			}
		},
		// Discard WIP
		discardWIP : function(){
			var confirmation = confirm("You are about to discard WIP: " + 
				$scope.currentSelectedRow.entity.name + "."+
				" Do you want to proceed?");
			if (confirmation == true){
				var dataToSend = $.param($scope.selectedRecipe),
				selectedId = $scope.selectedRecipe.recipeId,
				selectedVersion = $scope.selectedRecipe.versionNumber;
				restCalls.discardWIP({details:selectedId, moreDetails:selectedVersion}, 
					dataToSend, function(){
					$log.info('Success on Discarding WIP');
					$scope.alerts.addAlert({type: 'success', msg: 'WIP Discarded', closeIn: 1500});
					listRecipes();
				}, function(){
					$log.warn('Error on Discarding WIP');
					$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to discard WIP'});
				});	
			}
		},
		// Validate Syntax
		validateSyntax : function(){
			var dataToSend = $.param({'sourceCode': $scope.selectedRWip.sourceCode});
			restCalls.validate({}, dataToSend, function(data){
				$scope.currentSyntaxValidation = data.warnings || [{"error":0,"reason":"No errors found"}];
				$scope.activeSubTabs.editor.syntaxErrors = true;
			}, function(data){
				$log.warn('Error on validation');
				$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to validate Syntax'});
			});
		},
		
		diff : function() {
			$scope.showCompareCodeDialog(
			   $scope.selectedRVersion.sourceCode,
			   $scope.selectedRWip.sourceCode,
			   ("Version " + $scope.selectedRVersion.versionNumber),
			   "WIP"
			);
		}

	}

    $scope.showCompareCodeDialog = function(leftSourceCode, rightSourceCode, leftLabel, rightLabel) {
		$scope.toCompare = {
			"leftSourceCode":leftSourceCode, 	
			"rightSourceCode":rightSourceCode,
			"leftLabel":leftLabel,
			"rightLabel":rightLabel
		};
	    var dialogInstance = $modal.open({
	        templateUrl: 'cookbook/sourceCodeDiffDialog.gsp',
	        controller: SourceCodeDiffController,
	        scope: $scope,
	        resolve: {
	        	leftSourceCode: function () {
	               return $scope.toCompare.leftSourceCode;
	            },
	            rightSourceCode: function () {
		           return $scope.toCompare.rightSourceCode;
	            },
	            leftLabel: function () {
			       return $scope.toCompare.leftLabel;
                },
	            rightLabel: function () {
			       return $scope.toCompare.rightLabel;
	            }
	          }
	    });

	    dialogInstance.opened.then(function (modalReady) {
	    	    $scope.$broadcast("sourceCodeDiffModalLoaded");
	        }
	    );		    
	}

	//----------------------

	$scope.secureHTML = function(param){
		return $sce.trustAsHtml(param);
	}
	

	// Update recipe. After editing. 
	$scope.updateEntity = function(row) {
		var recipeToUpdate = {
			name : $scope.gridData[row.rowIndex].name,
			description: $scope.gridData[row.rowIndex].description
		},
		dataToSend = $.param(recipeToUpdate),
		rid = $scope.gridData[row.rowIndex].recipeId;


		if(!$scope.save) {
			$scope.save = { promise: null, pending: false, row: null };
		}

		if(!$scope.executeUpdate){
			return false;
		};
		
		$scope.save.row = row.rowIndex;
		if(!$scope.save.pending && (recipeToUpdate.name != $scope.currentSelectedRecipe.name ||
			recipeToUpdate.description != $scope.currentSelectedRecipe.description)) {
			$scope.save.pending = true;
			$scope.save.promise = $timeout(function(){
				restCalls.putInRecipe({details:rid}, recipeToUpdate, function(data){
					if(data.data){
						$log.info('Racipe Updated');
						$scope.save.pending = false;

						$scope.currentSelectedRecipe.name = recipeToUpdate.name;
						$scope.currentSelectedRecipe.description = recipeToUpdate.description;

						$scope.alerts.addAlert({type: 'success', msg: 'Saved', closeIn: 1000});
					}else{
						$log.warn('Moved Temporarily');
						location.reload();
					}
				}, function(){
					$log.warn('Recipe updating error');
					$scope.alerts.addAlert({type: 'danger', msg: 'Error: Could not save the recipe'});                    
				});
			}, 500);
		}  
	};

	// CLONE
	$scope.clone = {
		contextArray : [{name: 'All'}, {name: 'Event'}, {name: 'Bundle'}, {name: 'Application'}],
		projectsArray : [],
		projectsStateArray : [],
		selectedContext : '',
		selectedProject : '',
		selectedProjectState : '',
		activeTabs : {
			createNew : true,
			clone : false
		},
		newRecipe : {
			name: '',
			description: ''
		},
		currentSelectedRecipeRow : '',
		selectedRecipe : '',
		givenProjects : { 
			"status": "success",
			"data": {
				"projects": [
					{
						"id": 123,
						"name": "Foo",
						"description":"Move from NYC to NJ",
						"projectCode": "MOVE_1",
						"status": "active",
						"completionDate": "2014/12/15",
						"clientId": 343,
						"clientName": "Acme, Inc.",
					},
					{
						"id": 124,
						"name": "Foo2",
						"description":"Move from NJ to OH",
						"projectCode": "MOVE_2",
						"status": "delayed",
						"completionDate": "2014/12/17",
						"clientId": 343,
						"clientName": "Acme, Inc.",
					},
					{
						"id": 125,
						"name": "Foo3",
						"description":"Move from UT to SF",
						"projectCode": "MOVE_3",
						"status": "active",
						"completionDate": "2014/12/19",
						"clientId": 343,
						"clientName": "Acme, Inc.",
					}
				]
			}
		}
	}

	$scope.clone.selectedContext = $scope.clone.contextArray[0];

		// Grid stuff
	$scope.clone.colDef = [
		{field:'recipe', displayName:'Recipe', enableCellEdit: false, width: '**'},
		{field:'description', displayName:'Description', enableCellEdit: false, width: '**'},
		{field:'editor', displayName:'Editor', enableCellEdit: false, width: '**'},
		{field:'last', displayName:'Last', enableCellEdit: false, width: '**'},
		{field:'version', displayName:'Version', enableCellEdit: false, width: '**'},
	]
		// grid 
	$scope.clone.projectsGrid = {
		data: 'clone.gridData',
		multiSelect: false,
		columnDefs: 'clone.colDef',
		selectedItems: [],
		enableCellEditOnFocus: false,
		afterSelectionChange: function(rowItem){
			if(rowItem != $scope.clone.currentSelectedRecipeRow){
				$scope.clone.currentSelectedRecipeRow = rowItem;
				// This hack is to avoid changeRecipe() to be executed many times. 
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				$timeout.cancel(lastLoop);
				lastLoop = $timeout(function(){
					if(rowItem.entity.name){
						$log.info('Row changed');
						$scope.clone.selectedRecipe = rowItem.entity;
						$scope.clone.newRecipe.context = $scope.clone.selectedRecipe.context;
						$scope.clone.newRecipe.clonedFrom = $scope.clone.selectedRecipe.recipeId;
					}
				}, 50)
				
			}
		}
		//$scope.clone.newRecipe.context = 
	}; 

	getProjectsAndStatuses = function(){
		$log.log('getProjectsAndStatuses');
		restCalls.getUserProjects(
			{currentPage: 0, maxRows: 1000}, 
			function(data){
				$log.info('Success on getting Project list');
				$scope.clone.projectsArray = data.data.projects;
				angular.forEach($scope.clone.projectsArray, function(value, key){
					//$log.info($scope.clone.projectsStateArray);
					if($scope.clone.projectsStateArray.indexOf(value.status) == -1){
						$scope.clone.projectsStateArray.push(value.status);
					}
				})
			}, function(data){
				$log.warn('Error on getting Project list');
				$scope.alerts.addAlert({type: 'danger', msg: 'Error: Could not get the list of Projects'});
			}
		);
	}

	$scope.clone.optionsSelected = function(arg){
		if($scope.clone.selectedContext && $scope.clone.selectedProject/* && $scope.clone.selectedProjectState*/){
			$log.info('fill the grid');
			restCalls.getListOfRecipes(
				{context: $scope.clone.selectedContext.name, projectType: $scope.clone.selectedProject.id}, 
				function(data){
					$log.info('Success on getting Recipes to Clone');
					$log.info(data.data.list);
					$scope.clone.gridData = data.data.list;
					$timeout(function(){
						$scope.clone.projectsGrid.selectRow(0, true)
					}, 200)
				}, function(data){
					$scope.clone.gridData = [];
					$log.warn('Error on getting Recipes to Clone');
					//$scope.alerts.addAlert({type: 'danger', msg: 'Error: Could not get the list of Recipes'});
				}
			);
			//$scope.clone.gridData = angular.copy($scope.clone.projectsArray);
		}else{
			$log.info('blank the grid');
			$scope.clone.gridData = [];
			$scope.clone.projectsGrid.selectedItems[0] = '';
		}
	}

	$scope.clone.refreshGrid = function(){
		$scope.clone.colDef = [
			{field:'name', displayName:'Recipe', enableCellEdit: false, width: '**'},
			{field:'description', displayName:'Description', enableCellEdit: false, width: '**'},
			{field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '**'},
			{field:'lastUpdated', displayName:'Last', enableCellEdit: false, width: '**'},
			{field:'versionNumber', displayName:'Version', enableCellEdit: false, width: '**'},
		]
	}

	/////////////////////////////////

	// Modal general stuff ------------------------------
	$scope.modalBtns = {};

	//Hide or show the modal
	$scope.showModal = function (visible, elem) {
		if (!elem)
			elem = element;

		if (visible){
			elem.modal("show");
			getProjectsAndStatuses();                     
		}else{
			elem.modal("hide");
		}
	}

	// Syntax Modal Stuff

	$scope.codeEditorOptions = {
		lineNumbers: true,
		indentWithTabs: true,
		indentUnit: 4,
		lineWrapping : true,
		extraKeys: {"Ctrl-Space": "autocomplete"}
	};

	$scope.syntaxModal = {
		sourceCode : '',
		showModal : false,
		btns : {
			storeLocally : function(){
				$log.log('storing locally edition');
				$scope.selectedRWip.sourceCode = $scope.syntaxModal.sourceCode;
				$scope.selectedRWip.changelog = $scope.selectedRecipe.changelog;
				$scope.selectedRecipe = angular.copy($scope.selectedRWip);
				if($scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].opt
					 != 'wip'){
					$scope.switchWipRelease('wip');
				}
				$scope.syntaxModal.showModal = false;
			},
			cancel : function(){
				$log.log('cancel edition');
				$scope.syntaxModal.showModal = false;
			}
		}
	};

	// Watch the showSyntax variable
	$scope.$watch('syntaxModal.showModal', function (newValue, oldValue) {
		if($scope.selectedRecipe){
			$scope.syntaxModal.sourceCode = angular.copy($scope.selectedRecipe.sourceCode);
			$scope.showModal(newValue, $('#editSyntax'));
		}
	});
	//-------------------------------------------

	// Watch the showDialog variable
	$scope.$watch('showDialog', function (newValue, oldValue) {
		$scope.showModal(newValue, $('#createRecipeModal'));
	});

	var clearFields = function(){
		$scope.modalContextSelector = "";

		$scope.newRecipe = {
			name: '',
			description: '',
			context: $scope.modalContextSelector
		}
	}

	var save = function(args){
		var dataToSend = $.param(args);
		restCalls.createRecipe(dataToSend, function(data){
			$scope.alerts.addAlert({type: 'success', msg: 'Recipe Created', closeIn: 1500});
			$scope.activeTabs.editor  = true;
			listRecipes($scope.gridData.length);
		}, function(){
			$log.warn('Error when creating recipe');
			$scope.alerts.addAlert({type: 'danger', msg: 'Sorry but an unexpected error has occurred. Please contact support for assistance'});
		});

		clearFields();
	}

	$scope.modalBtns.save = function () {
		$scope.showDialog = false;
		var recipeToSave = ($scope.clone.activeTabs.newRecipe) ? $scope.newRecipe : $scope.clone.newRecipe;
		save(recipeToSave); 
	};

	$scope.modalBtns.cancel = function () {
		$scope.showDialog = false;
		$log.log('cancel create recipe');
	};

	$scope.showDialog = false;
	
	$scope.showCreateRecipeDialog = function() {
		if($scope.editingRecipe){                
			confirmation=confirm("Recipe " + $scope.currentSelectedRow.entity.name + " has unsaved changes."+ 
				"Press Okay to continue and loose those changes otherwise press Cancel");
			if (confirmation==true){
				$scope.showDialog = true;
			}
		} else {
			$scope.showDialog = true;
		}
	};	
	
	clearFields();
	//----------------------------------------------------

	// New recipe Validation
	$scope.tmpRecipe = {};

	$scope.update = function(newRecipe) {
		$scope.tmpRecipe = angular.copy(newRecipe);
	};

	$scope.isUnchanged = function(newRecipe) {
		return angular.equals(newRecipe, $scope.tmpRecipe);
	};
	//-----------------------

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


	// TASKS STUFF ///////////////////////////////////
	$scope.tasks = {
		eventsArray : [],
		boundlesArray : [],
		bundlesArrayUnassigned : [],
		bundlesArrayAssigned : [],
		applicationsArray : [],
		applicationsArrayUnassigned : [],
		applicationsArrayAssigned : [],
		selectedEvent : '',
		selectedBundle : '',
		selectedApplication : '',
		validCurrentSelection : false,
		showDeletePreviouslyGenerated : false,
		taskBatch : {},
		generateOpts : {
			contextId: null,// - the select value from the select that represents the context
			recipeId: null,// - the id of the Recipe record to use to generate the tasks
			recipeVersionId: null,// - the id of the RecipeVersion record to use to generate the tasks
			useWIP: false,
			autoPublish: false,
			deletePrevious: false
		},
		show : {
			generate: true,
			generating: false,
			completion: false
		}
	}

	// Returns true if the 'generate task' button should be enabled. Otherwise returns false
	$scope.tasks.validateCurrentSelection = function(){
		if($scope.currentSelectedRecipe && $scope.currentSelectedRecipe.context){
			var context = $scope.currentSelectedRecipe.context;
			if((context == 'Event' && $scope.tasks.selectedEvent) || (context == 'Bundle' && 
					$scope.tasks.selectedBundle) ||	(context == 'Application' && 
					$scope.tasks.selectedApplication)){
				$log.log('matches event, or bundle, or application');
				return true;
			}else{
				$log.log('not Matching');
				return false;
			}
			return false;
		}
	}

	$scope.tasks.checkValidSelection = function(){
		var context = $scope.currentSelectedRecipe.context,
			contextId,
			recipeId = $scope.currentSelectedRecipe.recipeId;

		switch(context){
			case 'Application':
			 	contextId = $scope.tasks.selectedApplication.id
				break;
			case 'Event':
				contextId = $scope.tasks.selectedEvent.id
				break;
			case 'Bundle':
				contextId = $scope.tasks.selectedBundle.id
				break;
			default:
				contextId = 0
		}
		$scope.tasks.validCurrentSelection = $scope.tasks.validateCurrentSelection();
		if($scope.tasks.validCurrentSelection){
			$scope.tasks.getTaskBatchInfo({recipeId: recipeId, contextId: contextId, logs: false});
		}
	}

	// Put select elements in blank. 
	$scope.tasks.blankBundles = function(){
		$scope.tasks.selectedBundle = '';
		$scope.tasks.selectedApplication = '';
		$scope.tasks.bundlesArrayAssigned = [];
		$scope.tasks.bundlesArray = $scope.tasks.bundlesArrayUnassigned;
		$scope.tasks.applicationsArrayAssigned = [];
		$scope.tasks.applicationsArray = $scope.tasks.applicationsArrayUnassigned;
	};
	$scope.tasks.blankApplications = function(){
		$scope.tasks.selectedApplication = '';
		$scope.tasks.applicationsArrayAssigned = [];
		$scope.tasks.applicationsArray = $scope.tasks.applicationsArrayUnassigned;
	};
	/////////////////////////////

	// Events for select elements.
	$scope.tasks.eventSelected = function(){
		$scope.tasks.selectedBundle = '';
		$scope.tasks.selectedApplication = '';
		if($scope.tasks.selectedEvent && $scope.currentSelectedRecipe.context != 'Event'){
			$scope.tasks.getListBundles($scope.tasks.selectedEvent);
		}else{
			$scope.tasks.blankBundles();
		}
		$scope.tasks.checkValidSelection();
	};
	$scope.tasks.bundleSelected = function(){
		$scope.tasks.selectedApplication = '';
		if($scope.tasks.selectedBundle && $scope.currentSelectedRecipe.context != 'Bundle'){
			$scope.tasks.getListInBundle($scope.tasks.selectedBundle);
		}else{
			$scope.tasks.blankApplications();
		}
		$scope.tasks.checkValidSelection();
	};
	////////////////////////////////

	// Reset selects
	$scope.tasks.resetSelects = function(){
		$scope.tasks.selectedEvent = '';
		$scope.tasks.selectedBundle = '';
		$scope.tasks.selectedApplication = '';
	};

	// generate an array to show the select element correctly
	// Expect opts with the following data: isUnassigned (boolean), unassignedArray 
	// (array with unassigned info)
	// assignedArray (array with assigned info), and groupName which would be the group name if 
	// isUnassigned is false.
	$scope.tasks.generateOptions = function(opts){
		var newArray = (opts.isUnassigned) ? opts.unassignedArray : opts.assignedArray;
		if(opts.isUnassigned){
			angular.forEach(newArray, function(value, key){
				value.group = 'unassigned';
			})
			if(opts.isGroup){
				$scope.groups.bundlesArrayUnassigned = newArray;
			}else{
				$scope.tasks.bundlesArrayUnassigned = newArray;
			}
		}else{
			angular.forEach(newArray, function(value, key){
				value.group = opts.groupName;
			})
		}
		return newArray
	}

	// Generate btn click.
	$scope.tasks.generateTask = function(e){
		var idSelected = null,
			dataToSend;
		switch($scope.currentSelectedRecipe.context){
		case 'Event':
			idSelected = $scope.tasks.selectedEvent.id;
			break;
		case 'Bundle':
			idSelected = $scope.tasks.selectedBundle.id;
			break;
		case 'Application':
			idSelected = $scope.tasks.selectedApplication.id;
			break;
		default:
			idSelected = null
		}

		$scope.tasks.generateOpts.contextId = idSelected;
		$scope.tasks.generateOpts.recipeVersionId = $scope.selectedRecipe.recipeId;

		dataToSend = $.param($scope.tasks.generateOpts);

		restCalls.generateTask({}, dataToSend, function(data){
			$log.info('Success on generating task');
			$log.info(data);
		}, function(){
			$log.info('Error on generating task');
		});
	}
	
	// Get Events and Bundles
	$scope.tasks.getEventsAndBundles = function(isGroup){
		restCalls.getEventsAndBundles({}, function(data){
			$log.info('Success on getting Events and Bundles');
			$log.info(data.data.list);
			if(isGroup){
				$scope.groups.eventsArray = data.data.list;
			}else{
				$scope.tasks.eventsArray = data.data.list;
			}
			$scope.tasks.getUsrPreferences(isGroup);
		}, function(){
			$log.info('Error on getting Events and Bundles');
		});
	}

	// Get List of Bundles for a given Event
	$scope.tasks.getListBundles = function(event, isGroup){
		var event = (event == 0) ? {id: 0} : event;
		restCalls.getListBundles({details: event.id}, function(data){
			$log.info('Success on getting Bundles');
			$log.info(data.data.list);
			if(event.id == 0){
				//generate an array to show the select element correctly
				if(isGroup){
					$scope.groups.bundlesArrayUnassigned = $scope.tasks.generateOptions({
						isUnassigned: true, 
						unassignedArray: data.data.list, 
						assignedArray: []
					});
				}else{
					$scope.tasks.bundlesArrayUnassigned = $scope.tasks.generateOptions({
						isUnassigned: true, 
						unassignedArray: data.data.list, 
						assignedArray: []
					});
				}
			}else{
				//generate an array to show the select element correctly
				if(isGroup){
					$scope.groups.bundlesArrayAssigned = $scope.tasks.generateOptions({
						isUnassigned: false, 
						unassignedArray: angular.copy($scope.groups.bundlesArrayUnassigned), 
						assignedArray: data.data.list, 
						groupName: event.name
					});
				}else{
					$scope.tasks.bundlesArrayAssigned = $scope.tasks.generateOptions({
						isUnassigned: false, 
						unassignedArray: angular.copy($scope.tasks.bundlesArrayUnassigned), 
						assignedArray: data.data.list, 
						groupName: event.name
					});
				}
			}
			if(isGroup){
				$scope.groups.bundlesArray = angular.copy($scope.groups.bundlesArrayAssigned)
				.concat(angular.copy($scope.groups.bundlesArrayUnassigned));
			}else{
				$scope.tasks.bundlesArray = angular.copy($scope.tasks.bundlesArrayAssigned)
				.concat(angular.copy($scope.tasks.bundlesArrayUnassigned));
			}
		}, function(){
			$log.info('Error on getting Bundles');
		});
	}

	// Get Applications in for a given Bundle
	$scope.tasks.getListInBundle = function(bundle, isGroup){
		var bundle = (bundle == 0) ? {id: 0} : bundle;
		if(bundle.group != 'unassigned'){
			restCalls.getListInBundle({details: bundle.id}, function(data){
				$log.info('Success on getting Applications');
				$log.info(data.data.list);
				if(bundle.id == 0){
					//generate an array to show the select element correctly
					if(isGroup){
						$scope.groups.applicationsArrayUnassigned =  $scope.tasks.generateOptions({
							isUnassigned: true, 
							unassignedArray: data.data.list, 
							assignedArray: []
						});
					}else{
						$scope.tasks.applicationsArrayUnassigned =  $scope.tasks.generateOptions({
							isUnassigned: true, 
							unassignedArray: data.data.list, 
							assignedArray: []
						});
					}
				}else{
					//generate an array to show the select element correctly
					if(isGroup){
						$scope.groups.applicationsArrayAssigned = $scope.tasks.generateOptions({
							isUnassigned: false, 
							unassignedArray: angular.copy($scope.tasks.applicationsArrayUnassigned), 
							assignedArray: data.data.list, 
							groupName: bundle.name,
							isGroup: true
						});
					}else{
						$scope.tasks.applicationsArrayAssigned = $scope.tasks.generateOptions({
							isUnassigned: false, 
							unassignedArray: angular.copy($scope.tasks.applicationsArrayUnassigned), 
							assignedArray: data.data.list, 
							groupName: bundle.name,
							isGroup: false
						});
					}
				}
				if(isGroup){
					$scope.groups.applicationsArray = angular.copy($scope.tasks.applicationsArrayAssigned).
					concat(angular.copy($scope.tasks.applicationsArrayUnassigned));
				}else{
					$scope.tasks.applicationsArray = angular.copy($scope.tasks.applicationsArrayAssigned).
					concat(angular.copy($scope.tasks.applicationsArrayUnassigned));
				}
			}, function(){
				$log.info('Error on getting Applications');
			});
		}else{
			$scope.tasks.applicationsArray = angular.copy($scope.tasks.applicationsArrayUnassigned)
		}
	}

	// Get User Preference
	$scope.tasks.getUsrPreferences = function(isGroup){
		restCalls.getUsrPreferences({details: 'MOVE_EVENT,CURR_BUNDLE'}, function(data){
			$log.info('Success on getting User Preferences');
			$log.info(data.data.preferences);
			if(data.data.preferences.MOVE_EVENT){
				if(isGroup){
					$scope.groups.selectedEvent = {};
					$scope.groups.selectedBundle = {};
					$scope.groups.selectedEvent.id = data.data.preferences.MOVE_EVENT;
					$scope.groups.selectedBundle.id = data.data.preferences.CURR_BUNDLE;
				}else{
					$scope.tasks.selectedEvent = {};
					$scope.tasks.selectedBundle = {};
					$scope.tasks.selectedEvent.id = data.data.preferences.MOVE_EVENT;
					$scope.tasks.selectedBundle.id = data.data.preferences.CURR_BUNDLE;
				}
			}
		}, function(){
			$log.info('Error on getting User Preferences');
		});
	}

	// Get Task Batch Info
	$scope.tasks.getTaskBatchInfo = function(params){
		restCalls.getTaskBatchInfo(params, function(data){
			var results = null;
			$log.info('Success on getting Task Batch Info');
			$log.info(data);
			if(results){
				$scope.tasks.showDeletePreviouslyGenerated = true;
			}
		}, function(){
			$log.info('Error on getting Task Batch Info');
		});
	}

	// Get List of Task batches for a given Recipe
	$scope.tasks.getListTaskBatches = function(params){
		$scope.enabledGridSelection = false;
		restCalls.getListTaskBatches(params, function(data){
			$log.info('Success on getting Task Batches');
			$log.info(data);

			var	tasksActionsTemplate = '<div class="gridIcon">'+
				'<a href="" class="actions edit" title="Edit"'+
					'ng-click="tasks.tasksGridActions(row, \'refresh\')">'+
				'<span class="glyphicon glyphicon-refresh"></span>'+
				'</a>'+
				'<a href="" class="actions remove" title="Remove"'+
					'ng-click="tasks.tasksGridActions(row, \'remove\')">'+
				'<span class="glyphicon glyphicon-trash"></span>'+
				'</a>'+
				'</div>',
				checkboxTemplate = '<div class="gridIcon">'+
				'<span class="actions" style="text-align: center;">'+
				'<input type="checkbox" name="isPublished" ng-checked="row.entity.isPublished"'+
					'id="isPublished" ng-model="tasks.isPublished"'+
					'ng-change="tasks.tasksGridActions(row, \'publishUnpublish\')"/>'+
				'</span>'+
				'</div>';
			
			$scope.tasks.gridData = (data.data.list.length > 0) ? 
				data.data.list : [{'message': 'No results found', 'context': 'none'}];

			$scope.tasks.colDef = (data.data.list.length > 0) ? [
				{field:'id', displayName:'Target', enableCellEdit: false, width: '**'},
				{field:'contextName', displayName:'Context', enableCellEdit: false, width: '**'},
				{field:'taskCount', displayName:'# of', cellClass: 'text-center', 
					enableCellEdit: false, width: '**'},
				{field:'exceptionCount', displayName:'Exceptions', cellClass: 'text-center', 
					enableCellEdit: false, width: '***'},
				{field:'createdBy', displayName:'Generated By', enableCellEdit: false, width: '****'},
				{field:'dateCreated', displayName:'Generated At', enableCellEdit: false, width: '****'},
				{field:'status', displayName:'Status', enableCellEdit: false, width: '**'},
				{field:'versionNumber', displayName:'Version', cellClass: 'text-center', 
					enableCellEdit: false, width: '**'},
				{field:'isPublished', displayName:'Published', cellClass: 'text-center', 
					enableCellEdit: false, width: '**',
					cellTemplate: checkboxTemplate},
				{field:'', displayName:'Actions', cellClass: 'text-center', enableCellEdit: false, 
					width: '**', sortable: false, cellTemplate: tasksActionsTemplate}
			] : [
				{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}
			];

			if(data.data.list.length == 0){
				$scope.tasks.selectedTaskBatch = null;
			}
			
			$timeout(function(){
				$scope.enabledGridSelection = true;
			}, 200)

		}, function(){
			$log.info('Error on getting Task Batches');
		});
	}

	// Get a TaskBatch for a given TaskBatch id
	$scope.tasks.getTaskBatch = function(id){
		restCalls.getTaskBatch({details: id}, function(data){
			$log.info('Success on getting Task Batch');
			$log.info(data);
		}, function(){
			$log.info('Error on getting Task Batch');
		});
	}

	$scope.tasks.getEventsAndBundles();
	$scope.tasks.getListBundles(0);
	$scope.tasks.getListInBundle(0);
	///////////////////////////////////////////////////////


	//Tasks Grid///////////////
	
	$scope.tasks.gridData = [{'message': 'No results found', 'context': 'none'}];

	$scope.tasks.colDef = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
	
	var lastLoop;
	$scope.tasks.tasksGrid = {
		data: 'tasks.gridData',
		multiSelect: false,
		columnDefs: 'tasks.colDef',
		selectedItems: [],
		plugins: [layoutPluginTasks],
		enableCellEditOnFocus: false,
		afterSelectionChange: function(rowItem){
			if(rowItem != $scope.tasks.currentSelectedTaskRow){
				$scope.tasks.currentSelectedTaskRow = rowItem;
				// This hack is to avoid changeRecipe() to be executed many times. 
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				$timeout.cancel(lastLoop);
				lastLoop = $timeout(function(){
					if(rowItem.entity.id){
						$log.info('Row changed');
						$scope.tasks.selectedTaskBatch = rowItem.entity;
					}
				}, 50)
				
			}
		}
	}; 

	// Tasks Grid Actions
	$scope.tasks.tasksGridActions = function(item, action){
		$log.info(action);
		if(action == 'refresh'){
			// TODO // There is no service for this yet.
		}else if(action == 'remove'){
			$scope.tasks.deleteTaskBatch(item.entity.id);
		}else if(action == 'publishUnpublish'){
			item.entity.isPublished = (item.entity.isPublished) ? false : true;
			$scope.tasks.publishUnpublishTaskBatch(item.entity);
		}
	};

	// Delete tasks batch function
	$scope.tasks.deleteTaskBatch = function(id){
		restCalls.deleteTaskBatch({section: id}, function(data){
			$log.info('Success on deleting task');
			$log.info(data);
		}, function(){
			$log.info('Error on deleting task');
		});
	}

	// Publish && Unpublish tasks batch functions
	$scope.tasks.publishUnpublishTaskBatch = function(obj){
		if(obj.isPublished){
			restCalls.publishTaskBatch({section: obj.id}, function(data){
				$log.info('Success on publishing task');
				$log.info(data);
			}, function(){
				$log.info('Error on publishing task');
			});
		}else{
			restCalls.unpublishTaskBatch({section: obj.id}, function(data){
				$log.info('Success on unpublishing task');
				$log.info(data);
			}, function(){
				$log.info('Error on unpublishing task');
			});
		}
	}

	// This should to be fired whenever the usr click on "Task Generation" tab. 
	// It selects the first row if there isn't any selected
	$scope.tasks.updateGrid = function(){
		$log.log($scope.tasks.tasksGrid);
		if($scope.tasks.tasksGrid.selectedItems.length == 0){
    		layoutPluginTasks.updateGridLayout();
    		$scope.tasks.tasksGrid.selectRow(0, true)
    	}
    };

	///////////////////////////

	//GROUPS///////////////////

	var isGroup = true;

	$scope.groups = {
		eventsArray : [],
		boundlesArray : [],
		bundlesArrayUnassigned : [],
		bundlesArrayAssigned : [],
		applicationsArray : [],
		applicationsArrayUnassigned : [],
		applicationsArrayAssigned : [],
		selectedEvent : '',
		selectedBundle : '',
		selectedApplication : '',
		fetchBtnDisabled : true,
		contextId: 0,
		gridData: [],
		colDef: [],
		groupsArray: [],
		assetsArray: [],
		selectedGroup: ''
	}

	// Put select elements in blank. 
	$scope.groups.blankBundles = function(){
		$scope.groups.selectedBundle = '';
		$scope.groups.selectedApplication = '';
		$scope.groups.bundlesArrayAssigned = [];
		$scope.groups.bundlesArray = $scope.groups.bundlesArrayUnassigned;
		$scope.groups.applicationsArrayAssigned = [];
		$scope.groups.applicationsArray = $scope.groups.applicationsArrayUnassigned;
	};
	$scope.groups.blankApplications = function(){
		$scope.groups.selectedApplication = '';
		$scope.groups.applicationsArrayAssigned = [];
		$scope.groups.applicationsArray = $scope.groups.applicationsArrayUnassigned;
	};

	// Events for select elements.
	$scope.groups.eventSelected = function(){
		$scope.groups.selectedBundle = '';
		$scope.groups.selectedApplication = '';
		if($scope.groups.selectedEvent && $scope.currentSelectedRecipe.context != 'Event'){
			$scope.tasks.getListBundles($scope.groups.selectedEvent, isGroup);
		}else{
			$scope.groups.blankBundles();
		}
		$scope.groups.getContextId();
	};
	$scope.groups.bundleSelected = function(){
		$scope.groups.selectedApplication = '';
		if($scope.groups.selectedBundle && $scope.currentSelectedRecipe.context != 'Bundle'){
			$scope.tasks.getListInBundle($scope.groups.selectedBundle, isGroup);
		}else{
			$scope.groups.blankApplications();
		}
		$scope.groups.getContextId();
	};
	$scope.groups.applicationSelected = function(){
		$scope.groups.getContextId();
	};

	$scope.groups.getContextId = function(){
		var context = $scope.selectedRecipe.context;
		$log.log(context);
		switch(context){
			case 'Application':
			 	$scope.groups.contextId = ($scope.groups.selectedApplication) ? $scope.groups.selectedApplication.id : 0;
				break;
			case 'Event':
				$scope.groups.contextId = ($scope.groups.selectedEvent) ? $scope.groups.selectedEvent.id : 0;
				break;
			case 'Bundle':
				$scope.groups.contextId = ($scope.groups.selectedBundle) ? $scope.groups.selectedBundle.id : 0;
				break;
			default:
				$scope.groups.contextId = 0;
		}
		$scope.groups.fetchBtnDisabled = ($scope.groups.contextId) ? false : true;
	}

	var countTemplate = '<div class="gridIcon">'+
		'<span class="actions" style="text-align: center;">'+
			'<span ng-bind="row.entity.assets.length"></span>'+
		'</span>'+
		'</div>';

	$scope.groups.fetchGroups = function(recipeId, contextId){
		$scope.enabledGridSelection = false;
		restCalls.getGroups({recipeVersionId: $scope.selectedRecipe.recipeVersionId, contextId: $scope.groups.contextId}, function(data){
			$log.info('Success on getting Groups');
			$log.info(data.data.groups);
			$scope.groups.groupsArray = data.data.groups;
			loadAssets([]);
			$timeout(function(){
				$scope.enabledGridSelection = true;
			}, 200)
		}, function(){
			$log.warn('Error on getting Groups');
			$scope.groups.groupsArray = [{'message': 'Unexpected error', 'context': 'none'}];
			$scope.groups.colDef = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
			$timeout(function(){
				$scope.enabledGridSelection = true;
			}, 200)
		});
	}

	$scope.groups.groupsGrid = {
		data: 'groups.groupsArray',
		multiSelect: false,
		columnDefs: 'groups.colDef',
		enableCellEditOnFocus: false,
		selectedItems: [],
		plugins: [layoutPluginGroups],
		afterSelectionChange: function(rowItem){
			if(rowItem != $scope.groups.selectedGroup){
				// This hack is to avoid changeRecipe() to be executed many times. 
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				$timeout.cancel(lastLoop);
				lastLoop = $timeout(function(){
					if(rowItem.entity.name){
						$log.info('Group changed');
						$scope.groups.selectedGroup = rowItem.entity;
						$log.info($scope.groups.selectedGroup);
						$scope.enabledGridSelection = false;
						loadAssets(rowItem.entity.assets);
					}
				}, 50)
				
			}
		}
	};

	// This should to be fired whenever the usr click on "Groups" tab. 
	// It selects the first row if there isn't any selected
	$scope.groups.updateGrid = function(){
    	$log.log($scope.groups.groupsGrid);
    	$scope.groups.colDef = [
			/*{field:'class', displayName:'Class', enableCellEdit: false},*/
			{field:'name', displayName:'Name', enableCellEdit: false},
			{field:'count', displayName:'Count', cellTemplate: countTemplate, enableCellEdit: false}
		];
    };

    $scope.tasks.getEventsAndBundles(isGroup);
	$scope.tasks.getListBundles(0, isGroup);
	$scope.tasks.getListInBundle(0, isGroup);



	// ASSETS //////////////
	$scope.assets = {
		colDef : [],
		gridData : [],
		showAssetsGrid: false,
		selectedAsset: ''
	}

	var lastLoop,
		loadAssets; 

	loadAssets = function(data){
		$scope.groups.showAssetsGrid = true;
		$scope.assets.gridData = data;
		$scope.assets.colDef = [
			{field:'name', displayName:'Name', enableCellEdit: false},
			{field:'assetType', displayName:'Asset Type', enableCellEdit: false}
		];

		$timeout(function(){
			$scope.enabledGridSelection = true;
		}, 200)
	};

	$scope.assets.assetsGrid = {
		data: 'assets.gridData',
		multiSelect: false,
		columnDefs: 'assets.colDef',
		enableCellEditOnFocus: false,
		selectedItems: [],
		afterSelectionChange: function(rowItem){
			if(rowItem != $scope.assets.selectedAsset){
				// This hack is to avoid changeRecipe() to be executed many times. 
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				$timeout.cancel(lastLoop);
				lastLoop = $timeout(function(){
					if(rowItem.entity.name){
						$log.info('Asset changed');
						$scope.assets.selectedAsset = rowItem.entity;
						$log.info($scope.assets.selectedAsset);
					}
				}, 50)
				
			}
		}
	};


    ///////////////////////

    // VERSIONS

    $scope.versions = {
    	versionsArray : [],
		selectedVersion : '',
		selectedVersionRow : '',
		currentSelectedTaskRow : '',
		toCompareVersions : [],
        toCompareVersion : null
    }

    $scope.versions.gridData = [];

    var	versionsActionsTemplate = '<div class="gridIcon">'+
		'<a href="" ng-hide="row.entity.isCurrentVersion || !row.entity.versionNumber"'+
			'class="actions edit" title="Edit"'+ 
			'ng-click="versions.versionsGridActions(row, \'revert\')">'+
			'<span class="glyphicon glyphicon-arrow-left"></span>'+
		'</a>'+
		'<a href="" class="actions remove" title="Remove"'+
		'ng-click="versions.versionsGridActions(row, \'remove\')">'+
			'<span class="glyphicon glyphicon-trash"></span>'+
		'</a>'+
		'</div>',
		currentVersionTemplate = '<div class="gridIcon">'+
		'<span class="actions" style="text-align: center;">'+
			'<span ng-bind="row.entity.isCurrentVersion && \'*\' || \'\'"></span>'+
		'</span>'+
		'</div>',
		versionNumberTemplate = '<div class="ngCellText" ng-class="col.colIndex()">'+
			'<span ng-cell-text ng-bind="row.entity.versionNumber && row.entity.versionNumber || \'WIP\'"></span>'+
		'</div>';
		

	$scope.versions.colDef = [
		{field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false, 
			cellTemplate: versionNumberTemplate, width: '**'},
		{field:'', displayName:'Current', enableCellEdit: false, sortable: false, 
			cellTemplate: currentVersionTemplate, width: '**'},
		{field:'lastUpdated', displayName:'Last Updated', enableCellEdit: false, width: '***'},
		{field:'createdBy', displayName:'Created By', enableCellEdit: false, width: '***'},
		{field:'', displayName:'Actions', cellClass: 'text-center', enableCellEdit: false, 
			sortable: false, cellTemplate: versionsActionsTemplate, width: '**'}
	];

	// Versions Grid Actions
	$scope.versions.versionsGridActions = function(item, action){
		$log.info(action);
		var version = (item.entity.versionNumber) ? 'version '+item.entity.versionNumber : 'WIP version';
		if(action == 'revert'){
			console.log(item.entity);
			confirmation = confirm("Reverting \"" + $scope.currentSelectedRecipe.name + "\" to " +
				version + "\n\nPress Okay to continue");
			if (confirmation == true){
				$log.info(item.entity);
				restCalls.revert({moreDetails:item.entity.id}, function(){
					$log.info('Success on Reverting');
					$scope.alerts.addAlert({type: 'success', msg: 'Reverted to version '+
						item.entity.versionNumber, closeIn: 1500});
					listRecipes();
					$scope.versions.getVersions({recipeId: $scope.selectedRecipe.recipeId})
				}, function(){
					$log.warn('Error on Reverting');
					$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to revert version'});
				});
			}
		}else if(action == 'remove'){
			console.log($scope.selectedRecipe);
			confirmation = confirm("Deleting " + version + " for \"" + 
				$scope.currentSelectedRecipe.name + "\" \n\nPress Okay to continue");
			if (confirmation == true){
				$log.info(item.entity);
				restCalls.discardWIP({details:$scope.selectedRecipe.recipeId, 
					moreDetails: item.entity.versionNumber}, function(){
					$log.info('Success on removing Recipe Version');
					$scope.alerts.addAlert({type: 'success', msg: 'Recipe Version Removed', closeIn: 1500});
					listRecipes();
					$scope.versions.getVersions({recipeId: $scope.selectedRecipe.recipeId});
				}, function(){
					$log.warn('Error on removing Recipe Version');
					$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to remove version'});
				})
			}
		}
	};

	// This should to be fired whenever the usr click on "Versions" tab. 
	// It selects the first row if there isn't any selected  
	$scope.versions.updateGrid = function(){
		$log.info('versions grid select');
		$log.info($scope.versions.versionsGrid);
		$scope.versions.getVersions({recipeId: $scope.selectedRecipe.recipeId});
    };

    $scope.versions.getRecipeVersionData = function(versionNumber){
    	restCalls.getARecipeVersion({details:$scope.currentSelectedRecipe.recipeId, moreDetails: versionNumber},
    	function(data){
	    	$log.info('Success on getting version');
			$log.info(data.data);
			$scope.versions.selectedVersion = data.data;
			$scope.versions.updateToCompareVersions();
		}, function(){
			$log.info('Error on getting version');
		});
    }

    $scope.versions.getRecipeVersionDataAndCompare = function(versionNumber){
    	if ($scope.versions.selectedVersion) {
	    	if (versionNumber == "WIP") {
	    		$scope.versions.compareVersions($scope.versions.selectedVersion, $scope.selectedRWip);
	    	} else {
	        	restCalls.getARecipeVersion({details:$scope.currentSelectedRecipe.recipeId, moreDetails: versionNumber},
	    	    	function(data){
	    		        $scope.versions.compareVersions($scope.versions.selectedVersion, data.data);
	    			}, function(){
	    				$log.info('Error on getting version');
	    			}
	    	    );
	    	}
    	}
    }

    $scope.versions.compareVersions = function(recipeVersion1, recipeVersion2){
		$scope.showCompareCodeDialog(
			recipeVersion1.sourceCode,
			recipeVersion2.sourceCode,
	 	    (angular.isNumber(recipeVersion1.versionNumber) && (recipeVersion1.versionNumber > 0))?("Version " + recipeVersion1.versionNumber):"WIP",
		    (angular.isNumber(recipeVersion2.versionNumber) && (recipeVersion2.versionNumber > 0))?("Version " + recipeVersion2.versionNumber):"WIP"
		);
    }

	$scope.versions.onCompareVersions = function() {
		$scope.versions.getRecipeVersionDataAndCompare($scope.versions.toCompareVersion);	
    };

	var lastLoop;
	$scope.versions.versionsGrid = {
		data: 'versions.versionsArray',
		multiSelect: false,
		columnDefs: 'versions.colDef',
		enableCellEditOnFocus: false,
		selectedItems: [],
		plugins: [layoutPluginVersions],
		afterSelectionChange: function(rowItem){
			if(rowItem != $scope.versions.selectedVersionRow){
				$scope.versions.selectedVersionRow = rowItem;
				// This hack is to avoid the code to be executed many times. 
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				$timeout.cancel(lastLoop);
				lastLoop = $timeout(function(){
					if(rowItem.entity.id){
						$log.info('Version Row changed');
						$log.info(rowItem.entity);
						var versionNumber = (rowItem.entity.versionNumber) ? rowItem.entity.versionNumber : 0;
						$scope.versions.getRecipeVersionData(versionNumber);
						$log.info($scope.versions.selectedVersionRow);
					}
				}, 50)
				
			}
		}
	};

	$scope.versions.getVersionsArray = function(arr){
		$scope.versions.versionsArray = [];
	}

	$scope.versions.getVersionsArray();

	$scope.versions.getVersions = function(obj){
		$scope.versions.selectedVersionRow = {};
		$scope.versions.versionsGrid.selectedItems = [];
		restCalls.getVersions({moreDetails: obj.recipeId}, function(data){
			$log.info('Success on getting versions');
			$log.info(data.data.recipeVersions);
			$scope.versions.versionsArray = data.data.recipeVersions;
			setTimeout(function(){
				$scope.versions.versionsGrid.selectRow(0, true);
			}, 100)
		}, function(){
			$log.info('Error on getting versions');
		});
	}

	$scope.versions.updateToCompareVersions = function() {
		var toCompareVersions = [];
		if ($scope.versions.versionsArray && $scope.versions.selectedVersion) {
			var addWIP = false;
			var selectedVersion = $scope.versions.selectedVersion;
			angular.forEach($scope.versions.versionsArray,
			    function(value, key){
				    if (selectedVersion.versionNumber != value.versionNumber) {
					    if (angular.isNumber(value.versionNumber)) {
						    toCompareVersions.push(value.versionNumber);
					    }
				    }
				}
			);
			if ($scope.editingRecipe || $scope.selectedRecipe.hasWIP) {
				toCompareVersions.splice(0, 0, "WIP");				
			}
		}
		$scope.versions.toCompareVersions = toCompareVersions;
		$scope.versions.toCompareVersion = null;
	}

	////////////////////////

	$(window).on('beforeunload', function(){
 	   if ($scope.editingRecipe){
 	      return 'Warning: Switching pages will cause you to loose unsaved changes to the recipe'
 	   }
 	});

});

angular.module('modNgBlur', [])
.directive('ngBlur', function () {
	return function (scope, elem, attrs) {
		elem.bind('blur', function () {
			scope.$apply(attrs.ngBlur);
		});
	};
});

app.factory('servicesInterceptor', [function() {
    var servicesInterceptor = {
        response: function(response) {
        	var loginRedirect = response.headers('x-login-url');
            if(!loginRedirect) {
            	try {
            		var json = angular.fromJson(response.data);
                	if (json.errors && json.errors.length > 0) {
						var errorDiv = angular.element(document.querySelector('#errorModalText'));
						var errorsHTML = "<ul>";
						for (var j = 0; j < json.errors.length; j++) {
							errorsHTML = errorsHTML + "<li>" + json.errors[j] + "</li>";
						}
						errorsHTML = errorsHTML + "</ul>";
						errorDiv.html(errorsHTML);
						$('#errorModal').modal('show');
                	} else {
                		return response;
                	}
            	} catch (e) {
                	return response;
            	}
            }else{
            	alert("Your session has expired and need to login again.");
            	//location.reload();
            	window.location.href = loginRedirect;
            }
        }
    };
    return servicesInterceptor;
}]);

/**
 * This controller implements the behaviour for the source code diff dialog
 */
var SourceCodeDiffController = function ($scope, $modalInstance, $timeout, leftSourceCode, rightSourceCode, leftLabel, rightLabel) {
	$scope.leftSourceCode = leftSourceCode;
	$scope.rightSourceCode = rightSourceCode;
	$scope.leftLabel = leftLabel;
	$scope.rightLabel = rightLabel;

	$scope.removeSrcCodeLoaderListener = $scope.$on('sourceCodeDiffModalLoaded', function(evt) {
		$timeout( function() {
		    var compareView = angular.element('#compareViewport');
		    compareView.mergely({
			   viewport: true,
			   editor_width: '47%',
			   editor_height: '402px',
			   change_timeout: 100,
			   cmsettings: { mode: "", readOnly: true, lineNumbers: true, lineWrapping: false },
			   lhs: function(setValue) {
   				   setValue($scope.leftSourceCode);
			   },
			   rhs: function(setValue) {
				   setValue($scope.rightSourceCode);
			   }
		    });
		    $scope.removeSrcCodeLoaderListener();
		}, 250);
	});

	$scope.close = function () {
		$modalInstance.dismiss('close');
	};
};
