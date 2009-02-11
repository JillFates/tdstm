

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>File Import</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
        </div>
        <div class="body">
            <h1>File Import</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="upload" method="post" enctype="multipart/form-data">
                <div class="dialog">
                    <table>
                    <thead>
                    <th>
                    Upload file
                    </th>
                    </thead>
                        <tbody>
                            
						<tr>
							<td><label for="file">File:</label> <input type="file"
								name="file" id="file" /></td>
						</tr>
						<tr>
							<td><label for="file">Project Name:</label> <g:select
								optionKey="id"
								from="${Project.findAll('from Project p order by p.projectName')}"
								name="projectName.id" id="projectNameId"
								value="${project?.projectName?.id}"></g:select></td>
						</tr>
						<tr>
							<td class="buttonR"><input class="button" type="submit" value="Upload" /></td>
						</tr>
					</table>
				
                        
                        </tbody>
                    </table>
                </div>
            </g:form>
        </div>
    </body>
</html>
