

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Asset Import/Export</title>
  </head>
  <body>
    <div class="menu2">
      <ul>
        <li><g:link class="home" controller="projectUtil">Project </g:link></li>
        <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectId]" >Staff</g:link></li>
        <li><g:link class="home" controller="asset">Assets </g:link></li>
        <li><g:link class="home" controller="asset" action="assetImport" params="[projectId:projectId]">Import/Export</g:link> </li>
        <li><a href="#">Contacts </a></li>
        <li><a href="#">Applications </a></li>
        <li><g:link class="home" controller="moveBundle" params="[projectId:projectId]">Move Bundles</g:link></li>
      </ul>
    </div>
    <div class="body">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
        <h1>Asset Import</h1>
        <g:form action="upload" method="post" name="importForm" enctype="multipart/form-data">
          <input type="hidden" value="${projectId}" name="projectIdImport" >
          <div class="dialog">
            <table>
              <thead>
                <th colspan="2">
                  Import
                </th>
              </thead>
              <tbody>
              
              	<tr>
                  <td valign="top" class="name">Import Type:</td>
                  <td valign="top" class="value"><select id="dataTransferSet" name="dataTransferSet">                    
                    <g:each status="i" in="${dataTransferSetImport}" var="dataTransferSet">
                      <option value="${dataTransferSet?.id}">${dataTransferSet?.title}</option>
                    </g:each>
                </select></td>
                </tr>

                <tr>
                  <td><label for="file">File:</label></td>
                  <td><input size="40" type="file" name="file" id="file" /></td>
                </tr>              
                
                <tr>
                  <td class="buttonR"><input class="button" type="submit" value="Import Batch" /></td>
                </tr>
                
                <tr>
                	<td valign="top" class="name">Manage Batches:</td>
                	<td valign="top" class="name"><g:link controller="dataTransferBatch" params="[projectId:projectId]">${dataTransferBatchs}</g:link></td>
                </tr>
              </tbody>
            </table>
          </div>
        </g:form>
      
      <h1>Asset Export</h1>

      <g:form action="export" method="post" name="exportForm">
        <input type="hidden" value="${projectId}" name="projectIdExport" >
        <div class="dialog">
          <table>
            <tbody>
            <thead>
              <th colspan="2">Export</th>
            </thead>
            <tbody>
              <tr>
                  <td valign="top" class="name">Export Type:</td>
                  <td valign="top" class="value"><select id="dataTransferSet" name="dataTransferSet">                    
                    <g:each status="i" in="${dataTransferSetExport}" var="dataTransferSet">
                      <option value="${dataTransferSet?.id}">${dataTransferSet?.title}</option>
                    </g:each>
                </select></td>
              </tr>
              <tr>
                <td valign="top" class="name">Bundle(s):</td>
                <td valign="top" class="value"><select MULTIPLE id="bundleId" name="bundle">
                    <option value="" selected="selected">All</option>
                    <g:each status="i" in="${moveBundleInstanceList}" var="moveBundle">
                      <option value="${moveBundle?.id}">${moveBundle?.name}</option>
                    </g:each>
                </select></td>
              </tr>                           
              <tr>
                <td class="buttonR"><input class="button" type="submit" value="Generate" /></td>
              </tr>
            </tbody>
          </table>
        </div>
    </g:form></div>
  </body>
</html>
