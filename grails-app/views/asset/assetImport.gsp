

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
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <h1>File Import</h1>
      <g:form action="upload" method="post" enctype="multipart/form-data">
        <input type="hidden" value="<%= request.getAttribute("projectId") %>" name="projectIdImport" >
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
                <td class="buttonR"><input class="button" type="submit" value="Upload" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </g:form>

      <h1>File Export</h1>

      <g:form action="export" method="post">
        <input type="hidden" value="<%= request.getAttribute("projectId") %>" name="projectIdExport" >
        <div class="dialog">
          <table>
            <tbody>
              <thead>
                <th>Export</th>
              </thead>

              <tr>
                <td class="buttonR"><input class="button" type="submit" value="Export" /></td>
              </tr>
            </tbody>
          </table>
        </div>
    </g:form></div>
  </body>
</html>
