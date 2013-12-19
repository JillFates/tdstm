<%@page import="com.tds.asset.AssetCableMap"%>
<script type="text/javascript">
	if(!${assetCablingDetails[0]?.hasImageExist}){
		$("#cablingPanel").css("height",${assetCablingDetails[0].usize? assetCablingDetails[0].usize*30+2 : 0}+'px')
		$("#cablingPanel").css("background-color","LightGreen")
	} else {
		$("#rearImage_${assetCablingDetails[0]?.model}").show()
		$("#cablingPanel").css("background-color","#FFF")
	}
	$("#cablingDialogId").dialog( "option", "title", "${assetCablingDetails[0]?.title}");
	$('div.connector_Left').each(function(index) {
		$(this).attr("style","margin-left:-"+$(this).children().width()+"px");
	}); 
	if(!isIE7OrLesser)
		$("select.assetConnectSelect").select2();
</script>
	<div id="cablingPanel" style="height: auto; ">
	<g:if test="${assetCablingDetails}">
		<g:each in="${assetCablingDetails}" var="assetCabling">
			<div id='connector${assetCabling.id}' style='top: "${(assetCabling.connectorPosY / 2)}+"px; left: "${assetCabling.connectorPosX}+"px;'>
				<a href='#'><div><img id='${assetCabling.status}' src='../i/cabling/${assetCabling.status.toLowerCase()}.png' onclick="openActionButtonsDiv( '${assetCabling.id}', this.id, '${assetCabling.type}')"></div></a>
				<div class='connector_${assetCabling.labelPosition}'><span>${assetCabling.label}</span></div>
			</div>
		</g:each>
	</g:if>
	<g:if test="${currentBundle}">
		<g:each in="${models}" var="model">
			<g:if test="${model?.rearImage && model?.useImage == 1}">
				<img id="rearImage_${model.id}" src="${createLink(controller:'model', action:'getRearImage', id:model.id)}" style="display: none;"/>
			</g:if>
		</g:each>
	</g:if>
	</div>
	<div class="inputs_div">
		<g:form action="updateCablingDetails" name="cablingDetailsForm">
		<div id="actionButtonsDiv" style="margin-top: 5px;float: left;display: none;">
			<input type="button" value="Unknown" onclick="openActionDiv(this.id)" id="unknownId"/>
			<input type="button" value="0" onclick="openActionDiv(this.id)" style="background-color: #5F9FCF;" id="emptyId"/>
			<input type="button" value="X" onclick="openActionDiv(this.id)" id="cabledId"/>
			<input type="button" value="Assign" onclick="openActionDiv(this.id)" id="assignId"/>
		</div>
		<div id="actionDiv" style="margin-top: 5px;float: right;display: none;">
			<img src="${resource(dir:'images',file:'check12.png')}" class="pointer" onclick="submitAction($('form[name=cablingDetailsForm]'))" style="width:18px;"/>
			<img src="${resource(dir:'images',file:'delete.png')}" class="pointer" onclick="cancelAction()" style="width:18px;"/>
			<g:select id="colorId" name="color" from="${AssetCableMap.constraints.cableColor.inList}" noSelection="${['':'Please Select']}" onchange="updateCell(this.value)"></g:select>
			<input type="reset" id="formReset" style="display: none;"/>
		</div>
		<div style="clear: both;"></div>
		<div style="text-align: center;margin-bottom: 5px;display: none;" id="assignFieldDiv">
			<div id="inputDiv">
			<g:select id="assetFromId" class="assetConnectSelect" name="assetFromId" from="${currRoomRackAssets}" optionKey="id" 
				 optionValue="assetName" noSelection="${[null:'Please Select']}" onchange="assetModelConnectors(this.value)"></g:select>
				<span id="modelConnectorList">&nbsp;
					<g:render template="modelConnectorList" model="[modelConnectList:'']"></g:render>
				</span>
				<input type="text" name="cableComment" id="cableComment" size="10"/>
				<g:select id="status" name="status" style="width:75px;" from="${dependencyStatus}" optionValue="value"></g:select>
			</div>
			<div id="powerDiv" style="display: none;">
				<input type="radio" name="staticConnector" id="staticConnector_A" value="A">A</input>&nbsp;
				<input type="radio" name="staticConnector" id="staticConnector_B" value="B">B</input>&nbsp;
				<input type="radio" name="staticConnector" id="staticConnector_C" value="C">C</input>
			</div>
			<div>
				<input type="hidden" name="assetCable" id="cabledTypeId"/>
				<input type="hidden" name="actionType" id="actionTypeId"/>
				<input type="hidden" name="connectorType" id="connectorTypeId"/>
				<input type="hidden" name="assetEntityId" id="assetEntityId"/>
				<input type="hidden" id="previousColor"/>
			</div>
		</div>
		
		</g:form>
	</div>
	<div style="clear: both;"></div>
	<div class="list">
		<table>
			<thead>
				<tr>
					<th>Type</th>
					<th>Label</th>
					<th>Status</th>
					<th>Color</th>
					<th>assetTo/Conn</th>
				</tr>
			</thead>
			<tbody id="cablingTableBody">
			<g:if test="${!assetCablingDetails}">
			<tr>
				<td colspan="5">No Connectors found</td>
			</tr>
			</g:if>
			<g:else>
				<g:each in="${assetCablingDetails}" var="assetCabling">
					<tr id='connectorTr${assetCabling.id}'>
					<td title="${assetCabling.status}" onclick="openActionButtonsDiv( '${assetCabling.id}', this.title, '${assetCabling.type}' , '${assetCabling.toAssetId}')">
						${assetCabling.type}
						<input type="hidden" id="comment_${assetCabling.id}" value="${assetCabling.comment}"/>
						<input type="hidden" id="status_${assetCabling.id}" value="${assetCabling.status}"/>
						<input type="hidden" id="asset_${assetCabling.id}" value="${assetCabling.toAssetId}"/>
						<input type="hidden" id="toport_${assetCabling.id}" value="${assetCabling.toAssetPortId}"/>
					</td>
					<td title="${assetCabling.status}" onclick="openActionButtonsDiv( '${assetCabling.id}', this.title, '${assetCabling.type}' , '${assetCabling.toAssetId}')">${assetCabling.label}</td>
					<td title="${assetCabling.status}" onclick="openActionButtonsDiv( '${assetCabling.id}', this.title, '${assetCabling.type}' , '${assetCabling.toAssetId}')">${assetCabling.displayStatus}</td>
		
					<g:if test="${assetCabling.color != ''}">
						<td id='color_${assetCabling.id}' title="${assetCabling.status}" class="${assetCabling.color}" onclick="openActionButtonsDiv( '${assetCabling.id}', this.title, '${assetCabling.type}' )">&nbsp;</td>
					</g:if> 
					<g:else>
						<td id='color_${assetCabling.id}' title="${assetCabling.status}" onclick="openActionButtonsDiv( '${assetCabling.id}', this.title, '${assetCabling.type}' )">&nbsp;</td>
					</g:else>
					<g:if test="${assetCabling.toAssetId != null}">
						<td id='connectorTd${assetCabling.id}'>
						<a title='${assetCabling.status}' style='text-decoration: underline;color:blue;' href="javascript:openCablingDiv('${assetCabling.toAssetId}')">${assetCabling.fromAssetId}</a></td>
						</tr>
					</g:if> 
					<g:else>
						<td id='connectorTd${assetCabling.id}'>
						<input type="hidden" id="power_${assetCabling.id}" value="${assetCabling.rackUposition}"/>
							${assetCabling.fromAssetId}
						</td>
						</tr>
					</g:else>
				</g:each>
			</g:else>
			</tbody>
		</table>
	</div>
