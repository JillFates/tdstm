<%@page defaultCodec="html" %>
<table id="detailsTable" class="tds-detail-list">
	<tbody id="detailsBody" class="one-column">
		<%-- NOTE: Specifying the order property will cause the element to be ordered in the table. 
		Not specifying the property will cause the element to be positioned last.
		Order is specified in multiples of 5 to allow for easy reordering in the future. --%>
		<tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}"/>
		<tds:clrRowDetail style="order: 20" field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}"/>
		<tds:clrRowDetail style="order: 25" field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}"/>
		<tds:clrRowDetail style="order: 30" field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}" tooltipDataPlacement="bottom"/>
		<tds:clrRowDetail style="order: 35" field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}"/>
		<tr style="order: 40">
			<tds:clrInputLabel field="${standardFieldSpecs.sme}" value="${applicationInstance.sme}"/>

			<td>
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme.tip?:standardFieldSpecs.sme.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
				</span>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 45" field="${standardFieldSpecs.environment}" value="${applicationInstance.environment}"/>
		<tds:clrRowDetail style="order: 50" field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}" tooltipDataPlacement="bottom"/>
		<tds:clrRowDetail style="order: 55" field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}"/>
		<tr style="order: 60">
			<tds:clrInputLabel field="${standardFieldSpecs.sme2}" value="${applicationInstance.sme2}"/>

			<td>
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme2.tip?: standardFieldSpecs.sme2.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
				</span>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 65" field="${standardFieldSpecs.criticality}" value="${applicationInstance.criticality}"/>
		<tds:clrRowDetail style="order: 70" field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}" tooltipDataPlacement="bottom"/>
		<tds:clrRowDetail style="order: 75" field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}"/>
		<tr style="order: 80">
			<tds:clrInputLabel field="${standardFieldSpecs.appOwner}" value="${applicationInstance.appOwner}"/>
			<td>
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appOwner.tip?: standardFieldSpecs.appOwner.label}">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
				</span>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 85" field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}" tooltipDataPlacement="bottom"/>
		<tds:clrRowDetail style="order: 90" field="${standardFieldSpecs.license}" value="${applicationInstance.license}"/>
		<tds:clrRowDetail style="order: 95" field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}"/>
		<tds:clrRowDetail style="order: 100" field="${standardFieldSpecs.planStatus}" value="${applicationInstance.planStatus}"/>
		<tds:clrRowDetail style="order: 105" field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}" tooltipDataPlacement="bottom"/>
		<tr style="order: 110">
			<tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${applicationInstance.retireDate}"/>
			<td>
				<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
					<tds:convertDate date="${applicationInstance?.retireDate}" />
				</tds:tooltipSpan>
			</td>
		</tr style="order: 115">
		<tds:clrRowDetail style="order: 120" field="${standardFieldSpecs.validation}" value="${applicationInstance.validation}"/>
		<tds:clrRowDetail style="order: 125" field="${standardFieldSpecs.testProc}" value="${applicationInstance.testProc}" tooltipDataPlacement="bottom"/>
		<tr style="order: 130">
			<tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${applicationInstance.maintExpDate}"/>
			<td>
				<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
					<tds:convertDate date="${applicationInstance?.maintExpDate}" format="12hrs" />
				</tds:tooltipSpan>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 135" field="${standardFieldSpecs.latency}" value="${applicationInstance.latency}"/>
		<tds:clrRowDetail style="order: 140" field="${standardFieldSpecs.startupProc}" value="${applicationInstance.startupProc}" tooltipDataPlacement="bottom"/>
		<tr style="order: 145">
			<tds:clrInputLabel field="${standardFieldSpecs.url}" value="${applicationInstance.url}" />
			<td>
				<tds:tooltipSpan field="${standardFieldSpecs.url}" tooltipDataPlacement="bottom">
				  <tds:textAsLink  text="${applicationInstance.url}" target="_new"/>
				</tds:tooltipSpan>
			</td>
		</tr>
		<tds:clrRowDetail style="order: 150" field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}"/>
		<tr style="order: 155">
			<tds:clrInputLabel field="${standardFieldSpecs.shutdownBy}" value="${applicationInstance.shutdownBy}"/>
			<td>
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
		</tr>
		<tds:clrRowDetail style="order: 160" field="${standardFieldSpecs.shutdownDuration}" value="${applicationInstance.shutdownDuration}" tooltipDataPlacement="bottom"/>
		<tr style="order: 165">
			<tds:clrInputLabel field="${standardFieldSpecs.startupBy}" value="${applicationInstance.startupBy}"/>
			<td>
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
		</tr>
		<tds:clrRowDetail style="order: 170" field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}"/>
		<tr style="order: 175">
			<tds:clrInputLabel field="${standardFieldSpecs.testingBy}" value="${applicationInstance.testingBy}"/>
			<td>
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
		</tr>
		<tds:clrRowDetail style="order: 180" field="${standardFieldSpecs.testingDuration}" value="${applicationInstance.testingDuration}" tooltipDataPlacement="bottom"/>

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

