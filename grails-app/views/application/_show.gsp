<%@page defaultCodec="html" %>

<table class="planning-application-table">
	<tbody>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.assetName}"/>
			<td colspan="3" style="font-weight:bold;" class="${standardFieldSpecs.validation.imp}">${applicationInstance.assetName}</td>
			
			<tds:inputLabel field="${standardFieldSpecs.description}"/>
			<td colspan="3" class="${standardFieldSpecs.validation.imp}" >${applicationInstance.description}</td>
		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appAccess}" fieldValue="${applicationInstance.assetType}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.supportType}" fieldValue="${applicationInstance.supportType}"/>
			
			<tds:showLabelAndField field="${standardFieldSpecs.appFunction}" fieldValue="${applicationInstance.appFunction}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.userCount}" fieldValue="${applicationInstance.userCount}"/>
			
		</tr>
		<tr>
			
			<tds:showLabelAndField field="${standardFieldSpecs.appVendor}" fieldValue="${applicationInstance.appVendor}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.sme}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
				</span>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.environment}" fieldValue="${applicationInstance.environment}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.userLocations}" fieldValue="${applicationInstance.userLocations}"/>

		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appVersion}" fieldValue="${applicationInstance.appVersion}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.sme2}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
				</span>
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.criticality}" fieldValue="${applicationInstance.criticality}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.useFrequency}" fieldValue="${applicationInstance.useFrequency}"/>

		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appTech}" fieldValue="${applicationInstance.appTech}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.appOwner}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
				</span>
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.moveBundle}" fieldValue="${applicationInstance.moveBundle}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.drRpoDesc}" fieldValue="${applicationInstance.drRpoDesc}"/>
		
		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appSource}" fieldValue="${applicationInstance.appSource}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.businessUnit}" fieldValue="${applicationInstance.businessUnit}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.planStatus}" fieldValue="${applicationInstance.planStatus}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.drRtoDesc}" fieldValue="${applicationInstance.drRtoDesc}"/>
		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.license}" fieldValue="${applicationInstance.license}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.retireDate}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<tds:convertDate date="${applicationInstance?.retireDate}" />
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.validation}" fieldValue="${applicationInstance.validation}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.testProc}" fieldValue="${applicationInstance.testProc}"/>

		</tr>
		<tr>
			<td></td>
			<td></td>
			
			<tds:inputLabel field="${standardFieldSpecs.maintExpDate}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<tds:convertDate date="${applicationInstance?.maintExpDate}" format="12hrs" />
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.latency}" fieldValue="${applicationInstance.latency}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.startupProc}" fieldValue="${applicationInstance.startupProc}"/>

		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.url}" fieldValue="${applicationInstance.url}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" fieldValue="${applicationInstance.externalRefId}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.shutdownBy}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}" nowrap="nowrap">
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

			<tds:showLabelAndField field="${standardFieldSpecs.shutdownDuration}" fieldValue="${applicationInstance.shutdownDuration}"/>

		</tr>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.startupBy}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}" nowrap="nowrap" >
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
			
			<tds:showLabelAndField field="${standardFieldSpecs.startupDuration}" fieldValue="${applicationInstance.startupDuration}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.testingBy}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}" nowrap="nowrap">
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
			
			<tds:showLabelAndField field="${standardFieldSpecs.testingDuration}" fieldValue="${applicationInstance.testingDuration}"/>

		</tr>
		<g:render template="../assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs]"></g:render>
	</tbody>
</table>