<head>
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
			$('.menu-reports-server-conflicts').addClass('active');
			$('.menu-parent-reports').addClass('active');

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
	%{--<tds:subHeader title="Server Conflicts" crumbs="['Reports','Server Conflicts']"/>--}%
	<div class="body" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets" style="width:100%;">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>${project.name} : ${moveBundle} - Includes servers matching: ${title?:'' }</b><br/>
			This analysis was performed on <tds:convertDateTime date="${new Date()}" format="12hrs" /> for ${tds.currentPersonName()}.
		</div>
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}
		<table class="planning-application-table">
			<tbody id="serverConflictTbody">
				<g:render template="/reports/serverConflicts"></g:render>
			</tbody>
		</table>

		<div ng-controller="tds.comments.controller.MainController as comments">
			<jqgrid:wrapper id="applicationId" />
		</div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>

	</div>
</body>
</html>
