<%
  def assetClassMap = [
    AssetEntity:'device',
    Application:'app',
    Database:'db',
    Files:'files'
  ]
%><html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>Manage Asset Import Batches</title>

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />
		<g:render template="../layouts/responsiveAngularResources" />
		<g:javascript src="progressBar.js" />
    <style type="text/css">
      .block-anchor{
        text-align: center;
        width: 70px;
        display: inline-block;
      }
    </style>
	
	</head>
    <body>
	    <br>
        <div class="body fluid">
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
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                 
                    <td>${dataTransferBatch.id}</td>
                
                    <td><tds:convertDateTime date="${dataTransferBatch?.dateCreated}" /></td>
                
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
                      <% def showDiv = true %>
                      <div class="block-anchor">
                        <g:if test="${dataTransferBatch?.statusCode == 'PENDING'}">                    
                          <% def className = assetClassMap[dataTransferBatch?.eavEntityType?.domainName] %>
                          <span id="${className}ReviewId_${dataTransferBatch.id}">
                            <a href="javascript:" onclick="kickoffProcess('${className}', 'r', '${dataTransferBatch.id}')">Review</a>
                          </span>                          
                          <%-- 
                            -- Generate the Process button that will be used to replace the Review after the review function is called 
                          --%>
                          <span id="${className}ProcessId_${dataTransferBatch.id}" style="display: none;" >
                            <a href="javascript:" onclick="return kickoffProcess('${className}', 'p', '${dataTransferBatch.id}');">Process</a>
                          </span>                          
                        </g:if><g:else>
                          <g:if test="${dataTransferBatch?.hasErrors == 1}">
                            <a href="errorsListView?id=${dataTransferBatch?.id}">View Errors</a>
                          </g:if><g:else><% showDiv = false %></g:else>
                        </g:else>
                      </div>
                      <span ${(!showDiv)?"style='visibility:hidden'":""}>|</span>										
											<% def dataLog = dataTransferBatch?.importResults %>                      
											<g:if test="${dataLog}">
												<a href="#" data-link="${createLink(action: 'importResults', id: dataTransferBatch?.id)}" title="View Log" class="lnkViewLog"><g:img uri="/icons/script_error.png" width="16" height="16" alt="View Log"/></a> | 
											</g:if><g:else><div style="display:inline-block;width:16px;text-align: center;">-</div></g:else>
                      <g:link action="delete" params="[batchId:dataTransferBatch.id]" title="Delete Batch">
                        <g:img uri="/icons/delete.png" width="16" height="16" alt="Delete Batch"/>
                      </g:link>
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

    <!-- Modal -->
    <div id="dlgLog" class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h5 class="modal-title" id="myModalLabel">Import Results</h5>
          </div>
          <div class="modal-body" style="max-height:20em; overflow-y:auto"></div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
          </div>
        </div>
      </div>
    </div>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
		<g:javascript src="jquery/ui.progressbar.js"/>
		<script type="text/javascript">
      //Manage Import Results Dialog
      $("a.lnkViewLog").on("click", function(e){
        e.preventDefault();
        var uri = $(e.currentTarget).attr("data-link");
        $.get(uri).done(function(data){
          var msg = data.data.importResults
          if(msg){
            $("#dlgLog div.modal-body").html(msg);
            $("#dlgLog").modal({show:true});
          }
        }).fail(function(jqXHR, textStatus, errorThrown){
          console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
          alert('An error occurred while invoking retrieving the information.');          
        });        
      })

			var messageDiv = $("#messageId");

			var progressKey='';
			var messageDiv = $("#messageId");
			var postingFlag = false;	// used to limit one posting at a time
			var progressModal;

			// This method will use Ajax to kickoff the Process function and activate the progress modal
			function kickoffProcess(assetClass, reviewOrProcess, batchId) {
				if (postingFlag) {
					alert('You can only perform one action at a time.');
					return false;
				}
				if ( reviewOrProcess == 'p') {
					if (! confirm('Please confirm that you want to post the imported assets to inventory?') ) {
						return false;
					}
				}
				postingFlag = true;

				messageDiv.html('').hide();

				var title = (reviewOrProcess=='r' ? 'Reviewing assets in batch ' : 'Posting assets to inventory for batch ')+batchId;
				var uri = '/import/invokeAssetImport' + (reviewOrProcess=='r'?'Review':'Process') + '/' + batchId;
				$.post(
          tdsCommon.createAppURL(uri)
        ).done(function(data){
          if (data.status == 'error') {
            alert(data.errors);
            console.log('Error: kickoffProcess() : ' + data.errors);
          } else {
            var results = data.data.results;
            progressKey = results.progressKey;  // Used to get the progress updates

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
        }).fail(function(jqXHR, textStatus, errorThrown){
          // stopProgressBar();
          console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
          alert('An error occurred while invoking the posting process.');          
        }).always(function(){
          postingFlag = false;
        });
        
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
        $.post(
          tdsCommon.createAppURL('/import/importResults/'+batchId)
        ).done(function(data){
          if (data.status == 'error') {
            console.log('Error: showProcessResults() : ' + data.errors);
            $("#statusCode"+batchId).html( results.batchStatusCode);
          } else {
            var results = data.data;
            $("#statusCode"+batchId).html( results.batchStatusCode);
          }
        }).fail(function(jqXHR, textStatus, errorThrown){
          console.log('ERROR: kickoffProcess() failed : ' + errorThrown);
          alert('An error occurred while getting the posting results.');
        }).always(function(){
          // allow another action to occur
          postingFlag=false;
        });				
			}

			// This is called when the progress view receives a failure message
			function processFailed(assetClass, batchId, reviewOrProcess) {
				console.log("Progress failed for "+ assetClass+ " batch "+ batchId+ " for " + (reviewOrProcess=='r' ? 'Review' : 'Posting'));
			}

    	currentMenuId = "#assetMenu";
        $(".menu-parent-assets-manage-batches").addClass('active');
        $(".menu-parent-assets").addClass('active');
        $('#assetMenu').show();
        $('#reportsMenu').hide();

		</script>
		
    </body>
</html>
