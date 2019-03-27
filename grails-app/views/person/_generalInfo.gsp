<%@ page import="net.transitionmanager.domain.Person" %>
<%@page import="net.transitionmanager.security.Permission"%>
<g:form name="personDialogForm" action="updatePerson" autocomplete="off">
	<div id="generalInfoEditId" class="person">
		<input type="hidden" name="id" value="${person.id}" />
		<div class="dialog">
			<table class="personTable">
				<tbody>

					<tr class="prop">
						<td valign="top" class="name">
							<label>Company:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<span class="personShow" id="companyId">
								${company}
							</span>
						</td>
					</tr>


					<tr class="prop">
						<td valign="top" class="name">
							<label for="firstName"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
						</td>
						<td valign="top" class="value" style="width: 40px">
							<input type="text" maxlength="64" id="firstNameId" name="firstName" value="${person.firstName}" size="10" />
						</td>
						<td rowspan="2" style="position:absolute; left:60%">
							<g:if test="${!person.personImageURL}">
								<img src="${resource(dir:'images',file:'blankPerson.jpg')}" alt="Smiley face" height="60" width="60">
							</g:if>
							<g:else>
								<img src="${person.personImageURL}" onError="this.onerror=null;this.src='../../images/blankPerson.jpg'" height="60" width="60">
							</g:else>
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="middleName">Middle Name:</label>
						</td>
						<td valign="top" class="value" colspan="2" width="50%">
							<input type="text" maxlength="64" id="middleNameId" name="middleName" value="${person.middleName}" size="10" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="lastName">Last Name:</label>
						</td>
						<td valign="top" class="value" colspan="2" width="50%">
							<input type="text" maxlength="64" id="lastNameId" name="lastName" value="${person.lastName}" size="10" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="title">Title:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="text" maxlength="34" id="titleId" name="title" value="${person.title}" />
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="nickName">Email:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="text" maxlength="64" id="emailId" name="email" value="${person.email}" />
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="nickName">Image URL:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="text" id="personImageId" name="personImageURL" value="${person.personImageURL}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="nickName">Work Phone:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="text" maxlength="64" id="workPhoneId" name="workPhone" value="${person.workPhone}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="nickName">Mobile Phone:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="text" maxlength="64" id="mobilePhoneId" name="mobilePhone" value="${person.mobilePhone}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="nickName">City/State/Zip:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="text" maxlength="64" id="locationId" name="location" value="${person.location}" size="10" />
							<input type="text" maxlength="64" id="stateProvId" name="stateProv" value="${person.stateProv}" size="4" />
							<input type="text" maxlength="64" id="countryId" name="country" value="${person.country}" size="4" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="nickName">Active:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<g:select from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(Person).active.inList}" id="activeId" name="active" value="${person.active}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="keyWords">Keywords:</label>
						</td>
						<td valign="top" class="value" >
							<input type="text" maxlength="64" id="keyWordsId" name="keyWords" size="20" value="${person.keyWords}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="tdsNote">Comments:</label>
						</td>
						<td valign="top" class="value" colspan="2" >
							<input type="text" maxlength="64" id="tdsNoteId" name="tdsNote" value="${person.tdsNote}" size="20" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="tdsLink">Contact URL:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="text" id="tdsLinkId" name="tdsLink" size="20" value="${person.tdsLink}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="staffType">Staff Type:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<g:select id="staffTypeId" name="staffType" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(Person).staffType.inList}" value="${person.staffType}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label for="travelOK">Can Travel:</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<input type="checkbox" id="travleOKId" name="travelOK"
								   onclick="if(this.checked){this.value = 1} else {this.value = 0 }" ${person.travelOK == 1 ? 'checked="checked"' : 1 }
								   value="${person.travelOK}" />
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">
							<label>Team :</label>
						</td>
						<td valign="top" class="value" colspan="2">
							<table style="border: 0px">
								<tbody id="funcsTbodyId">
									<g:each in="${personFunctions}" status="i" var="function">
										<tr id="funcTrId_${i}">
											<td>
												<g:select from="${availabaleFunctions}" id="functionId"
													name="function" value="${function.id}" optionKey="id"
													optionValue="${{it.description}}"
													onChange="changeManageFuncs()" /> &nbsp;&nbsp;
												<a href="javascript:deleteFuncsRow('funcTrId_${i}')">
												<span class="clear_filter">X</span></a><br />
											</td>
										</tr>
									</g:each>
								</tbody>
							</table>
							<span style="cursor: pointer;" onclick="addFunctions()">
								<b>Add Team </b>
							</span>
						</td>
					</tr>
				</tbody>

			</table>
			<input type="hidden" id="maxSize" value="${sizeOfassigned }" />

			<input type="hidden" id="manageFuncsId" name="manageFuncs" value="0" />


			<div id="availableFuncsId" style="display: none">
				<g:select from="${availabaleFunctions}" id="functionId" name="funcToAdd"
					optionValue="${{it.description}}"
					value="" optionKey="id" />
			</div>
		</div>

	</div>
</g:form>
<tds:hasPermission permission="${Permission.PersonEdit}">
	<div class="footer_buttons">
		<button class="btn btn-default" role="button" onClick="Person.updatePerson('generalInfoShow','personDialogForm')"><span class="glyphicon  glyphicon-ok"></span> Update</button>
		<button class="btn btn-default" role="button" onClick="Person.closePersonDiv('personGeneralViewId');Person.showPersonDialog(${person.id}, 'generalInfoShow')"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</tds:hasPermission>
