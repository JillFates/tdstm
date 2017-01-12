<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Create Manufacturer</title>
	</head>
	<body>
		<div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${manufacturerInstance}">
				<div class="errors">
					<g:renderErrors bean="${manufacturerInstance}" as="list" />
				</div>
			</g:hasErrors>
			<g:form action="list" name="manufacturerDialogForm">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="name"><b>Name:&nbsp;<span style="color: red">*</span></b></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'name','errors')}">
									<input type="text" id="name" name="name" value="${fieldValue(bean:manufacturerInstance,field:'name')}"/>
								</td>
							</tr>
							
							<tr>
								<td valign="top" class="name">AKA:</td>
								<td>
									<table style="border:0px ;margin-left:-8px">
										<tbody id="addAkaTableId">
											<tr><td nowrap="nowrap">
											<input type="text" class="akaValidate" name="aka" id="akaId" value="" onchange="validateAKA(this.value,'','errSpan', 'manufacturer')"> <span style="cursor: pointer;" onclick="addAka()"><b>Add AKA</b></span>
											<br><div class="errors" style="display: none" id="errSpan"></div>
											
											</td></tr>
										</tbody>
									</table>
								</td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name">
									<label for="description">Description:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'description','errors')}">
									<input type="text" id="description" name="description" value="${fieldValue(bean:manufacturerInstance,field:'description')}"/>
								</td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name">
									<label for="corporateName">Corporate Name:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'corporateName','errors')}">
									<input type="text" id="corporateName" name="corporateName" value="${fieldValue(bean:manufacturerInstance,field:'corporateName')}"/>
								</td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name">
									<label for="corporateLocation">Corporate Location:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'corporateLocation','errors')}">
									<input type="text" id="corporateLocation" name="corporateLocation" value="${fieldValue(bean:manufacturerInstance,field:'corporateLocation')}"/>
								</td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name">
									<label for="website">Website:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:manufacturerInstance,field:'website','errors')}">
									<input type="text" id="website" name="website" value="${fieldValue(bean:manufacturerInstance,field:'website')}"/>
								</td>
							</tr>
							
						</tbody>
					</table>
				</div>
				
				<div class="buttons">
					<span class="button"><g:actionSubmit class="save" action="save" value="Save"></g:actionSubmit></span>
				</div>
			</g:form>
			<div id="akaDiv" style="display:none;"> 
				<input type="text" class="akaValidate" name="aka" id="akaId" value="" onchange="validateAKA(this.value,'', 'errSpan', 'manufacturer' )">
			</div>
			<input type="hidden" id="manageAkaId" value="-1" >
		</div>
	</body>
</html>