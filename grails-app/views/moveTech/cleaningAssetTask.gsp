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
	
        
</head>
<body>
<div id="serverInfoDialog" title ="Server Info" >			
</div>
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
		<div class="mainbody" style="width: 100%;">
			<div class="colum_techlogin" style="float:left;">
			<div style="float:left; width:97.5%; margin-left:20px; margin-top:17px;">              									
		         <g:link params='["bundle":bundle,"team":team,"location":location,"project":project,"user":"ct"]' style="height:18px; padding-top:3px; width:45px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Home</g:link>
				 <a href="#" style="height:18px; width:60px; float:left; padding-top:3px; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;background:#aaefb8;padding:auto 0px;text-align:center;">My Task</a>
				 <g:link action="cleaningAssetSearch" params='["bundle":bundle,"menu":"true","team":team,"location":location,"project":project,"user":"ct"]' style="height:18px; padding-top:3px; width:63px; float:left; margin:auto 0px;color: #5b5e5c; border:1px solid #5b5e5c; margin:0px;padding:auto 0px;text-align:center;">Asset</g:link>								           
			</div>			
			<div class="w_techlog" style="margin-top:15px; height:auto;">				
      			<g:form method="post" name="bundleTeamAssetForm" action="cleaningAssetSearch">      					
					<input name="bundle" type="hidden" value="${bundle}" />
					<input name="team" type="hidden" value="${team}" />
					<input name="location" type="hidden" value="${location}" />
					<input name="project" type="hidden" value="${project}" />
					<input name="tab" type="hidden" value="${tab}" />								              	
			<div style="float:left; width:100%; margin:5px 0; ">              								
              		<table style="border:0px;">
					<tr>
					<td id="todoId"><g:link style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;background:#aaefb8;padding:2px;" action="cleaningAssetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link></td>
					<td id="allId"><g:link  style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;padding:2px;margin-right:300px;" action="cleaningAssetTask"  params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"All"]'>All&nbsp;(${allSize})</g:link></td>
					<td style="text-align:right;"><input  type="text" size="10" value="" id="search" name="search" />&nbsp;<img src="${createLinkTo(dir:'images',file:'search.png')}"/></td></tr>
					</table>
					</div>  
		    <div id="mydiv" onclick="$('#mydiv').hide();setFocus()">						            
			   		<g:if test="${flash.message}">
					<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
					</g:if> 
					</div>		
           <div style="float:left;border:0px; width:100%; height:auto; margin:5px 0;font-size:10px "><b>My Tasks:</b></div>
           <div id="assetTable"style="float:left;width:100%;height:auto;">
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
					<tr class="${assetList.cssVal}" onclick="assetSearch('${assetList?.item?.assetTag}')">						
						<td class="asset_details_block">${assetList?.item?.assetTag}</td>
						<g:if test="${location == 's'}">
						<td class="asset_details_block">${assetList?.item?.sourceRack}/${assetList?.item?.sourceRackPosition}</td>
						</g:if>
						<g:else>
						<td class="asset_details_block">${assetList?.item?.targetRack}/${assetList?.item?.targetRackPosition}</td>
						</g:else>
						<td sclass="asset_details_block">${assetList?.item?.model}</td>											
					    </tr>
				        </g:each>
                        </tbody>
                        </table>
        	 
      			</g:form>
  			</div>
  			</div>
  			<script > setFocus();</script>
</body>
</html>
