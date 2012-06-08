<g:form method="post" action="update" name="editAssetsFormId">
	<input type="hidden" name="id" value="${applicationInstance?.id}" />
	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label></td>
								<td ><input type="text" id="assetName" name="assetName" value="${applicationInstance.assetName}" tabindex="10" /></td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="3"><input type="text" id="description" name="description" value="${applicationInstance.description}" size="50" tabindex="20" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><input type="text" id="assetType" name="assetType" value="Application" readonly="readonly" /></td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td ><input type="text" id="supportType"
									name="supportType" value="${applicationInstance.supportType}"  tabindex="21" />
								</td>
								<td class="label" nowrap="nowrap"><label for="appFunction">Function</label>
								</td>
								<td ><input type="text" id="appFunction"
									name="appFunction" value="${applicationInstance.appFunction}"  tabindex="31" />
								</td>
								<td class="label" nowrap="nowrap"><label for="userCount">Users</label>
								</td>
								<td ><input type="text" id="userId" name="userCount" value="${applicationInstance.userCount}"  tabindex="41" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
								<td ><input type="text" id="appVendor"
									name="appVendor" value="${applicationInstance.appVendor}"  tabindex="11" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme">SME1</label></td>
								<td ><input type="text" id="sme" name="sme"
									value="${applicationInstance.sme}"  tabindex="22" />
								</td>
								<td class="label" nowrap="nowrap"><label for="environment">Environment</label>
								</td>
								<td ><g:select id="environment" name="environment" from="${applicationInstance.constraints.environment.inList}" value="${applicationInstance.environment}"  tabindex="32"></g:select>
								</td>
								<td class="label" nowrap="nowrap"><label for="userLocations">User Location</label>
								</td>
								<td ><input type="text" id="userLocations"
									name="userLocations"
									value="${applicationInstance.userLocations}"  tabindex="42" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVersion">Version</label>
								</td>
								<td ><input type="text" id="appVersion"
									name="appVersion" value="${applicationInstance.appVersion}"  tabindex="12" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme2">SME2</label></td>
								<td ><input type="text" id="sme2" name="sme2"
									value="${applicationInstance.sme2}"  tabindex="23" />
								</td>
								<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label>
								</td>
								<td ><g:select id="criticality" name="criticality" from="${applicationInstance.constraints.criticality.inList}" value="${applicationInstance.criticality}"  tabindex="33"></g:select></td>
								<td class="label" nowrap="nowrap"><label for="useFrequency">Use	Frequency</label>
								</td>
								<td ><input type="text" id="useFrequency" name="useFrequency" value="${applicationInstance.useFrequency}" tabindex="43" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label></td>
								<td ><input type="text" id="appTech" name="appTech" value="${applicationInstance.appTech}" tabindex="13" />
								</td>
								<td class="label" nowrap="nowrap"><label for="businessUnit">Bus	Unit</label>
								</td>
								<td ><input type="text" id="businessUnit" name="businessUnit" value="${applicationInstance.businessUnit}" tabindex="24" />
								</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td ><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${applicationInstance.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="34" />
								</td>
								<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label>
								</td>
								<td ><input type="text" id="drRpoDesc"	name="drRpoDesc" value="${applicationInstance.drRpoDesc}" tabindex="44" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appSource">Source</label></td>
								<td ><input type="text" id="appSource"	name="appSource" value="${applicationInstance.appSource}" tabindex="14" />
								</td>
								<td class="label" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
								<td ><input type="text" id="appOwner" name="appOwner"	value="${applicationInstance.appOwner}" tabindex="25" />
								</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label>
								</td>
								<td ><g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${applicationInstance.planStatus}" tabindex="35" />
								</td>
								<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label>
								</td>
								<td ><input type="text" id="drRtoDesc"	name="drRtoDesc" value="${applicationInstance.drRtoDesc}" tabindex="45" />
								</td>
							</tr>
							<tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="license">License</label></td>
								<td ><input type="text" id="license" name="license" value="${applicationInstance.license}" tabindex="15" />
								</td>
								<td class="label"><label for="retireDate">Retire Date:</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
									</script>
									<input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" tabindex="26"
									value="<tds:convertDate date="${applicationInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />" > 
								</td>
								<td class="label" nowrap="nowrap"><label for="testProcs">Validation</label>
								</td>
								<td ><g:select  id="validation"	name="validation" from="${applicationInstance.constraints.validation.inList }"  value="${applicationInstance.validation}" tabindex="36" />
								</td>
								<td class="label" nowrap="nowrap"><label for="testProcs">Test Proc OK</label>
								</td>
								<td ><g:select  id="testProc"	name="testProc"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.testProc}" tabindex="46"/>
								</td>
							</tr>
							    <td></td>
							    <td></td>
							    <td  class="label"><label for="maintExpDate">Maint Exp.</label></td>
								<td valign="top"
										class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
									</script>
									<input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate" tabindex="27"
									value="<tds:convertDate date="${applicationInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />" > 
								</td>
								<td class="label" nowrap="nowrap"><label for="latency">Latency OK</label>
								</td>
								<td ><g:select  id="latency"	name="latency"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.latency}" tabindex="37" />
								</td>
								<td class="label" nowrap="nowrap"><label for="startupProc">Startup Proc OK</label>
								</td>
								<td ><g:select  id="testProcs"	name="startupProc" from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.startupProc}" tabindex="47"/>
								</td>
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom1">${applicationInstance.project.custom1 ?: 'Custom1'} </label></td>
								<td ><input type="text" id="custom1"	name="custom1" value="${applicationInstance.custom1}" tabindex="16" />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom1">${applicationInstance.project.custom2 ?: 'Custom2'}</label></td>
								<td ><input type="text" id="custom2"	name="custom2" value="${applicationInstance.custom2}" tabindex="28" />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom3">${applicationInstance.project.custom3 ?: 'Custom3'}</label></td>
								<td ><input type="text" id="custom3"	name="custom3" value="${applicationInstance.custom3}" tabindex="38" />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom4">${applicationInstance.project.custom4 ?: 'Custom4'}</label></td>
								<td ><input type="text" id="custom4"	name="custom4" value="${applicationInstance.custom4}" tabindex="48" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom5">${applicationInstance.project.custom5 ?: 'Custom5'}</label></td>
								<td ><input type="text" id="custom5"	name="custom5" value="${applicationInstance.custom5}" tabindex="17"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom6">${applicationInstance.project.custom6 ?: 'Custom6'}</label></td>
								<td ><input type="text" id="custom6"	name="custom6" value="${applicationInstance.custom6}"  tabindex="29"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom7">${applicationInstance.project.custom7 ?: 'Custom7'}</label></td>
								<td ><input type="text" id="custom7"	name="custom7" value="${applicationInstance.custom7}" tabindex="39"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom8">${applicationInstance.project.custom8 ?: 'Custom8'}</label></td>
								<td ><input type="text" id="custom8"	name="custom8" value="${applicationInstance.custom8}"  tabindex="49" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="license">URL</label></td>
								<td colspan="7"><input type="text" id="url" name="url" value="${applicationInstance.url}" size=50 tabindex="18" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap" ><label for="events">Event</label></td>
								<td colspan="7">
									<g:each in="${moveEvent}" var="moveEventList">
										<div  class="label" style="float: left;width: auto;padding: 5px;" ><label for="moveEvent"><b>${moveEventList?.name}</b></label>
	      								    <g:select id="okToMove__${moveEventList?.id}" name="okToMove_${moveEventList?.id}" from="${['Y', 'N']}" value="${AppMoveEvent.findByApplicationAndMoveEvent(applicationInstance,moveEventList)?.value}" noSelection="['':'?']" />
		                                  </label>
		                                </div>
									</g:each>
								</td>
								
							</tr> 
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Entity Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createSupportsList">
							<g:each in="${supportAssets}" var="support" status="i">
								<tr id='row_s_${i}'>
									<td><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
									<td><g:select name="entity_support_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${support?.asset?.assetType}"></g:select></td>
									  <g:if test="${support?.asset?.assetType=='Server'|| support?.asset.assetType=='Blade' || support?.asset.assetType=='VM'}">
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
									</g:if>
									<g:else>
									  <td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project =? order by assetName asc',[support?.asset?.assetType, project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
									</g:else>
									<td><g:select name="dtype_support_${i}" value="${support.type}" from="${dependencyType.value}" optionValue="value" />
									</td>
									<td><g:select name="status_support_${i}" value="${support.status}" from="${dependencyStatus.value}" optionValue="value" />
									</td>
									<td><a href="javascript:deleteRow('row_s_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')" /></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Entity Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr id='row_d_${i}'>
								<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td><g:select name="entity_dependent_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${dependent?.dependent?.assetType}"></g:select></td>
								<g:if test="${dependent?.dependent?.assetType=='Server'|| dependent?.dependent?.assetType=='Blade' || dependent?.dependent?.assetType=='VM'}">
								  <td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
								</g:if>
								<g:else>
								  <td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? order by assetName asc ',[dependent?.dependent?.assetType, project])}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
								</g:else>
								<td><g:select name="dtype_dependent_${i}" value="${dependent.type}" from="${dependencyType.value}" optionValue="value" />
								</td>
								<td><g:select name="status_dependent_${i}" value="${dependent.status}" from="${dependencyStatus.value}" optionValue="value" />
								</td>
								<td><a href="javascript:deleteRow('row_d_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}" />
					<input name="supportCount"  id="supportCount" type="hidden" value="${supportAssets.size()}" />
					<input name="redirectTo" id="redirectTo" type="hidden" value="${redirectTo}"/>
					<input type = "hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<input type = "hidden" id = "appId"  value ="${applicationInstance.id}"/>
					<input type = "hidden" id = "tabType" name="tabType" value =""/>
					<input name="updateView" id="updateView" type="hidden" value=""/>
					<g:if test="${redirectTo!='planningConsole'}">
					  <span class="button"><g:actionSubmit class="save" value="Update/Close" action="Update" /> </span>
					  <span class="button"><input type="button" class="save" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><g:actionSubmit class="delete"	onclick=" return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					  <span class="button"><input id="updatedId" name="updatedId" type="button" class="save" value="Update/Close" onclick="submitRemoteForm()"> </span>
					  <span class="button"><input type="button" class="save" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#appId').val(),'app')" value="Delete" /> </span>
					</g:else>
                </div></td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val())
</script>
