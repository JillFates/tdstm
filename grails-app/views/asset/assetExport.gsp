

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>File Export</title>
  </head>
  <body>
    <div class="nav"><span class="menuButton"><g:link
      class="home" controller="auth" action="home">Home</g:link></span> <span</div>
    <div class="body">
      <h1>File Export</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if> <g:form action="export" method="post">
        <div class="dialog">
          <table>
            <tbody>
              <thead>
                <th>Export</th>
              </thead>
              <tr>
                <td><label for="project">Project Name:</label> <g:select
                    optionKey="id"
                    from="${Project.findAll()}"
                    name="projectName.id" id="projectNameId"
                  value="${project?.project?.id}"></g:select></td>
              </tr>
              <tr>
                <td class="buttonR"><input class="button" type="submit" value="Export" /></td>
              </tr>
            </tbody>
          </table>
        </div>
    </g:form></div>
  </body>
</html>
