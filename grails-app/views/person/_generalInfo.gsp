

<div class="menu4">
	<ul>
		<li><a href="#" id="generalHeadId" class="mobmenu mobselect"
			onclick="switchTab(${person.id},'generalInfoId','generalHeadId')">General</a></li>
		<li><a href="#" id="availHeadId" class="mobmenu"
			onclick="switchTab(${person.id},'availabilityId','availHeadId')">Availability</a></li>
		<g:if test="${isProjMgr==true}">
			<li><a href="#" id="tdsHeadId" class="mobmenu"
				onclick="switchTab(${person.id},'tdsUtilityId','tdsHeadId')">TDS</a></li>
		</g:if>
	</ul>
</div>
<g:form name="personDialogForm" action="updatePerson">
	<div id="generalInfoId" class="person">
		<input type="hidden" name="id" value="${person.id}">
		<div class="dialog">
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label for="firstName"><b>First
									Name:&nbsp;<span style="color: red">*</span>
							</b></label></td>
						<td valign="top" class="value" style="width: 40px"><input
							type="text" maxlength="64" id="firstNameId" name="firstName"
							value="${person.firstName}" size="10" /></td>
						<td rowspan="2"><g:if test="${person.personImageURL==null}">
								<img src="../images/blankPerson.jpg" alt="Smiley face"
									height="60" width="60">
							</g:if> <g:else>
								<img src="${person.personImageURL}" height="60" width="60">
							</g:else></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="lastName">Last
								Name:</label></td>
						<td valign="top" class="value" colspan="2" width="50%"><input
							type="text" maxlength="64" id="lastNameId" name="lastName"
							value="${person.lastName}" size="10" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="lastName">Company:</label>
						</td>
						<td valign="top" class="value" colspan="2"><input type="text"
							maxlength="64" id="companyId" name="Company" value="${company}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="title">Title:</label>
						</td>
						<td valign="top" class="value" colspan="2"><input type="text"
							maxlength="34" id="titleId" name="title" value="${person.title}" />
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label for="nickName">Email:</label>
						</td>
						<td valign="top" class="value" colspan="2"><input type="text"
							maxlength="64" id="emailId" name="email" value="${person.email}" />
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label for="nickName">Image
								URL:</label></td>
						<td valign="top" class="value" colspan="2"><input type="text"
							id="personImageId" name="personImageURL"
							value="${person.personImageURL}" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="nickName">Work
								Phone:</label></td>
						<td valign="top" class="value" colspan="2"><input type="text"
							maxlength="64" id="workPhoneId" name="workPhone"
							value="${person.workPhone}" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="nickName">Mobile
								Phone:</label></td>
						<td valign="top" class="value" colspan="2"><input type="text"
							maxlength="64" id="mobilePhoneId" name="mobilePhone"
							value="${person.mobilePhone}" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="nickName">Location
								:</label></td>
						<td valign="top" class="value" colspan="2"><input type="text"
							maxlength="64" id="locationId" name="location"
							value="${person.location}" size="10" /> <input type="text"
							maxlength="64" id="stateProvId" name="stateProv"
							value="${person.stateProv}" size="4" /> <input type="text"
							maxlength="64" id="countryId" name="country"
							value="${person.country}" size="4" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="nickName">Active
								:</label></td>
						<td valign="top" class="value" colspan="2"><g:select
								from="${Person.constraints.active.inList}" id="activeId"
								name="active" value="${person.active}" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label>Roles :</label></td>
						<td valign="top" class="value" colspan="2">
							<table style="border: 0px">
								<tbody id="rolesTbodyId">
									<g:each in="${rolesForPerson}" status="i" var="role">
										<tr id="roleTrId_${i}">
											<td><g:select from="${availabaleRoles}" id="roleId"
													name="role" value="${role.id}" optionKey="id"
													optionValue="${{it.description.substring(it.description.lastIndexOf(':') +1).trim()}}"
													onChange="changeManageRole()" /> &nbsp;&nbsp; <a
												href="javascript:deleteRolesRow('roleTrId_${i}')"><span
													class="clear_filter">X</span></a><br /></td>
										</tr>
									</g:each>
								</tbody>
							</table> <span style="cursor: pointer;" onclick="addRoles()"><b>Add
									Roles </b></span>
						</td>
					</tr>
				</tbody>

			</table>
			<input type="hidden" id="maxSize" value="${sizeOfassigned }">

			<input type="hidden" id="manageRolesId" name="manageRoles" value="0">


			<div id="availableRolesId" style="display: none">
				<g:select from="${availabaleRoles}" id="roleId" name="roleToAdd"
					optionValue="${{it.description.substring(it.description.lastIndexOf(':') +1).trim()}}"
					value="" optionKey="id" />
			</div>
		</div>

	</div>

	<div id="availabilityId" class="person" style="display: none;">
		<div>
			<script type="text/javascript" charset="utf-8">
	jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
	
	function showCalender(id){
		jQuery(function($){$(id).datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
}
</script>
			<table style="border: 0px">
				<tbody id="blackOutDay">
					<tr>
						<td><span><b>Available , except for the following
									dates</b></span></td>
					</tr>
					<g:each in="${blackOutdays}" var="blackOutDay" status="i">
						<tr id="dateTrId_${i}">
							<td align="center"><input type="text" class="dateRange"
								size="15" style="width: 112px; height: 14px;"
								name="availability" id="availabilityId_${i}"
								value="<tds:convertDate date='${blackOutDay.exceptionDay}' timeZone='${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}'/>" />
								<a href="javascript:deleteRolesRow('dateTrId_${i}')">&nbsp;&nbsp;<span
									class='clear_filter'>X</span></a></td>
						</tr>
					</g:each>
				</tbody>
			</table>
			<br /> <span id="" onclick="addBlackOutDay()"
				style="cursor: pointer;"><b> Add Date </b></span> <input
				type="hidden" id="availableId" value="1">

			<div id="dateDivId" style="display: none">
				<input type="text" size="15" style="width: 112px; height: 14px;"
					name="available" id="availId" />
			</div>
		</div>
	</div>

	<div id="tdsUtilityId" style="display: none;" class="person">
		<div class="dialog">
			<div class="dialog">
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><label for="keyWords">KeyWords
									: </label></td>
							<td valign="top" class="value" style="width: 40px"><input
								type="text" maxlength="64" id="keyWordsId" name="keyWords"
								value="${person.keyWords}" /></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="tdsNote">TDS
									Note:</label></td>
							<td valign="top" class="value" colspan="2" width="50%"><input
								type="text" maxlength="64" id="tdsNoteId" name="tdsNote"
								value="${person.tdsNote}" size="10" /></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="tdsLink">TDS
									Link</label></td>
							<td valign="top" class="value" colspan="2"><input
								type="text" id="tdsLinkId" name="tdsLink"
								value="${person.tdsLink}" /></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="staffType ">StaffType
									:</label></td>
							<td valign="top" class="value" colspan="2"><g:select
									id="staffTypeId" name="staffType"
									from="${Person.constraints.staffType.inList}" value="Salary" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="travleOK ">TravleOK
									:</label></td>
							<td valign="top" class="value" colspan="2"><input
								type="checkbox" id="travleOKId" name="travelOK"
								onclick="if(this.checked){this.value = 1} else {this.value = 0 }" ${person.travelOK == 1 ? 'checked="checked"' : 1 }
								value="${person.travelOK}" /></td>
						</tr>


					</tbody>

				</table>

			</div>
		</div>
	</div>
</g:form>
<div class="buttons buttonsToUpdate">
	<input class="save" type="button" id="updateBId" value="Update"
		onClick="updatePerson('generalInfoShow','personDialogForm')" /> <input
		class="save" type="button" id="cancelBId" value="Cancel"
		onClick="$('#personGeneralViewId').dialog('close')" />
</div>

