

<div class="menu4">
	<ul>
		<li><a href="#" id="generalShowHeadId" class="mobmenu mobselect"
			onclick="switchTab(${person.id},'generalInfoShowId','generalShowHeadId')">General</a></li>
		<li><a href="#" id="availShowHeadId" class="mobmenu"
			onclick="switchTab(${person.id},'availabilityShowId','availShowHeadId')">Availability</a></li>
		<li><a href="#" id="tdsShowHeadId" class="mobmenu"
			onclick="switchTab(${person.id},'tdsUtilityShowId','tdsShowHeadId')">TDS</a></li>
	</ul>
</div>
<div id="generalInfoShowId" class="person">
	<g:form name="personDialogForm" action="updatePerson">
		<div class="dialog">
			<input type="hidden" name="id" value="${person.id}">
			<div>
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><label for="firstName"><b>First
										Name:&nbsp;<span style="color: red">*</span>
								</b></label></td>
							<td valign="top" class="value" style="width: 40px"><span
								class="personShow" id="firstNameId"
								class="asset_details_block_task">
									${person.firstName}
							</span></td>
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
							<td valign="top" class="value" colspan="2" width="50%"><span
								class="personShow" id="lastNameId"> ${person.lastName}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="lastName">Company:</label>
							</td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="companyId">
									${company}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="title">Title:</label>
							</td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="titleId">
									${person.title}
							</span></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">Email:</label>
							</td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="emailId">
									${person.email}
							</span></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">Work
									Phone:</label></td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="workPhoneId">
									${person.workPhone}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">Mobile
									Phone:</label></td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="mobilePhoneId">
									${person.mobilePhone}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">Location
									:</label></td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="locationId">
									${person.location}
							</span> <span class="personShow" id="stateProvId">
									${person.stateProv}
							</span> <span class="personShow" id="countryId">
									${person.country}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="nickName">Active
									:</label></td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="activeId">
									${person.active}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label>Roles :</label></td>
							<td valign="top" class="value" colspan="2">
								<table style="border: 0px">
									<tbody id="rolesTbodyId">
										<g:each in="${rolesForPerson}" status="i" var="role">
											<tr id="roleTrId_${i}">
												<td><span class="personShow">
														${role.description.substring(role.description.lastIndexOf(':') +1).trim()}
												</span><br /></td>
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


<div id="availabilityShowId" class="person" style="display: none;">
	<div>
		<table style="border: 0px">
			<tbody id="blackOutDay" >
				<tr>
					<td><span><b>Available , except for the following dates</b></span></td>
				</tr>
				<g:each in="${blackOutdays}" var="blackOutDay">
					<tr>
						<td><span class="personShow">
							<tds:convertDate date='${blackOutDay.exceptionDay}' timeZone='${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}'/>
							</span>
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</div>

<div id="tdsUtilityShowId" class="person" style="display: none;">
	<div class="dialog">
          <div class="dialog">
            <table>
              <tbody>
                <tr class="prop">
					<td valign="top" class="name">
						<label for="keyWords">KeyWords : </label>
					</td>
					<td valign="top" class="value" style="width: 40px" >
						<span id="keyWordsId" >Cabling</span >
					</td>
				</tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="tdsNote">TDS Note:</label>
                  </td>
                  <td valign="top" class="value" colspan="2"  width="50%">
                    <span id="tdsNoteId" >Has a lot data center Exp.</span>
                  </td>
                </tr>
                
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="tdsLink">TDS Link</label>
                  </td>
                  <td valign="top" class="value" colspan="2">
                    <span id="tdsLinkId" >link</span>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="staffType ">StaffType :</label>
                  </td>
                  <td valign="top" class="value" colspan="2">
                    <span id="staffTypeId" >Contractor</span>
                  </td>
                </tr>
                
                
              </tbody>
            </table>
             </div>
             </div>
</div>
<div class="buttons">
	<input class="save" type="button" id="edtBId" value="Edit"
		onClick="loadPersonDiv(${person.id},'generalInfo')" /> <input
		class="save" type="button" id="cancelBId" value="Cancel"
		onClick="$('#personGeneralViewId').dialog('close')" />
</div>