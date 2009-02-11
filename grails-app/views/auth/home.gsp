<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>JsecUser List</title>
        <style type="text/css">
        a:hover {
    		text-decoration:underline;
		}
        </style>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
        </div>
        <div class="body">
            <div >&nbsp;</div>
            <div >
                <table>
                    <thead>
                        <tr>
                        
                   	        <th colspan="2">List of Entities</th>
                        
                        </tr>
                    </thead>
                    <tbody>
                        <tr >
							<td><g:link controller="party"
								style="color:black">Party </g:link></td>
							<td><g:link controller="assetType" 
								style="color:black">Asset Type</g:link></td>
						</tr>
						<tr >
							<td><g:link controller="person"
								style="color:black">Person </g:link></td>
							<td><g:link controller="asset" action="assetImport"
								style="color:black">Import </g:link></td>
						</tr>
						<tr >
							<td><g:link controller="userLogin"
								style="color:black">User </g:link></td>
								<td><g:link controller="asset" action="assetExport"
								style="color:black">Export </g:link></td>
						</tr>
						<tr >
							<td><g:link controller="role"
								style="color:black">Role</g:link></td>
								<td><g:link controller="partyRole"
								style="color:black">Party Role</g:link></td>
						</tr>
						
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
