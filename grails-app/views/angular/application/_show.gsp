<%@page defaultCodec="html" %>

<table class="planning-application-table">
	<tbody>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${asset.assetName}"/>

			<td colspan="3" style="font-weight:bold;" class="${standardFieldSpecs.assetName.imp}">
				<tds:tooltipSpan field="${standardFieldSpecs.assetName}">
					${asset.assetName}
				</tds:tooltipSpan>
			</td>
			<tds:inputLabel field="${standardFieldSpecs.description}" value="${asset.description}"/>
			<td colspan="3" class="${standardFieldSpecs.description.imp}" >
				<tds:tooltipSpan field="${standardFieldSpecs.description}" tooltipDataPlacement="bottom">
					${asset.description}
				</tds:tooltipSpan>
			</td>
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
				<span class="clickableText" onClick="Person.showPersonDialog(${asset.sme?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme.tip?:standardFieldSpecs.sme.label}">
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
				<span class="clickableText" onClick="Person.showPersonDialog(${asset.sme2?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme2.tip?: standardFieldSpecs.sme2.label}">
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
				<span class="clickableText" onClick="Person.showPersonDialog(${asset.appOwner?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appOwner.tip?: standardFieldSpecs.appOwner.label}">
					<tds:nameAndCompany client="${client}" person="${asset.appOwner}" />
				</span>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.moveBundle}" value="${asset.moveBundle}"/>
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
			<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
			<td class="valueNW ${standardFieldSpecs.retireDate.imp}">
			<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
				<tds:convertDate date="${asset?.retireDate}" />
			</tds:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.testProc}" value="${asset.testProc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
			<td class="valueNW ${standardFieldSpecs.maintExpDate.imp}">
			<tdsAngular:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
				<tds:convertDate date="${asset?.maintExpDate}" />
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.latency}" value="${asset.latency}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.startupProc}" value="${asset.startupProc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.url}" value="${asset.url}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownBy}" value="${asset.shutdownBy}"/>
			<td class="valueNW ${standardFieldSpecs.shutdownBy.imp}" nowrap="nowrap">
			<tdsAngular:tooltipSpan field="${standardFieldSpecs.shutdownBy}">
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
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.shutdownDuration}" value="${asset.shutdownDuration}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${asset.startupBy}"/>
			<td class="valueNW ${standardFieldSpecs.startupBy.imp}" nowrap="nowrap" >
			<tdsAngular:tooltipSpan field="${standardFieldSpecs.startupBy}">
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
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.startupDuration}" value="${asset.startupDuration}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${asset.testingBy}"/>
			<td class="valueNW ${standardFieldSpecs.testingBy.imp}" nowrap="nowrap">
			<tdsAngular:tooltipSpan field="${standardFieldSpecs.testingBy}">
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
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.testingDuration}" value="${asset.testingDuration}" tooltipDataPlacement="bottom"/>
		</tr>
		<g:render template="/angular/common/customShow" model="[asset:asset,customs:customs]"></g:render>
	</tbody>
</table>

<table class="dates-info" >
	<tr>
		<td class="date-created">Date created: ${dateCreated}</td>
		<td class="last-updated">Last updated: ${lastUpdated}</td>
	</tr>
</table>
