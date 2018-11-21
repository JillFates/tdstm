<%@page defaultCodec="html" %>

<table class="planning-application-table">
	<tbody>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${asset.assetName}"/>

			<td colspan="3" style="font-weight:bold;" class="${standardFieldSpecs.assetName.imp}">${asset.assetName}</td>
			<tds:inputLabel field="${standardFieldSpecs.description}" value="${asset.description}"/>
			<td colspan="3" class="${standardFieldSpecs.description.imp}" >${asset.description}</td>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appVendor}" value="${asset.appVendor}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${asset.supportType}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appFunction}" value="${asset.appFunction}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.userCount}" value="${asset.userCount}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appVersion}" value="${asset.appVersion}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${asset.sme}"/>
			<td class="valueNW ${standardFieldSpecs.sme.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${asset.sme?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${asset.sme}" />
                </span>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.environment}" value="${asset.environment}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.userLocations}" value="${asset.userLocations}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appTech}" value="${asset.appTech}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${asset.sme2}"/>
			<td class="valueNW ${standardFieldSpecs.sme2.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${asset.sme2?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${asset.sme2}" />
				</span>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.criticality}" value="${asset.criticality}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.useFrequency}" value="${asset.useFrequency}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appSource}" value="${asset.appSource}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${asset.appOwner}"/>
			<td class="valueNW ${standardFieldSpecs.appOwner.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${asset.appOwner?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${asset.appOwner}" />
				</span>
			</td>
			<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
				<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
					${standardFieldSpecs.moveBundle.label} : Dep. Group
				</label>
			</td>
			<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">
				${asset?.moveBundle}
				<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${asset.drRpoDesc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.license}" value="${asset.license}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.businessUnit}" value="${asset.businessUnit}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${asset.drRtoDesc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}" />
			<td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}"><tds:convertDate date="${asset?.retireDate}" endian="${dateFormat}"/></td>

			<tdsAngular:showLabelAndField field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.testProc}" value="${asset.testProc == '?' ? '' : asset.testProc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}" />
			<td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}"><tds:convertDate date="${asset?.maintExpDate}" endian="${dateFormat}" /></td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.latency}" value="${asset.latency == '?' ? '' : asset.latency}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.startupProc}" value="${asset.startupProc == '?' ? '' : asset.startupProc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.url}" value="${asset.url}" />
			<td class="valueNW ${standardFieldSpecs.url.imp}" ><tds:textAsLink  text="${asset.url}" target="_new"/></td>

			<tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownBy}" value="${asset.shutdownBy}"/>
			<td class="valueNW ${standardFieldSpecs.shutdownBy.imp}" nowrap="nowrap">
			<g:if test="${shutdownById == -1}">
				${shutdownBy}
			</g:if>
			<g:else>
				<span class="clickableText" onClick="Person.showPersonDialog(${shutdownById},'generalInfoShow')">
					${shutdownBy}
				</span>
			</g:else>
			<g:if test="${asset.shutdownFixed ==1 }">
				<input type="checkbox" id="shutdownFixedShowId" disabled="disabled" name="shutdownFixed" checked="checked"/>
				<label for="shutdownFixedId" >Fixed</label>
			</g:if>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
			<td class="valueNW ${standardFieldSpecs.startupBy.imp}" nowrap="nowrap" >
			<g:if test="${startupById == -1}">
				${startupBy}
			</g:if>
			<g:else>
				<span class="clickableText" onClick="Person.showPersonDialog(${startupById},'generalInfoShow')">
					${startupBy}
				</span>
			</g:else>
			<g:if test="${asset.startupFixed ==1 }">
				<input type="checkbox" id="startupFixedShowId" disabled="disabled" name="startupFixed" value="${asset.startupFixed}" checked="checked"/>
				<label for="startupFixedId" >Fixed</label>
			</g:if>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>
			<td class="valueNW ${standardFieldSpecs.testingBy.imp}" nowrap="nowrap">
			<g:if test="${testingById == -1}">
				${testingBy}
			</g:if>
			<g:else>
				<span class="clickableText" onClick="Person.showPersonDialog(${testingById},'generalInfoShow')">
					${testingBy}
				</span>
			</g:else>
			<g:if test="${asset.testingFixed ==1 }">
				<input type="checkbox" id="testingFixedShowId" disabled="disabled" name="testingFixed" checked="checked" />
				<label for="testingFixedId" >Fixed</label>
			</g:if>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}" tooltipDataPlacement="bottom"/>
		</tr>
		<g:render template="/angular/common/customShow" model="[asset:asset,customs:customs]"></g:render>
        <g:render template="/angular/common/assetTags"></g:render>
	</tbody>
</table>



