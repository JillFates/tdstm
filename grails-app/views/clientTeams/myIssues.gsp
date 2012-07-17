<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
	<title>My Issues</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
</head>
<body>
	<div class="mainbody">
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" >Teams</g:link></li>
			<li><g:link class="mobmenu mobselect" action="listComment" params='["tab":"todo"]'>My Tasks</g:link></li>
			<li><a href="#" class="mobmenu">Details</a></li>
		</ul>
	</div>
	<div class="mobbodyweb">
      	<g:form method="post" name="issueAssetForm" action="showIssue">
			<input id="issueId" name="issueId" type="hidden" value="" />
			<input name="tab" type="hidden" value="${tab}" />								              	
		<div id="mydiv" onclick="this.style.display = 'none';setFocus();">						            
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
				<g:if test="${tab && tab == 'todo'}">
				  <g:link class="tab_select" action="listComment"  params='["tab":"todo","search":search]'>Todo&nbsp;(${todoSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="listComment"  params='["tab":"todo","search":search]'>Todo&nbsp;(${todoSize})</g:link>
				</g:else>
			</td>
			<td id="allId" class="tab">
				<g:if test="${tab == 'all'}">
				  <g:link class="tab_select" action="listComment" params='["tab":"all","search":search]'>All&nbsp;(${allSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="listComment" params='["tab":"all","search":search]'>All&nbsp;(${allSize})</g:link>
				</g:else>
			</td>
			<td class="tab_search"><input  type="text" size="08" value="${search}" id="search" name="search" autocorrect="off" autocapitalize="off" onfocus="changeAction()" onblur="retainAction()"/></td>
			<td class="tab_search">
			    <select id="selectTimedId"
				onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setUpdateTime(e)') }">
				<option value="never">Never</option>
				<option value="30000">30s</option>
				<option value="60000">1m</option>
				<option value="120000">2m</option>
				<option value="300000">5m</option>
				<option value="600000">10m</option>
			</select>
		  </td>
		  <td class="tab_search"><input type="button" id="updateId"
				value="Update" onclick="pageReload();"/>
		</td>
		</tr>
		</table>
		</div>
		<div id="assetIssueDiv" style="float:left;width:220px; ">
			<table id="issueTable" style="height:80px;">
			<thead>
				<tr>
					<g:sortableColumn class="sort_column" style="width:60px;"  action="listComment" property="comment" title="Task" params="['tab':tab]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:100px;" action="listComment" property="date_created" title="Created" params="['tab':tab,]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:100px;" action="listComment" property="due_date" title="Due" params="['tab':tab]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:60px;" action="listComment" property="asset_entity_id" title="Related to" params="['tab':tab]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:60px;" action="listComment" property="status" title="Status" params="['tab':tab]"></g:sortableColumn>
				</tr>
			</thead>
			<tbody>
			<g:each status="i" in="${listComment}" var="issue">
				<tr class="${issue.css}" style="cursor: pointer;" onclick="actionSubmit(${issue?.item?.id})">
					<td class="asset_details_block">${issue?.item?.comment?.size() > 50 ? issue?.item?.comment?.substring(0,40)+'...' : issue?.item?.comment}</td>
					<td class="asset_details_block col2"><tds:convertDate date="${issue?.item?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
					<td class="asset_details_block"><tds:convertDate date="${issue?.item?.dueDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
					<td class="asset_details_block">${issue?.item?.assetEntity?.assetName}</td>
					<td class="asset_details_block">${issue?.item?.status}</td>
				</tr>
			</g:each>
			</tbody>
			</table>
		</div>
		</div>
      		</g:form>
      		<br />
      <div>
      	<g:link class="mobfooter"  saction="listComment" params="[viewMode:'mobile']">Use Mobile Site</g:link>
      </div>
	</div>
 <script type="text/javascript">
$( function() {
	var timers = '${timers}'
	if(timers && timers != "never" ){
   	  $("#selectTimedId").val( ${timers} );
   	  timedUpdate(${timers})
   	}
});

 
function actionSubmit(id){
   $('#issueId').val(id)
   document.issueAssetForm.submit();
}

function changeAction(){
	 document.issueAssetForm.action = 'listComment'
}

function retainAction(){
	 document.issueAssetForm.action = 'showIssue'
}

function setUpdateTime(e) {
	var timeUpdate = eval("(" + e.responseText + ")")
	if(timeUpdate){
		timedUpdate(timeUpdate[0].updateTime.MY_ISSUE_REFRESH)
	}
}
	
var timer
function timedUpdate(timeoutPeriod) {
	if(timeoutPeriod != 'never'){
		clearTimeout(timer)
		timer = setTimeout("pageRefresh()",timeoutPeriod);
	} else {
		clearTimeout(timer)
	}
}
function pageRefresh(){
	document.issueAssetForm.action = 'listComment'
	document.issueAssetForm.submit()
}
function pageReload(){
	window.location.href = document.URL;
}
 </script>
 <script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
 </script>
</body>
</html>