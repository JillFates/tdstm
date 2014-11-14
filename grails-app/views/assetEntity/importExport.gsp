<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Asset Import</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
	<g:javascript src="jquery/ui.progressbar.js"/>
	<g:javascript src="import.export.js"/>
	<script type="text/javascript">
		/* 
		 * used to show the Progress bar
		 */
		var handle=0;
		var requestCount=0;
		var buttonClicked=false;

		function showProcessBar(e) {
			var progress = eval('(' + e.responseText + ')');
			if (progress) {
				$("#progressbar").reportprogress(progress[0].imported,progress[0].total);
		        if (progress[0].imported==progress[0].total){
		            clearInterval(handle);
		        }
			}
		}

		/*
		 * Used to set the interval to display Progress
		 */
		jQuery(function($) {
	        $("#run").click(function() {
	        	if (buttonClicked) {
	        		alert('You already have already clicked the Import Batches button.');
	        		return false;
	        	} 
	        	buttonClicked=true;
	        	// Some reason if the button gets disabled it prevents the original click process to continue, we should switch to the JQuery Once function
	        	//$('#run').prop('disabled',true);

	        	var progressBar = $("#progressbar");
				progressBar.reportprogress(0, 0, 'Uploading &amp; verifying spreadsheet...');
				progressBar.css("display","block");
				clearInterval(handle);
				if (${isMSIE}) {
					handle = setInterval("${remoteFunction(action:'getProgress', onComplete:'showProcessBar(e)')}", 5000);
				} else {
					// Increased interval by 5 sec as server was hanging over chrome with quick server request. 
					handle=setInterval(getProgress, 5000);
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
		
		// Used to handle the AJax response for the progress update
		function onIFrameLoad() {
			var serverResponse = $("#iFrame").contents().find("pre").html();
			var jsonProgress
			if(serverResponse){
				$("#requestCount").val(parseInt(requestCount)+1)
				jsonProgress = JSON.parse( serverResponse )
			}
			if (jsonProgress) {
				var progressBar = $("#progressbar");
				progressBar.reportprogress(jsonProgress[0].imported,jsonProgress[0].total);
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
            <table style="width:500px; margin-left:10px;">
              <thead>
              	<tr><th colspan="3">Import</th></tr>
              </thead>
              <tbody>
              
              	<tr>
					<td valign="top" class="name">Import Type:</td>
					<td valign="top" class="value">
						<select id="dataTransferSet" name="dataTransferSet">                    
							<g:each status="i" in="${dataTransferSetImport}" var="dataTransferSet">
								<option value="${dataTransferSet?.id}">${dataTransferSet?.title}</option>
							</g:each>
						</select>
                	</td>
					<td></td>
                </tr>

				<tr>
					<td>
						<label for="file">File:</label></td>
					<td colspan="2">
						<input size="40" type="file" name="file" id="file" />
					</td>
				</tr>              

				<tr>
					<td></td>
					<td colspan="2">
						<div id="progressbar" style="display: none;" ></div>
					</td>
				</tr>

                <tr>
                	<td>Choose Items to Import:</td>
                	<td style="vertical-align:top;">
						<span>
							<input type="checkbox" id="applicationId" name="application" value="application" 
								onclick="importExportPreference($(this),'ImportApplication')" 
								${prefMap['ImportApplication']=='true' ? 'checked="checked"' :''}/>
							&nbsp;
							<label for="applicationId">Application</label>
						</span>
						<br>

						<span>
							<input type="checkbox" id="assetId" name="asset" value="asset" 
								onclick="importExportPreference($(this),'ImportServer')"
								${prefMap['ImportServer'] =='true'? 'checked="checked"' :''}/>
							&nbsp;
							<label for="assetId">Devices</label>
						</span>
						<br>

						<span>
							<input type="checkbox" id="databaseId" name="database" value="database" 
								onclick="importExportPreference($(this),'ImportDatabase')"
								${prefMap['ImportDatabase'] =='true' ? 'checked="checked"' :''}/>
							&nbsp;
							<label for="databaseId">Database</label>
						</span>
						<br>

						<span>
							<input type="checkbox" id="storageId" name="storage" value="storage"  
								onclick="importExportPreference($(this),'ImportStorage')"
								${prefMap['ImportStorage']=='true' ? 'checked="checked"' :''}/>
							&nbsp;
							<label for="storageId">Storage</label>
						</span>

					</td><td style="vertical-align:top;">

						<span>
							<input type="checkbox" id="dependencyId" name="dependency" value="dependency" 
								onclick="importExportPreference($(this),'ImportDependency')"
								${prefMap['ImportDependency'] =='true' ? 'checked="checked"' :''}/>
							&nbsp;
							<label for="dependencyId">Dependency</label>
						</span>
						<br>

						<span>
							<input type="checkbox" id="cablingId" name="cabling" value="cable" 
								onclick="importExportPreference($(this),'ImportCabling')"
								${prefMap['ImportCabling']=='true' ? 'checked="checked"' :''}/>
							&nbsp;
							<label for="cablingId">Cabling</label><
						/span>
						<br>

						<span>
							<input type="checkbox" id="commentId" name="comment" value="comment" 
								onclick="importExportPreference($(this),'ImportComment')"
								${prefMap['ImportComment']=='true' ? 'checked="checked"' :''}/>
							&nbsp;
							<label for="commentId">Comment</label>
						</span>
						
	                </td>
                </tr>

                <tds:hasPermission permission="Import">
				<tr>
					<td class="buttonR" colspan="3">
						<input class="button" id="run" type="submit" value="Import Spreadsheet" />
					</td>
				</tr>
				<tr><td colspan="3"></td></tr>
                <tr>
                	<td valign="top" class="buttonR" colspan="3">
                		<g:link controller="dataTransferBatch" >Manage Batches: ${dataTransferBatchs}</g:link>
                	</td>
                </tr>
				</tds:hasPermission>

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
