<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
		<asset:javascript src="resources/tds-common.js"/>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="angular/angular.js" />
		<asset:javascript src="angular/plugins/angular-ui.js"/>
		<asset:javascript src="angular/plugins/angular-resource.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}"></script>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<asset:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<asset:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<asset:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
		<g:javascript src="projectStaff.js" />
		<asset:stylesheet href="css/tds.css" />
		<asset:stylesheet href="css/qvga.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />
		<asset:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<asset:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
		<title>Project Staff</title>


		<asset:stylesheet href="css/jquery.autocomplete.css" />
		<asset:stylesheet href="css/ui.accordion.css" />
		<asset:stylesheet href="css/ui.resizable.css" />
		<asset:stylesheet href="css/ui.slider.css" />
		<asset:stylesheet href="css/ui.tabs.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />
		<asset:stylesheet href="css/resources/ui.datetimepicker.css" />
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<asset:stylesheet href="components/comment/comment.css" />
		<asset:stylesheet href="css/spinner.css" />

	</head>
	<body>
		<tds:subHeader title="Project Staff" crumbs="['Project', 'Staff']"/><br />
		<div class="body" ng-app="tdsProjectStaff" ng-controller="tds.staffing.controller.MainController as staffing" style="float:none;">
			<g:render template="/assetEntity/listTitleAlerts" ></g:render>
			<div id="staffSelectId" style=" overflow-x: auto; ">
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
								${onlyClientStaff=='1'? 'checked="checked" value=1'  : 'value=0'}
							/>
						</td>
						<td>
							<span><b id="onlyAssignedLabel">Only Assigned</b></span>
							<br/>
								<input type="checkbox" name="assigned" id="assignedId"
									onChange="this.value = this.checked ? 1 : 0; loadFilteredStaff('lastName','staff');"
									${assigned=='1'? 'checked="checked" value=1' : 'value=0'}
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
								<asset:image src="images/spinner.gif" alt="Spinner" />
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
		</div>

		<script type="text/javascript">
			$(document).ready(function() {
				loadFilteredStaff('lastName','staff');

				// handle the scrolling header
				$(window).scroll( function() {
					handleHeader();
				});

				handleHeader();

				// watch out, this code detect when the screen has just finished his resize event.
				var toResize;
				$(window).resize(function() {
					clearTimeout(toResize);
					toResize = setTimeout(function() {
						handleHeader('resize');
					}, 100);
				});

				$('#staffSelectId').scroll(function() {
					var position = 31 - $(this).scrollLeft();
					$('.floatingHeader').css('left', position + "px");
				});

				$("#unselectDialog").dialog({
      				autoOpen: false
				});

			});

			function handleHeader (eventType) {

				if($('#staffingTable').length > 0) {
					var scrollLimit = $('#staffingTable').offset().top;
					var header = $('#headerRowId');
					var leftOffset = header.offset().left;
					handleHeaderPositionGeneral(scrollLimit, header, 0, leftOffset, eventType);
				}
			}

			// Used to make it easier for users to click the surrounding area of the checkbox
			function clkCB(event, elem, personId, projectId, moveEventId , projectStaffRole, action) {
				if( $(event.target).is("td")) {
					toggleChangeChckboxState($(':checkbox', elem));
				}

				if(action === 'addRemoveProjectTeam') {
					addRemoveProjectTeam($(':checkbox', elem), personId, projectId, projectStaffRole);
				} else if(action === 'togPrjStaff') {
					togPrjStaff($(':checkbox', elem), personId, projectId, projectStaffRole);
				} else if(action === 'togEvtStaff') {
					togEvtStaff($(':checkbox', elem), personId, projectId, moveEventId, projectStaffRole);
				}

			}

	 	</script>

	 	<div id="unselectDialog" title="Confirm before proceeding">

		</div>


		<div id="overlay">
			<div id="overlay-wrapper">
			</div>
		</div>

	<script>
		 $('.menu-projects-project-staff').addClass('active');
		 $('.menu-parent-projects').addClass('active');
	</script>
	</body>
 </html>
