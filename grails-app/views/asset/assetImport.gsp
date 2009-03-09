

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>File Import</title>
  </head>
  <body>
    <div class="menu2">
      <ul>
        <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectId]" >Staff</g:link></li>
        <li><g:link class="home" controller="asset">Assets </g:link></li>
        <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
        <li><a href="#">Team </a></li>
        <li><a href="#">Contacts </a></li>
        <li><a href="#">Applications </a></li>
        <li><a href="#">Move Bundles </a></li>
      </ul>
    </div>
    <div class="nav">
      <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
    </div>
    <div class="body">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:if test="${!assetsByProject}">
        <h1>File Import</h1>
        <g:form action="upload" method="post" enctype="multipart/form-data">
          <input type="hidden" value="${projectId}" name="projectIdImport" >
          <div class="dialog">
            <table>
              <thead>
                <th colspan="2">
                  Upload file
                </th>
              </thead>
              <tbody>

                <tr>
                  <td><label for="file">File:</label></td>
                  <td> <input type="file" name="file" id="file" /></td>
                </tr>
                <tr>
                  <td valign="top" class="name">TDS Master List:</td>
                  <td valign="top" class="value">
                    <select name="tdsMaster">
                      <option>TDS Master1</option>
                      <option>TDS Master2</option>
                      <option>TDS Master3</option>
                      <option>TDS Master4</option>
                      <option>TDS Master5</option>
                    </select>
                  </td>
                </tr>
                <tr>
                  <td valign="top" class="name">Origin Walk-Through:</td>
                  <td valign="top" class="value">
                    <select name="originWalkThrough">
                      <option>Origin Walk-Through1</option>
                      <option>Origin Walk-Through2</option>
                      <option>Origin Walk-Through3</option>
                      <option>Origin Walk-Through4</option>
                      <option>Origin Walk-Through5</option>
                    </select>
                  </td>
                </tr>
                <tr>
                  <td valign="top" class="name">Target Walk-Through:</td>
                  <td valign="top" class="value">
                    <select name="targetWalkThrough">
                      <option>Target Walk-Through1</option>
                      <option>Target Walk-Through2</option>
                      <option>Target Walk-Through3</option>
                      <option>Target Walk-Through4</option>
                      <option>Target Walk-Through5</option>
                    </select>
                  </td>
                </tr>
                <tr>
                  <td valign="top" class="name">EMC Run Book:</td>
                  <td valign="top" class="value">
                    <select name="emcRunBook">
                      <option>EMC Run Book 1</option>
                      <option>EMC Run Book 2</option>
                      <option>EMC Run Book 3</option>
                      <option>EMC Run Book 4</option>
                      <option>EMC Run Book 5</option>
                    </select>
                  </td>
                </tr>
                <tr>
                  <td class="buttonR"><input class="button" type="submit" value="Upload" /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </g:form>
      </g:if>
      <h1>File Export</h1>

      <g:form action="export" method="post">
        <input type="hidden" value="${projectId}" name="projectIdExport" >
        <div class="dialog">
          <table>
            <tbody>
            <thead>
              <th colspan="2">Export</th>
            </thead>
            <tbody>
              <tr>
                <td valign="top" class="name">TDS Master List:</td>
                <td valign="top" class="value">
                  <select name="tdsMaster">
                    <option>TDS Master1</option>
                    <option>TDS Master2</option>
                    <option>TDS Master3</option>
                    <option>TDS Master4</option>
                    <option>TDS Master5</option>
                  </select>
                </td>
              </tr>
              <tr>
                <td valign="top" class="name">Origin Walk-Through:</td>
                <td valign="top" class="value">
                  <select name="originWalkThrough">
                    <option>Origin Walk-Through1</option>
                    <option>Origin Walk-Through2</option>
                    <option>Origin Walk-Through3</option>
                    <option>Origin Walk-Through4</option>
                    <option>Origin Walk-Through5</option>
                  </select>
                </td>
              </tr>
              <tr>
                <td valign="top" class="name">Target Walk-Through:</td>
                <td valign="top" class="value">
                  <select name="targetWalkThrough">
                    <option>Target Walk-Through1</option>
                    <option>Target Walk-Through2</option>
                    <option>Target Walk-Through3</option>
                    <option>Target Walk-Through4</option>
                    <option>Target Walk-Through5</option>
                  </select>
                </td>
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
