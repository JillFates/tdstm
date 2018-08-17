<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="margin-top: 89px;width: auto;">
		<table id="envOptionTableId">
			<thead>
				<tr>
					<th colspan="2"><h3>Asset Environment</h3></th>
				</tr>
			</thead>
			<tbody id="envOptionTbodyId">
				<g:each in="${environment}" status="i" var="env">
					<tr id="environment_${env.id}">
						<td>${env.value}</td>
						<td>
							<span class="deleteEnvironment clear_filter" style="display: none; cursor: pointer;"
								onClick="deleteAssetStatus(${env.id},$('#envOptionHiddenId').val())"><b>X</b>
							</span>
						</td>
					</tr>
				</g:each>
		</table>
		<input type="hidden" id="envOptionHiddenId" name="hiddenId" value="environment" /> 
		<span id="newEnvironment" style="display: none;"> 
			<input type="text" id="environment" name="environment" maxlength="20" value=""> 
		</span> 
		<input type="button" id="addEnvironmentButtonId" name="createEnvironment" value="EDIT" onclick="addAssetOptions($('#envOptionHiddenId').val())"/>
	</div>
</div>