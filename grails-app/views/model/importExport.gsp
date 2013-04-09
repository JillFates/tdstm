<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Sync Management</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
	<script type="text/javascript">
		
	</script>
  </head>
  <body>
   
    <div class="body">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
        <h1>Import</h1>
        <h1 style="font-size: 12px;">Import or export TDS model data. This feature only synchronizes those with the Source TDS flag when checked.</h1>
        <g:form action="upload" method="post" name="importForm" enctype="multipart/form-data" >
          <input type="hidden" value="${projectId}" name="projectIdImport" />
          <div class="dialog">
            <table>
              <thead>
              	<tr><th colspan="2">Import</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td><label for="file">File:</label></td>
                  <td><input size="40" type="file" name="file" id="file" />
                  </td>
                </tr>              
                <tr>
                  	<td><label>Only TDS Models : </label> <g:checkBox id="checkImport" name="importCheckbox" value="${true}" /></td>
                  	<td style="padding-left: 176px;" class="buttonR"><input class="button" type="submit" value="Import" /></td>
                	</tr>
				<tr>
					<td  valign="top" class="buttonR"><g:link controller="modelSyncBatch" >Manage Imports: ${batchCount}</g:link></td>
                	<td >&nbsp;</td>
                </tr>
              </tbody>
            </table>
          </div>
        </g:form>
      
      <h1>Export</h1>

      <g:form action="export" method="post" name="exportForm" >
        <input type="hidden" value="${projectId}" name="projectIdExport" />
        <div class="dialog">
          <table>
            <tbody>
            <thead>
              <tr><th colspan="5">Export</th></tr>
            </thead>
            <tbody>
              <tr>
				<td><label>Only TDS Models : </label> <g:checkBox id="checkImport" name="exportCheckbox" value="1"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" /></td>
				<td style="padding-right: auto;" class="buttonR"><g:actionSubmit value="Export"  style ="margin-left: 102px;"/>
			  </tr>             
              </tbody>
          </table>
        </div>
    </g:form>
    </div>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
  </body>
</html>
