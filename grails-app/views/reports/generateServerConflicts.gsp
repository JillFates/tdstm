<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Server Conflicts</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<script>
	var maxR
	var ofst
	var bundleConflicts 
	var unresolvedDependencies
	var noRunsOn
	var vmWithNoSupport
	var moveBundleId
	var appCount
	
	$(document).ready(function() {
		$("#showEntityView").dialog({ autoOpen: false })
		$("#editEntityView").dialog({ autoOpen: false })
		currentMenuId = "#reportsMenu";
		$("#reportsMenuId a").css('background-color','#003366')

		maxR = ${maxR}
		ofst = ${ofst}
		bundleConflicts = '${bundleConflicts}'
		unresolvedDependencies = '${unresolvedDependencies}'
		noRunsOn = '${noRunsOn}'
		vmWithNoSupport = '${vmWithNoSupport}'
		moveBundleId = '${moveBundleId}'
		appCount = ${appCount}
		if(appCount>50)
			generateServers()
	});
	function generateServers(){
		jQuery.ajax({
			url: contextPath+"/reports/generateServerConflicts",
			data: {'rows':maxR, 'appCount':appCount,'offset':ofst, 'bundleConflicts':bundleConflicts, 'unresolvedDep':unresolvedDependencies, 'vmWithNoSupport':vmWithNoSupport, 'moveBundle':moveBundleId, 'noRuns':noRunsOn},
			type:'POST',
			success: function(data) {
				$("#serverConflictTbody").append(data)
				if(ofst<appCount){
					ofst = ofst+maxR;
					generateServers()
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while updating asset.")
			}
		});
	}
</script>
</head>
<body>
	<div class="body" style="width:1000px;">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Server Conflicts - ${project.name} : ${moveBundle} - Includes servers matching: ${title?:'' }</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> for ${session.getAttribute("LOGIN_PERSON").name }.
		</div> 
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}
		<table>
			<tbody id="serverConflictTbody">
			<g:render template="serverConflicts"></g:render>
			</tbody>
		</table>

		
	<div id="showEntityView" style="display: none;"></div>
	<div id="editEntityView" style="display: none;"></div>

	</div>
</body>
</html>
