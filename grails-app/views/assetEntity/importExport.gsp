<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Asset Import/Export</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'progressbar.css')}" />
	<script type="text/javascript">
		/* ---------------------------------
		 * 	Author : Lokanada Reddy		
		 *	function to show the Progress bar
		 * ------------------------------- */
		var handle=0;
		function showProcessBar(e){
			var progress = eval('(' + e.responseText + ')');
			if(progress){
				$("#progressbar").reportprogress(progress[0].imported,progress[0].total);
		        if(progress[0].imported==progress[0].total){
		                clearInterval(handle);
		        }
			}
		}
		/* ---------------------------------
		 * 	Author : Lokanada Reddy
		 *	JQuery function to set the interval to display Progress
		 * ------------------------------- */
		jQuery(function($){
	        $("#run").click(function(){
	        	$("#progressbar").css("display","block")
	        	clearInterval(handle);
				handle=setInterval("${remoteFunction(action:'getProgress', onComplete:'showProcessBar(e)')}",500);
	        });
		});
	</script>
  </head>
  <body>
   
    <div class="body">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
        <h1>Asset Import</h1>
        <g:form action="upload" method="post" name="importForm" enctype="multipart/form-data" >
          <input type="hidden" value="${projectId}" name="projectIdImport" />
          <div class="dialog">
            <table>
              <thead>
              	<tr><th colspan="2">Import</th></tr>
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
                  <td><input size="40" type="file" name="file" id="file" />
                  </td>
                </tr>              
                
                <tr>
                  <td >&nbsp;</td>
                  <td ><div id="progressbar" style="display: none;" ></div></td>
                </tr>
                <tr>
                  <td class="buttonR"><input class="button" id="run" type="submit" value="Import Batch" /></td>
                </tr>
                
                <tr>
                	<td valign="top" class="buttonR"><g:link controller="dataTransferBatch" params="[projectId:projectId]">Manage Batches: ${dataTransferBatchs}</g:link></td>
                	<td valign="top" class="name">&nbsp;</td>
                </tr>
              </tbody>
            </table>
          </div>
        </g:form>
      
      <h1>Asset Export</h1>

      <g:form action="export" method="post" name="exportForm">
        <input type="hidden" value="${projectId}" name="projectIdExport" />
        <div class="dialog">
          <table>
            <tbody>
            <thead>
              <tr><th colspan="5">Export</th></tr>
            </thead>
            <tbody>
              <tr>
                  <td valign="top" class="name">Export Type:</td>
                  <td valign="top" class="value" colspan="4"><select id="dataTransferSet" name="dataTransferSet">                    
                    <g:each status="i" in="${dataTransferSetExport}" var="dataTransferSet">
                      <option value="${dataTransferSet?.id}">${dataTransferSet?.title}</option>
                    </g:each>
                </select></td>
              </tr>
              <tr>
                <td valign="top" class="name">Bundle(s):</td>
                <td valign="top" class="value"  colspan="4"><select MULTIPLE id="bundleId" name="bundle">
                    <option value="" selected="selected">All</option>
                    <g:each status="i" in="${moveBundleInstanceList}" var="moveBundle">
                      <option value="${moveBundle?.id}">${moveBundle?.name}</option>
                    </g:each>
                </select></td>
              </tr>                           
              <tr>
                <td class="buttonR" colspan="5"><input class="button" type="submit" value="Generate"/></td>
              </tr>
              <tr>
                <td><input type="checkbox" id="assetId" name="asset" value="asset" checked="checked"/>&nbsp;<label for="assetId">Asset</label></td>
                <td><input type="checkbox" id="applicationId" name="application" value="application"/>&nbsp;<label for="applicationId">Application</label></td>
                <td><input type="checkbox" id="filesId" name="files" value="files"  />&nbsp;<label for="filesId">Files</label></td>
                <td><input type="checkbox" id="databaseId" name="database" value="database" />&nbsp;<label for="databaseId">Database</label></td>
                <td><input type="checkbox" id="dependencyId" name="dependency" value="dependency" />&nbsp;<label for="dependencyId">dependency</label></td>
                </tr>
            </tbody>
          </table>
        </div>
    </g:form>
    </div>
    <script type="text/javascript">
		$('#assetMenu').show();
		$('#reportsMenu').hide();
	</script>
  </body>
</html>
