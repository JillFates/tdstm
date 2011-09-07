<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="layout" content="projectHeader" />
<title>My Tasks</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'cleaning.css')}"/>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />

	
 	<script type="text/javascript">
	$(document).ready(function() {
	$("#serverInfoDialog").dialog({ autoOpen: false })	       
	})
	</script>
	
   <script type="text/javascript">
  		
        function setFocus(){
	        var tab = '${tab}' ;
	        if(tab != 'Todo'){
		       $('#todoId').children().eq(0).css("backgroundColor","#FFFFFF");
		       $('#allId').children().eq(0).css("backgroundColor","#aaefb8");
	        }
	        $('#search').focus();
        }
        
        function assetSearch(assetTag) {
        	$('#search').val(assetTag)
        	$('form#bundleTeamAssetForm').attr({action: "logisticsAssetSearch"});
       		$('form#bundleTeamAssetForm').submit();
        }
    
   </script>           
</head>
<body>
<div id="serverInfoDialog" title ="Server Info" >			
</div>
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
		<div class="mainbody" style="width: 100%;">
			<div class="colum_techlogin" style="float:left;">
				<div class="menu4">
				<ul>
					<li><g:link class="mobmenu" controller="clientTeams" params="[projectId:projectId]">Teams</g:link></li>
					<li><g:link class="mobmenu" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"user":"ct"]'>Home</g:link></li>
					<li><g:link class="mobmenu mobselect" action="logisticsMyTask" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>My Tasks</g:link></li>
					<li><g:link class="mobmenu" action="logisticsAssetSearch" params='["bundleId":bundleId,"menu":"true","teamId":teamId,"location":location,"projectId":projectId,"user":"ct"]'>Asset</g:link></li>
				</ul>
				</div>
			</div>			
			<div class="w_techlog" style="margin-top:15px; height:auto;">				
      			<g:form method="post" name="bundleTeamAssetForm" action="logisticsAssetSearch">      					
					<input name="bundleId" type="hidden" value="${bundleId}" />
					<input name="teamId" type="hidden" value="${teamId}" />
					<input name="location" type="hidden" value="${location}" />
					<input name="projectId" type="hidden" value="${projectId}" />
					<input name="tab" type="hidden" value="${tab}" />								              	
			<div style="float:left; width:100%; margin:5px 0; ">              								
              		<table style="border:0px;">
					<tr>
					<td id="todoId"><g:link style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;background:#aaefb8;padding:2px;" action="logisticsMyTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link></td>
					<td id="allId"><g:link  style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;padding:2px;margin-right:300px;" action="logisticsMyTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"All"]'>All&nbsp;(${allSize})</g:link></td>
					<td style="text-align:right;"><input  type="text" size="10" value="" id="search" name="search" />&nbsp;<img src="${createLinkTo(dir:'images',file:'search.png')}"/></td></tr>
					</table>
					</div>  
		    <div id="mydiv" onclick="$('#mydiv').hide();setFocus();">						            
			   		<g:if test="${flash.message}">
					<div style="color: red;float: left;">
					<ul><li>${flash.message}</li>					
					<g:each status="i" in="${issuecomments}" var="comments">
						<g:if test="${assetIssueCommentListSize == 1}">
							<dl>
						  		<dt></dt><dd>${comments}</dd>
							</dl>
						</g:if>
						<g:else>
					    	<dl>
							  <dt></dt><dd>${comments}&nbsp;( reason ${i+1} )</dd>
							</dl>
						</g:else>
				  	</g:each>
				  	</ul>
					</div>
					</g:if></div>	
           <div style="float:left;border:0px; width:100%; height:auto; margin:5px 0;font-size:10px "><b>My Tasks:</b></div>
           <div id="assetDiv" style="float:left;width:100%;height:auto;">
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
						<td class="asset_details_block">${assetList?.item?.model}</td>											
					    </tr>
				        </g:each>
                        </tbody>
                        </table>
        	 	</div>
      			</g:form>
  			</div>
  			</div></div>
  			<script type="text/javascript"> setFocus();</script>
</body>
</html>
