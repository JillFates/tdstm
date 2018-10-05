<%@page import="net.transitionmanager.domain.Project" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<asset:stylesheet href="css/dashboard.css" />
<title>Workflow Roles</title>
</head>
<body>
<div class="body">
<g:form name="myForm" action="updateWorkflowRoles" >
<div class="steps_table" style="text-align: left;">
	<span class="span"><b>Workflow Roles</b></span>

<div class="buttons" style="margin-left: 10px;margin-right: 10px;text-align: left;">
	<input type="hidden" name="workflow" value="${workflow?.id}" />
    <input type="hidden" name="currentStatus" value="${workflowTransition?.id}" />
	<span class="button"><input type="submit" class="edit" value="Update" /></span>
	<span class="button"><input type="submit" class="delete" onclick="return setAction()" value="Cancel" /></span>
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
	<tr class="prop">
		<td valign="top"  class="name">Current Status:</td>
		<td valign="top"  class="value">${workflowTransition?.name}</td>
	</tr>
</table>
</div>
<div id="tableContainer" class="list" style="margin-left: 5px;margin-right: 10px;">
<table cellpadding="0" cellspacing="0" style="border:1px solid #63A242;width: 600px;">
	<thead>
		<tr>
			<th style="padding: 5px 6px">Transitions</th>

			<g:each status="i" in="${swimlanes}"  var="swimlane">
				<th> <input type="text" style= " border:none ;  width: 100px; background-color:inherit; padding: 5px 6px; font-weight: bold; "  value='${swimlane?.actorId}' readOnly=true id='textBox${i}' class="name"  onclick= "changeTitle(this.id,'button${i}')" ></input>
				     <input type="hidden" name="id" id="workFlow${i}" value="${workflow?.id}" />
				     <input type="hidden" name="name" id="name${i}" value="${swimlane?.name}" />
				     <input type='button' id='button${i}' value="save" style='display:none' onclick="saveWorkflowId('textBox${i}',this.id,'workFlow${i}','name${i}')"/>
				     </th>
			</g:each>

		</tr>
	</thead>
	<tbody id="workflowRolesBody">
		<g:if test="${workflowTransitionsList}">
		<g:each in="${roleWiseTransitions}" var="roleWiseTransition" status="i">
			<tr id="transition_${roleWiseTransition.transition?.id}"  class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<td class="name"  nowrap="nowrap">
				<g:link action="workflowRoles" params="[workflowTransition:roleWiseTransition.transition?.id]" style="font-weight: ${workflowTransition?.id == roleWiseTransition?.transition.id ? 'bold' : 'normal'}; color: ${roleWiseTransition?.transition.header ? roleWiseTransition?.transition.header : roleWiseTransition?.transition.type == 'boolean' ? '#FF8000' : '#336600'}">
				${roleWiseTransition?.transition?.name}
				</g:link>
			</td>
			<g:each in="${roleWiseTransition.transitionsMap}" var="transitionMap">
				<td id="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.transId}" nowrap="nowrap">
					<g:if test="${workflowTransition?.id != roleWiseTransition?.transition.id }">
						<g:if test="${transitionMap.workflowTransitionMap}">
							<input type="checkbox" name="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" checked="checked"/>
							<input type="text" name="flag_${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" value="${transitionMap.workflowTransitionMap.flag}" />
						</g:if>
						<g:else>
							<input type="checkbox" name="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}"/>
							<input type="text" name="flag_${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" value=""/>
						</g:else>
					</g:if>
					<g:else>
						<input type="checkbox" name="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" disabled="disabled"/>
					</g:else>
				</td>
			</g:each>
			</tr>
		</g:each>
		</g:if>
		<g:else>
			<tr><td colspan="40" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</div>
</g:form>
</div>
<script type="text/javascript">

var buffer = "";
function setAction(){
	if(confirm('Are you sure?')){
		$("form").attr("action","workflowList")
		return true;
	} else {
		return false;
	}
}
function changeTitle(textBox,button){
  $('#'+textBox).attr('readOnly',false);
  $('#'+textBox).css('border','solid black 1px');
  $('#'+textBox).css('background','white');
  $('#'+button).css('display','inline','border','solid black 1px');
  $('#'+button).css('background','yellow');
}
function saveWorkflowId(textBox,button,workFlow,name){
  var actorId = $('#'+textBox).val();
  var workFlowId = $('#'+workFlow).val();
  var swimlaneName =$('#'+name).val();
  ${remoteFunction(controller:'workflow', action:'saveActorName', params:'\'actorId=\' + actorId +\'&workflow=\'+workFlowId +\'&swimlaneName=\'+swimlaneName',before:'setBuffer(actorId)', onSuccess: 'setHeader(textBox,button)')};
}
function setBuffer(actorId){
	buffer = actorId;
}
function setHeader(headerId, buttonId){
	 $('#'+headerId).val(buffer);
	 $('#'+headerId).css("background-color", "inherit");
	 $('#'+headerId).css("border", "none");
     $('#'+buttonId).css("display","none");
 }
</script>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
</body>
</html>
