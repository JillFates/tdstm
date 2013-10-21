<div class="menu4">
	<ul>
		<li><a href="#" id="generalShowHeadId" class="mobmenu mobselect"
			onclick="switchTab(${person.id},'generalInfoShowId','generalShowHeadId')">General</a></li>
		<li><a href="#" id="availShowHeadId" class="mobmenu"
			onclick="switchTab(${person.id},'availabilityShowId','availShowHeadId')">Availability</a></li>
		<tds:hasPermission permission='EditTDSPerson'>
			<li><a href="#" id="tdsShowHeadId" class="mobmenu"
				onclick="switchTab(${person.id},'tdsUtilityShowId','tdsShowHeadId')">TDS</a></li>
		</tds:hasPermission>
	</ul>
</div>
<div id="generalInfoShowId" class="person" >
	<g:form name="personDialogForm" action="updatePerson">
		<div class="dialog">
			<input type="hidden" name="id" value="${person.id}">
			<div>
				<table class="personTable">
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><label for="firstName">First
										Name:&nbsp;
								</label></td>
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
							<td valign="top" class="name"><label for="middleName">Middle
									Name:</label></td>
							<td valign="top" class="value" colspan="2" width="50%"><span
								class="personShow" id="middleNameId"> ${person.middleName}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="lastName">Last
									Name:</label></td>
							<td valign="top" class="value" colspan="2" width="50%"><span
								class="personShow" id="lastNameId"> ${person.lastName}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="company">Company:</label>
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
							<td valign="top" class="name"><label for="email">Email:</label>
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
							<td valign="top" class="name"><label for="nickName">City/State/Zip
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
							<td valign="top" class="name"><label for="active">Active
									:</label></td>
							<td valign="top" class="value" colspan="2"><span
								class="personShow" id="activeId">
									${person.active}
							</span></td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label>Team :</label></td>
							<td valign="top" class="value" colspan="2">
								<table style="border: 0px">
									<tbody id="funcsTbodyId">
										<g:each in="${personFunctions}" status="i" var="function">
											<tr id="funcTrId_${i}">
												<td><span class="personShow">
														${function.description.substring(function.description.lastIndexOf(':') +1).trim()}
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
		<table class="personTable">
			<tbody id="blackOutDay" >
				<tr>
					<td><span><b>Available , except for the following dates</b></span></td>
				</tr>
				<g:each in="${blackOutdays}" var="blackOutDay">
					<tr>
						<td><span class="personShow">
								<tds:convertDate date="${blackOutDay.exceptionDay}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
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
            <table class="personTable">
              <tbody class="personShow">
                <tr class="prop">
					<td valign="top" class="name personShow">
						<label for="keyWords">KeyWords :</label>
					</td>
					<td valign="top" class="value personShow" style="width: 40px" >
						<span id="keyWordsId" >${person.keyWords }</span >
					</td>
				</tr>

                <tr class="prop">
                  <td valign="top" class="name personShow">
                    <label for="tdsNote">TDS Note :</label>
                  </td>
                  <td valign="top" class="value personShow" colspan="2"  width="50%">
                    <span id="tdsNoteId" >${person.tdsNote}</span>
                  </td>
                </tr>
                
                <tr class="prop">
                  <td valign="top personShow" class="name">
                    <label for="tdsLink">TDS Link :</label>
                  </td>
                  <td valign="top" class="value personShow" colspan="2">
                    <span id="tdsLinkId" >${person.tdsLink}</span>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name personShow">
                    <label for="staffType ">StaffType :</label>
                  </td>
                  <td valign="top" class="value personShow" colspan="2">
                    <span id="staffTypeId" >${person.staffType}</span>
                  </td>
                </tr>
                 <tr class="prop">
                  <td valign="top" class="name personShow" >
                    <label for="travelOK ">TravelOK :</label>
                  </td>
                  <td valign="top" class="value personShow" colspan="2">
                    <span id="travelOK" ><input type="checkbox" ${person.travelOK == 1 ? 'checked="checked"':''} disabled="disabled"/> </span>
                  </td>
                </tr>
                
                
              </tbody>
            </table>
             </div>
             </div>
</div>
<tds:hasPermission permission='PersonEditView'>
<div class="buttons">
	<input class="edit" type="button" id="edtBId" value="Edit"
		onClick="loadPersonDiv(${person.id},'generalInfo','edit')" /> <input
		class="delete" type="button" id="cancelBId" value="Cancel"
		onClick="closePersonDiv('personGeneralViewId')"/>
</div>
 </tds:hasPermission>