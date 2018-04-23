<%@page import="com.tds.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%-- <g:set var="assetClass" value="${(new Application()).assetClass}" /> --%>
<g:set var="assetClass" value="Application" />

<style>
	#select2-drop{ width: 200px !important; }
</style>

<div class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span
				aria-hidden="true">×</span></button>
		<h4 class="modal-title">Application Edit</h4>
	</div>

	<div class="modal-body">
		<div>
			<form name="storageEditForm">
				<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset.assetName}" />
								<td colspan="3">
									<tdsAngular:inputControl field="${standardFieldSpecs.assetName}" tabindex="11"
															 value="${asset.assetName}"  ngmodel="model.asset.assetName"  />
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${asset.description}"/>
								<td colspan="3">
									<tdsAngular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="11"
													  value="${asset.description}"  ngmodel="model.asset.description"
															 tooltipDataPlacement="bottom"/>
								</td>
							</tr>
							<tr>
								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVendor}"
															   value="${asset.appVendor}" ngmodel="model.asset.appVendor" tabindex="13"/>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.supportType}"
															   value="${asset.supportType}" ngmodel="model.asset.supportType" tabindex="22"/>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appFunction}"
															   value="${asset.appFunction}" ngmodel="model.asset.appFunction" tabindex="32"/>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userCount}"
															   value="${asset.userCount}"  ngmodel="model.asset.userCount"
															   tabindex="42" tooltipDataPlacement="bottom"/>
							</tr>

							<tr>
								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appVersion}"
															   value="${asset.appVersion}" ngmodel="model.asset.appVersion" tabindex="14"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
								<td data-for="sme" class="${standardFieldSpecs.sme.imp?:''}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.sme}">

								%{--	<g:select from="${personList}" id="sme1" name="sme.id" class="${standardFieldSpecs.sme.imp?:''} personContact assetSelect" optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value,this.id)" value="${applicationInstance.sme?.id}"
										tabindex="23"
										noSelection="${['null':' Please Select']}"
									/>--}%
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.environment}">
									<kendo-dropdownlist
											class="select"
											name="modelAssetEnvironment"
											[(ngModel)]="model.asset.environment"
											[defaultItem]="'Please Select'"
											[data]="model.environmentOptions">
									</kendo-dropdownlist>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.userLocations}"
															   value="${asset.userLocations}" ngmodel="model.asset.userLocations" tabindex="43" tooltipDataPlacement="bottom"/>
							</tr>

							<tr>
								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appTech}"
															   value="${asset.appTech}" ngmodel="model.asset.appTech" tabindex="15"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
								<td data-for="sme2" class="${standardFieldSpecs.sme2.imp?:''} suffleTd" >
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.sme2}">
								 <img src="${resource(dir:'images',file:'swapicon.png')}" onclick="shufflePerson('sme1','sme2')" class="SuffleImage" alt="Swap Contacts" title="Swap Contacts"/>

							%{--		<g:select from="${personList}" id="sme2" name="sme2.id" class="${standardFieldSpecs.sme2.imp?:''} suffleSelect personContact assetSelect" optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value, this.id)"
										value="${applicationInstance.sme2?.id}"
										tabindex="24"
										noSelection="${['null':' Please Select']}"
									/>--}%

								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.criticality}" value="${asset.criticality}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.criticality}">
									%{--// TODO add data--}%
									<kendo-dropdownlist
											class="select"
											name="modelAssetCriticality"
											[(ngModel)]="model.asset.criticality"
											[defaultItem]="'Please Select'"
											[data]="">
									</kendo-dropdownlist>

								%{--	<g:select id="criticality" class="${standardFieldSpecs.criticality.imp?:''}" name="criticality"
										from="${applicationInstance.constraints.criticality.inList}" value="${applicationInstance.criticality}"
										noSelection="${['':'Please select']}"
										tabindex="34">
									</g:select>--}%
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.useFrequency}"
															   value="${asset.useFrequency}" ngmodel="model.asset.useFrequency" tabindex="44" tooltipDataPlacement="bottom"/>

							</tr>
							<tr>
								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.appSource}"
															   value="${asset.appSource}" ngmodel="model.asset.appSource" tabindex="16"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
								<td class="suffleTd ${standardFieldSpecs.appOwner.imp?:''}" data-for="appOwner">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.appOwner}">

								 <img src="${resource(dir:'images',file:'swapicon.png')}" onclick="shufflePerson('sme2','appOwnerEdit')" class="SuffleImage" alt="Swap Contacts" title="Swap Contacts"/>
%{--
									<g:select from="${personList}" id="appOwnerEdit" class="${standardFieldSpecs.appOwner.imp?:''} suffleSelect personContact assetSelect" name="appOwner.id"  optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value, this.id)"
										value="${applicationInstance.appOwner?.id}"
										tabindex="25"
										noSelection="${['null':' Please Select']}"/>
--}%

								</tdsAngular:tooltipSpan>

								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
									<kendo-dropdownlist
											class="select"
											name="modelAssetMoveBundle"
											[data]="model.moveBundleList"
											[(ngModel)]="model.asset.moveBundle"
											[textField]="'name'"
											[valueField]="'id'">
									</kendo-dropdownlist>

								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}"
															   value="${asset.drRpoDesc}"  ngmodel="model.asset.drRpoDesc" tabindex="45" tooltipDataPlacement="bottom"/>

							</tr>
							<tr>
								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.license}"
															   value="${asset.license}" ngmodel="model.asset.license" tabindex="17"/>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.businessUnit}"
															   value="${asset.businessUnit}" ngmodel="model.asset.businessUnit" tabindex="26"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.planStatus}">
									<kendo-dropdownlist
											class="select"
											name="modelAssetPlanStatus"
											[(ngModel)]="model.asset.planStatus"
											[defaultItem]="'Please Select'"
											[data]="model.planStatusOptions">
									</kendo-dropdownlist>
									%{--<g:select from="${planStatusOptions}" id="planStatus" class="${standardFieldSpecs.planStatus.imp?:''}" name="planStatus" value="${applicationInstance.planStatus}" tabindex="36" />--}%
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}"
															   value="${asset.drRtoDesc}" ngmodel="model.asset.drRtoDesc" tabindex="46" tooltipDataPlacement="bottom"/>

							</tr>
							<tr>
							<tr>
								<td></td>
								<td></td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
								<td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.retireDate}">
									<kendo-datepicker
											name="modelAssetRetireDate"
											[format]="dateFormat"
											[(value)]="model.asset.retireDate">
									</kendo-datepicker>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.validation}">
								%{--// TODO add data--}%
									<kendo-dropdownlist
											class="select"
											name="modelAssetValidation"
											[(ngModel)]="model.asset.validation"
											[defaultItem]="'Please Select'"
											[data]="">
									</kendo-dropdownlist>
%{--									<g:select  id="validation"	class="${standardFieldSpecs.validation.imp?:''}" name="validation"
										from="${applicationInstance.constraints.validation.inList }"
										value="${applicationInstance.validation}" tabindex="37" />--}%
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.testProc}" value="${asset.testProc}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.testProc}">
								%{--// TODO add data--}%
									<kendo-dropdownlist
											class="select"
											name="modelAssetTestProc"
											[(ngModel)]="model.asset.testProc"
											[defaultItem]="'Please Select'"
											[data]="">
									</kendo-dropdownlist>
									%{--
									<g:select  id="testProc" class="${standardFieldSpecs.testProc.imp?:''} ynselect" name="testProc"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.testProc}" tabindex="47"/>--}%
								</tdsAngular:tooltipSpan>
								</td>
							</tr>
							<tr>
								<td></td>
								<td></td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
								<td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
									<kendo-datepicker
											name="modelAssetMaintExpDate"
											[format]="dateFormat"
											[(value)]="model.asset.maintExpDate">
									</kendo-datepicker>
								    %{--TODO FIX convertDate--}%
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.latency}" value="${asset.latency}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.latency}">
								%{--// TODO add data --}%
									<kendo-dropdownlist
											class="select"
											name="modelAssetLatency"
											[(ngModel)]="model.asset.latency"
											[defaultItem]="'Please Select'"
											[data]="">
									</kendo-dropdownlist>

								%{--	<g:select  id="latency" class="${standardFieldSpecs.latency.imp?:''} ynselect"	name="latency"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.latency}" tabindex="38" />--}%
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}"/>
								<td>
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.startupProc}">
										<kendo-dropdownlist
												class="select"
												name="modelAssetStartupProc"
												[(ngModel)]="model.asset.startupProc"
												[defaultItem]="'Please Select'"
												[data]="">
										</kendo-dropdownlist>

									%{--	<g:select  id="startupProc" class="${standardFieldSpecs.startupProc.imp?:''} ynselect" name="startupProc" from="${['Y', 'N']}" value="?"
											 noSelection="['':'?']" tabindex="46" value="${applicationInstance.startupProc}" tabindex="48"/>--}%

									</tdsAngular:tooltipSpan>
								</td>

							</tr>
							<tr>
								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.url}"
															   value="${asset.url}" ngmodel="model.asset.url" tabindex="18"/>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.externalRefId}"
															   value="${asset.externalRefId}" ngmodel="model.asset.externalRefId" tabindex="28"/>

								<td class="label ${standardFieldSpecs.shutdownBy.imp?:''}" nowrap="nowrap">
									<label for="shutdownBy">
										<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownBy}">
											${standardFieldSpecs.shutdownBy.label}
										</tdsAngular:tooltipSpan>
									</label>
								</td>
								<td class="${standardFieldSpecs.shutdownBy.imp?:''}" data-for="shutdownBy" nowrap="nowrap">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownBy}">

								   %{--<g:render template="bySelect" model="[name:'shutdownBy' , id:'shutdownByEditId', className:'assetSelect']"></g:render>--}%

									<input type="checkbox" id="shutdownByEditIdFixed"  name="shutdownFixed" value="${applicationInstance.shutdownFixed} "
										${!applicationInstance.shutdownBy || applicationInstance.shutdownBy.contains('@') ? 'disabled="disabled"' : ''}
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
										${applicationInstance.shutdownFixed==1? 'checked="checked"' : ''}/>Fixed
									</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownDuration}" tooltipDataPlacement="bottom">
									<input type="text" id="shutdownDuration" name="shutdownDuration" class="${standardFieldSpecs.shutdownDuration.imp?:''}"
										value="${applicationInstance.shutdownDuration}" tabindex="48" size="7"/>m
								</tdsAngular:tooltipSpan>
								</td>
							</tr>
							<tr>
								<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
								<td colspan="1" nowrap="nowrap" data-for="startupBy" class="${standardFieldSpecs.startupBy.imp?:''}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.startupBy}">

								   %{--<g:render template="bySelect" model="[name:'startupBy', id:'startupByEditId', className:'assetSelect']" tabindex="19"></g:render>--}%

									<input type="checkbox" id="startupByEditIdFixed" name="startupFixed" value="${applicationInstance.startupFixed}"
										${!applicationInstance.startupBy || applicationInstance.startupBy.contains('@') ? 'disabled="disabled"' : ''}
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
										${applicationInstance.startupFixed ==1? 'checked="checked"' : ''}/>Fixed
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}"
														value="${asset.startupDuration}"
															   ngmodel="model.asset.startupDuration" tabindex="29"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>
								<td colspan="1" nowrap="nowrap" data-for="testingBy" class="${standardFieldSpecs.testingBy.imp?:''}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.testingBy}">

								  %{--<g:render template="bySelect" model="[name:'testingBy', id:'testingByEditId', className:'assetSelect']"></g:render>--}%

									<input type="checkbox" id="testingByEditIdFixed" name="testingFixed" value="${applicationInstance.testingFixed}"
										${!applicationInstance.testingBy || applicationInstance.testingBy.contains('@') ? 'disabled="disabled"' : ''}
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
										${applicationInstance.testingFixed ==1? 'checked="checked"' : ''}/>Fixed
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.testingDuration}" tooltipDataPlacement="bottom">
									<input type="text" id="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}" name="testingDuration"
											value="${applicationInstance.testingDuration}" tabindex="49"  size="7"/>m
								</tdsAngular:tooltipSpan>
								</td>
							</tr>

							<%-- Custom User Defined Fields Section --%>
		%{--
							<tbody class="customTemplate">
								<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:applicationInstance]"></g:render>
							</tbody>--}%

						</tbody>
					</table>
				</div></td>
		</tr>

		<%-- Dependency Edit Block --%>
		<tr id="applicationDependentId" class="assetDependent">
			<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/></span></td>
		</tr>

		<%-- Action Buttons --%>
		<tr>
			<td colspan="2">
				<div class="buttons">
					%{--<g:render template="../assetEntity/editButtons" model="[assetEntity:applicationInstance]"></g:render>--}%
				</div>
			</td>
		</tr>
	</table>
			</form>
		</div>
	</div>

	<div class="modal-footer form-group-center">
		<button class="btn btn-primary pull-left" type="button" (click)="onUpdate()"><span
				class="fa fa-fw fa-floppy-o"></span> Update</button>
		<tds:hasPermission permission="${Permission.AssetDelete}">
			<button class="btn btn-danger pull-left mar-left-50" (click)="onDelete()" type="button"><span
					class="glyphicon glyphicon-trash"></span> Delete</button>
		</tds:hasPermission>
		<button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span
				class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</div>