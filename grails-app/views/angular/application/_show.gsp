<%@page defaultCodec="html" %>

<table class="planning-application-table">
	<tbody>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${applicationInstance.assetName}"/>

			<td colspan="3" style="font-weight:bold;" class="${standardFieldSpecs.assetName.imp}">
				<tds:tooltipSpan field="${standardFieldSpecs.assetName}">
					${applicationInstance.assetName}
				</tds:tooltipSpan>
			</td>
			<tds:inputLabel field="${standardFieldSpecs.description}" value="${applicationInstance.description}"/>
			<td colspan="3" class="${standardFieldSpecs.description.imp}" >
				<tds:tooltipSpan field="${standardFieldSpecs.description}" tooltipDataPlacement="bottom">
					${applicationInstance.description}
				</tds:tooltipSpan>
			</td>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.sme}" value="${applicationInstance.sme}"/>
			<td class="valueNW ${standardFieldSpecs.sme.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme.tip?:standardFieldSpecs.sme.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
                </span>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.environment}" value="${applicationInstance.environment}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.sme2}" value="${applicationInstance.sme2}"/>
			<td class="valueNW ${standardFieldSpecs.sme2.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme2.tip?: standardFieldSpecs.sme2.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
				</span>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.criticality}" value="${applicationInstance.criticality}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.appOwner}" value="${applicationInstance.appOwner}"/>
			<td class="valueNW ${standardFieldSpecs.appOwner.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appOwner.tip?: standardFieldSpecs.appOwner.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
				</span>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.moveBundle}" value="${applicationInstance.moveBundle}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.license}" value="${applicationInstance.license}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${applicationInstance.planStatus}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${applicationInstance.retireDate}"/>
			<td class="valueNW ${standardFieldSpecs.retireDate.imp}">
			<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
				{{'${applicationInstance?.retireDate}' | date:'dd/MM/yy' }}
			</tds:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.validation}" value="${applicationInstance.validation}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.testProc}" value="${applicationInstance.testProc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${applicationInstance.maintExpDate}"/>
			<td class="valueNW ${standardFieldSpecs.maintExpDate.imp}">
			<tdsAngular:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
				{{'${applicationInstance?.maintExpDate}' | date:'dd/MM/yy' }}
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.latency}" value="${applicationInstance.latency}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.startupProc}" value="${applicationInstance.startupProc}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.url}" value="${applicationInstance.url}"/>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.shutdownBy}" value="${applicationInstance.shutdownBy}"/>
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
			<g:if test="${applicationInstance.shutdownFixed ==1 }">
				<input type="checkbox" id="shutdownFixedShowId" disabled="disabled" name="shutdownFixed" checked="checked"/>
				<label for="shutdownFixedId" >Fixed</label>
			</g:if>
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.shutdownDuration}" value="${applicationInstance.shutdownDuration}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tdsAngular:inputLabel field="${standardFieldSpecs.startupBy}" value="${applicationInstance.startupBy}"/>
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
			<g:if test="${applicationInstance.startupFixed ==1 }">
				<input type="checkbox" id="startupFixedShowId" disabled="disabled" name="startupFixed" value="${applicationInstance.startupFixed}" checked="checked"/>
				<label for="startupFixedId" >Fixed</label>
			</g:if>
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}"/>
			<tdsAngular:inputLabel field="${standardFieldSpecs.testingBy}" value="${applicationInstance.testingBy}"/>
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
			<g:if test="${applicationInstance.testingFixed ==1 }">
				<input type="checkbox" id="testingFixedShowId" disabled="disabled" name="testingFixed" checked="checked" />
				<label for="testingFixedId" >Fixed</label>
			</g:if>
			</tdsAngular:tooltipSpan>
			</td>
			<tdsAngular:showLabelAndField field="${standardFieldSpecs.testingDuration}" value="${applicationInstance.testingDuration}" tooltipDataPlacement="bottom"/>
		</tr>
		<g:render template="assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs]"></g:render>
	</tbody>
</table>

<table class="dates-info" >
	<tr>
		<td class="date-created">Date created: ${applicationInstance.dateCreated}</td>
		<td class="last-updated">Last updated: ${applicationInstance.lastUpdated}</td>
	</tr>
</table>
