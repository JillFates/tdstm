

<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Application List</title>
        <g:javascript library="prototype"/>

    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'jquery-1.3.1.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.core.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.draggable.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.resizable.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.dialog.js')}"></script>

    <script>

      $(document).ready(function() {

        $("#dialog").dialog({ autoOpen: false })
        $("#dialog1").dialog({ autoOpen: false })
        $("#dialog2").dialog({ autoOpen: false })

      })

    </script>
    
    <g:javascript>
      var rowId
      function showApplicationDialog( e ) {
		
      var application = eval('(' + e.responseText + ')')
		
      document.getElementById('id').value = application.id
  
      document.getElementById('applicationCodes').value = application.appCode
      document.getElementById('names').value = application.name
      document.getElementById('commentss').value = application.comment
      document.getElementById('environments').value = application.environment
      if( application.applicationOwnerFL != null){
      	document.getElementById('applicationOwners').value = application.applicationOwnerFL.firstName + application.applicationOwnerFL.lastName
      	document.getElementById('applicationOwnerd').value = application.applicationOwnerFL.id
      }else{
      	document.getElementById('applicationOwners').value = ""
      	document.getElementById('applicationOwnerd').value = ""
      }
      if( application.subjectMatterExpert != null){
      	document.getElementById('subjectMatterExperts').value = application.subjectMatterExpert.firstName + application.subjectMatterExpert.lastName
      	document.getElementById('subjectMatterExpertd').value = application.subjectMatterExpert.id	
      }else{
      	document.getElementById('subjectMatterExperts').value = ""
      	document.getElementById('subjectMatterExpertd').value = ""
      }
      if( application.primaryContact != null){
      	document.getElementById('primaryContacts').value = application.primaryContact.firstName + application.primaryContact.lastName
      	document.getElementById('primaryContactd').value = application.primaryContact.id
      }else{
      	document.getElementById('primaryContacts').value = ""
      	document.getElementById('primaryContactd').value = ""
      }
      if( application.secondContact != null){
      	document.getElementById('secondContacts').value = application.secondContact.firstName + application.secondContact.lastName
      	document.getElementById('secondContactd').value = application.secondContact.id
      }else{
      	document.getElementById('secondContacts').value = ""
      	document.getElementById('secondContactd').value = ""
      }
      
      document.getElementById('applicationCoded').value = application.appCode
      document.getElementById('named').value = application.name
      document.getElementById('commentsd').value = application.comment
      document.getElementById('environmentd').value = application.environment
     
      $("#dialog").dialog('option', 'width', 400)
      $("#dialog").dialog("open")

      }

      function editApplicationDialog() {
      document.getElementById('applicationCoded')
      
      $("#dialog").dialog("close")
      
      $("#dialog1").dialog('option', 'width', 500)
      
      $("#dialog1").dialog("open")
      
      }

      function showEditApplication(e) {

      $("#dialog1").dialog("close")
      var application = eval('(' + e.responseText + ')')

      var x=document.getElementById('applicationTable').rows
      var y=x[rowId].cells
      x[rowId].style.background = '#65a342'
      if(application.id == null) {
      y[1].innerHTML = ""
      }else{
      y[1].innerHTML = application.appCode
      }
      y[2].innerHTML = application.name
      
      
      y[5].innerHTML = application.comment

      }

      function callUpdateDialog() {
      
      var applicationId = document.getElementById('id')
      var applicationCode = document.getElementById('applicationCoded')
      var environment = document.getElementById('environmentd')
      var applicationOwner = document.getElementById('applicationOwnerd')
      var applicationOwners = document.getElementById('applicationOwners')
      var subjectMatterExpertd = document.getElementById('subjectMatterExpertd')
      var primaryContactd = document.getElementById('primaryContactd')
      var secondContactd = document.getElementById('secondContactd')
      var named = document.getElementById('named')
      var commentsd = document.getElementById('commentsd')
      var applicationNameDialog = new Array()
      applicationNameDialog[0] = applicationId.value
      applicationNameDialog[1] = applicationCode.value
      applicationNameDialog[2] = environment.value
      applicationNameDialog[3] = applicationOwner.value
      applicationNameDialog[4] = subjectMatterExpertd.value
      applicationNameDialog[5] = primaryContactd.value
      applicationNameDialog[6] = secondContactd.value
      applicationNameDialog[7] = named.value
      applicationNameDialog[8] = ((commentsd.value)?commentsd.value:"")
      applicationNameDialog[9] = "null"

      ${remoteFunction(action:'updateApplication', params:'\'applicationDialog=\' + applicationNameDialog', onSuccess:'showEditApplication(e)', onFailure:'alert("ApplicationCode already exist or should not be left blank")')}
      return true
      }

      function createDialog(){

      $("#dialog2").dialog('option', 'width', 500)
      $("#dialog2").dialog("open")

      }

      function setRowId(val){

      rowId = val.id

      }

    </g:javascript>
    </head>
    <body>
    <div class="menu2">
		<ul>
			<li><g:link class="home" controller="partyGroup" action="show" id="${partyGroupInstance?.id}">Company</g:link></li>
			<li><g:link class="home" controller="person" id="${partyGroupInstance?.id}">Staff</g:link></li>
			<li><g:link class="home" controller="application" id="${partyId}">Applications </g:link></li>
			<li><a href="#">Locations </a></li>
			<li><a href="#">Rooms </a></li>
		</ul>
	</div>
        <div class="body">
            <h1>Application List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${applicationInstance}">
            <div class="errors">
                <g:renderErrors bean="${applicationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <div class="list">
                <table id="applicationTable">
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="appCode" title="Application Code" />
                   	    
                   	        <g:sortableColumn property="name" title="Name" />
                        
                            <g:sortableColumn property="environment" title="Environment" />
                        
                   	        <g:sortableColumn property="lastUpdated" title="Owner" />
                   	        
                   	        <g:sortableColumn property="comment" title="Comment" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    
                     <%  int k = 1 %>
                    <g:each in="${applicationInstanceList}" status="i" var="applicationInstance">
                    <tr id="${k}" onClick="setRowId(this)" onmouseover="style.backgroundColor='#87CEEE';" onmouseout="style.backgroundColor='white';">
                       
                        
                            <td><g:remoteLink controller="application" action="editShow" id="${applicationInstance.id}" onComplete ="showApplicationDialog( e );">${fieldValue(bean:applicationInstance, field:'id')}</g:remoteLink></td>
                        
                            <td>${fieldValue(bean:applicationInstance, field:'appCode')}</td>
                        
                            <td>${fieldValue(bean:applicationInstance, field:'name')}</td>
                            
                            <td>${fieldValue(bean:applicationInstance, field:'environment')}</td>
                        
                            <td>${fieldValue(bean:applicationInstance, field:'owner')}</td>
                        
                            <td>${fieldValue(bean:applicationInstance, field:'comment')}</td>
                        
                        </tr>
                        <%  k = ++k %>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${Application.count()}" />
            </div>
            <div class="buttons">
                <g:form>
                <input type="hidden" name="partyId" id="partyId" value="${partyId}">
                    <span class="button"><g:actionSubmit class="create" value="Create a New Application" action="create" /></span>
                </g:form>
            </div>
        </div>
       
      <div id="dialog" title="Show Application" style="display:none;">
      <g:form method="post" name="showForm">
      <div class="dialog">
        <table>
          <tbody>

            <tr class="prop">
              <td valign="top" class="name">Application Code:</td>

              <td valign="top" class="value"><input type="text" id="applicationCodes" name="applicationCodes" value="" style="border: 0px" readonly></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">name:</td>

              <td valign="top" class="value"><input type="text" id="names" name="names" value="" style="border: 0px" readonly></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Environment:</td>

              <td valign="top" class="value"><input type="text" id="environments" name="environments" value="" style="border: 0px" readonly></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Application Owner:</td>

              <td valign="top" class="value"><input type="text" id="applicationOwners" name="applicationOwners" value="" style="border: 0px" readonly></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Subject Matter Expert:</td>

              <td valign="top" class="value"><input type="text" id="subjectMatterExperts" name="subjectMatterExperts" value="" style="border: 0px" readonly></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Primary Contact:</td>

              <td valign="top" class="value"><input type="text" id="primaryContacts" name="primaryContacts" value="" style="border: 0px" readonly></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Second Contact:</td>

              <td valign="top" class="value"><input type="text" id="secondContacts" name="secondContacts" value="" style="border: 0px" readonly></td>

            </tr>
           
            <tr class="prop">
              <td valign="top" class="name">Comment:</td>

              <td valign="top" class="value"><input type="text" id="commentss" name="commentss" value="" style="border: 0px" readonly></td>

            </tr>

           </tbody>
        </table>
      </div>
      <div class="buttons">
        
          <span class="button"><input type="button" class="edit" value="Edit" onClick="return editApplicationDialog()"/></span>
        </g:form>
      </div>
    </div>

    <div id="dialog1" title="Edit Application" style="display:none;">
     <g:form method="post" name="editForm">
        <input type="hidden" id="id" name="id" value="" />
        <div class="dialog">
          <table>
            <tbody>

              

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="applicationName">Application Code:</label>
                </td>
                <td valign="top">
                  <input type="text" id="applicationCoded" name="applicationCoded" value=""/>
                </td>
              </tr>

              <tr class="prop">
              <td valign="top" class="name">name:</td>

              <td valign="top" class="value"><input type="text" id="named" name="named" value="" ></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Environment:</td>

              <td valign="top" class="value"><g:select id="environmentd" name="environmentd" from="${applicationInstance.constraints.environment.inList}" value="${applicationInstance.environment}" ></g:select></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Application Owner:</td>

              <td valign="top" class="value"><tds:staffSelect optionKey="id" id="applicationOwnerd" name="applicationOwnerd" from="${Person.list()}" companyId="${partyId}" default="${personId}" isNew="true" /></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Subject Matter Expert:</td>
 
              <td valign="top" class="value"><tds:staffSelect optionKey="id" id="subjectMatterExpertd" name="subjectMatterExpertd" from="${Person.list()}" companyId="${partyId}" default="${personId}" isNew="true"/></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Primary Contact:</td>

              <td valign="top" class="value"><tds:staffSelect optionKey="id" id="primaryContactd" name="primaryContactd" from="${Person.list()}" companyId="${partyId}" default="${personId}" isNew="true" /></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Second Contact:</td>

              <td valign="top" class="value"><tds:staffSelect optionKey="id" id="secondContactd" name="secondContactd" from="${Person.list()}" companyId="${partyId}" default="${personId}" isNew="true" /></td>

            </tr>
            
            <tr class="prop">
              <td valign="top" class="name">Comment:</td>

              <td valign="top" class="value"><input type="text" id="commentsd" name="commentsd" value=""  ></td>

            </tr>


            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><input type="button" class="save" value="Update Application" onClick="return callUpdateDialog()"/></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </div>
      </g:form>
    </div>
    </body>
</html>
