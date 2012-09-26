<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Asset Export</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'progressbar.css')}" />
  </head>
  <body>
 <div class="body">
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
              <tds:hasPermission permission="EditAndDelete ">
                <td class="buttonR">
                	<input class="button" type="submit" value="Generate"/> 
                	<g:link controller="assetEntity" action="exportSpecialReport">
                		<input class="button" type="button" value="Export Special Report"/>
                	</g:link>
                </td>
              </tds:hasPermission>
              </tr>
              <tr><td colspan="2">
	                <span><input type="checkbox" id="assetId" name="asset" value="asset" checked="checked"/>&nbsp;<label for="assetId">Asset</label></span>&nbsp;
	                <span><input type="checkbox" id="applicationId" name="application" value="application"/>&nbsp;<label for="applicationId">Application</label></span>&nbsp;
	                <span><input type="checkbox" id="filesId" name="files" value="files"  />&nbsp;<label for="filesId">Files</label></span>&nbsp;
	                <span><input type="checkbox" id="databaseId" name="database" value="database" />&nbsp;<label for="databaseId">Database</label></span>&nbsp;
	                <span><input type="checkbox" id="dependencyId" name="dependency" value="dependency" />&nbsp;<label for="dependencyId">dependency</label></span>&nbsp;
	                </td>
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