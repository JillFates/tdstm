<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Asset Import</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
	<g:javascript src="jquery/ui.progressbar.js"/>
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
	        	if(${isMSIE}){
	        		handle=setInterval("${remoteFunction(action:'getProgress', onComplete:'showProcessBar(e)')}",500);
		        } else {
					handle=setInterval(getProgress,1500);
		        }
	        });
		});


		//This code is used to display progress bar at chrome as Chrome browser cancel all ajax request while uploading .
		function getProgress(){	
			 $("#iFrame").attr('src', contextPath+'/assetEntity/getProgress');
		}
		function onIFrameLoad() {
		   var serverResponse = $("#iFrame").contents().find("pre").html();
		   var jsonProgress
		   if(serverResponse)
		     jsonProgress = JSON.parse( serverResponse )
		   if(jsonProgress){
			   $("#progressbar").reportprogress(jsonProgress[0].imported,jsonProgress[0].total);
		       if(jsonProgress[0].imported==jsonProgress[0].total){
	  	         clearInterval(handle);
		       }
		   }
		 }
	</script>
	
  </head>
  <body>
  	<iframe id='iFrame' class="iFrame" onload='onIFrameLoad()'></iframe>   
    <div class="body">
    <g:if test="${warnMsg || (message && args) }">
    	<div class="message"><g:message code="${flash.message}" args="${args}" /> <br/>${warnMsg} </div>
    </g:if>
    <g:elseif test="${!args && flash.message}">
    	<div class="message">${flash.message}</div>
    </g:elseif>
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
                <tds:hasPermission permission="Import">
                  <td class="buttonR"><input class="button" id="run" type="submit" value="Import Batch" /></td>
                 </tds:hasPermission>
                </tr>
                
                <tr>
                	<td valign="top" class="buttonR"><g:link controller="dataTransferBatch" >Manage Batches: ${dataTransferBatchs}</g:link></td>
                	<td valign="top" class="name">&nbsp;</td>
                	 
                </tr>
                <tr><td colspan="2">
	                <span><input type="checkbox" id="applicationId" name="application" value="application" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportApplication')" 
	                		${prefMap['ImportApplication']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="applicationId">Application</label></span>&nbsp;
	                <span><input type="checkbox" id="assetId" name="asset" value="asset" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportServer')"
	                		${prefMap['ImportServer'] =='TRUE'? 'checked="checked"' :''}/>&nbsp;
	                <label for="assetId">Server</label></span>&nbsp;
	                <span><input type="checkbox" id="databaseId" name="database" value="database" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportDatabase')"
	                		${prefMap['ImportDatabase'] =='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="databaseId">Database</label></span>&nbsp;
	                <span><input type="checkbox" id="filesId" name="files" value="files"  
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportStorage')"
	                		${prefMap['ImportStorage']=='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="filesId">Storage</label></span>&nbsp;
	                <span><input type="checkbox" id="dependencyId" name="dependency" value="dependency" 
	                		onclick="if(this.checked){this.value = 'TRUE' } else {this.value = 'FALSE'};importExportPreference(this.value,'ImportDependency')"
	                		${prefMap['ImportDependency'] =='TRUE' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="dependencyId">Dependency</label></span>&nbsp;
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
	
	function importExportPreference(value,forWhom){
		jQuery.ajax({
			url:contextPath+'/assetEntity/setImportPerferences',
			data:{'selected':value, 'prefFor':forWhom}
		});
	}
</script>
  </body>
</html>
