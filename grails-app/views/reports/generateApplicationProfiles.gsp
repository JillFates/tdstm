<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	%{--<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />--}%
	%{--<title>Application Profiles</title>--}%
	%{--<g:javascript src="asset.tranman.js" />--}%
	%{--<g:javascript src="entity.crud.js" />--}%
	%{--<g:javascript src="projectStaff.js" />--}%
	%{--<g:render template="/layouts/responsiveAngularResources" />--}%
	%{--<g:javascript src="asset.comment.js" />--}%
    <link rel="stylesheet" href="/tdstm/assets/css/tds-style.css?compile=false">
    <link rel="stylesheet" href="/tdstm/assets/css/resources/jquery-ui-smoothness.css?compile=false">
    <link rel="stylesheet" href="/tdstm/assets/css/resources/jquery-ui-1.10.4.custom.min.css?compile=false">
</head>
<body>
<style type="text/css">
td[data-for].Y,
td[data-for].G,
td[data-for].B,
td[data-for].P,
td[data-for].O,
td[data-for].H,
td[data-for].U,
td[data-for].N {
    background-color:#FFF !important;
}
/* YELLOW */
td[data-for].Y .select2-choice,
.Y{
    background-color:#FAFF9B !important;
    background-image: none !important;
}
/* GREEN */
td[data-for].G .select2-choice,
.G{
    background-color:#D4F8D4 !important;
    background-image: none !important;
}

/* BLUE */
td[data-for].B .select2-choice,
.B {
    background-color:#A9D6F2 !important;
    background-image: none !important;
}

/* PINK */
td[data-for].P .select2-choice,
.P {
    background-color:#FFA5B4 !important;
    background-image: none !important;
}

/* ORANGE */
td[data-for].O .select2-choice,
.O {
    background-color:#FFC65E !important;
    background-image: none !important;
}

/* NORMAL */
td[data-for].N.label,
td.N.label {
    background-color: #ddd !important;
    background-image: none !important;
}
/* UNIMPORTANT */
td[data-for].U.label,
td.U.label {
    background-color: #F4F4F4 !important;
    background-image: none !important;
}
/* HIDDEN */
td[data-for].H .select2-choice,
.H{
    background-color:#fff !important;
    background-image: none !important;
}
</style>
<style type="text/css" media="print">
<%--Had given these css property in css file but was not reflecting. so defined in page itself--%>
@page {
    size: landscape; /* auto is the current printer page size */
    margin: 4mm; /* this affects the margin in the printer settings */
}

body {
    position: relative;
}

table.tablePerPage {
    page-break-inside: avoid;
    -webkit-region-break-inside: avoid;
    position: relative;
    margin-left:0px !important;
}

div.onepage {
    page-break-after: always;
}
</style>
	<div class="body old-legacy-content content-generate-application-profiles" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets" style="width:100%">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Application Profiles - ${project.name} : ${moveBundle}, SME : ${sme} and App Owner : ${appOwner}</b><br/>
			This report generated on <tds:convertDateTime date="${new Date()}" format="12hrs" /> for ${tds.currentPersonName()}.
		</div>

		<g:each var="appList" in="${applicationList}" var="application" status="i">
			<div class='onepage'>
				<table class="tablePerPage planning-application-table">
					<tbody>
						<tr>
							<th>
								<a href="javascript:void(0);" class="inlineLink"
                                   data-asset-class="${application.app.assetClass}" data-asset-id="${application.app.id}">${application.app.assetName}</a>
									<g:if test="${application.app.moveBundle?.useForPlanning}"> (${application.app.moveBundle})</g:if>
									- Supports ${application.supportAssets.size()} , Depends on ${application.dependentAssets.size()}
							</th>
							<td></td>
						</tr>
						<tr>
							<td colspan="2">
								<g:render template="/application/show"
										  model="[applicationInstance:application.app, customs: customs,
												  project:project, standardFieldSpecs: standardFieldSpecs,
												  shutdownBy: application.shutdownBy, shutdownById: application.shutdownById,
												  startupBy: application.startupBy, startupById: application.startupById,
												  testingBy: application.testingBy, testingById: application.testingById]" >
								</g:render>
							</td>
						</tr>
						<tr id="deps">
							<g:render template="/assetEntity/dependentShow" model="[assetEntity:application.app,supportAssets:application.supportAssets,dependentAssets:application.dependentAssets]" ></g:render>
						</tr>
					</tbody>
				</table>
			</div>
		</g:each>
		<div ng-controller="tds.comments.controller.MainController as comments">
			<jqgrid:wrapper id="applicationId" />
		</div>
		%{--<div id="showEntityView" style="display: none;"></div>--}%
		%{--<div id="editEntityView" style="display: none;"></div>--}%

	</div>
	<g:render template="/assetEntity/initAssetEntityData"/>

	%{--<script type="application/javascript">--}%
		%{--$(document).ready(function() {--}%
			%{--$("#showEntityView").dialog({ autoOpen: false });--}%
			%{--$("#editEntityView").dialog({ autoOpen: false });--}%
			%{--currentMenuId = "#reportsMenu";--}%
			%{--$('.menu-reports-application-profiles').addClass('active');--}%
			%{--$('.menu-parent-reports').addClass('active');--}%
			%{--$('[data-toggle="popover"]').popover();--}%
		%{--});--}%
	%{--</script>--}%

</body>
</html>
