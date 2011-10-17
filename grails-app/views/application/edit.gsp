

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <g:javascript src="asset.tranman.js" />


		<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
		
        <title>Edit Application</title>
    </head>
    <body>
       <div class="body">
            <g:form method="post" >
                <input type="hidden" name="id" value="${applicationInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetName">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${applicationInstance.assetName}"/>
                                </td>
                                <td valign="top" class="name">
                                  Desc:
                                </td>
                                <td>
                                    <input type="text" id="name" name="Desc" value="This Application Support the XYZ Business"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="assetType">Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'assetType','errors')}">
                                    <input type="text" id="name" name="assetType" value="${applicationInstance.assetType}"/>
                                </td>
                               <td valign="top" class="name">
                                    <label for="supportType">Support:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'supportType','errors')}">
                                    <input type="text" id="name" name="supportType" value="${applicationInstance.supportType}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="appFunction">Function:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appFunction','errors')}">
                                    <input type="text" id="name" name="appFunction" value="${applicationInstance.appFunction}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="userConcurrent">ConCurren:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'userConcurrent','errors')}">
                                    <input type="text" id="name" name="userConcurrent" value="${applicationInstance.userConcurrent}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appVendor">Vendor:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appVendor','errors')}">
                                    <input type="text" id="name" name="appVendor" value="${applicationInstance.appVendor}"/>
                                </td>
                               <td valign="top" class="name">
                                    <label for="sme">SME1:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'sme','errors')}">
                                    <input type="text" id="name" name="sme" value="${applicationInstance.sme}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="environment">Enviorn:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'environment','errors')}">
                                    <input type="text" id="name" name="environment" value="${applicationInstance.environment}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="userLocations">User Loc:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'userLocations','errors')}">
                                    <input type="text" id="name" name="userLocations" value="${applicationInstance.userLocations}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appVersion">Version:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appVersion','errors')}">
                                    <input type="text" id="name" name="appVersion" value="${applicationInstance.appVersion}"/>
                                </td>
                               <td valign="top" class="name">
                                    <label for="sme2">SME2:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'sme2','errors')}">
                                    <input type="text" id="name" name="sme2" value="${applicationInstance.sme2}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="criticality">criticality:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'criticality','errors')}">
                                    <input type="text" id="name" name="criticality" value="${applicationInstance.criticality}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="userConcurrent">ConCurren:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'userConcurrent','errors')}">
                                    <input type="text" id="name" name="userConcurrent" value="${applicationInstance.userConcurrent}"/>
                                </td>
                            </tr>
                             <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appTech">Tech.:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appTech','errors')}">
                                    <input type="text" id="name" name="appTech" value="${applicationInstance.appTech}"/>
                                </td>
                               <td valign="top" class="name">
                                    <label for="businessUnit">Bus Unit:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'businessUnit','errors')}">
                                    <input type="text" id="name" name="businessUnit" value="${applicationInstance.businessUnit}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="moveBundle">Bundle:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'moveBundle','errors')}">
                                    <input type="text" id="name" name="moveBundle" value="${applicationInstance.moveBundle}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="useFrequency">Use Freq:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'userConcurrent','errors')}">
                                    <input type="text" id="name" name="useFrequency" value="${applicationInstance.userConcurrent}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appSource">Source:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'appSource','errors')}">
                                    <input type="text" id="name" name="appSource" value="${applicationInstance.appSource}"/>
                                </td>
                               <td valign="top" class="name">
                                    <label for="owner">Owner:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'owner','errors')}">
                                    <input type="text" id="name" name="owner" value="${applicationInstance.owner}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="planStatus">Plan Status:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'planStatus','errors')}">
                                    <input type="text" id="name" name="planStatus" value="${applicationInstance.planStatus}"/>
                                </td>
                                <td valign="top" class="name">
                                    <label for="useFrequency">DR RPO:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'drRpoDesc','errors')}">
                                    <input type="text" id="name" name="useFrequency" value="${applicationInstance.drRpoDesc}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="license">License:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'license','errors')}">
                                    <input type="text" id="name" name="license" value="${applicationInstance.license}"/>
                                </td>
                               <td valign="top" class="name">
                                    Retire:
                                </td>
                                <td><input type="text" id="name" name="Retire" value="Retire"/>
                                </td>
                                <td>&nbsp;</td>
						        <td>&nbsp;</td>
                                <td valign="top" class="name">
                                    <label for="drRtoDesc">DR RTO:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'drRtoDesc','errors')}">
                                    <input type="text" id="name" name="drRtoDesc" value="${applicationInstance.drRtoDesc}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td>&nbsp;</td>
						        <td>&nbsp;</td>
                               <td valign="top" class="name">
                                   Main Exp.
                                </td>
                                <td><input type="text" id="name" name="name" value="12/1/2011"/>
                                </td>
                                <td>&nbsp;</td>
						        <td>&nbsp;</td>
                                <td valign="top" class="name">
                                    <label for="drRtoDesc">DR RTO:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'drRtoDesc','errors')}">
                                    <input type="text" id="name" name="drRtoDesc" value="${applicationInstance.drRtoDesc}"/>
                                </td>
                            </tr>
                    </table>
                </div>
       <div>
			<div style="float: left; margin-bottom: 10px;">
				<h1>Supports:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
						<th>Frequency</th>
						<th>Asset</th>
						<th>Type</th>
						<th>Status</th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${supportAssets}" var="support">
						<tr>
							<td>${support?.comment}</td>
							<td>${support?.asset}</td>
							<td><g:select id="selectId" value=${support.type}
									from="${AssetDependency.constraints.type.inList}" />
							</td>
							<td><g:select id="typeId" value=${support.status}
									from="${AssetDependency.constraints.status.inList}" />
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
			</div>
			<div style="float: left; margin-left: 10px;">
				<h1>Is dependent on:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
						<th>Frequency</th>
						<th>Asset</th>
						<th>Type</th>
						<th>Status</th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${dependentAssets}" var="dependent">
						<tr>
							<td>${dependent.dataFlowFreq}</td>
							<td>${dependent.dependent}</td>
							<td>${support.type}</td>
							<td>${support.status}</td>
						</tr>
					</g:each>
					</tbody>
				</table>
			</div>
		</div>
        <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
	<script type="text/javascript">
	    $('#assetMenu').show();
	    $('#reportsMenu').hide();
    </script>
    </body>
</html>
