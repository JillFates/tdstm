<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Sync Management</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'progressbar.css')}" />
  </head>
  <body>
   
    <div class="body">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
        <h1 style="font-size: 12px;">Import or export TDS model data. This feature only synchronizes those with the Source TDS flag.</h1>
        <g:form action="upload" method="post" name="importForm" enctype="multipart/form-data" >
          <input type="hidden" value="${projectId}" name="projectIdImport" />
          <div class="dialog">
            <table>
              <thead>
              	<tr><th colspan="3">Import/Export</th></tr>
              </thead>
              <tbody>
              

                <tr>
                  <td><label for="file">File:</label></td>
                  <td><input size="40" type="file" name="file" id="file" /></td>
                  <td class="buttonR"><input class="button" type="submit" value="Import" /></td>
                </tr>
				<tr>
					<td colspan="2">&nbsp;</td>
                	<td valign="top" class="buttonR"><g:link controller="modelSyncBatch" >Manage Imports: ${batchCount}</g:link></td>
                </tr>
				<tr>
					<td colspan="2">&nbsp;</td>
					<td class="buttonR"><g:actionSubmit value="Export" /></td>
				</tr>              
              </tbody>
            </table>
          </div>
        </g:form>
      
    </div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
  </body>
</html>
