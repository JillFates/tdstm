<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<g:if test="${flash.message}">
<script type="text/javascript">
alert("${flash.message}")
</script>
<% flash.message = null %>
</g:if>
<script type="text/javascript">
$(document).ready(function() { 
	var assetType = "${assetEntity.assetType}"
	if(assetType=='Blade'){
		$(".bladeLabel").show()
		$(".rackLabel").hide()
		$(".vmLabel").hide()
	} else if(assetType=='VM') {
		$(".bladeLabel").hide()
		$(".rackLabel").hide()
		$(".vmLabel").show()
	} else {
		$(".bladeLabel").hide()
		$(".rackLabel").show()
		$(".vmLabel").hide()
	}
	var prefVal = '${prefValue}'

	if(prefVal == 'FALSE'){
		$(".resolved").hide()
	} else{
		$(".resolved").show()
	}
	$(".showAllChecked").click(function(){
		 var selected = $(this).is(":checked") ? '1' : '0'
		 showTask(selected)
	})
	changeDocTitle('${assetEntity.assetName}');
})
</script>
 	<g:form method="post">
 	<table style="border:0;width:1000px;">
		<tr>
			<td colspan="2">
			<div class="dialog" <tds:hasPermission permission='AssetEdit'>ondblclick="editEntity('${redirectTo}','Server', ${assetEntity?.id})" </tds:hasPermission> >
			<g:if test="${errors}">
				<div id="messageDivId" class="message">${errors}</div>
			</g:if>
					<table>
						<tbody>
							<tr  class="prop">
								<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name</label></td>
								<td style="font-weight:bold" class="${config.assetName}">${assetEntity.assetName}</td>
								<td class="label ${config.description}" nowrap="nowrap"><label for="description">Description</label></td>
								<td colspan="2" class="${config.description}">${assetEntity.description}</td>
								<td class="label_sm">Current</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr class="prop">
								<td class="label ${config.assetType}" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td class="valueNW ${config.assetType}">${assetEntity.assetType}</td>
								<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label></td>
								<td class="valueNW ${config.environment}">${assetEntity.environment}</td>
								<td class="label ${config.sourceLocation}" nowrap="nowrap"><label for="sourceLocation">Location</label></td>
								<td class="valueNW ${config.sourceLocation}">${assetEntity.sourceLocation}</td>
								<td class="valueNW ${config.targetLocation}">${assetEntity.targetLocation}</td>
							</tr>
							<tr class="prop">
								<td class="label ${config.manufacturer}" nowrap="nowrap"><label for="manufacturer">Manufacturer</label></td>
								<td class="valueNW ${config.manufacturer}"><a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a></td>
								<td class="label ${config.priority}" nowrap="nowrap"><label for="priority">Priority</label></td>
								<td class="valueNW ${config.priority}">${assetEntity.priority}</td>
								<td class="label ${config.sourceRoom}" nowrap="nowrap"><label for="sourceRoom">Room</label></td>
								<td class="valueNW ${config.sourceRoom}" >${assetEntity.roomSource?.roomName}</td>
								<td class="valueNW ${config.targetRoom}">${assetEntity.roomTarget?.roomName}</td>
							</tr>
							<tr class="prop">
								<td class="label ${config.model}" nowrap="nowrap"><label for="model">Model</label></td>
								<td class="valueNW ${config.model}"><a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
								  <g:if test="${assetEntity.model?.modelStatus!='valid'}"> <span style="color: red;"><b>?</b></span></g:if>
								</td>
								<td class="label ${config.ipAddress}" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td class="valueNW ${config.ipAddress}">${assetEntity.ipAddress}</td>
								<td class="label rackLabel ${config.sourceRack}"  nowrap="nowrap" id="rackId"><label for="sourceRackId">Rack/Cab</label></td>
								<td class="label bladeLabel ${config.sourceBladeChassis}" nowrap="nowrap" id="bladeId" style="display: none"><label for="sourceBladeChassisId">Blade</label></td>
								<td class="label vmLabel ${config.virtualHost}" style="display: none" class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label></td>

								<td class="rackLabel ${config.sourceRack}">${assetEntity.rackSource?.tag}</td>
								<td class="rackLabel ${config.targetRack}">${assetEntity.rackTarget?.tag}</td>

								<td class="bladeLabel ${config.sourceBladeChassis}" style="display: none">${assetEntity.sourceBladeChassis}</td>
								<td class="bladeLabel ${config.targetBladeChassis}" style="display: none" >${assetEntity.targetBladeChassis}</td>

								<td class="vmLabel ${config.virtualHost}" style="display: none">${assetEntity.virtualHost}</td>
								<td class="vmLabel" style="display: none"></td>
							</tr>
							<tr class="prop">
								<td class="label ${config.shortName}" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td class="valueNW ${config.shortName}">${assetEntity.shortName}</td>
								<td class="label ${config.os}" nowrap="nowrap"><label for="os">OS</label></td>
								<td class="valueNW ${config.os}">${assetEntity.os}</td>
								<td class="label ${config.sourceRackPosition}" nowrap="nowrap"><label for="sourceRack">Position</label></td>
								<td class="rackLabel valueNW ${config.sourceRackPosition}">${assetEntity.sourceRackPosition}</td>
								<td class="rackLabel valueNW ${config.targetRackPosition}">${assetEntity.targetRackPosition}</td>
								<td class="bladeLabel ${config.sourceBladePosition}" style="display: none" >${assetEntity.sourceBladePosition}</td>
								<td class="bladeLabel ${config.targetBladePosition}" style="display: none" >${assetEntity.targetBladePosition}</td>
							</tr>
							<tr class="prop">
								<td class="label ${config.serialNumber}" nowrap="nowrap"><label for="serialNumber">S/N</label></td>
								<td class="valueNW ${config.serialNumber}">${assetEntity.serialNumber}</td>
								<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support Type</label></td>
								<td class="valueNW ${config.supportType}">${assetEntity.supportType}</td>
								<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle / Dep. Group</label></td>
								<td class="valueNW ${config.moveBundle}">${assetEntity.moveBundle}${dependencyBundleNumber?' / ' : ''}${dependencyBundleNumber}</td>
								<td class="label ${config.size}" nowrap="nowrap"><label for="size">Size/Scale </label></td>
                                <td nowrap="nowrap" class="sizeScale ${config.size}">
                                    ${assetEntity.size} ${assetEntity.scale?.value()}
                                </td>
							</tr>
							<tr class="prop">
								<td class="label ${config.assetTag}" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td class="valueNW ${config.assetTag}">${assetEntity.assetTag}</td>
								<td class="label ${config.retireDate}"><label for="retireDate">Retire Date:</label></td>
								<td class="valueNW ${config.retireDate}"><tds:convertDate date="${assetEntity?.retireDate}"
							  		timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
								</td>
								<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td class="valueNW ${config.planStatus}">${assetEntity.planStatus}</td>
								<td class="label ${config.rateOfChange}" nowrap="nowrap"><label for="rateOfChange">Rate of Change (%)</label></td>
                                <td class="valueNW ${config.rateOfChange}">${assetEntity.rateOfChange}</td>
							</tr>
							<tr class="prop">
								<td class="label ${config.railType}" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								<td class="valueNW ${config.railType}">${assetEntity.railType}</td>
								<td class="label ${config.maintExpDate}"><label for="maintExpDate">Maint Exp.</label></td>
								<td class="valueNW ${config.maintExpDate}"><tds:convertDate date="${assetEntity?.maintExpDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							    </td>
								<td class="label ${config.validation}"><label for="validation">Validation</label></td>
								<td class="valueNW ${config.validation}">${assetEntity.validation}</td>
								<td>&nbsp;</td>
							</tr>
							<tr>
								<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
								<td class="${config.externalRefId}">${assetEntity.externalRefId}</td>
								<td class="label ${config.truck}" nowrap="nowrap"><label for="truck">Truck/Cart/Shelf</label></td>
								<td class="valueNW ${config.truck}">${assetEntity.truck}/${assetEntity.cart}${assetEntity.shelf? ' / '+assetEntity.shelf : ''}</td>
							</tr>
							<g:render template="customShow" ></g:render>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr id="deps">
			<g:render template="dependentShow" model= "[assetEntity:assetEntity]"></g:render>
		</tr>
	<tr id="commentListId">
		<g:render template="commentList" ></g:render>
	</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}">
					<input type="hidden" name="id" id="assetsId" value="${assetEntity?.id}" />
					<input type ="hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<g:render template="showButtons" 
						model="[assetEntity:assetEntity, redirectTo:redirectTo,type:'Server', forWhom:'server']" />
				</div>
			</td>
		</tr>
	</table>
</g:form>
