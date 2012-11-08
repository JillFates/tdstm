

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Data Transfer Batch List</title>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'progressbar.css')}" />
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
		        if(progress[0].imported==progress[0].total){
		        		checkProgressBar = false;
		                clearInterval(handle);
		                location.reload(true);
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
				if(returnStatus){
					$("#progressbar").css("display","block")
				    clearInterval(handle);
					handle=setInterval("${remoteFunction(action:'getProgress', onComplete:'showProcessBar(e)')}",500);
					return true;
				} else {
					return false;
				}
			} else {
				alert("Please wait, process is in progress.");
				return false;
			}
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
        
        <div class="body">
        	<table style="border: 0"><tr><td><h1>Data Transfer Batch List</h1></td>
        	<td style="vertical-align: bottom;" align="right"><div id="progressbar" style="display: none;" /></td></tr> </table>
            <g:if test="${flash.message}">
            <div class="message" style="background: #f3f8fc">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <th>Batch Id</th>
                        
                   	       	<th>Date</th>
                        
                   	        <th>Created By</th>
                        
                   	        <th>Set</th>
                   	        
                   	        <th>Asset Type</th>
                   	        
                   	        <th>Assets</th>
                        
                   	        <th>Errors</th>
                   	        
                   	        <th>Status</th>
                   	        
                   	        <th>Action</th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${dataTransferBatchList}" status="i" var="dataTransferBatch">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${fieldValue(bean:dataTransferBatch, field:'id')}</td>
                        
                            <td><tds:convertDate date="${dataTransferBatch?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                        
                            <td>${dataTransferBatch?.userLogin?.person?.lastName} ${dataTransferBatch?.userLogin?.person?.firstName}</td>
                        
                            <td>${dataTransferBatch?.dataTransferSet?.title}</td>
                            
                            <td>${dataTransferBatch?.eavEntityType?.domainName == 'Files' ? 'Storage' : dataTransferBatch?.eavEntityType?.domainName}</td>
                        
                            <td>${DataTransferValue.executeQuery('select count(d.id) from DataTransferValue d where d.dataTransferBatch = '+ dataTransferBatch?.id +' group by rowId' ).size()}</td>
                            
                            <td></td>
                            
                            <td>${fieldValue(bean:dataTransferBatch, field:'statusCode')}</td>
                            <td>
	                            <g:if test="${dataTransferBatch?.statusCode == 'PENDING'}">
	                                <g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'AssetEntity'}">
	                            	   <g:link action="serverProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>|<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                </g:if> 
	                                 <g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Application'}">
	                            	   <g:link action="appProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>|<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if> 
	                                 <g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Database'}">
	                            	   <g:link action="dbProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>|<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if>
	                                 <g:if test="${dataTransferBatch?.eavEntityType?.domainName == 'Files'}">
	                            	   <g:link action="fileProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>|<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if> 
	                                  <g:if test="${dataTransferBatch?.eavEntityType?.domainName == null}">
	                            	   <g:link action="serverProcess" params="[batchId:dataTransferBatch.id]" onclick = "return getProgress();" >Process</g:link>|<g:link action="delete" params="[batchId:dataTransferBatch.id]">Remove</g:link>
	                                 </g:if>                     
	                            </g:if>
	                             <g:else>
	                            	<g:if test="${dataTransferBatch?.hasErrors == 1}">|<a href="errorsListView?id=${dataTransferBatch?.id}">View Errors</a></g:if>
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
		</script>
		
    </body>
</html>
