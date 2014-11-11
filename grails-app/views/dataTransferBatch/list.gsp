<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Manage Asset Import Batches</title>
	
		<script type="text/javascript">
			var checkProgressBar;
			var handle=0;
			var progressBar = $("#progressbar");
			var messageDiv = $("#messageId");
			var reloadPageWhenDone=false;

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
						clearInterval(handle);

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
							<a href="javascript:" onclick="return kickoffProcess('asset', 'deviceProcess', '${dataTransferBatch.id}');" >Process</a> |
						</span>

						<span id="appDisabledProcessId_${dataTransferBatch.id}" style="display: none;">
							<a href="javascript:" class="disableButton">Process</a> |
						</span>
						<span id="appProcessId_${dataTransferBatch.id}" style="display: none;" >
							<g:link action="appProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >
								<span>Process</span>
							</g:link> |
						</span>

						<span id="dbDisabledProcessId_${dataTransferBatch.id}" style="display: none;">
							<a href="javascript:" class="disableButton">Process</a> |
						</span>
						<span id="dbProcessId_${dataTransferBatch.id}" style="display: none;" >
							<g:link action="dbProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >
								<span>Process</span>
							</g:link> |
						</span>

						<span id="filesDisabledProcessId_${dataTransferBatch.id}" style="display: none;">
							<a href="javascript:" class="disableButton">Process</a> |
						</span>
						<span id="filesProcessId_${dataTransferBatch.id}" style="display: none;" >
							<g:link action="fileProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >
								<span>Process</span>
							</g:link> |
						</span>

                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                         
                            <td>${dataTransferBatch.id}</td>
                        
                            <td><tds:convertDateTime date="${dataTransferBatch?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                        
                            <td>${dataTransferBatch?.userLogin?.person}</td>
                        
                            <td>${dataTransferBatch?.dataTransferSet?.title}</td>
                            
                            <td>${dataTransferBatch?.eavEntityType?.domainName == 'Files' ? 'Storage' : dataTransferBatch?.eavEntityType?.domainName}</td>
                        
                            <td>${DataTransferValue.executeQuery('select count(d.id) from DataTransferValue d where d.dataTransferBatch = '+ dataTransferBatch?.id +' group by rowId' ).size()}</td>
                            
                            <td></td>
                            
                            <td>${fieldValue(bean:dataTransferBatch, field:'statusCode')}</td>

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
			var handle=0;
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
			function kickoffProcess(forWhom, action, batchId) {
				if ( ! confirm('Please confirm that you want to post the import to inventory?') ) {
					return false;
				}

				messageDiv.html('Posting imported assets to inventory').show();
				// debugger;
				$.ajax({
					type: "POST",
					async: true,
					url: tdsCommon.createAppURL('/dataTransferBatch/' + action + '/' + batchId),
					dataType: "json",
					success: function (response, textStatus, jqXHR) { 
						stopProgressBar();
						if (response.status == 'error') {
							alert('An error occurred during the Process update');
							console.log('Error: kickoffProcess() errored : ' + response.data.errors);
						} else {
							debugger;
							messageDiv.html(response.data.results).show();
							$("#"+forWhom+"ReviewId_"+batchId).hide();

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
				progressBar.reportprogress(0,100,true);
				progressBar.css("display","block");
				clearInterval(handle);
				if (${isMSIE}) {
					handle=setInterval("${remoteFunction(action:'getProgress', onComplete:'showProcessBar(e)')}", 5000);
				} else {
					//Increased interval by 5 sec as server was hanging over chrome with quick server request.
					handle=setInterval(getProcessedDataInfo, 1000);
				}
			}

			// Used to stop the interval calls to the progress web service and hide the visual
			function stopProgressBar() {
				progressBar.hide();
				clearInterval();
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
			function reviewBatch(dataTransferBatchId, forWhom) {
				//messageDiv.html($("#spinnerId").html()).show()
				messageDiv.html('Reviewing batch for duplicate references, unknown Mfg/Model information and other discrepencies in the imported data.').show();

				reloadPageWhenDone=false;
				startProgressBar();

				jQuery.ajax({
					url: '../dataTransferBatch/reviewBatch',
					data: {'id':dataTransferBatchId},
					type:'POST',
					success: function(data) {
						// Set the Process or Revew button appropriately
						// debugger;
						if (data.importPerm || !data.errorMsg)
							$("#"+forWhom+"ReviewId_"+dataTransferBatchId).html($("#"+forWhom+"ProcessId_"+dataTransferBatchId).html());
						else
							$("#"+forWhom+"ReviewId_"+dataTransferBatchId).html($("#"+forWhom+"DisabledProcessId_"+dataTransferBatchId).html());
							
						// User message
						if (data.errorMsg)
							messageDiv.html(data.errorMsg);
						else	
							messageDiv.html("Batch was reviewed and no errors were found. You may now click the 'Process' button to complete the import.");

						stopProgressBar();
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
