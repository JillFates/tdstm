<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Task Report</title>
</head>
<body>
<div class="body">
<h1>Task Report</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<g:form >
	<table>
		<tbody>
			<tr>
				<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>
			<tr>
				<td valign="top" class="name" style="paddingleft:10px;vertical-align: middle;">
					<label>&nbsp;&nbsp;&nbsp;&nbsp;<b>Events:<span style="color: red;">*</span> </b></label>
				</td>
				<td valign="top" class="value" align="left">
				<select id="moveEventId" name="moveEvent" multiple="multiple" size="4">
					<option value="all" selected="selected">All Events</option>
					<g:each in="${moveEventInstanceList}" var="moveEvent">
						<option value="${moveEvent?.id}">${moveEvent?.name}</option>
					</g:each>
				</select>
				</td>
			</tr>
			<tr>
				<td></td>
				<td style="width:auto;">
					<input type="checkbox" name="wComment" checked="checked"/>
					Include comments in report
				</td>
			</tr>
			<tr>
				<td></td>
				<td style="width:auto;">
					<input type="checkbox" name="wUnresolved" checked="checked" />
					Include only remaining tasks in report
				</td>
			</tr>
			<tds:hasPermission permission="PublishTasks">
			<tr>
				<td></td>
				<td style="width:auto;">
					<input type="checkbox" name="viewUnpublished" />
					Include Unpublished Tasks
				</td>
			</tr>
			</tds:hasPermission>
			<tr>
			<td colspan="2" class="buttonR">
				<g:actionSubmit type="submit"  value="Generate Web" action="tasksReport" />
				<g:actionSubmit type="submit"  value="Generate Xls" action="tasksReport"/>
				<g:actionSubmit type="submit"  value="Generate Pdf" action="tasksReport"/>
			</td>
		</tr>
		</tbody>
	</table>
</g:form>
</div>
</div>
<script type="text/javascript">
	$(document).ready(function() {
	    currentMenuId = "#reportsMenu";
	    $("#reportsMenuId a").css('background-color','#003366')
	});
</script>
</body>
</html>