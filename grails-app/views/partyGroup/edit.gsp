<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>Edit Company</title>

        <g:javascript>
        function textCounter(field, maxlimit){
            if (field.value.length > maxlimit){ // if too long...trim it!
                field.value = field.value.substring(0, maxlimit);
                return false;
            } else {
                return true;
            }
        }
        </g:javascript>
    </head>
    <body>
    <tds:subHeader title="Edit Company" crumbs="['Admin','Company', 'Edit']"/><br/>
    <div class="body">
            <!-- <h1>Edit Company</h1> -->
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" name="editpartyGroup">
                <input type="hidden" name="id" value="${partyGroupInstance?.id}" />
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
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:partyGroupInstance,field:'name')}" maxlength="64" size="64"/>
                                <g:hasErrors bean="${partyGroupInstance}" field="name">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="name"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'comment','errors')}">
                                    <textarea rows="3" cols="80" name="comment"	onkeydown="textCounter(document.editpartyGroup.comment,200);" onkeyup="textCounter(document.editpartyGroup.comment,200);">${fieldValue(bean:partyGroupInstance,field:'comment')}</textarea>
                                <g:hasErrors bean="${partyGroupInstance}" field="comment">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="comment"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Partner:</label>
                                </td>
                                <td valign="top">
                                    <input type="checkbox" name="partner" value="Y" <g:if test="${partner}">checked="true"</g:if> <g:if test="${projectPartner}">disabled="true"</g:if> />
                                    <span style="padding-left: 5px"><b>Note:</b><i>Partners can participate on projects of other companies</i></span>
                                </td>
                            </tr> 

                        </tbody>
                    </table>
                </div>
		<div class="buttons">
                  <span class="button">
				<g:actionSubmit class="save" value="Update" />
			</span>
                  <span class="button">
				<g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" />
			</span>
                  <span class="button">
				<input type="button" class="cancel" value="Cancel" id="cancelButtonId" onclick="window.location = contextPath + '/partyGroup/show/${partyGroupInstance?.id}'"/>
			</span>
		</div>
            </g:form>
        </div>
<script>
	currentMenuId = "#adminMenu";
    $('.menu-list-companies').addClass('active');
    $('.menu-parent-admin').addClass('active');
</script>
    </body>
<r:layoutResources />
</html>
