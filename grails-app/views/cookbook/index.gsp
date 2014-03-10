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
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'cookbook.css')}" />
	<g:javascript src="angularNewVersion/angular.js" />
	<g:javascript src="codemirror/codemirror.js" />
	<g:javascript src="codemirror/ui-codemirror.js" />
	<g:javascript src="codemirror/addon/dialog.js" />
	<g:javascript src="codemirror/addon/search.js" />
	<g:javascript src="codemirror/addon/show-hint.js" />
	<g:javascript src="codemirror/addon/javascript-hint.js" />
	<g:javascript src="codemirror/javascript.js" />
	<g:javascript src="bootstrap.js" />
	<g:javascript src="angular-resource.js" />
	<g:javascript src="ui-bootstrap-tpls-0.10.0.js" />
	<g:javascript src="ng-grid-2.0.7.debug.js" />
	<g:javascript src="controllers/cookbook.js" />
</head>
<body>
	<div class="body" id="cookbookRecipesEditor" ng-app="cookbookRecipes" ng-controller="CookbookRecipeEditor">
		<div class="container">
			<form id="gridControls" class="row-fluid clearfix form-inline groups">
				<div class="col-md-4 col-xs-4 form-group">
					<label for="contextSelector">Context: 
						<select class="form-control" name="contextSelector" id="contextSelector" ng-model="context" ng-options="c for c in ['All', 'Event', 'Bundle', 'Application']" ng-disabled="editingRecipe" ng-change="changeRecipeList()"></select>
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
					<button class="btn btn-default createRecipe" ng-click="showDialog = true">Create Recipe</button>
				</div>
					%{-- <div class="col-md-4 paginationWrapper">
						<pagination boundary-links="true" total-items="totalItems" page="currentPage" class="pagination-sm" previous-text="&lsaquo;" next-text="&rsaquo;" first-text="&laquo;" last-text="&raquo;"></pagination>
					</div> --}%
				</div>
				<div class="row-fluid clearfix">
					<div class="col-md-12">
						<tabset id="mainTabset">
							%{-- Task Generation --}%
							<tab heading="Task Generation" active="activeTabs.taskGeneration">
								<p>Task Generation Content</p>
							</tab>

							%{-- History --}%
							<tab heading="History" active="activeTabs.history">
								<div class="table-responsive historyMainTable">
									<table class="table table-hover table-striped ngGridTable">
										<thead>
											<tr>
												<th>Target</th>
												<th># of Tasks</th>
												<th>User</th>
												<th>Generated At</th>
												<th>Version</th>
												<th>Published</th>
												<th>Del</th>
											</tr>
										</thead>
										<tbody>
											<tr>
												<td>Wave-001-HR</td>
												<td>20</td>
												<td>Jim Lauchure</td>
												<td>1/12/14 4:38pm</td>
												<td>3</td>
												<td>
													<input type="checkbox" name="pulished" id="pulished">
												</td>
												<td>
													<a href="#" class="actions delete"><span class="glyphicon glyphicon-remove"></span></a>
												</td>
											</tr>
											<tr>
												<td>Wave-001-Payroll</td>
												<td>65</td>
												<td>Robin Banks</td>
												<td>1/8/14 08:12am</td>
												<td>6</td>
												<td>
													<input type="checkbox" name="pulished" id="pulished" checked>
												</td>
												<td>
													<a href="#" class="actions delete"><span class="glyphicon glyphicon-remove"></span></a>
												</td>
											</tr>
										</tbody>
									</table>
								</div>

								<div class="innerTabWrapper">
									
									<tabset>
										%{-- Actions Content --}%
										<tab heading="Task Generation" active="activeSubTabs.history.actions">
											<div class="btn-group">
												<button type="button" class="btn btn-default">Publish</button>
												<button type="button" class="btn btn-default">Refresh</button>
												<button type="button" class="btn btn-default">Delete</button>
											</div>

											<p class="actionsText">The selected tasks presently are unpublished. Press the _*Confirm*_ button in order to publish the tasks to the users.</p>

											<input type="button" value="Confirm">
										</tab>
										
										%{-- Tasks Content --}%
										<tab heading="Task Content" active="activeSubTabs.history.tasks">
											<div class="table-responsive">
												<table class="table table-hover table-striped ngGridTable">
													<thead>
														<tr>
															<th>Task #</th>
															<th>Description</th>
															<th>Asset</th>
															<th>Team</th>
															<th>Person</th>
															<th>Due Date</th>
															<th>Status</th>
														</tr>
													</thead>
													<tbody> 
														<tr>
															<td>8132</td>
															<td>Application current state assessment</td>
															<td>HR</td>
															<td>App Owner</td>
															<td>Fran Tick</td>
															<td>1/18/14</td>
															<td>Done</td>
														</tr>
														<tr>
															<td>8132</td>
															<td>Application current state assessment</td>
															<td>HR</td>
															<td>App Owner</td>
															<td>Fran Tick</td>
															<td>1/18/14</td>
															<td>Done</td>
														</tr>
														<tr>
															<td>8134</td>
															<td>Stack holder assessment</td>
															<td>HR</td>
															<td>Proj Mgr</td>
															<td>Jim Lauchure</td>
															<td>2/13/14</td>
															<td>Done</td>
														</tr>
													</tbody>
												</table>
											</div>
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
											<h5 class="headingTitle col-xs-6">Recipe</h5>
											<div class="versionLinks col-xs-6" style="text-align: right;">
												<label for="releasedVersionRadio" ng-show="gridOptions.selectedItems[0].versionNumber > 0">
													<input type="radio" ng-checked="wipConfig[gridData[currentSelectedRow.rowIndex].recipeId] == 'release'" ng-model="wipConfig[gridData[currentSelectedRow.rowIndex].recipeId]" value="release" ng-click="changeRecipe('release')" name="releasedWipVersion" id="releasedVersionRadio"> Version {{gridOptions.selectedItems[0].versionNumber}}
												</label>
												<label for="wipVersionRadio" style="margin-left: 15px;">
													<input type="radio" ng-checked="wipConfig[gridData[currentSelectedRow.rowIndex].recipeId] == 'wip'" ng-model="wipConfig[gridData[currentSelectedRow.rowIndex].recipeId]" value="wip" ng-click="changeRecipe('wip')" name="releasedWipVersion" id="wipVersionRadio"> WIP
												</label>
											</div>
										</div>
										<section class="codeMirrorWrapper"> 
											<textarea readonly name="recipeCode" title="Double click to edit" id="recipeCode" rows="10" ng-model="selectedRecipe.sourceCode" ng-dblclick="syntaxModal.showModal = true" ng-disabled="!currentSelectedRecipe" value="{{selectedRecipe.sourceCode}}"></textarea>
										</section>
										<div class="clearfix btns">
											<div class="btn-group pull-left" style="margin-right:15px;">
												<button type="button" class="btn btn-default" ng-disabled="!currentSelectedRecipe" ng-click="editorActions.saveWIP()">Save WIP</button>
												<button type="button" ng-disabled="!selectedRecipe.hasWIP || !currentSelectedRecipe || selectedRecipe.versionNumber > 0" class="btn btn-default" ng-click="editorActions.releaseVersion()">Release</button>
											</div>
											<div class="btn-group pull-left">
												<button type="button" class="btn btn-default" ng-disabled="!editingRecipe || !currentSelectedRecipe" ng-click="editorActions.cancelChanges()">Cancel</button>
												<button type="button" class="btn btn-default" ng-disabled="!selectedRecipe.hasWIP || !currentSelectedRecipe || selectedRecipe.versionNumber > 0" ng-click="editorActions.discardWIP()">Discard WIP</button>
											</div>
											<button type="submit" class="btn btn-default pull-right" ng-disabled="selectedRecipe.sourceCode == '' || !currentSelectedRecipe" ng-click="editorActions.validateSyntax()">Validate Syntax</button>
										</div>
									</div>
									<div class="col-xs-6">
										<tabset>
											
											%{-- Change Logs Content --}%
											<tab heading="Change Logs" active="activeSubTabs.editor.logs">
												<label for="logs" class="sr-only">Logs </label>
												<textarea name="logs" id="logs" rows="6" ng-model="selectedRecipe.changelog" ng-disabled="!currentSelectedRecipe" value="{{selectedRecipe.changelog}}"></textarea>
											</tab>

											%{-- Groups Content --}%
											<tab heading="Groups" active="activeSubTabs.editor.groups">
												<form action="#" class="form-inline groups clearfix">
													<div class="form-group col-xs-7">
														<label for="testWith">Test With: </label>
														<select name="testOptions" class="form-control" id="testOptions" ng-disabled="!currentSelectedRecipe" >
															<option value="wave1">Wave 1</option>
															<option value="wave2">Wave 2</option>
														</select>
													</div>
													<div class="form-group col-xs-5">
														<button type="submit" ng-disabled="!currentSelectedRecipe"  class="btn btn-default pull-right">Refresh</button>
													</div>
												</form>
												<div class="table-responsive groupsTable">
													<table class="table table-hover table-striped ngGridTable">
														<thead>
															<tr>
																<th>Class</th>
																<th>Name</th>
																<th>Count</th>
															</tr>
														</thead>
														<tbody> 
															<tr>
																<td>Application</td>
																<td>APP_ALL</td>
																<td>15</td>
															</tr>
															<tr>
																<td>Application</td>
																<td>APP_CRIT</td>
																<td>5</td>
															</tr>
															<tr>
																<td>Application</td>
																<td>APP_NONCRIT</td>
																<td>10</td>
															</tr>
															<tr>
																<td>Server</td>
																<td>SRV_ALL</td>
																<td>38</td>
															</tr>
															<tr>
																<td>Server</td>
																<td>SRV_VM</td>
																<td>12</td>
															</tr>
														</tbody>
													</table>
												</div>
												<div class="table-responsive groupsTable2">
													<table class="table table-hover table-striped ngGridTable">
														<thead>
															<tr>
																<th>Asset Name</th>
															</tr>
														</thead>
														<tbody> 
															<tr>
																<td>Payroll</td>
															</tr>
															<tr>
																<td>HR</td>
															</tr>
															<tr>
																<td>SAP</td>
															</tr>
															<tr>
																<td>Exchange</td>
															</tr>
														</tbody>
													</table>
												</div>
											</tab>
											%{-- Syntax Errors Content --}%
											<tab heading="Syntax Errors" active="activeSubTabs.editor.syntaxErrors">
												<ul class="syntaxErrors">
													<li ng-repeat="error in originalDataRecipe.syntaxValidation">
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
							<tab heading="Versions" active="activeTabs.versions">Versions Content</tab>
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

				<div modal-show="showDialog" class="modal fade" id="createRecipeModal">
					<div class="modal-dialog modal-lg">
						<form class="form-horizontal modal-content" name="form" role="form" novalidate >
							<div class="modal-header">
								<h3>Create a recipe</h3>
							</div>
							<div class="modal-body">
								<tabset>
									%{-- New Recipe Tab --}%
									<tab heading="Brand New Recipe" >
										<div class="form-group">
											<label for="inputName" class="col-sm-2 control-label">Name*</label>
											<div class="col-sm-10">
												<input type="text" class="form-control" id="inputName" placeholder="" name="inputName" ng-model="newRecipe.name" required>
												<div ng-show="form.inputName.$dirty && form.inputName.$invalid">
													<pre class="error-msg" ng-show="form.inputName.$error.required">Recipe Name is required.</pre>
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
									<tab heading="Clone An Existing Recipe">
										Clone Recipe Stuff
									</tab>
								</tabset>
							</div>
							<div class="modal-footer">
								<button class="btn btn-primary" ng-disabled="form.$invalid || isUnchanged(newRecipe)" ng-click="modalBtns.save()">Save</button>
								<button class="btn btn-warning" ng-click="modalBtns.cancel()">Cancel</button>
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
								<button class="btn btn-primary" ng-click="syntaxModal.btns.save()">Close</button>
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