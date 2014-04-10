<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<title>Application Conflicts</title>
<g:javascript src="report.js"/>
</head>
<body>
	<div class="body">
		<h1>Application Conflicts</h1>
		<div class="message" id="preMoveErrorId" style="display: none">Please select the bundle to start the report.</div>
		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:form action="generateApplicationConflicts" name="applicationConflicts" method="post" onsubmit="return disableGenerateButton(this.name)">
			<table>
				<tbody>
					<tr>
						<td>Bundle: <g:select from="${moveBundles}" id="moveBundleId" name="moveBundle"
								optionKey="id" optionValue="name" value="${moveBundleId}" onChange="changeSmeSelect(this.value,'conflict')"/></td>
					</tr>
					<tbody id="smeAndAppOwnerTbody">
						<g:render template="smeSelectByBundle"  model="[smeList:'', appOwnerList:appOwnerList, forWhom:'conflict']" />
					</tbody>
					<tr>
						<td>
							<input type="checkbox" name="missing" id="missing" checked="checked" />&nbsp;Missing
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="unresolved" id="unresolved" checked="checked" />&nbsp;Unresolved
						</td>
					</tr>
					<tr>
						<td>
							<input type="checkbox" name="conflicts" id="conflicts" checked="checked" />&nbsp;Conflicts
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
						<td><input type="submit" class="submit" value="Generate" id="applicationConflictsButton" onclick="return verifyBundle();"/></td>
					</tds:hasPermission>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>
	<script type="text/javascript">
	
	currentMenuId = "#reportsMenu"
	$("#reportsMenuId a").css('background-color','#003366')
	
	$(document).ready(function() {
		$("#moveBundleId").append("<option value='useForPlanning'>Planning Bundles</option>");
		$("#applicationConflictsButton").removeAttr('disabled');
	});
	function submitForm(form){
		if($("form input:radio:checked").val() == "web"){
			$('#checkListId').html('Loading...')
			jQuery.ajax({
				url: $(form).attr('action'),
				data: $(form).serialize(),
				type:'POST',
				success: function(data) {
					$('#checkListId').html(data)
				}
			});
			return false
		} else {
			return true
		}
	}
	
	function verifyBundle(){
		if( $('#moveBundleId').val() == null ) {
			$('#preMoveErrorId').css('display','block')
			return false
		} else {
			return true
		}
	}
	
	</script>
</body>
</html>
