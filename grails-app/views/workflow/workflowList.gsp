<%@page import="net.transitionmanager.task.AssetComment; net.transitionmanager.project.Project" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Workflow</title>
<script type="text/javascript"> var showOption = 'show'</script>
</head>
<body>
<tds:subHeader title="Workflow" crumbs="['Admin','Workflow']"/><br />
<div class="body">
	
<div class="steps_table">
	<span class="span"><b>Workflow</b></span>
<div class="buttons" style="margin-left: 10px;margin-right: 10px;" id="showWorkflowActionButtons">
	<div class="menuButton" style="float: left;padding-top: 5px;">
		<g:link class="list" action="home">Workflow List</g:link>
		<a class="create" href="#" onclick="openWorkflowDialog()">Copy Workflow</a>
	</div>
	<g:form action="workflowRoles" name="workflowRolesForm">
		<div class="button" style="float: left;">
			<input type="hidden" name="workflowTransition" id="workflowTransitionId">
		</div>
	</g:form>
	<div style="float: left;">
	<g:form action="deleteWorkflow" onsubmit="return confirm('WARNING: Deleting this Workflow will remove any associated Projects and projects related data?');">
    	<input type="hidden" name="id" value="${workflow?.id}" />
        <span class="button"><input type="button" class="edit" value="Edit" onclick="editWorkflowList()"/></span>
        <!--span class="button"><input type="submit" class="delete" value="Delete" /></span-->
	</g:form>
	</div>
</div>
<div class="buttons" style="margin-left: 10px;margin-right: 10px;display: none;" id="editWorkflowActionButtons">
	<div class="menuButton" style="float: left;padding-top: 5px;">
		<g:link class="list" action="home">Workflow List</g:link>
		<a class="create" href="#" onclick="openWorkflowDialog()">Copy Workflow</a>
	</div>
	<div style="float: left;">
	<g:form action="workflowList">
		<input type="hidden" name="workflow" value="${workflow?.id}" />
		<span class="button"><input type="button" class="save" value="Update" onclick="validateAndSubmitUpdateForm()"/></span>
		<span class="button"><input type="button" class="create" onclick="addStep('edit');" value="Add Step" /></span>
		<span class="button"><input type="submit" class="cancel" onclick="return confirm('Are you sure?')" value="Cancel" /></span>
	</g:form>
    </div>
</div>
<div>
<table border="0" style="width: 400px;margin: 0px 10px 10px 20px; ">
	<tr class="prop">
		<td valign="top"  class="name">Workflow:</td>
		<td valign="top"  class="value">${workflow?.process}</td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Created On:</td>
		<td valign="top"  class="value"><tds:convertDateTime date="${workflow?.dateCreated}" /></td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Update On:</td>
		<td valign="top"  class="value"><tds:convertDateTime date="${workflow?.lastUpdated}" /></td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Updated By:</td>
		<td valign="top"  class="value">${workflow?.updatedBy}</td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Used On:</td>
		<td valign="top"  class="value">
		<g:if test="${workflow?.process}">
			<div style="height:60px; width:300px; overflow-x:auto;"><ul>
				<g:each in="${Project.findAllByWorkflowCode(workflow?.process)}" var="project">
				<li><g:link controller="project" action="show" id="${project.id}">${project?.name}</g:link></li>
				</g:each>
				</ul>
			</div>
			</g:if>
		</td>
	</tr>
</table>
</div>
<div class="errors">
	<g:each in="${workflowTransitionsList}" var="workflowTransition">
		<g:hasErrors bean="${workflowTransition}">
		<g:renderErrors bean="${workflowTransition}" as="list" />
		<script type="text/javascript">showOption = 'edit'</script>
		</g:hasErrors>
	</g:each>
</div>
<g:if test="${flash.message}">
	<div class="errors" style="padding-left:30px; background: url('/icons/exclamation.png') no-repeat scroll 8px 0 transparent">${flash.message}</div>
	<script type="text/javascript">showOption = 'edit'</script>
</g:if>
<div class="required"> Fields marked ( * ) are mandatory </div>
<br/>
<div class="list" style="border: 1px solid #5F9FCF; margin-left: 10px;margin-right: 10px;">
<div id="showWorkflowList">
<table>
	<thead>
		<tr>

			<th class="sortable">Step Code<span style="color: red">*</span></th>

			<th class="sortable">Label<span style="color: red">*</span></th>

			<th class="sortable">Dashboard Label</th>

			<th class="sortable">Sequence<span style="color: red">*</span></th>

			<th class="sortable">Category</th>

			<th class="sortable">Team</th>

			<th class="sortable">Duration</th>

			<th class="sortable">Action</th>

		</tr>
	</thead>
	<tbody>
		<g:if test="${workflowTransitionsList}">
		<g:each in="${workflowTransitionsList}" status="i" var="transitions">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td class="name">${transitions.transition?.code}</td>

				<td class="name">${transitions.transition?.name}</td>

				<td class="name">${transitions.transition?.dashboardLabel}</td>

				<td class="name">${transitions.transition?.transId}</td>

				<td class="name">${transitions.transition?.category}</td>

				<td class="name">${transitions.transition?.role}</td>

				<td class="name">${transitions.transition?.duration}</td>

				<td class="name">
				<g:if test="${transitions.donotDelete}">
					<a href="javascript:deleteWorkflowStep(${workflow.id}, ${transitions.transition.id})">
						<g:if test="${transitions.isExist}">
							Delete Step and History
						</g:if>
						<g:else>
							Delete
						</g:else>
					</a>
				</g:if>
				</td>
			</tr>
		</g:each>
		</g:if>
		<g:else>
		<tr><td colspan="8" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</div>
<div id="editWorkflowList" style="display: none;">
<g:form action="updateWorkflowSteps" name="updateWorkflowStepsForm">
<input type="hidden" name="workflow" value="${workflow?.id}" />
<input type="hidden" name="additionalSteps" id="additionalStepsId" value="0">
<input type="hidden" name="currentSteps" id="currentStepsId" value="${workflowTransitionsList.size()}">
<table>
	<thead>
		<tr>

			<th class="sortable">Step Code<span style="color: red">*</span></th>

			<th class="sortable">Label<span style="color: red">*</span></th>

			<th class="sortable">Dashboard Label</th>

			<th class="sortable">Sequence<span style="color: red">*</span></th>

			<th class="sortable">Category</th>

			<th class="sortable">Team</th>

			<th class="sortable">Duration</th>

			<th class="sortable">Action</th>

		</tr>
	</thead>
	<tbody id="editWorkflowStepsTbody">
		<g:if test="${workflowTransitionsList}">
		<g:each in="${workflowTransitionsList}" status="i" var="transitions">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td nowrap="nowrap">
					<input type="text" name="code_${transitions.transition.id}" id="codeId_${transitions.transition.id}" value="${transitions.transition?.code}" onchange="validateField(this.value, this.id, 'Code')" style="width: 100px;"/>
				</td>

				<td nowrap="nowrap">
					<input type="text" name="name_${transitions.transition.id}" id="nameId_${transitions.transition.id}" value="${transitions.transition?.name}"  onchange="validateField(this.value, this.id, 'Name')" style="width: 100px;"/>
				</td>

				<td nowrap="nowrap">
					<input type="text" name="dashboardLabel_${transitions.transition.id}" id="dashboardLabelId_${transitions.transition.id}" value="${transitions.transition?.dashboardLabel}" style="width: 100px;"/>
				</td>

				<td nowrap="nowrap">
					<input type="text" name="transId_${transitions.transition.id}" id="transIdId_${transitions.transition.id}" value="${transitions.transition?.transId}" style="width: 60px;" maxlength="3"  onchange="validateField(this.value, this.id, 'transId')"/>
				</td>

				<td nowrap="nowrap">
					<g:select id="category_${transitions.transition.id}" name="category_${transitions.transition.id}" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(net.transitionmanager.task.AssetComment).category.inList}" optionValue="${{ e -> e.capitalize() }}" value="${transitions.transition.category}" noSelection="['':'Please select']" ></g:select>
				</td>

				<td nowrap="nowrap">
				<g:select from="${roles}" id="role_${transitions.transition.id}" name="role_${transitions.transition.id}"
					optionValue="${{it.description}}"
					value="${transitions.transition?.role?.id ? transitions.transition?.role?.id : 'PROJ_MGR'  }" optionKey="id" />
				</td>
				<td nowrap="nowrap">
					<input type="text" name="duration_${transitions.transition.id}" id="durationId_${transitions.transition.id}" value="${transitions.transition?.duration}"  style="width: 50px;" maxlength="4"/>
				</td>
				<td nowrap="nowrap">
				<g:if test="${transitions.donotDelete}">
					<a href="javascript:deleteWorkflowStep(${workflow.id}, ${transitions.transition.id})">
						<g:if test="${transitions.isExist}">
							Delete Step and History
						</g:if>
						<g:else>
							Delete
						</g:else>
					</a>
				</g:if>
				</td>
			</tr>
		</g:each>
		</g:if>
		<g:else>
		<tr><td colspan="8" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</g:form>
</div>
</div>

<div class="buttons" style="margin-left: 10px;margin-right: 10px;display: none;" id="editWorkflowActionButtons">
	<g:form action="workflowList">
		<input type="hidden" name="workflow" value="${workflow?.id}" />
		<span class="button"><input type="button" class="save" value="Update" onclick="validateAndSubmitUpdateForm()"/></span>
		<span class="button"><input type="button" class="create" onclick="addStep('edit');" value="Add Step" /></span>
		<span class="button"><input type="submit" class="cancel" onclick="return confirm('Are you sure?')" value="Cancel" /></span>
	</g:form>
</div>
<div id="copyWorkflowDialog" class="static-dialog" title="Copy Workflow" style="display:none;">
	<g:form action="createWorkflow" onsubmit="return checkInputData()">
		<input type="hidden" name="workflow" value="${workflow?.id}" />
          <div class="dialog">
            <table>
              <tbody>
              <tr>
				<td colspan="2"><div class="required"></div> </td>
				</tr>
              	<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="password">Workflow:&nbsp;</label>
					</td>
                    <td valign="top" class="value">
						<input type="text" name="process" id="processId" value=""/>
					</td>
				</tr>
              </tbody>
            </table>
          </div>
		<div class="buttons">
			<span class="button"><input type="submit" class="create" value="Copy" /></span>
			<span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#copyWorkflowDialog').dialog('close');"/></span>
		</div>
    </g:form>
    <div style="display: none;" >
		<g:select id="createCategory" name="createCategory" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(net.transitionmanager.task.AssetComment).category.inList}" optionValue="${{ e -> e.capitalize() }}" noSelection="['':'Please select']" ></g:select>
    </div>
    <div style="display: none;" >
    		 <g:select id="addRole" name="addRole" from="${roles}"  optionValue="${{it.description}}"
					value="PROJ_MGR" optionKey="id" />
    </div>
</div>
</div>
<script type="text/javascript">
$(document).ready(function() {
	$("#copyWorkflowDialog").dialog({ autoOpen: false })
})
/*=========================================
 * redirect to steps roles form
 *========================================*/
/*function showWorkflowRoles( workflowTransitionId ){
	$("#workflowTransitionId").val( workflowTransitionId );
	$("form[name=workflowRolesForm]").submit();
}*/
/*=========================================
 * show workflow steps edit form
 *========================================*/
function editWorkflowList(){
	$("#editWorkflowList").show()
	$("#editWorkflowActionButtons").show()
	$("#showWorkflowList").hide()
	$("#showWorkflowActionButtons").hide()
	$("#additionalStepsId").val(0)
	$("#currentStepsId").val("${workflowTransitionsList.size()}")
}
if(showOption == "edit"){editWorkflowList()}
/*=========================================
 * Validate workflow steps update form before submit
 *========================================*/
function validateAndSubmitUpdateForm(){
	if($(".field_error").length > 0){
		alert("Input entry problem. Please correct highlighted fields before saving")
	} else {
		$("form[name=updateWorkflowStepsForm]").submit();
	}
}
/*=========================================
 * Validate field when changed
 *========================================*/
function validateField(value, objId, field){
	var intRegExp = /^ *[0-9]+ *$/
	if(field == "Code" || field == "Name" ){
		if(!value) {
			$("#"+objId).addClass('field_error')
			$("#"+objId).attr("title",field+"should not be blank")
		} else {
			$("#"+objId).removeClass('field_error')
			$("#"+objId).removeAttr("title")
		}
	} else if(field == "transId") {
		if(!value) {
			$("#"+objId).addClass('field_error')
			$("#"+objId).attr("title","Sequence should not be blank")
		} else if(!intRegExp.test(value)){
			$("#"+objId).addClass('field_error')
			$("#"+objId).attr("title","Sequence should be numaric")
		} else {
			$("#"+objId).removeClass('field_error')
			$("#"+objId).removeAttr("title")
		}
	} else if(field == "predecessor"){
		if(value && !intRegExp.test(value)){
			$("#"+objId).addClass('field_error')
			$("#"+objId).attr("title","Start should be numaric")
		} else {
			$("#"+objId).removeClass('field_error')
			$("#"+objId).removeAttr("title")
		}
	}
}
/*===============================================
 * Add new step row
 *============================================*/
function addStep( type ){
	var additionalSteps = parseInt($("#additionalStepsId").val()) + 1
	var currentSteps = parseInt($("#currentStepsId").val())
	var cssClass = currentSteps % 2 == 0 ? 'odd' : 'even'
	var stepRow = "<tr class="+cssClass+">"+
						"<td><input type='text' class='field_error' name='code_"+additionalSteps+"' id='codeId_"+additionalSteps+"' onchange=\"validateField(this.value, this.id, 'Code' )\" /></td>"+
						"<td><input type='text' class='field_error' name='name_"+additionalSteps+"' id='nameId_"+additionalSteps+"' onchange=\"validateField(this.value, this.id, 'Name' )\" /></td>"+
						"<td><input type='text' name='dashboardLabel_"+additionalSteps+"' id='dashboardLabelId_"+additionalSteps+"' /></td>"+
						"<td><input type='text' class='field_error' name='transId_"+additionalSteps+"' id='transIdId_"+additionalSteps+"'  style='width: 60px;' maxlength='3'  onchange=\"validateField(this.value, this.id, 'transId')\"/></td>"+
						"<td><select id='category_"+additionalSteps+"' name='category_"+additionalSteps+"'>"+
								$('#createCategory').html()+
							"</select></td>"+
						"<td><select id='role_"+additionalSteps+"' name='role_"+additionalSteps+"'>"+
								$('#addRole').html()+
							"</select></td>"+
						"<td><input type='text' name='duration_"+additionalSteps+"' id='durationId_"+additionalSteps+"'   style='width: 50px;' maxlength='7'/></td>"+
					"<tr>"
	$("#additionalStepsId").val(additionalSteps)
	$("#"+type+"WorkflowStepsTbody").append(stepRow)
	$("#currentStepsId").val(additionalSteps + 1)
	$("#codeId_"+additionalSteps).focus()
}
/****************************************
 * Copy workflow to anther workflow
 ***************************************/
function openWorkflowDialog(){
	$('#copyWorkflowDialog').dialog('option', 'modal', 'auto');
	$('#copyWorkflowDialog').dialog('open');
}
function checkInputData(){
	var process = $("#processId").val()
	var returnVal = true
	if( !process ) {
		alert("Workflow should not be blank ")
		returnVal = false
	}
	return returnVal
}
function deleteWorkflowStep(workflowId, transitionId) {
	window.location.href = tdsCommon.createAppURL('/workflow/deleteTransitionFromWorkflow/' + transitionId + '?workflow=' + workflowId)
	return false;
}
</script>
<script>
	currentMenuId = "#adminMenu";
	$('.menu-list-workflows').addClass('active');
	$('.menu-parent-admin').addClass('active');
</script>
</body>
</html>
