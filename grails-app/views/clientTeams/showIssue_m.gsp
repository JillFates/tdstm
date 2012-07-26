<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Task Details</title>
<jq:plugin name="jquery"/>
 
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
<meta name="viewport" content="height=device-height,width=220" />
	
<script type="text/javascript">
        window.addEventListener('load', function(){
                setTimeout(scrollTo, 0, 0, 1);
        }, false);
</script>
</head>
<body>
	<a name="top"></a>
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" style="width: 220px;">
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div class="message"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>
	<g:form name="issueUpdateForm" controller="assetEntity" action="updateComment">
		<a name="comments"></a>
		<input id="issueId" name="id" type="hidden" value="${assetComment.id}" />
		<input id="redirectTo" name="redirectTo" type="hidden" value="taskList" />
		<table style="width: 100%;">
			<tr>
				<td class="heading" colspan=2><a class="heading" href="#comments">Task details:</a></td>
			</tr>
			<tr>
			<td colspan=2>
			</td>
			</tr>		
			<tr>
			<td colspan=2>
			  <textarea rows="4" cols="130" style="width:100%;padding:0px;" title="Edit Comment..." id="editComment_${assetComment.id}" name="comment" >${assetComment.comment}</textarea>
			</td></tr>	
			<tr class="prop" >
				<td valign="top" class="name"><label for="status">Status:</label></td>
				<td style="width: 20%;">
					<g:select id="statusEditId_${assetComment.id}" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="${assetComment.status}"
					noSelection="['':'please select']" onChange="showResolve()" ></g:select>
				</td>	
			</tr>	
			 <% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;%>
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" id="assignedToEditTdId" style="" >
					<g:select id="assignedToEditId_${assetComment.id}" name="assignedTo" from="${partyList}" value="${assetComment.assignedTo.id}" optionKey="id" noSelection="['':'please select']"></g:select>
				</td>
			</tr> 
			<tr class="prop issue" id="dueDatesEditId"  >
				<td valign="top" class="name"><label for="dueDate">Due Date:</label></td>
				<td valign="top" class="value" id="dueDatesEditId_${assetComment.id}" ><script type="text/javascript" charset="utf-8">
	             jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
	             </script><input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="dueDate" id="dueDateEdit_${assetComment.id}"
						value="<tds:convertDate date="${assetComment?.dueDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" /></td>
				
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value"><g:select id="categoryEditId_${assetComment.id}" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"></g:select></td>
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
				<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
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
				<td valign="top" class="name"><label for="notes">Note:</label></td>
				
			</tr>
			<tr class="prop" id="noteTrId_${assetComment.id}"><td valign="top" class="value" colspan="2">
				   <textarea cols="130" rows="4" id="noteEditId_${assetComment.id}" name="note" style="width:100%;padding:0px;"></textarea>
				</td>
			</tr>
			<tr class="prop" id="resolutionId_${assetComment.id}" style="display: none">
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
					<td valign="top" class="value" colspan="1">
						<span id="resolvedByTd" >${assetComment.resolvedBy} on <tds:convertDate date="${assetComment?.dateResolved}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span>
					</td>
				</tr> 
			</g:if>
			
			<tr>
			    <td class="buttonR" ><input type="button" value="Cancel" onclick="cancelButton(${assetComment.id})" /> </td>
				<td class="buttonR" colspan="1" style="text-align:right;">
					<input type="button" value="Update Task" onclick="validateComment(${assetComment.id})" />
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
			<table style="width: 220px; margin-bottom: 30px;">
			<tr>
				<td class="heading"><a href="#detail">Details</a></td>
				<td><span style="float:right;"><a href="#top">Top</a></span></td>
			</tr>
			<tr><td colspan=2>
			<dl>
               <g:if test="${assetComment?.assetEntity.assetType=='Application'}">
	                <dt>Application Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>Validation:</dt><dd>&nbsp;${assetComment?.assetEntity.validation}</dd>
					<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
					<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
               </g:if>
                <g:elseif test="${assetComment?.assetEntity.assetType=='Database'}">
                    <dt>Database Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>DB Size:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>DB Format:</dt><dd>&nbsp;${assetComment?.assetEntity.dbFormat}</dd>
					<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
                </g:elseif>
                <g:elseif test="${assetComment?.assetEntity.assetType=='Files'}">
                    <dt>File Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>FIle Size:</dt><dd>&nbsp;${assetComment?.assetEntity.fileSize}</dd>
					<dt>File Format:</dt><dd>&nbsp;${assetComment?.assetEntity.fileFormat}</dd>
					<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
                </g:elseif>
                <g:else>
					<dt>Asset Tag:</dt><dd>&nbsp;${assetComment?.assetEntity.assetTag}</dd>
					<dt>Asset Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
					<dt>Model:</dt><dd>&nbsp;${assetComment?.assetEntity.model}</dd>
					<dt>Serial #:</dt><dd>&nbsp;${assetComment?.assetEntity.serialNumber}</dd>
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
	       $('#noteTrId_'+${assetComment.id}).hide()
	       $('#resolutionId_'+${assetComment.id}).show()
	       $('#resolutionTrId_'+${assetComment.id}).show()
	 }
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
 function validateComment(objId){
	 var status = $('#statusEditId_'+${assetComment.id}).val()
	 if(status=='Completed' && $('#resolutionEditId_'+${assetComment.id}).val()==''){
        alert("Please Enter Resolution")
	 }else{
		var params = {   'comment':$('#editComment_'+objId).val(), 'resolution':$('#resolutionEditId_'+objId).val(), 
						 'category':$('#categoryEditId_'+objId).val(), 'assignedTo':$('#assignedToEditId_'+objId).val(),
						 'dueDate':$('#dueDateEdit_'+objId).val(), 'status':$('#statusEditId_'+objId).val(),
						 'note':$('#noteEditId_'+objId).val(),'id':objId }
		 jQuery.ajax({
				url: '../assetEntity/updateComment',
				data: params,
				type:'POST',
				success: function(data) {
					var myClass = $('#issueTrId_'+data.assetComment.id).attr("class");
					$('#comment_'+data.assetComment.id).html(truncate(data.assetComment.comment))
					$('#lastUpdated_'+data.assetComment.id).html(formatDueDate(data.assetComment.lastUpdated))
					$('#dueDate_'+data.assetComment.id).html(formatDueDate(data.assetComment.dueDate))
					if(data.assetComment.assetEntity){
					 	$('#asset_'+data.assetComment.id).html(data.assetComment.assetEntity.assetName)
					}
					$('#statusTd_'+data.assetComment.id).html(data.assetComment.status)
					$('#detailTdId_'+data.assetComment.id).hide()
					$('#issueTrId_'+data.assetComment.id).removeClass(myClass).addClass(data.statusCss);
					$('#issueTr_'+data.assetComment.id).removeClass(myClass).addClass(data.statusCss);
					if(data.assetComment.status==''|| data.assetComment.status=='Started'|| data.assetComment.status=='Pending' || data.assetComment.status==null){
						$('#started_'+data.assetComment.id).hide()
						$('#showStatusId_'+data.assetComment.id).css('display','table-row')
						if(data.assetComment.status=='Pending'){
							$('#started_'+data.assetComment.id).show()
						}
						$('#toDoAllId').html(parseInt($('#toDoAllId').html())+1)
					}else{
						$('#showStatusId_'+data.assetComment.id).hide()
						$('#issueTrId_'+data.assetComment.id).remove()
						$('#toDoNumberId').html(parseInt($('#toDoNumberId').html())-1)
						B1.Start(60);
					}
				}
			});
	 }
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
