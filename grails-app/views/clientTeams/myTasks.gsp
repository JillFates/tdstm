<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
	<title>My Tasks</title>
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<%--<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
	 <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.move_tech_dialog.css')}" /> 
	 <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />--%>
<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />

<script type="text/javascript">    	
	window.addEventListener('load', function(){
		setTimeout(scrollTo, 0, 0, 1);
	}, false);

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
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody">
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" params="[projectId:project?.id]">Teams</g:link></li>
			<li><g:link class="mobmenu" action='home' params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId]'>Home</g:link></li>
			<li><g:link class="mobmenu mobselect" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":project?.id,"tab":"Todo"]'>Tasks</g:link></li>
			<li><a href="#" class="mobmenu">Asset</a></li>
		</ul>
	</div>
		<div class="timebar" ><div id="timebar" ></div></div>

      		<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
      					
	        <input name="bundleId" type="hidden" value="${bundleId}" />
		<input name="teamId" type="hidden" value="${teamId}" />
		<input name="location" type="hidden" value="${location}" />
		<input name="projectId" type="hidden" value="${projectId}" />
		<input name="tab" type="hidden" value="${tab}" />								              	
		<div id="mydiv" onclick="this.style.display = 'none';setFocus()">						            
 			<g:if test="${flash.message}">
				<br />
				<div style="color: red;"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>		
		<div style="float:left; width:220px; margin:2px 0; ">              								
		<table style="border:0px;width:220px;">
		<tr>
			<td style="border-bottom:2px solid #507028;"><b>Tasks:</b></td>
			<td id="todoId" class="tab">
				<g:if test="${tab && tab == 'Todo'}">
				  <g:link class="tab_select" action="myTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="myTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:else>
			</td>
			<td id="allId" class="tab">
				<g:if test="${tab == 'All'}">
				  <g:link class="tab_select" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:else>
			</td>
			<td class="tab_search" style="border-bottom:2px solid #507028;"><input  type="text" size="08" value="" id="search" name="search" autocorrect="off" autocapitalize="off" /></td>
		</tr>
		</table>
		</div>
		<br />
            	<div id="assetTable" style="float:left;width:220px; ">
           		<div style=" width:220px; ">          
             			<table id="assetTable" style="height:80px;">
              				<thead>
                				<tr>
                  				<g:sortableColumn class="sort_column" style="width:60px;" action="myTasks" property="asset_tag" title="AssetTag" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column" style="width:60px;display:none;" action="myTasks" property="asset_name" title="AssetName" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column" style="width:60px;" action="myTasks" property="source_rack" title="Rack/Pos" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column" action="myTasks" property="model" title="Model" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
						</tr>
					</thead>
					<tbody>
						<g:each status="i" in="${assetList}" var="assetList">
							<tr class="${assetList.cssVal}"  onclick="assetSubmit('${assetList?.item?.assetTag}');">
								<td class="asset_details_block">${assetList?.item?.assetTag}</td>
								<td class="asset_details_block col2" style="display:none;">${assetList?.item?.assetName}</td>
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
      		<br />
                          	<g:link class="mobbutton" action="list" params="[projectId:projectId, viewMode:'mobile']" class="mobbutton" style="width:75px;">Mobile Site</g:link>
	</div>
<script type="text/javascript">setFocus();</script>
</body>
</html>
