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
	
	<script>
	$(document).ready(function() {
	$("#serverInfoDialog").dialog({ autoOpen: false })	       
	})
	</script>

	<script type="text/javascript">    
        function serverInfo(e){        
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
        }
        
        function validation(){      
        var enterNote = document.assetSearchForm.enterNote.value;      
        if(enterNote == ""){
      	alert('Please enter note');
        document.assetSearchForm.enterNote.focus();   
      	return false;
      	}else{
      	if(confirm('Are you sure???')){
      	return true;
      	}
      	}   
      	}  
      	function doTransition(){
      	if(validation()){
      	document.assetSearchForm.action="placeHold";
      	document.assetSearchForm.submit();
      	}else {
      	return false;
      	}
      	}
      
      	function clean(){
      	if(doCheckValidation()){  
   		document.assetSearchForm.action = "cleaning";      
   		document.assetSearchForm.submit();
   		}else{
   		return false;
   		}
      	}
      
     	function doCheckValidation(){
	 	var trArray = new Array()
	 	var j = 0;
     	trArray = document.getElementsByName('myCheckbox');        
     	for(i=0; i < trArray.length; i++){         
     	if(trArray[i].checked == false){
     	j=1;
     	}
    	}
     	if(j == 0){     
      	return true;
      	}else{
      	alert('Please select all instructions');                                   
      	return false;
      	}     
      	}
      	function setFocus(){ 
        document.assetSearchForm.textSearch.focus();
        }
           
    </script>
</head>
<body>
<div id="serverInfoDialog" title="Server Info" onclick="$('#serverInfoDialog').dialog('close')">
</div>
		
		<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
			<div class="mainbody" style="width: 100%;">
			<div class="colum_techlogin" style="float: left;">
			<div style="float: left; width: 97.5%; margin-left: 20px;">
		        	<g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"ct"]' style="height:21px; width:45px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Home</g:link>
					<g:link action="cleaningAssetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' style="height:21px; width:60px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">My Task</g:link>
					<a href="#" style="height:21px; width:63px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;background:#aaefb8;padding:auto 0px;text-align:center;">Asset</a>
			</div>
			<div class="w_techlog">
			<div style="float: left; width: 100%; margin: 5px 0;">
				<g:form	name="assetSearchForm" action="cleaningAssetSearch">
					<input name="bundle" type="hidden" value="${bundle}" />
					<input name="team" type="hidden" value="${team}" />
					<input name="location" type="hidden" value="${location}" />
					<input name="project" type="hidden" value="${project}" />
					<input name="search" type="hidden" value="${search}" />
					<input name="assetCommt" type="hidden" value="${assetCommt}" />
					<input name="label" type="hidden" value="${label}" />
					<input name="actionLabel" type="hidden" value="${actionLabel}"  />
					<input name="user" type="hidden" value="ct" />
					<input name="assetPage" type="hidden" value="assetPage" />
					<table style="border: 0px;">
			<div id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none';">
					<g:if test="${flash.message}">
					<div style="color: red;">
					<ul>
					<li>${flash.message}</li>
					</ul>
					</div>
					</g:if></div>
			<div style="float:right;margin-right:10px;">
				<input type="text" name="textSearch" size="10"/>&nbsp;<img src="${createLinkTo(dir:'images',file:'search.png')}"/>
			</div>
			<div onclick="${remoteFunction(action:'getServerInfo', params:'\'assetId=\'+'+projMap.asset.id,onComplete: 'serverInfo(e)')}">
					
					<g:if test="${projMap}">
					<dl><dt>Asset Tag:</dt><dd>${projMap?.asset?.assetTag}</dd>
					<dt>Asset Name:</dt><dd> ${projMap?.asset?.assetName}</dd>
					<dt>Model:</dt><dd> ${projMap?.asset?.model}</dd>
					<dt>Rack/Pos:</dt><dd> <g:if test="${location == 's'}">${projMap?.asset?.sourceRack}/${projMap?.asset?.sourceRackPosition}</g:if><g:else test="${location == 't'}">${projMap?.asset?.targetRack}/${projMap?.asset?.targetRackPosition}</g:else> </dd>
					</dl>	
					</g:if>
					<g:else>
					<dl><dt>Asset Tag:</dt><dd>&nbsp;</dd>
					<dt>Asset Name:</dt><dd>&nbsp;</dd>
					<dt>Model:</dt><dd>&nbsp;</dd>
					<dt>Rack/Pos:</dt><dd>&nbsp;</dd>
					</dl>
					</g:else>
			</div>	
					<g:if test="${projMap}">	
					<tr>
					<td>Labels: <select name="labels">
					<option value="1">1</option>
					<option value="2" selected="selected">2</option>
					<option value="3">3</option>
					</select></td>
					<td class="buttonR"><input type="button" value="Print" onclick="alert('Labels were printed')"/></td>
					</tr>
					</g:if>		
					<tr>
					<td><strong>Instructions</strong></td>
					<td><strong>Confirm</strong></td>
					</tr>
					<g:each status="i" in="${assetCommt}" var="comments">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td >${comments.comment}</td>
					<g:if test="${comments.mustVerify == 1}">
					<td><g:checkBox name="myCheckbox" value="${false}" /></td>
					</g:if>
					<g:else>
					<td></td>
					</g:else>
					</tr>
					</g:each>		
					<g:if test="${actionLabel}">
					<tr>
					<td class="buttonR" style="text-align: right;" colspan="2"><input type="button" value="${actionLabel}" onclick="clean()" /></td>
					</tr>
					</g:if>
					<table>
					<tr>
					<td><textarea rows="2" cols="110" title="Enter Note..." name="enterNote"></textarea></td>
					</tr>
					<g:if test="${projMap}">
					<tr>
					<td class="buttonR" style="text-align: right;"><input type="button"
					value="Place on HOLD" onclick="doTransition()" /></td>
					<tr>
					</g:if>
					</table>
					</table>
				</g:form>
		</div>
		</div>
		</div>
		</div>
<script>setFocus();</script>		
</body>
</html>
