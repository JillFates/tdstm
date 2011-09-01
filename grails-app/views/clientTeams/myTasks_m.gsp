<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>My Tasks</title>
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<%--<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
	 <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.move_tech_dialog.css')}" /> 
	 <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />--%>
<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<meta name="viewport" content="height=device-height,width=220" />
	
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
	function updateOrientation(){
		var displayStr = "Orientation : ";
		switch(window.orientation) {
                    case 0:
			displayStr += "Portrait";
			var elems = document.getElementsByClassName("col2");
			for(var i = 0; i < elems.length; i++) elems[i].style.display = "none";
                    break;
                    case -90:
			displayStr += "Landscape (right, screen turned clockwise)";
			var elems = document.getElementsByClassName("col2");
			for(var i = 0; i < elems.length; i++) elems[i].style.display = "block";
                    break;
                    case 90:
			displayStr += "Landscape (left, screen turned counterclockwise)";
			var elems = document.getElementsByClassName("col2");
			for(var i = 0; i < elems.length; i++) elems[i].style.display = "block";
                    break;
                    case 180:
			displayStr += "Portrait (upside-down portrait)";
			var elems = document.getElementsByClassName("col2");
			for(var i = 0; i < elems.length; i++) elems[i].style.display = "none";
                    break;
                }
		document.bundleTeamAssetForm.search.value = displayStr;
	}
</script>      
</head>
<body onorientationchange="updateOrientation();">
	<div id="spinner" class="spinner" style="display: none;"><img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" style="width: 220px;" >
		<table border=0 cellpadding=0 cellspacing=0><tr>
		<td><g:link class="mobmenu" controller="clientTeams" params="[projectId:project?.id]">Teams</g:link></td>
		<td><g:link action='home' params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId]' class="mobmenu">Home</g:link></td>
		<td><a href="#" class="mobmenu mobselect">Tasks</a></td>
		<td><a href="#" class="mobmenu">Asset</a></td>
		</tr></table>

      		<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
      					
	        <input name="bundleId" type="hidden" value="${bundleId}" />
		<input name="teamId" type="hidden" value="${teamId}" />
		<input name="location" type="hidden" value="${location}" />
		<input name="projectId" type="hidden" value="${projectId}" />
		<input name="tab" type="hidden" value="${tab}" />								              	
		<div style="float:left; width:210px; margin:2px 0; ">              								
		<table style="border:0px;width:210px;">
		<tr>
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
                  				<g:sortableColumn class="sort_column col1" style="width:60px;" action="myTasks" property="asset_tag" title="AssetTag" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column col2" style="width:60px; display:none;" action="myTasks" property="asset_name" title="AssetName" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column col3" style="width:60px;" action="myTasks" property="source_rack" title="Rack/Pos" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column col4" action="myTasks" property="model" title="Model" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
						</tr>
					</thead>
					<tbody>
						<g:each status="i" in="${assetList}" var="assetList">
							<tr class="${assetList.cssVal}"  onclick="assetSubmit('${assetList?.item?.assetTag}');">
								<td class="asset_details_block col1">${assetList?.item?.assetTag}</td>
								<td class="asset_details_block col2" style="display:none;">${assetList?.item?.assetName}</td>
								<g:if test="${location == 's'}">
								<td class="asset_details_block col3">${assetList?.item?.sourceRack}/${assetList?.item?.sourceRackPosition}</td>
								</g:if>
								<g:else>
								<td class="asset_details_block col3">${assetList?.item?.targetRack}/${assetList?.item?.targetRackPosition}</td>
								</g:else>
								<td class="asset_details_block col4">${assetList?.item?.model}</td>
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
