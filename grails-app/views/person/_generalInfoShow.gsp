<%@page import="net.transitionmanager.domain.Person" %>
<%@page import="net.transitionmanager.security.Permission"%>
<div id="generalInfoShowId" class="person" >
	<g:form name="personDialogForm" action="updatePerson">
		<div class="dialog">
			<input type="hidden" name="id" value="${person.id}" />
			<div>
				<table class="personTable">
					<tbody>

						<tr class="prop">
							<td valign="top" class="name"><label for="company">Company:</label></td>
							<td valign="top" class="value" colspan="2">
								<span class="personShow" id="companyId">${company}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="firstName">First Name:&nbsp;</label>
							</td>
							<td valign="top" class="value" style="width: 40px">
								<span class="personShow" id="firstNameId" class="asset_details_block_task">
									${person.firstName}
								</span>
							</td>
							<td rowspan="2">
								<g:if test="${!person.personImageURL}">
									<img src="${resource(dir:'images',file:'blankPerson.jpg')}" alt="Smiley face" height="60" width="60" />
								</g:if>
								<g:else>
									<img src="${person.personImageURL}" height="60" width="60" />
								</g:else>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="middleName">Middle Name:</label></td>
							<td valign="top" class="value" colspan="2" width="50%">
								<span class="personShow" id="middleNameId">${person.middleName}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="lastName">Last Name:</label></td>
							<td valign="top" class="value" colspan="2" width="50%">
								<span class="personShow" id="lastNameId">${person.lastName}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="title">Title:</label></td>
							<td valign="top" class="value" colspan="2">
								<span class="personShow" id="titleId">${person.title}</span>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="email">Email:</label></td>
							<td valign="top" class="value" colspan="2">
								<span class="personShow" id="emailId">${person.email}</span>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">Work Phone:</label></td>
							<td valign="top" class="value" colspan="2">
								<span class="personShow" id="workPhoneId">${person.workPhone}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">Mobile Phone:</label></td>
							<td valign="top" class="value" colspan="2">
								<span class="personShow" id="mobilePhoneId">${person.mobilePhone}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">City/State/Zip:</label></td>
							<td valign="top" class="value" colspan="2">
								<span class="personShow" id="locationId">${person.location}</span>
								<span class="personShow" id="stateProvId">${person.stateProv}</span>
								<span class="personShow" id="countryId">${person.country}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="active">Active:</label></td>
							<td valign="top" class="value" colspan="2">
								<span class="personShow" id="activeId">${person.active}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name personShow">
								<label for="keyWords">Keywords :</label>
							</td>
							<td valign="top" class="value personShow" style="width: 40px" >
								<span id="keyWordsId" >${person.keyWords }</span >
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name personShow">
								<label for="tdsNote">Comments :</label>
							</td>
							<td valign="top" class="value personShow" colspan="2"  width="50%">
								<span id="tdsNoteId" >${person.tdsNote}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top personShow" class="name personShow">
								<label for="tdsLink">Contact URL :</label>
							</td>
							<td valign="top" class="value personShow" colspan="2">
								<span id="tdsLinkId" >${person.tdsLink}</span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name personShow">
								<label for="staffType ">Staff Type :</label>
							</td>
							<td valign="top" class="value personShow" colspan="2">
								<span id="staffTypeId" >${person.staffType}</span>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name personShow" >
								<label for="travelOK ">Can Travel :</label>
							</td>
							<td valign="top" class="value personShow" colspan="2">
								<span id="travelOK" ><input type="checkbox" ${person.travelOK == 1 ? 'checked="checked"':''} disabled="disabled"/> </span>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label>Team :</label></td>
							<td valign="top" class="value" colspan="2">
								<table style="border: 0px">
									<tbody id="funcsTbodyId">
										<g:each in="${personFunctions}" status="i" var="function">
											<tr id="funcTrId_${i}">
												<td>
													<span class="personShow">
														${function.description.substring(function.description.lastIndexOf(':') +1).trim()}
													</span>
													<br />
												</td>
											</tr>
										</g:each>
									</tbody>
								</table>
							</td>
						</tr>
					</tbody>

				</table>
			</div>
		</div>
	</g:form>
</div>
<tds:hasPermission permission="${Permission.PersonEdit}">
<div class="footer_buttons">
	<button class="btn btn-default" role="button" onClick="Person.showPersonDialog(${person.id},'generalInfo','edit')"><span class="glyphicon glyphicon-pencil"></span> Edit</button>
	<button class="btn btn-default" role="button" onClick="Person.closePersonDiv('personGeneralViewId')"><span class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
</div>
 </tds:hasPermission>
