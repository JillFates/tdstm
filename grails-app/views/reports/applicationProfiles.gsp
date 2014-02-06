<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<title>Application Profiles</title>
</head>
<body>
	<div class="body">
		<h1>Application Profiles</h1>
		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:form action="generateApplicationProfiles" name="applicationProfiles" method="post">
			<table>
				<tbody>
					<tr>
						<td><g:select from="${moveBundles}" id="moveBundleId" name="moveBundle" onChange="changeSmeSelect(this.value)"
								optionKey="id" optionValue="name" value="${moveBundleId}" noSelection="['useForPlanning':'Use For Planning']"/></td>
					</tr>
					<tr>
						<td>
							<g:render template="smeSelectByBundle"  model="[smeList:smeList]" />
						</td>
					</tr>
					<tr>
						<td>
							<div style="width: 150px; float: left;">
								<label><strong>Output:</strong></label>&nbsp;<br />
								<label for="web"><input type="radio" name="output" id="web" checked="checked" value="web" />&nbsp;Web</label><br />
								<label for="pdf"><input type="radio" name="output" id="pdf" value="pdf" />&nbsp;PDF</label><br />
							</div>
						</td>
					</tr>
					<tr class="buttonR">
					<tds:hasPermission permission="ShowMovePrep">
						<td><input type="submit" class="submit" value="Generate"/></td>
					</tds:hasPermission>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>
	<script type="text/javascript">
		currentMenuId = "#reportsMenu"
		$("#reportsMenuId a").css('background-color','#003366')
		
		function changeSmeSelect(bundle){
			jQuery.ajax({
				url: contextPath+'/reports/generateSmeByBundle',
				data: {'bundle':bundle},
				type:'POST',
				success: function(data) {
					console.log("success");
					$("#smeByModel").html(data)
				}
			});
		}

	</script>
</body>
</html>