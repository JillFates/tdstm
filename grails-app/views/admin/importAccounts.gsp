<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Import Accounts</title>
</head>
<body>
	<div class="body">
		<h1>Import Accounts - Step 1 &gt; Upload Import File</h1>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<div>
			<p>
			This form is used to import and manage staff and user login accounts for the current project. To begin you 
			should download a blank spreadsheet template (link below) or use the <i>Export Accounts</i> menu option to 
			manage existing accounts. Upon making changes in the spreadsheet you can upload the file and step through the
			verification process and post your changes to the application.
			</p>

			<p>
				<br>
				Select the spreadsheet file to be uploaded:<br>
				<g:uploadForm action="importAccounts">
					<input type="file" name="${fileParamName}" />
					<input type="hidden" name="step" value="upload" />
					<br />
					<br />
					<label>
						<input type="checkbox" name="verifyProject" value="Y"> YES - I want to import into project ${projectName}
					</label>
					<br />
					<br />
					<input type="submit" value="Upload Spreadsheet" />
				</g:uploadForm> 

				<br>
				<g:link controller="admin" action="importAccountsTemplate">Click here</g:link> to download a blank Account Import template.
			</p>
		</div>
	</div>
</body>
</html>