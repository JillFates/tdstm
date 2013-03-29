<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta name="layout" content="projectHeader" />
	<title>My Tasks</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'cleaning.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />

	
<script type="text/javascript">
	$(document).ready(function() {
		$("#serverInfoDialog").dialog({ autoOpen: false })
	})

	function setFocus(){
		document.bundleTeamAssetForm.search.value = '';
		document.bundleTeamAssetForm.search.focus();
	}

	function assetSubmit(searchVal){
	document.bundleTeamAssetForm.search.value = searchVal; 
	document.bundleTeamAssetForm.submit();
	}
</script>      
</head>
<body>
	<div id="serverInfoDialog" title="Server Info" ></div>
	<div class="mainbody" style="width: 100%;">
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" params="[projectId:projectId]">Teams</g:link></li>
			<li><g:link class="mobmenu" action="logisticsHome" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"user":"ct"]'>Home</g:link></li>
			<li><g:link class="mobmenu mobselect" action="logisticsMyTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Tasks</g:link></li>
			<li><g:link class="mobmenu" action="logisticsAssetSearch" params='["bundleId":bundleId,"menu":"true","teamId":teamId,"location":location,"projectId":projectId,"user":"ct"]'>Asset</g:link></li>
		</ul>
</div>
	<div class="timebar" ><div id="timebar" ></div></div>
	<div class="mobbodyweb">				
      		<g:form method="post" name="bundleTeamAssetForm" action="logisticsAssetSearch">      					
			<input name="bundleId" type="hidden" value="${bundleId}" />
			<input name="teamId" type="hidden" value="${teamId}" />
			<input name="location" type="hidden" value="${location}" />
			<input name="projectId" type="hidden" value="${projectId}" />
			<input name="tab" type="hidden" value="${tab}" />								              	
		<div id="mydiv" onclick="$('#mydiv').hide();setFocus();">						            
			<g:if test="${flash.message}">
				<div style="color: red;float: left;">
				<ul><li>${flash.message}</li>					
				<g:each status="i" in="${issuecomments}" var="comments">
					<g:if test="${assetIssueCommentListSize == 1}">
						<dl><dt></dt><dd>${comments}</dd></dl>
					</g:if>
					<g:else>
					    	<dl><dt></dt><dd>${comments}&nbsp;( reason ${i+1} )</dd></dl>
					</g:else>
				</g:each>
				</ul>
				</div>
			</g:if>
		</div>
		<div style="float:left; width:100%; margin:5px 0; ">              								
		<table style="border:0px;">
		<tr>
			<td style="border-bottom:2px solid #507028;"><b>My Tasks:</b></td>
			<td id="todoId" class="tab">
				<g:if test="${tab && tab == 'Todo'}">
				  <g:link class="tab_select" action="logisticsMyTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="logisticsMyTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:else>
			</td>
			<td id="allId" class="tab">
				<g:if test="${tab == 'All'}">
				  <g:link class="tab_select" action="logisticsMyTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="logisticsMyTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:else>
			</td>
			<td class="tab_search"><input  type="text" size="10" value="" id="search" name="search" autocorrect="off" autocapitalize="off" />&nbsp;<img src="${resource(dir:'images',file:'search.png')}"/></td>
		</tr>
		</table>
		</div>
		<div id="assetTableDiv" style="float:left;width:400px; ">
			<table id="assetTable" style="height:80px;">
			<thead>
				<tr>
					<g:sortableColumn class="sort_column" style="width:60px;" action="logisticsMyTasks" property="asset_tag" title="AssetTag" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:60px;" action="logisticsMyTasks" property="asset_name" title="AssetName" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:60px;" action="logisticsMyTasks" property="source_rack" title="Rack/Pos" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" action="logisticsMyTasks" property="model" title="Model" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
					<th class="sort_column sortable"><a href="javascript:">Status</a></th>
				</tr>
			</thead>
			<tbody>
			<g:each status="i" in="${assetList}" var="assetList">
				<tr class="${assetList.cssVal}"  onclick="assetSubmit('${assetList?.item?.assetTag}');">
					<td class="asset_details_block">${assetList?.item?.assetTag}</td>
					<td class="asset_details_block col2">${assetList?.item?.assetName}</td>
					<g:if test="${location == 'source'}">
						<td class="asset_details_block">${assetList?.item?.sourceRack}/${assetList?.item?.sourceRackPosition}</td>
					</g:if>
					<g:else>
						<td class="asset_details_block">${assetList?.item?.targetRack}/${assetList?.item?.targetRackPosition}</td>
					</g:else>
					<td class="asset_details_block">${assetList?.item?.model}</td>
					<td class="asset_details_block">${assetList?.nextStatus}</td>
				</tr>
			</g:each>
			</tbody>
			</table>
		</div>
		</div>
      		</g:form>
  		</div>
  		</div></div>
<script type="text/javascript"> setFocus();</script>
<script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
</script>
</body>
</html>
