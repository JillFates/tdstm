<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create PartyGroup</title>         
    </head>
    <body>
        <div class="body">
            <h1>Create PartyGroup</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>

            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                          <g:hasErrors bean="${partyGroupInstance}" idCheck>
					            <div class="errors">
					                <g:renderErrors bean="${partyGroupInstance}" as="list" idCheck/>
					            </div>
					            </g:hasErrors>
                            <tr class="prop">
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
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:partyGroupInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:partyGroupInstance,field:'name')}"/>
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
                                    <input type="text" id="comment" name="comment" value="${fieldValue(bean:partyGroupInstance,field:'comment')}"/>
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
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
