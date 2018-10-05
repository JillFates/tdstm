<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Activity Metrics Report</title>
<asset:stylesheet href="css/ui.datepicker.css" />
<asset:stylesheet href="components/comment/comment.css" />
</head>
<body>
<tds:subHeader title="Activity Metrics Report" crumbs="['Reports','Activity','Metrics']"/>
<div class="body">
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
					<label>&nbsp;&nbsp;&nbsp;&nbsp;<b>Projects:<span style="color: red;">*</span> </b></label>
				</td>
				<td valign="top" class="value" align="left">
				<select id="projectId" name="projectId" multiple="multiple" size="6">
					<option value="all" selected="selected">All Projects</option>
					<g:each in="${userProjects}" var="project">
						<option value="${project?.id}">${project?.projectCode}</option>
					</g:each>
				</select>
				</td>
			</tr>
			<tr>
				<td valign="top" class="name" style="paddingleft:10px;vertical-align: middle;">
					<label>&nbsp;&nbsp;&nbsp;&nbsp;<b>Start Date:<span style="color: red;">*</span> </b></label>
				</td>
				<td style="width:auto;">
					<input type="text" class="dateRange" size="15" style="width: 138px;" name="startDate" id="startDate" tabindex="26"
						value="${tds.convertDate(date: startDate, timeZone: tds.timeZone())}" />
				</td>
			</tr>
			<tr>
				<td valign="top" class="name" style="paddingleft:10px;vertical-align: middle;">
					<label>&nbsp;&nbsp;&nbsp;&nbsp;<b>End Date:<span style="color: red;">*</span> </b></label>
				</td>
				<td style="width:auto;">
					<input type="text" class="dateRange" size="15" style="width: 138px;" name="endDate" id="endDate" tabindex="26"
						value="${tds.convertDate(date: endDate, timeZone: tds.timeZone())}" />
				</td>
			</tr>
			<tr>
				<td></td>
				<td style="width:auto;">
					<input type="checkbox" name="includeNonPlanning" />
					Include Non-planning
				</td>
			</tr>
            <tr>
                <td colspan="2" class="buttonR">
                    <button type="submit" name="_action_projectActivityMetricsExport" class="btn btn-default" value="Generate Xls">
                        Generate (.xls)
                        <span class="exportIcon glyphicon glyphicon-download" aria-hidden="true"></span>
                    </button>
                </td>
            </tr>
		</tbody>
	</table>
</g:form>
</div>
</div>
<script type="text/javascript" charset="utf-8">
$(document).ready(function() {
	$(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() });
})
$('.menu-reports-activity-metrics').addClass('active');
$('.menu-parent-reports').addClass('active');
</script>
</body>
</html>
