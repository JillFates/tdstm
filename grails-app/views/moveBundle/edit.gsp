

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="moveBundleHeader" />
    <title>Edit MoveBundle</title>

    <g:javascript library="jquery"/>

    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" />

    <jq:plugin name="ui.core"/>
    <jq:plugin name="ui.draggable"/>
    <jq:plugin name="ui.resizable"/>
    <jq:plugin name="ui.dialog"/>
    <jq:plugin name="ui.datetimepicker"/>
    
    <g:javascript>

      function initialize(){
      // This is called when the page loads to initialize Managers
      $('#moveManagerId').val('${moveManager}');
      $('#projectManagerId').val('${projectManager}');

      }
	
    </g:javascript>
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
				$("input[title='linear']").each(function(){ 
			    	  $(this).attr("checked",false); // show the value text 
		  		});
			}
			
		}
    	function enableInput( stepId){
    		if($("#checkbox_"+stepId ).is(':checked')){
				$("#labelText_"+stepId ).hide();
				$("#labelInput_"+stepId ).show();
				$("#startTimeText_"+stepId ).hide();
				$("#startTimeInput_"+stepId ).show();
				$("#completionTimeText_"+stepId ).hide();
				$("#completionTimeInput_"+stepId ).show();
				$("#durationText_"+stepId ).hide();
				$("#durationInput_"+stepId ).show();
				$("#calcMethodText_"+stepId ).hide();
				$("#calcMethodInput_"+stepId ).show();
				if($("#calcMethod__"+stepId) == "M"){
					$("#tasksCompletedText_"+stepId ).hide();
					$("#tasksCompletedInput_"+stepId ).show();
				}
				var moveBundle = $("#moveBundleId").val()
				//${remoteFunction(controller:'moveBundle', action:'createMoveBundleStep', params:'\'moveBundleId=\'+ moveBundle +\'&transitionId=\'+ stepId ')}
    		} else {
    			$("#labelText_"+stepId ).show();
				$("#labelInput_"+stepId ).hide();
				$("#startTimeText_"+stepId ).show();
				$("#startTimeInput_"+stepId ).hide();
				$("#completionTimeText_"+stepId ).show();
				$("#completionTimeInput_"+stepId ).hide();
				$("#durationText_"+stepId ).show();
				$("#durationInput_"+stepId ).hide();
				$("#calcMethodText_"+stepId ).show();
				$("#calcMethodInput_"+stepId ).hide();
				$("#tasksCompletedText_"+stepId ).show();
				$("#tasksCompletedInput_"+stepId ).hide();
    		}
        }
        function showTaskCompleted(type, stepId){
			if(type == "M" ){
				$("#tasksCompletedText_"+stepId ).hide();
				$("#tasksCompletedInput_"+stepId ).show();
			} else {
				$("#tasksCompletedText_"+stepId ).show();
				$("#tasksCompletedInput_"+stepId ).hide();
			}
        }
        function calculateDuration( stepId ){
            var start = $("#startTime_"+stepId).val()
            var completion = $("#completionTime_"+stepId).val()
            if(completion && start ){
            	var duration
                var startTime = new Date( start ).getTime()
                var completionTime  = new Date( completion ).getTime()
                if(completionTime > startTime){
                	duration = completionTime - startTime    
                } else {
                    alert("Completion Time should be greater than Start Time")
                }
                if(duration){
                	$("#duration_"+stepId).val(convertIntoHHMM(duration / 1000))
                	$("#durationIn_"+stepId).val(duration / 1000)
                }
            }
        } 
        function convertIntoHHMM( seconds ){
        	var timeFormate 
    	    var hours = parseInt(seconds / 3600) 
    	    	timeFormate = hours >= 10 ? hours : '0'+hours
    	    var minutes =  parseInt((seconds % 3600 ) / 60 )
    	    	timeFormate += ":"+(minutes >= 10 ? minutes : '0'+minutes)
    	    	return timeFormate
        }
        function changeCompletionTime(time, stepId){
            var hours = time.substring(0,2)
            var min = time.substring(3,5)
            if(hours  && min){
                var ms = (3600000 * hours )+ (60000 * min)
            }
            var completionTimeString = $("#completionTime_"+stepId).val()
            if(completionTimeString){
    	        var completionTime = new Date( completionTimeString )
        	    var updatedTime = new Date(completionTime.getTime() + ms)
	     		var month =  updatedTime.getMonth() + 1;
        		var monthday    = updatedTime.getDate();
        		var year        = updatedTime.getYear() + 1900;
        		
        		var hour   = updatedTime.getHours();
        		var minute = updatedTime.getMinutes();
        		var second = updatedTime.getSeconds();
        		
        		if (hour   < 10) { hour   = "0" + hour;   }
        		if (minute < 10) { minute = "0" + minute; }
        		if (second < 10) { second = "0" + second; }
        		var timeString = month+"/"+monthday+"/"+year+" "+hour + ':' + minute ;
        		$("#completionTime_"+stepId).val( timeString )
            }
        }
    </script>
  </head>
  <body>
    <div class="body" style="width: 350px;">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:form method="post" >
        <input type="hidden" name="id" value="${moveBundleInstance?.id}" />
        <div class="steps_table">
        <span class="span"><b> Edit Move Bundle </b></span>
          <table>
            <tbody>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="name">Name:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'name','errors')}">
                  <input type="text" id="name" name="name" value="${fieldValue(bean:moveBundleInstance,field:'name')}"/>
                  <g:hasErrors bean="${moveBundleInstance}" field="name">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="name"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="description">Description:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'description','errors')}">
                  <input type="text" id="description" name="description" value="${fieldValue(bean:moveBundleInstance,field:'description')}"/>
                  <g:hasErrors bean="${moveBundleInstance}" field="description">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="description"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="startTime">Start Time:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'startTime','errors')}">
                  <link rel="stylesheet"
                        href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" />
                  <script type="text/javascript">
                    $(document).ready(function(){
                      $("#startTime").datetimepicker();
                    });
                  </script> <input type="text" class="dateRange" size="15" readOnly style="width: 112px; height: 14px;" id="startTime" name="startTime"
        					value="<tds:convertDateTime date="${moveBundleInstance?.startTime}"/>">
        					<g:hasErrors bean="${moveBundleInstance}" field="startTime">
                    		<div class="errors">
                      			<g:renderErrors bean="${moveBundleInstance}" as="list" field="startTime"/>
                    		</div>
                  			</g:hasErrors>
               	</td>
             </tr>
	
    	        <tr class="prop">
                <td valign="top" class="name">
                  <label for="completionTime">Completion Time:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'completionTime','errors')}">
                  <link rel="stylesheet"
                        href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" />
                  <script type="text/javascript">
                    $(document).ready(function(){
                      $("#completionTime").datetimepicker();
                    });
                  </script> <input type="text" class="dateRange" size="15" readOnly
				        style="width: 112px; height: 14px;" id="completionTime" name="completionTime"
				        value="<tds:convertDateTime date="${moveBundleInstance?.completionTime}"/>">
				        <g:hasErrors bean="${moveBundleInstance}" field="completionTime">
				                    <div class="errors">
				                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="completionTime"/>
				                    </div>
				                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="projectManager">Project
                Manager:</label></td>
                <td valign="top" class="value"><select id="projectManagerId"
                                 name="projectManager">

                    <option value="" selected="selected">Please Select</option>

                    <g:each status="i" in="${managers}" var="managers">
                      <option value="${managers?.staff?.id}">${managers?.staff?.lastName}, ${managers?.staff?.firstName}<g:if test="${managers?.staff?.title}"> - ${managers?.staff?.title}</g:if></option>
                    </g:each>

                </select></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="moveManager">Move
                Manager:</label></td>
                <td valign="top" class="value"><select id="moveManagerId"
                                    name="moveManager">

                    <option value="" selected="selected">Please Select</option>

                    <g:each status="i" in="${managers}" var="managers">
                      <option value="${managers?.staff?.id}">${managers?.staff?.lastName}, ${managers?.staff?.firstName}<g:if test="${managers?.staff?.title}"> - ${managers?.staff?.title}</g:if></option>
                    </g:each>

                </select></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="operationalOrder">Move Event:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'moveEvent','errors')}">
                  <g:select from="${MoveEvent.findAllByProject(Project.get(projectId))}" id="moveEvent" name="moveEvent.id" 
                  value="${moveBundleInstance?.moveEvent?.id}" optionKey="id" noSelection="['':'Please Select']"></g:select>
                  <g:hasErrors bean="${moveBundleInstance}" field="moveEvent">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="moveEvent"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="operationalOrder">Order:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'operationalOrder','errors')}">
                  <g:select from="${1..25}" id="operationalOrder" name="operationalOrder" value="${moveBundleInstance?.operationalOrder}" ></g:select>
                  <g:hasErrors bean="${moveBundleInstance}" field="operationalOrder">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="operationalOrder"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

            </tbody>
          </table>
        </div>
        <br>
        <div class="buttons" style="float: left;">
          <input type="hidden" name="project.id" value="${projectId }"/>
          <input type="hidden" name="projectId" value="${projectId }"/>
          <span class="button"><g:actionSubmit class="save" value="Update" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Cancel" action="show" /></span>
        </div>
      <g:javascript>
        initialize();
      </g:javascript>
    </div>
    <div class="body" style="margin: 0;">
   		<g:if test="${remainingSteps}">
        	<div class="message">There are some steps which are not associated to current dashboard steps</div>
     	</g:if>
    <div  class="steps_table">
		<span class="span"><b>Dashboard Steps </b></span>
		<table id="assetEntityTable">
			<thead>
				<tr>
					<th>Key off Step</th>
					<th>Dashboard</th>
					<th>Dashboard Label</th>
					<th>Start</th>
					<th>Completion</th>
					<th>Duration</th>
					<th>Type</th>
					<th>Value</th>
				</tr>
			</thead>
			<tbody id="commetAndNewsBodyId">
				<input type="hidden" name="moveBundleId" id="moveBundleId" value="${moveBundleInstance?.id}" />
		        <g:each in="${ dashboardSteps }"	status="i" var="dashboardStep">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}" id="commentsRowId_${dashboardStep.step.id }">
							<td>
								${dashboardStep.step.name}
							</td>
							<td> <input type="checkbox" name="checkbox_${dashboardStep.step.id }" id="checkbox_${dashboardStep.step.id }" 
								onclick="enableInput(${dashboardStep.step.id })"> </td>
							<td>
								<span id="labelText_${dashboardStep.step.id }" title="text">${dashboardStep.moveBundleStep?.label}</span>
								<span id="labelInput_${dashboardStep.step.id }" style="display: none;" title="input">
								<input type="text" name="dashboardLabel_${dashboardStep.step.id }" value="${dashboardStep.moveBundleStep?.label}">
								</span>
							</td>
							<td>
							<span id="startTimeText_${dashboardStep.step.id }" title="text"><tds:convertToGMT date="${dashboardStep.moveBundleStep?.planStartTime}"/></span>
							
							<span id="startTimeInput_${dashboardStep.step.id }" style="display: none;" title="input">
								<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" />
			                  <script type="text/javascript">
			                    $(document).ready(function(){
			                      $("#startTime_${dashboardStep.step.id }").datetimepicker();
			                    });
			                  </script>
								<input type="text" name="startTime_${dashboardStep.step.id }" id="startTime_${dashboardStep.step.id }"
								value="<tds:convertToGMT date='${dashboardStep.moveBundleStep?.planStartTime}'/>" onchange="calculateDuration(${dashboardStep.step.id })">
							</span>
							</td>
							<td>
							<span id="completionTimeText_${dashboardStep.step.id }" title="text"><tds:convertToGMT date="${dashboardStep.moveBundleStep?.planCompletionTime}"/></span>
							
							<span id="completionTimeInput_${dashboardStep.step.id }" style="display: none;" title="input">
							<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" />
			                  <script type="text/javascript">
			                    $(document).ready(function(){
			                      $("#completionTime_${dashboardStep.step.id }").datetimepicker();
			                    });
			                  </script>
								<input type="text" name="completionTime_${dashboardStep.step.id }" id="completionTime_${dashboardStep.step.id }" 
								value="<tds:convertToGMT date='${dashboardStep.moveBundleStep?.planCompletionTime}'/>" onchange="calculateDuration(${dashboardStep.step.id })">
							</span>
							</td>
							<td>
							<span id="durationText_${dashboardStep.step.id }" title="text">
							<tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/> </span>
							
							<span id="durationInput_${dashboardStep.step.id }" style="display: none;" title="input">
							<input type="hidden" name="duration_${dashboardStep.step.id }" id="durationIn_${dashboardStep.step.id }"
								value="<tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/>">
								<input type="text" id="duration_${dashboardStep.step.id }"	style="width: 60px;"
								value="<tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/>"	onchange="changeCompletionTime(this.value, ${dashboardStep.step.id })">
							</span>
							</td>
							<td>
							<span id="calcMethodText_${dashboardStep.step.id }" title="text">
							<g:if test="${dashboardStep.moveBundleStep}"><g:if test="${dashboardStep.moveBundleStep?.calcMethod != 'L'}">Manual</g:if>
							<g:else>Linear</g:else></g:if>
							</span>
							<span id="calcMethodInput_${dashboardStep.step.id }" style="display: none;" title="input">
								<g:select from="${['L', 'M']}" valueMessagePrefix="step.calcMethod" name="calcMethod_${dashboardStep.step.id }"
								value="${dashboardStep.moveBundleStep?.calcMethod}" onchange="showTaskCompleted(this.value, ${dashboardStep.step.id })"/>
							</span>
							</td>
							
							<td>
								<span id="tasksCompletedText_${dashboardStep.step.id }" title="text">${dashboardStep.stepSnapshot?.tasksCompleted}
								</span>
								
								<span style="display: none;" id="tasksCompletedInput_${dashboardStep.step.id }" title="input">
									<input type="text" name="tasksCompleted_${dashboardStep.step.id }" style="width: 25px;" value="${dashboardStep.stepSnapshot?.tasksCompleted}" 
									id="tasksCompleted_${dashboardStep.step.id }" maxlength="3" >
								</span><g:if  test="${dashboardStep.moveBundleStep?.calcMethod == 'M'}">%</g:if>
							</td>
							
					</tr>
				</g:each>
				</g:form>
			</tbody>
		</table>
	</div>
	<div>
          <span style="float: right;">Dashboard Server : <input type="button" name="serverOn" value="On" />&nbsp;<input type="button" name="serverOff" value="Off" /></span>
      </div>
	</div>
  </body>
</html>
