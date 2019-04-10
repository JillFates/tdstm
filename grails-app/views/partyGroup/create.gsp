<%@page import="net.transitionmanager.party.PartyType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
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
        <r:layoutResources />
    </head>
    <body>
        <tds:subHeader title="Create Company" crumbs="['Admin','Company', 'Create']"/><br/>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>

            <g:form action="save" method="post" name="editpartyGroup" >
                <div class="dialog">
                    <table>
                        <tbody>
                        <tr>
							<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
						</tr>
                        <g:hasErrors bean="${partyGroupInstance}">
							<div class="errors">
								<g:renderErrors bean="${partyGroupInstance}" as="list" />
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

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Partner:</label>
                                </td>
                                <td valign="top">
                                    <input type="checkbox" name="partner" value="Y"> <span style="padding-left: 5px"><b>Note:</b><i>Partners can participate on projects of other companies</i></span>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
			<div class="buttons">
				<span class="button">
					<input class="save" type="submit" value="Save" />
				</span>
				<span class="button">
					<input type="button" class="cancel" value="Cancel" onclick="window.history.back()"/>
				</span>
			</div>
            </g:form>
        </div>
<script>
	currentMenuId = "#adminMenu";
    $('.menu-list-companies').addClass('active');
    $('.menu-parent-admin').addClass('active');

</script>
<r:layoutResources />
    </body>
</html>
