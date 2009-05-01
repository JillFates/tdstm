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
'<tr><td>Name:  '+asset.assetName+'</td></tr>'+
'<tr><td>Asset Tag:  '+asset.assetTag+'</td></tr>'+
'<tr><td>Serial Number:  '+asset.serialNumber+'</td></tr>'+
'<tr><td>Model:  '+asset.model+'</td></tr>'+
'<tr><td>Location:  '+location+'</td></tr>'+
'<tr><td>Room:  '+room+'</td></tr>'+
'<tr><td>Rack/Position:  '+rack+'/'+pos+'</td></tr>'+
'<tr><td>PDU:  '+asset.powerPort+'</td></tr>'+
'<tr><td>NIC:  '+asset.nicPort+'</td></tr>'+
'<tr><td>HBA:  '+asset.hbaPort+'</td></tr>'+
'</tbody></table>' 
        var getDialogId = document.getElementById('serverInfoDialog')
        getDialogId.innerHTML = htmlBody
         $("#serverInfoDialog").dialog('option', 'width', 200)                     
		 $("#serverInfoDialog").dialog('option', 'position', ['left','center']);
        $('#serverInfoDialog').dialog('open');
        }
        function setFocus(){
        document.bundleTeamAssetForm.search.focus();
        }
        </script>      
        
</head>
<body onload="setFocus()">
<div id="serverInfoDialog" title ="Server Info" onclick="$('#serverInfoDialog').dialog('close');setFocus()">
  
			
    </div>
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin" style="float:left;">
				<div style="float:left; width:100%; margin-left:20px;">
              									
		              									<g:link params='["bundle":bundle,"team":team,"location":location,"project":project]' style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'home.png')}" border="0"/></g:link>
							              				
              											<a href="#" style="height:26px; width:64px; float:left; margin:auto 0px;"><img src="${createLinkTo(dir:'images',file:'my_task_h.png')}" border="0" /><a>
              											
              											<img  src="${createLinkTo(dir:'images',file:'asset.png')}" border="0"/>
								           
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
								            		<td ><g:link style="color: #328714; border:1px solid #328714; margin:5px; background:#ffffff;" action="assetTask" style="color: #328714" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo(${todoSize})</g:link></td>
								            		<td><g:link  style="color: #328714; border:1px solid #328714; margin:5px; background:#ffffff;" action="assetTask" style="color: #328714" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All(${allSize})</g:link></td>
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
                  <th>AssetTag</th>
                  <th>Rack/Pos</th>
                  <th>Model</th>
                 	
				</tr>
               </thead>
               <tbody>
                  <g:each status="i" in="${assetList}" var="assetList">
					<tr class="${assetList.cssVal}" onclick="${remoteFunction(action:'getServerInfo', params:'\'assetId=\'+'+assetList.item.asset.id,onComplete: 'serverInfo(e)')}">
						
						<td>${assetList?.item?.asset.assetTag}</td>
						<g:if test="${location == 's'}">
						<td>${assetList?.item?.asset.sourceRack}/${assetList?.item?.asset.sourceRackPosition}</td>
						</g:if>
						<g:else>
						<td>${assetList?.item?.asset.targetRack}/${assetList?.item?.asset.targetRackPosition}</td>
						</g:else>
						<td>${assetList?.item?.asset.model}</td>						
											
					</tr>
				</g:each>
                 </tbody>
                </table>
        </div>
      </g:form>
  </div>
  </div>
  

  
    
  </body>

</html>
