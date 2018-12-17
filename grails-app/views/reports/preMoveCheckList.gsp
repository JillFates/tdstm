<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<asset:stylesheet href="css/rackLayout.css" />
<title>Pre-Event Checklist</title>
</head>
<body>
<tds:subHeader title="Pre-Event Checklist" crumbs="['Reports','Pre-Event Checklist']"/>
<div class="body">
		<div class="message" id="preMoveErrorId" style="display: none">Please select the event to start the report.</div>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:form action="generateCheckList" name="preMoveCheck" method="post">
			<table>
				<tbody>
					<tr>
						<td><g:select from="${moveEvents}" id="moveEventId" name="moveEvent"
								optionKey="id" optionValue="name" value="${moveEventId}"/></td>
					</tr>
					<tr>

						<td>
							<div style="width: 150px; float: left;">
								<label><strong>Output:</strong></label>&nbsp;<br />
								<label for="web"><input type="radio" name="output" id="web" checked="checked" value="web" />&nbsp;Web</label><br />
								<%--<label for="pdf"><input type="radio" name="output" id="pdf" value="pdf" />&nbsp;PDF</label><br />--%>
							</div>
						</td>
					</tr>
					<tr class="buttonR">
					<tds:hasPermission permission="${Permission.ReportViewEventPrep}">
						<td>
							<button type="submit" class="btn btn-default" onclick="return verifyEvent();">
								Generate
								<span class="uploadIcon glyphicon glyphicon-check" aria-hidden="true"></span>
							</button>
					</tds:hasPermission>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>
	<script type="text/javascript">
	currentMenuId = "#reportsMenu";
	$('.menu-planning-pre-checklist').addClass('active');
	$('.menu-parent-planning').addClass('active');
	 function submitForm(form){
			if($("form input:radio:checked").val() == "web"){
		    	$('#checkListId').html('Loading...');
				jQuery.ajax({
					url: $(form).attr('action'),
					data: $(form).serialize(),
					type:'POST',
					success: function(data) {
						$('#checkListId').html(data);
					}
				});
		 		return false;
			} else {
				return true
			}
	     }
      function verifyEvent(){
            if($('#moveEventId').val()==null){
                $('#preMoveErrorId').css('display','block')
              return false
            }else{
              return true
            }
      }

	</script>
</body>
</html>
