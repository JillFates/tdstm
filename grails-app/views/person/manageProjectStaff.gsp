<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<g:javascript src="tds-common.js" />
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="angular/angular.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
		<g:javascript src="angular/plugins/angular-resource.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}"></script>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<g:javascript src="bootstrap.js" />
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
		<g:javascript src="projectStaff.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
		<title>Project Staff</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />


				<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link href="/tdstm/css/jqgrid/ui.jqgrid.css" rel="stylesheet" type="text/css" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		
	</head>
	<body>
		<div class="body" ng-app="tdsProjectStaff" ng-controller="tds.staffing.controller.MainController as staffing">
			<h1>Project Staff</h1>
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
			<div id="staffSelectId" >
			<table id="staffFilterId" style="border: 0px;width: 100%;" >
				<tr>
					<td>
						<span><b id="teamLabel">Team</b></span><br/>
						<label for="role">
							<g:select id="role" name="role" from="${roleTypes}" optionKey="id" optionValue="${{it.description}}"  value="${currRole}" 
								onChange="loadFilteredStaff('lastName','staff')"
								noSelection="${['0':'All']}">
							</g:select>
						</label>
					</td>
					<td>
						<span><b id="onlyClientStaffLabel">Only Client Staff</b></span>
						<br/>
						<input type="checkbox" name="clientStaff" id="clientStaffId" 
							onChange="this.value = this.checked ? 1 : 0; loadFilteredStaff('lastName','staff');"
							${onlyClientStaff=='1'? 'checked="checked" value="1"'  : 'value="0"'}
						/>
					</td>
					<td>
						<span><b id="onlyAssignedLabel">Only Assigned</b></span>
						<br/>
							<input type="checkbox" name="assigned" id="assignedId"  
								onChange="this.value = this.checked ? 1 : 0; loadFilteredStaff('lastName','staff');"
								${assigned=='1'? 'checked="checked" value="1"' : 'value="0"'}
							/>
					</td>
					<td>
						<span><b id="projectLabel">Project</b></span><br/>
						<label for="project">
							<select id="project" name="project" ng-model="selectedProject" onChange="loadFilteredStaff('fullNameName','staff')" ng-init="selectedProject=${projectId}">
								<g:each in="${projects}" var="project">
									<option value="${project.id}">
										${project.name}
									</option>
								</g:each>
							</select>
						</label>
					</td>
					<td>
						<span><b>Events Option</b></span><br/>
						<select id="eventsOption" name="eventsOption" onChange="loadFilteredStaff('fullNameName','staff')">
							<option value="A">Active</option>
							<option value="X">All</option>
							<option value="C">Completed</option>
						</select>

						<span id="spinner" class="spinner" style="margin-left:10px; position:inherit;">
							<img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
						</span>
					</td>
				</tr>
			</table>
			<br/>
			<input type="hidden" id="manageStaff" value="manageStaff" />
			<div id="projectStaffTableId">
				<% // <g:render template="projectStaffTable"></g:render> %>
			</div>
			</div>
			<div id="personGeneralViewId" style="display: none;" title="Manage Staff "></div>
		</div>

		<script type="text/javascript">
			$(document).ready(function() {
				$("#personGeneralViewId").dialog({ autoOpen: false });
				loadFilteredStaff('lastName','staff');
				
				// handle the scrolling header
				$(window).scroll( function() {
					handleHeader();
				});
				handleHeader();
			})
			
			function handleHeader () {

				if($('#staffingTable').length > 0) {
					var scrollLimit = $('#staffingTable').offset().top;
					var header = $('#headerRowId');
					var leftOffset = header.offset().left;
					handleHeaderPositionGeneral(scrollLimit, header, 0, leftOffset);
				}
			}

			// Used to make it easier for users to click the surrounding area of the checkbox
			function clkCB(elem) {
				$(':checkbox', elem).trigger('click');
			}
			
	 	</script>
	 </body>
 </html>
