<%@page import="com.tds.asset.TaskDependency;"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Task Details</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
</head>
<body>
	<a name="top"></a>
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" id="mainbody">
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red; font-size:15px"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>
	<g:form name="issueUpdateForm" controller="assetEntity" action="updateComment">
		<a name="comments"></a>
		<input id="issueId" name="id" type="hidden" value="${assetComment.id}" />
		<input id="redirectTo" name="redirectTo" type="hidden" value="taskList" />
		<table style="margin-left: -2px;">
			<tr>
				<td class="heading" colspan=2><a class="heading" href="#comments">Task details:</a></td>
			</tr>
			<tr>
			<td colspan="3">
			</td>
			</tr>		
			<tr>
				<td valign="top" class="name"><label for="comment">Task:</label></td>
				<td colspan="3">
				  <input type="text"  title="Edit Comment..." id="editComment_${assetComment.id}" name="comment" value="${assetComment.comment}" style="width: 500px"/>
				</td>
			</tr>	
			<tr>
				<td valign="middle" class="name"><label>Dependencies:</label></td>
				<td valign="top" class="name" colspan="3"><label>Predecessors:</label>&nbsp;&nbsp;
				<span style="width: 50%">
							<g:each in="${assetComment.taskDependencies}" var="task">
							<span class="${task.predecessor?.status ? 'task_'+task.predecessor?.status?.toLowerCase() : 'task_na'}" onclick="showAssetComment(${task.predecessor.id})">
								${task.predecessor.category}&nbsp;&nbsp;&nbsp;&nbsp;${task.predecessor}
							</span>
							</g:each>&nbsp;&nbsp;
				</span>
				<label>Successors:</label>
				<span  style="width: 50%">
							<g:each in="${successor}" var="task">
							<span class="${task.assetComment?.status ? 'task_'+task.assetComment?.status?.toLowerCase() : 'task_na'}" onclick="showAssetComment(${task.assetComment.id})">
								${task.assetComment.category}&nbsp;&nbsp;&nbsp;&nbsp;${task.assetComment}
							</span>
							</g:each>
				</span>
				</td>
			</tr>
		 	<% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;%>
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" id="assignedToEditTdId" style="width: 20%;" colspan="3" >
					<g:select id="assignedToEditId_${assetComment.id}" name="assignedTo" from="${partyList}" value="${assetComment.assignedTo.id}" optionKey="id" noSelection="['':'please select']"></g:select>
				</td>
			</tr> 
			<tr class="prop issue" id="estFinishShowId"  >
				<td valign="top" class="name"><label for="estFinish">Est.Finish:</label></td>
				<td valign="top" class="value" id="estFinishShowId_${assetComment.id}" colspan="3" nowrap="nowrap">${etFinish}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value" colspan="3"><g:select id="categoryEditId_${assetComment.id}" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"></g:select></td>
			</tr>
			<tr>
			<g:if test="${assetComment.assetEntity}">
		   		  <td>Asset:</td><td style="width: 1%">&nbsp;${assetComment?.assetEntity.assetName}</td>
		   		</g:if>
		   		<g:if test="${assetComment.moveEvent}">
		   		  <td style="width: 6%">Move Event:</td><td>${assetComment?.moveEvent.name}</td>
		   		</g:if>
		   	</tr>
		   	<tr class="prop">
				<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
				<td valign="top" class="value" colspan="3"><span id="categoryEditId">${assetComment?.createdBy} on <tds:convertDate date="${assetComment?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span></td>
			</tr>
			<tr class="prop" >
				<td valign="top" class="name">
					<label for="status">Status:</label>
					<input id="currentStatus_${assetComment.id}" name="currentStatus" type="hidden" value="${assetComment.status}" />
				</td>
				<td style="width: 20%;" id="statusEditTrId_${assetComment.id}" colspan="3">
					<g:select id="statusEditId_${assetComment.id}" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="${assetComment.status}"
					noSelection="['':'please select']"  onChange="showResolve()" ${statusWarn==1 ? 'disabled="true"' : ''}></g:select>
				</td>	
			</tr>				
			 <tr class="prop">
				<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
				<td valign="top" class="value" colspan="3"><div id="previousNote" style="width: 380px;">
				 <table style="table-layout: fixed; width: 100%;border: 1px solid green;" >
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
				<td valign="top" class="name"><label for="notes">Note:</label></td>
				<td valign="top" class="value" colspan="3">
				   <textarea cols="80" rows="4" id="noteEditId_${assetComment.id}" name="note" style="width:100%;padding:0px;"></textarea>
				</td>
			</tr>
			<tr class="prop" id="resolutionId_${assetComment.id}" style="display: none;">
				<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
				<td valign="top" class="value" colspan="3">
					<textarea cols="100" rows="4" style="width:100%;padding:0px;" id="resolutionEditId_${assetComment.id}" name="resolution" >${assetComment.resolution}</textarea>
				</td>
			</tr> 
			<g:if test="${assetComment.dateResolved}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved At:</label></td>
					<td valign="top" class="value" colspan="3">
						<span id="dateResolvedTd" ><tds:convertDate date="${assetComment?.dateResolved}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span>
					</td>
				</tr> 
			</g:if>
			<g:if test="${assetComment.resolvedBy}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved By:</label></td>
					<td valign="top" class="value" colspan="3">
						<span id="resolvedByTd" >${assetComment.resolvedBy}</span>
					</td>
				</tr> 
			</g:if>
			
			<tr>
			    <td class="buttonR" >
					<g:if test="${permissionForUpdate==true}">
						<input type="button" value="Update Task" onclick="validateComment(${assetComment.id})" />
					</g:if>
					<g:else>
						<input type="button" value="Update Task" disabled="disabled" />
					</g:else>
				</td>
				<td class="buttonR" colspan="3" style="text-align:right;padding: 5px 3px;">
					<input type="button" value="Cancel" onclick="cancelButton(${assetComment.id})" />
				</td>
			</tr>	
		</table>
</g:form>

		<%--<div class="clear" style="margin:4px;"></div>
		<a name="detail"></a>
	 	<div>

			<table style="width:420px;">
			<tr>
				<td class="heading"><a href="#detail">Asset Details</a></td>
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
		   		  <dt>Move Event:</dt><dd>&nbsp;${assetComment?.moveEvent.name}</dd>
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
		<a name="detail" ></a>
		<g:if test="${assetComment?.assetEntity}">
		 	<div style="float: left;width: 100%">
				<table style="float: left;margin-left: -2px;">
				<tr>
					<td class="heading"><a href="#detail">${assetComment?.assetEntity?.assetType} Details</a></td>
					<td><span style="float:right;"><a href="#top">Top</a></span></td>
				</tr>
				<tr><td colspan=2>
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
	                    <dt>File Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
						<dt>FIle Size:</dt><dd>&nbsp;${assetComment?.assetEntity.fileSize}</dd>
						<dt>File Format:</dt><dd>&nbsp;${assetComment?.assetEntity.fileFormat}</dd>
						<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
	                </g:elseif>
	                <g:else>
						<dt>Asset Tag:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetTag}</dd>
						<dt>Asset Name:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetName}</dd>
						<dt>Model:</dt><dd>&nbsp;${assetComment?.assetEntity?.model}</dd>
						<dt>Serial #:</dt><dd>&nbsp;${assetComment?.assetEntity?.serialNumber}</dd>
						<g:if test="${location == 'source'}">			   	
					   		<dt>Location:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceLocation}</dd>
					   		<dt>Room:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRoom}</dd>
					   		<dt>Rack/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRack}/${assetComment?.assetEntity.sourceRackPosition}</dd>
					   		<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
							<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>  			   	
						</g:if>
						<g:else>				
					   		<dt>Location:</dt><dd>&nbsp;${assetComment?.assetEntity.targetLocation}</dd>
					   		<dt>Room:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRoom}</dd>
					   		<dt>Rack/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRack}/${assetComment?.assetEntity.targetRackPosition}</dd>
					   		<dt>Truck:</dt><dd>&nbsp;${assetComment?.assetEntity.truck}</dd>
					   		<dt>Cart/Shelf:</dt><dd>&nbsp;${assetComment?.assetEntity.cart}/${assetComment?.assetEntity.shelf}</dd>
					   		<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
							<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>  			   	
						</g:else>
					</g:else>
				</dl>
			</tr>
			</table>
			
			</div>
		</g:if>
</div>
<script type="text/javascript">
$( function() {
	 if($('#statusEditId_'+${assetComment.id}).val()=='Completed'){
	       $('#noteId_'+${assetComment.id}).hide()
	       $('#resolutionId_'+${assetComment.id}).show()
	 }
});

 function showResolve(){
   if($('#statusEditId_'+${assetComment.id}).val()=='Completed'){
       $('#noteId_'+${assetComment.id}).hide()
       $('#resolutionId_'+${assetComment.id}).show()
   }else{
	   $('#noteId_'+${assetComment.id}).show()
       $('#resolutionId_'+${assetComment.id}).hide()
   }
 }
 function validateComment(objId){
	 var status = $('#statusEditId_'+${assetComment.id}).val()
	 var params = {   'comment':$('#editComment_'+objId).val(), 'resolution':$('#resolutionEditId_'+objId).val(), 
						 'category':$('#categoryEditId_'+objId).val(), 'assignedTo':$('#assignedToEditId_'+objId).val(),
						 'status':$('#statusEditId_'+objId).val(),'currentStatus':$('#currentStatus_'+objId).val(), 
						 'note':$('#noteEditId_'+objId).val(),'id':objId,'view':'myTask', 'tab': $('#tabId').val()
						}
		 jQuery.ajax({
				url: '../assetEntity/updateComment',
				data: params,
				type:'POST',
				success: function(data) {
					if (typeof data.error !== 'undefined') {
						alert(data.error);
					} else {
					     $('#myTaskList').html(data)
					     $('#showStatusId_'+objId).show()
						 $('#issueTrId_'+id).each(function(){
							$(this).removeAttr('onclick')
							$(this).unbind("click").bind("click", function(){
								hideStatus(objId,status)
						    });
						})
						 if(status=='Started'){
						 	$('#started_'+objId).hide()
						 }
						 B1.Restart(60);
					}
				},
				error: function(jqXHR, textStatus, errorThrown) {
					alert("An unexpected error occurred while attempting to update task/comment")
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
 <script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
</script>
</body>
</html>