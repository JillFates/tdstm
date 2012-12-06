<%@page import="com.tdssrc.grails.WebUtil";%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Show Manufacturer</title>
        <script type="text/javascript">
			$(document).ready(function() {
			   $("#showMergeDialog").dialog({ autoOpen: false })
			})
		</script>
    </head>
    <body>
        <div class="body">
         <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="create" action="create">New Manufacturer</g:link></span>
        </div>
            <h1>Show Manufacturer</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:manufacturerInstance, field:'name')}</td>
                            
                        </tr>
                    	<tr>
							 <td valign="top" class="name">AKA:</td>
							<td valign="top" class="value">${manuAlias}</td>
						</tr>
                        <tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:manufacturerInstance, field:'description')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${manufacturerInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><input class="create" type="button" value="Merge" onclick="showMergeDialog()"/></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
            <div id="showMergeDialog" title="Select the item to merge to:" style="display: none;" class="list">
				<table cellpadding="0" cellspacing="0">
					<thead>
						<tr><th>Name</th><th>AKA</th></tr>
					</thead>
                    <tbody>
                    	<g:each in="${Manufacturer.findAllByIdNotEqual(manufacturerInstance?.id)?.sort{it.name}}" status="i" var="manufacturer">
                    		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                            <td valign="top" class="name">
	                            	<g:link action="merge" id="${manufacturer.id}" params="[fromId:manufacturerInstance?.id]" style="font-weight: ${manufacturer.aka ? 'bold' : 'normal'}">
		                            	${manufacturer.name}
		                            </g:link>
	                            </td>
	                            <td valign="top" class="value">${WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer(manufacturer).name)}</td>
	                        </tr>
                    	</g:each>
                    </tbody>
                </table>
			</div>
        </div>
        <script type="text/javascript">
        function showMergeDialog(){
        	$("#showMergeDialog").dialog('option', 'height', 530 )
            $("#showMergeDialog").dialog('open')
        }
        </script>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
