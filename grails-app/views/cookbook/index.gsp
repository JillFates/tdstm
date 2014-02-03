<html ng-app="cookbookRecipes">
	<head>
		<title>Cookbook</title>
    	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		%{-- <meta name="layout" content="projectHeader" />	 --}%
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.css')}" />
    	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />
    	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ng-grid.css')}" />
    	<g:javascript src="jquery-1.9.1.js" />
    	<g:javascript src="bootstrap.js" />
    	<g:javascript src="angular.js" />
    	<g:javascript src="angular-resource.js" />
    	<g:javascript src="ui-bootstrap-tpls-0.10.0.js" />
    	<g:javascript src="ng-grid-2.0.7.min.js" />
    	<g:javascript src="controllers/cookbook.js" />
	</head>
	<body ng-controller="CookbookRecipeEditor">
		%{-- <div>{{recipes}}</div> --}%
		<div class="container">
			<div class="row-fluid clearfix" style="margin-top:10px;"> %{-- This last style attr should be removed --}%
				<div class="col-md-6 col-xs-6">
					<g:form id="switchContext" action="#">
						%{-- <div class="btn-group">
							<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
								Context <span class="caret"></span>
							</button>
							<ul class="dropdown-menu" role="menu">
								<li ng-repeat="c in contexts" ng-click="change()" ><a href="#">{{c}}</a></li>
							</ul>
						</div> --}%
						<select name="contextSelector" id="contextSelector" ng-model="context" ng-options="c for c in contexts" ng-change="change()">
						</select>
					</g:form>
				</div>
				<div class="col-md-6 col-xs-6">
					<div class="checkbox">
						<label class="pull-right">
							<input type="checkbox" name="viewArchived" id="viewArchived" value="n" ng-model="archived" ng-true-value="y" ng-false-value="n" ng-change="change()"> View Archived Recipes
						</label>
					</div>
				</div>
			</div>
			<div class="row-fluid clearfix">
				<div class="col-md-12">
					<div class="gridStyle" ng-grid="gridOptions"></div>
				</div>				
			</div>
			<div class="row-fluid clearfix">
				<div class="col-md-4 col-md-offset-4 paginationWrapper">
					<pagination boundary-links="true" total-items="totalItems" page="currentPage" class="pagination-sm" previous-text="&lsaquo;" next-text="&rsaquo;" first-text="&laquo;" last-text="&raquo;"></pagination>
				</div>
			</div>
			<div class="row-fluid clearfix">
				<div class="col-md-12">
					<ul class="nav nav-tabs">
						<li><a href="#">Task Generation</a></li>
						<li class="active"><a href="#">History</a></li>
						<li><a href="#">Editor</a></li>
						<li><a href="#">Versions</a></li>
					</ul>
					%{-- Task Generation --}%
					<div class="tabContainer hidden">Task Generation Content</div>

					%{-- History --}%
					<div class="tabContainer hidden">
						<div class="table-responsive historyMainTable">
							<table class="table table-hover table-striped recipesTable">
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
							<ul class="nav nav-tabs">
								<li><a href="#">Actions</a></li>
								<li class="active"><a href="#">Tasks</a></li>
								<li><a href="#">Generation Log</a></li>
							</ul>

							%{-- Actions Content --}%
							<div class="tabContainer hidden">
								<div class="btn-group">
									<button type="button" class="btn btn-default">Publish</button>
									<button type="button" class="btn btn-default">Refresh</button>
									<button type="button" class="btn btn-default">Delete</button>
								</div>

								<p class="actionsText">The selected tasks presently are unpublished. Press the _*Confirm*_ button in order to publish the tasks to the users.</p>

								<input type="button" value="Confirm">
							</div>

							%{-- Tasks Content --}%
							<div class="tabContainer hidden">
								<div class="table-responsive">
									<table class="table table-hover table-striped recipesTable">
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
							</div>

							%{-- Generation Log Content --}%
							<div class="tabContainer hidden">
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
							</div>
						</div>
					</div>

					%{-- Editor --}%
					<div class="tabContainer hidden">
						<div class="row clearfix">
							<div class="col-xs-6">
								<h5 class="title">Recipe</h5>
								<textarea name="recipeCode" id="recipeCode" rows="10"></textarea>
								<div class="clearfix btns">
									<div class="btn-group pull-left">
										<button type="button" class="btn btn-default">Save WIP</button>
										<button type="button" class="btn btn-default">Release</button>
										<button type="button" class="btn btn-default">Revert</button>
									</div>
									<button type="submit" class="btn btn-default pull-right">Validate Syntax</button>
								</div>
							</div>
							<div class="col-xs-6">
								<ul class="nav nav-tabs">
									<li class="active"><a href="#">Change Logs</a></li>
									<li><a href="#">Groups</a></li>
								</ul>

								%{-- Change Logs Content --}%
								<div class="tabContainer hidden">
									<ul class="logs">
										<li>Added Win2008 group</li>
										<li>Added step to re-ip Linux VMs before shutting them down</li>
										<li>Changed duration on Window 2003 reboots to 15 minutes</li>
										<li>Added Win2008 group</li>
										<li>Added step to re-ip Linux VMs before shutting them down</li>
										<li>Changed duration on Window 2003 reboots to 15 minutes</li>
										<li>Added Win2008 group</li>
										<li>Added step to re-ip Linux VMs before shutting them down</li>
										<li>Changed duration on Window 2003 reboots to 15 minutes</li>
										<li>Added Win2008 group</li>
										<li>Added step to re-ip Linux VMs before shutting them down</li>
										<li>Changed duration on Window 2003 reboots to 15 minutes</li>
									</ul>
								</div>
								<div class="tabContainer hidden">
									<form action="#" class="form-inline groups clearfix">
										<div class="form-group">
											<label for="testWith">Test With: </label>
											<select name="testOptions" id="testOptions">
												<option value="wave1">Wave 1</option>
												<option value="wave2">Wave 2</option>
											</select>
										</div>
										<button type="submit" class="btn btn-default pull-right">Refresh</button>
									</form>
									<div class="table-responsive groupsTable">
										<table class="table table-hover table-striped recipesTable">
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
										<table class="table table-hover table-striped recipesTable">
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
								</div>
							</div>
						</div>
					</div>

					%{-- Versions --}%
					<div class="tabContainer hidden">Versions Content</div>
				</div>
			</div>
			%{-- <div class="row-fluid">
				<div class="span6">Hi team I'm ${name}</div>
				<div class="span6">${role}</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<g:each in="${books}" var="book">
						<p>
							<span>Name: ${book.name} - </span>
							<span>Author: ${book.author} - </span>
							<span>Description: ${book.description}</span>
						</p>
					</g:each>
				</div>
				This is ${name} and I am a ${gender}
			</div> --}%
		</div>
		<script>
			$('#viewArchived').on('click', function(e){
				if($(this).val() == 'n'){
					$(this).val('y');
				}else{
					$(this).val('n')
				}
			})

			$('ul.nav-tabs > li.active').each(function(){
				var ind = $(this).index();
				$(this).parent().parent().find('.tabContainer:eq(' + ind + ')').removeClass('hidden');
			})
			$('ul.nav-tabs > li > a').on('click', function(e){
				e.preventDefault();
				if(!$(this).hasClass('active')){
					var ind = $(this).parent().index(),
						ulElement = $(this).parent().parent();
					ulElement.find('li').removeClass('active');
					$(this).parent().addClass('active');
					ulElement.parent().children('.tabContainer').addClass('hidden');
					ulElement.parent().children('.tabContainer:eq(' + ind + ')').removeClass('hidden');
				}
			});
		</script>
	</body>
</html>