<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
<title>Pre-Move Checklist</title>
</head>
<body>

	<div class="body">
		<h1>Pre-Move Checklist</h1>
		<div class="message" id="preMoveErrorId" style="display: none">Please select the event to start the report.</div>
		
		<g:form action="generateCheckList" name="preMoveCheck" method="post">
			<table>
				<tbody>
					<tr>
						<td><g:select from="${moveEvents}" id="moveEventId" name="moveEvent"
								optionKey="id" optionValue="name" /></td>
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
						<td><input type="submit" class="submit" value="Generate" onclick="return verifyEvent();"/></td>
					</tds:hasPermission>
					</tr>
				</tbody>
			</table>
		</g:form>
	</div>
	<script type="text/javascript">
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