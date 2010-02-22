

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="moveBundleHeader" />
   
    <title>Show MoveBundle</title>
  </head>
  <body>   
    
    <div class="body">
      <h1>Show Move Bundle</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="dialog">
        <table>
          <tbody>

            <tr class="prop">
              <td valign="top" class="name">Name:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'name')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Description:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'description')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Start Time:</td>

              <td valign="top" class="value"><tds:convertDateTime date="${moveBundleInstance?.startTime}" /></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Completion Time:</td>

              <td valign="top" class="value"><tds:convertDateTime date="${moveBundleInstance?.completionTime}" /></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Project Manager:</td>

              <td valign="top" class="value">
                <g:if test="${projectManager}">
                  ${projectManager?.partyIdTo?.lastName}, ${projectManager?.partyIdTo?.firstName} - ${projectManager?.partyIdTo?.title}
                </g:if>
              </td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Move Manager:</td>

              <td valign="top" class="value">
                <g:if test="${moveManager}">
                  ${moveManager?.partyIdTo?.lastName}, ${moveManager?.partyIdTo?.firstName} - ${moveManager?.partyIdTo?.title}
                </g:if>
              </td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Order:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'operationalOrder')}</td>

            </tr>

          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <input type="hidden" name="id" value="${moveBundleInstance?.id}" />
          <input type="hidden" name="projectId" value="${projectId}" />
          <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </g:form>
      </div>
    </div>
    <div style="float: left;" class="steps_table">
    <h1>Move Bundle Steps</h1>
    <g:if test="${flash.result}">
        <div class="message">${flash.result}</div>
    </g:if>
	<g:form action="createManualStep">
      <div class="dialog">
        <table >
        	<thead>
	        	<tr>
	        		<th>Label</th>
	        		<th>Start Time</th>
	        		<th>Completion Time</th>
	        		<th>Type</th>
	        		<th>Value</th>
	        	</tr>
	        </thead>
          <tbody>

          		<input type="hidden" name="moveBundleId" value="${moveBundleInstance?.id}" />
          		<input type="hidden" name="projectId" value="${projectId}">
          		<g:each in="${MoveBundleStep.findAll('FROM MoveBundleStep mbs WHERE mbs.calcMethod = :cm AND mbs.moveBundle = :mb ',[cm:'M',mb:moveBundleInstance]) }"
          			status="i" var="moveBundleStep">
				<tr>
				<td>
				<input type="hidden" value="${moveBundleStep.id }" name="moveBundleStepId">
				${moveBundleStep.label}</td>
				<td><tds:convertDateTime date="${moveBundleStep.planStartTime}" formate="mm/dd"/></td>
				<td><tds:convertDateTime date="${moveBundleStep.planCompletionTime}" formate="mm/dd"/></td>
				<td>Manual</td>
				<td><input type="text" name="tasksCompleted_${moveBundleStep.id}" style="width: 25px;" maxlength="3">%</td>
				</tr>
				</g:each>
          </tbody>
        </table>
        <div class="buttons">
          <span class="button"><input type="submit" class="save" value="Save" /></span>
      </div>
      </div>
      </g:form>
    </div>
  </body>
</html>
