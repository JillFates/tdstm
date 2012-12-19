<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>MoveTech Home</title>
<jq:plugin name="jquery"/>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
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
		src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
	</div>
	<div class="mainbody" style="width: 220px; border:0;" >
	<div class="border_bundle_team" style="border:0px;">
		<table border=0 cellpadding="0" cellspacing="0"><tr>
		<td><a href="#" class="home_select">Home</a></td>
		<td><g:link action="assetTask" params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]' class="my_task">My Tasks</g:link></td>
		<td><a href="#" class="asset_search">Asset</a>
		</tr></table>
			<div class="w_techlog" style="border:0px;">
				<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">      					
					      <input name="bundle" type="hidden" value="${bundle}" />
							<input name="team" type="hidden" value="${team}" />
							<input name="location" type="hidden" value="${location}" />
							<input name="project" type="hidden" value="${project}" />
							<input name="tab" type="hidden" value="Todo" />	
							<input name="home" type="hidden" value="home" />									
				<div style="float:left; width:100%; margin:5px 0; ">              								
					<table style="border:0px;">
						<tr>
							<td style="text-align:left;">
								Scan Asset:<br/>
								<input type="text" size="12" value="" name="search" autocorrect="off" autocapitalize="off" />
							</td>
							<td valign="middle"><g:link controller="moveTech" action="signOut" class="sign_out">Log out</g:link></td>
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
						<dt>Project:&nbsp;</dt><dd>${project}</dd>
						<dt>Bundle:&nbsp;</dt><dd>${bundleName}</dd> 
						<dt>Team:&nbsp;</dt><dd>${projectTeam}</dd>
						<dt>Members:&nbsp;</dt><dd>${members}</dd>              					     
						<dt>Action:&nbsp;</dt><dd>${loc}</dd>
					</dl>
				</div>
				</g:form>
			</div>
		</div>
	</div>
<script type="text/javascript">setFocus();</script>
</body>
</html>
