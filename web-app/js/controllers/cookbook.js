/********************************************************************************
 * MODULE: Cookbook
 ********************************************************************************/

/**
 * Create namespaces
 */
tds.cookbook = tds.cookbook || {};
tds.cookbook.controller = tds.cookbook.controller || {};
tds.cookbook.service = tds.cookbook.service || {};
tds.cookbook.util = tds.cookbook.util || {};
tds.cookbook.directive = tds.cookbook.directive || {};


/********************************************************************************
 * CONTROLLERS
 ********************************************************************************/

/********************************************************************************
 * Main page controller
 */
tds.cookbook.controller.MainController = function(scope, rootScope, log, recipeManager) {

	scope.cookbook = {
		loadingIndicatorEnabled: true,
		progressPromise: null
	};

	rootScope.previousState = '';
	rootScope.currentState = '';

	rootScope.$on('$stateChangeSuccess', function(ev, to, toParams, from, fromParams) {
		rootScope.previousState = from.name;
		rootScope.currentState = to.name;
		log.log('Previous state:' + rootScope.previousState);
		log.log('Current state:' + rootScope.currentState);

		//Disable loading indicator when cookbook is in the state
		scope.cookbook.loadingIndicatorEnabled = (rootScope.currentState != "recipes.detail.gentasks.progress");

		//Clear timer if the state is not recipes.detail.gentasks.progress
		if ((scope.cookbook.progressPromise != null) &&
			(rootScope.currentState != "recipes.detail.gentasks.progress")) {
			clearInterval(scope.cookbook.progressPromise);
			scope.cookbook.progressPromise = null;
		}
	});

	$(window).on('beforeunload', function(){
	   if (recipeManager.isEditingRecipe()){
		  return 'Warning: Switching pages will cause you to loose unsaved changes to the recipe';
	   }
	});

};

tds.cookbook.controller.MainController.$inject = ['$scope', '$rootScope', '$log', 'recipeManager'];


/********************************************************************************
 * Recipes list controller
 */
tds.cookbook.controller.RecipesController = function(scope, rootScope, timeout, log, location, anchorScroll, sce, state, stateParams, utils, cookbookService, alerts, recipeManager) {

	scope.applyRootPath = utils.url.applyRootPath;

	// All Vars used
	var listRecipes, columnSel, actionsTemplate, lastLoop, lastLoopData, rowToShow;

	// Default data to get recipes
	scope.context = stateParams.context?stateParams.context:'All';
	scope.archived = stateParams.archived?stateParams.archived:'n';
	scope.dateCellTemplate = '<div class="ngCellText" ng-class="col.colIndex()"><span ng-cell-text>{{convertTZ(row.getProperty(col.field))}}</span></div>';

	scope.convertTZ = function(date) {
		var m = moment(date);
		m.tz(tdsCommon.timeZone());
		return tdsCommon.formatDateTime(m);
	}

	rowToShow = null;

	// Method to Get the list of Recipes.
	listRecipes = function() {
		scope.recipes = cookbookService.getListOfRecipes({archived: scope.archived, context: scope.context, rand: tdsCommon.randomString(16) },
			function(data){
			log.info('Success on getting Recipes');
			if (data.data) {
				scope.totalItems = data.data.list.length;
				scope.gridData = (scope.totalItems) ? data.data.list :
					[{'message': 'No results found', 'context': 'none'}];
				scope.colDefinition = (scope.totalItems) ? scope.colDef : scope.colDefNoData;
				timeout(function(){
					checkForSelectedRow();
				}, 100);
			} else {
				log.warn('Moved Temporarily');
				location.reload();
			}
		}, function(data){
			log.warn('Error on getting Recipes');
			alerts.addAlert({type: 'danger', msg: 'Received unexpected error while retrieving the list of Recipes'});
		});
	}

	// This should be called whenever we need to update the recipes Grid
	scope.changeRecipeList = function(){
		state.go('recipes', {'context': scope.context, 'archived': scope.archived});
	}

	// Initial call to get the list of Recipes
	listRecipes();

	scope.$on("refreshRecipes", function(evt) {
		listRecipes();
	});

	// Capture the sorted data when a user sorts the datagrid because the original data never gets sorted
	scope.$on("ngGridEventSorted", function(evt) {
		debugger;
		scope.sortedRows = evt.targetScope.renderedRows
	});

	// Pagination Stuff
	scope.totalItems = 4;
	scope.currentPage = 1;
	scope.maxSize = 5;

	scope.setPage = function (pageNo) {
		scope.currentPage = pageNo;
	};

	scope.bigTotalItems = 175;
	scope.bigCurrentPage = 1;
	//----------------------------

	scope.testingBoolean = true;

	// ng-grid stuff

	scope.preventSelection = false;

	scope.executeUpdate = true;

	columnSel = {index: 0},
	actionsTemplate = '<div class="gridIcon">'+
			'<a href="" class="actions edit" title="Edit Recipe" recipe-id="{{row.entity.recipeId}}" ng-click="gridActions(row, 0)">'+
				'<img src="'+ utils.url.applyRootPath('/icons/script_edit.png') + '" alt="Edit">' +
			'</a>'+
			'<a href="" class="actions revert" ng-class="{ disabled: gridData[row.rowIndex].versionNumber < 1 }"'+
				'title="Revert to other version" ng-click="gridActions(row, 1)">'+
				'<img src="'+ utils.url.applyRootPath('/icons/arrow_undo.png') + '" alt="Revert">' +
			'</a>'+
			'<a href="" class="actions archive" title="Archive Recipe" ng-click="gridActions(row, 2)"'+
				'ng-hide="archived == \'y\'">'+
				'<img src="'+ utils.url.applyRootPath('/icons/folder.png') + '" alt="Archive">' +
			'</a>'+
			'<a href="" class="actions unarchive" title="Unarchive Recipe" ng-click="gridActions(row, 4)"'+
				'ng-hide="archived == \'n\'">'+
				'<img src="'+ utils.url.applyRootPath('/icons/folder_go.png') + '" alt="Archive">' +
			'</a>'+
			'<a href="" class="actions remove" title="Delete Recipe" recipe-id="{{row.entity.recipeId}}" ng-click="gridActions(row, 3)">'+
			'<img src="'+ utils.url.applyRootPath('/icons/delete.png') + '" alt="Delete">' +
			'</a>'+
		'</div>';
	scope.edittableField = '<input class="ngGridCellEdit" ng-class="colt' + columnSel.index +
		'" ng-input="COL_FIELD" ng-model="COL_FIELD" ng-keydown="keyPressed($event, row, col)" />';
	scope.colDef = [
	{field:'name', displayName:'Recipe', enableCellEdit: true, enableCellEditOnFocus: false, width: '***',
		editableCellTemplate: scope.edittableField},
	{field:'description', displayName:'Description', enableCellEdit: true, enableCellEditOnFocus: false,
		width: '******', editableCellTemplate: scope.edittableField},
	{field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '***'},
	{field:'lastUpdated', displayName:'Last Updated', enableCellEdit: false, width: '****', cellTemplate : scope.dateCellTemplate},
	{field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false,
		width: '**'},
	{field:'hasWIP', displayName:'WIP', cellClass: 'text-center', enableCellEdit: false, width: '*'},
	{field:'', displayName:'Actions', cellClass: 'text-center', enableCellEdit: false, width: '**',
		sortable: false, cellTemplate: actionsTemplate}
	];

	scope.colDefNoData = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
	scope.colDefinition = scope.colDef;

	scope.currentSelectedRow = {};

	scope.enabledGridSelection = true;

	scope.recipesGridOptions = {
		data: 'gridData',
		multiSelect: false,
		selectedItems: [],
		columnDefs: 'colDefinition',
		enableCellEditOnFocus: false,
		enableCellEdit: true,
		enableHighlighting: true,
        enableColumnResize:true,
		beforeSelectionChange: function(rowItem){
			if (rowItem.rowIndex == scope.currentSelectedRow.rowIndex) {
				return (!scope.preventSelection);
			}
			if(recipeManager.isEditingRecipe()) {
				var confirmation = confirm("You have unsaved changes to the recipe '" + scope.currentSelectedRow.entity.name + "'." +
					"\n\nPress OK to continue and loose those changes otherwise press Cancel.");
				recipeManager.setEditingRecipe(!confirmation);
				return (confirmation == true);
			} else {
				return (!scope.preventSelection);
			}
		},
		// Function called after a datagrid row is selected
		afterSelectionChange: function(rowItem) {
			if ( (scope.currentSelectedRow.entity == undefined || rowItem.entity.recipeId != scope.currentSelectedRow.entity.recipeId) && scope.enabledGridSelection) {
				scope.currentSelectedRow = rowItem;
				// This hack is to avoid changeRecipe() to be executed many times.
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				timeout.cancel(lastLoop);
				lastLoop = timeout(function(){
					if(rowItem.entity.name){
						log.info('change recipe');
						scope.currentSelectedRecipe = angular.copy(rowItem.entity);

						goToSelectedRecipeState();

						//scope.changeRecipe();
					}else{
						scope.currentSelectedRecipe = null;
					}
				}, 50)

			}
		}
	};

	var goToSelectedRecipeState = function() {
		if (state.includes("**.recipes.detail.versions.**")) {
			state.go(state.$current.name, { recipeId: scope.currentSelectedRecipe.recipeId, recipeVersion: ''});
		} else {
			state.go(getRecipeNextState(), { recipeId: scope.currentSelectedRecipe.recipeId });
		}
	}

	var recipeValidNextStates = [
		"**.recipes.detail.gentasks.**",
		"**.recipes.detail.history.**",
		"**.recipes.detail.code.**",
		"**.recipes.detail.versions.**"
	];

	var getRecipeNextState = function() {
		var next = null;
		for (var i=0; i < recipeValidNextStates.length; i++) {
			if (state.includes(recipeValidNextStates[i])) {
				next = state.$current.name;
				break;
			}
		}
		if (next == null) {
			next = "recipes.detail"
		}
		return next;
	}

	/**
	 * This method is responsible for highlighting the selected row when clicked and when reloading the list
	 * after various update functions (e.g. Save WIP).
	 */
	var checkForSelectedRow = function() {
		var row = -1;
		if (state.params.recipeId) {
			// The scope.sortedRows is a separate dataset from the scope.gridData that gets populated when the user sorts the
			// list. The original scope.gridData does not get remains in the order when retrieved from the server. So if sortedRows
			// exists then this logic will figure out the selected row index from that dataset otherwise use the gridData list.
			// Note that the location of the recipeId is in different places within the List<Object>.
			var datagridSet = scope.sortedRows ? scope.sortedRows : scope.gridData;
			var recipeId = state.params.recipeId;
			row = (datagridSet.length > 0) ? 0 : -1;
			for (var i = 0; i < datagridSet.length; i++) {
				if (datagridSet[i].recipeId == recipeId || (datagridSet[i].entity && datagridSet[i].entity.recipeId == recipeId)) {
					row = i;
					break;
				}
			}
		} else if (!state.includes('**.create.**') && (datagridSet.length > 0)) {
			row = 0;
		}
		if (row >= 0) {
			scope.recipesGridOptions.selectRow(row, true);
			var grid = scope.recipesGridOptions.ngGrid;
			grid.$viewport.scrollTop(grid.rowMap[row] * grid.config.rowHeight);
		}
	}

	scope.$on('ngGridEventEndCellEdit', function(evt){
		var row=scope.currentSelectedRow;
		if (!row) {
			return true;
		}
		scope.updateRecipeData(row);
	});

	// Update recipe. After editing.
	scope.updateRecipeData = function(row) {
		var recipeToUpdate = {
			name : scope.gridData[row.rowIndex].name,
			description: scope.gridData[row.rowIndex].description
		},
		dataToSend = $.param(recipeToUpdate),
		rid = scope.gridData[row.rowIndex].recipeId;


		if(!scope.save) {
			scope.save = { promise: null, pending: false, row: null };
		}

		if(!scope.executeUpdate){
			return false;
		};

		scope.save.row = row.rowIndex;
		if(!scope.save.pending && (recipeToUpdate.name != scope.currentSelectedRecipe.name ||
			recipeToUpdate.description != scope.currentSelectedRecipe.description)) {
			scope.save.pending = true;
			scope.save.promise = timeout(function(){
				cookbookService.putInRecipe({details:rid}, recipeToUpdate, function(data){
					if(data.data){
						log.info('Recipe Updated');
						scope.save.pending = false;

						scope.currentSelectedRecipe.name = recipeToUpdate.name;
						scope.currentSelectedRecipe.description = recipeToUpdate.description;

						alerts.addAlert({type: 'success', msg: 'Saved', closeIn: 1000});
					}else{
						log.warn('Moved Temporarily');
						location.reload();
					}
				}, function(){
					log.warn('Recipe updating error');
					alerts.addAlert({type: 'danger', msg: 'Could not save the recipe'});
				});
			}, 500);
		}
	};

	scope.keyPressed = function(ev, row, col) {
		var charCode = ev.which || ev.keyCode
		if (charCode==13 || charCode==9){
			ev.stopPropagation();
			scope.$broadcast('ngGridEventEndCellEdit');
			//$scope.executeUpdate = true;
			//$('.gridStyle input:visible').blur();
		}else if(charCode==27){
			//$scope.executeUpdate = false;
			//$('.gridStyle input:not(:visible)').focus();
		}
	}

	//------------------------------------------

	var selectRecipeAndGo = function(row, stateName, recipeId) {
		scope.recipesGridOptions.selectRow(row.rowIndex, true);
		state.go(stateName, {'recipeId': recipeId});
	}

	var recipeGridAvailableActions = ['edit', 'revert', 'archive', 'delete', 'unarchive'];

	var recipeGridActions = {
		edit : function(row) {
			timeout(function(){
				selectRecipeAndGo(row, "recipes.detail.code.logs", row.entity.recipeId);
				anchorScroll();
				scope.preventSelection = false;
			}, 100)
		},

		revert : function(row) {
			timeout(function(){
				selectRecipeAndGo(row, "recipes.detail.versions", row.entity.recipeId);
				anchorScroll();
				scope.preventSelection = false;
			}, 100);
		},

		archive : function(row) {
			log.info('Archive code');
			var selectedId = row.entity.recipeId;
			cookbookService.archive({moreDetails:selectedId}, function(){
				log.info('Success on archiving Recipe');
				alerts.addAlert({type: 'success', msg: 'Recipe Archived', closeIn: 1500});
				listRecipes();
				scope.preventSelection = false;
			}, function(){
				log.warn('Error on archiving Recipe');
				alerts.addAlert({type: 'danger', msg: 'Error: Unable to Archive Recipe'});
				scope.preventSelection = false;
			});
		},

		unarchive : function(row) {
			log.info('Archive code');
			var selectedId = row.entity.recipeId;
			cookbookService.unarchive({moreDetails:selectedId}, function(){
				log.info('Success on unarchiving Recipe');
				alerts.addAlert({type: 'success', msg: 'Recipe UnArchived', closeIn: 1500});
				listRecipes();
				scope.preventSelection = false;
			}, function(){
				log.warn('Error on unarchiving Recipe');
				alerts.addAlert({type: 'danger', msg: 'Unable to UnArchive Recipe'});
				scope.preventSelection = false;
			});
		},

		delete : function(row) {
			var confirmation = confirm("You are about to delete the recipe '" + row.entity.name + "'.\n\nPress OK to delete the recipe otherwise press Cancel." );
			if (confirmation == true){
				var selectedId = row.entity.recipeId;
				cookbookService.discardWIP({details:selectedId}, function(){
					log.info('Success on removing Recipe');
					alerts.addAlert({type: 'success', msg: 'Recipe Removed', closeIn: 1500});
					scope.currentSelectedRow = {};
					rootScope.$broadcast("refreshRecipes");
					scope.preventSelection = false;
				}, function(){
					log.warn('Error on removing Recipe');
					alerts.addAlert({type: 'danger', msg: 'Error: Unable to Remove Recipe'});
					scope.preventSelection = false;
				});
			}
		}
	};

	// Actions for the Recipe Grid
	scope.gridActions = function(row, ind){

		if (ind != 0) {
			scope.preventSelection = true;
		}

		if (ind == 1 && scope.gridData[row.rowIndex].versionNumber < 1) {
			return false;
		}

		recipeGridActions[recipeGridAvailableActions[ind]](row);
	};

	scope.createRecipe = function() {
		var showDialog = true;
		if(recipeManager.isEditingRecipe()){
			var confirmation=confirm("You have unsaved changes in recipe '" + scope.currentSelectedRow.entity.name + "'.\n\n" +
				"Press OK to continue and loose those changes otherwise press Cancel.");
			showDialog = confirmation;
		}
		if (showDialog) {
			recipeManager.setEditingRecipe(false);
			timeout(function() {
				state.go("recipes.create");
			},
			100);
		}
	};

	// Initialize Component after rendered
	scope.init = function() {
		jQuery('.gridStyle').resizable({
			minHeight: 154,
			handles: 's'
		})
	};

	scope.init();
};

tds.cookbook.controller.RecipesController.$inject = ['$scope', '$rootScope', '$timeout', '$log', '$location', '$anchorScroll', '$sce', '$state', '$stateParams', 'utils', 'cookbookService', 'alerts', 'recipeManager'];


/********************************************************************************
 * Recipe detail controller
 */
tds.cookbook.controller.RecipeDetailController = function(scope, state, stateParams, $http, log, utils, cookbookService, recipeManager, alerts) {
	// To test alert msg directly
	//alerts.addAlert({type: 'success', msg: 'Could not save the recipe'});

	scope.recipeId = stateParams.recipeId;

	var fillDefault = function(clone){
		el = {
			"recipeId": scope.recipeId,
			"name": (clone!=null)?clone.name:'',
			"description": (clone!=null)?clone.description:'',
			"createdBy": null,
			"lastUpdated": "",
			"versionNumber": "",
			"hasWIP": false,
			"sourceCode": (clone!=null)?clone.sourceCode:'',
			"changelog": "",
			"clonedFrom": "",
			"recipeVersionId": null
		}

		return el;
	}

	scope.recipeTabs = [
		{ heading: "Task Generation", route:"recipes.detail.gentasks", active:false },
		{ heading: "History", route:"recipes.detail.history", active:false },
		{ heading: "Editor", route:"recipes.detail.code", active:false },
		{ heading: "Versions", route:"recipes.detail.versions", active:false }
	];

	scope.editor = {
		editingRecipe: false,
		recipeType: 'wip',
		selectedRVersion: fillDefault(),
		selectedRWip: fillDefault(),
		selectedRecipe: {},
		justReleased: false
	};
	scope.editor.selectedRecipe = scope.editor.selectedRVersion;

	scope.findEntityById = function(list, entityId) {
		var result = null;
		for (var i=0; i<list.length;i++) {
			if (list[i].id == entityId) {
				result = list[i];
				break;
			}
		}
		return result;
	}

	scope.contexts = {
		assetSelector: {},
		contextId: 0,
		eventsArray : [],
		boundlesArray : [],
		selectedEvent : '',
		validCurrentSelection : false,
		enableClearDefaultContext : false
	}

	// Returns true if the 'generate task' button should be enabled. Otherwise returns false
	scope.contexts.validateCurrentSelection = function(){
		var recipe = scope.editor.selectedRVersion;
		if (recipe && recipe.context) {
			if(scope.contexts.selectedEvent || scope.contexts.assetSelector && scope.contexts.assetSelector.tag && scope.contexts.assetSelector.tag.length > 0){
				log.log('matches event or tag');
				return true;
			}else{
				log.log('not Matching');
				return false;
			}
		}
		return false;
	}

	scope.$on("recipeUpdated", function(evt, recipe) {
		scope.contexts.checkValidSelection();
	});

	scope.contexts.onAssetSelectorChange = function() {
		scope.contexts.checkValidSelection();
	};

	scope.contexts.checkValidSelection = function(){
		var context = scope.editor.selectedRVersion?scope.editor.selectedRVersion.context:'';
		var recipeId = stateParams.recipeId;

		scope.contexts.enableClearDefaultContext = false;

		if(scope.contexts) {
			scope.contexts.validCurrentSelection = scope.contexts.validateCurrentSelection();
			if (!scope.$$phase) scope.$digest();
			if (scope.contexts.validCurrentSelection && scope.contexts.selectedEvent) {
				scope.$broadcast('validContextSelection', recipeId, scope.contexts.selectedEvent.id);
			}
		}

		// There is no context available
		if (_.isEmpty(scope.editor.selectedRVersion.context)) {
			return;
		}

		var sameEvent = validEventStatus();
		if(!sameEvent && (scope.editor.selectedRVersion.context.eventId == null && scope.contexts.selectedEvent != '' && scope.contexts.selectedEvent != null) || scope.editor.selectedRVersion.context.eventId != null &&  scope.contexts.selectedEvent == null) {
			return; // dirty
		}

		var sameTag = validTagStatus();
		if(!sameTag) {
			if (!scope.$$phase) scope.$digest();
			return; // dirty
		}

		scope.contexts.enableClearDefaultContext = true;

		if (!scope.$$phase) scope.$digest();
	};

	var validEventStatus = function () {
		var eventId = scope.contexts.selectedEvent? scope.contexts.selectedEvent.id:0;
		return ((!angular.isUndefined(eventId)) && (scope.editor.selectedRVersion.context.eventId == eventId));
	};

	var validTagStatus = function () {
		try {
			var context = scope.editor.selectedRVersion? scope.editor.selectedRVersion.context:'';
			var diff = scope.contexts.assetSelector.tag.filter(x => !context.tag.find(tag => tag.id === x.id)).concat(context.tag.filter(x => !scope.contexts.assetSelector.tag.find(tag => tag.id === x.id)));
			return diff.length === 0;
		} catch (e) {
			return true;
		}
	};

	////////////////////////////////
	// Events for select elements //
	////////////////////////////////

	/**
	 * This is triggered when the Event Selector is loaded and when the user changes the select in either
	 * the Generate tab or in the Editor > Groups tab. The latter two cases the resetSaveAsDefault is passed true. For the
	 * initial load it should be set to false.
	 * @param resetSaveAsDefault - set to true to show the Set Default Context otherwise the Clear Default Context is shown.
	 */
	scope.contexts.eventSelected = function(){
		scope.contexts.checkValidSelection();
		if (scope.contexts.selectedEvent && scope.contexts.selectedEvent.id) {
			$http.get(utils.url.applyRootPath('/ws/tag/event/' + scope.contexts.selectedEvent.id),
				{ headers: {'Content-Type': 'application/json'} }
			).then(function successCallback(response) {
				var result = response.data;
				if (result && result.data && result.data.length >= 1) {
					result.data.forEach(function(eventTag) {
						var eventId = eventTag.id;
						var tagId = eventTag.tagId;
						eventTag.id = tagId;
						eventTag.eventId = eventId;
						eventTag.label = eventTag.name;
					});
					scope.editor.selectedRVersion.context.tag = result.data;
					scope.contexts.checkValidSelection();
					if (!scope.$$phase) scope.$digest();
				}

				if (!validEventStatus() || !validTagStatus()) {
					scope.contexts.enableClearDefaultContext = false;
				} else {
					scope.contexts.enableClearDefaultContext = true;
				}
			});
		}
	};

	// Reset selects
	scope.contexts.resetSelects = function(){
		scope.contexts.selectedEvent = '';
	};

	scope.active = function(route){
		return state.includes("**." + route + ".**");
	};

	var checkActiveTabs = function() {
		scope.recipeTabs.forEach(function(tab) {
			tab.active = scope.active(tab.route);
		});
	};

	scope.contexts.setRecipeDefaultContext = function() {
		if (scope.recipeId > 0) {
			var dataToSend = {
				'context': {
					'tagMatch': (scope.contexts.assetSelector.operator === 'ALL') ? true : false,
					'tag': getTagsIds(scope.contexts.assetSelector.tag)
				}
			};
			if (scope.contexts && scope.contexts.selectedEvent && scope.contexts.selectedEvent.id) {
				dataToSend.context.eventId = scope.contexts.selectedEvent.id;
			}

			// Partially Remove Prototype
			if (window.Prototype) {
				delete Array.prototype.toJSON;
			}

			$http.post(utils.url.applyRootPath('/ws/cookbook/recipe/context/' + stateParams.recipeId),
				JSON.stringify(dataToSend),
				{headers: {'Content-Type': 'application/json'}}
			).then(function successCallback(response) {
				alerts.addAlert({type: 'success', msg: 'Default context updated', closeIn: 3000});
				// log.info('Success on set default context');
				scope.getRecipeData('wip');
			}, function(){
				alerts.addAlert({type: 'danger', msg: 'An error occurred while attempting to set the default context'});
				log.error('Error on set default context');
			});
		}
	}

	scope.contexts.clearRecipeDefaultContext = function() {
		cookbookService.deleteRecipeContext({'moreDetails':stateParams.recipeId}, function(data){
			alerts.addAlert({type: 'success', msg: 'Default context cleared.', closeIn: 3000});
			log.info('Default context cleared.');
			scope.getRecipeData('wip');
		}, function(data, status, headers, config){
			alerts.addAlert({type: 'danger', msg: 'Error on clear default context.'});
			log.info('Error on clear default context');
		});
	}

	/**
	 * Used to populate the Event and Tag Selectors
	 */
	scope.contexts.getEventsAndBundles = function() {
		cookbookService.getEventsAndBundles({rand: tdsCommon.randomString(16)}, function(data){
			log.info('Success on getting Events');
			log.info(data.data.list);
			scope.contexts.eventsArray = data.data.list;

			if (scope.editor.selectedRVersion && scope.editor.selectedRVersion.context && scope.editor.selectedRVersion.context.eventId) {
				scope.contexts.selectedEvent = scope.findEntityById(scope.contexts.eventsArray, scope.editor.selectedRVersion.context.eventId);
				scope.contexts.eventSelected();
			} else if (scope.contexts.selectedEvent != null) {
				scope.contexts.selectedEvent = scope.findEntityById(scope.contexts.eventsArray, scope.contexts.selectedEvent.id);
			}
		}, function(){
			log.info('Error on getting Events');
		});
	}

	/**
	 * Get the list of tags id from the current selected tags context
	 */
	var getTagsIds = function(tags) {
		var tagIds = [];
		tags.each( function(a){
			tagIds.push(parseInt(a.id));
		})
		return tagIds;
	}

	// Get User Preference
	var getUserPreferences = function() {
		cookbookService.getUserPreferences({details: 'MOVE_EVENT', rand: tdsCommon.randomString(16)}, function(data){
			log.info('Success on getting User Preferences');
			log.info(data.data.preferences);
			if (data.data.preferences.MOVE_EVENT) {
				scope.contexts.selectedEvent = {};
				scope.contexts.selectedEvent.id = data.data.preferences.MOVE_EVENT;
			}
		}, function(){
			log.info('Error on getting User Preferences');
		});
	}

	scope.$on("$stateChangeSuccess", function() {
		checkActiveTabs();
	});

	scope.go = function(route){
		if (!scope.active(route)) {
			state.go(route);
		}
	};

	// Switching RELEASE or WIP
	scope.switchWipRelease = function(recipeTypeToShow) {
		scope.editor.recipeType = recipeTypeToShow;

		if (scope.editor.justReleased){
			scope.editor.selectedRWip.changelog = '';
			scope.editor.justReleased = false;
		}

		scope.editor.selectedRecipe = (recipeTypeToShow == 'release') ? scope.editor.selectedRVersion : scope.editor.selectedRWip;
	}

	/**
	 * Used to load the recipe information after a recipe is selected
	 */
	scope.getRecipeData = function(defaultView) {
		cookbookService.getARecipeVersion({details:stateParams.recipeId, rand: tdsCommon.randomString(16)}, function(data){
			scope.editor.selectedRVersion = (data.data) ? data.data : null;
			if (scope.editor.selectedRVersion.context && scope.editor.selectedRVersion.context.tag) {
				scope.contexts.assetSelector.tag = scope.editor.selectedRVersion.context.tag;
			}
			if ((defaultView == 'release') && (scope.editor.selectedRVersion.versionNumber <= 0)) {
				defaultView = 'wip';
			}
			if(scope.editor.selectedRVersion.hasWIP){
				defaultView = 'wip';
				// Only call getWipRecipe if there is the recipe has WIP
				getWipData(defaultView);
			}else{
				scope.editor.selectedRWip = fillDefault(scope.editor.selectedRVersion);
				updateVersionSelection(defaultView);
			}

			// Trigger loading the events and tags for the generation context
			scope.contexts.getEventsAndBundles();

			log.info('Success on getting selected released recipe');
			log.info(data.data);
		}, function(){
			scope.editor.selectedRVersion = fillDefault();
			scope.editor.selectedRWip = angular.copy(scope.editor.selectedRVersion);
			updateVersionSelection('wip');
			log.info('No records found for selected released Recipe');
		});
	};

	// Only call getWipRecipe if there is the recipe has WIP
	var getWipData = function(defaultView){
		cookbookService.getARecipeVersion({details:stateParams.recipeId, moreDetails: 0, rand: tdsCommon.randomString(16)}, function(data){
			// This is the selected recipe data.
			scope.editor.selectedRWip = (data.data) ? data.data : null;
			log.info('Success on getting selected wip recipe');
			updateVersionSelection(defaultView);
		}, function(){
			scope.editor.selectedRWip = fillDefault(scope.editor.selectedRVersion);
			updateVersionSelection(defaultView);
			log.info('No records found for selected wip Recipe');
		});
	}

	var updateVersionSelection = function(defaultView) {
		recipeManager.setActiveVersion(scope.editor.selectedRVersion);
		recipeManager.setWip(scope.editor.selectedRWip);
		recipeManager.setEditingRecipe(false);
		scope.editor.editingRecipe = false;
		scope.switchWipRelease((defaultView!=null)?defaultView:'wip');
		// scope.contexts.getEventsAndBundles();
		scope.$broadcast("recipeUpdated", scope.editor.selectedRVersion);
	}

	// generate an array to show the select element correctly
	// Expect opts with the following data: isUnassigned (boolean), unassignedArray
	// (array with unassigned info)
	// assignedArray (array with assigned info), and groupName which would be the group name if
	// isUnassigned is false.
	scope.generateOptions = function(opts, scopeContext){
		var newArray = (opts.isUnassigned) ? opts.unassignedArray : opts.assignedArray;
		if(!opts.isUnassigned){
			angular.forEach(newArray, function(value, key){
				value.group = opts.groupName;
			})
		}
		return newArray
	}

	scope.getRecipeData('release');
}

tds.cookbook.controller.RecipeDetailController.$inject = ['$scope', '$state', '$stateParams', '$http', '$log', 'utils', 'cookbookService', 'recipeManager', 'alerts'];


/********************************************************************************
 * Create recipe controller
 */
tds.cookbook.controller.CreateRecipeController = function(scope, log, cookbookService, modalInstance, timeout, alerts) {

	var lastLoop;

	scope.newRecipe = {
		name: '',
		description: ''
	};

	scope.dateCellTemplate = '<div class="ngCellText" ng-class="col.colIndex()"><span ng-cell-text>{{convertTZ(row.getProperty(col.field))}}</span></div>';

	scope.convertTZ = function(date) {
		var m = moment(date);
		m.tz(tdsCommon.timeZone());
		return tdsCommon.formatDateTime(m);
	}

	scope.clone = {
		projectsArray : [],
		projectsStateArray : [],
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
				"projects": []
			}
		}
	}

	// Grid stuff
	scope.clone.colDef = [
		{field:'recipe', displayName:'Recipe', enableCellEdit: false, width: '**'},
		{field:'description', displayName:'Description', enableCellEdit: false, width: '**'},
		{field:'editor', displayName:'Editor', enableCellEdit: false, width: '**'},
		{field:'last', displayName:'Last', enableCellEdit: false, width: '**', cellTemplate : scope.dateCellTemplate},
		{field:'version', displayName:'Version', enableCellEdit: false, width: '**'},
	];

	// grid
	scope.clone.projectsGrid = {
		data: 'clone.gridData',
		multiSelect: false,
		columnDefs: 'clone.colDef',
		selectedItems: [],
		enableCellEditOnFocus: false,
		enableHighlighting: true,
		afterSelectionChange: function(rowItem){
			if(rowItem != scope.clone.currentSelectedRecipeRow){
				scope.clone.currentSelectedRecipeRow = rowItem;
				// This hack is to avoid changeRecipe() to be executed many times.
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				timeout.cancel(lastLoop);
				lastLoop = timeout(function(){
					if(rowItem.entity.name){
						log.info('Row changed');
						scope.clone.selectedRecipe = rowItem.entity;
						scope.clone.newRecipe.context = scope.clone.selectedRecipe.context;
						scope.clone.newRecipe.clonedFrom = scope.clone.selectedRecipe.recipeId;
					}
				}, 50)
			}
		}
	};

	var getProjectsAndStatuses = function(){
		log.log('getProjectsAndStatuses');
		cookbookService.getUserProjects(
			{currentPage: 0, maxRows: 1000, rand: tdsCommon.randomString(16)},
			function(data){
				log.info('Success on getting Project list');
				scope.clone.projectsArray = data.data.projects;
				angular.forEach(scope.clone.projectsArray, function(value, key){
					//$log.info($scope.clone.projectsStateArray);
					if(scope.clone.projectsStateArray.indexOf(value.status) == -1){
					   scope.clone.projectsStateArray.push(value.status);
					}
				})
			}, function(data){
				log.warn('Error on getting Project list');
				alerts.addAlert({type: 'danger', msg: 'Could not get the list of Projects'});
			}
		);
	};

	getProjectsAndStatuses();

	scope.optionsSelected = function(arg){
		if(scope.clone.selectedProject/* && $scope.clone.selectedProjectState*/){
			log.info('fill the grid');
			var putProjectId = (scope.clone.selectedProject && scope.clone.projectsArray[scope.clone.selectedProject])? scope.clone.projectsArray[scope.clone.selectedProject].id: '';
			cookbookService.getListOfRecipes(
				{projectType: putProjectId, rand: tdsCommon.randomString(16)},
				function(data){
					log.info('Success on getting Recipes to Clone');
					log.info(data.data.list);
					scope.clone.gridData = data.data.list;
					timeout(function(){
						scope.clone.projectsGrid.selectRow(0, true)
					}, 200)
				}, function(data){
					scope.clone.gridData = [];
					log.warn('Error on getting Recipes to Clone');
					//alerts.addAlert({type: 'danger', msg: 'Error: Could not get the list of Recipes'});
				}
			);
			//$scope.clone.gridData = angular.copy($scope.clone.projectsArray);
		}else{
			log.info('blank the grid');
			scope.clone.gridData = [];
			scope.clone.projectsGrid.selectedItems[0] = '';
		}
	}

	scope.refreshGrid = function(){
		scope.clone.colDef = [
			{field:'name', displayName:'Recipe', enableCellEdit: false, width: '**'},
			{field:'description', displayName:'Description', enableCellEdit: false, width: '**'},
			{field:'createdBy', displayName:'Editor', enableCellEdit: false, width: '**'},
			{field:'lastUpdated', displayName:'Last', enableCellEdit: false, width: '**', cellTemplate : scope.dateCellTemplate},
			{field:'versionNumber', displayName:'Version', enableCellEdit: false, width: '**'},
		]
	}

	var saveRecipe = function(args){
		var dataToSend = $.param(args);
		cookbookService.createRecipe(dataToSend, function(data){
			alerts.addAlert({type: 'success', msg: 'Recipe Created', closeIn: 1500});
			modalInstance.close(data.data.recipeId);
		}, function(){
			log.warn('Error when creating recipe');
			alerts.addAlert({type: 'danger', msg: 'Sorry but an unexpected error has occurred. Please contact support for assistance'});
		});
	}

	scope.save = function () {
		var recipeToSave = (scope.clone.activeTabs.newRecipe) ? scope.newRecipe : scope.clone.newRecipe;
		saveRecipe(recipeToSave);
	};

	scope.cancel = function () {
		modalInstance.dismiss('close');
		log.log('cancel create recipe');
	};

}

tds.cookbook.controller.CreateRecipeController.$inject = ['$scope', '$log', 'cookbookService', '$modalInstance', '$timeout',  'alerts'];


/********************************************************************************
 * Task Generation controller
 */
tds.cookbook.controller.TaskGenerationController = function(scope, state, $http, stateParams, log, timeout, utils, cookbookService, alerts, recipeManager) {

	scope.applyRootPath = utils.url.applyRootPath;

	scope.tasks = {
		activeTaskBatch: null,
		progressPercent:0,
		progressRemaining:'',
		currentTaskBeingGenerated : -1,
		taskBatch : {},
		generateOpts : {
			contextId: null,// - the select value from the select that represents the context
			recipeId: null,// - the id of the Recipe record to use to generate the tasks
			recipeVersionId: null,// - the id of the RecipeVersion record to use to generate the tasks
			useWIP: false,
			autoPublish: false,
			deletePrevious: true
		},
		show : {
			start: true,
			progress: false,
			completed: false
		},
		generation : {
			status: "",
			taskCreated : 0,
			exceptions : 0,
			exceptionLog : "",
			infoLog : ""
		}
	}

	// Get Task Batch Info
	scope.tasks.getTaskBatchInfo = function(params){
		params.rand = tdsCommon.randomString(16);
		cookbookService.getTaskBatchInfo(params, function(data){
			log.info('Success on getting Task Batch Info');
			if (data.status !== 'error') {
				scope.tasks.activeTaskBatch = data.data.taskBatch;
			}
			scope.tasks.show.completed = ((scope.tasks.activeTaskBatch != null) && (scope.tasks.activeTaskBatch.status == "Completed"));
		}, function(){
			scope.tasks.activeTaskBatch = null;
			log.info('Error on getting Task Batch Info');
		});
	}

	scope.tasks.viewGeneratedResults = function(e){
		var id = scope.tasks.currentTaskBeingGenerated;
		state.go('recipes.detail.history.detail.actions', { 'taskBatchId' : id});
	}

	/**
	 * e: event passed from the caller
	 * conf: configuration object passed
	 */
	scope.tasks.viewTaskGraph = function(e, conf){
		conf = conf || {}
		var eventsArray = scope.contexts.eventsArray;
		var eventName = scope.tasks.generation.eventName;
		var eventId = null;
		for (var i=0; i<eventsArray.length;i++) {
			if (eventsArray[i].name == eventName) {
				eventId = eventsArray[i].id;
				break;
			}
		}

		var location = utils.url.applyRootPath("/task/taskGraph?moveEventId=" + eventId);
		if(conf.target){ //Open in new Target
			window.open(location, conf.target);
		}else{
			window.location = location;
		}
	}

	scope.tasks.startOver = function(e){
		state.go('recipes.detail.gentasks.start');
	}

	//
	// Generate Tasks button clicked
	//
	scope.tasks.generateTask = function(e){
		var	dataToSend;

		scope.tasks.generateOpts.recipeId = stateParams.recipeId;

		dataToSend = $.param(scope.tasks.generateOpts);

		var callCenerateTask = true;

		if (scope.tasks.activeTaskBatch != null) {
			callCenerateTask = confirm("There are tasks previously created with this recipe for the selected context.\n\nPress Okay to delete or Cancel to abort.");
		}

		var getTagsIds = function(tags) {
			var tagIds = [];
			tags.each( function(a){
				tagIds.push(parseInt(a.id));
			})
			return tagIds;
		};

		if (callCenerateTask) {

			var postData = {
				recipeId: scope.tasks.generateOpts.recipeId ,
				recipeVersionId: scope.tasks.generateOpts.recipeVersionId ,
				useWIP: scope.tasks.generateOpts.useWIP ,
				autoPublish: scope.tasks.generateOpts.autoPublish ,
				deletePrevious: scope.tasks.generateOpts.deletePrevious ,
				eventId: (scope.contexts.selectedEvent)? scope.contexts.selectedEvent.id : null,
				tag: getTagsIds(scope.contexts.assetSelector.tag)
			};
			// Partially Remove Prototype
			if(window.Prototype) {
				delete Object.prototype.toJSON;
				delete Array.prototype.toJSON;
				delete Hash.prototype.toJSON;
				delete String.prototype.toJSON;
			}

			$http.post(utils.url.applyRootPath('/ws/task/generateTasks?rand='), JSON.stringify(postData), {headers: {'Content-Type': 'application/json'}}).then(function successCallback(response) {
				log.info('Success on generating task');
				log.info(response.data);
				// Flip the user to the progress screen
				//scope.tasks.show.start=false;
				scope.tasks.show.completed = false;

				if(response.data.data){
					var jobId = response.data.data.jobId;
					state.go("recipes.detail.gentasks.progress", {'jobId': jobId});
				}
			}, function(){
				alerts.addAlert({type: 'danger', msg: 'Unable to generate tasks. ' + response.data.headers().errormessage});
				log.info('Error on generating task');
			});
		}
	}

	scope.getTaskGenerationProgress = function() {
		return scope.tasks.progressPercent;
	}

	scope.getTaskGenerationStatus = function() {
		return scope.tasks.generation.status;
	}

	scope.getTaskGenerationTasksCreated = function() {
		return scope.tasks.generation.taskCreated;
	}

	scope.getTaskGenerationExceptions = function() {
		return scope.tasks.generation.exceptions;
	}

	scope.$on('validContextSelection', function(evt, recipeId, contextId) {
		scope.tasks.getTaskBatchInfo({recipeId: recipeId, eventId: contextId, logs: false});
	});

	if (state.is("recipes.detail.gentasks")) {
		timeout(function() {
			state.go("recipes.detail.gentasks.start");
		},
		100);
	}

}

tds.cookbook.controller.TaskGenerationController.$inject = ['$scope', '$state', '$http', '$stateParams', '$log', '$timeout', 'utils', 'cookbookService', 'alerts', 'recipeManager'];


/********************************************************************************
 * Task Batch Generation Start controller
 */
tds.cookbook.controller.TaskGenerationStartController = function(scope, state, stateParams, log, utils, cookbookService, alerts) {

}

tds.cookbook.controller.TaskGenerationStartController.$inject = ['$scope', '$state', '$stateParams', '$log', 'utils', 'cookbookService', 'alerts'];


/********************************************************************************
 * Task Batch Generation Progress controller
 */
tds.cookbook.controller.TaskGenerationProgressController = function(scope, state, stateParams, log, utils, cookbookService, alerts) {

	scope.tasks.progressPercent = 0;
	scope.tasks.progressRemaining = "";

	var jobId = stateParams.jobId;
	var taskId = jobId.split('-')[1];

	scope.tasks.currentTaskBeingGenerated = taskId;

	scope.cancelGeneration = function() {
		clearInterval(scope.cookbook.progressPromise);
		scope.cookbook.progressPromise = null;
		state.go("recipes.detail.gentasks.start", {'recipeId': stateParams.recipeId, 'taskBatchId': taskId});
	};

	scope.cookbook.progressPromise = setInterval(function() {
		cookbookService.getProgress({section: jobId, rand: tdsCommon.randomString(16)}, {"id" : jobId}, function(data) {
			scope.tasks.progressPercent = data.data.percentComp;
			scope.tasks.show.progress = ((data.data.percentComp < 100) &&
				((data.data.status == "Pending") || (data.data.status == "Processing")));

			if (!scope.tasks.show.progress) {
				clearInterval(scope.cookbook.progressPromise);
				scope.cookbook.progressPromise = null;
				if (data.data.status == "Failed" ) {
					scope.tasks.progressRemaining = "Generation FAILED";
					alerts.addAlert({type: 'danger', msg: utils.string.htmlToPlaintext(data.data.detail)});
				} else {
					scope.tasks.progressRemaining = data.data.detail;
					alerts.addAlert({type: 'success', msg: 'Finish generating tasks', closeIn: 3000});

					state.go("recipes.detail.gentasks.completed", {'taskBatchId': taskId});
				}
			} else {
				scope.tasks.progressRemaining = utils.string.htmlToPlaintext(data.data.detail);
			}
		});
	}, 1000);

}

tds.cookbook.controller.TaskGenerationProgressController.$inject = ['$scope', '$state', '$stateParams', '$log', 'utils', 'cookbookService', 'alerts'];


/********************************************************************************
 * Task Batch Generation Completed controller
 */
tds.cookbook.controller.TaskGenerationCompletedController = function(scope, state, stateParams, log, sce, utils, cookbookService) {

	var taskBatchId = stateParams.taskBatchId;

	cookbookService.getTaskBatch({section: taskBatchId, rand: tdsCommon.randomString(16)}, function(data){
		scope.tasks.activeTaskBatch = data.data.taskBatch;
		scope.tasks.generation.status = data.data.taskBatch.status;
		scope.tasks.generation.taskCreated = data.data.taskBatch.taskCount;
		scope.tasks.generation.exceptions = data.data.taskBatch.exceptionCount;
		scope.tasks.generation.exceptionLog = sce.trustAsHtml(data.data.taskBatch.exceptionLog);
		scope.tasks.generation.infoLog = sce.trustAsHtml(data.data.taskBatch.infoLog);
		scope.tasks.generation.eventName = data.data.taskBatch.eventName;
		scope.tasks.generation.tagNames = data.data.taskBatch.tagNames;
	}, function(){
		log.info('Error on getting Task Batch');
	});

}

tds.cookbook.controller.TaskGenerationCompletedController.$inject = ['$scope', '$state', '$stateParams', '$log', '$sce', 'utils', 'cookbookService'];


/********************************************************************************
 * Task Batch History controller
 */
tds.cookbook.controller.TaskBatchHistoryController = function(scope, state, stateParams, log, timeout, utils, cookbookService, alerts) {

	$(".menu-parent-tasks-cookbook").removeClass('active');
	$(".menu-parent-tasks-generation-history").addClass('active');
	$(".content-header h1").html("Generation History");
	$(".content-header ol.breadcrumb li:last-child").html("Generation History")
	scope.tasks = {};

	scope.tasks.gridData = [{'message': 'No results found', 'context': 'none'}];
	scope.tasks.colDef = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
	scope.tasks.singleRecipe = (stateParams.recipeId != null);
	scope.tasks.statePrefix = "recipes.detail.history";
	scope.tasks.limitDays = 'All';

	if (!scope.tasks.singleRecipe) {
		scope.tasks.statePrefix = "generationHistory";
	}

	var	tasksActionsTemplate = '<div class="gridIcon">'+
		'<a href="" class="actions edit" title="Reset Tasks"'+
			'ng-click="tasks.tasksGridActions(row, \'reset\')">'+
			'<img src="'+ utils.url.applyRootPath('/icons/table_refresh.png') + '" alt="Reset">' +
		'</a>'+
		'<a href="" class="actions remove" title="Delete Task Batch"'+
			'ng-click="tasks.tasksGridActions(row, \'remove\')">'+
			'<img src="'+ utils.url.applyRootPath('/icons/delete.png') + '" alt="Delete">' +
		'</a>'+
		'</div>';
	var checkboxTemplate = '<div class="gridIcon">'+
		'<span class="actions" style="text-align: center;">'+
		'<input type="checkbox" name="isPublished" ng-checked="row.entity.isPublished"'+
			'id="isPublished" ng-model="row.entity.isPublishedValue" ng-readonly="true"'+
			'ng-click="tasks.tasksGridActions(row, \'publishUnpublish\', $event)"/>'+
		'</span>'+
		'</div>';
	var recipeTemplate = '<div class="ngCellText" ng-class="col.colIndex()"><a href="#/recipes/{{row.entity.recipeId}}/code/logs">{{row.entity.recipeName}}</a></div>';

	var lastLoop;
	var layoutPluginTasks = new ngGridLayoutPlugin();

	scope.tasks.tasksGrid = {
		data: 'tasks.gridData',
		multiSelect: false,
		columnDefs: 'tasks.colDef',
		selectedItems: [],
		plugins: [layoutPluginTasks],
		enableCellEditOnFocus: false,
		enableHighlighting: true,
		afterSelectionChange: function(rowItem){
			if(rowItem != scope.tasks.currentSelectedTaskRow){
				scope.tasks.currentSelectedTaskRow = rowItem;
				// This hack is to avoid changeRecipe() to be executed many times.
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				timeout.cancel(lastLoop);
				lastLoop = timeout(function(){
					if(rowItem.entity.id){
						log.info('Task row changed');
						scope.tasks.selectedTaskBatch = rowItem.entity;
					}
				}, 50)
				var nextState = getNextState();
				if (nextState != null) {
					state.go(getNextState(), { 'taskBatchId' : rowItem.entity.id});
				}
			}
		}
	};

	var validNextStates = [
		"**." + scope.tasks.statePrefix + ".detail.logs.**",
		"**." + scope.tasks.statePrefix + ".detail.actions.**",
		"**." + scope.tasks.statePrefix + ".detail.tasks.**"
	];

	var getNextState = function() {
		var next = null;
		for (var i=0; i < validNextStates.length; i++) {
			if (state.includes(validNextStates[i])) {
				next = state.$current.name;
				break;
			}
		}
		if (next == null && state.includes("**." + scope.tasks.statePrefix + ".**")) {
			next = scope.tasks.statePrefix + ".detail"
		}
		return next;
	}

	// Tasks Grid Actions
	scope.tasks.tasksGridActions = function(item, action, event){
		log.info(action);
		if(action == 'reset'){
			scope.tasks.resetTaskBatch(item.entity.id);
		}else if(action == 'remove'){
			scope.tasks.deleteTaskBatch(item.entity.id);
		}else if(action == 'publishUnpublish'){
			scope.tasks.publishUnpublishTaskBatch(item.entity, event);
		}
	};

	// Delete tasks batch function
	scope.tasks.deleteTaskBatch = function(id){
		var confirmation = confirm("You are about to delete the tasks generated by this batch.\n\n"+
			"Press OK to delete the tasks otherwise press Cancel.");

		if (confirmation) {
			cookbookService.deleteTaskBatch({section: id}, function(data){
				alerts.addAlert({type: 'success', msg: 'Finish deleting tasks', closeIn: 3000});
				log.info('Success on deleting task');
				log.info(data);
				scope.tasks.refreshTaskBatches();
			}, function(){
				log.info('Error on deleting task');
			});
		}
	}

	scope.tasks.refreshTaskBatches = function() {
		scope.tasks.getListTaskBatches({recipeId: stateParams.recipeId, limitDays: scope.tasks.limitDays});
	}

	// Publish && Unpublish tasks batch functions
	scope.tasks.publishUnpublishTaskBatch = function(obj, event){
		var taskBatchStatus = "";
		if (obj.isPublished) {
			taskBatchStatus = "unpublish";
		} else {
			taskBatchStatus = "publish";
		}

		var confirmation = confirm("You are about to " + taskBatchStatus + " the generated tasks.\n\n"+
		"Press OK to " + taskBatchStatus + " the tasks otherwise press Cancel.");

		if (confirmation) {
			if(obj.isPublished){
				cookbookService.unpublishTaskBatch({section: obj.id}, function(data){
					alerts.addAlert({type: 'success', msg: 'Finish ' + taskBatchStatus + 'ing tasks', closeIn: 3000});
					log.info('Success on publishing task');
					log.info(data);
					scope.tasks.refreshTaskBatches();
				}, function(){
					log.info('Error on publishing task');
					scope.tasks.refreshTaskBatches();
				});
			}else{
				cookbookService.publishTaskBatch({section: obj.id}, function(data){
					alerts.addAlert({type: 'success', msg: 'Finish ' + taskBatchStatus + 'ing tasks', closeIn: 3000});
					log.info('Success on unpublishing task');
					log.info(data);
					scope.tasks.refreshTaskBatches();
				}, function(){
					log.info('Error on unpublishing task');
					scope.tasks.refreshTaskBatches();
				});
			}
		} else {
			event.preventDefault();
		}
	}

	// Reset tasks batch function
	scope.tasks.resetTaskBatch = function(taskBatchId){
		var confirmation = confirm("You are about to reset the generated tasks that clears out the comments and resets the task statuses.\n\n"+
				"Press OK to continue otherwise press Cancel.");

		if (confirmation) {
			cookbookService.resetTaskBatch({section: taskBatchId}, function(data){
				alerts.addAlert({type: 'success', msg: 'Finish resetting tasks', closeIn: 3000});
				log.info('Success on resetting task');
				log.info(data);
				scope.tasks.refreshTaskBatches();
			}, function(){
				log.info('Error on resetting task');
				scope.tasks.refreshTaskBatches();
			});
		}
	}

	// This should to be fired whenever the usr click on "Task Generation" tab.
	// It selects the first row if there isn't any selected
	scope.tasks.updateGrid = function(){
		log.log(scope.tasks.tasksGrid);
		if(scope.tasks.tasksGrid.selectedItems.length == 0){
			layoutPluginTasks.updateGridLayout();
			scope.tasks.tasksGrid.selectRow(0, true)
		}
	};

	// Get List of Task batches for a given Recipe
	scope.tasks.getListTaskBatches = function(params){
		scope.enabledGridSelection = false;
		params.rand = tdsCommon.randomString(16);
		cookbookService.getListTaskBatches(params, function(data){
			log.info('Success on getting Task Batches');
			log.info(data);

			scope.tasks.gridData = (data.data.list.length > 0) ?
				data.data.list : [{'message': 'No results found', 'context': 'none'}];

			scope.tasks.colDef = (data.data.list.length > 0) ? [
				// {field:'id', displayName:'Target', enableCellEdit: false, width: '**'},
				{field:'eventName', displayName:'Event', enableCellEdit: false, width: '***'},
				{field:'tagNames', displayName:'Tags', enableCellEdit: false, width: '***'},
				{field:'taskCount', displayName:'Tasks', cellClass: 'text-center',
					enableCellEdit: false, width: '**'},
				{field:'exceptionCount', displayName:'Exceptions', cellClass: 'text-center',
					enableCellEdit: false, width: '**'},
				{field:'createdBy', displayName:'Generated By', enableCellEdit: false, width: '****'},
				{field:'dateCreated', displayName:'Generated At', enableCellEdit: false, width: '****', cellTemplate : scope.dateCellTemplate},
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

			if ((data.data.list.length > 0) && (!scope.tasks.singleRecipe)) {
				scope.tasks.colDef.splice(0, 0,
					{field:'recipeName', displayName:'Recipe', enableCellEdit: false, width: '***', cellTemplate: recipeTemplate}
				);
			}

			if(data.data.list.length == 0){
				scope.tasks.selectedTaskBatch = null;
			}

			timeout(function(){
				scope.enabledGridSelection = true;
				checkForSelectedRow();
			}, 200)

		}, function(){
			log.info('Error on getting Task Batches');
		});
	}

	var checkForSelectedRow = function() {
		var row = -1;
		if (state.params.taskBatchId) {
			var taskBatchId = state.params.taskBatchId;
			row = (scope.tasks.gridData.length > 0)?0:-1;
			for (var i = 0; i < scope.tasks.gridData.length; i++) {
				if (scope.tasks.gridData[i].id == taskBatchId) {
					row = i;
					break;
				}
			}
		} else if (scope.tasks.gridData.length > 0) {
			row = 0;
		}
		if (row >= 0) {
			scope.tasks.tasksGrid.selectRow(row, true);
			var grid = scope.tasks.tasksGrid.ngGrid;
			grid.$viewport.scrollTop(grid.rowMap[row] * grid.config.rowHeight);
		}
	}

	scope.tasks.refreshTaskBatches();
}

tds.cookbook.controller.TaskBatchHistoryController.$inject = ['$scope', '$state', '$stateParams', '$log', '$timeout', 'utils', 'cookbookService', 'alerts'];


/********************************************************************************
 * Task Batch History Detail controller
 */
tds.cookbook.controller.TaskBatchHistoryDetailController = function(scope, state, stateParams, utils, cookbookService) {

	scope.recipeId = stateParams.recipeId;
	scope.isTaskBatchSelected = (stateParams.taskBatchId != null && stateParams.taskBatchId != "");

	scope.taskBatchTabs = [
		{ heading: "Actions", route: scope.tasks.statePrefix + ".detail.actions", active:false },
		{ heading: "Tasks", route: scope.tasks.statePrefix + ".detail.tasks", active:false },
		{ heading: "Generation Log", route: scope.tasks.statePrefix + ".detail.logs", active:false }
	];

	scope.active = function(route){
		return state.includes("**." + route + ".**");
	};

	var checkActiveTabs = function() {
		scope.taskBatchTabs.forEach(function(tab) {
			tab.active = scope.active(tab.route);
		});
	};

	scope.$on("$stateChangeSuccess", function() {
		checkActiveTabs();
	});

	scope.go = function(route){
		if (!scope.active(route)) {
			state.go(route);
		}
	};

}

tds.cookbook.controller.TaskBatchHistoryDetailController.$inject = ['$scope', '$state', '$stateParams', 'utils', 'cookbookService'];


/********************************************************************************
 * Task Batch History Actions controller
 */
tds.cookbook.controller.TaskBatchHistoryActionsController = function(scope, state, stateParams, log, utils, cookbookService, alerts) {

}

tds.cookbook.controller.TaskBatchHistoryActionsController.$inject = ['$scope', '$state', '$stateParams', '$log', 'utils', 'cookbookService', 'alerts'];


/********************************************************************************
 * Task Batch History Tasks controller
 */
tds.cookbook.controller.TaskBatchHistoryTasksController = function(scope, state, stateParams, log, utils, cookbookService, alerts) {

	var layoutPluginTasks = new ngGridLayoutPlugin();

	var taskBatchId = stateParams.taskBatchId;

	scope.assetComments = {};
	scope.assetComments.noGridData = [{'message': 'No results found', 'context': 'none'}];
	scope.assetComments.gridData = scope.assetComments.noGridData;

	scope.assetComments.noColDef = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
	scope.assetComments.withDataColDef = [
		{field:'id', displayName:'Task #', enableCellEdit: false},
		{field:'description', displayName:'Description', enableCellEdit: false},
		{field:'asset', displayName:'Asset', enableCellEdit: false},
		{field:'team', displayName:'Team', enableCellEdit: false},
		{field:'person', displayName:'Person', enableCellEdit: false},
		{field:'dueDate', displayName:'Due date', enableCellEdit: false},
		{field:'status', displayName:'Status', enableCellEdit: false}
	];

	scope.assetComments.colDef = scope.assetComments.noColDef;

	scope.assetComments.tasksGrid = {
		data: 'assetComments.gridData',
		multiSelect: false,
		columnDefs: 'assetComments.colDef',
		selectedItems: [],
		plugins: [layoutPluginTasks],
		enableCellEditOnFocus: false,
		enableHighlighting: true,
		afterSelectionChange: function(rowItem) {
			if(rowItem.selected){
				log.info(rowItem.commentId);
				//$rootScope.$broadcast("viewComment", commentUtils.commentTO(rowItem.entity.commentId, 'issue'), 'show');
				showAssetComment(rowItem.entity.commentId, 'show');
			}
		}
	};

	if ((taskBatchId != null) && (taskBatchId != "")) {
		cookbookService.getTasksOfTaskBatch({section: taskBatchId, rand: tdsCommon.randomString(16)}, function(data){
			log.info('Success on reading tasks of task batch');
			log.info(data);
			scope.assetComments.colDef = scope.assetComments.withDataColDef;
			scope.assetComments.gridData = data.data.tasks;
		}, function(){
			alerts.addAlert({type: 'danger', msg: 'Encountered error while getting tasks for task batch ' + taskBatchId, closeIn: 3000});
			log.info('Error on reading tasks of task batch');
			scope.assetComments.colDef = scope.assetComments.noColDef;
			scope.assetComments.gridData = scope.assetComments.noGridData;
		});
	} else {
		scope.assetComments.colDef = scope.assetComments.noColDef;
		scope.assetComments.gridData = scope.assetComments.noGridData;
	}

}

tds.cookbook.controller.TaskBatchHistoryTasksController.$inject = ['$scope', '$state', '$stateParams', '$log', 'utils', 'cookbookService', 'alerts'];


/********************************************************************************
 * Task Batch History Logs controller
 */
tds.cookbook.controller.TaskBatchHistoryLogsController = function(scope, state, stateParams, sce, utils, cookbookService) {

	scope.logRadioModel = 'exceptionLog';

	scope.secureHTML = function(param){
		return sce.trustAsHtml(param);
	}

	scope.showLog = function(type) {
		if (scope.tasks.currentSelectedTaskRow) {
			scope.taskBatchLogs = scope.secureHTML(scope.tasks.currentSelectedTaskRow.entity[type]);
		}
	}

	scope.showLog('exceptionLog');
}

tds.cookbook.controller.TaskBatchHistoryLogsController.$inject = ['$scope', '$state', '$stateParams', '$sce', 'utils', 'cookbookService'];


/********************************************************************************
 * Recipe Editor controller
 */
tds.cookbook.controller.RecipeEditorController = function(scope, rootScope, state, stateParams, log, modal, timeout, utils, cookbookService, recipeManager, alerts) {

	scope.recipeId = stateParams.recipeId;

	scope.editorTabs = [
		{ heading: "Change Logs", route:"recipes.detail.code.logs", active:false },
		{ heading: "Groups", route:"recipes.detail.code.groups", active:false },
		{ heading: "Syntax Errors", route:"recipes.detail.code.errors", active:false }
	];

	scope.active = function(route){
		return state.includes("**." + route + ".**");
	};

	var checkActiveTabs = function() {
		scope.editorTabs.forEach(function(tab) {
			tab.active = scope.active(tab.route);
		});
	};

	scope.$on("$stateChangeSuccess", function() {
		checkActiveTabs();
	});

	scope.go = function(route){
		if (!scope.active(route)) {
			state.go(route);
		}
	};

	scope.showEditPopup = function() {
		scope.editor.originalRecipeType = scope.editor.recipeType
		var modalInstance = modal.open({
			templateUrl: utils.url.applyRootPath('/components/cookbook/editor/recipe-code-editor-popup-template.html'),
			controller: tds.cookbook.controller.RecipeCodeEditController,
			backdrop : 'static',
			scope: scope,
			windowClass: 'code-editor-modal',
			resolve: {
				sourceCode: function () {
					return scope.editor.selectedRecipe.sourceCode;
				}
			}
		});
		modalInstance.result.then(function (sourceCode) {
			log.log('storing locally edition');
			scope.editor.selectedRWip.sourceCode = sourceCode;
			scope.editor.selectedRWip.changelog = scope.editor.selectedRecipe.changelog;
			recipeManager.setEditingRecipe(true);
			scope.editor.editingRecipe = true;
			scope.editor.selectedRecipe = scope.editor.selectedRWip;
			if (scope.editor.recipeType != 'wip') {
				scope.switchWipRelease('wip');
			}
		});
	};

	scope.saveWIP = function() {
		var proceedToSave = true
		if(scope.editor.originalRecipeType && scope.editor.originalRecipeType == 'release' && scope.editor.selectedRecipe.hasWIP){
			proceedToSave = confirm("There is already a WIP of this recipe. Press Okay to overwrite the existing WIP with this version of the recipe or Cancel to abort.")
		}
		if(proceedToSave){
			var tmpObj = angular.copy(scope.editor.selectedRWip);
			var selectedId = stateParams.recipeId;
			var selectedVersion = scope.editor.selectedRWip.versionNumber;
			dataToSend = $.param(tmpObj)
			cookbookService.saveWIP({details:selectedId}, dataToSend, function(){
				log.info('Success on Saving WIP');
				alerts.addAlert({type: 'success', msg: 'WIP Saved', closeIn: 1500});
				scope.getRecipeData('wip');
				rootScope.$broadcast("refreshRecipes");
			}, function(){
				log.warn('Error on Saving WIP');
				alerts.addAlert({type: 'danger', msg: 'Error: Unable to save WIP'});
			});
		}

	};

	// Release
	scope.releaseVersion = function() {
		var dataToSend = $.param(scope.editor.selectedRecipe);
		var selectedId = stateParams.recipeId;
		if (scope.editor.editingRecipe) {
			alert("You have unsaved changes. Please click Save WIP or Undo before proceeding.");
		} else {
			var confirmation = confirm("You are about to create a release of the recipe that will be a permanent version of the source.\n\nPress OK to continue otherwise press Cancel.");
			if (confirmation == true){
				cookbookService.release({moreDetails:selectedId}, dataToSend, function(data) {
					log.info('Success on Releasing');
					alerts.addAlert({type: 'success', msg: 'Version Released', closeIn: 1500});
					scope.editor.justReleased = true;
					scope.getRecipeData('release');
					rootScope.$broadcast("refreshRecipes");
				}, function(){
					log.warn('Error on Saving WIP');
					if(scope.editor.selectedRecipe.hasWIP){
						alerts.addAlert({type: 'danger', msg: 'Unable to release version'});
					}else{
						alerts.addAlert({type: 'danger', msg: 'You can only release recipes'+
							' saved as WIP'});
					}
				});
			}
		}
	};

	// Cancel
	scope.cancelChanges = function() {
		var confirmation = confirm("You are about to undo local changes of the recipe.\n\nPress OK to undo changes otherwise press Cancel.");
		if (confirmation == true) {
			scope.getRecipeData('wip');
			return true;
		}else{
			return false;
		}
	};

	// Discard WIP
	scope.discardWIP = function() {
		var confirmation = confirm("You are about to permanently discard the WIP version of the recipe.\n\nPress OK discard WIP otherwise press Cancel.");
		if (confirmation == true){
			var dataToSend = $.param(scope.editor.selectedRecipe),
			selectedId = stateParams.recipeId,
			selectedVersion = scope.editor.selectedRecipe.versionNumber;
			cookbookService.discardWIP({details:selectedId, moreDetails:selectedVersion},
				dataToSend, function(){
				log.info('Success on Discarding WIP');
				alerts.addAlert({type: 'success', msg: 'WIP Discarded', closeIn: 1500});
				scope.getRecipeData('wip');
				rootScope.$broadcast("refreshRecipes");
			}, function(){
				log.warn('Error on Discarding WIP');
				alerts.addAlert({type: 'danger', msg: 'Error: Unable to discard WIP'});
			});
		}
	};

	// Validate Syntax
	scope.validateSyntax = function() {
		var dataToSend = $.param({'sourceCode': scope.editor.selectedRWip.sourceCode});
		cookbookService.validate({}, dataToSend, function(data){
			scope.currentSyntaxValidation = data.warnings || [{"error":0,"reason":"No errors found"}];
			state.go('recipes.detail.code.errors')
		}, function(data){
			log.warn('Error on validation');
			alerts.addAlert({type: 'danger', msg: 'Unable to validate Syntax'});
		});
	};

	scope.diff = function() {
		scope.showCompareCodeDialog(
			(scope.editor.selectedRVersion.hasWIP?recipeManager.wipBackup().sourceCode:recipeManager.versionBackup().sourceCode),
			scope.editor.selectedRWip.sourceCode,
			(scope.editor.selectedRVersion.hasWIP?"WIP":("Version " + scope.editor.selectedRVersion.versionNumber)),
			"Local WIP"
		);
	};

	scope.showCompareCodeDialog = function(leftSourceCode, rightSourceCode, leftLabel, rightLabel) {
		scope.toCompare = {
			"leftSourceCode":leftSourceCode,
			"rightSourceCode":rightSourceCode,
			"leftLabel":leftLabel,
			"rightLabel":rightLabel
		};
		var dialogInstance = modal.open({
			templateUrl: utils.url.applyRootPath('/components/cookbook/sourceCodeDiffDialog.html'),
			controller: tds.cookbook.controller.SourceCodeDiffController,
			scope: scope,
			windowClass: 'code-diff-modal',
			resolve: {
				leftSourceCode: function () {
					return scope.toCompare.leftSourceCode;
				},
				rightSourceCode: function () {
					return scope.toCompare.rightSourceCode;
				},
				leftLabel: function () {
					return scope.toCompare.leftLabel;
				},
				rightLabel: function () {
					return scope.toCompare.rightLabel;
				}
			  }
		});

		dialogInstance.opened.then(function (modalReady) {
				scope.$broadcast("sourceCodeDiffModalLoaded");
			}
		);
	}

	scope.wipConfig = [];

	var codeViewLoaded = function(codeView) {
		codeView.setSize("100%", "250px");
		codeView.on("dblclick", scope.showEditPopup);
	};

	scope.codeViewOptions = {
		lineNumbers: true,
		indentWithTabs: true,
		indentUnit: 4,
		lineWrapping : true,
		readOnly: true,
		onLoad : codeViewLoaded,
		extraKeys: {"Ctrl-Space": "autocomplete"}
	};

}

tds.cookbook.controller.RecipeEditorController.$inject = ['$scope', '$rootScope', '$state', '$stateParams', '$log', '$modal', '$timeout', 'utils', 'cookbookService', 'recipeManager', 'alerts'];


/********************************************************************************
 * Recipe Editor Logs controller
 */
tds.cookbook.controller.RecipeEditorLogsController = function(scope, state, stateParams, utils, cookbookService, recipeManager) {

	scope.recipeId = stateParams.recipeId;

	scope.onEditChangeLog = function() {
		scope.editor.editingRecipe = true;
		recipeManager.setEditingRecipe(true);
	};

}

tds.cookbook.controller.RecipeEditorLogsController.$inject = ['$scope', '$state', '$stateParams', 'utils', 'cookbookService', 'recipeManager'];


/********************************************************************************
 * Recipe Editor Groups controller
 */
tds.cookbook.controller.RecipeEditorGroupsController = function(scope, state, $http, stateParams, log, timeout, utils, cookbookService) {

	scope.recipeId = stateParams.recipeId;

	var lastLoop;
	var isGroup = true;
	var layoutPluginGroups = new ngGridLayoutPlugin();

	scope.groups = {
		gridData: [],
		colDef: [],
		groupsArray: [],
		assetsArray: [],
		selectedGroup: ''
	}

	var countTemplate = '<div class="gridIcon">'+
		'<span class="actions" style="text-align: center;">'+
			'<span ng-bind="row.entity.assets.length"></span>'+
		'</span>'+
		'</div>';

	scope.groups.fetchGroups = function(){
		scope.enabledGridSelection = false;
		var recVerId = scope.editor.selectedRecipe.recipeVersionId;
		var source = null
		if (scope.editor.editingRecipe && (scope.editor.recipeType == 'wip')) {
			recVerId = null;
		}
		if (recVerId == null) {
			source = scope.editor.selectedRWip.sourceCode;
		}

		// Partially Remove Prototype
		if(window.Prototype) {
			delete Object.prototype.toJSON;
			delete Array.prototype.toJSON;
			delete Hash.prototype.toJSON;
			delete String.prototype.toJSON;
		}

		var getTagsIds = function(tags) {
			var tagIds = [];
			tags.each( function(a){
				tagIds.push(parseInt(a.id));
			})
			return tagIds;
		};

		var postData = {
			recipeVersionId: recVerId,
			context: {
				eventId: (scope.contexts.selectedEvent)? scope.contexts.selectedEvent.id: null,
				tag: getTagsIds(scope.contexts.assetSelector.tag)
			},
			sourceCode: source
		};

		$http.post(utils.url.applyRootPath('/ws/cookbook/groups?rand='), JSON.stringify(postData), {headers: {'Content-Type': 'application/json'}}).then(function successCallback(response) {
			log.info('Success on getting Groups');
			if (response && response.data) {
				scope.groups.groupsArray = response.data.data.groups;
				scope.groups.updateGrid();
				loadAssets([]);
				timeout(function(){
					scope.enabledGridSelection = true;
				}, 200)
			}
		}, function(){
			log.warn('Error on getting Groups');
			scope.groups.groupsArray = [{'message': 'Unexpected error', 'context': 'none'}];
			scope.groups.colDef = [{field:'message', displayName:'Message', enableCellEdit: false, width: '100%'}];
			timeout(function(){
				scope.enabledGridSelection = true;
			}, 200)
		});
	}

	scope.groups.groupsGrid = {
		data: 'groups.groupsArray',
		multiSelect: false,
		columnDefs: 'groups.colDef',
		enableCellEditOnFocus: false,
		enableHighlighting: true,
		selectedItems: [],
		plugins: [layoutPluginGroups],
		afterSelectionChange: function(rowItem){
			if(rowItem != scope.groups.selectedGroup){
				// This hack is to avoid changeRecipe() to be executed many times.
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				timeout.cancel(lastLoop);
				lastLoop = timeout(function(){
					if(rowItem.entity.name){
						log.info('Group changed');
						scope.groups.selectedGroup = rowItem.entity;
						log.info(scope.groups.selectedGroup);
						scope.enabledGridSelection = false;
						loadAssets(rowItem.entity.assets);
					}
				}, 50)

			}
		}
	};

	// This should to be fired whenever the usr click on "Groups" tab.
	// It selects the first row if there isn't any selected
	scope.groups.updateGrid = function(){
		log.log(scope.groups.groupsGrid);
		scope.groups.colDef = [
			/*{field:'class', displayName:'Class', enableCellEdit: false},*/
			{field:'name', displayName:'Name', enableCellEdit: false},
			{field:'count', displayName:'Count', cellTemplate: countTemplate, enableCellEdit: false}
		];
	};

	scope.assets = {
		colDef : [],
		gridData : [],
		showAssetsGrid: false,
		selectedAsset: ''
	};

	scope.assets.assetsGrid = {
		data: 'assets.gridData',
		multiSelect: false,
		columnDefs: 'assets.colDef',
		enableCellEditOnFocus: false,
		enableHighlighting: true,
		selectedItems: [],
		afterSelectionChange: function(rowItem){
			if(rowItem != scope.assets.selectedAsset){
				// This hack is to avoid changeRecipe() to be executed many times.
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				timeout.cancel(lastLoop);
				lastLoop = timeout(function(){
					if(rowItem.entity.name){
						log.info('Asset changed');
						scope.assets.selectedAsset = rowItem.entity;
						log.info(scope.assets.selectedAsset);
					}
				}, 50)
			}
		}
	};

	var loadAssets = function(data){
		scope.groups.showAssetsGrid = true;
		scope.assets.gridData = data;
		scope.assets.colDef = [
			{field:'name', displayName:'Name', enableCellEdit: false},
			{field:'assetType', displayName:'Asset Type', enableCellEdit: false}
		];

		timeout(function(){
			scope.enabledGridSelection = true;
		}, 200)
	};

}

tds.cookbook.controller.RecipeEditorGroupsController.$inject = ['$scope', '$state', '$http', '$stateParams', '$log', '$timeout', 'utils', 'cookbookService'];


/********************************************************************************
 * Recipe Editor Errors controller
 */
tds.cookbook.controller.RecipeEditorErrorsController = function(scope, state, stateParams, sce, utils, cookbookService) {

	scope.secureHTML = function(param){
		return sce.trustAsHtml(param);
	}

}

tds.cookbook.controller.RecipeEditorErrorsController.$inject = ['$scope', '$state', '$stateParams', '$sce', 'utils', 'cookbookService'];


/********************************************************************************
 * Recipe Versions controller
 */
tds.cookbook.controller.RecipeVersionsController = function(scope, rootScope, state, stateParams, log, timeout, modal, utils, cookbookService, recipeManager, alerts, recipeVersionsData) {

	var layoutPluginVersions = new ngGridLayoutPlugin();

	scope.versions = {
		versionsArray : recipeVersionsData,
		selectedVersion : '',
		selectedVersionRow : '',
		currentSelectedTaskRow : '',
		toCompareVersions : [],
		toCompareVersion : null
	}

	scope.versions.gridData = [];

	var	versionsActionsTemplate = '<div class="gridIcon">'+
		'<a href="" ng-hide="row.entity.isCurrentVersion || !row.entity.versionNumber"'+
			'class="actions edit" title="Revert Recipe Version"'+
			'ng-click="versions.versionsGridActions(row, \'revert\')">'+
			'<img src="'+ utils.url.applyRootPath('/icons/arrow_undo.png') + '" alt="Revert">' +
		'</a>'+
		'<a href="" class="actions remove" title="Delete Version"'+
			'ng-click="versions.versionsGridActions(row, \'remove\')">'+
			'<img src="'+ utils.url.applyRootPath('/icons/delete.png') + '" alt="Delete">' +
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


	scope.versions.colDef = [
		{field:'versionNumber', displayName:'Version', cellClass: 'text-right', enableCellEdit: false,
			cellTemplate: versionNumberTemplate, width: '**'},
		{field:'', displayName:'Current', enableCellEdit: false, sortable: false,
			cellTemplate: currentVersionTemplate, width: '**'},
		{field:'lastUpdated', displayName:'Last Updated', enableCellEdit: false, width: '***', cellTemplate : scope.dateCellTemplate},
		{field:'createdBy', displayName:'Created By', enableCellEdit: false, width: '***'},
		{field:'', displayName:'Actions', cellClass: 'text-center', enableCellEdit: false,
			sortable: false, cellTemplate: versionsActionsTemplate, width: '**'}
	];

	// Versions Grid Actions
	scope.versions.versionsGridActions = function(item, action){
		log.info(action);
		var version = (item.entity.versionNumber) ? 'version '+item.entity.versionNumber : 'WIP version';
		if(action == 'revert'){
			console.log(item.entity);
			var confirmation = confirm("You are about to revert the current recipe '" + scope.currentSelectedRecipe.name + "' to version " +
				version + ".\n\nPress OK to continue otherwise press Cancel.");
			if (confirmation == true){
				log.info(item.entity);
				cookbookService.revert({moreDetails:item.entity.id}, function(){
					log.info('Success on Reverting');
					alerts.addAlert({type: 'success', msg: 'Reverted to version '+
						item.entity.versionNumber, closeIn: 1500});
					rootScope.$broadcast("refreshRecipes");
					scope.versions.updateGrid();
				}, function(){
					log.warn('Error on Reverting');
					alerts.addAlert({type: 'danger', msg: 'Error: Unable to revert version'});
				});
			}
		}else if(action == 'remove'){
			console.log(scope.editor.selectedRecipe);
			var confirmation = confirm("You are about to delete version " + version + " of recipe '" +
				scope.currentSelectedRecipe.name + "'.\n\nPress OK to continue otherwise press Cancel.");
			if (confirmation == true){
				log.info(item.entity);
				var versionToDelete = item.entity.versionNumber?item.entity.versionNumber:0;
				cookbookService.discardWIP({details:scope.editor.selectedRecipe.recipeId,
					moreDetails: versionToDelete}, function(data){
					if (data.status == 'fail') {
						alerts.addAlert({type: 'danger', msg: data.data});
					} else {
						log.info('Success on removing Recipe Version');
						alerts.addAlert({type: 'success', msg: 'Recipe Version Removed', closeIn: 1500});
						rootScope.$broadcast("refreshRecipes");
						scope.versions.updateGrid();
					}
				}, function(){
					log.warn('Error on removing Recipe Version');
					alerts.addAlert({type: 'danger', msg: 'Error: Unable to remove version'});
				})
			}
		}
	};

	scope.versions.getRecipeVersionDataAndCompare = function(versionNumber){
		if (scope.versions.selectedVersion) {
			if (versionNumber == "WIP") {
				scope.versions.compareVersions(scope.versions.selectedVersion, recipeManager.wip());
			} else {
				cookbookService.getARecipeVersion({details:scope.currentSelectedRecipe.recipeId, moreDetails: versionNumber, rand: tdsCommon.randomString(16)},
					function(data){
						scope.versions.compareVersions(scope.versions.selectedVersion, data.data);
					}, function(){
						log.info('Error on getting version');
					}
				);
			}
		}
	}

	scope.versions.compareVersions = function(recipeVersion1, recipeVersion2){
		scope.showCompareCodeDialog(
			recipeVersion1.sourceCode,
			recipeVersion2.sourceCode,
			(angular.isNumber(recipeVersion1.versionNumber) && (recipeVersion1.versionNumber > 0))?("Version " + recipeVersion1.versionNumber):"WIP",
			(angular.isNumber(recipeVersion2.versionNumber) && (recipeVersion2.versionNumber > 0))?("Version " + recipeVersion2.versionNumber):"WIP"
		);
	}

	scope.showCompareCodeDialog = function(leftSourceCode, rightSourceCode, leftLabel, rightLabel) {
		scope.toCompare = {
			"leftSourceCode":leftSourceCode,
			"rightSourceCode":rightSourceCode,
			"leftLabel":leftLabel,
			"rightLabel":rightLabel
		};
		var dialogInstance = modal.open({
			templateUrl: utils.url.applyRootPath('/components/cookbook/sourceCodeDiffDialog.html'),
			controller: tds.cookbook.controller.SourceCodeDiffController,
			scope: scope,
			windowClass: 'code-diff-modal',
			resolve: {
				leftSourceCode: function () {
					return scope.toCompare.leftSourceCode;
				},
				rightSourceCode: function () {
					return scope.toCompare.rightSourceCode;
				},
				leftLabel: function () {
					return scope.toCompare.leftLabel;
				},
				rightLabel: function () {
					return scope.toCompare.rightLabel;
				}
			  }
		});

		dialogInstance.opened.then(function (modalReady) {
				scope.$broadcast("sourceCodeDiffModalLoaded");
			}
		);
	};

	scope.versions.onCompareVersions = function() {
		scope.versions.getRecipeVersionDataAndCompare(scope.versions.toCompareVersion);
	};

	var lastLoop;
	scope.versions.versionsGrid = {
		data: 'versions.versionsArray',
		multiSelect: false,
		columnDefs: 'versions.colDef',
		enableCellEditOnFocus: false,
		enableHighlighting: true,
		selectedItems: [],
		plugins: [layoutPluginVersions],
		afterSelectionChange: function(rowItem){
			if(rowItem != scope.versions.selectedVersionRow){
				scope.versions.selectedVersionRow = rowItem;
				// This hack is to avoid the code to be executed many times.
				// This is a known issue on the ng-grid for the afterSelectionChange event.
				timeout.cancel(lastLoop);
				lastLoop = timeout(function(){
					if(rowItem.entity.id){
						log.info('Version Row changed');
						log.info(rowItem.entity);
						var versionNumber = (rowItem.entity.versionNumber) ? rowItem.entity.versionNumber : '0';
						state.go(getNextState(), {'recipeVersion': versionNumber.toString()});
					}
				}, 50)

			}
		}
	};

	var validNextStates = [
		"**.recipes.detail.versions.detail.logs.**",
		"**.recipes.detail.versions.detail.code.**",
		"**.recipes.detail.versions.detail.diff.**"
	];

	var getNextState = function() {
		var next = null;
		for (var i=0; i < validNextStates.length; i++) {
			if (state.includes(validNextStates[i])) {
				next = state.$current.name;
				break;
			}
		}
		if (next == null) {
			next = "recipes.detail.versions.detail"
		}
		return next;
	}

	scope.versions.getVersions = function(obj){
		scope.versions.selectedVersionRow = {};
		scope.versions.versionsGrid.selectedItems = [];
		cookbookService.getVersions({moreDetails: obj.recipeId, rand: tdsCommon.randomString(16)}, function(data){
			log.info('Success on getting versions');
			log.info(data.data.recipeVersions);
			scope.versions.versionsArray = data.data.recipeVersions;
			updateSelectedVersion();
		}, function(){
			log.info('Error on getting versions');
		});
	}

	// This should to be fired whenever the usr click on "Versions" tab.
	// It selects the first row if there isn't any selected
	scope.versions.updateGrid = function() {
		log.info('versions grid select, recipeId: ' + stateParams.recipeId);
		scope.versions.getVersions({recipeId: stateParams.recipeId});
	};

	var updateSelectedVersion = function() {
		timeout(function() {
			if (scope.versions.versionsArray.length > 0) {
				scope.versions.versionsGrid.selectRow(0, true);
			}
		}, 100)
	}

	updateSelectedVersion();
}

tds.cookbook.controller.RecipeVersionsController.$inject = ['$scope', '$rootScope', '$state', '$stateParams', '$log', '$timeout', '$modal', 'utils', 'cookbookService', 'recipeManager', 'alerts', 'recipeVersionsData'];


/********************************************************************************
 * Recipe Versions Detail controller
 */
tds.cookbook.controller.RecipeVersionsDetailController = function(scope, state, stateParams, log, utils, cookbookService, recipeVersionData) {

	scope.versions.selectedVersion = recipeVersionData;

	scope.versionsInnerTabs = [
		{ heading: "Change Log", route:"recipes.detail.versions.detail.logs", active:false },
		{ heading: "Source Code", route:"recipes.detail.versions.detail.code", active:false },
		{ heading: "Diff", route:"recipes.detail.versions.detail.diff", active:false },
	];

	scope.active = function(route){
		return state.includes("**." + route + ".**");
	};

	var checkActiveTabs = function() {
		scope.versionsInnerTabs.forEach(function(tab) {
			tab.active = scope.active(tab.route);
		});
	};

	scope.$on("$stateChangeSuccess", function() {
		checkActiveTabs();
	});

	scope.go = function(route){
		if (!scope.active(route)) {
			state.go(route);
		}
	};

}

tds.cookbook.controller.RecipeVersionsDetailController.$inject = ['$scope', '$state', '$stateParams', '$log', 'utils', 'cookbookService', 'recipeVersionData'];


/********************************************************************************
 * Recipe Versions Code controller
 */
tds.cookbook.controller.RecipeVersionsCodeController = function(scope, state, stateParams, utils, cookbookService) {

}

tds.cookbook.controller.RecipeVersionsCodeController.$inject = ['$scope', '$state', '$stateParams', 'utils', 'cookbookService'];


/********************************************************************************
 * Recipe Versions Change Logs controller
 */
tds.cookbook.controller.RecipeVersionsLogsController = function(scope, state, stateParams, utils, cookbookService) {

}

tds.cookbook.controller.RecipeVersionsLogsController.$inject = ['$scope', '$state', '$stateParams', 'utils', 'cookbookService'];


/********************************************************************************
 * Recipe Versions Diff controller
 */
tds.cookbook.controller.RecipeVersionsDiffController = function(scope, state, stateParams, utils, cookbookService, recipeManager) {

	var updateToCompareVersions = function() {
		var toCompareVersions = [];
		if (scope.versions.versionsArray && scope.versions.selectedVersion) {
			var addWIP = false;
			var selectedVersion = scope.versions.selectedVersion;
			angular.forEach(scope.versions.versionsArray,
				function(value, key){
					if (selectedVersion.versionNumber != value.versionNumber) {
						if (angular.isNumber(value.versionNumber)) {
							toCompareVersions.push(value.versionNumber);
						}
					}
				}
			);
			if (recipeManager.isEditingRecipe() || scope.editor.selectedRecipe.hasWIP) {
				toCompareVersions.splice(0, 0, "WIP");
			}
		}
		scope.versions.toCompareVersions = toCompareVersions;
		scope.versions.toCompareVersion = null;
	}

	updateToCompareVersions();
}

tds.cookbook.controller.RecipeVersionsDiffController.$inject = ['$scope', '$state', '$stateParams', 'utils', 'cookbookService', 'recipeManager'];


/********************************************************************************
 * Recipe Code Editor
 */
tds.cookbook.controller.RecipeCodeEditController = function($scope, $state, $stateParams, $log, $modalInstance, $timeout, utils, cookbookService, sourceCode) {

	$scope.modal = {};
	$scope.modal.sourceCode = (!sourceCode || sourceCode.length == 0) ? ' ' : sourceCode;

	$scope.codeEditorOptions = {
		lineNumbers: true,
		indentWithTabs: true,
		indentUnit: 4,
		lineWrapping : true,
		extraKeys: {"Ctrl-Space": "autocomplete"}
	};

	$scope.storeCode = function() {
		$scope.codeEditorOptions.fullScreen = false;
		$modalInstance.close($scope.modal.sourceCode);
	};

	$scope.cancel = function () {
		$scope.codeEditorOptions.fullScreen = false;
		$modalInstance.dismiss('close');
	};

	$modalInstance.opened.then(function() {
		// Boostrap opened on modalInstance is data fetch ready, not DOM
		setTimeout( function() {
			$scope.toggleFullScreenMode();
		}, 0);
	});

	$scope.toggleFullScreenMode = function(){
		$("div.code-editor-modal .modal-dialog").toggleClass("modal-dialog-fullscreen");
		$("div.code-editor-modal .modal-content").toggleClass("modal-content-fullscreen");
		$("div.code-editor-modal .modal-body").toggleClass("modal-body-fullscreen");
		$("div.code-editor-modal .modal-content  .CodeMirror-wrap").toggleClass("CodeMirror-wrap-fullscreen");
		$scope.codeEditorOptions.fullScreen = true;
		$("[ui-codemirror='codeEditorOptions']").toggleClass("sourceCode-fullscreen");
	};
}


/********************************************************************************
 * This controller implements the behaviour for the source code diff dialog
 */
tds.cookbook.controller.SourceCodeDiffController = function ($scope, $modalInstance, $timeout, leftSourceCode, rightSourceCode, leftLabel, rightLabel) {
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


/********************************************************************************
 * SERVICES
 ********************************************************************************/

/********************************************************************************
 * Factory used to interact with the cookbook/tasks services
 */
tds.cookbook.service.CookbookService = function(utils, http, resource) {

	http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";

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
			getProgress: {
				method: "GET",
				params: {
					domain: "progress"
				}
			},
			getUserPreferences: {
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
			// TODO : JPM 7/2015 : Do not believe getTaskBatches is used
			getTaskBatches: {
				method: "GET",
				params: {
					domain: "task",
					section: "listTaskBatches"
				}
			},
			getTaskBatch: {
				method: "GET",
				params: {
					domain: "task"
				}
			},
			getTasksOfTaskBatch: {
				method: "GET",
				params: {
					domain: "task",
					details: "tasks"
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
			resetTaskBatch: {
				method: "POST",
				params: {
					domain: "task",
					details: "taskReset"
				}
			},
			deleteTaskBatch: {
				method: "DELETE",
				params: {
					domain: "task"
				}
			},
			getGroups: {
				method: "POST",
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
			},
			deleteRecipeContext: {
				method: "DELETE",
				params: {
					domain: "cookbook",
					section: "recipe",
					details: "context"
				}
			}
	};

	restCalls = resource(
		utils.url.applyRootPath('/ws/:domain/:section/:details/:moreDetails'),
		{
			domain: "@domain",
			section: "@section",
			details: "@details",
			moreDetails: "@moreDetails",
			rand: '',
		},
		restMethodDefinitions
	);

	return restCalls;
};

/********************************************************************************
 * Factory used to edit the recipe, maintain active and WIP version
 */
tds.cookbook.service.RecipeManager = function(utils) {

	var selectedRVersion = {}; // Recipe release version data
	var selectedRWip = {}; // Recipe WIP data
	var wipBackup = {};
	var versionBackup = {};
	var editingRecipe = false;

	var getActiveVersion = function() {
		return selectedRVersion;
	}

	var getWIP = function() {
		return selectedRWip;
	}

	var getWipBackup = function() {
		return wipBackup;
	}

	var getVersionBackup = function() {
		return versionBackup;
	}

	var setActiveVersion = function(data) {
		selectedRVersion = data;
		versionBackup = angular.copy(data);
	}

	var setWIP = function(data) {
		selectedRWip = data;
		wipBackup = angular.copy(data);
	}

	var isEditingRecipe = function() {
		return editingRecipe;
	}

	var setEditingRecipe = function(status) {
		editingRecipe = status;
	}

	return {
		wip: getWIP,
		activeVersion: getActiveVersion,
		wipBackup: getWipBackup,
		versionBackup: getVersionBackup,
		setWip: setWIP,
		setActiveVersion: setActiveVersion,
		setEditingRecipe: setEditingRecipe,
		isEditingRecipe: isEditingRecipe
	};

};


/********************************************************************************
 * Cookbook module configuration
 ********************************************************************************/
tds.cookbook.module = angular.module('tdsCookbook', ['ngGrid', 'ngResource', 'ui.bootstrap', 'modNgBlur',
	 'ui.codemirror', 'ui.router', 'tdsComments']);

tds.cookbook.module.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push('servicesInterceptor');
}]);

tds.cookbook.module.config(['$logProvider', function($logProvider) {
   //$logProvider.debugEnabled(false);
}]);

tds.cookbook.module.factory('servicesInterceptor', [tds.core.interceptor.LoggedCheckerInterceptor]);

tds.cookbook.module.factory('cookbookService', ['utils', '$http', '$resource', tds.cookbook.service.CookbookService]);

tds.cookbook.module.factory('recipeManager', ['utils', tds.cookbook.service.RecipeManager]);

tds.cookbook.module.config(function($stateProvider, $urlRouterProvider, servRootPathProvider) {
	$urlRouterProvider.otherwise('/recipes');
	$stateProvider
		// ----------------------------------------------------------------------
		// STATE DESC: List all recipes for current project
		.state('recipes', {
			url: '/recipes?context&archived',
			templateUrl: servRootPathProvider.$get() + '/components/cookbook/recipes-template.html',
			controller: tds.cookbook.controller.RecipesController
		  })
		// ----------------------------------------------------------------------
		// STATE DESC: Show details for selected recipe
		.state('recipes.detail', {
			url: '/{recipeId:[0-9]*}',
			views: {
				"recipeDetail": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/recipe-detail-template.html',
					controller: tds.cookbook.controller.RecipeDetailController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show create recipe popup
		.state('recipes.create', {
			url: '/create',
			onEnter: function($state, $modal) {
				$modal.open({
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/create/create-recipe-template.html',
					controller: tds.cookbook.controller.CreateRecipeController,
					backdrop : 'static'
				}).result.then(function (recipeId) {
					return $state.go("recipes.detail.code", {'recipeId': recipeId});
				}, function () {
					return $state.go("recipes");
				});
			},
			onExit: function($rootScope) {
				$rootScope.$broadcast("refreshRecipes");
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show generate task view
		.state('recipes.detail.gentasks', {
			url: '/gentasks',
			views: {
				"recipeDetailContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/generation/task-generation-template.html',
					controller: tds.cookbook.controller.TaskGenerationController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show generate task view and tash batch progres
		.state('recipes.detail.gentasks.start', {
			url: '/start',
			views: {
				"taskBatchStart": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/generation/task-generation-start-template.html',
					controller: tds.cookbook.controller.TaskGenerationStartController
				},
				"taskBatchProgress": {
					template: ''
				},
				"taskBatchCompleted": {
					template: ''
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show generate task view and tash batch progres
		.state('recipes.detail.gentasks.progress', {
			url: '/:jobId/progress',
			views: {
				"taskBatchStart": {
					template: ''
				},
				"taskBatchProgress": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/generation/task-generation-progress-template.html',
					controller: tds.cookbook.controller.TaskGenerationProgressController
				},
				"taskBatchCompleted": {
					template: ''
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show generate task view and is completed
		.state('recipes.detail.gentasks.completed', {
			url: '/{taskBatchId:[0-9]*}/completed',
			views: {
				"taskBatchStart": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/generation/task-generation-start-template.html'
				},
				"taskBatchProgress": {
					template: ''
				},
				"taskBatchCompleted": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/generation/task-generation-completed-template.html',
					controller: tds.cookbook.controller.TaskGenerationCompletedController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history
		.state('recipes.detail.history', {
			url: '/history',
			views: {
				"recipeDetailContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history details
		.state('recipes.detail.history.detail', {
			url: '/{taskBatchId:[0-9]*}',
			views: {
				"taskBatchInnerContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-detail-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryDetailController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history actions
		.state('recipes.detail.history.detail.actions', {
			url: '/actions',
			views: {
				"taskBatchTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-actions-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryActionsController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history tasks
		.state('recipes.detail.history.detail.tasks', {
			url: '/tasks',
			views: {
				"taskBatchTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-tasks-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryTasksController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history logs
		.state('recipes.detail.history.detail.logs', {
			url: '/logs',
			views: {
				"taskBatchTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-logs-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryLogsController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show code editor
		.state('recipes.detail.code', {
			url: '/code',
			views: {
				"recipeDetailContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/editor/recipe-editor-template.html',
					controller: tds.cookbook.controller.RecipeEditorController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show code editor
		.state('recipes.detail.code.logs', {
			url: '/logs',
			views: {
				"recipeEditorContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/editor/recipe-editor-logs-template.html',
					controller: tds.cookbook.controller.RecipeEditorLogsController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show code editor
		.state('recipes.detail.code.groups', {
			url: '/groups',
			views: {
				"recipeEditorContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/editor/recipe-editor-groups-template.html',
					controller: tds.cookbook.controller.RecipeEditorGroupsController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show code editor
		.state('recipes.detail.code.errors', {
			url: '/errors',
			views: {
				"recipeEditorContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/editor/recipe-editor-errors-template.html',
					controller: tds.cookbook.controller.RecipeEditorErrorsController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show recipes versions
		.state('recipes.detail.versions', {
			url: '/versions',
			views: {
				"recipeDetailContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/versions/recipe-versions-template.html',
					controller: tds.cookbook.controller.RecipeVersionsController
				}
			},
			resolve: {
				"recipeVersionsData": function($q, $stateParams, $log, cookbookService) {
					var deferred = $q.defer();
					cookbookService.getVersions({moreDetails: $stateParams.recipeId, rand: tdsCommon.randomString(16)}, function(data){
						$log.info('Success on getting versions');
						deferred.resolve(data.data.recipeVersions);
					}, function(){
						$log.info('Error on getting versions');
						deferred.reject([]);
					});
					return deferred.promise;
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show recipes versions and change logs
		.state('recipes.detail.versions.detail', {
			url: '/{recipeVersion:[0-9]*}',
			views: {
				"versionsInnerContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/versions/recipe-versions-detail-template.html',
					controller: tds.cookbook.controller.RecipeVersionsDetailController
				}
			},
			resolve: {
				"recipeVersionData": function($q, $stateParams, $log, cookbookService) {
					var deferred = $q.defer();
					cookbookService.getARecipeVersion({details: $stateParams.recipeId, moreDetails: $stateParams.recipeVersion, rand: tdsCommon.randomString(16)},
					function(data){
						$log.info('Success on getting version');
						$log.info(data.data);
						deferred.resolve(data.data);
					}, function(){
						$log.info('Error on getting version');
						deferred.reject(null);
					});
					return deferred.promise;
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show recipes versions and change logs
		.state('recipes.detail.versions.detail.logs', {
			url: '/logs',
			views: {
				"versionsInnerTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/versions/recipe-versions-logs-template.html',
					controller: tds.cookbook.controller.RecipeVersionsLogsController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show recipes versions and source code
		.state('recipes.detail.versions.detail.code', {
			url: '/code',
			views: {
				"versionsInnerTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/versions/recipe-versions-code-template.html',
					controller: tds.cookbook.controller.RecipeVersionsCodeController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show recipes versions and make diff against versions
		.state('recipes.detail.versions.detail.diff', {
			url: '/diff',
			views: {
				"versionsInnerTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/versions/recipe-versions-diff-template.html',
					controller: tds.cookbook.controller.RecipeVersionsDiffController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history
		.state('generationHistory', {
			url: '/generationHistory',
			templateUrl: servRootPathProvider.$get() + '/components/cookbook/generation-history/generation-history-template.html',
			controller: tds.cookbook.controller.TaskBatchHistoryController
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history details
		.state('generationHistory.detail', {
			url: '/{taskBatchId:[0-9]*}',
			views: {
				"taskBatchInnerContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-detail-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryDetailController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history actions
		.state('generationHistory.detail.actions', {
			url: '/actions',
			views: {
				"taskBatchTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-actions-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryActionsController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history tasks
		.state('generationHistory.detail.tasks', {
			url: '/tasks',
			views: {
				"taskBatchTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-tasks-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryTasksController
				}
			}
		})
		// ----------------------------------------------------------------------
		// STATE DESC: Show task batch history logs
		.state('generationHistory.detail.logs', {
			url: '/logs',
			views: {
				"taskBatchTabsContent": {
					templateUrl: servRootPathProvider.$get() + '/components/cookbook/history/task-batch-history-logs-template.html',
					controller: tds.cookbook.controller.TaskBatchHistoryLogsController
				}
			}
		})
		;

});
