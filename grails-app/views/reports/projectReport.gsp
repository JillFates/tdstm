<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Projects Summary Report</title>
</head>
<body>
<div class="body">
<h1>Projects Summary Report</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<g:form >
<table>
		<tbody>
			<tr>
				<td></td>
				<td style="width:auto;">
					<input type="checkbox" name="active" checked="checked"/>
					Active Projects
				</td>
			</tr>
			<tr>
				<td></td>
				<td style="width:auto;">
					<input type="checkbox" name="inactive"/> InActive Projects
				</td>
			</tr>
			<tr>
			<td colspan="2" class="buttonR">
				<g:actionSubmit type="submit"  value="Generate Web" action="projectReport" />
			</td>
		</tr>
		</tbody>
	</table>
</g:form>
</div>
</div>
<script type="text/javascript">
	$(document).ready(function() {
	    currentMenuId = "#adminMegaMenu";
	    $("#adminMenuId a").css('background-color','#003366')
	});
</script>
</body>
</html>