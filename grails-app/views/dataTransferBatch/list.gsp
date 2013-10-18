

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Data Transfer Batch List</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
	<g:javascript src="jquery/ui.progressbar.js"/>
	<script type="text/javascript">
		/* ---------------------------------
		 * 	Author : Lokanada Reddy		
		 *	function to show the Progress bar
		 * ------------------------------- */
		var checkProgressBar;
		var handle=0;
		function showProcessBar(e){
			var progress = eval('(' + e.responseText + ')');			
			if(progress){
				checkProgressBar = true;
				$("#progressbar").reportprogress(progress[0].processed,progress[0].total);
		        if(progress[0].processed==progress[0].total){
		        	/*
		        		checkProgressBar = false;
		                clearInterval(handle);
		                location.reload(true);
		             */
		        }
			}
		}
		/* ---------------------------------
		 * 	Author : Lokanada Reddy
		 *	JQuary function to set the interval to display Progress
		 * ------------------------------- */
		function getProgress() {		
			if ( !checkProgressBar ) {
				var returnStatus =  confirm('Do you really want to process Batch ?');
				if(returnStatus) {
					/*
					$("#progressbar").css("display","block")
				    clearInterval(handle);
					if(${isMSIE}){
						handle=setInterval("${remoteFunction(action:'getProgress', onComplete:'showProcessBar(e)')}",500);
					} else {
						//Increased interval by 5 sec as server was hanging over chrome with quick server request.
						handle=setInterval(getProcessedDataInfo,5000);
			        }
			        */
					return true;
				} else {
					return false;
				}
			} else {
				alert("Please wait, process is in progress.");
				return false;
			}
		}
		//This code is used to display progress bar at chrome as Chrome browser cancel all ajax request while uploading .
		function getProcessedDataInfo(){	
		//	 $("#iFrame").attr('src', contextPath+'/dataTransferBatch/getProgress');
		}
		function onIFrameLoad() {
		/*
		   var serverResponse = $("#iFrame").contents().find("pre").html();
		   var jsonProgress
		   if(serverResponse)
		     jsonProgress = JSON.parse( serverResponse )
		   if(jsonProgress){
			   checkProgressBar = true;
			   $("#progressbar").reportprogress(jsonProgress[0].processed, jsonProgress[0].total);
		       if(jsonProgress[0].processed==jsonProgress[0].total){
		    	   checkProgressBar = false;
	  	           clearInterval(handle);
	  	           location.reload(true);
		       }
		   }
		*/
		}
		function removeDataTrsferBatch(id){
			var id = id
	           jQuery.ajax({
                  url:'../dataTransferBatch/delete',
                  data:{'id':id},
                  type:'POST',
                  success: function(data){
                     window.location.reload();
                  }
			    });
		}
		
	</script>
    </head>
    <body>
    
	    <br>
         <iframe id='iFrame'  class='iFrame' onload='onIFrameLoad()'></iframe>
        <div class="body">
        	<table style="border: 0"><tr><td><h1>Data Transfer Batch List</h1></td>
        	<td style="vertical-align: bottom;" align="right"><div id="progressbar" style="display: none;" /></td></tr> </table>
            <g:if test="${flash.message}">
            <div class="message" style="background: #f3f8fc">${flash.message}</div>
            </g:if>
            <div >
				<div id="messageId" class="message" style="display:none">
				</div>
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
                    	  <span id="assetDisabledProcessId_${dataTransferBatch.id}" style="display: none;"><a href="javascript:" class="disableButton">Process</a></span>
              			  <span id="assetProcessId_${dataTransferBatch.id}" style="display: none;" >
              			  	<g:link action="serverProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>
              			  </span>
              			  
              			  <span id="appDisabledProcessId_${dataTransferBatch.id}" style="display: none;"><a href="javascript:" class="disableButton">Process</a></span>
              			  <span id="appProcessId_${dataTransferBatch.id}" style="display: none;" >
             			  	<g:link action="appProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >
                            	   			<span>Process</span></g:link>
	                      </span>
	                      
	                      <span id="dbDisabledProcessId_${dataTransferBatch.id}" style="display: none;"><a href="javascript:" class="disableButton">Process</a></span>
              			  <span id="dbProcessId_${dataTransferBatch.id}" style="display: none;" >
             			  	<g:link action="dbProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >
                            	   			<span>Process</span></g:link>
	                      </span>
	                      
	                      <span id="filesDisabledProcessId_${dataTransferBatch.id}" style="display: none;"><a href="javascript:" class="disableButton">Process</a></span>
              			  <span id="filesProcessId_${dataTransferBatch.id}" style="display: none;" >
             			  	<g:link action="fileProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >
                            	   			<span>Process</span></g:link>
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
                           	   			<span id="assetReviewId_${dataTransferBatch.id}"><a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','asset')">Review</a></span>|<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                </g:if> 
	                                 <g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Application'}">
	                                   <span id="appReviewId_${dataTransferBatch.id}"><a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','app')">Review</a></span>
	                            	   |<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if> 
	                                 <g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Database'}">
	                            	  <span id="dbReviewId_${dataTransferBatch.id}"><a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','db')">Review</a></span>
	                            	  |<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if>
	                                 <g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Files'}">
	                            	  <span id="filesReviewId_${dataTransferBatch.id}"><a href="javascript:" onclick="reviewBatch('${dataTransferBatch.id}','files')">Review</a></span>
	                            	  |<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if> 
	                                  <g:if test="${dataTransferBatch?.eavEntityType?.domainName == null}">
	                            	   <g:link action="serverProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>|<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if>                     
	                            </g:if>
	                             <g:else>
	                            	<g:if test="${dataTransferBatch?.hasErrors == 1}"><a href="errorsListView?id=${dataTransferBatch?.id}">View Errors</a> | </g:if>
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
           <!--  <div class="buttons">
	            <g:form>
		            <span class="button"><input type="button" value="New" class="create" onClick="#"/></span>
	        	</g:form>
        	</div> -->
        	<script type="text/javascript">
        	$.post("${createLink(action:'updateAssetRacks')}", {
        		ajax: 'true'
        	});
        	</script>
        </div>
        <script type="text/javascript">
    		currentMenuId = "#assetMenu";
    		$("#assetMenuId a").css('background-color','#003366')
			$('#assetMenu').show();
			$('#reportsMenu').hide();

			function reviewBatch(dataTransferBatchId, forWhom){
				$("#messageId").html($("#spinnerId").html()).show()
				jQuery.ajax({
					url: '../dataTransferBatch/reviewBatch',
					data: {'id':dataTransferBatchId},
					type:'POST',
					success: function(data) {
						if(data.importPerm || !data.errorMsg)
							$("#"+forWhom+"ReviewId_"+dataTransferBatchId).html($("#"+forWhom+"ProcessId_"+dataTransferBatchId).html())
						else
							$("#"+forWhom+"ReviewId_"+dataTransferBatchId).html($("#"+forWhom+"DisabledProcessId_"+dataTransferBatchId).html())
							
						data.errorMsg ? $("#messageId").html(data.errorMsg) : $("#messageId").html(" Reviewed , there were no errors in the review.")
					},
					error: function(jqXHR, textStatus, errorThrown) {
						alert("An Unexpected error while populating dependent asset.")
					}
				});
			}
		</script>
		
    </body>
</html>
