

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="moveBundleHeader" />
    <title>Edit MoveBundle</title>

	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" type="text/css"/>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery-ui.css')}" type="text/css"/>
	<g:javascript src="jquery/jquery.ui.widget.js"/>
	<g:javascript src="jquery/jquery.ui.mouse.js"/>
	<g:javascript src="jquery/jquery.ui.slider.js"/>

	
    <script type="text/javascript">
	     var maxDuration = calculateMaxDuration()
	     function initialize(){
	     	// This is called when the page loads to initialize Managers
	     	$('#moveManagerId').val('${moveManager}');
	      	$('#projectManagerId').val('${projectManager}');
	     }
	    /*
	    	function to calculate the bundle duration to adjust the slider
	    */ 
		function calculateMaxDuration(){
			var start = ${moveBundleInstance?.startTime?.getTime()}
				
			var completion = ${moveBundleInstance?.completionTime?.getTime()}
			var startTime = 0
			var completionTime = 300000
			if(start){
				startTime = new Date( start ).getTime()
			}
			if(completion){
				completionTime = new Date( completion ).getTime()
			}
			var durationInMinutes = (completionTime - startTime ) / 60000
			return durationInMinutes  
		} 
		/*
		convert the date into HH:MM ap formate to show at slider place.
		*/
		function convertToTimeFormat( date ){
	    	var timeString = ""
	    	var month =  date.getMonth();
	    	if( !isNaN(month) ){
	    		//month = month + 1
				//var monthday    = date.getDate();

				var hour   = date.getHours();
				var minute = date.getMinutes();
				var second = date.getSeconds();
				//if(month < 10 ){ month = "0"+ month }
				//if(monthday < 10 ){ monthday = "0"+ monthday }
	    		
	    		var ap = "AM";
	    		if (hour   > 11) { ap = "PM";             }
	    		if (hour   > 12) { hour = hour - 12;      }
	    		if (hour   == 0) { hour = 12;             }
	    		if (hour   < 10) { hour   = "0" + hour;   }
	    		if (minute < 10) { minute = "0" + minute; }
	    		if (second < 10) { second = "0" + second; }
	    			
	    		var timeString = hour + ':' + minute + ' ' + ap;
	    	}
	    	return timeString
		}
    </script>
  </head>
  <body>
  	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px;width: 219px; margin:9px 14px 0px">
			<span class="menuButton"><g:link class="list" action="list">Bundle List</g:link></span>
		    <jsec:hasRole name="ADMIN">
		    	<span class="menuButton"><g:link class="create" action="create" params="[projectId:projectId]">New Bundle</g:link></span>
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
          <span class="button"><g:actionSubmit class="save" value="Update" onclick="return validateStepsData()"/></span>
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
					<!--<th>Start</th>
					<th>Completion</th>
					<th>Duration</th> -->
					<th>Start - Completion</th>
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
									<div id="labelText_${dashboardStep.step.id }" title="text" style="display: none;" >${dashboardStep.moveBundleStep?.label}</div>
									<div id="labelInput_${dashboardStep.step.id }" title="input">
									<input type="text" name="dashboardLabel_${dashboardStep.step.id }" id="dashboardLabel_${dashboardStep.step.id }" 
									value="${dashboardStep.moveBundleStep?.label ? dashboardStep.moveBundleStep?.label : dashboardStep.step.name}"/>
									</div>
								</td>
								<td>
								<div id="startTimeInput_${dashboardStep.step.id }" title="input">
									<table>
										<tr><td style="padding: 0 6px;width: 300px;">
										<input type="hidden" name="startTime_${dashboardStep.step.id }" id="startTime_${dashboardStep.step.id }"
											value="<tds:convertDateTime date='${dashboardStep.moveBundleStep?.planStartTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"/>
										<input type="hidden" name="completionTime_${dashboardStep.step.id }" id="completionTime_${dashboardStep.step.id }"
											value="<tds:convertDateTime date='${dashboardStep.moveBundleStep?.planCompletionTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"/>
										<div id="slider_${dashboardStep.step.id }"></div>
										<script type="text/javascript">
											$(function() {
												$("#slider_${dashboardStep.step.id }").slider({
													range: true,
													min: 0,
													max: maxDuration,
													step:5,
													values: [getDuration($("#startTime_${dashboardStep.step.id }").val()),
													         getDuration($("#completionTime_${dashboardStep.step.id }").val())],
													slide: function(event, ui) {
														$("#show_${dashboardStep.step.id }").html( showSliderInput("start", "${dashboardStep.step.id }", ui.values[0])+" - "+showSliderInput("completion","${dashboardStep.step.id }", ui.values[1]));
													}
												});
											});
										</script>
										</td>
										<td id="show_${dashboardStep.step.id }" style="text-align: center;padding: 0 6px" nowrap="nowrap">
											<script type="text/javascript">
												$("#show_${dashboardStep.step.id }").html(convertToTimeFormat( new Date( $("#startTime_${dashboardStep.step.id }").val() ) )+" - "+convertToTimeFormat(new Date( $("#completionTime_${dashboardStep.step.id }").val() ) ))
											</script>
										</td>
										</tr>
									</table>
								</div>
								</td>
								<td>
									<div id="calcMethodText_${dashboardStep.step.id }" title="text" style="display: none;" >
									<g:if test="${dashboardStep.moveBundleStep}"><g:if test="${dashboardStep.moveBundleStep?.calcMethod != 'L'}">Manual</g:if>
									<g:else>Linear</g:else></g:if>
									</div>
									<div id="calcMethodInput_${dashboardStep.step.id }" title="input">
										<g:select from="${['L', 'M']}" valueMessagePrefix="step.calcMethod" name="calcMethod_${dashboardStep.step.id }"
										value="${dashboardStep.moveBundleStep?.calcMethod}" onchange="showTaskCompleted(this.value, ${dashboardStep.step.id })"/>
									</div>
								</td>
								<td>
									<g:if  test="${dashboardStep.moveBundleStep?.calcMethod == 'M'}">
										<div id="tasksCompletedInput_${dashboardStep.step.id }" title="input">
											<input type="text" name="tasksCompleted_${dashboardStep.step.id }" style="width: 25px;" value="${dashboardStep.stepSnapshot?.tasksCompleted}" 
											id="tasksCompleted_${dashboardStep.step.id }" maxlength="3"  onchange="validateManulaValue(this.value, ${dashboardStep.step.id })"/>%
										</div>
										<div id="stepValueImg_${dashboardStep.step.id }" style="display: none;" title="">
											<img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}">
										</div>
									</g:if>
									<g:else>
										<div id="tasksCompletedInput_${dashboardStep.step.id }" title="input" style="display: none;">
											<input type="text" name="tasksCompleted_${dashboardStep.step.id }" style="width: 25px;" value="" 
											id="tasksCompleted_${dashboardStep.step.id }" maxlength="3"  onchange="validateManulaValue(this.value, ${dashboardStep.step.id })"/>%
										</div>
										<div id="stepValueImg_${dashboardStep.step.id }" style="display: none;" title="">
											<img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}">
										</div>
									</g:else>
								</td>
							</g:if>
							<g:else>
								<td>
								<input type="checkbox" name="checkbox_${dashboardStep.step.id }" id="checkbox_${dashboardStep.step.id }" 
									onclick="enableInput(${dashboardStep.step.id })"/>
								</td>
								<td>
									<div id="labelText_${dashboardStep.step.id }" title="text">${dashboardStep.moveBundleStep?.label}</div>
									<div id="labelInput_${dashboardStep.step.id }" style="display: none;" title="input">
									<input type="text" name="dashboardLabel_${dashboardStep.step.id }" id="dashboardLabel_${dashboardStep.step.id }" 
									value="${dashboardStep.moveBundleStep?.label ? dashboardStep.moveBundleStep?.label : dashboardStep.step.name}"/>
									</div>
								</td>
								<td>
									<div id="startTimeInput_${dashboardStep.step.id }" style="display: none;" title="input">
					                  <table>
										<tr><td style="padding: 0 6px;width: 300px;">
										<input type="hidden" name="startTime_${dashboardStep.step.id }" id="startTime_${dashboardStep.step.id }" 
											value="<tds:convertDateTime date='${moveBundleInstance?.startTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"/>
										<input type="hidden" name="completionTime_${dashboardStep.step.id }" id="completionTime_${dashboardStep.step.id }" 
											value="<tds:convertDateTime date='${moveBundleInstance?.completionTime}' formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"/>
										<div id="slider_${dashboardStep.step.id }"></div>
										<script type="text/javascript">
											$(function() {
												$("#slider_${dashboardStep.step.id }").slider({
													range: true,
													min: 0,
													max: maxDuration,
													step:5,
													values: [0, maxDuration],
													slide: function(event, ui) {
														$("#show_${dashboardStep.step.id }").html( showSliderInput("start", "${dashboardStep.step.id }", ui.values[0])+" - "+showSliderInput("completion","${dashboardStep.step.id }", ui.values[1]));
													}
												});
											});
										</script>
										</td>
										<td id="show_${dashboardStep.step.id }" style="text-align: center;padding: 0 6px" nowrap="nowrap"></td>
										</tr>
										<tr><td></td> </tr>
									</table>
									</div>
								</td>
								<td>
								<div id="calcMethodText_${dashboardStep.step.id }" title="text">
								<g:if test="${dashboardStep.moveBundleStep}"><g:if test="${dashboardStep.moveBundleStep?.calcMethod != 'L'}">Manual</g:if>
								<g:else>Linear</g:else></g:if>
								</div>
								<div id="calcMethodInput_${dashboardStep.step.id }" style="display: none;" title="input">
									<g:select from="${['L', 'M']}" valueMessagePrefix="step.calcMethod" name="calcMethod_${dashboardStep.step.id }"
									value="${dashboardStep.moveBundleStep?.calcMethod}" onchange="showTaskCompleted(this.value, ${dashboardStep.step.id })"/>
								</div>
								</td>
								<td>
									<div style="display: none;" id="tasksCompletedInput_${dashboardStep.step.id }" title="input">
										<input type="text" name="tasksCompleted_${dashboardStep.step.id }" style="width: 25px;" value="${dashboardStep.stepSnapshot?.tasksCompleted}" 
											id="tasksCompleted_${dashboardStep.step.id }" maxlength="3" onchange="validateManulaValue(this.value, ${dashboardStep.step.id })" />%
									</div>
									<div id="stepValueImg_${dashboardStep.step.id }" style="display: none;" title="">
										<img src="${createLinkTo(dir:'images/skin',file:'exclamation.png')}">
									</div>
								</td>								
							</g:else>	
					</tr>
				</g:each>
			</tbody>
		</table>
		
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
	/*
	Will enable the step input fields when user select the checkbox.
	*/
	function enableInput( stepId){
		if($("#checkbox_"+stepId ).is(':checked')){
			$("#labelText_"+stepId ).hide();
			$("#labelInput_"+stepId ).show();
			$("#startTimeInput_"+stepId ).show();
			
			$("#startTime_"+stepId ).val($("#startTime").val())
			$("#completionTime_"+stepId ).val($("#completionTime").val())
			$("#show_"+stepId ).html(convertToTimeFormat( new Date( $("#startTime").val() ) )+" - "+convertToTimeFormat(new Date( $("#completionTime").val() ) ))

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
			$("#startTimeInput_"+stepId ).hide();
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
    /*
    	Change the minuits into HH:MM ap formate
    */
    function showSliderInput( type, stepId, minuits ){
        var date
        if(type =="start"){
        	var startTime = new Date($("#startTime").val())
        	date = convertToTimeFormat(new Date(startTime.getTime() + minuits * 60000 ))
        	$("#startTime_"+stepId).val(convertDate(new Date(startTime.getTime() + minuits * 60000 )))
        } else {
        	var completionTime = new Date($("#completionTime").val())
        	minuits = minuits - maxDuration
        	date = convertToTimeFormat(new Date(completionTime.getTime() + minuits * 60000 ))
        	$("#completionTime_"+stepId).val(convertDate(new Date(completionTime.getTime() + minuits * 60000 )))
        }
        return date
    }
	function getDuration( dateString ){
		var dateTime = new Date(dateString).getTime()
		var startTime = new Date($("#startTime").val()).getTime()
		var duration = (dateTime - startTime) / 60000
		return duration 
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
        var bundleStart = $("#startTime").val()
        var bundleStartTime = bundleStart ? new Date(bundleStart).getTime() : new Date().getTime()
                	
        var bundleCompletion = $("#completionTime").val()
        var bundleCompletionTime = bundleCompletion ? new Date(bundleCompletion).getTime() : new Date().getTime()
                	
    	$("input[type='checkbox']").each(function(){
    		  var id = $(this).attr("id")
	    	  if($(this).is(':checked') ){
		    	  var stepId = id.substring(9,id.length)
		    	  var dashboardLabel = $("#dashboardLabel_"+stepId).val()
		    	  var startTime = new Date($("#startTime_"+stepId).val()).getTime();
		    	  var completionTime = new Date($("#completionTime_"+stepId).val()).getTime()
		    	  // any step start/completion time that is earlier/later than the bundle start/completion should be changed to the bundle start/completion
		    	  if( startTime < bundleStartTime ){
		    		  $("#startTime_"+stepId).val(bundleStart)
	  	    	  } else if( completionTime > bundleCompletionTime){
	  	    		  $("#completionTime_"+stepId).val(bundleCompletion)
	  	    	  }
	  	    	  
		    	  if( !$("#dashboardLabel_"+stepId).val()){
				  	  checked =  false;
				      message ="Dashboard Labels are mandatory for selected key off steps";
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
      	} else if(date){
      		$("#"+objId).removeClass("field_error");
      		$("#"+imgObj).hide();
      		var objDateinMs =  new Date(date).getTime()
      		var alertMess
      		 maxDuration = calculateMaxDuration()
      		$("input[type='checkbox']").each(function(){
  	    	  if($(this).is(':checked') ){
  	    		  var id = $(this).attr("id")
  		    	  var stepId = id.substring(9,id.length)
  		    	  var dateTime = new Date($("#"+objId+"_"+stepId).val()).getTime();
  		    	  if(objId =="startTime" && dateTime < objDateinMs){
  		    		$("#show_"+stepId).html( showSliderInput("start", stepId, 0)+" - "+showSliderInput("completion",stepId, maxDuration));
  		    		alertMess = "Step start times will be changed to the bundle start"
  	    	  	  } else if(objId =="completionTime" && dateTime > objDateinMs){
  	    	  		$("#show_"+stepId).html( showSliderInput("start", stepId, 0)+" - "+showSliderInput("completion",stepId, maxDuration));
  	    	  		alertMess = "Step completion times will be changed to the bundle completion"
  	    	  	  }
  		    	$("#slider_"+stepId).slider( "option", "max", maxDuration );
  	    	  }
  	    	});
      		if(alertMess){
          		alert(alertMess)
      		}
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
 // validate the dial manual input valueinput value
	function validateManulaValue( value, stepId ){
		if( isNaN(value) ){
			$("#tasksCompleted_"+stepId).addClass("field_error");
			$("#stepValueImg_"+stepId).attr("title","Manula step value should be Alpha Numeric.")
			$("#stepValueImg_"+stepId).show();
		} else if(value > 100) {
			$("#tasksCompleted_"+stepId).addClass("field_error");
			$("#stepValueImg_"+stepId).attr("title","Manula step value should not be greater than 100.")
			$("#stepValueImg_"+stepId).show();
		} else {
			$("#tasksCompleted_"+stepId).removeClass("field_error");
			$("#stepValueImg_"+stepId).hide();
		}
		
		return check
	}
	</script>
  </body>
</html>
