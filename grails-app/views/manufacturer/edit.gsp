

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Edit Manufacturer</title>
    </head>
    <body>
        <div class="body">
	        <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
	            <span class="menuButton"><g:link class="create" action="create">New Manufacturer</g:link></span>
	        </div>
            <h1>Edit Manufacturer</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${manufacturerInstance}">
            <div class="errors">
                <g:renderErrors bean="${manufacturerInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${manufacturerInstance?.id}" />
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
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="aka">AKA:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'aka','errors')}">
                                 
                                 <table style="border: 0px;margin-left: -8px;">
                                  <tbody id="addAkaTableId">
                                  <g:each in="${manuAlias}" var="alias">
                                   <tr id="aka_${alias.id}"><td nowrap="nowrap">
                                  	 <input type="text" id="aka" name="aka_${alias.id}" value="${alias.name}" onchange="changedAka(this.value,${alias.id})"/>
                                  	 <a href="javascript:deleteAkaRow('aka_${alias.id}',true)"><span class='clear_filter'><u>X</u></span></a>
                                   </td></tr>
                                  </g:each>
                                  </tbody>
                                 </table>
                                 <span style="cursor: pointer;" onclick="addAka()"><b>Add AKA</b></span>
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
                <input type="hidden" name="deletedAka" id="deletedAka" />
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
             <div id="akaDiv" style="display:none;"> 
             	<input type="text" name="aka" id="akaId" value="">
             </div>
             <input type="hidden" id="manageAkaId" value="-1" >
        </div>
		<script>
			currentMenuId = "#adminMenu";
			$("#adminMenuId a").css('background-color','#003366')
			
			function deleteAkaRow(id, save){
				$("#"+id).remove()
				if(save){
					var deletedId = id.split("_")[1]
					$("#deletedAka").val() ? $("#deletedAka").val($("#deletedAka").val()+","+deletedId) : $("#deletedAka").val(deletedId)
				}
			}
		
			function addAka(){
				var trId = $("#manageAkaId").val() 
				$("#addAkaTableId").append("<tr id='akaId_"+trId+"'><td nowrap='nowrap'>"+$("#akaDiv").html()+
				"<a href=\"javascript:deleteAkaRow(\'akaId_"+trId+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
				$("#manageAkaId").val(parseInt(trId)-1)
			}
		
		</script>
    </body>
</html>
