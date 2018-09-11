<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>My Tasks</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-main.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" type="image/x-icon" href="${assetPath(src: 'images/favicon.ico')}"/>
<meta name="viewport" content="height=device-height,width=220" />
	
	<%--
	<g:javascript src="prototype/prototype.js" />
	<jq:plugin name="jquery.combined" />
	--%>
	
<script type="text/javascript">    	
	window.addEventListener('load', function(){
		setTimeout(scrollTo, 0, 0, 1);
	}, false);

    <%--
	$(document).ready(function() {
	$("#serverInfoDialog").dialog({ autoOpen: false })	       
	})--%>
	<%--  function serverInfo(e){
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
		'<tr><td class="asset_details_block"><b>PDU:</b>  '+asset[0].item.pduPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>NIC:</b>  '+asset[0].item.nicPort+'</td></tr>'+
		'<tr><td class="asset_details_block"><b>HBA:</b>  '+asset[0].item.hbaPort+'</td></tr>'+
		'</tbody></table>' 
        var getDialogId = document.getElementById('serverInfoDialog')
        getDialogId.innerHTML = htmlBody
         $("#serverInfoDialog").dialog('option', 'width', 215)                     
		 $("#serverInfoDialog").dialog('option', 'position', ['left','top']);
         $('#serverInfoDialog').dialog('open');
        }*/
       --%>
        function setFocus(){
	        document.bundleTeamAssetForm.search.focus();
        }
        function assetSubmit(searchVal){
        
        document.bundleTeamAssetForm.search.value = searchVal; 
        document.bundleTeamAssetForm.submit();
        
        }
     </script>      
        
</head>
<body>
	<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" style="width: 220px;" >
		<table border=0 cellpadding=0 cellspacing=0><tr>
		<td><g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt"]' class="home">Home</g:link></td>
		<td><a href="#" class="my_task_select">My Tasks</a></td>
		<td><a href="#" class="asset_search">Asset</a></td>
		</tr></table>

      		<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
      					
	        <input name="bundle" type="hidden" value="${bundle}" />
		<input name="team" type="hidden" value="${team}" />
		<input name="location" type="hidden" value="${location}" />
		<input name="project" type="hidden" value="${project}" />
		<input name="tab" type="hidden" value="${tab}" />								              	
		<div style="float:left; width:210px; margin:2px 0; ">              								
		<table style="border:0px;width:210px;">
		<tr>
			<td id="todoId" class="tab">
				<g:if test="${tab && tab == 'Todo'}">
				  <g:link class="tab_select" action="assetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="assetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:else>
			</td>
			<td id="allId" class="tab">
				<g:if test="${tab == 'All'}">
				  <g:link class="tab_select" action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:else>
			</td>
			<td class="tab_search"><input  type="text" size="08" value="" id="search" name="search" autocorrect="off" autocapitalize="off" /></td>
		</tr>
		</table>
		</div> 
		<div id="mydiv" onclick="this.style.display = 'none';setFocus()">						            
 			<g:if test="${flash.message}">
				<br />
				<div style="color: red;"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>		
           	<div style="float:left; width:220px; margin:5px 5px;"><b>My Tasks:</b></div>
            	<div id="assetTable" style="float:left;width:220px; ">
           		<div style=" width:220px; ">          
             			<table id="assetTable" style="height:80px;">
              				<thead>
                				<tr>
                  				<g:sortableColumn class="sort_column" style="width:60px;" action="assetTask" property="asset_tag" title="AssetTag" params="['bundle':bundle, 'team':team, 'tab':tab,'location':location,'project':project ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column" style="width:65px;" action="assetTask" property="source_rack" title="Rack/Pos" params="['bundle':bundle, 'team':team, 'tab':tab,'location':location,'project':project ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column" action="assetTask" property="model" title="Model" params="['bundle':bundle, 'team':team, 'tab':tab,'location':location,'project':project ]"></g:sortableColumn>
						</tr>
					</thead>
					<tbody>
						<g:each status="i" in="${assetList}" var="assetList">
							<tr class="${assetList.cssVal}"  onclick="assetSubmit('${assetList?.item?.assetTag}');">
								<td class="asset_details_block">${assetList?.item?.assetTag}</td>
								<g:if test="${location == 's'}">
								<td class="asset_details_block">${assetList?.item?.sourceRack}/${assetList?.item?.sourceRackPosition}</td>
								</g:if>
								<g:else>
								<td class="asset_details_block">${assetList?.item?.targetRack}/${assetList?.item?.targetRackPosition}</td>
								</g:else>
								<td class="asset_details_block">${assetList?.item?.model}</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
      		</g:form>
  	</div>
<script type="text/javascript" >setFocus();</script>
</body>
</html>
