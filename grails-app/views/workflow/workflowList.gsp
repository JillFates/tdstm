<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="companyHeader" />
<title>Workflow</title>
<script type="text/javascript"> var showOption = 'show'</script>
</head>
<body>
<div class="body">

<div class="steps_table">
	<span class="span"><b>Workflow</b></span>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px;margin: 0px 10px 10px 10px;">
	<span class="menuButton"><g:link class="list" action="home">Workflow List</g:link></span>
</div>
<div>
<table border="0" style="width: 400px;margin: 0px 10px 10px 20px; ">
	<tr class="prop">
		<td valign="top"  class="name">Workflow:</td>
		<td valign="top"  class="value">${workflow?.process}</td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Created On:</td>
		<td valign="top"  class="value"><tds:convertDateTime date="${workflow?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Update On:</td>
		<td valign="top"  class="value"><tds:convertDateTime date="${workflow?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Updated By:</td>
		<td valign="top"  class="value">${workflow?.updateBy}</td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Used On:</td>
		<td valign="top"  class="value">
		<g:if test="${workflow?.process}">
			<table><ul>
				<g:each in="${Project.findAllByWorkflowCode(workflow?.process)}" var="project">
				<tr>
				<li><g:link controller="project" action="show" id="${project.id}">${project?.name}</g:link></li>
				</tr>
				</g:each>
				</ul>
			</table>
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
	<div class="errors" style="padding-left:30px; background: url('../images/skin/exclamation.png') no-repeat scroll 8px 0 transparent"> Field with value ${flash.message} must be unique</div>
	<script type="text/javascript">showOption = 'edit'</script>
</g:if>
<div class="required"> Fields marked ( * ) are mandatory </div>
<br/>
<div class="list" style="border: 1px solid #5F9FCF; margin-left: 10px;margin-right: 10px;">
<div id="showWorkflowList">
<table>
	<thead>
		<tr>
			
			<th class="sortable">Step<span style="color: red">*</span></th>
			
			<th class="sortable">Label<span style="color: red">*</span></th>
			
			<th class="sortable">Dashboard Label</th>
			
			<th class="sortable">Sequence<span style="color: red">*</span></th>
			
			<th class="sortable">Type<span style="color: red">*</span></th>
			
			<th class="sortable">Start</th>
			
			<th class="sortable">Color</th>
			
			<th class="sortable">Header</th>
			
		</tr>
	</thead>
	<tbody>
		<g:if test="${workflowTransitionsList}">
		<g:each in="${workflowTransitionsList}" status="i" var="transitions">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="showWorkflowRoles('${transitions.id}')">

				<td nowrap="nowrap">${transitions?.code}</td>
				
				<td nowrap="nowrap">${transitions?.name}</td>
				
				<td nowrap="nowrap">${transitions?.dashboardLabel}</td>
				
				<td nowrap="nowrap">${transitions?.transId}</td>
				
				<td nowrap="nowrap"><g:message code="workflow.type.${transitions?.type}" /></td>

				<td nowrap="nowrap">${transitions?.predecessor}</td>

				<td nowrap="nowrap">${transitions?.color}</td>
				
				<td nowrap="nowrap">${transitions?.header}</td>

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
			
			<th class="sortable">Step<span style="color: red">*</span></th>
			
			<th class="sortable">Label<span style="color: red">*</span></th>
			
			<th class="sortable">Dashboard Label</th>
			
			<th class="sortable">Sequence<span style="color: red">*</span></th>
			
			<th class="sortable">Type<span style="color: red">*</span></th>
			
			<th class="sortable">Start</th>
			
			<th class="sortable">Color</th>
			
			<th class="sortable">Header</th>
			
		</tr>
	</thead>
	<tbody id="editWorkflowStepsTbody">
		<g:if test="${workflowTransitionsList}">
		<g:each in="${workflowTransitionsList}" status="i" var="transitions">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td nowrap="nowrap">
					<input type="text" name="code_${transitions.id}" id="codeId_${transitions.id}" value="${transitions?.code}" onchange="validateField(this.value, this.id, 'Code')"/>
				</td>
				
				<td nowrap="nowrap">
					<input type="text" name="name_${transitions.id}" id="nameId_${transitions.id}" value="${transitions?.name}"  onchange="validateField(this.value, this.id, 'Name')"/>
				</td>
				
				<td nowrap="nowrap">
					<input type="text" name="dashboardLabel_${transitions.id}" id="dashboardLabelId_${transitions.id}" value="${transitions?.dashboardLabel}"/>
				</td>
				
				<td nowrap="nowrap">
					<input type="text" name="transId_${transitions.id}" id="transIdId_${transitions.id}" value="${transitions?.transId}" style="width: 60px;" maxlength="3"  onchange="validateField(this.value, this.id, 'transId')"/>
				</td>
				
				<td nowrap="nowrap">
					<g:select id="typeId_${transitions.id}" name="type_${transitions.id}" from="${transitions.constraints.type.inList}" value="${transitions.type}" valueMessagePrefix="workflow.type"></g:select>
				</td>

				<td nowrap="nowrap">
					<input type="text" name="predecessor_${transitions.id}" id="predecessorId_${transitions.id}" value="${transitions?.predecessor}"  style="width: 60px;" maxlength="3"  onchange="validateField(this.value, this.id, 'predecessor')"/>
				</td>

				<td nowrap="nowrap">
					<input type="text" name="color_${transitions.id}" id="colorId_${transitions.id}" value="${transitions?.color}"/>
				</td>
				
				<td nowrap="nowrap">
					<input type="text" name="header_${transitions.id}" id="headerId_${transitions.id}" value="${transitions?.header}"  style="width: 60px;" maxlength="7"/>
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
<div class="buttons" style="margin-left: 10px;margin-right: 10px;" id="showWorkflowActionButtons"> 
	<g:form action="workflowRoles" name="workflowRolesForm">
		<span class="button">
			<input type="hidden" name="workflowTransition" id="workflowTransitionId">
		</span>
	</g:form>
	<g:form onsubmit="return false">
    	<input type="hidden" name="id" value="${workflow?.id}" />
        <span class="button"><input type="button" class="edit" value="Edit" onclick="editWorkflowList()"/></span>
        <span class="button"><g:actionSubmit class="delete" onclick="return confirm('WARNING: Deleting this Workflow will remove any Projects and any related data?');" value="Delete" /></span>
	</g:form>
</div>

<div class="buttons" style="margin-left: 10px;margin-right: 10px;display: none;" id="editWorkflowActionButtons"> 
	<g:form action="workflowList">
		<input type="hidden" name="workflow" value="${workflow?.id}" />
		<span class="button"><input type="button" class="save" value="Update" onclick="validateAndSubmitUpdateForm()"/></span>
	    <span class="button"><input type="submit" class="delete" onclick="return confirm('Are you sure?')" value="Cancel" /></span>
	    <span class="button"><input type="button" class="create" onclick="addStep('edit');" value="Add Step" /></span>
    </g:form>
</div>
</div>
<script type="text/javascript">
/*=========================================
 * redirect to steps roles form
 *========================================*/
function showWorkflowRoles( workflowTransitionId ){
	$("#workflowTransitionId").val( workflowTransitionId );
	$("form[name=workflowRolesForm]").submit();
}
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
						"<td><select id='typeId_"+additionalSteps+"' name='type_"+additionalSteps+"'>"+
								"<option value='process'>Process</option>"+
								"<option value='boolean'>Boolean</option>"+
							"</select></td>"+
						"<td><input type='text' name='predecessor_"+additionalSteps+"' id='predecessorId_"+additionalSteps+"'  style='width: 60px;' maxlength='3'  onchange=\"validateField(this.value, this.id, 'predecessor')\"/></td>"+
						"<td><input type='text' name='color_"+additionalSteps+"' id='colorId_"+additionalSteps+"' /></td>"+
						"<td><input type='text' name='header_"+additionalSteps+"' id='headerId_"+additionalSteps+"'   style='width: 60px;' maxlength='7'/></td>"+
					"<tr>"
	$("#additionalStepsId").val(additionalSteps)
	$("#"+type+"WorkflowStepsTbody").append(stepRow)
	$("#currentStepsId").val(additionalSteps + 1)
}
</script>
</body>
</html>
