<%@page import="com.tds.asset.Application"%>
<%@page import="com.tds.asset.AssetType"%>
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<g:set var="assetClass" value="Application" />
<%@page import="grails.converters.JSON"%>

<div class="modal-content tds-angular-component-content" tabindex="0">
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
									<kendo-dropdownlist #controlSme
											class="person-list"
											name="modelAssetSme"
											[(ngModel)]="model.asset.sme.id"
											[defaultItem]="defaultItem"
											[textField]="'fullName'"
											[valueField]="'personId'"
											[data]="${personList as JSON}">
									</kendo-dropdownlist>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.environment}">
									<kendo-dropdownlist
											class="person-list"
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
									<div class="swapper-image" (click)="shufflePerson('sme', 'sme2')" title="Swap Contacts"></div>
								<kendo-dropdownlist  #controlSme2
									class="person-list"
									name="modelAssetSme2"
									[(ngModel)]="model.asset.sme2.id"
									[defaultItem]="defaultItem"
									[textField]="'fullName'"
									[valueField]="'personId'"
									[data]="${personList as JSON}">
								</kendo-dropdownlist>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.criticality}" value="${asset.criticality}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.criticality}">
									<kendo-dropdownlist
											name="modelAssetCriticality"
											[(ngModel)]="model.asset.criticality"
											[defaultItem]="'Please Select'"
											[data]="${asset.constraints.criticality.inList as JSON}">
									</kendo-dropdownlist>
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
									<div class="swapper-image" (click)="shufflePerson('sme2', 'appOwner')" title="Swap Contacts"></div>
									<kendo-dropdownlist
											class=""
											name="modelAssetappOwner"
											[(ngModel)]="model.asset.appOwner.id"
											[defaultItem]="defaultItem"
											[textField]="'fullName'"
											[valueField]="'personId'"
											[data]="${personList as JSON}">
									</kendo-dropdownlist>
								</tdsAngular:tooltipSpan>
								</td>
								<tdsAngular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle?.id}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
									<kendo-dropdownlist
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
											name="modelAssetPlanStatus"
											[(ngModel)]="model.asset.planStatus"
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
											name="modelAssetValidation"
											[data]="${asset.constraints.validation.inList as JSON}"
											[(ngModel)]="model.asset.validation">
									</kendo-dropdownlist>

																	%{--<g:select  id="validation"	class="${standardFieldSpecs.validation.imp?:''}" name="validation"
																		from="${applicationInstance.constraints.validation.inList }"
																		value="${applicationInstance.validation}" tabindex="37" />--}%

								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.testProc}" value="${asset.testProc}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.testProc}">
								%{--// TODO add data--}%
									<kendo-dropdownlist
											name="modelAssetTestProc"
											[(ngModel)]="model.asset.testProc"
											[defaultItem]="'?'"
											[data]="yesNoList">
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
									<kendo-dropdownlist
											name="modelAssetLatency"
											[(ngModel)]="model.asset.latency"
											[defaultItem]="'?'"
											[data]="yesNoList">
									</kendo-dropdownlist>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}"/>
								<td>
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.startupProc}">
										<kendo-dropdownlist
												name="modelAssetStartupProc"
												[(ngModel)]="model.asset.startupProc"
												[defaultItem]="'?'"
												[data]="yesNoList">
										</kendo-dropdownlist>
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
										<tds-combobox-group
												[model]="model.asset.shutdownBy"
												(modelChange)="model.asset.shutdownBy = $event"
												(isFixedChange)="model.asset.shutdownFixed = $event"
											    [isFixed]="${asset.shutdownFixed}"
												[people]="${personList as JSON}"
												[team]="${availableRoles as JSON}"></tds-combobox-group>
									</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownDuration}" tooltipDataPlacement="bottom">
									<input type="text" id="shutdownDuration" name="shutdownDuration"
										   class="${standardFieldSpecs.shutdownDuration.imp?:''}"
										   [(ngModel)]="model.asset.shutdownDuration" tabindex="48" size="7"/>m
								</tdsAngular:tooltipSpan>
								</td>
							</tr>
							<tr>
								<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
								<td colspan="1" nowrap="nowrap" data-for="startupBy" class="${standardFieldSpecs.startupBy.imp?:''}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.startupBy}">
									<tds-combobox-group
											[model]="model.asset.startupBy"
											(modelChange)="model.asset.startupBy = $event"
											(isFixedChange)="model.asset.startupFixed = $event"
											[isFixed]="${asset.startupFixed}"
											[people]="${personList as JSON}"
											[team]="${availableRoles as JSON}"></tds-combobox-group>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabelAndField field="${standardFieldSpecs.startupDuration}"
														value="${asset.startupDuration}"
															   ngmodel="model.asset.startupDuration" tabindex="29"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>
								<td colspan="1" nowrap="nowrap" data-for="testingBy" class="${standardFieldSpecs.testingBy.imp?:''}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.testingBy}">
									<tds-combobox-group
											[model]="model.asset.testingBy"
											(modelChange)="model.asset.testingBy = $event"
											(isFixedChange)="model.asset.testingFixed = $event"
											[isFixed]="${asset.testingFixed}"
											[people]="${personList as JSON}"
											[team]="${availableRoles as JSON}"></tds-combobox-group>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}"/>
								<td>
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.testingDuration}" tooltipDataPlacement="bottom">
									<input type="text" id="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}" name="testingDuration"
										   [(ngModel)]="model.asset.testingDuration"
										   tabindex="49"  size="7"/>m
								</tdsAngular:tooltipSpan>
								</td>
							</tr>
						</tbody>
					</table>
				</div></td>
		</tr>

		<%-- Dependency Edit Block --%>
		<tr id="applicationDependentId" class="assetDependent">
			<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/></span></td>
		</tr>

		<!-- Dependencies -->
		<tr id="deps">
			<tds-supports-depends [(model)]="model"></tds-supports-depends>
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