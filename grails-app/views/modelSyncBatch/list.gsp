<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Model Sync Batch List</title>
    </head>
    <body>
	    <br>
        <div class="body">
        	<table style="border: 0"><tr><td><h1>Model Sync Batch List</h1></td>
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
                        
                   	        <th>Manufacturers</th>
                   	        
                   	        <th>Models</th>
                        
                   	        <th>Connectors</th>
                   	        
                   	        <th>Status</th>
                   	        
                   	        <th>Action</th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${modelSyncBatchInstanceList}" status="i" var="modelSyncBatchInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${fieldValue(bean:modelSyncBatchInstance, field:'id')}</td>
                        
                            <td><tds:convertDate date="${modelSyncBatchInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
                        
                            <td>${modelSyncBatchInstance?.userlogin?.person?.lastNameFirst}</td>
                        
                            <td>${ManufacturerSync.countByBatch(modelSyncBatchInstance)}</td>
                        
                            <td>${ModelSync.countByBatch(modelSyncBatchInstance)}</td>
                            
                            <td>${ModelConnectorSync.countByBatch(modelSyncBatchInstance)}</td>
                            
                            <td>${fieldValue(bean:modelSyncBatchInstance, field:'statusCode')}</td>
                            
                            <td>
	                            <g:if test="${modelSyncBatchInstance?.statusCode == 'PENDING'}">
	                            	<g:link action="process" params="[batchId:modelSyncBatchInstance.id]" onclick = "return getProgress();" >Process</g:link>|<a href="#">Void</a>
	                            </g:if>
	                             <g:else>
	                            	<a href="#">Remove</a>
	                            </g:else>
                            </td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
