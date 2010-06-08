

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="moveBundleHeader" />
    <title>Edit MoveBundle</title>

	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" type="text/css"/>

    
    <g:javascript>

      function initialize(){
      // This is called when the page loads to initialize Managers
      $('#moveManagerId').val('${moveManager}');
      $('#projectManagerId').val('${projectManager}');

      }
	
    </g:javascript>
  </head>
  <body>
  	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px;width: 219px; margin:9px 14px 0px">
			<span class="menuButton"><g:link class="list" action="list">Bundle List</g:link></span>
		    <jsec:hasRole name="ADMIN">
		    	<span class="menuButton"><g:link class="create" action="create">New Bundle</g:link></span>
			</jsec:hasRole>
	</div>
  <g:form method="post" >
    <div class="body" style="width: 350px;">
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
        <input type="hidden" name="id" value="${moveBundleInstance?.id}" />
        <div class="steps_table">
        <span class="span"><b> Edit Move Bundle </b></span>
          <table>
            <tbody>
			<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="name"><b>Name:&nbsp;<span style="color: red">*</span></b></label>
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
                  <script type="text/javascript">
                    $(document).ready(function(){
                      $("#startTime").datetimepicker();
                    });
                  </script><span><input type="text" class="dateRange" size="15" style="width: 130px; height: 14px;" id="startTime" name="startTime"
        					value="<tds:convertDateTime date="${moveBundleInstance?.startTime}"  formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
        					onchange="isValidDate(this.value, this.id,'startTimeImg')"/></span>
        					<span id="startTimeImg" style="display: none;"><img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}"></span>
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
                  <script type="text/javascript">
                    $(document).ready(function(){
                      $("#completionTime").datetimepicker();
                    });
                  </script> <span><input type="text" class="dateRange" size="15" style="width: 130px; height: 14px;" 
                  			id="completionTime" name="completionTime"
				        value="<tds:convertDateTime date="${moveBundleInstance?.completionTime}"  formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
				        onchange="isValidDate(this.value, this.id, 'completionTimeImg')"/></span>
				        <span id="completionTimeImg" style="display: none;"><img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}"></span>
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
                  value="${moveBundleInstance?.moveEvent?.id}" optionKey="id" noSelection="['null':'Please Select']"></g:select>
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
        <br/>
        <div class="buttons" style="float: left;">
          <input type="hidden" name="project.id" value="${projectId }"/>
          <input type="hidden" name="projectId" value="${projectId }"/>
          <span class="button"><g:actionSubmit class="save" value="Update" onclick=" return validateStepsData()"/></span>
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
					<th>Value 
					<input type="hidden" name="moveBundleId" id="moveBundleId" value="${moveBundleInstance?.id}" />
					</th>
				</tr>
			</thead>
			<tbody id="commetAndNewsBodyId">
		        <g:each in="${ dashboardSteps }"	status="i" var="dashboardStep">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}" id="commentsRowId_${dashboardStep.step.id }">
							<td>
								${dashboardStep.step.name}
								<input type="hidden"  id="keyOffStep_${dashboardStep.step.id }" value="${dashboardStep.step.name}"/>
							</td>
							<g:if test="${dashboardStep.moveBundleStep}">
								<td>
								 <input type="checkbox" name="checkbox_${dashboardStep.step.id }" id="checkbox_${dashboardStep.step.id }" 
									onclick="enableInput(${dashboardStep.step.id })" checked="checked"/>
								</td>
								<td>
									<span id="labelText_${dashboardStep.step.id }" title="text" style="display: none;" >${dashboardStep.moveBundleStep?.label}</span>
									<span id="labelInput_${dashboardStep.step.id }" title="input">
									<input type="text" name="dashboardLabel_${dashboardStep.step.id }" id="dashboardLabel_${dashboardStep.step.id }" 
									value="${dashboardStep.moveBundleStep?.label ? dashboardStep.moveBundleStep?.label : dashboardStep.step.name}"/>
									</span>
								</td>
								<td>
								<span id="startTimeText_${dashboardStep.step.id }" title="text" style="display: none;" >
									<tds:convertDateTime date="${dashboardStep.moveBundleStep?.planStartTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
								</span>
								
								<span id="startTimeInput_${dashboardStep.step.id }" title="input">
				                  <script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#startTime_${dashboardStep.step.id }").datetimepicker();
				                    });
				                  </script>
									<input type="text" name="startTime_${dashboardStep.step.id }" id="startTime_${dashboardStep.step.id }"
									value="<tds:convertDateTime date='${dashboardStep.moveBundleStep?.planStartTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
									onchange="getTimeFormate(this.id, this.value, ${dashboardStep.step.id })"/>
								</span>
								<span id="startTimeImg_${dashboardStep.step.id }" style="display: none;"><img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}"></span>
								</td>
								<td>
								<span id="completionTimeText_${dashboardStep.step.id }" title="text" style="display: none;" >
									<tds:convertDateTime date="${dashboardStep.moveBundleStep?.planCompletionTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
								</span>
								
								<span id="completionTimeInput_${dashboardStep.step.id }" title="input">
				                  <script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#completionTime_${dashboardStep.step.id }").datetimepicker();
				                    });
				                  </script>
									<input type="text" name="completionTime_${dashboardStep.step.id }" id="completionTime_${dashboardStep.step.id }" 
									value="<tds:convertDateTime date='${dashboardStep.moveBundleStep?.planCompletionTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
									onchange="getTimeFormate(this.id, this.value, ${dashboardStep.step.id })"/>
								</span>
								<span id="completionTimeImg_${dashboardStep.step.id }" style="display: none;"><img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}"></span>
								</td>
								<td>
								<span id="durationText_${dashboardStep.step.id }" title="text" style="display: none;" >
								<tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/> </span>
								
								<span id="durationInput_${dashboardStep.step.id }" title="input">
								<input type="hidden" name="duration_${dashboardStep.step.id }" id="durationIn_${dashboardStep.step.id }"
									value="${dashboardStep.stepSnapshot?.duration}"/>
									<input type="text" id="duration_${dashboardStep.step.id }"	style="width: 60px;"
									value="<tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/>"	onchange="changeCompletionTime(this.value, ${dashboardStep.step.id })"/>
								</span>
								</td>
								<td>
								<span id="calcMethodText_${dashboardStep.step.id }" title="text" style="display: none;" >
								<g:if test="${dashboardStep.moveBundleStep}"><g:if test="${dashboardStep.moveBundleStep?.calcMethod != 'L'}">Manual</g:if>
								<g:else>Linear</g:else></g:if>
								</span>
								<span id="calcMethodInput_${dashboardStep.step.id }" title="input">
									<g:select from="${['L', 'M']}" valueMessagePrefix="step.calcMethod" name="calcMethod_${dashboardStep.step.id }"
									value="${dashboardStep.moveBundleStep?.calcMethod}" onchange="showTaskCompleted(this.value, ${dashboardStep.step.id })"/>
								</span>
								</td>
							</g:if>
							<g:else>
								<td>
								<input type="checkbox" name="checkbox_${dashboardStep.step.id }" id="checkbox_${dashboardStep.step.id }" 
									onclick="enableInput(${dashboardStep.step.id })"/>
								</td>
								<td>
									<span id="labelText_${dashboardStep.step.id }" title="text">${dashboardStep.moveBundleStep?.label}</span>
									<span id="labelInput_${dashboardStep.step.id }" style="display: none;" title="input">
									<input type="text" name="dashboardLabel_${dashboardStep.step.id }" id="dashboardLabel_${dashboardStep.step.id }" 
									value="${dashboardStep.moveBundleStep?.label ? dashboardStep.moveBundleStep?.label : dashboardStep.step.name}"/>
									</span>
								</td>
								<td>
									<span id="startTimeText_${dashboardStep.step.id }" title="text">
										<tds:convertDateTime date="${dashboardStep.moveBundleStep?.planStartTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
									</span>
									
									<span id="startTimeInput_${dashboardStep.step.id }" style="display: none;" title="input">
					                  <script type="text/javascript">
					                    $(document).ready(function(){
					                      $("#startTime_${dashboardStep.step.id }").datetimepicker();
					                    });
					                  </script>
										<input type="text" name="startTime_${dashboardStep.step.id }" id="startTime_${dashboardStep.step.id }"
										value="<tds:convertDateTime date='${dashboardStep.moveBundleStep?.planStartTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
										onchange="getTimeFormate(this.id, this.value, ${dashboardStep.step.id },'startTimeImg')"/>
									</span>
									<span id="startTimeImg_${dashboardStep.step.id }" style="display: none;">
										<img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}">
									</span>
									
								</td>
								<td>
									<span id="completionTimeText_${dashboardStep.step.id }" title="text">
										<tds:convertDateTime date="${dashboardStep.moveBundleStep?.planCompletionTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
									</span>
									
									<span id="completionTimeInput_${dashboardStep.step.id }" style="display: none;" title="input">
					                  <script type="text/javascript">
					                    $(document).ready(function(){
					                      $("#completionTime_${dashboardStep.step.id }").datetimepicker();
					                    });
					                  </script>
										<input type="text" name="completionTime_${dashboardStep.step.id }" id="completionTime_${dashboardStep.step.id }" 
										value="<tds:convertDateTime date='${dashboardStep.moveBundleStep?.planCompletionTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
										onchange="getTimeFormate(this.id, this.value, ${dashboardStep.step.id },'completionTimeImg')"/>
									</span>
									<span id="completionTimeImg_${dashboardStep.step.id }" style="display: none;"><img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}"></span>
								</td>
								<td>
								<span id="durationText_${dashboardStep.step.id }" title="text">
								<tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/> </span>
								
								<span id="durationInput_${dashboardStep.step.id }" style="display: none;" title="input">
								<input type="hidden" name="duration_${dashboardStep.step.id }" id="durationIn_${dashboardStep.step.id }"
									value="${dashboardStep.stepSnapshot?.duration}"/>
									<input type="text" id="duration_${dashboardStep.step.id }"	style="width: 60px;"
									value="<tds:formatIntoHHMMSS value="${dashboardStep.stepSnapshot?.duration}"/>"	onchange="changeCompletionTime(this.value, ${dashboardStep.step.id })"/>
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
							</g:else>	
							<td>
								<span id="tasksCompletedText_${dashboardStep.step.id }" title="text">${dashboardStep.stepSnapshot?.tasksCompleted}
								</span>
								
								<span style="display: none;" id="tasksCompletedInput_${dashboardStep.step.id }" title="input">
									<input type="text" name="tasksCompleted_${dashboardStep.step.id }" style="width: 25px;" value="${dashboardStep.stepSnapshot?.tasksCompleted}" 
									id="tasksCompleted_${dashboardStep.step.id }" maxlength="3" />
								</span><g:if  test="${dashboardStep.moveBundleStep?.calcMethod == 'M'}">%</g:if>
							</td>
							
					</tr>
				</g:each>
			</tbody>
		</table>
		
	</div>
	<div>
          <span style="float: right;">Dashboard Server : <input type="button" name="serverOn" value="On" />&nbsp;<input type="button" name="serverOff" value="Off" /></span>
      </div>
	</div>
	</g:form>
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
			$("input[type='checkbox']").each(function(){ 
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
			if($("#startTime_"+stepId ).hasClass(".field_error")){
				$("#startTimeImg_"+stepId ).show();
			}
			$("#completionTimeText_"+stepId ).hide();
			$("#completionTimeInput_"+stepId ).show();
			if($("#completionTime_"+stepId ).hasClass(".field_error")){
				$("#completionTimeImg_"+stepId ).show();
			}
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
			$("#startTime_"+stepId ).val("");
			$("#startTime_"+stepId ).removeClass("field_error");
			$("#startTimeImg_"+stepId ).hide();
			$("#completionTimeText_"+stepId ).show();
			$("#completionTimeInput_"+stepId ).hide();
			$("#completionTime_"+stepId ).val("");
			$("#completionTime_"+stepId ).removeClass("field_error");
			$("#completionTimeImg_"+stepId ).hide();
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
        var bundleStart = $("#startTime").val();
        var bundleCompletion = $("#completionTime").val();
        if(bundleStart && bundleCompletion){
        	var start = $("#startTime_"+stepId).val();
            var completion = $("#completionTime_"+stepId).val();
            var startTime
            var completionTime
            var bundleStartTime = new Date( bundleStart ).getTime()
            var bundleCompletionTime = new Date( bundleCompletion ).getTime()
            if(start){
            	startTime = new Date( start ).getTime();
            	if(bundleStartTime > startTime || bundleCompletionTime < startTime){
            		<%--alert("Step Start Time should be in between Bundle Start Time and Completion Time.");
    				 $("#startTime_"+stepId).val(""); --%>
    				$("#startTime_"+stepId).addClass("field_error");
    				$("#startTimeImg_"+stepId).attr("title","Step Start Time should be in between Bundle Start Time and Completion Time.")
    				$("#startTimeImg_"+stepId).show()
    				//return;
            	} else {
            		if( objRegExp.test( start ) ){
	            		$("#startTime_"+stepId).removeClass("field_error");
	    				$("#startTimeImg_"+stepId).hide()
            		}
            	}
            }
            if(completion){
            	completionTime = new Date( completion ).getTime();
            	if(bundleStartTime > completionTime || bundleCompletionTime < completionTime){
            		<%-- alert("Step Completion Time should be in between Bundle Start Time and Completion Time.");
    				$("#completionTime_"+stepId).val(""); --%>
    				$("#completionTime_"+stepId).addClass("field_error");
    				$("#completionTimeImg_"+stepId).attr("title","Step Completion Time should be in between Bundle Start Time and Completion Time.")
    				$("#completionTimeImg_"+stepId).show();
    				return;
            	} else {
            		if( objRegExp.test( completion ) ){
	            		$("#completionTime_"+stepId).removeClass("field_error");
	    				$("#completionTimeImg_"+stepId).hide()
            		}
            	}
            }
        	
        	
	        if(completion && start){
	        	var duration
	            if(completionTime > startTime){
	            	duration = completionTime - startTime
	            	$("#completionTime_"+stepId).removeClass("field_error");
	                $("#completionTimeImg_"+stepId).hide();    
	            } else {
	                //alert("Completion Time should be greater than Start Time");
	                $("#completionTime_"+stepId).addClass("field_error");
	                $("#completionTimeImg_"+stepId).attr("title","Completion Time should be greater than Start Time.")
	                $("#completionTimeImg_"+stepId).show();
	            	return;
	            }
	            if(duration){
	            	$("#duration_"+stepId).val(convertIntoHHMM(duration / 1000))
	            	$("#durationIn_"+stepId).val(duration / 1000)
	            	$("#duration_"+stepId).focus()
	            }
	        }
        } else {
        	alert("Bundle Start Time and Completion Time should not be blank");
			return;
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
        var timeArr = time.split(":")
        var hours = timeArr[0]
        var min = timeArr[1]
        if(hours  && min){
            var ms = (3600000 * hours ) + (60000 * min)
        }
        var startTimeString = $("#startTime_"+stepId).val()
        if(startTimeString){
	        var completionTime = new Date( startTimeString )
    	    var updatedTime = new Date(completionTime.getTime() + ms)
	        $("#durationIn_"+stepId).val(ms / 1000)
    		$("#completionTime_"+stepId).val( convertDate( updatedTime ))
        }
    }
    function getTimeFormate( objId, dateString, stepId , imgId)
	{
    	if(dateString && isValidDate(dateString, objId, imgId+"_"+stepId)){
	    	//var date= new Date(dateString)
			//$("#"+objId).val(convertDate( date ))
		   	calculateDuration(stepId);
    	}
	   
	}
	function convertDate( date ){
		var timeString = ""
		var month =  date.getMonth();
			
		if( !isNaN(month) ){
			month = month + 1
			var monthday    = date.getDate();
			var year        = date.getFullYear();
			
			var hour   = date.getHours();
			var minute = date.getMinutes();
			var second = date.getSeconds();
			if(month < 10 ){ month = "0"+ month }
			if(monthday < 10 ){ monthday = "0"+ monthday }
			var ap = "AM";
			if (hour   > 11) { ap = "PM";             }
			if (hour   > 12) { hour = hour - 12;      }
			if (hour   == 0) { hour = 12;             }
			if (hour   < 10) { hour   = "0" + hour;   }
			if (minute < 10) { minute = "0" + minute; }
			if (second < 10) { second = "0" + second; }
			
			var timeString = month+"/"+monthday+"/"+year+" "+hour + ':' + minute + ' ' + ap;
		}
		return timeString
	}
	var objRegExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
    function validateStepsData(){
        var checked = true
        var keyOffStep = ""
		var message = ""
        var uncheckedSteps = new Array()
    	$("input[type='checkbox']").each(function(){
    		  var id = $(this).attr("id")
	    	  if($(this).is(':checked') ){
		    	  var stepId = id.substring(9,id.length)
		    	  var dashboardLabel = $("#dashboardLabel_"+stepId).val()
		    	  var startTime = $("#startTime_"+stepId).val();
		    	  var completionTime = $("#completionTime_"+stepId).val()
		    	  if( !$("#dashboardLabel_"+stepId).val() || !startTime || !completionTime ){
				  	  checked =  false;
				      message ="Dashboard Label, Start & Completion times are mandatory for selected key off steps";
					  return false;
		    	  } else if( !objRegExp.test( startTime ) || !objRegExp.test( completionTime ) ) {
				      checked =  false;
				      message = "Dates should be in 'mm/dd/yyyy hh:mm' format. Please correct highlighted fields before saving.";
			    	  return false;
		    	  } else if( $(".field_error").length > 0 ){
				      checked =  false;
				      message = "Date/Time entry problem. Please correct highlighted fields before saving.";
			    	  return false;
		    	  }
	    	  } else {
	    		  uncheckedSteps.push(id.substring(id.indexOf("_")+1,id.length));
	    	  }
    	});
    	
    	if( !checked ){
    		alert(message);
	    	return checked;
    	} else {
    		return checkForProgressSteps(uncheckedSteps);
    	}
    }
    function checkForProgressSteps(uncheckedSteps){
    	var moveBundle = $("#moveBundleId").val()
    	var checked = true
    	$.ajax({
    		  url: 'checkStepSnapshotRecord',
    		  data: "moveBundleId="+moveBundle+"&steps="+uncheckedSteps,
    		  async: false,
    		  success: function(data) {
    		    if(data == "failure"){
        		    if( !confirm("You have chosen to not track dashboard status for some steps already in progress") ){
        		    	checked =  false;
        		    } 
    		    }
    		 }
    	});
    	if( !checked ){
    		return checked;
    	} else {
    		return validateDates();
    	}
    }
    function isValidDate( date , objId, imgObj){
        var returnVal = true;
      	if( date && !objRegExp.test(date) ){
          	//alert("Date should be in 'mm/dd/yyyy HH:MM AM/PM' format");
          	$("#"+objId).addClass("field_error");
          	$("#"+imgObj).attr("title","Date should be in 'mm/dd/yyyy HH:MM AM/PM' format")
          	$("#"+imgObj).show();
          	returnVal  =  false;
      	} else {
      		$("#"+objId).removeClass("field_error");
      		$("#"+imgObj).hide();
      	}
      	return returnVal;
	}
    function validateDates(){
    	var returnval = false
        var startTime = $("#startTime").val();
        var completionTime = $("#completionTime").val();
        if(isValidDate(startTime,"startTime","startTimeImg") && isValidDate(completionTime, "completionTime", "completionTimeImg")){
        	returnval = true;
		} 
		return returnval;
	}
	
	</script>
  </body>
</html>
