<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta name="layout" content="projectHeader" />
	<title>Home</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'cleaning.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<script type="text/javascript">
	/*----------------------------------------------------------
	* To load the installed printers into session by initializing TFORMer
	*---------------------------------------------------------*/
	function initializeTF(){
		window.TF.RefreshOSPrinters();
		var def = "";
		var dropdown = new Array();
		for (i = 0; i < window.TF.GetOSPrintersCount(); i++){
			dropdown.push(window.TF.GetOSPrinter(i))
		}

		${remoteFunction(controller:'moveTech', action:'setPrintersIntoSession', params:'\'dropdown=\' + dropdown')}
	}
	function setUserTimeZone( tz ){
		${remoteFunction(controller:'project', action:'setUserTimeZone', params:'\'tz=\' + tz')}
  	}
	</script>
</head>
<body>
<OBJECT id="TF" classid="clsid:18D87050-AAC9-4e1a-AFF2-9D2304F88F7C" CODEBASE="${resource(dir:'resource',file:'TFORMer60.cab')}" style="height:1px;"></OBJECT>
	<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
		<div class="mainbody" style="width: 100%;" >
			<div class="colum_techlogin" style="float:left;">
				<div class="menu4">
				<ul>
					<li><g:link class="mobmenu" controller="clientTeams" params="['projectId':projectId]">Teams</g:link></li>
		        		<li><a href="#" class="mobmenu mobselect">Home</a></li>
					<li><g:link class="mobmenu" action="logisticsMyTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Tasks</g:link></li>
					<li><g:link class="mobmenu" action="logisticsAssetSearch" params='["bundleId":bundleId,"menu":"true","teamId":teamId,"location":location,"projectId":projectId,"user":"ct"]'>Asset</g:link></li>
				</ul>
				</div>
				<div class="border_bundle_team">
				<div id="mydiv" onclick='$("#mydiv").hide()'>
					<g:if test="${flash.message}">
						<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
					</g:if>
				</div>
				<g:if test="${browserTest == true}" >
					<div style="color: red;"><ul><li>Please note that in order to print barcode labels you will need to use the Internet Explorer browser</li></ul></div>
				</g:if>
				<div class="w_techlog" style="margin-top:15px;">
      					<g:form method="post" name="bundleTeamAssetForm">
					<div style="float:left; width:100%; margin:5px 0; ">
              					<table style="border:0px;">
							<tr><td><g:link controller="auth" action="signOut" style="color: #5b5e5c; border:1px solid #5b5e5c; margin:5px;background:#aaefb8;">Log out</g:link></td>
								<td><g:select name="timeZone" id="timeZoneId" from="${['GMT','PST','PDT','MST','MDT','CST','CDT','EST','EDT']}"
	                    				value="${session.getAttribute('CURR_TZ')?.CURR_TZ ? session.getAttribute('CURR_TZ')?.CURR_TZ : 'EDT'}" onchange="setUserTimeZone(this.value)"/>
								</td>
							</tr>
						</table>
					</div>
              				<div style="float:left; width:100%; margin:5px 0; ">
              					<table style="border:0px;">
              					<tr><td colspan="2"><b>Currently Logged in as:</b></td></tr>
              					<tr><td style="width:20px;"><b>Project:</b></td><td>${project}</td></tr>
              					<tr><td style="width:20px;"><b>Bundle:</b></td><td>${bundleName}</td></tr>
              					<tr><td style="width:20px;"><b>Team:</b></td><td>${projectTeam}</td></tr>
              					<tr><td style="width:20px;"><b>Members:</b></td><td>${members}</td></tr>
              					<tr><td style="width:20px;"><b>Location:</b></td><td>${loc}</td></tr>
              					</table>
              				</div>
              				</g:form>
  				</div>
  				</div>
		</div>
		</div>
		<script type="text/javascript">
		initializeTF();
		</script>
		<script>
			currentMenuId = "#teamMenuId";
			$("#teamMenuId a").css('background-color','#003366')
		</script>
</body>
</html>
