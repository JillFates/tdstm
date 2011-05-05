<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="companyHeader" />
    <title>Sync Management</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'progressbar.css')}" />
  </head>
  <body>
   
    <div class="body">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
        <h1 style="font-size: 12px;">Import or export TDS model data. Only synchronizes those with the Source TDS flag.</h1>
        <g:form action="import" method="post" name="importForm" enctype="multipart/form-data" >
          <input type="hidden" value="${projectId}" name="projectIdImport" />
          <div class="dialog">
            <table>
              <thead>
              	<tr><th colspan="2">Import/Export</th></tr>
              </thead>
              <tbody>
              
                <tr>
                  <td><label for="file">File:</label></td>
                  <td><input size="40" type="file" name="file" id="file" />
                  </td>
                </tr>              
                
                <tr>
                  <td class="buttonR"><input class="button" type="submit" value="Import" /></td>
                  <td class="buttonR"><g:actionSubmit value="Export" /></td>
                </tr>
                
                <tr>
                	<td valign="top" class="buttonR"><g:link controller="dataTransferBatch" params="[projectId:projectId]">Manage Imports: ${dataTransferBatchs}</g:link></td>
                	<td valign="top" class="name">&nbsp;</td>
                </tr>
              </tbody>
            </table>
          </div>
        </g:form>
      
    </div>
  </body>
</html>
