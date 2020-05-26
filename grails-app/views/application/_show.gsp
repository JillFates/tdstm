<%@page defaultCodec="html" %>

<table id="detailsTable" class="planning-application-table">
	<tbody id="detailsBody">
		<%-- NOTE: Specifying the order property will cause the element to be ordered in the table. 
		Not specifying the property will cause the element to be positioned last.
		Order is specified in multiples of 5 to allow for easy reordering in the future. --%>
		<tr>
			<tds:clrInputLabel style="order: 15" field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}"/>
			<tds:labelForShowField style="order: 15" field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}"/>
			<tds:clrInputLabel style="order: 20" field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}"/>
			<tds:labelForShowField style="order: 20" field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}"/>
			<tds:clrInputLabel style="order: 25" field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}"/>
			<tds:labelForShowField style="order: 25" field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}"/>
			<tds:clrInputLabel style="order: 30" field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 30" field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr style="order: 40">
			<tds:clrInputLabel style="order: 35" field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}"/>
			<tds:labelForShowField field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}"/>
			<tds:clrInputLabel field="${standardFieldSpecs.sme}" value="${applicationInstance.sme}"/>
			<tds:labelForShowField field="${standardFieldSpecs.sme}" value="${applicationInstance.sme}"/>
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme.tip?:standardFieldSpecs.sme.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
				</span>
			<tds:clrInputLabel style="order: 45" field="${standardFieldSpecs.environment}" value="${applicationInstance.environment}"/>
			<tds:labelForShowField style="order: 45" field="${standardFieldSpecs.environment}" value="${applicationInstance.environment}"/>
			<tds:clrInputLabel style="order: 50" field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 50" field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}" tooltipDataPlacement="bottom"/>
		</tr>
		<tr>
			<tds:clrInputLabel style="order: 55" field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}"/>
			<tds:labelForShowField style="order: 55" field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}"/>
			<tds:clrInputLabel field="${standardFieldSpecs.sme2}" value="${applicationInstance.sme2}" style="order: 60" />
			<tds:labelForShowField field="${standardFieldSpecs.sme2}" value="${applicationInstance.sme2}" style="order: 60" />
			<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme2.tip?: standardFieldSpecs.sme2.label}" style="order: 60">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
			</span>
			<tds:clrInputLabel style="order: 65" field="${standardFieldSpecs.criticality}" value="${applicationInstance.criticality}"/>
			<tds:labelForShowField style="order: 65" field="${standardFieldSpecs.criticality}" value="${applicationInstance.criticality}"/>
			<tds:clrInputLabel style="order: 70" field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 70" field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}" tooltipDataPlacement="bottom"/>
		</tr>
		
		<tr>
			<tds:clrInputLabel style="order: 75" field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}"/>
			<tds:labelForShowField style="order: 75" field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}"/>
			<tds:clrInputLabel field="${standardFieldSpecs.appOwner}" value="${applicationInstance.appOwner}" style="order: 80"/>
			<tds:labelForShowField field="${standardFieldSpecs.appOwner}" value="${applicationInstance.appOwner}" style="order: 80"/>
			<span style="order: 80" class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appOwner.tip?: standardFieldSpecs.appOwner.label}">
				<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
			</span>
			<td colspan="2"></td>
			<tds:clrInputLabel style="order: 85" field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 85" field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}" tooltipDataPlacement="bottom"/>
		</tr>

		<tr>
			<tds:clrInputLabel style="order: 90" field="${standardFieldSpecs.license}" value="${applicationInstance.license}"/>
			<tds:labelForShowField style="order: 90" field="${standardFieldSpecs.license}" value="${applicationInstance.license}"/>
			<tds:clrInputLabel style="order: 95" field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}"/>
			<tds:labelForShowField style="order: 95" field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}"/>
			<tds:clrInputLabel style="order: 100" field="${standardFieldSpecs.planStatus}" value="${applicationInstance.planStatus}"/>
			<tds:labelForShowField style="order: 100" field="${standardFieldSpecs.planStatus}" value="${applicationInstance.planStatus}"/>
			<tds:clrInputLabel style="order: 105" field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 105" field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}" tooltipDataPlacement="bottom"/>
		</tr>

		<tr>
			<td colspan="2"></td>
			<tds:clrInputLabel style="order: 110" field="${standardFieldSpecs.retireDate}" value="${applicationInstance.retireDate}"/>
			<tds:labelForShowField style="order: 110" field="${standardFieldSpecs.retireDate}" value="${applicationInstance.retireDate}"/>
				<tds:tooltipSpan style="order: 110" field="${standardFieldSpecs.retireDate}">
					<tds:convertDate date="${applicationInstance?.retireDate}" />
				</tds:tooltipSpan>
			<tds:clrInputLabel style="order: 120" field="${standardFieldSpecs.validation}" value="${applicationInstance.validation}"/>
			<tds:labelForShowField style="order: 120" field="${standardFieldSpecs.validation}" value="${applicationInstance.validation}"/>
			<tds:clrInputLabel style="order: 125" field="${standardFieldSpecs.testProc}" value="${applicationInstance.testProc}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 125" field="${standardFieldSpecs.testProc}" value="${applicationInstance.testProc}" tooltipDataPlacement="bottom"/>
		</tr>

		<tr>
			<td colspan="2"></td>
			<tds:clrInputLabel style="order: 130" field="${standardFieldSpecs.maintExpDate}" value="${applicationInstance.maintExpDate}"/>
			<tds:labelForShowField style="order: 130" field="${standardFieldSpecs.maintExpDate}" value="${applicationInstance.maintExpDate}"/>
			<tds:tooltipSpan style="order: 130" field="${standardFieldSpecs.maintExpDate}">
				<tds:convertDate style="order: 130" date="${applicationInstance?.maintExpDate}" format="12hrs" />
			</tds:tooltipSpan>
			<tds:clrInputLabel style="order: 135" field="${standardFieldSpecs.latency}" value="${applicationInstance.latency}"/>
			<tds:labelForShowField style="order: 135" field="${standardFieldSpecs.latency}" value="${applicationInstance.latency}"/>
			<tds:clrInputLabel style="order: 140" field="${standardFieldSpecs.startupProc}" value="${applicationInstance.startupProc}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 140" field="${standardFieldSpecs.startupProc}" value="${applicationInstance.startupProc}" tooltipDataPlacement="bottom"/>
		</tr>

		<tr style="order: 145">
			<tds:clrInputLabel style="order: 145" field="${standardFieldSpecs.url}" value="${applicationInstance.url}" />
			<tds:labelForShowField style="order: 145" field="${standardFieldSpecs.url}" value="${applicationInstance.url}" />
				<tds:tooltipSpan style="order: 145" field="${standardFieldSpecs.url}" tooltipDataPlacement="bottom">
				  <tds:textAsLink style="order: 145" text="${applicationInstance.url}" target="_new"/>
				</tds:tooltipSpan>
			<tds:clrInputLabel style="order: 150" field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}"/>
			<tds:labelForShowField style="order: 150" field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}"/>
			<tds:clrInputLabel style="order: 155" field="${standardFieldSpecs.shutdownBy}" value="${applicationInstance.shutdownBy}"/>
			<tds:labelForShowField style="order: 155" field="${standardFieldSpecs.shutdownBy}" value="${applicationInstance.shutdownBy}"/>
				<tds:tooltipSpan style="order: 155" field="${standardFieldSpecs.shutdownBy}">
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
			<tds:clrInputLabel style="order: 160" field="${standardFieldSpecs.shutdownDuration}" value="${applicationInstance.shutdownDuration}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 160" field="${standardFieldSpecs.shutdownDuration}" value="${applicationInstance.shutdownDuration}" tooltipDataPlacement="bottom"/>
		</tr>

		<tr>
			<tds:clrInputLabel style="order: 165" field="${standardFieldSpecs.startupBy}" value="${applicationInstance.startupBy}"/>
			<tds:labelForShowField style="order: 165" field="${standardFieldSpecs.startupBy}" value="${applicationInstance.startupBy}"/>			
				<tds:tooltipSpan style="order: 165" field="${standardFieldSpecs.startupBy}">
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
			<tds:clrInputLabel style="order: 170" field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}"/>
			<tds:labelForShowField style="order: 170" field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}"/>
			<tds:clrInputLabel style="order: 175" field="${standardFieldSpecs.testingBy}" value="${applicationInstance.testingBy}"/>
			<tds:labelForShowField style="order: 175" field="${standardFieldSpecs.testingBy}" value="${applicationInstance.testingBy}"/>
				<tds:tooltipSpan style="order: 175" field="${standardFieldSpecs.testingBy}">
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
			<tds:clrInputLabel style="order: 180" field="${standardFieldSpecs.testingDuration}" value="${applicationInstance.testingDuration}" tooltipDataPlacement="bottom"/>
			<tds:labelForShowField style="order: 180" field="${standardFieldSpecs.testingDuration}" value="${applicationInstance.testingDuration}" tooltipDataPlacement="bottom"/>
		</tr>
		<g:render template="/assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs]"></g:render>
		<%-- TODO: Move the asset tags outside of the table --> There is an issue with how they're rendered when this is done... --%>
		<g:render template="/comment/assetTagsShow" model="[tagAssetList: tagAssetList, tagAssetsFromServer: tagAssetsFromServer]"></g:render>
	</tbody>
</table>

<script type="text/javascript">
	$(document).ready(function() {
		EntityCrud.loadAssetTags(${applicationInstance?.id});
	});
</script>

