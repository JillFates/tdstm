<!doctype html>
<html xmlns:ng="http://angularjs.org">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Cookbook</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ng-grid.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'codemirror/codemirror.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'codemirror/addon/dialog.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'codemirror/addon/show-hint.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'mergely/mergely.css')}" />	
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'cookbook.css')}" />
	<g:javascript src="angular/angular.min.js" />
	<g:javascript src="codemirror/codemirror.js" />
	<g:javascript src="codemirror/ui-codemirror.js" />
	<g:javascript src="codemirror/addon/dialog.js" />
	<g:javascript src="codemirror/addon/search.js" />
	<g:javascript src="codemirror/addon/show-hint.js" />
	<g:javascript src="codemirror/addon/javascript-hint.js" />
	<g:javascript src="codemirror/javascript.js" />
	<g:javascript src="mergely/mergely.js" />	
	<g:javascript src="bootstrap.js" />
	<g:javascript src="angular/plugins/angular-resource.js" />
	<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
	<g:javascript src="controllers/cookbook.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
</head>
<body>

<g:include view="/layouts/_error.gsp" />

	<div class="body" id="cookbookRecipesEditor" ng-app="cookbookRecipes" ng-controller="CookbookRecipeEditor">
		<div class="container">
			<form id="gridControls" class="row-fluid clearfix form-inline groups">
				<div class="col-md-4 col-xs-4 form-group">
					<label for="contextSelector">Context: 
						<select name="contextSelector" id="contextSelector" ng-model="context" ng-options="c for c in ['All', 'Event', 'Bundle', 'Application']" ng-disabled="editingRecipe" ng-change="changeRecipeList()"></select>
					</label>
				</div>
				<div class="col-md-6 col-xs-6 form-group pull-right archiveCheckWrapper">
					<div class="checkbox pull-right">
						<label class="pull-right">
							<input type="checkbox" name="viewArchived" id="viewArchived" value="n" ng-model="archived" ng-true-value="y" ng-false-value="n" ng-disabled="editingRecipe" ng-change="changeRecipeList()"> View Archived Recipes
						</label>
					</div>
				</div>
			</form>
			<div class="row-fluid clearfix">
				<div class="col-md-12">
					<div class="gridStyle" ng-grid="gridOptions"></div>
				</div>				
			</div>
			<div class="row-fluid clearfix">
				<div class="col-md-4">
					<button class="btn btn-default createRecipe" ng-click="showCreateRecipeDialog()">Create Recipe</button>
				</div>
					%{-- <div class="col-md-4 paginationWrapper">
						<pagination boundary-links="true" total-items="totalItems" page="currentPage" class="pagination-sm" previous-text="&lsaquo;" next-text="&rsaquo;" first-text="&laquo;" last-text="&raquo;"></pagination>
					</div> --}%
				</div>
				<div class="row-fluid clearfix">
					<div class="col-md-12">
						<tabset id="mainTabset" class="hidden" ng-class="{show : true}">
							%{-- Task Generation --}%
							<tab heading="Task Generation" active="activeTabs.taskGeneration">
								<p>Select appropriate context to generate tasks using the {{selectedRecipe.name}} recipe:</p>
								<form action="#" class="form-inline taskGeneration clearfix" ng-show="tasks.show.generate">
									<label class="inline" for="eventSelect">Event:
										<select name="eventSelect" id="eventSelect" ng-model="tasks.selectedEvent" ng-change="tasks.eventSelected()" ng-options="item as item.name for item in tasks.eventsArray" ng-change="tasks.validCurrentSelection = tasks.getGenerateBtnStatus()">
											<option value="">Please select</option>
										</select>
									</label>
									<label class="inline" ng-show="currentSelectedRecipe.context == 'Bundle' || currentSelectedRecipe.context == 'Application' || currentSelectedRecipe.context == 'All'" for="eventSelect">Bundle:
										<select name="bundleSelect" id="bundleSelect" ng-model="tasks.selectedBundle" ng-change="tasks.bundleSelected()" ng-options="item as item.name group by item.group for item in tasks.bundlesArray">
											<option value="">Please select</option>
										</select>
									</label>
									<label class="inline" ng-show="currentSelectedRecipe.context == 'Application' || currentSelectedRecipe.context == 'All'" for="eventSelect">Application:
										<select name="applicationSelect" id="applicationSelect" ng-model="tasks.selectedApplication" ng-change="tasks.checkValidSelection()" ng-options="item as item.name group by item.group for item in tasks.applicationsArray">
											<option value="">Please select</option>
										</select>
									</label>

									<div>
										<label for="autoPublishTasks">
											<input type="checkbox" name="autoPublishTasks" id="autoPublishTasks" ng-model="tasks.generateOpts.autoPublish">
											Automatically publish tasks
										</label>
									</div>	
									<div ng-show="currentSelectedRecipe.hasWIP">	
										<label for="generateUsingWIP">
											<input type="checkbox" name="generateUsingWIP" id="generateUsingWIP" ng-model="tasks.generateOpts.useWIP">
											Generate using WIP recipe
										</label>
									</div>	
									<div>	
										<label for="deletePreviouslyGenerated" ng-show="">
											<input type="checkbox" name="deletePreviouslyGenerated" id="deletePreviouslyGenerated" ng-model="tasks.generateOpts.deletePrevious">
											Delete previously generated tasks that were created using this context & recipe
										</label>
									</div>
									<div class="generateWrapper">
										<label for="generateTask">
											<a class="btn btn-default has-spinner" id="generateTask" ng-disabled="!tasks.validCurrentSelection" ng-click="tasks.generateTask(this)">
												<span class="spinner"><i class="icon-spin icon-refresh"></i></span>Generate
											</a>
										</label>
									</div>
								</form>
								<div class="completionWrapper ng-hide" ng-show="tasks.show.generating">
									<tabset id="taskGenerationTabs">
										<tab heading="Summary" active="activeSubTabs.tasks.summary">
											
											<ul class="summaryList">
												<li>Status: Completed | Canceled</li>
												<li>Tasks Created: 1462</li>
												<li>Number of Exceptions: 25</li>
											</ul>

										</tab>
										<tab heading="Exceptions" active="activeSubTabs.tasks.exceptions"></tab>
										<tab heading="Info" active="activeSubTabs.tasks.info"></tab>
									</tabset>

									<div class="completionButtons">
										<a class="btn btn-default" href="#"><span class="glyphicon glyphicon-tasks"></span> View Results</a>
										<a class="btn btn-default" href="#"><span class="glyphicon glyphicon-stats"></span> View Task Graph</a>
										<a class="btn btn-default" href="#"><span class="glyphicon glyphicon-arrow-left"></span> Start Over</a>
									</div>
								</div>
								<div class="progressWrapper row ng-hide" ng-show="tasks.show.completion">
									<div class="col-md-3 col-xs-3">
										<p class="text-right">Creating Tasks: </p>
									</div>
									<div class="col-md-6 col-xs-6">
										<div class="progress">
											<div class="progress-bar" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width: 60%;">
												60%
											</div>
										</div>
										<p class="text-center">Estimated to finish in 3 min 40 sec </p>
										<div class="text-center"><button class="btn btn-default">Cancel Generation</button></div>
									</div>
								</div>
							</tab>

							%{-- History --}%
							<tab heading="History" active="activeTabs.history" ng-click="tasks.updateGrid()">
								
								<div class="gridStyleTasks" ng-grid="tasks.tasksGrid"></div>
								
								<div class="innerTabWrapper">
									<tabset>
										%{-- Actions Content --}%
										<tab heading="Task Generation" active="activeSubTabs.history.actions">
											<div class="btn-group">
												<button type="button" class="btn btn-default" ng-disabled="tasks.selectedTaskBatch == null" ng-bind="tasks.selectedTaskBatch.isPublished && 'Unpublish' || 'Publish'" ng-click="tasks.publishUnpublishTaskBatch(tasks.selectedTaskBatch)">Publish</button>
												<button type="button" class="btn btn-default" ng-disabled="tasks.selectedTaskBatch == null">Refresh</button>
												<button type="button" class="btn btn-default" ng-disabled="tasks.selectedTaskBatch == null" ng-click="tasks.deleteTaskBatch(tasks.selectedTaskBatch.id)">Delete</button>
											</div>
										</tab>
										
										%{-- Tasks Content --}%
										<tab heading="Task Content" active="activeSubTabs.history.tasks">
											
										</tab>

										%{-- Generation Log Content --}%
										<tab heading="Generation Log" active="activeSubTabs.history.logs">
											<form action="#">
												<div class="radio-inline">
													<label>
														<input type="radio" name="logRadio" id="exceptions" value="option1" checked>
														Exceptions
													</label>
												</div>
												<div class="radio-inline">
													<label>
														<input type="radio" name="logRadio" id="infoWarnings" value="option2">
														Info/Warning
													</label>
												</div>
												<textarea name="logsArea" id="logsArea" rows="10"></textarea>
											</form>
										</tab>
									</tabset>
								</div>
							</tab>

							%{-- Editor Content --}%
							<tab heading="Editor" active="activeTabs.editor">
								<div class="row clearfix edition">
									<div class="col-xs-6">
										<div class="titleWrapper row">
											<h5 class="headingTitle col-xs-6">{{currentSelectedRecipe.name}}</h5>
											<div class="versionLinks col-xs-6" style="text-align: right;">
												<label for="releasedVersionRadio" ng-show="gridOptions.selectedItems[0].versionNumber > 0">
													<input type="radio" ng-model="wipConfig[gridData[currentSelectedRow.rowIndex].recipeId].opt" value="release" ng-change="switchWipRelease('release')" name="releasedWipVersion" id="releasedVersionRadio"> Version {{gridOptions.selectedItems[0].versionNumber}}
												</label>
												<label for="wipVersionRadio" style="margin-left: 15px;">
													<input type="radio" ng-model="wipConfig[gridData[currentSelectedRow.rowIndex].recipeId].opt" value="wip" ng-change="switchWipRelease('wip')" name="releasedWipVersion" id="wipVersionRadio"> WIP
												</label>
											</div>
										</div>
										<section class="codeMirrorWrapper"> 
											<textarea readonly name="recipeCode" title="Double click to edit" id="recipeCode" rows="10" ng-model="selectedRecipe.sourceCode" ng-dblclick="syntaxModal.showModal = true" ng-disabled="!currentSelectedRecipe" value="{{selectedRecipe.sourceCode}}"></textarea>
										</section>
										<div class="clearfix btns">
											<div class="btn-group pull-left" style="margin-right:6px;">
												<button type="button" class="btn btn-default" ng-disabled="!currentSelectedRecipe || !editingRecipe" ng-click="editorActions.saveWIP()">Save WIP</button>
												<button type="button" ng-disabled="!selectedRecipe.hasWIP || !currentSelectedRecipe || selectedRecipe.versionNumber > 0" class="btn btn-default" ng-click="editorActions.releaseVersion()">Release</button>
											</div>
											<div class="btn-group pull-left" style="margin-right:6px;">
												<button type="button" class="btn btn-default" ng-disabled="!editingRecipe || !currentSelectedRecipe" ng-click="editorActions.cancelChanges()">Cancel</button>
												<button type="button" class="btn btn-default" ng-disabled="!selectedRecipe.hasWIP || !currentSelectedRecipe || selectedRecipe.versionNumber > 0" ng-click="editorActions.discardWIP()">Discard WIP</button>
											</div>
											<div class="btn-group pull-left">
												<button type="button" class="btn btn-default" ng-disabled="!((selectedRWip != null) && (selectedRVersion != null && (selectedRVersion.versionNumber > 0)))" ng-click="editorActions.diff()">Diff</button>
												<button type="submit" class="btn btn-default pull-right" ng-disabled="selectedRecipe.sourceCode == '' || !currentSelectedRecipe" ng-click="editorActions.validateSyntax()">Validate Syntax</button>
											</div>
										</div>
									</div>
									<div class="col-xs-6">
										<tabset>
											
											%{-- Change Logs Content --}%
											<tab heading="Change Logs" active="activeSubTabs.editor.logs">
												<label for="logs" class="sr-only">Logs </label>
												<textarea name="logs" id="logs" rows="6" ng-model="selectedRecipe.changelog" ng-disabled="!currentSelectedRecipe || wipConfig[gridData[currentSelectedRow.rowIndex].recipeId].opt == 'release'" value="{{selectedRecipe.changelog}}"></textarea>
											</tab>

											%{-- Groups Content --}%
											<tab heading="Groups" active="activeSubTabs.editor.groups" ng-click="groups.updateGrid()">
												<div class="groups">
													<div class="selectors in">
														<div class="form-group">
															<label class="inline text-right" for="eventSelect">Event:</label>
																<select name="eventSelect" id="eventSelect" ng-model="groups.selectedEvent" ng-change="groups.eventSelected()" ng-options="item as item.name for item in groups.eventsArray">
																	<option value="">Please select</option>
																</select>
														</div>	
														
														<div class="form-group" ng-show="currentSelectedRecipe.context == 'Bundle' || currentSelectedRecipe.context == 'Application' || currentSelectedRecipe.context == 'All'">
															<label class="inline text-right" for="eventSelect">Bundle:</label>
																<select name="bundleSelect" id="bundleSelect" ng-model="groups.selectedBundle" ng-change="groups.bundleSelected()" ng-options="item as item.name group by item.group for item in groups.bundlesArray">
																	<option value="">Please select</option>
																</select>
														</div>

														<div class="form-group" ng-show="currentSelectedRecipe.context == 'Application' || currentSelectedRecipe.context == 'All'">
															<label class="inline text-right" for="eventSelect">Application:</label>
																<select name="applicationSelect" id="applicationSelect" ng-model="groups.selectedApplication" ng-change="groups.applicationSelected()" ng-options="item as item.name group by item.group for item in groups.applicationsArray">
																	<option value="">Please select</option>
																</select>
														</div>

														<div class="refreshWrapper">
															<label for="refreshGroups">
																<a class="btn btn-default" ng-disabled="groups.fetchBtnDisabled" id="refreshGroups" ng-click="groups.fetchGroups()">
																	Fetch
																</a>
															</label>
														</div>
													</div>

													<a class="selectorsHideShow" data-toggle="collapse" data-target=".selectors">Show/Hide Selectors</a>
													
													<div class="gridStyleGroups" style="margin-top:15px;" ng-grid="groups.groupsGrid"></div>

													<div class="gridStyleGroups" ng-show="groups.showAssetsGrid" style="margin-top:15px;" ng-grid="assets.assetsGrid"></div>

												</div>
										
											</tab>
											%{-- Syntax Errors Content --}%
											<tab heading="Syntax Errors" class="no-padding" active="activeSubTabs.editor.syntaxErrors">
												<ul class="syntaxErrors">
													<li ng-repeat="error in currentSyntaxValidation">
														<p style="font-weight: bold" ng-bind="error.reason"></p>
														<p ng-bind-html="secureHTML(error.detail)"></p>
													</li>
												</ul>
											</tab>
										</tabset>
									</div>
								</div>
							</tab>

							%{-- Versions --}%
							<tab heading="Versions" active="activeTabs.versions" ng-click="versions.updateGrid()">
								<div class="row">
									<div class="col-xs-7">
										<div class="gridStyleGroups" ng-grid="versions.versionsGrid"></div>
									</div>
									<div class="col-xs-5">
										<tabset>
											<tab heading="Change Log" class="no-padding" active="activeSubTabs.versions.changeLog">
												<textarea class="fullWidth" name="versions_changelog" id="versions_changelog" cols="30" rows="10" value="{{versions.selectedVersion.changelog}}" readonly></textarea>
											</tab>
											<tab heading="Source Code" class="no-padding" active="activeSubTabs.versions.sourceCode">
												<textarea class="fullWidth" name="versions_sourcecode" id="versions_sourcecode" cols="30" rows="10" value="{{versions.selectedVersion.sourceCode}}" readonly></textarea>
											</tab>
											<tab heading="Diff" class="no-padding" active="activeSubTabs.versions.diff">
											     <p><label ng-show="versions.toCompareVersions.length > 0">Compare syntax differences with another version</label></p>
											     <p>
											       <label ng-show="versions.toCompareVersions.length > 0">Versions:</label>
												   <select  ng-show="versions.toCompareVersions.length > 0" name="vertionToCompareSelector" id="vertionToCompareSelector" ng-model="versions.toCompareVersion" ng-options="version for version in versions.toCompareVersions" required>
													 <option value="">Please select a version</option>
												   </select>
											     </p>
											     <p>
											        <button type="submit" ng-disabled="!versions.toCompareVersion" ng-show="versions.toCompareVersions.length > 0" ng-click="versions.onCompareVersions()">Compare</button>
											     </p>
                                                 <label ng-show="versions.toCompareVersions.length < 1">No versions available to compare</label>											     
											</tab>	
										</tabset>
									</div>
								</div>
							</tab>
						</tabset>
					</div>
				</div>

				%{-- <div class="saved alert alert-warning fade in">
					Saved!
				</div>

				<div class="error alert alert-error fade in">
					Error: {{responseMsg.error}}
				</div> --}%

				%{-- <alert ng-repeat="alert in alerts.list" type="alert.type" close="alerts.closeAlert($index)" class="animate-hide" ng-class="{lalala: alert.hidden}">{{alert.msg}}</alert> --}%

				<div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
					<button type="button" class="close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
					{{alert.msg}}
				</div>

				%{-- <div ng-model="selectedRecipe.sourceCode" ui-codemirror="codeEditorOptions" class="codeMirrorWrapper"></div> --}%

				<div modal-show="showDialog" class="modal fade" id="createRecipeModal" data-backdrop="static" data-keyboard="false">
					<div class="modal-dialog modal-lg">
						<form class="form-horizontal modal-content" id="createRecipeForm" name="createRecipeForm" role="form" novalidate >
							<div class="modal-header">
								<h3>Create a recipe</h3>
							</div>
							<div class="modal-body">
								<tabset>
									%{-- New Recipe Tab --}%
									<tab heading="Brand New Recipe" active="clone.activeTabs.newRecipe">
										<div class="form-group">
											<label for="inputName" class="col-sm-2 control-label">Name*</label>
											<div class="col-sm-10">
												<input type="text" class="form-control" id="inputName" placeholder="" name="inputName" ng-model="newRecipe.name" required>
												<div ng-show="createRecipeForm.inputName.$dirty && createRecipeForm.inputName.$invalid">
													<pre class="error-msg" ng-show="createRecipeForm.inputName.$error.required">Recipe Name is required.</pre>
												</div>
											</div>
										</div>
										<div class="form-group">
											<label for="textareaDescription" class="col-sm-2 control-label">Description</label>
											<div class="col-sm-10">
												<textarea class="form-control" rows="3" id="textareaDescription" placeholder="" ng-model="newRecipe.description"></textarea>
											</div>
										</div>
										<div class="form-group">
											<label for="contextSelector2" class="col-sm-2 control-label selectLabel">Context*</label>
											<div class="col-sm-10">
												<select name="contextSelector2" id="contextSelector2" ng-model="newRecipe.context" ng-options="d for d in ['Event', 'Bundle', 'Application']" required>
													<option value="">Select context</option>
												</select>
											</div>
										</div>							
									</tab>

									%{-- Clone tab --}%
									<tab heading="Clone An Existing Recipe" active="clone.activeTabs.clone" ng-click="clone.refreshGrid()">
										<div class="cloneSelectors">
											<div class="form-group">
												<label class="col-sm-2 control-label" for="eventSelect">Context</label>
												<div class="col-sm-10">
													<select class="form-control" name="contextSelect" id="cloneContextSelect" ng-model="clone.selectedContext" ng-change="clone.optionsSelected('context')" ng-options="item as item.name for item in clone.contextArray">
													</select>
												</div>
											</div>
											<div class="form-group">
												<label class="col-sm-2 control-label" for="projectSelect">Project</label>
												<div class="col-sm-10">
													<select class="form-control" name="projectSelect" id="cloneprojectSelect" ng-model="clone.selectedProject" ng-change="clone.optionsSelected('project')" ng-options="item as item.name for item in clone.projectsArray">
														<option value="">Please select</option>
													</select>
												</div>
											</div>
											%{-- <div class="form-group">
												<label class="col-sm-2 control-label" for="projectStateSelect">Project State</label>
												<div class="col-sm-10">
													<select class="form-control" name="projectStateSelect" id="projectStateSelect" ng-model="clone.selectedProjectState" ng-change="clone.optionsSelected('projectState')">
														<option value="">Please select</option>
														<option ng-repeat="ps in clone.projectsStateArray" value="{{ps}}">{{ps}}</option>
													</select>
												</div>
											</div> --}%
											<div class="form-group">
												<label class="col-sm-2 control-label">Recipe to clone</label>
												<div class="col-sm-10">
													<div class="gridStyle form-control" ng-grid="clone.projectsGrid" style="padding:0"></div>
												</div>
											</div>
											<div class="form-group">
												<label for="recipeName" class="col-sm-2 control-label">Name</label>
												<div class="col-sm-10">
													<input type="text" name="recipeName" ng-model="clone.newRecipe.name" class="form-control">
												</div>
											</div>
											<div class="form-group">
												<label for="recipeDescription" class="col-sm-2 control-label">Description</label>
												<div class="col-sm-10">
													<input type="text" name="recipeDescription" ng-model="clone.newRecipe.description" class="form-control"> 
												</div>
											</div>
										</div>
									</tab>
								</tabset>
							</div>
							<div class="modal-footer">
								<button class="btn btn-default" ng-disabled="(clone.activeTabs.newRecipe && (createRecipeForm.$invalid || isUnchanged(newRecipe))) || (!clone.activeTabs.newRecipe && (!clone.newRecipe.name || !clone.newRecipe.description || !clone.projectsGrid.selectedItems[0]))" ng-click="modalBtns.save()">Save</button>
								<button class="btn btn-default" ng-click="modalBtns.cancel()">Cancel</button>
							</div>
						</form>
					</div>
				</div>

				<div modal-show="syntaxModal" class="modal fade" id="editSyntax" data-backdrop="static" data-keyboard="false">
					<div class="modal-dialog modal-lg">
						<form class="form-horizontal modal-content" name="form" role="form" novalidate >
							<div class="modal-body">
								<div ng-model="syntaxModal.sourceCode" ui-codemirror="codeEditorOptions"></div>
							</div>
							<div class="modal-footer">
								<button class="btn btn-primary" ng-click="syntaxModal.btns.storeLocally()">Close</button>
								<button class="btn btn-warning" ng-click="syntaxModal.btns.cancel()">Cancel</button>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
		$('#editSyntax').on('shown.bs.modal', function (e) {
			$('.CodeMirror').each(function(i, el){
				setTimeout(function(){
					el.CodeMirror.refresh();
					el.CodeMirror.focus();
				}, 10)
			});
		})
		</script>
	</body>
	</html>