<%@page defaultCodec="html" %>

<table class="planning-application-table">
	<tbody>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${applicationInstance.assetName}"/>
			<td colspan="3" style="font-weight:bold;" class="${standardFieldSpecs.validation.imp}">${applicationInstance.assetName}</td>
			
			<tds:inputLabel field="${standardFieldSpecs.description}" value="${applicationInstance.description}"/>
			<td colspan="3" class="${standardFieldSpecs.validation.imp}" >${applicationInstance.description}</td>
		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appAccess}" value="${applicationInstance.assetType}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}"/>
			
			<tds:showLabelAndField field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}"/>
			
		</tr>
		<tr>
			
			<tds:showLabelAndField field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.sme}" value="${applicationInstance.sme}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme}" />
				</span>
			</td>

			<tds:showLabelAndField field="${standardFieldSpecs.environment}" value="${applicationInstance.environment}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}"/>

		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.sme2}" value="${applicationInstance.sme2}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.sme2?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.sme2}" />
				</span>
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.criticality}" value="${applicationInstance.criticality}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}"/>

		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.appOwner}" value="${applicationInstance.appOwner}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<span class="clickableText" onClick="Person.showPersonDialog(${applicationInstance.appOwner?.id},'generalInfoShow')">
					<tds:nameAndCompany client="${client}" person="${applicationInstance.appOwner}" />
				</span>
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.moveBundle}" value="${applicationInstance.moveBundle}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}"/>
		
		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${applicationInstance.planStatus}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}"/>
		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.license}" value="${applicationInstance.license}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${applicationInstance.retireDate}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<tds:convertDate date="${applicationInstance?.retireDate}" />
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.validation}" value="${applicationInstance.validation}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.testProc}" value="${applicationInstance.testProc}"/>

		</tr>
		<tr>
			<td></td>
			<td></td>
			
			<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${applicationInstance.maintExpDate}"/>
			<td class="valueNW ${standardFieldSpecs.validation.imp}">
				<tds:convertDate date="${applicationInstance?.maintExpDate}" format="12hrs" />
			</td>
			
			<tds:showLabelAndField field="${standardFieldSpecs.latency}" value="${applicationInstance.latency}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.startupProc}" value="${applicationInstance.startupProc}"/>

		</tr>
		<tr>
			<tds:showLabelAndField field="${standardFieldSpecs.url}" value="${applicationInstance.url}"/>

			<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.shutdownBy}" value="${applicationInstance.shutdownBy}"/>
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

			<tds:showLabelAndField field="${standardFieldSpecs.shutdownDuration}" value="${applicationInstance.shutdownDuration}"/>

		</tr>
		<tr>
			<tds:inputLabel field="${standardFieldSpecs.startupBy}" value="${applicationInstance.startupBy}"/>
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
			
			<tds:showLabelAndField field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}"/>
			
			<tds:inputLabel field="${standardFieldSpecs.testingBy}" value="${applicationInstance.testingBy}"/>
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
			
			<tds:showLabelAndField field="${standardFieldSpecs.testingDuration}" value="${applicationInstance.testingDuration}"/>

		</tr>
		<g:render template="../assetEntity/customShow" model="[assetEntity:applicationInstance,customs:customs]"></g:render>
	</tbody>
</table>