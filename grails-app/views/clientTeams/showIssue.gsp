<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
	<div class="mainbody">
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red; font-size:15px"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>
	<g:form name="issueUpdateForm" controller="assetEntity" action="updateComment">
		<a name="comments"></a>
		<input id="issueId" name="id" type="hidden" value="${assetComment.id}" />
		<input id="redirectTo" name="redirectTo" type="hidden" value="taskList" />
		<table style="width:420px;margin-bottom:30px">
			<tr>
				<td class="heading" colspan=2><a class="heading" href="#comments">Task details:</a></td>
			</tr>
			<tr>
			<td colspan=2>
			</td>
			</tr>		
			<tr>
			<td valign="top" class="name"><label for="comment">Task:</label></td>
			<td colspan=2>
			  <textarea rows="4" cols="130" style="width:188px;padding:0px;" title="Edit Comment..." id="editComment" name="comment" >${assetComment.comment}</textarea>
			</td></tr>	
			<tr class="prop" >
				<td valign="top" class="name"><label for="status">Status:</label></td>
				<td style="width: 20%;">
					<g:select id="statusEditId" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="${assetComment.status}"
					noSelection="['':'please select']" ></g:select>
				</td>	
			</tr>	
			 <% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;%>
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" id="assignedToEditTdId" style="width: 20%;" >
					<g:select id="assignedToEditId" name="assignedTo" from="${partyList}" value="${assetComment.assignedTo.id}" optionKey="id" noSelection="['':'please select']"></g:select>
				</td>
			</tr> 
			<tr class="prop issue" id="dueDatesEditId"  >
				<td valign="top" class="name"><label for="dueDate">Due Date:</label></td>
				<td valign="top" class="value" id="dueDatesEditId" ><script type="text/javascript" charset="utf-8">
	             jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
	             </script><input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="dueDate" id="dueDateEdit"
						value="<tds:convertDate date="${assetComment?.dueDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" /></td>
				
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value"><g:select id="categoryEditId" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"></g:select></td>
			</tr>
			<tr>
			<g:if test="${assetComment.assetEntity}">
		   		  <td>Asset:</td><td>&nbsp;${assetComment?.assetEntity.assetName}</td>
		   		</g:if>
		   		<g:if test="${assetComment.moveEvent}">
		   		  <td>Move Event:</td><td>&nbsp;${assetComment?.moveEvent.name}</td>
		   		</g:if>
		   	</tr>
		   	<tr class="prop">
				<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
				<td valign="top" class="value"><span id="categoryEditId">${assetComment?.createdBy} on <tds:convertDate date="${assetComment?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
				<td valign="top" class="value" colspan="2">
					<textarea cols="100" rows="2" style="width:188px;padding:0px;" id="resolutionEditId" name="resolution" >${assetComment.resolution}</textarea>
				</td>
			</tr> 
			 <tr class="prop">
				<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
				<td valign="top" class="value"><div id="previousNote" style="width: 180px">
				 <table>
                   <g:each in="${notes}" var="note" status="i" >
                    <tr>
	                    <td>${note[0]}</td>
	                    <td>${note[1]}</td>
                        <td>${note[2]}</td>
                     </tr>
                   </g:each>
				 </table>
				</div></td>
			</tr>
		    <tr class="prop">
				<td valign="top" class="name"><label for="notes">Note:</label></td>
				<td valign="top" class="value">
				   <textarea cols="80" rows="4" id="noteEditId" name="note" style="width:188px;padding:0px;"></textarea>
				</td>
			</tr>
			<g:if test="${assetComment.dateResolved}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved At:</label></td>
					<td valign="top" class="value" colspan="2">
						<span id="dateResolvedTd" ><tds:convertDate date="${assetComment?.dateResolved}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span>
					</td>
				</tr> 
			</g:if>
			<g:if test="${assetComment.resolvedBy}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved By:</label></td>
					<td valign="top" class="value" colspan="2">
						<span id="resolvedByTd" >${assetComment.resolvedBy}</span>
					</td>
				</tr> 
			</g:if>
			
			<tr>
			    <td class="buttonR" ><input type="button" value="Cancel" onclick="cancelButton()" /> </td>
				<td class="buttonR" colspan="1" style="text-align:right;">
				<input type="submit" value="Update Task" onclick="return validateComment()" />
			</td>
			</tr>	
		</table>
</g:form>

		<%--<div class="clear" style="margin:4px;"></div>
		<a name="detail" ></a>
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
		<div style="margin:2px;" class="reset" ></div>
		<a name="detail" ></a>
		<g:if test="${assetComment?.assetEntity}">
		 	<div style="float: left;">
				<table style="width:420px;margin-bottom:30px">
				<tr>
					<td class="heading"><a href="#detail">Details</a></td>
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
 function validateComment(){
	 var status = $('#statusEditId').val()
	 var boo = true
	 if(status=='Completed' && $('#resolutionEditId').val()==''){
		 boo= false
         alert("Please Enter Resolution")
	 }
	 return boo
 }

 </script>
 <script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
</script>
</body>
</html>