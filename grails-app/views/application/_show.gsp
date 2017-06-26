<%@page defaultCodec="html" %>

<table class="planning-application-table">
	<tbody>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.assetName}"/>
			<td colspan="3" style="font-weight:bold;" class="">${applicationInstance.assetName}</td>
			
			<tds:inputLabel field="${standardFieldSpecs.description}"/>
			<td colspan="3" class="" >${applicationInstance.description}</td>
		</tr>
		<tr>
			<tds:inputLabelAndField field="${standardFieldSpecs.appAccess}" fieldValue="${applicationInstance.assetType}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" fieldValue="${applicationInstance.supportType}"/>
			
			<tds:inputLabelAndField field="${standardFieldSpecs.appFunction}" fieldValue="${applicationInstance.appFunction}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.userCount}" fieldValue="${applicationInstance.userCount}"/>
			
		</tr>
		<tr>
			
			<tds:inputLabelAndField field="${standardFieldSpecs.appVendor}" fieldValue="${applicationInstance.appVendor}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.sme}"/>
			<td class="valueNW">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
				</span>
			</td>

			<tds:inputLabelAndField field="${standardFieldSpecs.environment}" fieldValue="${applicationInstance.environment}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.userLocations}" fieldValue="${applicationInstance.userLocations}"/>

		</tr>
		<tr>
			<tds:inputLabelAndField field="${standardFieldSpecs.appVersion}" fieldValue="${applicationInstance.appVersion}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.sme2}"/>
			<td class="valueNW">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
				</span>
			</td>
			
			<tds:inputLabelAndField field="${standardFieldSpecs.criticality}" fieldValue="${applicationInstance.criticality}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.useFrequency}" fieldValue="${applicationInstance.useFrequency}"/>

		</tr>
		<tr>
			<tds:inputLabelAndField field="${standardFieldSpecs.appTech}" fieldValue="${applicationInstance.appTech}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.appOwner}"/>
			<td class="valueNW">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
				</span>
			</td>
			
			<tds:inputLabelAndField field="${standardFieldSpecs.moveBundle}" fieldValue="${applicationInstance.moveBundle}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" fieldValue="${applicationInstance.drRpoDesc}"/>
		
		</tr>
		<tr>
			<tds:inputLabelAndField field="${standardFieldSpecs.appSource}" fieldValue="${applicationInstance.appSource}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.businessUnit}" fieldValue="${applicationInstance.businessUnit}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.planStatus}" fieldValue="${applicationInstance.planStatus}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" fieldValue="${applicationInstance.drRtoDesc}"/>
		</tr>
		<tr>
			<tds:inputLabelAndField field="${standardFieldSpecs.license}" fieldValue="${applicationInstance.license}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.retireDate}"/>
			<td class="valueNW">
				<tds:convertDate date="${applicationInstance?.retireDate}" />
			</td>
			
			<tds:inputLabelAndField field="${standardFieldSpecs.validation}" fieldValue="${applicationInstance.validation}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.testProc}" fieldValue="${applicationInstance.testProc}"/>

		</tr>
		<tr>
			<td></td>
			<td></td>
			
			<tds:inputLabel field="${standardFieldSpecs.maintExpDate}"/>
			<td class="valueNW">
				<tds:convertDate date="${applicationInstance?.maintExpDate}" format="12hrs" />
			</td>
			
			<tds:inputLabelAndField field="${standardFieldSpecs.latency}" fieldValue="${applicationInstance.latency}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.startupProc}" fieldValue="${applicationInstance.startupProc}"/>

		</tr>
		<tr>
			<tds:inputLabelAndField field="${standardFieldSpecs.url}" fieldValue="${applicationInstance.url}"/>

			<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" fieldValue="${applicationInstance.externalRefId}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.shutdownBy}"/>
			<td class="valueNW" nowrap="nowrap">
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

			<tds:inputLabelAndField field="${standardFieldSpecs.shutdownDuration}" fieldValue="${applicationInstance.shutdownDuration}"/>

		</tr>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.startupBy}"/>
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
			
			<tds:inputLabelAndField field="${standardFieldSpecs.startupDuration}" fieldValue="${applicationInstance.startupDuration}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.testingBy}"/>
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
			
			<tds:inputLabelAndField field="${standardFieldSpecs.testingDuration}" fieldValue="${applicationInstance.testingDuration}"/>

		</tr>
		<g:render template="../assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs]"></g:render>
	</tbody>
</table>