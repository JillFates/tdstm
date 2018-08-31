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
			<tds:showLabelAndField field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}" tooltipDataPlacement="bottom"/>
		</tr>

		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}"/>

			<tds:inputLabel field="${standardFieldSpecs.sme}" value="${applicationInstance.sme}"/>

			<td class="valueNW ${standardFieldSpecs.sme.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme.tip?:standardFieldSpecs.sme.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
				</span>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.environment}" value="${applicationInstance.environment}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}" tooltipDataPlacement="bottom"/>

		</tr>

		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}"/>

			<tds:inputLabel field="${standardFieldSpecs.sme2}" value="${applicationInstance.sme2}"/>

			<td class="valueNW ${standardFieldSpecs.sme2.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme2.tip?: standardFieldSpecs.sme2.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
				</span>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.criticality}" value="${applicationInstance.criticality}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}" tooltipDataPlacement="bottom"/>

		</tr>

		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}"/>

			<tds:inputLabel field="${standardFieldSpecs.appOwner}" value="${applicationInstance.appOwner}"/>

			<td class="valueNW ${standardFieldSpecs.appOwner.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appOwner.tip?: standardFieldSpecs.appOwner.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
				</span>
			</td>

			<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
				<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
					${standardFieldSpecs.moveBundle.label} : Dep. Group
				</label>
			</td>
			<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">
				<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}">
					${applicationInstance?.moveBundle}
				</tds:tooltipSpan>
				<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${applicationInstance.assetName}"/>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}" tooltipDataPlacement="bottom"/>

		</tr>

		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.license}" value="${applicationInstance.license}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${applicationInstance.planStatus}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}" tooltipDataPlacement="bottom"/>
		</tr>

		<tr>
			<td></td>
			<td></td>

			<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${applicationInstance.retireDate}"/>

			<td class="valueNW ${standardFieldSpecs.retireDate.imp}">
			<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
				<tds:convertDate date="${applicationInstance?.retireDate}" />
			</tds:tooltipSpan>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.validation}" value="${applicationInstance.validation}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.testProc}" value="${applicationInstance.testProc}" tooltipDataPlacement="bottom"/>

		</tr>
		<tr>
			<td></td>
			<td></td>

			<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${applicationInstance.maintExpDate}"/>

			<td class="valueNW ${standardFieldSpecs.maintExpDate.imp}">
			<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
				<tds:convertDate date="${applicationInstance?.maintExpDate}" format="12hrs" />
			</tds:tooltipSpan>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.latency}" value="${applicationInstance.latency}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.startupProc}" value="${applicationInstance.startupProc}" tooltipDataPlacement="bottom"/>

		</tr>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.url}" value="${applicationInstance.url}" />
			<td class="valueNW ${standardFieldSpecs.url.imp}" >
				<tds:tooltipSpan field="${standardFieldSpecs.url}" tooltipDataPlacement="bottom">
				  <tds:textAsLink  text="${applicationInstance.url}" target="_new"/>
				</tds:tooltipSpan>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}"/>

			<tds:inputLabel field="${standardFieldSpecs.shutdownBy}" value="${applicationInstance.shutdownBy}"/>

			<td class="valueNW ${standardFieldSpecs.shutdownBy.imp}" nowrap="nowrap">
			<tds:tooltipSpan field="${standardFieldSpecs.shutdownBy}">
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
			</tds:tooltipSpan>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.shutdownDuration}" value="${applicationInstance.shutdownDuration}" tooltipDataPlacement="bottom"/>

		</tr>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.startupBy}" value="${applicationInstance.startupBy}"/>
			<td class="valueNW ${standardFieldSpecs.startupBy.imp}" nowrap="nowrap" >
			<tds:tooltipSpan field="${standardFieldSpecs.startupBy}">
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
			</tds:tooltipSpan>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}"/>

			<tds:inputLabel field="${standardFieldSpecs.testingBy}" value="${applicationInstance.testingBy}"/>
			<td class="valueNW ${standardFieldSpecs.testingBy.imp}" nowrap="nowrap">
			<tds:tooltipSpan field="${standardFieldSpecs.testingBy}">
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
			</tds:tooltipSpan>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.testingDuration}" value="${applicationInstance.testingDuration}" tooltipDataPlacement="bottom"/>

		</tr>
		<g:render template="../assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs]"></g:render>
		<g:render template="/comment/assetTagsShow"></g:render>
	</tbody>
</table>

<div class="dates-info-container">
	<table class="dates-info" >
		<tr>
			<td class="date-created">Date created: ${dateCreated}</td>
			<td class="last-updated">Last updated: ${lastUpdated}</td>
		</tr>
	</table>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		EntityCrud.loadAssetTags(${applicationInstance?.id});
	});
</script>

