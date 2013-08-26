<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta name="layout" content="projectHeader" />
	<title>Asset</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'cleaning.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />

<script language="JavaScript" type="text/javascript">
	/*--------------------------------------------------------
	* function to call printjob when user press on 1,2,3 or 4
	*--------------------------------------------------------*/
	if(${ projMap ? true : false}){
		   
		function keyCheck( e ){
		  if(!e && window.event) e=window.event;
		  var keyID = e.keyCode;
		  if(keyID == 9 ){
			  $('#textSearchId').focus();
			  return;
		  }
		  var labelQty = keyID - 48 
		  if(labelQty < 5 && labelQty > 0){
			  if($("input:focus").length < 1){
			  	$('#labelQuantity').val(labelQty);
			  	startprintjob();
			  }
		  }
		}
		document.onkeyup = keyCheck;
	}
	// Function to save a field.
	var domain		= '';
	var path		= '/';
	var secure		= 0;
	
	function save_field(obj) {
	var cookie_value = '';
	var objType = new String(obj.type);
	switch(objType.toLowerCase()) {
		case "checkbox" :
			if (obj.checked) cookie_value = obj.name + '=[1]'
			else cookie_value = obj.name + '=[0]'
			break;
		case "undefined" :
			// a.k.a. radio field.
			for (var i = 0; i < obj.length; i++) {
				if (obj[i].checked) cookie_value = obj[i].name + '=[' + i + ']'
			}
			break;
		case "select-one" :
			cookie_value = obj.name + '=[' + obj.selectedIndex + ']';
			break;
		case "select-multiple" :
			cookie_value = obj.name + '=[';
			for (var i = 0; i < obj.options.length; i++) {
				if (obj.options[i].selected) cookie_value += '+' + i
			}
			cookie_value += ']';
			break;
		default :
			// We assume all other fields will have
			// a valid obj.name and obj.value
			cookie_value = obj.name + '=[' + obj.value + ']';
	}
	if (cookie_value) {
		var expires = new Date();
		expires.setYear(expires.getYear() + 1);
		document.cookie = cookie_value +
		((domain.length > 0) ? ';domain=' + domain : '') +
		((path) ? ';path=' + path : '') +
		((secure) ? ';secure' : '') +
		';expires=' + expires.toGMTString();
	}
	return 1;
}

// Function to retrieve a field.
function retrieve_field(obj) {
	var cookie = '', real_value = '';
	cookie = document.cookie;
	var objType = new String(obj.type);
	if (obj.name)
		var objName = new String(obj.name);
	else
		var objName = new String(obj[0].name);
	var offset_start = cookie.indexOf(objName + '=[');
	if (offset_start == -1) return 1;
	var offset_start_length = objName.length + 2;
	offset_start = offset_start + offset_start_length;
	var offset_end = cookie.indexOf(']', offset_start);
	real_value = cookie.substring(offset_start, offset_end);
	switch(objType.toLowerCase()) {
		case "checkbox" :
			if (real_value == '1') obj.checked = 1
			else obj.checked = 0
			break;
		case "undefined" :
			obj[real_value].checked = 1;
			break;
		case "select-one" :
			obj.selectedIndex = real_value;
			break;
		case "select-multiple" :
			for (var i = 0; i < obj.options.length; i++) {
				if ((real_value.indexOf('+' + i)) > -1)
					obj.options[i].selected = 1;
				else
					obj.options[i].selected = 0;
			}
			break;
		default :
			obj.value = real_value;
			break;
	}
	return 1;
}
	
	</script>
<script type="text/javascript">
	$(document).ready(function() {
		$("#serverInfoDialog").dialog({ autoOpen: false })
		$('#textSearchId').focus();
	})
</script>
<script type="text/javascript" language="Javascript1.2">var sHint = "C:\\temp\\output";
//=============================================================================
// PRINT HERE
//=============================================================================
function startprintjob(){
	try{	
		var job = window.TF.CreateJob();
		var form = window.document.assetSearchForm;
		var jobdata = job.NewJobDataRecordSet();
		    job.RepositoryName = $('#urlPath').val();       			 
		    job.ProjectName = form.PrjName.value;     
		    job.FormName = form.FormName.value;                   
		    job.PrinterName = form.PrinterName.value;
		    var labelsCount = document.assetSearchForm.labels.value;  
	
	    // THIS IS THE PLACE TO ADD YOUR DATA
	    jobdata.ClearRecords();
	    for(var label = 0; label < labelsCount; label++) {
	    	jobdata.AddNewRecord();                					
	    	jobdata.SetDataField('serverName', document.assetSearchForm.serverName.value); 
	    	jobdata.SetDataField('model',   document.assetSearchForm.model.value);       
	    	jobdata.SetDataField('assetTag',document.assetSearchForm.assetTag.value); 
	    	jobdata.SetDataField('cart',document.assetSearchForm.cart.value);    	   		
	    	jobdata.SetDataField('shelf',document.assetSearchForm.shelf.value);
	    	jobdata.SetDataField('room',document.assetSearchForm.room.value);
	    	jobdata.SetDataField('rack',document.assetSearchForm.rack.value);
	    	jobdata.SetDataField('upos',document.assetSearchForm.upos.value);
	    	jobdata.SetDataField('cartQty',document.assetSearchForm.cartQty.value);
	    }
	    // now we print one copy of the label with default settings
	    try {
	    	job.PrintForm();
		    $('#printCheck').val('printed');
		    var cleanButton = $('#cleanButton');
		    if(cleanButton != null && !cleanButton.disabled) {
		    	cleanButton.focus();
		    }
		    save_field(document.assetSearchForm.Printers)
	    } catch (e) {
		    alert ("TFORMer returned an error!" + 
		           "\nError description: " + e.description + 
		           "\nError name: " + e.name + 
		           "\nError number: " + e.number + 
		           "\nError message: " + e.message);
	    }
	    $("#cleanButton").focus()
	} catch(ex){
		alert("It appears that your security settings are preventing printing. Please add this site to your Trusted Sites in setup.")
	}
}

//=============================================================================
// Add a new option to select element
//=============================================================================
function AddOption (selElement, text, value)
{
  opt = new Option(text, value, false, true);
  selElement.options[0] = opt;
}

//=============================================================================
// Set default data for TFORMer Runtime Properties
//=============================================================================
function InitData()
{
	//To check the Instructions for enable the Clean Button
	checkInstuction();
	var printButton = $('#printButton');
	if(!printButton.disabled){ 
		printButton.focus();
	}
	var form = window.document.assetSearchForm;
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
    path= unescape(path);	                        					// unescape!
    form.RepPath.value 	= path + '/Demo Repository/Demos.tfr';  // repository name
    form.PrjName.value 	= 'TFORMer_Runtime_Examples';						// project name
    form.FormName.value = 'BarcodeLabels';											// form name
    form.PrinterName.value = ''																	// use default printer
	// get list of installed printers
	var dropdown = document.assetSearchForm.Printers;
	AddOption (dropdown, "Zebra (ZPL-II)", "ZPL:" + sHint + ".ZPL");
	/* window.TF.RefreshOSPrinters();
	var def = 0;
	for (i = 0; i < window.TF.GetOSPrintersCount(); i++) 
	{
	  AddOption (dropdown, window.TF.GetOSPrinter(i), window.TF.GetOSPrinter(i));
	  if (window.TF.GetOSPrinter(i) == window.TF.GetOSDefaultPrinter())
	    def = i;
	}
	dropdown.options[def].selected = true;
	*/
	/*-----------------------------------------------------------------------
	   TFORMer initialization has been moved to Home page to get list of installed printers.
	   Printers are added by getting form session, which are setted at HomePage
	   Modified by Lokanath Reddy 
	
	var printersString = '${session.getAttribute( "PRINTERS" )}'
	var dropdownOptions = printersString.split(",");
	for(opt=0; opt< dropdownOptions.length; opt++){
		var doption = document.createElement("option");
		doption.value = dropdownOptions[opt];
		doption.innerHTML = dropdownOptions[opt];
		dropdown.appendChild( doption );
	}	
	*-------------------------------------------------------------------------*/
	retrieve_field(document.assetSearchForm.Printers)
	mySelect(dropdown);
	
	if(${ projMap ? true : false}){
		$('#labelQuantity').focus();
	} else {
		$('#textSearchId').focus();
	}
		
}

//=============================================================================
// Handle Browse Button
//=============================================================================
function FileFind_onchange()
{
var form = window.document.assetSearchForm;

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
		/*var timeInterval;
		function submittheform() {
			document.assetSearchForm.submit();
		}*/
		
       <%-- function serverInfo(e){        
        var loc = document.assetSearchForm.location.value;
      	var location;
        var room;
        var rack;
        var pos;       
        var asset = eval('(' + e.responseText + ')');    
        if(loc == 'source'){
        location = asset[0].item.sourceLocation
        room = asset[0].item.sourceRoom
        rack = asset[0].item.sourceRack
        pos = asset[0].item.sourceRackPosition
        }else{
        location = asset[0].item.targetLocation
        room = asset[0].item.targetRoom
        rack = asset[0].item.targetRack
        pos = asset[0].item.targetRackPosition
        }     
        var htmlBody = '<table ><thead></thead><tbody>'+
        '<tr><td class="asset_details_block"><b>Asset Tag:</b>  '+asset[0].item.assetTag+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Asset Name:</b>  '+asset[0].item.assetName+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Current State:</b>  '+asset[0].state+'</td></tr>'+		
		'<tr><td class="asset_details_block"><b>Serial Number:</b>  '+asset[0].item.serialNumber+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Model:</b>  '+asset[0].item.model+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Location:</b>  '+location+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Room:</b>  '+room+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>Rack/Position:</b>  '+rack+'/'+pos+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>PDU:</b>  '+asset[0].item.pduPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>NIC:</b>  '+asset[0].item.nicPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>HBA:</b>  '+asset[0].item.hbaPort+'</td></tr>'+
		'</tbody></table>' 
        var getDialogId = document.getElementById('serverInfoDialog')
        getDialogId.innerHTML = htmlBody
        $("#serverInfoDialog").dialog('option', 'width', 200)                     
		$("#serverInfoDialog").dialog('option', 'position', ['left','top']);
        $('#serverInfoDialog').dialog('open');
        } --%>
        
        function validation( actionType ){      
	        var enterNote = $('#enterNote').val();  
	        if(enterNote == "" || enterNote == "Enter Comment"){
		      	alert('Please enter note');
		        $('#enterNote').focus();   
	      		return false;
	      	}else{
	      		var alertText = ""
	      		if(actionType != 'hold'){
	      			alertText = "Add comment, are you sure?"
	      		} else {
	      			alertText = "Place on HOLD, are you sure?"
	      		}
		      	if(confirm( alertText )){
		      		return true;
	      		}
	      	}   
      	}
      	
      	
      	  
      	function doTransition( actionType ) {
	      	if(validation( actionType )){
	      		if( actionType != 'hold'){
	      			$('form#assetSearchForm').attr({action: "addComment"});
	      		} else {
		      		$('form#assetSearchForm').attr({action: "placeOnHold"});
		      	}
		      	var splittedComment
			      var enterNote = $('#enterNote').val();
				  var completeComment = '${session.getAttribute( "COMMENT_COMPLETE" )}'
				  completeComment = completeComment.split('~');
				  var checkLength = completeComment.length
				  for ( var i=0; i<checkLength; i++ ) {
				  	if ( enterNote == completeComment[i] ) {
				  		$('#similarComment').val('null');
				  	} 
				  }
		      	$('form#assetSearchForm').submit(); 
	      	}else {
	      		return false;
	      	}
      	}
      
      	function clean(){
      		if(doCheckValidation()){ 
      			var obj = $('#confirmCheck');
      			var printCheck = $('#printCheck');
      			if(printCheck.val() != "printed" )
      			{
      				if(confirm('You have not printed labels for this asset. Are you sure that you want to continue?')){
      					$('form#assetSearchForm').attr({action: "cleaning"});     
   						$('form#assetSearchForm').submit();
      				}else {
      					return false;
      				}
   				}else {
   						$('form#assetSearchForm').attr({action: "cleaning"});      
   						$('form#assetSearchForm').submit();
   				}
   			}else{
   				alert("Please select all instructions");
   				return false;
   			}
      	}
      	function cancel(){
      		$('form#assetSearchForm').attr({action: "cancelAssetSearch"});      
   			$('form#assetSearchForm').submit();
      	}
        function doCheckValidation(){
		    var j = 0;
	        var boxes = document.getElementsByName('checkChange'); 
			for (i = 0; i < boxes.length; i++) {
	          if (boxes[i].type == 'checkbox'){
	               if(boxes[i].checked == false){
	       			j=1;
	       		 }
	           }
	     	}      
	        if(j == 0){     
	        	return true;
	        }else{
	        	return false;
	        }     
        }
      function commentSelect(cmtVal) {
	      var splittedComment
		  var completeComment = '${session.getAttribute( "COMMENT_COMPLETE" )}'
		  completeComment = completeComment.split('~');
		  var checkLength = completeComment.length
		  for ( var i=0; i<checkLength; i++ ) {
		  	if ( completeComment[i].length > 25 ) {
		  	    if ( cmtVal != completeComment[i] ) {
		  			splittedComment = completeComment[i].substring(0,25);
		  		} else {
		  			splittedComment = completeComment[i];
		  		}
		  	} else {
		  		splittedComment = completeComment[i];
		  	}
		  	if ( cmtVal == splittedComment ) {
		  		$('#enterNote').val(completeComment[i]);
		  	}
		 }
		 if ( cmtVal == 'Select a common reason:' ) {
		  		$('#enterNote').val('Enter Comment');
		 }
	      $('#selectCmt').val(cmtVal);
      }
      /*-----------------------------------------------------------------------
      *To check all instructions checked or not to enable cleaned button
      *@author: Srinivas
      *@return: disable cleanbutton if all checkboxes not checked. enable if all checked
      *-------------------------------------------------------------------------*/
      function checkInstuction()
      {
      	var cleanButton = $('#cleanButton');
      	if(doCheckValidation()) {
	    	if(cleanButton != null ) {
	    		cleanButton.attr('disabled',false);
	    		cleanButton.focus()
	    	}
	    }else {
	    	if(cleanButton != null ) {
	    		cleanButton.attr('disabled',true);
	    	}	
	    }
      }
      /*-----------------------------------------------------------------------
      *To clear out the default Comment text("Enter Comment") in Hold Text area
      *@author: Srinivas
      *@return: Clear default Text in TextArea.
      *-------------------------------------------------------------------------*/
      function clearComment(holdText) {
      	if(holdText.value == "Enter Comment") {
      		holdText.value = ""
      	}	
      }
    </script>
</head>
<body>
	<div id="serverInfoDialog" title="Server Info" onclick="$('#serverInfoDialog').dialog('close')"></div>
	<OBJECT id="TF" classid="clsid:18D87050-AAC9-4e1a-AFF2-9D2304F88F7C"
		CODEBASE="${resource(dir:'resource',file:'TFORMer60.cab')}"
		style="height: 1px;"></OBJECT>
	<div class="mainbody" style="width: 100%;">
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" params="[projectId:projectId]">Teams</g:link></li>
			<li><g:link class="mobmenu" action="logisticsHome" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"user":"ct"]'>Home</g:link></li>
			<li><g:link class="mobmenu" action="logisticsMyTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Tasks</g:link></li>
			<li><g:link class="mobmenu mobselect" action="logisticsAssetSearch" params='["bundleId":bundleId,"menu":"true","teamId":teamId,"location":location,"projectId":projectId,"user":"ct"]'>Asset</g:link></li>
		</ul>
	</div>
	</div>
	<div style="float: left; width: 100%; margin: 5px 0;">
		<g:form name="assetSearchForm" action="logisticsAssetSearch">
			<input name="bundleId" type="hidden" value="${bundleId}" />
			<input type="hidden" name="printCheck" id="printCheck" value="notprinted" />
			<input type="hidden" name="urlPath" id="urlPath" value="<g:resource dir="resource" file="racking_label.tff" absolute="true"/>" />
			<input name="teamId" type="hidden" value="${teamId}" />
			<input name="location" type="hidden" value="${location}" />
			<input name="projectId" type="hidden" value="${projectId}" />
			<input name="search" type="hidden" value="${search}" />
			<input name="label" type="hidden" value="${label}" />
			<input name="actionLabel" type="hidden" value="${actionLabel}" />
			<input name="user" type="hidden" value="ct" />
			<input name="assetPage" type="hidden" value="assetPage" />
			<input name="serverName" type="hidden" value="${projMap?.asset?.assetName}" />
			<input name="model" type="hidden" value="${projMap?.asset?.model}" />
			<input name="cart" type="hidden" value="${projMap?.asset?.cart}" />
			<input name="shelf" type="hidden" value="${projMap?.asset?.shelf}" />
			<input name="room" type="hidden" value="${projMap?.asset?.targetRoom}" />
			<input name="rack" type="hidden" value="${projMap?.asset?.targetRack}" />
			<input name="upos" type="hidden" value="${projMap?.asset?.targetRackPosition}" />
			<input name="assetTag" type="hidden" value="${projMap?.asset?.assetTag}" />
			<input name="cartQty" type="hidden" value="${cartQty}" />
			<input name="similarComment" id="similarComment" type="hidden" value="nosimilar" />
		<div style="float: right; margin-right: 10px; margin-top: -20px;">
			<input type="text" name="textSearch" id="textSearchId" size="10" />&nbsp;<img src="${resource(dir:'images',file:'search.png')}" />
		</div>
		<div id="mydiv" onclick="$('#mydiv').hide();setFocus();">						            
			<g:if test="${flash.message}">
				<div style="color: red;float: left;">
				<ul><li>${flash.message}</li>					
				<g:each status="i" in="${issuecomments}" var="comments">
					<g:if test="${assetIssueCommentListSize == 1}">
						<dl><dt></dt><dd>${comments}</dd></dl>
					</g:if>
					<g:else>
					    	<dl><dt></dt><dd>${comments}&nbsp;( reason ${i+1} )</dd></dl>
					</g:else>
				</g:each>
				</ul>
				</div>
			</g:if>
		</div>
		<div style="width: 100%; height: auto; border: 1px solid #5F9FCF; margin-top: 10px; padding: 10px 0;">
		<span style="position: absolute; text-align: center; width: auto; margin: -17px 0 0 10px; padding: 0px 8px; background: #ffffff;"><b>Asset Details</b></span>

			<g:if test="${projMap}">
			<dl>
				<dt style="margin-left: 10px;">Asset Tag:</dt>
				<dd>${projMap?.asset?.assetTag}</dd>
				<dt style="margin-top: 8px; margin-left: 10px;">Asset Name:</dt>
				<dd style="margin-top: 8px;">${projMap?.asset?.assetName}</dd>
				<dt style="margin-top: 8px; margin-left: 10px;">Manufacturer:</dt>
				<dd style="margin-top: 8px; min-width: 25px;">${projMap?.asset?.manufacturer}</dd>
				<dt style="margin-top: 8px; margin-left: 10px;">Model:</dt>
				<dd style="margin-top: 8px;">${projMap?.asset?.model}</dd>
				<dt style="margin-top: 8px; margin-left: 10px;">Unrack Team:</dt>
				<dd style="margin-top: 8px;">${teamMembers}</dd>
				<dt style="margin-top: 8px; margin-left: 10px;">Cart/Shelf:</dt>
				<dd style="margin-top: 8px;">${projMap?.asset?.cart}/${projMap?.asset?.shelf}</dd>
				<dt style="margin-top: 8px; margin-left: 10px;">Current State:</dt>
				<dd style="margin-top: 8px;">${stateVal}</dd>
			</dl>
			</g:if>
			<g:else>
			<dl>
				<dt style="margin-left: 10px;">Asset Tag:</dt>
				<dd>&nbsp;</dd>
				<dt style="margin-left: 10px;">Asset Name:</dt>
				<dd>&nbsp;</dd>
				<dt style="margin-left: 10px;">Manufacturer:</dt>
				<dd>&nbsp;</dd>
				<dt style="margin-left: 10px;">Model:</dt>
				<dd>&nbsp;</dd>
				<dt style="margin-left: 10px;">Unrack Team:</dt>
				<dd>&nbsp;</dd>
				<dt style="margin-left: 10px;">Cart/Shelf:</dt>
				<dd>&nbsp;</dd>
				<dt style="margin-left: 10px;">Current State:</dt>
				<dd>&nbsp;</dd>
			</dl>
			</g:else>
		</div>
		<g:if test="${browserTest == true}">
			<div style="color: red;">
				<ul>
					<li>Please note that in order to print barcode labels you
						will need to use the Internet Explorer browser</li>
				</ul>
			</div>
		</g:if>
		<div style="width: 100%; height: auto; border: 1px solid #5F9FCF; margin-top: 10px; padding: 10px 0;">
			<span style="position: absolute; text-align: center; width: auto; margin: -17px 0 0 10px; padding: 0px 8px; background: #ffffff;"><b>Label
							Printing</b>
			</span>
			<table style="margin-top: 10px; border: 0px;">
			<g:if test="${projMap && browserTest != true}">
				<tr>
					<td style="width: 85%;"><b>Quantity: </b><select
									name="labels" id="labelQuantity"
									onkeyup="if(event.keyCode == 13 ){startprintjob();}">
										<option value="1">1</option>
										<option value="2" selected="selected">2</option>
										<option value="3">3</option>
										<option value="4">4</option>
								</select> <input type="hidden" id="RepPath" name="RepPath" /> <input
									type="hidden" name="PrjName" id="PrjName" /> <input
									type="hidden" name="FormName" id="FormName" /> <b>Printer:
								</b> <select type="hidden" id="Printers" name="Printers"
									onChange="javascript:mySelect(this);">
										<option value="Zebra (ZPL-II)">Zebra (ZPL-II)</option>
										<g:each in="${session.getAttribute( 'PRINTERS' )}"
											var="printer">
											<option value="${printer}">${printer}</option>
										</g:each>
								</select> <input type="hidden" name="PrinterName" id="PrinterName" /></td>

								<g:if test="${browserTest == true}">
									<td style="width: 15%;" class="buttonR"><input
										id="printButton" type="button" value="Print"
										onclick="startprintjob()" />
									</td>
							</tr>
						</g:if>
						<g:else>
							<td style="width: 15%;" class="buttonR"><input
								id="printButton" type="button" value="Print"
								onclick="startprintjob()" />
							</td>
							</tr>
						</g:else>

						</g:if>
						<g:else>
							<tr>
								<td style="width: 85%;"><b>Quantity: </b> <select
									name="labels" disabled="disabled">
										<option value="1">1</option>
										<option value="2" selected="selected">2</option>
										<option value="3">3</option>
								</select> <input type="hidden" id="RepPath" name="RepPath" /> <input
									type="hidden" name="PrjName" id="PrjName" /> <input
									type="hidden" name="FormName" id="FormName" /> <b>Printer:
								</b><select id="Printers" name="Printers" disabled="disabled"
									onChange="javascript:mySelect(this);" /> <input type="hidden"
									name="PrinterName" id="PrinterName" /></td>
								<td style="width: 15%; align: center; padding-right: 40px;"
									class="buttonR"><input id="printButton" type="button"
									value="Print" disabled="disabled" />
								</td>
							</tr>
						</g:else>
					</table>
				</div>
				<div
					style="width: 100%; height: auto; border: 1px solid #5F9FCF; margin-top: 10px; padding: 10px 0;">
					<span
						style="position: absolute; text-align: center; width: auto; margin: -17px 0 0 10px; padding: 0px 8px; background: #ffffff;"><b>Task</b>
					</span>
					<table style="margin-top: 10px; border: 0px;">
						<tr style="min-height: 300px; width: 95%; height: auto;">

							<td style="width: 90%; border-bottom: 1px solid #5F9FCF;">
								<div
									style="height: auto; min-height: 100px; border: 1px solid #5F9FCF;">
									<table style="min-height: 200px; border: 0px;">
										<thead>
											<tr>
												<th style="background-color: #cccccc; width: 90%;">Instruction/Comments</th>
												<th style="background-color: #cccccc; width: 10%;">Confirmed</th>
											</tr>
										</thead>
										<tbody style="min-height: 200px;">
											<g:each in="${assetComment}" status="i" var="assetComment">
												<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
													<td style="border-right: 1px;">${assetComment?.comment}</td>
													<td style="text-align: center;"><g:if
															test="${assetComment?.mustVerify == 1}">
															<input type="checkbox" id="confirmCheck"
																name="checkChange" onclick="checkInstuction()" />
														</g:if></td>

												</tr>
											</g:each>
										</tbody>
									</table>
								</div></td>
							<td style="width: 14%; border-bottom: 1px solid #5F9FCF;"
								align="center"><div
									style="border-right: 0px solid #5F9FCF; width: 110px; height: auto; min-height: 120px; float: left;">
									<table style="border: 0px;">
										<g:if test="${actionLabel}">
											<tr>
												<td
													style="width: 15%; align: left; padding-right: 20px; float: left"
													class="buttonR" colspan="2"><input id="cleanButton"
													type="button" disabled="disabled" value="${actionLabel}"
													onclick="return clean()" />
												</td>
											</tr>
										</g:if>
										<g:if test="${projMap}">
											<tr>
												<td class="buttonR"
													style="align: left; padding-right: 20px; float: left"
													colspan="2"><input id="cancelButton" type="button"
													value="Cancel" onclick="return cancel()" />
												</td>
											</tr>
										</g:if>
									</table>
								</div></td>
						</tr>
						<tr>
							<td style="width: 85%"><g:select
									style="width: 170px;padding:0px;text-align:left;"
									from="${commentsList}" id="selectCmt" name="selectCmt"
									value="Select a common reason:"
									noSelection="['Select a common reason:':'Select a common reason:']"
									onchange="commentSelect(this.value);"></g:select> <br /> <textarea
									rows="5" cols="98" title="Enter Note..." id="enterNote"
									name="enterNote" onclick="clearComment(this)"
									onkeypress="clearComment(this)"
									onkeydown="textCounter($('#enterNote'), 255)"
									onkeyup="textCounter($('#enterNote'), 255)">Enter Comment</textarea>
							</td>
							<g:if test="${projMap}">
								<td class="buttonClean"
									style="text-align: center; vertical-align: bottom; align: left;"
									colspan="2"><input type="button" value="Add Comment"
									onclick="return doTransition('comment')" /><br />
								<br /> <input type="button" value="Place on HOLD"
									class="placehold" onclick="return doTransition('hold')" /></td>
							</g:if>
						</tr>
					</table>
				</div>
			</g:form>
		</div>
	</div>
	</div>
	<script type="text/javascript">
		function textCounter(obj, maxlimit) {
		      if (obj.val().length > maxlimit) {// if too long...trim it!
		    	  obj.val( obj.val().substring(0, maxlimit) );
			      return false;
		      } else {
		      	return true;
		      }
	     }
		InitData()
		</script>
		<script>
		currentMenuId = "#teamMenuId";
		$("#teamMenuId a").css('background-color','#003366')
       </script>
</body>
</html>
