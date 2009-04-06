

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Data Transfer Batch List</title>
    </head>
    <body>
    	<div class="menu2">
	      <ul>
	        <li><g:link class="home" controller="projectUtil">Project </g:link></li>
	        <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectId]" >Staff</g:link></li>
	        <li><g:link class="home" controller="asset">Assets </g:link></li>
	        <li><g:link class="home" controller="asset" action="assetImport" params="[projectId:projectId]">Import/Export</g:link> </li>
	        <li><a href="#">Contacts </a></li>
	        <li><a href="#">Applications </a></li>
	        <li><g:link class="home" controller="moveBundle" params="[projectId:projectId]">Move Bundles</g:link></li>
	      </ul>
	    </div>
	    <br>
        
        <div class="body">
            <h1>Data Transfer Batch List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <th>Batch Id</th>
                        
                   	       	<th>Date</th>
                        
                   	        <th>Created By</th>
                        
                   	        <th>Set</th>
                   	        
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
                        
                            <td><tds:convertDate date="${dataTransferBatch?.dateCreated}" /></td>
                        
                            <td>${dataTransferBatch?.userLogin?.person?.lastName} ${dataTransferBatch?.userLogin?.person?.firstName}</td>
                        
                            <td>${dataTransferBatch?.dataTransferSet?.title}</td>
                        
                            <td></td>
                            
                            <td></td>
                            
                            <td>${fieldValue(bean:dataTransferBatch, field:'statusCode')}</td>
                            
                            <td>
	                            <g:if test="${dataTransferBatch?.statusCode == 'PENDING'}">
	                            	<g:link action="show" >View</g:link>|<g:link action="process" params="[batchId:dataTransferBatch.id, projectId:projectId]" onclick = "return confirm('Do you really want to process Batch ?');" >Process</g:link>|<g:link action="void" >Void</g:link>
	                            </g:if>
	                            <g:else>
	                            	<g:link action="show" >View</g:link>|<g:link action="delete" >Remove</g:link>
	                            </g:else>
                            </td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
           <!--  <div class="buttons">
	            <g:form>
		            <span class="button"><input type="button" value="New" class="create" onClick="#"/></span>
	        	</g:form>
        	</div> -->
        </div>
    </body>
</html>
