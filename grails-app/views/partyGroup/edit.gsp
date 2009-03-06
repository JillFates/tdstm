

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Company</title>
    </head>
    <body>
    <div class="menu2">
		<ul>
			<li><g:link class="home" controller="partyGroup" action="show" id="${partyGroupInstance?.id}">Company</g:link></li>
			<li><g:link class="home" controller="person" id="${partyGroupInstance?.id}">Staff</g:link></li>
			<li><a href="#">Applications </a></li>
			<li><a href="#">Locations </a></li>
			<li><a href="#">Rooms </a></li>
		</ul>
		</div>
        <div class="body">
            <h1>Edit Company</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" name="editpartyGroup">
                <input type="hidden" name="id" value="${partyGroupInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                                                 
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
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
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
