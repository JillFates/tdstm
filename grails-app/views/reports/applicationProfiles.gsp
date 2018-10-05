<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<asset:stylesheet href="css/rackLayout.css" />
		<asset:stylesheet href="css/spinner.css" />
		<title>Application Profiles</title>
		<g:javascript src="report.js"/>
	</head>
	<body>
		<tds:subHeader title="Application Profiles" crumbs="['Reports', 'Profiles']"/>
		<div class="body">

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>

			<g:form action="generateApplicationProfiles" name="applicationProfiles" method="post" onsubmit="return disableGenerateButton(this.name)">
				<table>
					<tbody>
						<tr>
							<td>Bundle : </td>
							<td>
								<g:select from="${moveBundles}" id="moveBundleId" name="moveBundle" onChange="changeSmeSelect(this.value)" optionKey="id" optionValue="name" value="${moveBundleId}"/>
							</td>
						</tr>
						<tbody id="smeAndAppOwnerTbody">
							<g:render template="smeSelectByBundle" model="[smeList:smeList, appOwnerList:appOwnerList, selectedSme:selectedSme, selectedOwner:selectedOwner]" />
						</tbody>
						<tr>
							<td>Maximum Applications to report:</td>
							<td>
								<select id="assetCap" name="report_max_assets">
									<option value="100">100</option>
									<option value="250">250</option>
									<option value="500">500</option>
								</select>
							</td>
						</tr>
						<tr class="buttonR">
							<tds:hasPermission permission="${Permission.ReportViewEventPrep}">
								<td colspan="2">
									<button type="button" class="btn btn-default" id="applicationProfilesButton"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Generate</button>
								</td>
							</tds:hasPermission>
						</tr>
					</tbody>
				</table>
			</g:form>
		</div>


		<div id="overlay">
		    <div id="overlay-wrapper">
		        <div id="floatingBarsG">
		            <div class="blockG" id="rotateG_01"></div>
		            <div class="blockG" id="rotateG_02"></div>
		            <div class="blockG" id="rotateG_03"></div>
		            <div class="blockG" id="rotateG_04"></div>
		            <div class="blockG" id="rotateG_05"></div>
		            <div class="blockG" id="rotateG_06"></div>
		            <div class="blockG" id="rotateG_07"></div>
		            <div class="blockG" id="rotateG_08"></div>
		        </div>
		    </div>
		</div>


		<script type="text/javascript">
			currentMenuId = "#reportsMenu"

			$('.menu-reports-application-profiles').addClass('active');
			$('.menu-parent-reports').addClass('active');

			$(document).ready(function() {
				$("#moveBundleId").prepend("<option value='' disabled >──────────</option>")
				.prepend("<option value='useForPlanning' id='planningBundlesId'>Planning Bundles</option>");
				$("#applicationProfilesButton").removeAttr('disabled');

				$("#applicationProfilesButton").click(function(){
					$("#overlay").css('display', 'inline')
					$("#applicationProfilesButton").attr('disabled', true)
					var form = $("form")[0]
					form.submit()
				})
			});

		</script>
	</body>
</html>
