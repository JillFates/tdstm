<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>My Tasks</title>
<g:javascript library="jquery" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
	 <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<%--<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
	 <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.move_tech_dialog.css')}" /> 
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />--%>
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	
	<%--<g:javascript library="prototype" />
	
	<jq:plugin name="ui.core" />
	<jq:plugin name="ui.dialog" /> --%>
	

	
	<script>
	<%--
	$(document).ready(function() {
	$("#serverInfoDialog").dialog({ autoOpen: false })	       
	})--%>
	</script>
	

    <script type="text/javascript">    	
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
		'<tr><td class="asset_details_block"><b>PDU:</b>  '+asset[0].item.powerPort+'</td></tr>'+
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
        var tab = '${tab}' ;
        if(tab != 'Todo'){
        $('#todoId').children().eq(0).css("backgroundColor","#FFFFFF");
        $('#allId').children().eq(0).css("backgroundColor","#aaefb8");
        }
        $('#search').focus();
        }
        function assetSubmit(searchVal){
        
        $('#search').val(searchVal); 
        $('form#bundleTeamAssetForm').submit();
        
        }
     </script>      
        
</head>
<body>
<%--  <div id="serverInfoDialog" title ="Server Info" onclick="$('#serverInfoDialog').dialog('close');setFocus()">			
</div> 
--%>
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" style="width: 100%;" >
				<div class="colum_techlogin" style="float:left;">
				<div style="float:left; width:200px; margin-left:13px;background-color:none;">
		        	<g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"mt"]' style="height:21px; width:45px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Home</g:link>
					<a href="#" style="height:21px; width:60px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;background:#aaefb8;padding:auto 0px;text-align:center;">My Task</a>
					<a href="#" style="height:21px; width:63px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Asset</a>
				</div>			
				<div class="w_techlog">				
      					<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
      					
					        <input name="bundle" type="hidden" value="${bundle}" />
							<input name="team" type="hidden" value="${team}" />
							<input name="location" type="hidden" value="${location}" />
							<input name="project" type="hidden" value="${project}" />
							<input name="tab" type="hidden" value="${tab}" />								              	
							<div style="float:left; width:210px; margin:2px 0; ">              								
              					<table style="border:0px;width:210px;">
								    <tr>								            										            		
								    <td id="todoId" style="padding-top:4px;padding-left:1px;padding-right:0px;padding-bottom:0px;width:50px;"><g:link  style="color: #5b5e5c; border:1px solid #5b5e5c; margin:2px;background:#aaefb8;padding:2px;" action="assetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link></td>
								    <td id="allId" style="padding-top:4px;padding-left:1px;padding-right:0px;padding-bottom:0px;"><g:link   style="color: #5b5e5c; border:1px solid #5b5e5c; margin:2px; padding:2px;" action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All&nbsp;(${allSize})</g:link></td>
								    <td style="padding-top:0px;padding-left:1px;padding-right:0px;padding-bottom:0px; height:10px;"><input  type="text" size="08" value="" id="search" name="search" style="color: #5b5e5c; border:1px solid #5b5e5c; margin:0px; padding:1px;" /></td>
								    </tr>
								    </table>
						    </div>  
							<div id="mydiv" onclick="$('#mydiv').hide();setFocus()">						            
			   					<g:if test="${flash.message}">
								<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
								</g:if> 
							</div>		
           					<div style="float:left; width:220px; margin:5px 0;"><b>My Tasks:</b></div>
            				<div id="assetTable"style="float:left;width:220px; ">
           					<div style=" width:220px; ">          
             					<table id="assetTable" style="height:80px;">
              					<thead>
                				<tr>
                  				<g:sortableColumn style="color: #5b5e5c; width:60px; border:1px  solid #5b5e5c; margin:2px;background:#aaefb8;padding:2px;" action="assetTask" property="asset_tag" title="AssetTag" params="['bundle':bundle, 'team':team, 'tab':tab,'location':location,'project':project ]"></g:sortableColumn>
                  				<g:sortableColumn style="color: #5b5e5c; width:65px; border:1px  solid #5b5e5c; margin:2px;background:#aaefb8;padding:2px;" action="assetTask" property="source_rack" title="Rack/Pos" params="['bundle':bundle, 'team':team, 'tab':tab,'location':location,'project':project ]"></g:sortableColumn>
                  				<g:sortableColumn style="color: #5b5e5c; border:1px solid #5b5e5c; margin:2px;background:#aaefb8;padding:2px;" action="assetTask" property="model" title="Model" params="['bundle':bundle, 'team':team, 'tab':tab,'location':location,'project':project ]"></g:sortableColumn>
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
      				</g:form>
  				</div>
  	</div>
<script>setFocus();</script>
</body>
</html>
                  				