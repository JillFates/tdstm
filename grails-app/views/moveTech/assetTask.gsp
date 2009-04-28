<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>My Tasks</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
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
        
        var asset = eval('(' + e.responseText + ')');       
        var htmlBody = '<table ><thead></thead><tbody>'+
'<tr><td>Name:  '+asset.assetName+'</td></tr>'+
'<tr><td>Asset Tag:  '+asset.assetTag+'</td></tr>'+
'<tr><td>Serial Number:  '+asset.serialNumber+'</td></tr>'+
'<tr><td>Model:  '+asset.model+'</td></tr>'+
'<tr><td>PDU:  '+asset.powerPort+'</td></tr>'+
'<tr><td>NIC:  '+asset.nicPort+'</td></tr>'+
'<tr><td>HBA:  '+asset.hbaPort+'</td></tr>'+
'</tbody></table>' 
        var getDialogId = document.getElementById('serverInfoDialog')
        getDialogId.innerHTML = htmlBody
        $('#serverInfoDialog').dialog('open');
        }
        </script>      
        
</head>
<body>
<div id="serverInfoDialog" title ="Server Info" >
  
			
    </div>
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin">			
				<div class="w_techlog">
				<div style="float:center; width:100%;float:left;text-align:left; height:20px; border-bottom:1px solid #5F9FCF;">
								<span style="text-align:left;font-size:10"><b>Task</b></span>
								</div>
      					<g:form method="post" name="bundleTeamAssetForm">
					        <div class="border_bundle_team">
          						<div style="float:left; margin:5px 0; width:100%; border:1px solid #5F9FCF;">
              									<div style="width:30%; float:left; border-left:1px solid #5585c7; ">
		              										<g:link params='["bundle":bundle,"team":team,"location":location,"project":project]'>Home</g:link></div>
							              				<div style="width:30%; float:left; background-color:#43ca56; border-left:1px solid #5585c7; ">
              											<g:link action="#" >My Tasks</g:link></div>
              											<div style="width:30%; float:left; border-left:1px solid #5585c7; ">
              											<g:link action="assetSearch" params='["bundle":bundle,"team":team,"location":location,"project":project]'>Asset</g:link></div>
								              	</div>
								              	</div>
								              	<div style="float:left; width:100%; margin:5px 0; ">	
              								<div style="float:left; margin:5px; width:20%; border:1px solid #5F9FCF;">
								            	<g:link controller="moveTech" action="signOut" style="color: #328714">Log out</g:link>
								            </div>
								            <div style="float:right; margin:5px; width:20%; border:1px solid #5F9FCF;">
								            	<a href="#" style="color: #328714">Search</a>
								            </div>
								            </div>
              				
           <div style="float:left; width:100%; margin:5px 0; "><b>My Tasks:</b></div>
            <div id="assetTable"style="float:left;width:100%; ">
            <div  style=" width:100%; ">
             <table id="assetTable" style="overflow:scroll;height:80px;">
              <thead>
                <tr>
                <th>Prty</th>
                  <th>Server</th>
                  <th>Rack</th>
                  <th>Type</th>					
				<th>Task</th>		
				</tr>
               </thead>
               <tbody>
                  <g:each status="i" in="${stateAssetList}" var="stateAssetList">
					<tr ondblclick="${remoteFunction(action:'getServerInfo', params:'\'assetId=\'+'+stateAssetList.assetVal.asset.id,onComplete: 'serverInfo(e)')}">
						<td>High</td>
						<td>${stateAssetList?.assetVal.asset.assetName}</td>
						<td>${stateAssetList?.assetVal.asset.sourceRack}</td>
						<td>${stateAssetList?.assetVal.asset.assetType}</td>
						
						<g:if test="${stateAssetList?.stateVal == 'Completed'}">
						<td><g:checkBox name="myCheckbox" value="${true}" /></td>
						</g:if>
						<g:elseif test="${stateAssetList?.stateVal == 'Hold'}">
						<td>Hold!</td>
						</g:elseif>	
						<g:elseif test="${stateAssetList?.stateVal == 'Unracking' && location == 's'}">
						<td>IP</td>
						</g:elseif>	
						<g:elseif test="${stateAssetList?.stateVal == 'Reracking' && location == 't'}">
						<td>IP</td>
						</g:elseif>									
						<g:else>
						<td><g:checkBox name="myCheckbox" value="${false}" /></td>
						</g:else>						
					</tr>
				</g:each>
                 </tbody>
                </table>
        </div>
      </g:form>
  </div>
  </div>
  
  </div>
  
    
  </body>

</html>
