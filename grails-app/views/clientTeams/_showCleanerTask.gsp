<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Task Details</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
	<g:javascript src="tech_teams.js" />
	<g:javascript src="asset.comment.js" />

</head>
<body>
	<a name="top"></a>
	<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" id="mainbody">
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red; font-size:15px"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>
		<g:if test="${canPrint == false}">
			<div style="color: red;">
				<ul>
					<li>Please note that in order to print barcode labels you will need the to use Windows Internet Explorer browser.</li>
				</ul>
			</div>
		</g:if>
	<g:form name="issueUpdateForm" controller="task" action="update">
		<a name="comments"></a>
		<input type="hidden" name="id" id="issueId" value="${assetComment.id}" />
		<input type="hidden" name="redirectTo" id="redirectTo" value="taskList" />
		<input type="hidden" name="FormName" id="FormName" />
		<input type="hidden" name="PrinterName" id="PrinterName" />
		<input type="hidden" name="RepPath" id="RepPath" />
		<input type="hidden" name="urlPath" id="urlPath" value="<g:resource dir="resource" file="racking_label.tff" absolute="true"/>" />
		<input type="hidden" name="model" id="model" value="${assetEntity?.model}" />
        <input type="hidden" name="cart" id="cart" value="${assetEntity?.cart}" />
        <input type="hidden" name="shelf" id="shelf" value="${assetEntity?.shelf}" />
        <input type="hidden" name="room" id="room" value="${assetEntity?.targetRoom}" />
        <input type="hidden" name="rack" id="rack" value="${assetEntity?.targetRack}" />
        <input type="hidden" name="upos" id="upos" value="${assetEntity?.targetRackPosition}" />
        <input type="hidden" name="cartQty" id="cartQty" value="${cartQty}" />
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
		 	
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" id="assignedToEditTdId" style="width: 20%;" colspan="3" >
					<g:select id="assignedToEditId_${assetComment.id}" name="assignedTo" from="${projectStaff}" value="${assetComment.assignedTo?.id}" optionKey="id" noSelection="['':'please select']"></g:select>
				</td>
			</tr> 
			<tr class="prop issue" id="estFinishShowId"  >
				<td valign="top" class="name"><label for="estFinish">Est.Finish:</label></td>
				<td valign="top" class="value" id="estFinishShowId_${assetComment.id}" colspan="3" nowrap="nowrap">
				<tds:convertDate date="${assetComment.estFinish}" format="M/d kk:mm" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value" colspan="3"><g:select id="categoryEditId_${assetComment.id}" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"></g:select>
				<g:if test="${assetComment.moveEvent}">
		   		  <span style="margin-left:60px;">Move Event:</span>
		   		  <span style="margin-left:10px;">${assetComment?.moveEvent.name}</span>
		   		</g:if>
				</td>
				
			</tr>
			<tr>
			<g:if test="${assetComment.assetEntity}">
		   		  <td>Asset:</td><td style="width: 1%">&nbsp;${assetComment?.assetEntity.assetName}</td>
		   		</g:if>
		   	</tr>
		   	<tr class="prop">
				<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
				<td valign="top" class="value" colspan="3"><span id="categoryEditId">${assetComment?.createdBy} on 
				<tds:convertDate date="${assetComment?.dateCreated}" format="M/d" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span></td>
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
				<td valign="top" class="value" colspan="3"><div id="previousNote">
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
			<g:if test="${assetComment.resolvedBy}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved By:</label></td>
					<td valign="top" class="value">
						<span id="resolvedByTd" >${assetComment.resolvedBy} on <tds:convertDate date="${assetComment?.dateResolved}" format="M/d" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span>
					</td>
				</tr> 
			</g:if>
			<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Printers :</label></td>
					<td nowrap="nowrap">
						<select type="hidden" id="Printers" name="Printers"	${canPrint ? '' : 'disabled="disabled"' } onChange="startprintjob();">
							<option value="Zebra (ZPL-II)" >Zebra (ZPL-II)</option>
							<g:each in="${session.getAttribute( 'PRINTERS' )}" var="printer">
								<option value="${printer}" ${prefPrinter==printer ? 'selected="selected"' : ''}>
									${printer}
								</option>
							</g:each>
						</select> 
						<b>Quantity: </b>
						<g:select  name="labels" id="labelQuantity" from="${1..4}" value="${lblQty}" onchange="${remoteFunction(controller:'task', action:'setLabelQuantityPref',params:'\'selected=\'+ this.value+\'&prefFor=printLabelQuantity\'')}"/>
					</td>
			</tr>
			<tr>
			    <td class="buttonR" >
					<g:if test="${permissionForUpdate==true}">
						<input type="button" value="Update Task" onclick="validateComment(${assetComment.id})" />
					</g:if>
					<g:else>
						<input type="button" value="Update Task" disabled="disabled" />
					</g:else>
				</td>
				<td class="buttonR" style="text-align:right;padding: 5px 3px;">
					<input type="button" id="printButton" value="Print" ${canPrint ? '': 'disabled="disabled"' } onclick="startprintjob();"  />
					<input type="button" id="printAndDoneButton" value="Print And Done"  
							onclick="printAndMarkDone(${assetComment.id}, 'Completed', '${assetComment.status}');"  />
				</td>
				<td class="buttonR" colspan="2" style="text-align:right;padding: 5px 3px;">
					<input type="button" value="Cancel" onclick="cancelButton(${assetComment.id})" />
				</td>
			</tr>	
		</table>
		 <input type="hidden" name ="assetName" id="assetName" value="${assetComment?.assetEntity?.assetName}">
		 <input type="hidden" name ="assetTag" id="assetTag" value="${assetComment?.assetEntity?.assetTag}">
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
					<td class="heading"><a href="#detail">${assetComment?.assetEntity?.assetType == 'Files' ? 'Storage' : assetComment?.assetEntity?.assetType} Details</a></td>
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
		 document.onkeyup = keyCheck;
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
function keyCheck( e ){
	var currentFocus = document.activeElement.id
	var focusId = currentFocus.substring(0,currentFocus.indexOf("_"))
	if(currentFocus != 'search' && focusId != 'editComment' && focusId != 'noteEditId' ){
	  if(!e && window.event) e=window.event;
	  var keyID = e.keyCode;
	  if(keyID == 13){
		  $("#printAndDoneButton").click()
		  return;
	  } else if(keyID == 80){
	       startprintjob();
	  }
	  var labelQty = keyID - 48 
	  if(labelQty < 5 && labelQty > 0){
	       $('#labelQuantity').focus()
		  if($("input:focus").length < 1){
		  	$('#labelQuantity').val(labelQty);
		  }
	  }
    }
}
		

 function printAndMarkDone(id, status, currentStatus){
 	startprintjob();
	 jQuery.ajax({
		url: '../task/update',
		data: {'id':id,'status':status,'currentStatus':currentStatus,view:'myTask'},
		type:'POST',
		success: function(data) {
			if (typeof data.error !== 'undefined') {
				alert(data.error);
			} else {
				 hideStatus(id, status)
				 $('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")')
				 if(status=='Started'){
				 	$('#started_'+id).hide()
				 }
				 $("#search").focus()
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while attempting to update task/comment")
		}
	});
 }
 
 function validateComment(objId){
	 var status = $('#statusEditId_'+${assetComment.id}).val()
	 var params = {   'comment':$('#editComment_'+objId).val(), 'resolution':$('#resolutionEditId_'+objId).val(), 
						 'category':$('#categoryEditId_'+objId).val(), 'assignedTo':$('#assignedToEditId_'+objId).val(),
						 'status':$('#statusEditId_'+objId).val(),'currentStatus':$('#currentStatus_'+objId).val(), 
						 'note':$('#noteEditId_'+objId).val(),'id':objId,'view':'myTask', 'tab': $('#tabId').val()
						}
		 jQuery.ajax({
				url: '../task/update',
				data: params,
				type:'POST',
				success: function(data) {
					if (typeof data.error !== 'undefined') {
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

 function hideStatus(id,status){
	$('#showStatusId_'+id).hide()
	$('#detailTdId_'+id).css('display','none')
	B1.Start(60);
}

 </script>
<script type="text/javascript" language="javascript">

var sHint = "C:\\temp\\output";
//=============================================================================
// PRINT HERE
//=============================================================================


function startprintjob() {
	var selectval=  $("#Printers").val();
	jQuery.ajax({
		url: '../task/setLabelQuantityPref',
		data:{'selected':selectval, 'prefFor':'PRINTER_NAME'},
		type:'POST'
	});
	/*
	alert('model:' + $("#model").val());
	alert(", cart: " + $("#cart").val());
	alert(", shelf: " + $("#shelf").val());
	alert(", room: " + $("#room").val());
	alert('first time2');
	alert(", rack: " + $("#rack").val());
	alert(", upos: " + $("#upos").val());
    alert(", urlPath: " + $("#urlPath").val());
    alert('first time3');
    */

	try {
		var tform = window.TF;
		if (typeof tform == 'undefined') {
			alert("Sorry but the necessary TFORMer ActiveX needed for label printing wasn't loaded.");
			return;
		}
		var job = tform.CreateJob();
		var jobdata = job.NewJobDataRecordSet();
	    var labelsCount = $('#labelQuantity').val();
	
		// var form = window.document.issueUpdateForm;
	    job.RepositoryName = $("#urlPath").val();  
	    job.FormName = $('#FormName').val();
	    job.PrinterName = $('#PrinterName').val();
	   
	    jobdata.ClearRecords();
	    
	    for(var label = 0; label < labelsCount; label++) {
		    jobdata.AddNewRecord();
		    jobdata.SetDataField('serverName', $("#assetName").val());
		    jobdata.SetDataField('assetTag', $("#assetTag").val());
		    
            jobdata.SetDataField('model', $("#model").val());     
            jobdata.SetDataField('cart', $("#cart").val());           
            jobdata.SetDataField('shelf', $("#shelf").val());
            jobdata.SetDataField('room', $("#room").val());
            jobdata.SetDataField('rack', $("#rack").val());
            jobdata.SetDataField('upos', $("#upos").val());
            jobdata.SetDataField('cartQty', $("#cartQty").val());
	    }
	    // now we print one copy of the label with default settings
	    try {
	    	job.PrintForm();
	    } catch (e) {
			if (e.message!="Error: The operation was canceled by the user. ") { 
		    	alert ("TFORMer returned an error!" + e +
		           "\nError description: " + e.description + 
		           "\nError name: " + e.name + 
		           "\nError number: " + e.number + 
		           "\nError message: " + e.message);
			}
	    }
	}catch(ex){
		alert("It appears that your security settings are preventing printing. Please add this site to your Trusted Sites in setup." + 
		   "\nException was:" + ex.message);
	}

}

//=============================================================================
// Add a new option to select element
//=============================================================================
function AddOption (selElement, text, value)
{
  opt = new Option(text, value, false, true);
//selElement.options[0] = opt;
}

//=============================================================================
// Set default data for TFORMer Runtime Properties
//=============================================================================
function InitData()
{
	//To check the Instructions for enable the Clean Button
	var printButton = $('#printButton');
	if(!printButton.disabled){ 
		printButton.focus();
	}
	var form = window.document.issueUpdateForm;
	var path = window.location.href;
	var i = -1;
	// the following code evaluates the path to the demo repository
	for (n=1; n<=3; n++)
	{
		i = path.lastIndexOf('/');
		if (i != -1)
		{
			path = path.substring(0,i)                              // one directory level up
		}
	}
	if (path.substr (0, 8) == "file:///")			                  // do not use URL-style for Repository file name - remove file:///
	    path = path.substr (8);
    path=unescape(path);	
    form.RepPath.value 	= path + '/Demo Repository/Demos.tfr';  // repository name
    form.FormName.value = 'BarcodeLabels';											// form name
    form.PrinterName.value = ''																	// use default printer
	// get list of installed printers
	var dropdown = document.issueUpdateForm.Printers;
	AddOption (dropdown, "Zebra (ZPL-II)", "ZPL:" + sHint + ".ZPL");
	
	retrieve_field(document.issueUpdateForm.Printers)
	
	mySelect(dropdown);
	
}

//=============================================================================
// Handle Browse Button
//=============================================================================
function FileFind_onchange()
{
var form = window.document.issueUpdateForm;

  form.RepPath.value = form.FileFind.value;
}

//=============================================================================
// The selected dprinter has changed
//=============================================================================
function mySelect(x)
{
	$('#PrinterName').val( x.options[x.selectedIndex].value );
	
}

</script>
<script type="text/javascript">
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
	InitData();
</script>
</body>
</html>
