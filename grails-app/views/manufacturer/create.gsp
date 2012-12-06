

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Create Manufacturer</title>         
    </head>
    <body>
        <div class="body">
        <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list"  params="[filter:true]">Manufacturer List</g:link></span>
        </div>
            <h1>Create Manufacturer</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${manufacturerInstance}">
            <div class="errors">
                <g:renderErrors bean="${manufacturerInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
	                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><b>Name:&nbsp;<span style="color: red">*</span></b></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:manufacturerInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        	<tr>
							<td valign="top" class="name">AKA:</td>
								<td>
								   <table style="border:0px ;margin-left:-8px">
								    <tbody id="addAkaTableId">
								      <tr><td nowrap="nowrap">
									  	<input type="text" name="aka" id="akaId" value="${manufacturerInstance?.aka}"> <span style="cursor: pointer;" onclick="addAka()"><b>Add AKA</b></span>
									  </td></tr>
									</tbody>
									</table>
								</td>
							</tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:manufacturerInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
               
                
               
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
             <div id="akaDiv" style="display:none;"> 
             	<input type="text" name="aka" id="akaId" value="">
             </div>
             <input type="hidden" id="manageAkaId" value="-1" >
        </div>
        <script type="text/javascript">
		function addAka(){
			var trId = $("#manageAkaId").val() 
			$("#addAkaTableId").append("<tr id='akaId_"+trId+"'><td>"+$("#akaDiv").html()+
			"<a href=\"javascript:deleteAkaRow(\'akaId_"+trId+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
			$("#manageAkaId").val(parseInt(trId)-1)
		}

		function deleteAkaRow(id){
			$("#"+id).remove()
		}
	    </script>
    </body>
</html>
