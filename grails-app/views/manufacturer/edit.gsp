

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>Edit Manufacturer</title>
    </head>
    <body>
        <div class="body">
            <g:hasErrors bean="${manufacturerInstance}">
            <div class="errors">
                <g:renderErrors bean="${manufacturerInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" action="update" name="editManufacturerFormId">
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
                                <td valign="top">
                                 
                                 <table style="border: 0px;margin-left: -8px;">
                                  <tbody id="addAkaTableId">
                                  <g:each in="${manuAlias}" var="alias">
                                   <tr id="aka_${alias.id}"><td nowrap="nowrap">
                                  	 <input type="text" class="akaValidate" id="aka" name="aka_${alias.id}" value="${alias.name}" onchange="validateAKA(this.value,${alias.id},'errSpan_${alias.id}', 'manufacturer')"/>
                                  	 <a href="javascript:deleteAkaRow('aka_${alias.id}',true)"><span class='clear_filter'><u>X</u></span></a>
                                  	 <br><div class="errors" style="display: none" id="errSpan_${alias.id}"></div>
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
                <div class="buttons" >
                    <span class="button"><input type="button" class="save" value="Update" onclick="updateManufacturer('Manufacturer')"/></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
             <div id="akaDiv" style="display:none;"> 
             	<input type="text" class="akaValidate" name="aka" id="akaId" value="" onchange="validateAKA(this.value,'${manufacturerInstance.id}', 'errSpan', 'manufacturer' )"/>
             </div>
             <input type="hidden" id="manageAkaId" value="-1" >
        </div>
		<script>
			currentMenuId = "#adminMenu";
			$("#adminMenuId a").css('background-color','#003366')
			
		</script>
    </body>
</html>
