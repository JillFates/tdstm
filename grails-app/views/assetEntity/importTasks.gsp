<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Import Tasks</title>
</head>
<body>
	<script>
		$(document).ready(function() {
			$("#uploadBtn").on('change', function() {
				var fileName  = $(this).val().split(/(\\|\/)/g).pop();
				$("#uploadFile").val(fileName);
			});
		});
	</script>
	<div class="body body-disabled-float">
		<h1>Import Tasks - Step 1 &gt; Upload Import File</h1>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<div>
			<br />
			<div class="panel panel-default account-import-step1">
				<div class="panel-heading">This form is used to bulk import tasks.</div>
				<div class="panel-body">
					Select the spreadsheet file to be uploaded:<br>
					<g:uploadForm action="importTask">

						<input id="uploadFile" placeholder="Choose File" disabled="disabled" />
						<div class="fileUpload btn btn-default">
							<span>Select</span>
							<input id="uploadBtn" type="file" class="upload" name="${fileParamName}" />
						</div>

						<input type="hidden" name="step" value="upload" />

						<br />
						<br>
						<button type="submit" class="btn btn-default">
							Upload Spreadsheet
							<span class="uploadIcon glyphicon glyphicon-upload" aria-hidden="true"></span>
						</button>

					</g:uploadForm>
				</div>
			</div>

		</div>
	</div>
	<script>
		$('.menu-parent-tasks').addClass('active');
		$('.menu-parent-tasks-import-tasks').addClass('active');
	</script>
</body>
</html>