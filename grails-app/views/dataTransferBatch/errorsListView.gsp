

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Data Transfer Error List</title>
    </head>
    <body>
    
	    <br>
        
        <div class="body">
        	<table style="border: 0"><tr><td><h1>Data Transfer Error List</h1></td></tr> </table>
        	
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <th>Asset Tag</th>
                        
                   	       	<th>Asset Name</th>
                        
                   	        <th>Attribute</th>
                        
                   	        <th>Error</th>
                   	        
                   	        <th>Current Value</th>
                        
                   	        <th>Import Value</th>
                   	        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${completeDataTransferErrorList}" status="i" var="dataTransferError">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${dataTransferError?.assetTag}</td>
                        
                            <td>${dataTransferError?.assetName}</td>
                        
                            <td>${dataTransferError?.attribute}</td>
                        
                            <td>${dataTransferError?.error}</td>
                        
                            <td>${dataTransferError?.currentValue}</td>
                            
                            <td>${dataTransferError?.importValue}</td>
                            
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
         <script type="text/javascript">
			$('#assetMenu').show();
			$('#reportsMenu').hide();
		</script>
    </body>
</html>
