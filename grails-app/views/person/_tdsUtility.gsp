<div class="menu4">
		<ul>
			<li><a href="#" class="mobmenu " onclick="loadPersonDiv(${person.id},'generalInfo')">General</a></li>
			<li><a href="#" class="mobmenu" onclick="loadPersonDiv(${person.id},'availability')">Availability</a></li>
			<li><a href="#" class="mobmenu mobselect">TDS</a></li>
		</ul>
</div>
<g:form  name="personDialogForm"  action="updatePerson">
      <div class="dialog">
      <input type="hidden" name="id" value="${person.id}">
          <div class="dialog">
            <table>
              <tbody>
                <tr class="prop">
					<td valign="top" class="name">
						<label for="keyWords">KeyWords : </label>
					</td>
					<td valign="top" class="value" style="width: 40px" >
						<input type="text" maxlength="64" id="keyWordsId" name="keyWords" value="" />
					</td>
				</tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="tdsNote">TDS Note:</label>
                  </td>
                  <td valign="top" class="value" colspan="2"  width="50%">
                    <input type="text" maxlength="64" id="tdsNoteId" name="tdsNote" value="" size="10"/>
                  </td>
                </tr>
                
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="tdsLink">TDS Link</label>
                  </td>
                  <td valign="top" class="value" colspan="2">
                    <input type="text" id="tdsLinkId" name="tdsLink" value=""/>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="staffType ">StaffType :</label>
                  </td>
                  <td valign="top" class="value" colspan="2">
                    <g:select id="staffTypeId" name="staffType"  from="${['Contractor', 'Hourly', 'Salary']}" value ="Salary" />
                  </td>
                </tr>
                
                
              </tbody>
              
            </table>
             
          </div>
          </div>
          </g:form>
            <div class="buttons">
				<input class="save" type="button" id="updateBId" value="Update"  />
				
				<input class="save" type="button" id="cancelBId" value="Cancel" onClick="$('#personGeneralViewId').dialog('close')"/>
			</div>