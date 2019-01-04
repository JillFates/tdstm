<%@ page contentType="text/html"%>
<html>
<body> 
<div style="font:courier ">
	<table style="border: 1px solid green;font-family:courier;">
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="projectName"><b>Project:</b></label></td>
			<td valign="top" class="value" id="projectNameId" >${projectName ? projectName : ''}</td>
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="TaskNumber" style=""><b>Task #:</b></label></td>
			<td valign="top" class="value" id="dateCreatedId"  >
			<a href="${createLink(controller:'task',  action:'userTask', params:['id': assetComment.id],  absolute:"true")}">${assetComment.taskNumber}</a></td>
		</tr>
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="comment"><b>Task:</b></label></td>
			<td valign="top" class="value" colspan="2">
				<textarea cols="80" rows="4" id="commentTdId" readonly="readonly">${assetComment.comment}</textarea>
			</td>
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="assignedTo"><b>Assigned To:</b></label></td>
			<td valign="top" class="value" id="assignedToTdId" >${assignedTo ? assignedTo : 'Unassigned'}</td>
		</tr>
		<tr style="margin-top: 10px">
         <td valign="top" class="name" ><label for="dueDate"><b>Due Date:</b></label></td>
         <td valign="top" class="value" id="dueDatesId" ><tds:convertDate date="${assetComment.dueDate}" endian="${dateFormat}" /></td>
        </tr> 
        <tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="category"><b> Event/Phase</b></label></td>
			<td valign="top" class="value" id="categoryTdId" >${assetComment.moveEvent ?: ''} / ${assetComment.category}</td>
		</tr>
		<g:if test="${assetName}">
		<tr id = "assetShowId" class="prop" style="margin-top: 10px" >
			<td valign="top" class="name" ><label for="asset"><b>Related Asset:</b></label></td>
			<td valign="top" class="value" id="assetShowValueId">${assetName} </td>
		</tr>
       </g:if>
	   <tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="status"><b>Status:</b></label></td>
			<td valign="top" class="value" id="statusShowId" >${assetComment.status}</td>
	   </tr>
	   <tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="dateCreated" style=""><b>Created By:</b></label></td>
			<td valign="top" class="value" id="dateCreatedId"  >${createdBy} at ${dtCreated}</td>
	   </tr>
       <tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="previousNotes"><b>Previous Notes:</b></label></td>
			<td valign="top" class="value" >
				<div id="previousNotesShowId" > 
				<table style="border:1px solid green;margin-right: 40px">
				<g:each in="${notes}" var="note">
					<tr>
						<td>
							<tds:convertDate date="${note.dateCreated}" endian="${dateFormat}"/>
						</td>
						<td>${note.createdBy }</td>
						<td>${note.note }</td>
					</tr>
				</g:each>
				</table>
				</div>
			</td>
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name"  colspan="2"><a href="${createLink(controller:'task',  action:'listUserTasks',  absolute:"true")}">See my tasks</a></td>
	   </tr>
	</table>
	</div>
	</body>
	</html>
