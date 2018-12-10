<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Projects Summary Report</title>
</head>
<body>
<tds:subHeader title="Projects Summary Report" crumbs="['Admin','Portal','Projects Summary Report']"/>
<div class="body">
<div class="dialog">
<g:form method="post" name="reportSummaryFormId"  action="projectSummaryReport">
<table style="width:auto !important;">
		<tbody>
			<tr>
				<td>
					<input type="checkbox" name="active" checked="checked"/>
					Active Projects
				</td>
				<td>
					<input type="checkbox" name="inactive"/> Inactive Projects
				</td>
			</tr>
			<tr>
			<td colspan="2" class="buttonR" style="text-align: center;">
				<button type="button" class="btn btn-default" onclick="generateProjectSummary()" value="Generate Web">
					Generate
				</button>
			</td>
		</tr>
		</tbody>
	</table>
</g:form>
</div>
<div id="processDiv" style="display: none;">
	<img src="${resource(dir:'images',file:'processing.gif')}" />
</div>
<br></br>
<div id="projectReportDiv" style="display:none;min-width: 1000px;"></div>
</div>
<script type="text/javascript">
	$(document).ready(function() {
	    currentMenuId = "#adminMenu";
		$('.menu-admin-portal').addClass('active');
		$('.menu-parent-admin').addClass('active');
	});
	function generateProjectSummary(){
		$("#projectReportDiv").hide();
		$("#processDiv").show();
		jQuery.ajax({
			url: $('#reportSummaryFormId').attr('action'),
			data: $('#reportSummaryFormId').serialize(),
			success: function(data) {
				$("#processDiv").hide();
				$("#projectReportDiv").show();
				$("#projectReportDiv").html(data);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while generating Project report Summary.")
			}
		});
	}
</script>
</body>
</html>