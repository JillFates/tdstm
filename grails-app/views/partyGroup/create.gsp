<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Create Company</title>

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
        <div class="body">
            <h1>Create Company</h1>
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>

            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
						</tr>
                        <g:hasErrors bean="${partyGroupInstance}" idCheck>
							<div class="errors">
								<g:renderErrors bean="${partyGroupInstance}" as="list" idCheck/>
							</div>
						</g:hasErrors>

                           <!--  <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="partyType">Party Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'partyType','errors')}">
                                    <g:select optionKey="id" from="${PartyType.list()}" name="partyType.id" value="${partyGroupInstance?.partyType?.id}" noSelection="['null':'']"></g:select>
                                <g:hasErrors bean="${partyGroupInstance}" field="partyType">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="partyType"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> -->
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                	<input  type="hidden" name="partyType.id" value="COMPANY"/>
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
                                   <textarea rows="3" cols="80"  name="comment"	onkeydown="textCounter(document.editpartyGroup.comment,200);" onkeyup="textCounter(document.editpartyGroup.comment,200);">${fieldValue(bean:partyGroupInstance,field:'comment')}</textarea>
                                <g:hasErrors bean="${partyGroupInstance}" field="comment">
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" field="comment"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 

                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Save" /></span>
                </div>
            </g:form>
        </div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
