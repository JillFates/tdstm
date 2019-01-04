<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
		<title>Export Runbook</title>
	</head>
	<script type="text/javascript"></script>
	<body>
		<tds:subHeader title="Export Runbook" crumbs="['Planning','Export Runbook']"/>
		<g:form name="moveEventForm" action="exportRunbookToExcel">
			<div class="body">

				<div class="dialog">
					<table>
						<tr><td>
							<g:select from="${moveEventList}" name="eventId" id="eventList" optionKey="id" optionValue="name" noSelection="['':'please select']" value="${tds.currentMoveEventId()}"/>
						</td></tr>

						<tds:hasPermission permission="${Permission.TaskPublish}">
							<tr>
								<td>
									<input type="checkbox" name="viewUnpublished" id="viewUnpublishedId" ${viewUnpublished=='1' ? 'checked="checked"' : ''} onchange="clearResults()"/>
									Include Unpublished Tasks
								</td>
							</tr>
						</tds:hasPermission>
						<tr>
							<td>
								<button type="button" class="btn btn-default" id="generateId"  onclick="generateRunbook();">
									Summary
									<span class="exportIcon glyphicon glyphicon-list-alt" aria-hidden="true"></span>
								</button>
							</td>
						</tr>
					</table>
				</div>

			</div>
			<div id="exportResultId" style="float: left ; display: none; margin-top: 39px ; margin-left: 40px"></div>
		</g:form>
		<script type="text/javascript">
		currentMenuId = "#eventMenu";
		$(".menu-parent-planning-export-runbook").addClass('active');
		$(".menu-parent-planning").addClass('active');
		function generateRunbook () {
			var moveEvent = $('#eventList').val();
			var boo = false;
			if (moveEvent) {
			boo = true;
			jQuery.ajax({
				url: '/moveEvent/runbookStats',
				data: {'id':moveEvent, 'viewUnpublished':($('#viewUnpublishedId').is(':checked') ? 1 : 0)},
				type:'POST',
				complete: function(e) {
					var resp = e.responseText;
					$('#exportResultId').html(resp);
					$('#exportResultId').css('display','block');
				}
			});
			} else {
				alert("Please Select Event")
			}
			return boo;
		}

		function clearResults () {
			$('#exportResultId').html('');
			$('#exportResultId').css('display','none');
		}
		</script>
	</body>

</html>
