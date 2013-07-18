<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Asset Export</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
  </head>
  <body>
 <div class="body">
   <h1>Asset Export</h1>
	  <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
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
              <tr><td colspan="2">
	                <span><input type="checkbox" id="applicationId" name="application" value="application" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportApplication')"
	                		${prefMap['ImportApplication']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="applicationId">Application</label></span>&nbsp;
	                <span><input type="checkbox" id="assetId" name="asset" value="asset" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportServer')"
	                		${prefMap['ImportServer']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="assetId">Server</label></span>&nbsp;
	                <span><input type="checkbox" id="databaseId" name="database" value="database"
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportDatabase')"
	                	 	${prefMap['ImportDatabase']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="databaseId">Database</label></span>&nbsp;
	                <span><input type="checkbox" id="filesId" name="files" value="files"
	                onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportStorage')"  
	                		${prefMap['ImportStorage']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="filesId">Storage</label></span>&nbsp;
	                <span><input type="checkbox" id="roomId" name="room" value="room" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportRoom')"
	                		${prefMap['ImportRoom']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="roomId">Room</label></span>&nbsp;
	                <span><input type="checkbox" id="rackId" name="rack" value="rack" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportRack')"
	                		${prefMap['ImportRack']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="rackId">Rack</label></span>&nbsp;
	                <span><input type="checkbox" id="dependencyId" name="dependency" value="dependency" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportDependency')"
	                		${prefMap['ImportDependency']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="dependencyId">Dependency</label></span>&nbsp;
	                </td>
               </tr>
               <tr>
              <tds:hasPermission permission="Export ">
                <td class="buttonR">
                	<input class="button" type="submit" value="Generate Standard"/> 
                	<g:link controller="assetEntity" action="exportSpecialReport">
                		<input class="button" type="button" value="Generate Special" onclick="window.location=this.parentNode.href;"/>
                	</g:link>
                </td>
              </tds:hasPermission>
              </tr>
            </tbody>
          </table>
        </div>
    </g:form>
    </div>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
	function importExportPreference(value,forWhom){
		jQuery.ajax({
			url:contextPath+'/assetEntity/setImportPerferences',
			data:{'selected':value, 'prefFor':forWhom}
		});
	}
</script>
  
  
  </body>
