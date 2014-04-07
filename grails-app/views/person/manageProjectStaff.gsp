<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="projectHeader" />
		<g:javascript src="projectStaff.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
		<title>Project Staff</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'calendarview.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		
	</head>
	<body>
		<div class="body">
			<h1>Project Staff</h1>
			<div id="staffSelectId">
			<table id="staffFilterId" style="border: 0px;width: 100%;" >
				<tr>
					<td>
						<span><b>Team</b></span><br/>
						<label for="role">
							<g:select id="role" name="role" from="${roleTypes}" optionKey="id" optionValue="${{it.description}}"  value="${currRole}" onChange="loadFilteredStaff('lastName','staff')"
									noSelection="${['0':'All']}"></g:select>
						</label>
					</td>
					<tds:hasPermission permission='EditTDSPerson'>
						<td>
							<span><b>Only Client Staff</b></span><br/>
								<input type="checkbox" name="clientStaff" id="clientStaffId"  onChange="if(this.checked){this.value = 1} else {this.value = 0 };loadFilteredStaff('lastName','staff')"
								${onlyClientStaff=='1'? 'checked="checked" value="1"'  : 'value="0"'}/>
						</td>
					</tds:hasPermission>
					<td>
						<span><b>Only Assigned</b></span><br/>
							<input type="checkbox" name="assigned" id="assignedId"  onChange="if(this.checked){this.value = 1} else {this.value = 0 };loadFilteredStaff('lastName','staff')"
							${assigned=='1'? 'checked="checked" value="1"' : 'value="0"'}/>
					</td>
					<%--<td>
						<span><b>Location</b></span><br/>
						<label for="location">
							<g:select id="location" name="location"  from="${['All', 'Local']}"  value="${currLoc }" onChange="loadFilteredStaff('lastName','staff')"></g:select>
						</label>
					</td>
					--%>
					<td>
						<span><b>Project</b></span><br/>
						<label for="project">
							<select id="project" name="project" onChange="loadFilteredStaff('lastName','staff')">
								<tds:hasPermission permission='EditProjectStaff'>
									<option value="0">All</option>
								</tds:hasPermission>
								<g:each in="${projects}" var="project">
									<option value="${project.id}" ${project.id == projectId ? 'selected="selected"' : ''}>
										${project.name}
									</option>
								</g:each>
							</select>
						</label>
					</td>
					<%--<td>
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
					</td>--%>
				</tr>
			</table>
			<br/>
			<input type="hidden" id="manageStaff" value="manageStaff">
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
			})
			
			
	 	</script>
	 </body>
 </html>
