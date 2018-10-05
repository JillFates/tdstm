<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Create Project</title>

	<asset:stylesheet href="css/ui.datepicker.css" />
	<script src="${resource(dir:'js',file:'project.js')}"></script>

	<script type="text/javascript">

		$(document).ready(function() {
			var now = new Date();
			if (!'${prevParam?.startDate}') {
				formatDate(now,'startDate');
				now.setDate(now.getDate() + 90) ;
				formatDate(now,'completionDate');
			}

			$("#timeZoneSelectPopup").dialog({ autoOpen: false });

			//appending the previous values.
			if ('${prevParam?.client?.id}') {
				$("#clientId").val('${prevParam?.client?.id}');
			}

			if ('${prevParam?.projectPartner}') {
				$("#projectPartnerId").val('${prevParam?.projectPartner}');
			}

			// Initialize company partners
			var companyPartners = []
			<g:each status="i" in="${partners}" var="partner">
			companyPartners.push({"id": ${partner.id}, "text": "${partner.name}" });
			</g:each>

			Project.loadCompanyPartners(companyPartners);
			Project.setActiveClientId("");
			Project.createStaffSelect("#projectManagerId", null);
			$("#clientId").select2({
				placeholder: "Please Select",
				width: "75%"
			});
			$("#workflowCode").select2({
				placeholder: "Please Select",
				width: "75%"
			});
			$("#projectType").select2({
				placeholder: "Please Select",
				width: "75%"
			});

            $("#completionDateId").kendoDatePicker({
					value: (($("#completionDateId") && $("#completionDateId").val() !== "")? new Date($("#completionDateId").val()) : new Date()),
					animation: false, format:tdsCommon.kendoDateFormat()
                });
		})

		function formatDate (dateValue,value) {
			var M = "" + (dateValue.getMonth()+1);
			var MM = "0" + M;
			MM = MM.substring(MM.length-2, MM.length);
			var D = "" + (dateValue.getDate());
			var DD = "0" + D;
			DD = DD.substring(DD.length-2, DD.length);
			var YYYY = "" + (dateValue.getFullYear());
			var currentDate = MM + "/" +DD + "/" + YYYY
			if (value == 'startDate') {
				$("#startDateId").val(currentDate);
			} else {
				$("#completionDateId").val(currentDate);
			}
		  }


		function setCompletionDate(startDate) {
			var completionDateObj = document.createProjectForm.completionDate;
			if (completionDateObj.value == "") {
				completionDateObj.value = startDate;
			}
		}
		function isValidDate( date ) {
			var returnVal = true;
			if ( date && !tdsCommon.isValidDate(date) ) {
				alert("Date should be in '" + tdsCommon.defaultDateFormat() + "' format");
				returnVal  =  false;
			}
			return returnVal;
		}
		function validateDates() {
			var returnval = false
			var startDateId = $("#startDateId").val();
			var completionDateId = $("#completionDateId").val();
			if (isValidDate(startDateId) && isValidDate(completionDateId)) {
				returnval = true;
			}
			return returnval;
		}

		function validateRequiredFields() {
		    // List of all the fields that are empty.
		    var emptyFields = [];
		    // Flag that everything went well.
			var allFieldsOK = true;
			// List of the ID of those required elements.
			var requiredFields = ["#name", "#projectCode" , "#clientId"];
			// The label for the required elements.
			var requiredLabels = ["Name", "Code", "Client"];
			// Iterate over the required fields. If they're empty, are the label to the list of empty fields.
		    for (var i = 0; i < requiredFields.length; i++) {
		    	if ($(requiredFields[i]).val().length === 0) {
		    	    emptyFields.push(requiredLabels[i]);
				}
			}

			// If any required field is empty, then false should be returned.
			allFieldsOK = emptyFields.size() === 0;
			if (!allFieldsOK) {
			    alert("The following field(s) cannot be null: " + emptyFields.join(", "));
			}
			return allFieldsOK;
		}

		function validateForm() {
			return validateRequiredFields() && validateDates() && Project.validSelectedPartners();
		}
	    </script>
	<r:layoutResources/>
</head>
<body>
	<tds:subHeader title="Create Project" crumbs="['Project','Create']"/>
	<div class="body">
		<!-- <h1>Create Project</h1> -->
		<br/>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<g:form action="save" method="post" name="createProjectForm" enctype="multipart/form-data">
			<div class="dialog">
				<table class="create-project-table">
					<tbody>
						<tr>
							<td colspan="4"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="client">Client:&nbsp;<span style="color: red">*</span></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'client','errors')}">
								<select id="clientId" name="client.id" tabindex="100"
								data-placeholder="Please select a client">
									<option value=""></option>
									<g:each in="${clients}" var="client" status="i">
									    <option value="${client.clientId}">${client.clientName}</option>
									</g:each>
								</select>
								<g:hasErrors bean="${projectInstance}" field="client">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="client" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="projectCode">Project Code:&nbsp;<span style="color: red">*</span></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'projectCode','errors')}">
								<input type="text" id="projectCode" name="projectCode" tabindex="110" maxlength="20" value="${fieldValue(bean:projectInstance,field:'projectCode')}" />
								<g:hasErrors bean="${projectInstance}" field="projectCode">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="projectCode" />
									</div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="name">Project Name:&nbsp;<span style="color: red">*</span></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'name','errors')}">
								<input type="text" id="name" name="name" tabindex="120" maxlength="64" value="${fieldValue(bean:projectInstance,field:'name')}" />
								<g:hasErrors bean="${projectInstance}" field="name">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="name" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="projectType">Project Type:&nbsp;<span style="color: red">*</span></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'projectType','errors')}">
								<g:select id="projectType" name="projectType" tabindex="130" from="${projectInstance.constraints.projectType.inList}" value="${projectInstance.projectType}"></g:select>
								<g:hasErrors bean="${projectInstance}" field="projectType">
									<div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="projectType" /></div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="description">Description:</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'description','errors')}">
								<textarea rows="3" cols="40" id="description" name="description"
									tabindex="140"
									placeholder="Enter a short description of the project"
									onkeydown="Project.textCounter(this.id,200);"
									onkeyup="Project.textCounter(this.id,200);">${fieldValue(bean:projectInstance,field:'description')}</textarea>
								<g:hasErrors bean="${projectInstance}" field="description">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="description" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="comment">Comment:</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'comment','errors')}">
								<textarea rows="3" cols="40" id="comment" name="comment"
									tabindex="150"
									onkeydown="Project.textCounter(this.id,200);"
									onkeyup="Project.textCounter(this.id,200);">${fieldValue(bean:projectInstance,field:'comment')}</textarea>
								<g:hasErrors bean="${projectInstance}" field="comment">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="comment" />
									</div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="startDate">Start Date:</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
								<script type="text/javascript" charset="utf-8">
									jQuery(function($){ $("#startDateId").kendoDatePicker({ value: new Date(), animation: false, format:tdsCommon.kendoDateFormat() }); });
								</script>
								<input type="text" class="dateRange" tabindex="160" size="15" style="width: 138px;" name="startDate" id="startDateId"
									value="<tds:convertDate date="${prevParam?.startDate?: projectInstance?.startDate}" />" onchange="setCompletionDate(this.value);isValidDate(this.value);" />
								<g:hasErrors bean="${projectInstance}" field="startDate">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="startDate" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="completionDate">
									Completion Date:&nbsp;<span style="color: red">*</span>
								</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
								<script type="text/javascript" charset="utf-8">

								</script>
								<input type="text" class="dateRange" tabindex="170" size="15" style="width: 138px;" id="completionDateId"
									name="completionDate" value="<tds:convertDate date="${prevParam?.completionDate?: projectInstance?.completionDate}" />" onchange="isValidDate(this.value);" />
								<g:hasErrors bean="${projectInstance}" field="completionDate">
									<div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="completionDate" /></div>
								</g:hasErrors>
							</td>
						</tr>

						<tr class="prop">
							<td class="name">
								<label for="projectPartner">Associated Partner(s):</label>
							</td>
							<td class="valueNW">
								<input type="button" value="Add Partner" tabindex="180" onclick="Project.addPartnerSelect('#partnersContainer');">
								<div class="create-project-partner-container" id="partnersContainer"></div>
							</td>
							<td class="name">
								<label for="client">Project Logo:</label>
							</td>
							<td class="valueNW">
								<input type="file" name="projectLogo" id="projectLogo" tabindex="190" />
								<br>
								<span class="footnote">Select a jpg or gif file smaller than 50KB to appear in header</span>
							</td>
						</tr>


						<tr class="prop">
							<td class="name"></td><td class="valueNW"></td>
							<td class="name">
								<label for="client">Default Bundle:</label>
							</td>
							<td class="valueNW">
								<input type="text" id="defaultBundle" name="defaultBundleName" tabindex="110" maxlength="60" value="TBD" />
							</td>
						</tr>


						<tr class="prop">
							<td class="name">
								<label for="projectManager">Project Manager:</label>
							</td>
							<td class="valueNW">
								<input type="text" id="projectManagerId" name="projectManagerId" tabindex="200">
							</td>
							<td class="name"><label for="timezone">Time Zone:</label></td>
							<td class="valueNW">
								<input type="text" id="timezone" name="timezone" value="${defaultTimeZone}" readonly style="width: 200px; padding-right: 20px">
								<input type="button" value="Change" onclick="Project.showTimeZoneSelect('timezone');" tabindex="210">
							</td>
						</tr>
						<tr class="prop">
							<td class="name"></td><td class="valueNW"></td>
							<td class="name">
								<label for="client">Collect Reporting Metrics:</label>
							</td>
							<td valign="top">
								<input type="checkbox" id="collectMetrics" name="collectMetrics" value="1" checked="checked"/>
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="workflowCode">Workflow:&nbsp;<span style="color: red">*</span></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'workflowCode','errors')}">
								<g:select id="workflowCode" name="workflowCode"
									from="${workflowCodes}"
									value="${projectInstance?.workflowCode}"
									noSelection="['STD_PROCESS':'STD_PROCESS']"
									tabindex="220">
								</g:select>
								<g:hasErrors bean="${projectInstance}" field="workflowCode">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="workflowCode" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="projectType">Plan Methodology:&nbsp;</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'planMethodology','errors')}">
								<g:if test="${planMethodologies}">
									<g:select id="planMethodology" name="planMethodology" indextab="130"
											  value="${projectInstance.planMethodology}"
											  from="${planMethodologies}"
											  optionKey="field" optionValue="label" />
								</g:if>
								<g:else>
									To be set after field specifications are defined
								</g:else>
								<g:hasErrors bean="${projectInstance}" field="planMethodology">
									<div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="planMethodology" /></div>
								</g:hasErrors>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="buttons">
				<span class="button">
					<input class="save" type="submit" value="Save" tabindex="300" onclick="return validateForm();" />
				</span>
				<span class="button">
					<input type="button" class="cancel" value="Cancel" tabindex="310" onclick="window.history.back()" />
				</span>
			</div>
		</g:form>
	</div>

     <%-- DIV for select time zone --%>
	<div id="timeZoneSelectPopup" style="display: none;min-width:250px;" title="Time Zone Select"></div>

	<g:javascript>
		$(window).load(function() {
			currentMenuId = "#projectMenu";
			$('.menu-projects-active-projects').addClass('active');
			$('.menu-parent-projects').addClass('active');

			// Disable the Enter Key from submitting the form (issue with the select2)
			$(window).keydown( function(event) {
				if(event.keyCode == 13) {
					event.preventDefault();
					return false;
				}
			});
		});
	</g:javascript>

	<r:layoutResources/>
</body>
</html>
