var app = angular.module('cookbookRecipes', ['ngGrid', 'ngResource', 'ui.bootstrap', 'modNgBlur', 'ui.codemirror']);

app.config(['$logProvider', function($logProvider) {  
   //$logProvider.debugEnabled(false);  
}]);

app.controller('CookbookRecipeEditor', function($scope, $rootScope, $http, $resource, $timeout, $modal, 
	$log, $location, $anchorScroll, $sce) {
	
	// All Vars used
	var restCalls, restCalls, listRecipes, columnSel, actionsTemplate, updateBtns, lastLoop, confirmation, 
	confirmation, rowToShow, ModalInstanceCtrl;

	$http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
	// Resource Calls

	restCalls = $resource(
		'/tdstm/ws/cookbook/:section/:details/:moreDetails',
		{
			section: "@section",
			details: "@details",
			moreDetails: "@moreDetails"
		},
		{
			archive: {
				method: "POST",
				params: {
					section: "recipe",
					details: "archive"
				}
			},
			unarchive: {
				method: "POST",
				params: {
					section: "recipe",
					details: "unarchive"
				}
			},
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
			discardWIP: {
				method: "DELETE",
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
					section: "recipe",
					details: "revert"
				}
			},
			validate: {
				method: "POST",
				params: {
					section: "recipe",
					details: "validateSyntax"
				}
			},
			putInRecipe: {
				method: "PUT",
				params: {
					section: "recipe"
				}
			},
			getTasks: {
				method: "GET",
				params: {
					section: "recipe"
				}
			}
		}
		);

	// Default data to get recipes
	$scope.context = 'All';
	$scope.archived = 'n';

	rowToShow = null;

	// Method to Get the list of Recipes.
	listRecipes = function(ind){
		$scope.recipes = restCalls.getListOfRecipes({archived: $scope.archived, context: $scope.context}, function(data){
			$log.info('Success on getting Recipes');
			if(data.data){
				$scope.totalItems = data.data.list.length;
				$scope.gridData = ($scope.totalItems) ? data.data.list : [{'message': 'No results found', 'context': 'none'}]; 
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
	'<a href="" class="actions archive" title="Archive" ng-click="gridActions(row, 2)" ng-hide="archived == \'y\'">'+
	'<span class="glyphicon glyphicon-folder-close"></span>'+
	'</a>'+
	'<a href="" class="actions unarchive" title="UnArchive" ng-click="gridActions(row, 4)" ng-hide="archived == \'n\'">'+
	'<span class="glyphicon glyphicon-folder-open"></span>'+
	'</a>'+
	'<a href="" class="actions remove" title="Remove" ng-click="gridActions(row, 3)">'+
	'<span class="glyphicon glyphicon-trash"></span>'+
	'</a>'+
	'</div>';
	$scope.edittableField = '<input ng-class="colt' + columnSel.index + 
		'" ng-input="COL_FIELD" ng-model="COL_FIELD" ng-keydown="keyPressed($event, row, col)"'+ 
		'ng-blur="updateEntity(row, updateEntity.execute)"/>';
	$scope.colDef = [
	{field:'name', displayName:'Recipe', enableCellEdit: true, enableCellEditOnFocus: false, width: '***', 
		editableCellTemplate: $scope.edittableField},
	{field:'description', displayName:'Description', enableCellEdit: true, enableCellEditOnFocus: false, width: '******', 
		editableCellTemplate: $scope.edittableField},
	{field:'context', displayName:'Context', enableCellEdit: false, width: '**'},
	{field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '***'},
	{field:'lastUpdated', displayName:'Last Updated', enableCellEdit: false, width: '****'},
	{field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false, width: '**'},
	{field:'hasWIP', displayName:'WIP', cellClass: 'text-center', enableCellEdit: false, width: '*'},
	{field:'', displayName:'Actions', cellClass: 'text-center', enableCellEdit: false, width: '**', sortable: false, 
		cellTemplate: actionsTemplate}
	];

	$scope.colDefNoData = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
	$scope.colDefinition = $scope.colDef;


	$scope.mySelection = [];
	$scope.currentSelectedRow = {};
	
	$scope.editingRecipe = false;
	$scope.gridOptions = {
		data: 'gridData',
		multiSelect: false,
		selectedItems: $scope.mySelection,
		columnDefs: 'colDefinition',
		enableCellEditOnFocus: false,
		enableCellEdit: true,
		beforeSelectionChange: function(rowItem){
			if($scope.editingRecipe){                
				confirmation=confirm("Recipe " + $scope.currentSelectedRow.entity.name + " has unsaved changes."+ 
					"Press Okay to continue and loose those changes otherwise press Cancel");
				if (confirmation==true){
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
			if(rowItem != $scope.currentSelectedRow){
				$scope.currentSelectedRow = rowItem;

				// This hack is to avoid changeRecipe() to be executed many times. 
				//This is a common issue on the ng-grid for the afterSelectionChange event.
				$timeout.cancel(lastLoop);
				lastLoop = $timeout(function(){
					if(rowItem.entity.name){
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
		if(typeof(rowToShow) == 'number'){
			$scope.gridOptions.selectRow(rowToShow, true);
			rowToShow = null;
		}else if(typeof($scope.currentSelectedRow.rowIndex) == 'number'){
			$scope.gridOptions.selectRow($scope.currentSelectedRow.rowIndex, true);
		}else{
			$scope.gridOptions.selectRow(0, true);
		}
	});

	$scope.keyPressed = function(ev, row, col) {
		var charCode = ev.which || ev.keyCode
		if (charCode==13){
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

				var fillTheVars = function(){
					
					if(!item.hasWIP && item.versionNumber > 0){
						$scope.wipConfig[ind].opt = 'release';
					}

					$scope.selectedRecipe = ($scope.wipConfig[ind].opt == 'release') ?
						angular.copy($scope.selectedRVersion) : angular.copy($scope.selectedRWip);

					// A deep copy of the selected Recipe data. It won't change when editing.
					$scope.originalDataRecipe = angular.copy($scope.selectedRecipe);

					if(!$scope.selectedRWip){
						$scope.selectedRWip = ($scope.selectedRVersion) ? $scope.selectedRVersion : fillDefault();
					}
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
					// This is the selected recipe data.
					$scope.selectedRVersion = (data.data) ? data.data : null;
					if($scope.selectedRVersion.hasWIP){
						// Only call getWipRecipe if there is the recipe has WIP
						callGetWipService();
					}else{
						$scope.selectedRWip = angular.copy($scope.selectedRVersion);
						fillTheVars();
					}
					$log.info('Success on getting selected released recipe');
				}, function(){
					$scope.selectedRVersion = fillDefault();
					$log.info('No records found for selected released Recipe');
				});

			}else if($scope.wipConfig[ind].opt == 'wip' && !item.hasWIP){
				/*if($scope.originalDataRecipe){*/
					$scope.selectedRecipe = fillDefault();
					$scope.originalDataRecipe = angular.copy($scope.selectedRecipe);
				/*}*/
			}else{
				$log.info('The selected recipe has no version yet or has no WIP yet. Creating empty recipe..');
				$scope.selectedRecipe = fillDefault();
				$scope.originalDataRecipe = angular.copy($scope.selectedRecipe);
			}
			if(!$scope.activeTabs.history)
				$scope.activeTabs.editor  = true;

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
			$scope.selectedRWip = angular.copy($scope.selectedRecipe)
		}else if($scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].justReleased){
			$scope.selectedRWip.changelog = '';
			$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].justReleased = false;
		}

		$scope.selectedRecipe = (par == 'release') ? angular.copy($scope.selectedRVersion) : angular.copy($scope.selectedRWip);
		$scope.originalDataRecipe = angular.copy($scope.selectedRecipe);
		
	}

	// Watch changes at the selected Recipe.
	$scope.$watch('selectedRecipe', function(newValue, oldValue) {
		oldValue = angular.copy($scope.originalDataRecipe);
		if (JSON.stringify(newValue) === JSON.stringify(oldValue) || !oldValue || newValue.name == "") {
			$scope.editingRecipe = false;
			return;
		}
		$scope.editingRecipe = true;
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
				confirmation=confirm("Delete Recipe \""+row.entity.name+"\"?");
				if (confirmation==true){
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
			var tmpObj = $scope.selectedRecipe,
			selectedId = $scope.selectedRecipe.recipeId,
			selectedVersion = $scope.selectedRecipe.versionNumber;
			dataToSend = $.param(tmpObj)
			restCalls.saveWIP({details:selectedId, moreDetails:selectedVersion}, dataToSend, function(){
				$log.info('Success on Saving WIP');
				$scope.alerts.addAlert({type: 'success', msg: 'WIP Saved', closeIn: 1500});
				$scope.originalDataRecipe = angular.copy($scope.selectedRecipe);
				$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].opt = 'wip'
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
			confirmation=confirm("Only publish recipes if it is ready for use by all users."+
				"Press Okay to publish otherwise press cancel");
			if (confirmation==true){
				restCalls.release({moreDetails:selectedId}, dataToSend, function(){
					$log.info('Success on Releasing');
					$scope.alerts.addAlert({type: 'success', msg: 'Version Released', closeIn: 1500});
					$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].opt = 'release';
					$scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].justReleased = true;
					listRecipes();
				}, function(){
					$log.warn('Error on Saving WIP');
					if($scope.selectedRecipe.hasWIP){
						$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to release version'});    
					}else{
						$scope.alerts.addAlert({type: 'danger', msg: 'Error: You can only release recipes saved as WIP'});
					}
					
				});
			}
		},
		// Cancel
		cancelChanges : function(){
			var confirmation=confirm("You are about to cancel the changes for recipe: " +
				$scope.currentSelectedRow.entity.name + ". You want to proceed?");
			if (confirmation==true){
				$scope.selectedRecipe = angular.copy($scope.originalDataRecipe);
				return true;
			}else{
				return false;
			}
		},
		// Discard WIP
		discardWIP : function(){
			var confirmation=confirm("You are about to discard WIP: " + $scope.currentSelectedRow.entity.name + "."+
				" Do you want to proceed?");
			if (confirmation==true){
				var dataToSend = $.param($scope.selectedRecipe),
				selectedId = $scope.selectedRecipe.recipeId,
				selectedVersion = $scope.selectedRecipe.versionNumber;
				restCalls.discardWIP({details:selectedId, moreDetails:selectedVersion}, dataToSend, function(){
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
			var dataToSend = $.param({'sourceCode': $scope.selectedRecipe.sourceCode});
			restCalls.validate({}, dataToSend, function(data){
				$scope.currentSyntaxValidation = data.errors;
				$scope.activeSubTabs.editor.syntaxErrors = true;
			}, function(data){
				$log.warn('Error on validation');
				$scope.alerts.addAlert({type: 'danger', msg: 'Error: Unable to validate Syntax'});
			});
		}

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

	// Modal general stuff ------------------------------
	$scope.modalBtns = {};

	//Hide or show the modal
	$scope.showModal = function (visible, elem) {
		if (!elem)
			elem = element;

		if (visible)
			elem.modal("show");                     
		else
			elem.modal("hide");
	}

	// Syntax Modal Stuff

	$scope.codeEditorOptions = {
		lineNumbers: true,
		indentWithTabs: true,
		lineWrapping : true,
		extraKeys: {"Ctrl-Space": "autocomplete"}
	};

	$scope.syntaxModal = {
		sourceCode : '',
		showModal : false,
		btns : {
			save : function(){
				$log.log('saving edition');
				$scope.selectedRWip.sourceCode = $scope.syntaxModal.sourceCode;
				$scope.selectedRWip.changelog = $scope.selectedRecipe.changelog;
				$scope.selectedRecipe = angular.copy($scope.selectedRWip);
				if($scope.wipConfig[$scope.gridData[$scope.currentSelectedRow.rowIndex].recipeId].opt != 'wip'){
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

	var save = function(){
		var dataToSend = $.param($scope.newRecipe);
		restCalls.createRecipe(dataToSend, function(data){
			$scope.alerts.addAlert({type: 'success', msg: 'Recipe Created', closeIn: 1500});
			listRecipes($scope.gridData.length);
		}, function(){
			$log.warn('Error when creating recipe');
			$scope.alerts.addAlert({type: 'danger', msg: 'Saved'});
		});

		clearFields();
	}

	$scope.modalBtns.save = function () {
		$scope.showDialog = false;
		save();
	};

	$scope.modalBtns.cancel = function () {
		$scope.showDialog = false;
		$log.log('cancel create recipe');
	};

	$scope.showDialog = false;
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

});

angular.module('modNgBlur', [])
.directive('ngBlur', function () {
	return function (scope, elem, attrs) {
		elem.bind('blur', function () {
			scope.$apply(attrs.ngBlur);
		});
	};
});