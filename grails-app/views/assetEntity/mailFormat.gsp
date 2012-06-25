<%@ page contentType="text/html"%>
<html>
<body> 
<div style="font:courier ">

	<table style="border: 1px solid green;font-family:courier;">
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="dateCreated" style=""><b>Created At:</b></label></td>
			<td valign="top" class="value" id="dateCreatedId"  >${dtCreated}</td>
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="createdBy"><b>Created By:</b></label></td>
			<td valign="top" class="value" id="createdById" >${personCreateObj}</td>
			
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="owner"><b>Owner:</b></label></td>
			<td valign="top" class="value" id="ownerTdId" >${owners}</td>
			
		</tr>
		<tr class="prop" style="margin-top: 10px">
			<td valign="top" class="name" ><label for="commentType"><b>Comment Type:</b></label></td>
			<td valign="top" class="value" id="commentTypeTdId" >${assetComment.commentType}</td>
			
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" ><label for="category"><b>Category:</b></label></td>
			<td valign="top" class="value" id="categoryTdId" >${assetComment.category}</td>
			
		</tr>
		<tr class="prop">
			<td valign="top" class="name" ><label for="mustVerify"><b>Must Verify:</b></label></td>
			<td valign="top" class="value" id="verifyTdId">
				<input type="checkbox" id="mustVerifyShowId" name="mustVerify" value="${assetComment.mustVerify}" disabled="disabled" />
			</td>
			
		</tr>
		<tr id = "assetShowId" class="prop" style="margin-top: 10px" >
			<td valign="top" class="name" ><label for="asset"><b>Asset:</b></label></td>
			<td valign="top" class="value" id="assetShowValueId" style="display: none;">${assetComment.assetEntity?.assetName} </td>
		</tr>
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
				<g:each in="${assetNotes}" var="assetNote">
				<tr>
				<td><tds:convertDate date="${assetNote.dateCreated}"
				     timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
				</td>
				<td>${assetNote.createdBy }</td>
				<td>${assetNote.note }</td>
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
			<td valign="top" class="name" ><label for="dateResolved"><b>Resolved At:</b></label></td>
			<td valign="top" class="value" id="dateResolvedId" >${dtResolved}</td>
		</tr>
		<tr style="margin-top: 10px">
			<td valign="top" class="name" nowrap="nowrap" ><label for="resolvedBy"><b>Resolved By:</b></label></td>
			<td valign="top" class="value" id="resolvedById" >${personResolvedObj}</td>
		</tr>
			<tr style="margin-top: 10px"><td valign="top" class="name" ><label for="dueDate"><b>dueDate:</b></label></td>
			<td valign="top" class="value" id="dueDatesId" ><tds:convertDate date="${assetComment.dueDate}" /></td>
		</tr>	
		
	</table>
	</div>
	</body>
	</html>