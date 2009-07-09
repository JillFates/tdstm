<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Asset</title>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}"/>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'cleaning.css')}"/>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}"/>
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
	
	<g:javascript library="prototype" />
	<g:javascript library="jquery" />
	
	<jq:plugin name="ui.core" />
	<jq:plugin name="ui.dialog" />
	<script language="JavaScript" >
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
	<script>
	$(document).ready(function() {
	$("#serverInfoDialog").dialog({ autoOpen: false })	       
	})
	</script>
<script type="text/javascript" language="Javascript1.2"><!--
var sHint = "C:\\temp\\output";
//=============================================================================
// PRINT HERE
//=============================================================================
function startprintjob()
{
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
    }

    

		

    // now we print one copy of the label with default settings
    try 
    {
    	job.PrintForm();
	    $('#printCheck').val('printed');
	    var cleanButton = $('#cleanButton');
	    if(cleanButton != null && !cleanButton.disabled) {
	    	cleanButton.focus();
	    }
	    save_field(document.assetSearchForm.Printers)
	    
    }
    catch (e)
    {
	    alert ("TFORMer returned an error!" + 
	           "\nError description: " + e.description + 
	           "\nError name: " + e.name + 
	           "\nError number: " + e.number + 
	           "\nError message: " + e.message);
    }

}

//=============================================================================
// Add a new option to select element
//=============================================================================
function AddOption (selElement, text, value)
{
  opt = new Option(text, value, false, true);
  selElement.options[selElement.length] = opt;
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
	*-------------------------------------------------------------------------*/
	var printersString = '${session.getAttribute( "PRINTERS" )}'
	var dropdownOptions = printersString.split(",");
	for(opt=0; opt< dropdownOptions.length; opt++){
		var doption = document.createElement("option");
		doption.value = dropdownOptions[opt];
		doption.innerHTML = dropdownOptions[opt];
		dropdown.appendChild( doption );
	}
	retrieve_field(document.assetSearchForm.Printers)
	mySelect(dropdown);
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
        if(loc == 's'){
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
		'<tr><td class="asset_details_block"><b>PDU:</b>  '+asset[0].item.powerPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>NIC:</b>  '+asset[0].item.nicPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>HBA:</b>  '+asset[0].item.hbaPort+'</td></tr>'+
		'</tbody></table>' 
        var getDialogId = document.getElementById('serverInfoDialog')
        getDialogId.innerHTML = htmlBody
        $("#serverInfoDialog").dialog('option', 'width', 200)                     
		$("#serverInfoDialog").dialog('option', 'position', ['left','top']);
        $('#serverInfoDialog').dialog('open');
        } --%>
        
        function validation(){      
        var enterNote = $('#enterNote').val();  
        if(enterNote == "" || enterNote == "Enter Comment"){
      	alert('Please enter note');
        $('#enterNote').focus();   
      	return false;
      	}else{
      	if(confirm('Are you sure?')){
      	return true;
      	}
      	}   
      	}
      	
      	
      	  
      	function doTransition(){
      	if(validation()){
      	$('form#assetSearchForm').attr({action: "placeHold"});
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
        var boxes = document.getElementsByTagName('input'); 
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
      	function setFocus(){ 
        $('#textSearch').focus();
        }
       function commentSelect(cmtVal) {
      $('#enterNote').val(cmtVal);
      $('#selectCmt').val('Select a common reason:');
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
<body onload="InitData()">
<div id="serverInfoDialog" title="Server Info" onclick="$('#serverInfoDialog').dialog('close')">
</div>
<OBJECT id="TF" classid="clsid:18D87050-AAC9-4e1a-AFF2-9D2304F88F7C" CODEBASE="${createLinkTo(dir:'resource',file:'TFORMer60.cab')}"></OBJECT>
		
		<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
			<div class="mainbody" style="width: 100%;">
			<div class="colum_techlogin" style="float: left;">
			<div style="float: left; width: 97.5%; margin-left: 20px; margin-top:;">
		        	<g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"ct"]' style="height:18px; padding-top:3px; width:45px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Home</g:link>
					<g:link action="cleaningAssetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' style="height:18px; padding-top:3px; width:60px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">My Task</g:link>
					<a href="#" style="height:18px; padding-top:3px; width:63px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;background:#aaefb8;padding:auto 0px;text-align:center;">Asset</a>
			</div>
			<div style="float: left; width: 100%; margin: 5px 0;">
				<g:form	name="assetSearchForm" action="cleaningAssetSearch">
					<input name="bundle" type="hidden" value="${bundle}" />
					<input type="hidden" name="printCheck" id="printCheck" value="notprinted"/>
					<input type="hidden" name="urlPath" id="urlPath" value="<g:createLinkTo dir="resource" file="racking_label.tff" absolute="true"/>"/>
					<input name="team" type="hidden" value="${team}" />
					<input name="location" type="hidden" value="${location}" />
					<input name="project" type="hidden" value="${project}" />
					<input name="search" type="hidden" value="${search}" />
					<input name="assetComment" type="hidden" value="${assetComment}" />
					<input name="label" type="hidden" value="${label}" />
					<input name="actionLabel" type="hidden" value="${actionLabel}"  />
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
					
			<div style="float:right;margin-right:10px;margin-top:-20px;">
				<input type="text" name="textSearch" size="10" />&nbsp;<img src="${createLinkTo(dir:'images',file:'search.png')}"/>
			</div>		
			<div id="mydiv" onclick='$("#mydiv").hide()'>
					<g:if test="${flash.message}">
					<div style="color: red;">
					<ul>
					<li>${flash.message}</li>
					</ul>
					</div>
					</g:if></div>
			<div  style="width:100%; height:auto; border:1px solid #5F9FCF; margin-top:10px;padding:10px 0;">
			<span style="position:absolute;text-align:center;width:auto;margin:-17px 0 0 10px;padding:0px 8px; background:#ffffff;"><b>Asset Details</b></span>
					
					<g:if test="${projMap}">
					<dl><dt style="margin-left:10px;">Asset Tag:</dt><dd>${projMap?.asset?.assetTag}</dd>
					<dt style="margin-top:8px;margin-left:10px;">Asset Name:</dt><dd style="margin-top:8px;"> ${projMap?.asset?.assetName}</dd>
					<dt style="margin-top:8px; margin-left:10px;">Manufacturer:</dt><dd style="margin-top:8px;  min-width:25px;"> ${projMap?.asset?.manufacturer}</dd>
					<dt style="margin-top:8px; margin-left:10px;">Model:</dt><dd style="margin-top:8px;"> ${projMap?.asset?.model}</dd>
					<dt style="margin-top:8px;margin-left:10px;">Unrack Team:</dt><dd style="margin-top:8px;"> ${teamMembers}</dd>
					<dt style="margin-top:8px;margin-left:10px;">Cart/Shelf:</dt><dd style="margin-top:8px;"> ${projMap?.asset?.cart}/${projMap?.asset?.shelf}</dd>
					<dt style="margin-top:8px;margin-left:10px;">Current State:</dt><dd style="margin-top:8px;"> ${stateVal}</dd>
					</dl>	
					</g:if>
					<g:else>
					<dl ><dt style="margin-left:10px;">Asset Tag:</dt><dd>&nbsp;</dd>
					<dt style="margin-left:10px; ">Asset Name:</dt><dd>&nbsp;</dd>
					<dt style="margin-left:10px;">Manufacturer:</dt><dd>&nbsp;</dd>
					<dt style="margin-left:10px;">Model:</dt><dd>&nbsp;</dd>
					<dt style="margin-left:10px;">Unrack Team:</dt><dd>&nbsp;</dd>
					<dt style="margin-left:10px;">Cart/Shelf:</dt><dd>&nbsp;</dd>
					<dt style="margin-left:10px;">Current State:</dt><dd>&nbsp;</dd>
					</dl>
					</g:else>
			</div>	
			<g:if test="${browserTest == true}" >
			<div style="color: red;">
					<ul>
					<li>Please note that in order to print barcode labels you will need to use the Internet Explorer browser</li>
					</ul>
					</div>
			</g:if>
			<div  style="width:100%; height:auto; border:1px solid #5F9FCF; margin-top:10px;padding:10px 0;">
					<span style="position:absolute;text-align:center;width:auto;margin:-17px 0 0 10px;padding:0px 8px; background:#ffffff;"><b>Label Printing</b></span>
			<table style="margin-top:10px; border:0px;">
					<g:if test="${projMap && browserTest != true}">	
					<tr>
					<td style="width:85%;"><b>Quantity: </b><select name="labels" >
					<option value="1">1</option>
					<option value="2" selected="selected">2</option>
					<option value="3">3</option>
					</select>
					<input type= "hidden" id="RepPath" name="RepPath">
      	  				<input type= "hidden" name="PrjName" id="PrjName">
          				<input type= "hidden" name="FormName" id="FormName">
	      				<b>Printer: </b><select type= "hidden" id="Printers" name="Printers" onChange="javascript:mySelect(this);"/>
          				<input type= "hidden" name="PrinterName" id="PrinterName">
		  				</td>
					
					<g:if test="${browserTest == true}" >
						<td style="width:15%;"class="buttonR"><input id="printButton" type="button" value="Print"  onclick="startprintjob()"/></td></tr>
					</g:if>
					<g:else>
					<td style="width:15%;"class="buttonR"><input id="printButton" type="button" value="Print"  onclick="startprintjob()"/></td>
					</g:else>
					</tr>
					</g:if>
					<g:else>
					<tr>
					<td style="width:85%;"><b>Quantity: </b> <select name="labels" disabled="disabled">
					<option value="1">1</option>
					<option value="2" selected="selected">2</option>
					<option value="3">3</option>
					</select>
					<input type= "hidden" id="RepPath" name="RepPath">
      	  				<input type= "hidden" name="PrjName" id="PrjName">
          				<input type= "hidden" name="FormName" id="FormName">
          				<b>Printer: </b><select type= "hidden" id="Printers" name="Printers" disabled="disabled" onChange="javascript:mySelect(this);"/>
          				<input type= "hidden" name="PrinterName" id="PrinterName">
						</td>
					<td style="width:15%;" class="buttonR" style=" align:center;padding-right:40px;"><input id="printButton" type="button" value="Print" disabled="disabled"/></td></tr>
					</g:else>		
					<tr>
					</table>
					</div>
					<div  style="width:100%; height:auto; border:1px solid #5F9FCF; margin-top:10px;padding:10px 0;">
					<span style="position:absolute;text-align:center;width:auto;margin:-17px 0 0 10px;padding:0px 8px; background:#ffffff;"><b>Task</b></span>
					<table style="margin-top:10px;border:0px; ">
					<tr style="min-height:300px; width:95%;height:auto; ">
					
					<td style="width:90%;border-bottom:1px solid #5F9FCF;">
					<div  style="height:auto;min-height:100px;border:1px solid #5F9FCF;">
					<table style="min-height:200px;border:0px;">
					<thead >
					<th style="background-color:#cccccc;width:90%;">Instruction/Comments</th>
					<th style="background-color:#cccccc;width:10%;">Confirmed</th></thead>
					<tbody style="min-height:200px;">
					<g:each in="${assetComment}" status="i" var="assetComment">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td style="border-right:1px;">${assetComment?.comment}</td>
					<td style="text-align:center;">
					<g:if test="${assetComment?.mustVerify == 1}" >
					<input type="checkbox" id="confirmCheck"  name="checkChange" onclick="checkInstuction()"/>
					</g:if>
					</td>
					
					</tr>
					</g:each></tbody>
					</table>
					</div>	
			        </td>
					<td  style="width:14%;border-bottom:1px solid #5F9FCF;" align="center"><div style="border-right:0px solid #5F9FCF; width:110px; height:auto; min-height:120px; float:left;">
					<table style="border:0px;">
					<g:if test="${actionLabel}">
					<tr>
					<td  style="width:15%;" class="buttonR" style="align:left; padding-right:20px;float:left" colspan="2"><input id="cleanButton" type="button" disabled="disabled" value="${actionLabel}" onclick="return clean()" /></td>
					</tr>
					</g:if>
					<g:if test="${projMap}">
					<tr>
					<td class="buttonR" style="align:left; padding-right:20px;float:left"  colspan="2"><input id="cancelButton" type="button" value="Cancel"  onclick="return cancel()" /></td>
					</tr>
					</g:if>
					</table>
					</div>
					</td>
					</tr>
					<tr>
					<td style="width:85%">
						
			<g:select style="width: 170px;padding:0px;text-align:left;" from="['Select a common reason:','Device not powered down','Device is not in expected rack','Device will not power up']" id="selectCmt" name="selectCmt" value="Select a common reason:" onchange="commentSelect(this.value);"></g:select>
			<br/>
					<textarea rows="5" cols="98" title="Enter Note..." id="enterNote" name="enterNote" onclick="clearComment(this)" onkeypress="clearComment(this)">Enter Comment</textarea></td>
					
					<g:if test="${projMap}">
					<td class="buttonClean" style="text-align:center;vertical-align:bottom;align:left; " colspan="2"><input  type="button"	value="Place on HOLD" onclick="return doTransition()" /></td>
					</g:if>	
					</tr>				
					</table>
					</div>
				</g:form>
		</div>
		</div>
		</div>
		<script>setFocus();</script>
</body>
</html>
