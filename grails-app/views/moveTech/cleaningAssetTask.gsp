<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>My Tasks</title>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}"/>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'cleaning.css')}"/>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	
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
        var loc = document.bundleTeamAssetForm.location.value;
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
        function setFocus(){
        var tab = '${tab}' ;
        if(tab != 'Todo'){
        document.getElementById('todoId').firstChild.style.backgroundColor="#FFFFFF"
        document.getElementById('allId').firstChild.style.backgroundColor="#aaefb8"
        }
        document.bundleTeamAssetForm.search.focus();
        }
   </script>      
        
</head>
<body>
<div id="serverInfoDialog" title ="Server Info" onclick="$('#serverInfoDialog').dialog('close');setFocus()">			
</div>
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
		<div class="mainbody" style="width: 100%;">
			<div class="colum_techlogin" style="float:left;">
			<div style="float:left; width:97.5%; margin-left:20px;">              									
		         <g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"ct"]' style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'home.png')}" border="0"/></g:link>							              				
              	 <a href="#" style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'my_task_h.png')}" border="0" /><a>              											
              	 <img  src="${createLinkTo(dir:'images',file:'asset.png')}" style="height:26px; width:64px; float:left; margin:auto 0px;"/>								           
			</div>			
			<div class="w_techlog">				
      			<g:form method="post" name="bundleTeamAssetForm" action="cleaningAssetSearch">      					
					<input name="bundle" type="hidden" value="${bundle}" />
					<input name="team" type="hidden" value="${team}" />
					<input name="location" type="hidden" value="${location}" />
					<input name="project" type="hidden" value="${project}" />
					<input name="tab" type="hidden" value="Todo" />								              	
			<div style="float:left; width:100%; margin:5px 0; ">              								
              		<table style="border:0px;">
					<tr>
					<td id="todoId"><g:link style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;background:#aaefb8;padding:2px;" action="cleaningAssetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link></td>
					<td id="allId"><g:link  style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;padding:2px;" action="cleaningAssetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All&nbsp;(${allSize})</g:link></td>
					<td style="text-align:right;"><input  type="text" size="25" value="" name="search" />&nbsp;<img src="${createLinkTo(dir:'images',file:'search.png')}"/></td></tr>
					</table>
					</div>  
		    <div id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none';setFocus()">						            
			   		<g:if test="${flash.message}">
					<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
					</g:if> 
					</div>		
           <div style="float:left; width:100%; margin:5px 0;font-size:10px "><b>My Tasks:</b></div>
           <div id="assetTable"style="float:left;width:100%; ">
           <div  style=" width:100%; ">          
             	   <table id="assetTable" style="overflow:scroll;height:80px;">
              	   <thead>
                   <tr>
                   <th class="asset_details_block">AssetTag</th>
                   <th class="asset_details_block">Rack/Pos</th>
                   <th class="asset_details_block">Model</th>                 	
				   </tr>
                   </thead>
                   <tbody>
                   <g:each status="i" in="${assetList}" var="assetList">
					<tr class="${assetList.cssVal}" onclick="${remoteFunction(action:'getServerInfo', params:'\'assetId=\'+'+assetList.item.asset.id,onComplete: 'serverInfo(e)')}">						
						<td class="asset_details_block">${assetList?.item?.asset.assetTag}</td>
						<g:if test="${location == 's'}">
						<td class="asset_details_block">${assetList?.item?.asset.sourceRack}/${assetList?.item?.asset.sourceRackPosition}</td>
						</g:if>
						<g:else>
						<td class="asset_details_block">${assetList?.item?.asset.targetRack}/${assetList?.item?.asset.targetRackPosition}</td>
						</g:else>
						<td sclass="asset_details_block">${assetList?.item?.asset.model}</td>											
					    </tr>
				        </g:each>
                        </tbody>
                        </table>
        	 </div>
      			</g:form>
  			</div>
  			</div>
  			<script > setFocus();</script>
</body>
</html>
