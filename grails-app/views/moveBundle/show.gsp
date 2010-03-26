

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="moveBundleHeader" />
    <title>Show Move Bundle</title>
    <script type="text/javascript">
    
   /*
    function to invoke ESC key to abandon the field
   */
    document.onkeypress = keyCheck;   
	function keyCheck( e ){
		var keyID 
		if(window.event){
			keyID = window.event.keyCode;
		} else {
			keyID = e.keyCode;
		}
		if(keyID == 27) {
			$("span[title='input']").each(function(){ 
		    	  $(this).hide(); // hide value input field
	    	});
			$("span[title='text']").each(function(){ 
		    	  $(this).show(); // show the value text 
	  		});
		}
	}
	/*
	* will call the Ajax web service to invoke the moveBundle/createSnapshot method 
	* when user hit the enter key
	*/
    function createSnapshot( stepId, value, e ) {
        var moveBundle = $("#moveBundleId").val()
    	var keyID = e.keyCode
    	if(keyID == 13){
    		${remoteFunction(controller:'moveBundle', action:'createManualStep', params:'\'moveBundleId=\'+ moveBundle +\'&moveBundleStepId=\'+ stepId +\'&tasksCompleted=\'+value', onComplete:'updateStepValue(e , stepId, value)')}
    	}
    }
    /*
    * update the value once ajax request success
    */
    
    function updateStepValue(e, stepId, value){
        if(e.status == 200){
			$("#tasksCompletedText_"+stepId).html(value);
			$("#tasksCompletedText_"+stepId).show();
			$("#tasksCompletedInput_"+stepId).hide();
        } else {
            alert("Error : "+e.status+", Record not created")
        }
    }
    </script>
  </head>
  <body>   
    
    <div class="body" style="width: auto;">
    	<div class="steps_table">
      <span class="span"><b> Show Move Bundle </b></span>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
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
              <td valign="top" class="name">Move Event:</td>

              <td valign="top" class="value"> ${fieldValue(bean:moveBundleInstance, field:'moveEvent')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Order:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'operationalOrder')}</td>

            </tr>

          </tbody>
        </table>
      </div>
      <br/>
      <div class="buttons" style="float: left;">
        <g:form>
          <input type="hidden" name="id" value="${moveBundleInstance?.id}" />
          <input type="hidden" name="projectId" value="${projectId}" />
          <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </g:form>
      </div>
    </div>
    <div class="body" style="margin: 0;">
    <div  class="steps_table">
		<span class="span"><b>Dashboard Steps </b></span>
		<table id="assetEntityTable">
			<thead>
				<tr>
					<th>Dashboard Label</th>
					<th>Start</th>
					<th>Completion</th>
					<th>Duration</th>
					<th>Type</th>
					<th>Value<input type="hidden" name="moveBundleId" id="moveBundleId" value="${moveBundleInstance?.id}" />
		        <input type="hidden" name="projectId" value="${projectId}"/></th>
				</tr>
			</thead>
			<tbody id="commetAndNewsBodyId">
		        <g:each in="${ dashboardSteps }"	status="i" var="dashboardStep">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}" id="commentsRowId_${dashboardStep.moveBundleStep.id }">
						<td>
							${dashboardStep.moveBundleStep.label}
						</td>
						<td><tds:convertToGMT date="${dashboardStep.moveBundleStep.planStartTime}"/></td>
						<td><tds:convertToGMT date="${dashboardStep.moveBundleStep.planCompletionTime}"/></td>
						<td><tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/></td>
						<td>
						<g:if test="${dashboardStep.moveBundleStep?.calcMethod != 'L'}">
							Manual
						</g:if>
						<g:else>Linear</g:else>
						</td>
						<g:if test="${dashboardStep.moveBundleStep?.calcMethod == 'M'}">
						<td onclick="$('#tasksCompletedText_${dashboardStep.moveBundleStep.id }').hide();$('#tasksCompletedInput_${dashboardStep.moveBundleStep.id }').show();">
							<span style="display: none;" id="tasksCompletedInput_${dashboardStep.moveBundleStep.id }" title="input">
								<input type="text" name="tasksCompleted" style="width: 25px;" id="tasksCompleted_${dashboardStep.moveBundleStep.id }" maxlength="3" 
								onkeypress="createSnapshot(${dashboardStep.moveBundleStep.id }, this.value, event )"/>
							</span>
							<span id="tasksCompletedText_${dashboardStep.moveBundleStep.id }" title="text">${dashboardStep.stepSnapshot?.tasksCompleted}</span>
							%
						</td></g:if>
						<g:else>
						<td>
							<span>${dashboardStep.stepSnapshot?.tasksCompleted}</span>
						</td></g:else>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
	<div>
          <span style="float: right;">Dashboard Server : <input type="button" name="serverOn" value="On" />&nbsp;<input type="button" name="serverOff" value="Off" /></span>
      </div>
	</div>
  </body>
</html>
