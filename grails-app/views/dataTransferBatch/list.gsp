<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Manage Asset Import Batches</title>

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />
		<g:javascript src="bootstrap.js" />
		<g:javascript src="angular/angular.min.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<g:javascript src="progressBar.js" />
	
	</head>
    <body>
    
	    <br>
        <div class="body">
			<h1>Manage Asset Import Batches</h1>
			<g:if test="${flash.message}">
			<div class="message" style="background: #f3f8fc">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none">test</div>
				<div id="progressbar" style="margin-bottom:4px; display: none;" class="centered"></div>
			</div>

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

						<span id="deviceProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('device', 'p', '${dataTransferBatch.id}');" >Process</a> |
						</span>

						<span id="appProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('app', 'p', '${dataTransferBatch.id}');" >Process</a> |
						</span>

						<span id="dbProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('db', 'p', '${dataTransferBatch.id}');" >Process</a> |
						</span>

						<span id="filesProcessId_${dataTransferBatch.id}" style="display: none;" >
							<a href="javascript:" onclick="return kickoffProcess('files', 'p', '${dataTransferBatch.id}');" >Process</a> |
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
										<span id="deviceReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="kickoffProcess('device', 'r', '${dataTransferBatch.id}')">Review</a> |
										</span>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if> 

									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Application'}">
										<span id="appReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="kickoffProcess('app', 'r', '${dataTransferBatch.id}')">Review</a> |
										</span>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if> 

									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Database'}">
										<span id="dbReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="kickoffProcess('db', 'r', '${dataTransferBatch.id}')">Review</a> | 
										</span>
										<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
									</g:if>

									<g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Files'}">
										<span id="filesReviewId_${dataTransferBatch.id}">
											<a href="javascript:" onclick="kickoffProcess('files', 'r', '${dataTransferBatch.id}')">Review</a> |
										</span>
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

			var messageDiv = $("#messageId");

			var progressKey='';
			var messageDiv = $("#messageId");
			var postingFlag = false;	// used to limit one posting at a time
			var progressModal;

			// This method will use Ajax to kickoff the Process function and activate the progress modal
			function kickoffProcess(assetClass, reviewOrProcess, batchId) {
				if (postingFlag) {
					alert('You can only perform one action at a time.');
					return;
				}
				if ( reviewOrProcess == 'p') {
					if (! confirm('Please confirm that you want to post the imported assets to inventory?') ) {
						return false;
					}
				}
				postingFlag = true;

				messageDiv.html('').hide();

				var title = '<h1>'+(reviewOrProcess=='r' ? 'Reviewing assets in batch ' : 'Posting assets to inventory for batch ')+batchId;
				var uri = '/import/invokeAssetImport' + (reviewOrProcess=='r'?'Review':'Process') + '/' + batchId;
				$.ajax({
					type: "POST",
					async: true,
					url: tdsCommon.createAppURL(uri),
					dataType: "json",
					success: function (response, textStatus, jqXHR) { 
						if (response.status == 'error') {
							alert(response.errors);
							console.log('Error: kickoffProcess() : ' + response.errors);
						} else {
							var results = response.data.results;
							progressKey = results.progressKey;	// Used to get the progress updates

							var progressModal = tds.ui.progressBar(
								progressKey, 
								5000, 
								function() {
									processFinished(assetClass, batchId, reviewOrProcess); 
								}, 
								function() { 
									processFailed(assetClass, batchId, reviewOrProcess);
								},
								title
							);
						}
					},
					error: function (jqXHR, textStatus, errorThrown) {
						// stopProgressBar();
						console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
						alert('An error occurred while invoking the posting process.');
						postingFlag = false;
					}
				});

				postingFlag=false;
				return false;
			}

			// This is called after a successful batch process that will make Ajax call to get the results of the review or posting results
			function processFinished(assetClass, batchId, reviewOrProcess) {
				var hiddenProcessButton = "#"+assetClass+'ProcessId_'+batchId;
				var currentButton = $("#"+assetClass+"ReviewId_"+batchId);
				if (reviewOrProcess=='r') {
					// Flip the review button over to the Process
					currentButton.html( $(hiddenProcessButton).html() );
				} else {
					currentButton.hide();
				}

				console.log("showProcessResults() was called");

				// Get the status of the batch and update the list accordingly
				$.ajax({
					type: "POST",
					async: true,
					url: tdsCommon.createAppURL('/import/importResults/'+batchId),
					dataType: "json",
					success: function (response, textStatus, jqXHR) { 
						if (response.status == 'error') {
							console.log('Error: showProcessResults() : ' + response.errors);
							$("#statusCode"+batchId).html( results.batchStatusCode);
						} else {
							var results = response.data;
							$("#statusCode"+batchId).html( results.batchStatusCode);
						}
					},
					error: function (jqXHR, textStatus, errorThrown) {
						console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
						alert('An error occurred while getting the posting results.');
					}
				});
				
				// allow another action to occur
				postingFlag=false;

			}

			// This is called when the progress view receives a failure message
			function processFailed(assetClass, batchId, reviewOrProcess) {
				console.log("Progress failed for "+ assetClass+ " batch "+ batchId+ " for " + (reviewOrProcess=='r' ? 'Review' : 'Posting'));
			}

    		currentMenuId = "#assetMenu";
    		$("#assetMenuId a").css('background-color','#003366')
			$('#assetMenu').show();
			$('#reportsMenu').hide();


		</script>
		
    </body>
</html>
