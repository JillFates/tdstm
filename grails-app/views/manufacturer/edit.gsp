<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Edit Manufacturer</title>
	</head>
	<body>
		<div class="body">
			<g:hasErrors bean="${manufacturerInstance}">
				<div class="errors">
					<g:renderErrors bean="${manufacturerInstance}" as="list" />
				</div>
			</g:hasErrors>
			<g:if test="${flash.message}">
				<div class="errors">
					<div id="messageDivId" class="message">${flash.message}</div>
				</div>
			</g:if>
			<g:form method="post" action="update" name="editManufacturerFormId">
				<input type="hidden" id="manufacturerId" name="id" value="${manufacturerInstance?.id}" />
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
									<input type="text" id="name" name="name" onchange="akaUtil.handleAkaChange(this, 'manufacturer', '${manufacturerInstance?.id}')" value="${fieldValue(bean:manufacturerInstance,field:'name')}"/>
								</td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name">
									<label for="aka">AKA:</label>
								</td>
								<td valign="top">
									<table style="border: 0px;margin-left: -8px;">
										<tbody id="addAkaTableId">
										<g:each in="${manuAlias}" var="alias">
											<tr id="aka_${alias.id}" js-is-unique="true"><td nowrap="nowrap">
												<input type="text" class="akaValidate" id="aka_${alias.id}" name="aka_${alias.id}" value="${alias.name}" onchange="akaUtil.handleAkaChange(this, 'manufacturer', '${manufacturerInstance?.id}')"/>
												<a href="javascript:akaUtil.deleteAkaRow('aka_${alias.id}', true, 'manufacturer')"><span class='clear_filter'><u>X</u></span></a>
												<br><div class="errors" style="display: none" id="errSpan_${alias.id}"></div>
											</td></tr>
										</g:each>
										</tbody>
									</table>
									<span style="cursor: pointer;" onclick="akaUtil.addAka('manufacturer')"><b>Add AKA</b></span>
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
				<input type="hidden" name="deletedAka" id="deletedAka" />
				<div class="buttons" >
					<span class="button">
						<input type="button" class="save" value="Update" id="saveManufacturerId" onclick="updateManufacturer('Manufacturer')"/>
					</span>
					<span class="button">
						<g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" />
					</span>
					<span class="button">
						<input type="button" class="cancel" value="Cancel" id="cancelButtonId" onclick="showOrEditModelManuDetails('manufacturer',${manufacturerInstance?.id},'Manufacturer','show','Show')" />
					</span>
				</div>
			</g:form>
			<div id="akaTemplateDiv" style="display:none;"> 
				<input type="text" class="akaValidate" name="aka" id="akaId" value="" onchange="akaUtil.handleAkaChange(this, 'manufacturer', '${manufacturerInstance?.id}')"/>
			</div>
		</div>
		<script>
			currentMenuId = "#adminMenu";
			$('.menu-list-manufacturers').addClass('active');
			$('.menu-parent-admin').addClass('active');
			
		</script>
	</body>
</html>
