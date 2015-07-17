<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>Export Accounts</title>
</head>
<body>
<div class="body">
<div>
	<h1>Export Accounts</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

		<div>
			<h3>Step 1 - Upload Import File:</h3>


			<g:uploadForm action="exportAccountsProcess">

				<g:select name="company"
	          		from="${partyRelationshipService.getCompaniesList()}"
	          		optionKey="${{it.id}}" />
	          	<br/>
	          	<input type="radio" name="partyRelTypeCode" value="STAFF">All Staff
				<input type="radio" name="partyRelTypeCode" value="PROJ_STAFF" checked>Project Staff<br/>
				<input type="checkbox" name="login" value="Y"> Include Login Information</br>
				<input type="submit" />
			</g:uploadForm> 

</body>
</html>