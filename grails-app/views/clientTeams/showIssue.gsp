<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
	<title>Issue Details</title>
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
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" >Teams</g:link></li>
			<li><g:link class="mobmenu" action="listComment" params='["tab":"Todo"]'>My Tasks</g:link></li>
			<li><a href="#" class="mobmenu mobselect">Details</a></li>
		</ul>
	</div>
		
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red;"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>

		<div class="clear" style="margin:5px;"></div>

		<table style="border:0px; width:420px;margin-top: 10px;margin-bottom: 10px;">
			<tr><td style="padding:0px;"><b>Comment</b>:&nbsp;<span id="search" >&nbsp;${assetComment.comment}</span><a href="#detail">(Details...)</a></td>
			</tr>
		</table>
	
 			

	<div class="clear" style="margin:4px;"></div>
	<g:form name="issueUpdateForm" controller="assetEntity" action="updateComment">
		<a name="comments"></a>
		<input id="issueId" name="id" type="hidden" value="${assetComment.id}" />
		<input id="redirectTo" name="redirectTo" type="hidden" value="myTask" />
		<table style="width:420px;">
			<tr>
				<td class="heading" colspan=2><a class="heading" href="#comments">Other Actions</a></td>
			</tr>
			<tr>
			<td colspan=2>
			</td>
			</tr>		
			<tr>
			<td valign="top" class="name"><label for="comment">Comment:</label></td>
			<td colspan=2>
			  <textarea rows="2" cols="100" style="width:188px;padding:0px;" title="Edit Comment..." id="editComment" name="comment" >${assetComment.comment}</textarea>
			</td></tr>	
			<tr class="prop issue" id="dueDatesEditId"  ><td valign="top" class="name"><label for="dueDate">DueDate:</label></td>
				<td valign="top" class="value" id="dueDatesEditId" ><script type="text/javascript" charset="utf-8">
	             jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
	             </script><input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="dueDate" id="dueDateEdit"
						value="<tds:convertDate date="${assetComment?.dueDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" /></td>
				
			</tr>
			<tr class="prop" >
				<td valign="top" class="name"><label for="status">Status:</label></td>
				<td style="width: 20%;">
					<g:select id="statusEditId" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="${assetComment.status}"
					noSelection="['':'please select']" ></g:select>
				</td>	
			</tr>	
			 <% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;%>
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned To:</label></td>
				<td valign="top" id="assignedToEditTdId" style="width: 20%;" >
					<g:select id="assignedToEditId" name="assignedTo" from="${partyList}" value="${assetComment.assignedTo.id}" optionKey="id" noSelection="['':'please select']"></g:select>
				</td>
			</tr> 
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value"><g:select id="categoryEditId" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"></g:select></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
				<td valign="top" class="value" colspan="2">
					<textarea cols="100" rows="2" style="width:188px;padding:0px;" id="resolutionEditId" name="resolution" >${assetComment.resolution}</textarea>
				</td>
			</tr> 
			
			<tr><td class="buttonR" colspan=1 style="text-align:center;">
				<input type="submit" value="Update Comment" onclick="return validateComment()" />
			</td>
			<td class="button_style" colspan=1 style="text-align:center;">
				<input type="submit" value="HOLD" onclick="return holdComment();" class="action_button_hold" />
			</td>
			</tr>	
		</table>
</g:form>

		<div class="clear" style="margin:4px;"></div>
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
</div>
<script type="text/javascript">
 function validateComment(){
	 var status = $('#statusEditId').val()
	 var boo = true
	 if(status=='Completed' && $('#resolutionEditId').val()==''){
		 boo= false
         alert("Please Enter Comment")
        
	 }
	 return boo
 }
 function holdComment(){
      $('#statusEditId').val('Hold')
      return true
 }

 </script>
</body>
</html>