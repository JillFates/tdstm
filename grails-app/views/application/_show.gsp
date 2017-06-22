<%@page defaultCodec="html" %>

<table class="planning-application-table">
	<tbody>
		<tr>
			<td class="label ${standardFieldSpecs.assetName.imp?:''}" nowrap="nowrap"><label for="assetName">${standardFieldSpecs.assetName.label}</label></td>
			<td colspan="3" style="font-weight:bold;" class="" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetName.tip?:''}">${applicationInstance.assetName}</td>
			
			<td class="label ${standardFieldSpecs.description.imp?:''}" nowrap="nowrap"><label for="description">${standardFieldSpecs.description.label}</label></td>
			<td colspan="3" class="" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.description.tip?:''}">${applicationInstance.description}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.appAccess.imp?:''}" nowrap="nowrap"><label for="appAccess">${standardFieldSpecs.appAccess.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appAccess.tip?:''}">${applicationInstance.assetType}</td>
			
			<td class="label ${standardFieldSpecs.supportType.imp?:''}" nowrap="nowrap"><label for="supportType">${standardFieldSpecs.supportType.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.supportType.tip?:''}">${applicationInstance.supportType}</td>
			
			<td class="label ${standardFieldSpecs.appFunction.imp?:''}" nowrap="nowrap"><label for="appFunction">${standardFieldSpecs.appFunction.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appFunction.tip?:''}">${applicationInstance.appFunction}</td>
			
			<td class="label ${standardFieldSpecs.userCount.imp?:''}" nowrap="nowrap"><label for="userCount">${standardFieldSpecs.userCount.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.userCount.tip?:''}">${applicationInstance.userCount}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.appVendor.imp?:''}" nowrap="nowrap"><label for="appVendor">${standardFieldSpecs.appVendor.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appVendor.tip?:''}">${applicationInstance.appVendor}</td>

			<td class="label ${standardFieldSpecs.sme.imp?:''}" nowrap="nowrap"><label for="sme">${standardFieldSpecs.sme.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme.tip?:''}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
				</span>
			</td>

			<td class="label ${standardFieldSpecs.environment.imp?:''}" nowrap="nowrap"><label for="environment">${standardFieldSpecs.environment.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip?:''}">${applicationInstance.environment}</td>
			
			<td class="label ${standardFieldSpecs.userLocations.imp?:''}" nowrap="nowrap"><label for="userLocations">${standardFieldSpecs.userLocations.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.userLocations.tip?:''}">${applicationInstance.userLocations}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.appVersion.imp?:''}" nowrap="nowrap"><label for="appVersion">${standardFieldSpecs.appVersion.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appVersion.tip?:''}">${applicationInstance.appVersion}</td>
			
			<td class="label ${standardFieldSpecs.sme2.imp?:''}" nowrap="nowrap"><label for="sme2">${standardFieldSpecs.sme2.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme2.tip?:''}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
				</span>
			</td>
			
			<td class="label ${standardFieldSpecs.criticality.imp?:''}" nowrap="nowrap"><label for="criticality">${standardFieldSpecs.criticality.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.criticality.tip?:''}">${applicationInstance.criticality}</td>
			
			<td class="label ${standardFieldSpecs.userFrequency.imp?:''}" nowrap="nowrap"><label for="useFrequency">${standardFieldSpecs.userFrequency.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.userFrequency.tip?:''}">${applicationInstance.useFrequency}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.appTech.imp?:''}" nowrap="nowrap"><label for="appTech">${standardFieldSpecs.appTech.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appTech.tip?:''}">${applicationInstance.appTech}</td>
			
			<td class="label ${standardFieldSpecs.appOwner.imp?:''}" nowrap="nowrap"><label for="appOwner">${standardFieldSpecs.appOwner.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appOwner.tip?:''}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
				</span>
			</td>
			
			<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle">${standardFieldSpecs.moveBundle.label} : Dep. Group</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:''}">${applicationInstance.moveBundle} : ${dependencyBundleNumber}</td>
			
			<td class="label ${standardFieldSpecs.drRpoDesc.imp?:''}" nowrap="nowrap"><label for="drRpoDesc">${standardFieldSpecs.drRpoDesc.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.drRpoDesc.tip?:''}">${applicationInstance.drRpoDesc}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.appSource.imp?:''}" nowrap="nowrap"><label for="appSource">${standardFieldSpecs.appSource.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appSource.tip?:''}">${applicationInstance.appSource}</td>
			
			<td class="label ${standardFieldSpecs.businessUnit.imp?:''}" nowrap="nowrap"><label for="businessUnit">${standardFieldSpecs.businessUnit.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.businessUnit.tip?:''}">${applicationInstance.businessUnit}</td>
			
			<td class="label ${standardFieldSpecs.planStatus.imp?:''}" nowrap="nowrap"><label for="planStatus">${standardFieldSpecs.planStatus.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.planStatus.tip?:''}">${applicationInstance.planStatus}</td>
			
			<td class="label ${standardFieldSpecs.drRtoDesc.imp?:''}" nowrap="nowrap"><label for="drRtoDesc">${standardFieldSpecs.drRtoDesc.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.drRtoDesc.tip?:''}">${applicationInstance.drRtoDesc}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.license.imp?:''}" nowrap="nowrap"><label for="license">${standardFieldSpecs.license.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.license.tip?:''}">${applicationInstance.license}</td>
			
			<td class="label ${standardFieldSpecs.retireDate.imp?:''}" nowrap="nowrap"><label for="retireDate">${standardFieldSpecs.retireDate.label}</label></td>
			<td class="" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.retireDate.tip?:''}">
				<tds:convertDate date="${applicationInstance?.retireDate}" />
			</td>
			
			<td class="label ${standardFieldSpecs.validation.imp?:''}" nowrap="nowrap"><label for="validation">${standardFieldSpecs.validation.label}</label></td>
			<td class="" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.validation.tip?:''}">${applicationInstance.validation}</td>
			
			<td class="label ${standardFieldSpecs.testProc.imp?:''}" nowrap="nowrap"><label for="testProc">${standardFieldSpecs.testProc.label}</label></td>
			<td class="" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.testProc.tip?:''}">${applicationInstance.testProc ? applicationInstance.testProc : '?'}</td>
		</tr>
		<tr>
			<td></td>
			<td></td>
			
			<td class="label ${standardFieldSpecs.maintExpDate.imp?:''}" nowrap="nowrap"><label for="maintExpDate">${standardFieldSpecs.maintExpDate.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.maintExpDate.tip?:''}">
				<tds:convertDate date="${applicationInstance?.maintExpDate}" format="12hrs" />
			</td>
			
			<td class="label ${standardFieldSpecs.latency.imp?:''}" nowrap="nowrap"><label for="latency">${standardFieldSpecs.latency.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.latency.tip?:''}">${applicationInstance.latency ? applicationInstance.latency : '?'}</td>
			
			<td class="label ${standardFieldSpecs.startupProc.imp?:''}" nowrap="nowrap"><label for="startupProc">${standardFieldSpecs.startupProc.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.startupProc.tip?:''}">${applicationInstance.startupProc ? applicationInstance.startupProc : '?'}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.url.imp?:''}" nowrap="nowrap"><label for="url">${standardFieldSpecs.url.label}</label></td>
			<td class="valueNW" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.url.tip?:''}"><tds:textAsLink text="${applicationInstance.url}" target="_new"/></td>

			<td class="label ${standardFieldSpecs.externalRefId.imp?:''}" nowrap="nowrap"><label for="externalRefId">${standardFieldSpecs.externalRefId.label}</label></td>
			<td class="" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.externalRefId.tip?:''}">${applicationInstance.externalRefId}</td>

			<td class="label ${standardFieldSpecs.shutdownBy.imp?:''}" nowrap="nowrap"><label for="shutdownBy">${standardFieldSpecs.shutdownBy.label}</label></td>
			<td class="valueNW" nowrap="nowrap" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.shutdownBy.tip?:''}">
			<g:if test="${shutdownById == -1}">
				${shutdownBy}
			</g:if>
			<g:else>
				<span class="clickableText" onClick="Person.showPersonDialog(${shutdownById},'generalInfoShow')">
					${shutdownBy}
				</span>
			</g:else>
			<g:if test="${applicationInstance.shutdownFixed ==1 }">
				<input type="checkbox" id="shutdownFixedShowId" disabled="disabled" name="shutdownFixed" checked="checked"/>
				<label for="shutdownFixedId" >Fixed</label>
			</g:if>
			</td>

			<td class="label ${standardFieldSpecs.shutdownDuration.imp?:''}" nowrap="nowrap"><label for="shutdownDuration">${standardFieldSpecs.shutdownDuration.label}</label></td>
			<td class="valueNW" nowrap="nowrap" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.shutdownDuration.tip?:''}">${applicationInstance.shutdownDuration ? applicationInstance.shutdownDuration+'m' : ''}</td>
		</tr>
		<tr>
			<td class="label ${standardFieldSpecs.startupBy.imp?:''}" nowrap="nowrap"><label for="startupBy">${standardFieldSpecs.startupBy.label}</label></td>
			<td class="valueNW" nowrap="nowrap" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.startupBy.tip?:''}">
			<g:if test="${startupById == -1}">
				${startupBy}
			</g:if>
			<g:else>
				<span class="clickableText" onClick="Person.showPersonDialog(${startupById},'generalInfoShow')">
					${startupBy}
				</span>
			</g:else>
			<g:if test="${applicationInstance.startupFixed ==1 }">
				<input type="checkbox" id="startupFixedShowId" disabled="disabled" name="startupFixed" value="${applicationInstance.startupFixed}" checked="checked"/>
				<label for="startupFixedId" >Fixed</label>
			</g:if>
			</td>
			
			<td class="label ${standardFieldSpecs.startupDuration.imp?:''}" nowrap="nowrap"><label for="shutdownDuration">${standardFieldSpecs.startupDuration.label}</label></td>
			<td class="valueNW" nowrap="nowrap" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.startupDuration.tip?:''}">${applicationInstance.startupDuration ? applicationInstance.startupDuration+'m' :''} </td>
			
			<td class="label ${standardFieldSpecs.testingBy.imp?:''}" nowrap="nowrap"><label for="testingBy">${standardFieldSpecs.testingBy.label}</label></td>
			<td class="valueNW" nowrap="nowrap" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.testingBy.tip?:''}">
			<g:if test="${testingById == -1}">
				${testingBy}
			</g:if>
			<g:else>
				<span class="clickableText" onClick="Person.showPersonDialog(${testingById},'generalInfoShow')">
					${testingBy}
				</span>
			</g:else>
			<g:if test="${applicationInstance.testingFixed ==1 }">
				<input type="checkbox" id="testingFixedShowId" disabled="disabled" name="testingFixed" checked="checked" />
				<label for="testingFixedId" >Fixed</label>
			</g:if>
			</td>
			
				<td class="label ${standardFieldSpecs.testingDuration.imp?:''}" nowrap="nowrap"><label for="testingDuration">${standardFieldSpecs.testingDuration.label}</label></td>
			<td class="valueNW" nowrap="nowrap" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.testingDuration.tip?:''}">${applicationInstance.testingDuration ? applicationInstance.testingDuration+'m' :''}</td>
		</tr>
		<g:render template="../assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs, highlightMap:highlightMap]"></g:render>
	</tbody>
</table>