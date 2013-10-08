<%@page import="com.tds.asset.TaskDependency;"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Task Details</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
<meta name="viewport" content="height=device-height,width=220" />
	
<script type="text/javascript">
        window.addEventListener('load', function(){
                setTimeout(scrollTo, 0, 0, 1);
        }, false);
</script>
</head>
<body>
	<a name="top"></a>
	<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" id="mainbody" style="width: 100%;">
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div class="message"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>
	<g:form name="issueUpdateForm" controller="task" action="update">
		<a name="comments"></a>
		<input id="issueId" name="id" type="hidden" value="${assetComment.id}" />
		<input id="redirectTo" name="redirectTo" type="hidden" value="taskList" />
		<table >
			<tr>
				<td class="heading" colspan="2"><a class="heading" href="#comments">Task details:</a></td>
			</tr>
			<tr>
				<td colspan="2">
				</td>
			</tr>		
			<tr>
				<td colspan="2">
					<input type="text" title="Edit Comment..." id="editComment_${assetComment.id}" name="comment" value="${assetComment.comment}" style="width:95%" />
				</td>
			</tr>
			<g:if test="${assetComment.taskDependencies?.size() > 0 }">		
			<tr>
				<td valign="top" class="name"><label>Predecessors:</label></td>
				<td><span style="width: 50%">
					<g:each in="${assetComment.taskDependencies}" var="task">
					<span class="${task.predecessor?.status ? 'task_'+task.predecessor?.status?.toLowerCase() : 'task_na'}">
						${task.predecessor.category}&nbsp;&nbsp;${task.predecessor}
							</span>
							</g:each>
					</span>
				</td>
			</tr>
			</g:if>
			<g:if test="${successor.size() > 0}">	
			<tr>
				<td valign="top" class="name"><label>Successors:</label></td>
				<td>	
					<span  style="width: 50%">
					<g:each in="${successor}" var="task">
					<span class="${task.assetComment?.status ? 'task_'+task.assetComment?.status?.toLowerCase() : 'task_na'}">
						${task.assetComment.category}&nbsp;&nbsp;${task.assetComment}
					</span>
					</g:each>
					</span>
				</td>
			</tr>
			</g:if>
			<tr class="prop" id="teamId"  >
				<td valign="top" class="name"><label for="team">Team:</label></td>
				<td valign="top" class="value" id="team_${assetComment.id}" nowrap="nowrap">${assetComment.role}</td>
			</tr>
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" id="assignedToEditTdId" >
					${assignToSelect}
				</td>
			</tr> 
			<g:if test="${assetComment.estStart}">	
				<tr class="prop issue" id="estStartShowId"  >
					<td valign="top" class="name"><label for="estStart">Est. Start:</label></td>
					<td valign="top" class="value" id="estStartShowId_${assetComment.id}" nowrap="nowrap">
					<tds:convertDate date="${assetComment.estStart}" format="M/d kk:mm" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
				</tr>
			</g:if>
			<g:if test="${assetComment.estFinish}">	
				<tr class="prop issue" id="estFinishShowId"  >
					<td valign="top" class="name"><label for="estFinish">Est. Finish:</label></td>
					<td valign="top" class="value" id="estFinishShowId_${assetComment.id}"  nowrap="nowrap" >
					<tds:convertDate date="${assetComment.estFinish}" format="M/d kk:mm" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
					</td>
				</tr>
			</g:if>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value"><g:select id="categoryEditId_${assetComment.id}" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"></g:select>
				<g:if test="${assetComment.moveEvent}">
		   		  <span style="margin-left:60px;">Move Event:</span>
		   		  <span style="margin-left:10px;">${assetComment?.moveEvent.name}</span>
		   		</g:if>
				</td>
			</tr>
			<tr>
				<g:if test="${assetComment.assetEntity}">
		   			<td>Asset:</td><td>&nbsp;${assetComment?.assetEntity.assetName}</td>
		   		</g:if>
		   	</tr>
		   	<tr class="prop">
				<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
				<td valign="top" class="value"><span id="categoryEditId">${assetComment?.createdBy} on 
				<tds:convertDate date="${assetComment?.dateCreated}" format="M/d" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span></td>
			</tr>
			<tr class="prop" >
				<td valign="top" class="name">
					<label for="status">Status:</label>
					<input id="currentStatus_${assetComment.id}" name="currentStatus" type="hidden" value="${assetComment.status}" />
				</td>
				<td id="statusEditTrId_${assetComment.id}" >
					<g:select id="statusEditId_${assetComment.id}" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="${assetComment.status}"
					noSelection="['':'please select']" onChange="showResolve()" ${statusWarn==1 ? 'disabled="true"' : ''}></g:select>
				</td>	
			</tr>	
			<tr class="prop">
				<td valign="top" class="name" colspan="2"><label for="notes">Previous Notes:</label></td>
			</tr>
			<tr class="prop">
			<td valign="top" class="value" colspan="2"><div id="previousNote" >
				 <table style="table-layout: fixed; width: 100%;border: 1px solid green;">
                   <g:each in="${notes}" var="note" status="i" >
                    <tr>
	                    <td>${note[0]}</td>
	                    <td>${note[1]}</td>
                        <td style="word-wrap: break-word">${note[2]}</td>
                     </tr>
                   </g:each>
				 </table>
				</div></td>
			</tr>
		    <tr class="prop" id="noteId_${assetComment.id}">
				<td valign="top" class="name" colspan="2"><label for="notes">Note:</label></td>
			</tr>
			<tr class="prop" id="noteTrId_${assetComment.id}"><td valign="top" class="value" colspan="2">
				   <textarea cols="130" rows="4" id="noteEditId_${assetComment.id}" name="note" style="width:100%;padding:0px;"></textarea>
				</td>
			</tr>
			<tr class="prop" id="resolutionId_${assetComment.id}" style="display: none;">
				<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			</tr> 
			<tr class="prop" id="resolutionTrId_${assetComment.id}" style="display: none">
				<td valign="top" class="value" colspan="2">
					<textarea cols="130" rows="4" style="width:100%;padding:0px;" id="resolutionEditId_${assetComment.id}" name="resolution" >${assetComment.resolution}</textarea>
				</td>
			</tr> 
			<g:if test="${assetComment.resolvedBy}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved By:</label></td>
					<td valign="top" class="value">
						<span id="resolvedByTd" >${assetComment.resolvedBy} on 
						<tds:convertDate date="${assetComment?.dateResolved}" format="M/d" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span>
					</td>
				</tr> 
			</g:if>
			
			<tr>
			    <td class="buttonR" >
			    	<input type="button" value="Cancel" onclick="cancelButton(${assetComment.id})" />
			    </td>
				<td class="buttonR" style="text-align:right;padding: 2px 6px;">
				 <g:if test="${permissionForUpdate==true}">
					<input type="button" value="Update Task" onclick="validateCommentMobile(${assetComment.id})" />
				</g:if>
				<g:else>
				   <input type="button" value="Update Task" disabled="disabled"/>
				</g:else>
				</td>
			</tr>	
		</table>
</g:form>

		<%--<div class="clear" style="margin:4px;"></div>
		<a name="detail"></a>
	 	<div>

			<table style="width:420px;">
			<tr>
				<td class="heading"><a href="#detail">Details</a></td>
				<td><span style="float:right;"><a href="#top">Top</a></span></td>
			</tr>
			<tr><td colspan=2>

				<dl>
				<dt>Comment:</dt><dd>&nbsp;${assetComment.comment}</dd>
				<dt>Assigned to:</dt><dd>&nbsp;${assetComment?.assignedTo}</dd>
				<dt>Due Date:</dt><dd>&nbsp;<tds:convertDate date="${assetComment?.dueDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></dd>
				<dt>Created by:</dt><dd>&nbsp;${assetComment?.createdBy} at ${assetComment?.dateCreated} </dd>
				<dt>Comment Type:</dt><dd>&nbsp;${assetComment?.commentType}</dd>
		   		<dt>Category:</dt><dd>&nbsp;${assetComment?.category}</dd>
		   		<g:if test="${assetComment.assetEntity}">
		   		  <dt>Asset Entity:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
		   		</g:if>
		   		<g:if test="${assetComment.moveEvent}">
		   		  <dt>Event:</dt><dd>&nbsp;${assetComment?.moveEvent.name}</dd>
		   		</g:if>
		   		<dt>Status:</dt><dd>&nbsp;${assetComment?.status}</dd>
		   		<dt>Resolution:</dt><dd>&nbsp;${assetComment?.resolution}</dd>
		   		<dt>Resolved At:</dt><dd>&nbsp;${assetComment?.dateResolved}</dd>
				<dt>Resolved By:</dt><dd>&nbsp;${assetComment?.resolvedBy}</dd>  			   	
			
				</dl>
		</tr>
		</table>
		</div>
		--%><div class="clear" style="margin:4px;"></div>
		<div style="margin:2px;" class="reset" ></div>
		<a name="detail" ></a>
		<g:if test="${assetComment?.assetEntity}">
	 		<div style="float: left;width:100%">
				<table style=" margin-bottom: 30px;">
				<tr>
					<td class="heading"><a href="#detail">Asset Highlights</a></td>
					<td><span style="float:right;"><a href="#top">Top</a></span></td>
			</tr>
			<tr class="prop"><td colspan=2>
			<dl>
	            	<g:if test="${assetComment?.assetEntity?.assetType=='Application'}">
	                <dt>Application Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>Validation:</dt><dd>&nbsp;${assetComment?.assetEntity.validation}</dd>
					<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
					<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
               </g:if>
	                <g:elseif test="${assetComment?.assetEntity?.assetType=='Database'}">
                    <dt>Database Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>DB Size:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>DB Format:</dt><dd>&nbsp;${assetComment?.assetEntity.dbFormat}</dd>
					<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
                </g:elseif>
	                <g:elseif test="${assetComment?.assetEntity?.assetType=='Files'}">
                    <dt>Storage Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>Storage Size:</dt><dd>&nbsp;${assetComment?.assetEntity.size}</dd>
					<dt>Storage Format:</dt><dd>&nbsp;${assetComment?.assetEntity.fileFormat}</dd>
					<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
                </g:elseif>
                <g:else>
						<dt>Asset Tag:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetTag}</dd>
						<dt>Asset Name:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetName}</dd>
						<dt>Model:</dt><dd>&nbsp;${assetComment?.assetEntity?.model}</dd>
						<dt>Serial #:</dt><dd>&nbsp;${assetComment?.assetEntity?.serialNumber}</dd>
					<dt>Current Loc/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRack}/${assetComment?.assetEntity.sourceRackPosition}</dd>
					<dt>Target Loc/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRack}/${assetComment?.assetEntity.targetRackPosition}</dd>
					<dt>Source Room:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRoom}</dd>
					<dt>Target Room:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRoom}</dd>
					<g:if test="${location == 'source'}">			   
				   		<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
						<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>  			   	
					</g:if>
					<g:else>				
       					<dt>Truck:</dt><dd>&nbsp;${assetComment?.assetEntity.truck}</dd>
				   		<dt>Cart/Shelf:</dt><dd>&nbsp;${assetComment?.assetEntity.cart}/${assetComment?.assetEntity.shelf}</dd>
				   		<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
						<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>  			   	
					</g:else>
				</g:else>
				<g:if test="${project.customFieldsShown > 0}">
					 <g:each in="${ (1..project.customFieldsShown) }" var="i">
						 <g:if test="${assetComment?.assetEntity.('custom'+i)}">
							<dt>${project.('custom'+i) ?: 'Custom'+i }:</dt>
							<dd>&nbsp;<tds:textAsLink  text="${assetComment?.assetEntity.('custom'+i)}" target="_new"/></dd>
						 </g:if>
					 </g:each>
				</g:if>
			</dl>
		</tr>
		</table>
		
		</div>
		</g:if>
</div>
<script type="text/javascript">
 $( function() {
	 var objId = ${assetComment.id}
	 var updatePerm = ${permissionForUpdate}
	 if($('#statusEditId_'+objId).val()=='Completed'){
	       $('#noteId_'+objId).hide()
	       $('#noteTrId_'+objId).hide()
	       $('#resolutionId_'+objId).show()
	       $('#resolutionTrId_'+objId).show()
	 }
	
	// Disable the AssignTo SELECT if user doensn't have permission
	var editAssignTo=${assignmentPerm ? 'true' : 'false'}
	if (!editAssignTo) {
		$('#assignedToEditId_'+objId).attr('disabled', 'disabled');
	}
	
	 /*
	 var updatePerm = ${permissionForUpdate}
	 $("#editComment_"+objId).keypress(function(e){
		 if(updatePerm && e.keyCode==13){e.preventDefault(); validateCommentMobile(objId); }
		 else if(!updatePerm && e.keyCode==13){e.preventDefault(); }
	 });
	*/
 });

 function showResolve(){
  if($('#statusEditId_'+${assetComment.id}).val()=='Completed'){
      $('#noteId_'+${assetComment.id}).hide()
      $('#resolutionId_'+${assetComment.id}).show()
       $('#noteTrId_'+${assetComment.id}).hide()
      $('#resolutionTrId_'+${assetComment.id}).show()
  }else{
	   $('#noteId_'+${assetComment.id}).show()
      $('#resolutionId_'+${assetComment.id}).hide()
      $('#noteTrId_'+${assetComment.id}).show()
      $('#resolutionTrId_'+${assetComment.id}).hide()
  }
 }
 function formatDueDate(input){
	 var currentDate = ""
	 if(input){
		  var datePart = input.match(/\d+/g),
		  year = datePart[0].substring(0), // get only two digits
		  month = datePart[1], day = datePart[2];
	      currentDate = month+'/'+day+'/'+year;
	 }
   return currentDate
 }
 function validateCommentMobile(objId){
	var status = $('#statusEditId_'+objId).val()
	var params = {'comment':$('#editComment_'+objId).val(), 'resolution':$('#resolutionEditId_'+objId).val(), 
					'category':$('#categoryEditId_'+objId).val(), 'assignedTo':$('#assignedToEditId_'+objId).val(),
					'status':$('#statusEditId_'+objId).val(),'currentStatus':$('#currentStatus_'+objId).val(), 
					'note':$('#noteEditId_'+objId).val(),'id':objId,'view':'myTask', 'tab': $('#tab_m').val(),
					'dueDate':$('#dueDateEdit_'+objId).val()
				}
		 jQuery.ajax({
				url: '../task/update',
				data: params,
				type:'POST',
				success: function(data) {
					     $('#myTaskListMobile').html(data)
					     $('#showStatusId_'+objId).show()
						 $('#issueTrId_'+objId).attr('onClick','hideStatus('+objId+',"'+status+'")')
						 if(status=='Started'){
						 	$('#started_'+objId).hide()
						 }
						 B1.Start(60);
				}
			});
 }
 function truncate( text ){
		var trunc = text
		if(text){
			if(text.length > 50){
				trunc = trunc.substring(0, 50);
				trunc += '...'
			}
		}
		return trunc;
 }
</script>
</body>
</html>
