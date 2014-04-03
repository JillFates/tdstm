<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Asset Import</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
	<g:javascript src="jquery/ui.progressbar.js"/>
	<g:javascript src="import.export.js"/>
	<script type="text/javascript">
		/* ---------------------------------
		 * 	Author : Lokanada Reddy		
		 *	function to show the Progress bar
		 * ------------------------------- */
		var handle=0;
		var requestCount=0;
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
			        // Increased interval by 5 sec as server was hanging over chrome with quick server request. 
					handle=setInterval(getProgress, 500);
		        }
	        });
		});

		//This code is used to display progress bar at chrome as Chrome browser cancel all ajax request while uploading .
		function getProgress(){	
			var hiddenVal=$("#requestCount").val()
			if(hiddenVal != requestCount){
				requestCount = hiddenVal
			 	$("#iFrame").attr('src', contextPath+'/assetEntity/getProgress');
			}
		}
		
		function onIFrameLoad() {
		   var serverResponse = $("#iFrame").contents().find("pre").html();
		   var jsonProgress
		   if(serverResponse){
			   $("#requestCount").val(parseInt(requestCount)+1)
		     	jsonProgress = JSON.parse( serverResponse )
		   }
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
		<g:if test="${flash.error}">
			<div class="errors">${flash.error}</div>
		</g:if>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:if test="${error}">
            <div class="message">${error}</div>
        </g:if>
        <g:if test="${message}">
            <div class="message">${message}</div>
        </g:if>
    	<h1>Asset Import</h1>
    	<g:hiddenField name="requestCount" id="requestCount" value="1"/>
        <g:form action="upload" method="post" name="importForm" enctype="multipart/form-data" >
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
                <tr><td colspan="2">
	                <span><input type="checkbox" id="applicationId" name="application" value="application" 
	                		onclick="importExportPreference($(this),'ImportApplication')" 
	                		${prefMap['ImportApplication']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="applicationId">Application</label></span>&nbsp;
	                <span><input type="checkbox" id="assetId" name="asset" value="asset" 
	                		onclick="importExportPreference($(this),'ImportServer')"
	                		${prefMap['ImportServer'] =='true'? 'checked="checked"' :''}/>&nbsp;
	                <label for="assetId">Server</label></span>&nbsp;
	                <span><input type="checkbox" id="databaseId" name="database" value="database" 
	                		onclick="importExportPreference($(this),'ImportDatabase')"
	                		${prefMap['ImportDatabase'] =='true' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="databaseId">Database</label></span>&nbsp;
	                <span><input type="checkbox" id="storageId" name="storage" value="storage"  
	                		onclick="importExportPreference($(this),'ImportStorage')"
	                		${prefMap['ImportStorage']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="filesId">Storage</label></span>&nbsp;
	                <span><input type="checkbox" id="dependencyId" name="dependency" value="dependency" 
	                		onclick="importExportPreference($(this),'ImportDependency')"
	                		${prefMap['ImportDependency'] =='true' ? 'checked="checked"' :''}/>&nbsp;
	                <label for="dependencyId">Dependency</label></span>&nbsp;
	                <span><input type="checkbox" id="cablingId" name="cabling" value="cable" 
		               		onclick="importExportPreference($(this),'ImportCabling')"
		               		${prefMap['ImportCabling']=='true' ? 'checked="checked"' :''}/>&nbsp;
		               		<label for="cablingId">Cabling</label></span>&nbsp;
	                <span><input type="checkbox" id="commentId" name="comment" value="comment" 
		               		onclick="importExportPreference($(this),'ImportComment')"
		               		${prefMap['ImportComment']=='true' ? 'checked="checked"' :''}/>&nbsp;
		               		<label for="cablingId">Comment</label></span>&nbsp;
	                </td>
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
