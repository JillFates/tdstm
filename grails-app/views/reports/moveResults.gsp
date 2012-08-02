<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Move Results Report</title>
<script type="text/javascript">
	$(document).ready(function() {
	    currentMenuId = "#reportsMenu";
	    $("#reportsMenuId a").css('background-color','#003366')
	});
</script>
</head>
<body>

<div class="body">
<h1>Move Results Report</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<g:form controller="moveEvent" action="getMoveResults">
<table>
	<tbody>
		<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label><b>Move Event:<span style="color: red;">*</span></b></label></td>
			<td valign="top" class="value">
				<select name="moveEvent" onchange="$('#moveEvent').val(this.value)">
					<option value="">Please Select</option>
					<g:each in="${moveEventInstanceList}" var="moveEventList">
						<option value="${moveEventList?.id}">${moveEventList?.name}</option>
					</g:each>
				</select>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label><b>Report Type:<span style="color: red;">*</span></b></label></td>
			<td valign="top" class="value">
				<select name="reportType" onchange="$('#moveEvent').val(this.value)">
					<option value="">Please Select</option>
					<option value="SUMMARY">Summary Report</option>
					<option value="DETAILED">Detailed Report</option>				
				</select>
			</td>
		</tr>
		<tr>
			<td class="buttonR" colspan="2">
				<input class="button" type="submit" name="generate" value="Generate XLS"/>
				<g:actionSubmit value="Generate PDF" action="getMoveEventResultsAsPDF"/>
				<g:actionSubmit value="Generate WEB" action="getMoveEventResultsAsWEB"/>
			</td>
		</tr>
	</tbody>
</table>
</g:form>
</div>
</div>
</body>
</html>
