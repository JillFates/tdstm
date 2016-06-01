<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Import Accounts</title>
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
		<h1>Import Accounts - Step 1 &gt; Upload Import File</h1>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<div>
			<br />
			<div class="panel panel-default account-import-step1">
				<div class="panel-heading">This form is used to import and manage staff and user login accounts for the current project.</div>
				<div class="panel-body">
					<p>
					To begin you should download a blank spreadsheet template (link below) or use the <i>Export Accounts</i> menu option to
					manage existing accounts. Upon making changes in the spreadsheet you can upload the file and step through the
					verification process and post your changes to the application.
					</p>
					<br>
					Select the spreadsheet file to be uploaded:<br>
					<g:uploadForm action="importAccounts">

						<input id="uploadFile" placeholder="Choose File" disabled="disabled" />
						<div class="fileUpload btn btn-default">
							<span>Select</span>
							<input id="uploadBtn" type="file" class="upload" name="${fileParamName}" />
						</div>

						<input type="hidden" name="step" value="upload" />

						<br />
						Import Option:<br>
						<select id="importOption" name="importOption">
							<option value="B" selected>Both Person and UserLogin</option>
							<option value="P">Person only</option>
							<option value="U">UserLogin only</option>
						</select>
						<br>
						<br>
						<button type="submit" class="btn btn-default">
							Upload Spreadsheet
							<span class="uploadIcon glyphicon glyphicon-upload" aria-hidden="true"></span>
						</button>

					</g:uploadForm>

					<br>
					<g:link controller="admin" action="importAccountsTemplate">Click here</g:link> to download a blank Account Import template.
				</div>
			</div>

		</div>
	</div>
</body>
</html>