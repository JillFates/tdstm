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
							<g:select id="role" name="role" from="${roleTypes}" optionKey="id" optionValue="${{it.description}}"  value="${currRole}" ng-click="loadFilteredStaff('fullName','staff')"
									noSelection="${['0':'All']}"></g:select>
						</label>
					</td>
					<tds:hasPermission permission='EditTDSPerson'>
						<td>
							<span><b id="onlyClientStaffLabel">Only Client Staff</b></span><br/>
								<input type="checkbox" name="clientStaff" id="clientStaffId"  ng-change = "loadFilteredStaff('fullName','staff')"
								ng-model="onlyClientStaff" ng-init="onlyClientStaff=${onlyClientStaff == 1}"/>
						</td>
					</tds:hasPermission>
					<td>
						<span><b id="onlyAssignedLabel">Only Assigned</b></span><br/>
							<input type="checkbox" name="assigned" id="assignedId"  ng-change = "loadFilteredStaff('fullName','staff')"
								ng-model="onlyAssigned" ng-init="onlyAssigned=${assigned == 1}"/>
					</td>
					<%--
					<td>
						<span><b>Location</b></span><br/>
						<label for="location">
							<g:select id="location" name="location"  from="${['All', 'Local']}"  value="${currLoc }" onChange="loadFilteredStaff('lastName','staff')"></g:select>
						</label>
					</td>
					--%>
					<td>
						<span><b id="projectLabel">Project</b></span><br/>
						<label for="project">
							<select id="project" name="project" ng-model="selectedProject" ng-change="loadFilteredStaff('fullNameName','staff')" ng-init="selectedProject=${projectId}">
								<tds:hasPermission permission='EditProjectStaff'>
									<option value="0">All</option>
								</tds:hasPermission>
								<g:each in="${projects}" var="project">
									<option value="${project.id}">
										${project.name}
									</option>
								</g:each>
							</select>
						</label>
					</td>
					<%--
					<td>
						<table style="border: 0px">
							<tr>
								<td><b>Phases</b></td>
								<td><label for="preMove"><input type="checkbox"
										name="PhaseCheck" id="preMove" checked="checked" onClick="unCheckAll();"/>&nbsp;PreMove</label>
								</td>
								<td><label for="physical-trg"><input
										type="checkbox" name="PhaseCheck" id="physical-trg"
										checked="checked" onClick="unCheckAll();"/>&nbsp;Physical-trg</label></td>
							</tr>
							<tr>
								<td><label for="allPhase"><input type="checkbox"
										name="allPhase" id="allPhase" checked="checked" onclick="if(this.checked){this.value = 1} else {this.value = 0 }; checkAllPhase();" value="1"/>&nbsp;All</label></td>
								<td><label for="ShutDown"><input type="checkbox"
										name="PhaseCheck" id="ShutDown" checked="checked" onClick="unCheckAll()"/>&nbsp;ShutDown</label>
								</td>
								<td><label for="startUp"><input type="checkbox"
										name="PhaseCheck" id="startUp" checked="checked" onClick="unCheckAll()"/>&nbsp;startUp</label>
								</td>
							</tr>
							<tr>
								<td></td>
								<td><label for="physical-src"><input
										type="checkbox" name="PhaseCheck" id="physical-src"
										checked="checked" onClick="unCheckAll()"/>&nbsp;physical-src</label></td>
								<td><label for="postMove"><input type="checkbox"
										name="PhaseCheck" id="postMove" checked="checked" onClick="unCheckAll()"/>&nbsp;postMove</label>
								</td>
							</tr>
						</table>
					</td>
					<td>
						<span><b>Scale</b></span><br/>
						<label for="scale">
							<select id="scale" name="scale" onChange="loadFilteredStaff('lastName','staff')">
							 <option value="1"> 1 Month </option>
							 <option value="2"> 2 Month </option>
							 <option value="3"> 3 Month </option>
							 <option value="6"> 6 Month </option>
							</select>
						</label>
					</td>
					--%>
				</tr>
			</table>
			<br/>
			<input type="hidden" id="manageStaff" value="manageStaff" />
			<div id="projectStaffTableId">
				<g:render template="projectStaffTable"></g:render>
			</div>
			</div>
			<div id="personGeneralViewId" style="display: none;" title="Manage Staff "></div>
		</div>
		<script type="text/javascript">
			$(document).ready(function() {
				$("#scale").val(${currScale})
				$("#personGeneralViewId").dialog({ autoOpen: false })
				
				// handle the scrolling header
				$(window).scroll( function() {
					handleHeader();
				});
				handleHeader();
			})
			
			function handleHeader () {
				var scrollLimit = $('#staffingTable').offset().top;
				var header = $('#headerRowId');
				handleHeaderPositionGeneral(scrollLimit, header);
			}
			
	 	</script>
	 </body>
 </html>
