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
	href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
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
        location = asset.sourceLocation
        room = asset.sourceRoom
         rack = asset.sourceRack
         pos = asset.sourceRackPosition
          }else{
          location = asset.targetLocation
        room = asset.targetRoom
         rack = asset.targetRack
         pos = asset.targetRackPosition
          }           
        var htmlBody = '<table ><thead></thead><tbody>'+
'<tr><td style="font-size:9px">Asset Tag:  '+asset.assetTag+'</td></tr>'+
'<tr><td style="font-size:9px">Asset Name:  '+asset.assetName+'</td></tr>'+
'<tr><td style="font-size:9px">Serial Number:  '+asset.serialNumber+'</td></tr>'+
'<tr><td style="font-size:9px">Model:  '+asset.model+'</td></tr>'+
'<tr><td style="font-size:9px">Location:  '+location+'</td></tr>'+
'<tr><td style="font-size:9px">Room:  '+room+'</td></tr>'+
'<tr><td style="font-size:9px">Rack/Position:  '+rack+'/'+pos+'</td></tr>'+
'<tr><td style="font-size:9px">PDU:  '+asset.powerPort+'</td></tr>'+
'<tr><td style="font-size:9px">NIC:  '+asset.nicPort+'</td></tr>'+
'<tr><td style="font-size:9px">HBA:  '+asset.hbaPort+'</td></tr>'+
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
				<div style="float:left; width:92%; margin-left:20px;">
		        	<g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt"]' style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'home.png')}" border="0"/></g:link>
					<a href="#" style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'my_task_h.png')}" border="0" /><a>
					<img  src="${createLinkTo(dir:'images',file:'asset.png')}" style="height:26px; width:64px; float:left; margin:auto 0px;"/>
				</div>			
				<div class="w_techlog" style="overflow-y: scroll; overflow-x: none;">
				
      					<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
      					
					        <input name="bundle" type="hidden" value="${bundle}" />
							<input name="team" type="hidden" value="${team}" />
							<input name="location" type="hidden" value="${location}" />
							<input name="project" type="hidden" value="${project}" />
							<input name="tab" type="hidden" value="Todo" />
							
								              	
								            <div style="float:left; width:100%; margin:5px 0; ">              								
              								   <table style="border:0px;">
								            		<tr>
								            										            		
								            		<td id="todoId" ><g:link  style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;background:#aaefb8;padding:2px;" action="assetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo(${todoSize})</g:link></td>
								            		<td id="allId"><g:link   style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px; padding:2px;" action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All(${allSize})</g:link></td>
								            		<td style="text-align:right;"><input  type="text" size="8" value="" name="search" style="background:url(${createLinkTo(dir:'images',file:'search.png')}) no-repeat center right;"/></td></tr>
								               </table>
								            </div>  
		<div id="mydiv" onclick="document.getElementById('mydiv').style.display = 'none';setFocus()">						            
			   <g:if test="${flash.message}">
	<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
</g:if> 
</div>		
           <div style="float:left; width:100%; margin:5px 0; "><b>My Tasks:</b></div>
            <div id="assetTable"style="float:left;width:100%; ">
            <div  style=" width:100%; ">
          
             <table id="assetTable" style="overflow:scroll;height:80px;">
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
