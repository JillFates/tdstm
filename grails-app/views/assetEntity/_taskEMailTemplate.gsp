<%@ page contentType="text/html"%>
<html>
<body> 
<div style="font:courier ">
	<table style="border: 1px solid green;font-family:courier;">
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="dateCreated" style=""><b>Created By:</b></label></td>
			<td valign="top" class="value" id="dateCreatedId"  >${createdBy} at ${dtCreated}</td>
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="assignedTo"><b>Assigned To:</b></label></td>
			<td valign="top" class="value" id="assignedToTdId" >${assignedTo ? assignedTo : 'Unassigned'}</td>
		</tr>
      <tr style="margin-top: 10px">
         <td valign="top" class="name" ><label for="dueDate"><b>Due Date:</b></label></td>
         <td valign="top" class="value" id="dueDatesId" ><tds:convertDate date="${assetComment.dueDate}" /></td>
      </tr> 
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="commentType"><b>Comment Type:</b></label></td>
			<td valign="top" class="value" id="commentTypeTdId" >${assetComment.commentType}</td>
			
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="category"><b>Category:</b></label></td>
			<td valign="top" class="value" id="categoryTdId" >${assetComment.category}</td>
			
		</tr>
		<g:if test="${assetName}">
		<tr id = "assetShowId" class="prop" style="margin-top: 10px" >
			<td valign="top" class="name" ><label for="asset"><b>Asset:</b></label></td>
			<td valign="top" class="value" id="assetShowValueId">${assetName} </td>
		</tr>
      </g:if>
      <g:if test="${moveEvent}">
      <tr id = "moveEventId" class="prop" style="margin-top: 10px" >
         <td valign="top" class="name" ><label for="moveEvent"><b>Move Event:</b></label></td>
         <td valign="top" class="value" id="moveEventShowValueId">${moveEvent} </td>
      </tr>
      </g:if>
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="comment"><b>Comment:</b></label></td>
			<td valign="top" class="value" colspan="2">
				<textarea cols="80" rows="4" id="commentTdId" readonly="readonly">${assetComment.comment}</textarea>
			</td>
		</tr>
	
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="previousNotes"><b>Previous Notes:</b></label></td>
			<td valign="top" class="value" >
				<div id="previousNotesShowId" > 
				<table style="border:1px solid green;margin-right: 40px">
				<g:each in="${notes}" var="note">
				<tr>
				<td><tds:convertDate date="${note.dateCreated}"
				     timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
				</td>
				<td>${note.createdBy }</td>
				<td>${note.note }</td>
				</tr>
				</g:each>
				</table>
				</div>
			</td>
		</tr>
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="status"><b>Status:</b></label></td>
			<td valign="top" class="value" id="statusShowId" >${assetComment.status}</td>
			
		</tr>
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="resolution"><b>Resolution:</b></label></td>
			<td valign="top" class="value" colspan="2">
				<div id="resolutionId" >${assetComment.resolution}</div>
			</td>
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="dateResolved"><b>Resolved By:</b></label></td>
			<td valign="top" class="value" id="dateResolvedId" ><g:if test="${resolvedBy}">${resolvedBy} at ${dtResolved}</g:if></td>
		</tr>
		
	</table>
	</div>
	</body>
	</html>