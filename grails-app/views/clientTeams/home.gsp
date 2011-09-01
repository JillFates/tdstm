<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
<title>Team Home</title>
<jq:plugin name="jquery"/>
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<meta name="viewport" content="height=device-height,width=220" />

<style type="text/css">
dt {
	font-weight: bold;
	float: left;
}
</style>
<script type="text/javascript">    	
	function setFocus(){
		document.bundleTeamAssetForm.search.focus();
	}
	function setUserTimeZone( tz ){
		jQuery.ajax({
		        type:"GET",
		        async : true,
		        cache: false,
		        url:"../project/setUserTimeZone?tz="+tz
		});
  	}

	window.addEventListener('load', function(){
	setTimeout(scrollTo, 0, 0, 1);
	}, false);
</script>  
</head>
<body>
	<div id="spinner" class="spinner" style="display: none;"><img
		src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody">
	<div class="border_bundle_team" style="border:0px;">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" params="[projectId:project?.id]">Teams</g:link></li>
			<li><g:link class="mobmenu mobselect" action='home' params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId]'>Home</g:link></li>
			<li><g:link class="mobmenu" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":project?.id,"tab":"Todo"]'>Tasks</g:link></li>
			<li><a href="#" class="mobmenu">Asset</a></li>
		</ul>
			<div class="w_techlog" style="border:0px;">
				<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">      					
					      <input name="bundleId" type="hidden" value="${bundleId}" />
							<input name="teamId" type="hidden" value="${teamId}" />
							<input name="location" type="hidden" value="${location}" />
							<input name="projectId" type="hidden" value="${project?.id}" />
							<input name="tab" type="hidden" value="Todo" />	
							<input name="home" type="hidden" value="home" />									
				<div style="float:left; width:100%; margin:5px 0; ">              								
					<table style="border:0px;">
						<tr>
							<td style="text-align:left;">
								Scan Asset:<br/>
								<input type="text" size="12" value="" name="search" autocorrect="off" autocapitalize="off" />
							</td>
						</tr>
					  </table>
				</div>
				<div style="float:left; width:100%;" id="mydiv" onclick="this.style.display = 'none'">
					<g:if test="${flash.message}">
						<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
					</g:if> 
				</div>  
				<div style="float:left; width:200px; margin:4px;">
					<b>Timezone:</b> <g:select name="timeZone" id="timeZoneId" from="${['GMT','PST','PDT','MST','MDT','CST','CDT','EST','EDT']}" 
                    			value="${session.getAttribute('CURR_TZ')?.CURR_TZ ? session.getAttribute('CURR_TZ')?.CURR_TZ : 'EDT'}" onchange="setUserTimeZone(this.value)"/>
					<br />
					<b>Currently Logged in as:</b>
					<dl compact>
						<dt>Project:&nbsp;</dt><dd>${project?.name}</dd>
						<dt>Bundle:&nbsp;</dt><dd>${bundleName}</dd> 
						<dt>Team:&nbsp;</dt><dd>${projectTeam}</dd>
						<dt>Members:&nbsp;</dt><dd>${members}</dd>              					     
						<dt>Location:&nbsp;</dt><dd>${loc}</dd>
					</dl>
				</div>
				</g:form>
				<table style="border: 0px;">
                <tbody>
                 	<tr>
                    	<td style="height: 2px;" nowrap="nowrap">
                          	<g:link class="home" action="list" params="[projectId:projectId, viewMode:'mobile']" class="mobbutton" style="width:75px;">Mobile Site</g:link>
						</td>
					</tr>
                </tbody>
        		</table>
			</div>
		</div>
	</div>
<script type="text/javascript">setFocus();</script>
</body>
</html>
