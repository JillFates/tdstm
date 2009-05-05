<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>My Tasks</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.move_tech_dialog.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.bgiframe.min"/>
<jq:plugin name="jquery.autocomplete"/>
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
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
'<tr><td style="font-size:9px"><b>Asset Tag:</b>  '+asset[0].item.assetTag+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Asset Name:</b>  '+asset[0].item.assetName+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Current State:</b>  '+asset[0].state+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Serial Number:</b>  '+asset[0].item.serialNumber+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Model:</b>  '+asset[0].item.model+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Location:</b>  '+location+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Room:</b>  '+room+'</td></tr>'+
'<tr><td style="font-size:9px"><b>Rack/Position:</b>  '+rack+'/'+pos+'</td></tr>'+
'<tr><td style="font-size:9px"><b>PDU:</b>  '+asset[0].item.powerPort+'</td></tr>'+
'<tr><td style="font-size:9px"><b>NIC:</b>  '+asset[0].item.nicPort+'</td></tr>'+
'<tr><td style="font-size:9px"><b>HBA:</b>  '+asset[0].item.hbaPort+'</td></tr>'+
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
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin" style="float:left;">
				<div style="float:left; width:200px; margin-left:20px;">
		        	<g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt"]' style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'home.png')}" border="0"/></g:link>
					<a href="#" style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'my_task_h.png')}" border="0" /><a>
					<img  src="${createLinkTo(dir:'images',file:'asset.png')}" style="height:26px; width:64px; float:left; margin:auto 0px;"/>
				</div>			
				<div class="w_techlog">
				
      					<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
      					
					        <input name="bundle" type="hidden" value="${bundle}" />
							<input name="team" type="hidden" value="${team}" />
							<input name="location" type="hidden" value="${location}" />
							<input name="project" type="hidden" value="${project}" />
							<input name="tab" type="hidden" value="Todo" />
							
								              	
								            <div style="float:left; width:225px; margin:2px 0; ">              								
              								   <table style="border:0px;width:225px">
								            		<tr>
								            										            		
								            		<td id="todoId" ><g:link  style="color: #5b5e5c; border:1px solid #5b5e5c; margin:2px;background:#aaefb8;padding:2px;" action="assetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo(${todoSize})</g:link></td>
								            		<td id="allId"><g:link   style="color: #5b5e5c; border:1px solid #5b5e5c; margin:2px; padding:2px;" action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All(${allSize})</g:link></td>
								            		<td style="text-align:right; margin:2px; padding:2px;"><input  type="text" size="10" value="" name="search" style="background:url(${createLinkTo(dir:'images',file:'search.png')}) no-repeat center right;margin:2px; padding:2px;"/></td></tr>
								               </table>
								            </div>  
		<div id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none';setFocus()">						            
			   <g:if test="${flash.message}">
	<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
</g:if> 
</div>		
           <div style="float:left; width:225px; margin:5px 0; "><b>My Tasks:</b></div>
            <div id="assetTable"style="float:left;width:225px; ">
            <div  style=" width:225px; ">
          
             <table id="assetTable" style="height:80px;">
              <thead>
                <tr>
                  <th style="font-size:9px">AssetTag</th>
                  <th style="font-size:9px">Rack/Pos</th>
                  <th style="font-size:9px">Model</th>
                 	
				</tr>
               </thead>
               <tbody>
                  <g:each status="i" in="${assetList}" var="assetList">
					<tr class="${assetList.cssVal}" onclick="${remoteFunction(action:'getServerInfo', params:'\'assetId=\'+'+assetList.item.asset.id,onComplete: 'serverInfo(e)')}">
						
						<td style="font-size:9px">${assetList?.item?.asset.assetTag}</td>
						<g:if test="${location == 's'}">
						<td style="font-size:9px">${assetList?.item?.asset.sourceRack}/${assetList?.item?.asset.sourceRackPosition}</td>
						</g:if>
						<g:else>
						<td style="font-size:9px">${assetList?.item?.asset.targetRack}/${assetList?.item?.asset.targetRackPosition}</td>
						</g:else>
						<td style="font-size:9px">${assetList?.item?.asset.model}</td>						
											
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
