<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Manage Asset Import Batches</title>
	
		<script type="text/javascript">
			var checkProgressBar;
			var progressBarRefreshRate=5000; // msec
			var progressIntervalHandle=0;
			var progressBar = $("#progressbar");
			var messageDiv = $("#messageId");
			var reloadPageWhenDone=false;

			// This method is used to capture the contents of the Progress meter bar Ajax call
			function onIFrameLoad() {
				var serverResponse = $("#iFrame").contents().find("pre").html();
				var jsonProgress;
				if (serverResponse)
					jsonProgress = JSON.parse( serverResponse );

				if (jsonProgress) {
					checkProgressBar = true;
					progressBar.reportprogress(jsonProgress[0].processed, jsonProgress[0].total);
					if (jsonProgress[0].processed >= jsonProgress[0].total) {
						checkProgressBar = false;
						clearInterval(progressIntervalHandle);

						// The post assets needs to reload the list after finishing while the review does not
						//if (reloadPageWhenDone)
						//	location.reload(true);
					}
				}
			}
		</script>
	</head>
    <body>
    
	    <br>
        <iframe id='iFrame'  class='iFrame' onload='onIFrameLoad()'></iframe>
        <div class="body">
			<h1>Manage Asset Import Batches</h1>
			<g:if test="${flash.message}">
			<div class="message" style="background: #f3f8fc">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none">test</div>
				<div id="progressbar" style="margin-bottom:4px; display: none;" class="centered"></div>
			</div>

			<span id="spinnerId" style="display: none">Reviewing ...<img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>

            <div class="list">
                <table>
                    <thead>
                        <tr>
                   	        <th>Batch Id</th>
                   	       	<th>Imported At</th>
                   	        <th>Imported By</th>
                   	        <th>Attribute Set</th>
                   	        <th>Class</th>
                   	        <th>Assets</th>
                   	        <th>Errors</th>
                   	        <th>Status</th>
                   	        <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${dataTransferBatchList}" status="i" var="dataTransferBatch">
                    	
                    	<%-- 
                    	  -- Generate the Process buttons that will be used to replace the Review after the review function is called 
                    	  --%>

						<span id="assetDisabledProcessId_${dataTransferBatch.id}" style="display: none;">
							<a href="javascript:" class="disableButton">Process</a> |
						</span>
						<span id="assetProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('asset', '${dataTransferBatch.id}');" >Process</a> |
						</span>

						<span id="appDisabledProcessId_${dataTransferBatch.id}" style="display: none;">
							<a href="javascript:" class="disableButton">Process</a> |
						</span>
						<span id="appProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('app', '${dataTransferBatch.id}');" >Process</a> |
						</span>

						<span id="dbDisabledProcessId_${dataTransferBatch.id}" style="display: none;">
							<a href="javascript:" class="disableButton">Process</a> |
						</span>
						<span id="dbProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('db', '${dataTransferBatch.id}');" >Process</a> |
						</span>

						<span id="filesDisabledProcessId_${dataTransferBatch.id}" style="display: none;">
							<a href="javascript:" class="disableButton">Process</a> |
						</span>
						<span id="filesProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('files', '${dataTransferBatch.id}');" >Process</a> |
						</span>

                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                         
                            <td>${dataTransferBatch.id}</td>
                        
                            <td><tds:convertDateTime date="${dataTransferBatch?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                        
                            <td>${dataTransferBatch?.userLogin?.person}</td>
                        
                            <td>${dataTransferBatch?.dataTransferSet?.title}</td>
                            
                            <g:set var="domainName" value="${dataTransferBatch?.eavEntityType?.domainName}" />
                            <td>${ (domainName == 'Files' ? 'Logical Storage' : (domainName == 'AssetEntity' ? 'Device' : domainName) ) }</td>
                        
                            <td>${DataTransferValue.executeQuery('select count(d.id) from DataTransferValue d where d.dataTransferBatch = '+ dataTransferBatch?.id +' group by rowId' ).size()}</td>
                            
                            <td></td>
                            
                            <td>
                            	<span id="statusCode${dataTransferBatch.id}">
                            		${fieldValue(bean:dataTransferBatch, field:'statusCode')}
                            	</span>
                            </td>

                            <td>
								<g:if test="${dataTransferBatch?.statusCode == 'PENDING'}">
									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'AssetEntity'}">
										<span id="assetReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','asset')">Review</a> |
										</span>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if> 

									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Application'}">
										<span id="appReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','app')">Review</a> |
										</span>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if> 

									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Database'}">
										<span id="dbReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','db')">Review</a> | 
										</span>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if>

									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Files'}">
										<span id="filesReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','files')">Review</a> |
										</span>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if> 

									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == null}">
										<g:link action="deviceProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if>                     

								</g:if>
								<g:else>
									<g:if test="${dataTransferBatch?.hasErrors == 1}">
										<a href="errorsListView?id=${dataTransferBatch?.id}">View Errors</a> | 
									</g:if>
									<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
								</g:else>
                        </td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
               
            </div>
             <div class="paginateButtons">
                <g:paginate total="${DataTransferBatch.findAll('from DataTransferBatch where project = '+projectId).size()}" params ="[projectId:projectId]"/>
            </div>
        </div>

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
		<g:javascript src="jquery/ui.progressbar.js"/>
		<script type="text/javascript">
			var checkProgressBar;
			var progressIntervalHandle=0;
			var progressBar = $("#progressbar");
			var messageDiv = $("#messageId");

			function showProcessBar(e){
				var progress = eval('(' + e.responseText + ')');			
				if (progress) {
					checkProgressBar = true;
					progressBar.reportprogress(progress[0].processed, progress[0].total);
				}
			}

			// This method will kickoff the Process function
			function kickoffProcess(forWhom, batchId) {
				if ( ! confirm('Please confirm that you want to post the import to inventory?') ) {
					return false;
				}

				messageDiv.html('Posting imported assets to inventory').show();

				$.ajax({
					type: "POST",
					async: true,
					url: tdsCommon.createAppURL('/dataTransferBatch/processImportBatch/'+batchId),
					dataType: "json",
					success: function (response, textStatus, jqXHR) { 
						stopProgressBar();
						if (response.status == 'error') {
							alert(response.errors);
							console.log('Error: kickoffProcess() : ' + response.errors);
						} else {
							var results = response.data.results;
							messageDiv.html(results.info).show();
							$("#"+forWhom+"ReviewId_"+batchId).hide();
							$("#statusCode"+batchId).html( results.batchStatusCode);

							// TODO : change the buttons appropriately
						}
					},
					error: function (jqXHR, textStatus, errorThrown) {
						stopProgressBar();
						console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
						alert('An error occurred during the Process update');
					}
				});

				reloadPageWhenDone=true;
				startProgressBar();
				return true;
			}

			// used to set the interval to display progress meter bar
			function getProgress() {		
				if ( ! checkProgressBar ) {
					if ( confirm('Please confirm that you want to post the import to inventory?') ) {
						messageDiv.html('Posting imported assets to inventory').show();
						reloadPageWhenDone=true;
						startProgressBar();
						return true;
					} else {
						return false;
					}
				} else {
					alert("Please wait, process is in progress.");
					return false;
				}
			}

			// Used to actual start the display of the progress bar and kick off the interval function to call back the getProgress web service
			function startProgressBar() {
				progressBar.reportprogress(0,0,'Initializing...');
				progressBar.css("display","block");
				clearInterval(progressIntervalHandle);
				if (${isMSIE}) {
					progressIntervalHandle=setInterval("${remoteFunction(action:'getProgress', onComplete:'showProcessBar(e)')}", progressBarRefreshRate);
				} else {
					//Increased interval by 5 sec as server was hanging over chrome with quick server request.
					progressIntervalHandle=setInterval(getProcessedDataInfo, progressBarRefreshRate);
				}
			}

			// Used to stop the interval calls to the progress web service and hide the visual
			function stopProgressBar() {
				progressBar.hide();
				clearInterval(progressIntervalHandle);
			}

			// This code is used to display progress bar at chrome as Chrome browser cancel all ajax request while uploading .
			function getProcessedDataInfo() {
				$("#iFrame").attr('src', contextPath+'/dataTransferBatch/getProgress');
			}

    		currentMenuId = "#assetMenu";
    		$("#assetMenuId a").css('background-color','#003366')
			$('#assetMenu').show();
			$('#reportsMenu').hide();

			// Used to kick-off the reviewBatch web service
			function reviewBatch(batchId, forWhom) {
				//messageDiv.html($("#spinnerId").html()).show()
				messageDiv.html('Reviewing batch for duplicate references, unknown Mfg/Model information and other discrepencies in the imported data.').show();

				reloadPageWhenDone=false;
				startProgressBar();

				jQuery.ajax({
					url: '../dataTransferBatch/reviewBatch',
					data: {'id':batchId},
					type:'POST',
					async: true,
					success: function(response, textStatus, jqXHR) {
						// Set the Process or Revew button appropriately
						stopProgressBar();

						if (response.status == 'error') {
							log.console('reviewBatch() ERROR ' + response.errors);
							alert(results.errors);
						} else {
							var results = response.data.results;

							// Get the active or disabled Process button HTML and overwrite the Review button HTML
							var buttonDomName = "#"+forWhom+(results.hasPerm ? '' : 'Disabled')+"ProcessId_"+batchId;
							var buttonDom = $("#"+forWhom+"ReviewId_"+batchId);
							buttonDom.html( $(buttonDomName).html() );
							
							if (results.info) {
								messageDiv.html(results.info);
							} else {
								messageDiv.html("Batch was reviewed and no errors were found. You may now click the 'Process' button to complete the import.");
							}
						}
					},
					error: function(jqXHR, textStatus, errorThrown) {
						stopProgressBar();
						messageDiv.html('').hide();
						console.log('ERROR: reviewBatch() ' + errorThrown);
						alert("An error occurred while attempting to review the batch");
					}
				});
			}
		</script>
		
    </body>
</html>
