<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="margin-top: 89px; margin-left: 50px; width: auto;">
		<table id="taskCategoryTableId">
			<thead>
			<tr>
				<th colspan="2"><h3>Task Category</h3></th>
			</tr>
			</thead>
			<tbody id="taskCategoryTbodyId">
			<g:each in="${taskCategory}" status="i" var="taskCategoryIt">
				<tr id="taskCategory_${taskCategoryIt.id}">
					<td>${taskCategoryIt.value}</td>
					<td>
						<span class=" deleteTaskCategory clear_filter"
						      style="display: none; cursor: pointer;"
						      onClick="deleteAssetStatus(${taskCategoryIt.id}, $('#taskCategoryHiddenId').val())"><b>X</b>
						</span>
					</td>
				</tr>
			</g:each>
		</table>
		<input type="hidden" id="taskCategoryHiddenId" name="hiddenId" value="taskCategory"/>
		<span id="newTaskCategory" style="display: none;">
			<input type="text" id="taskCategory" name="taskCategory" maxlength="20" value="">
		</span>
		<input type="button" id="addTaskCategoryButtonId" name="createTaskCategory" value="EDIT" onclick="addAssetOptions($('#taskCategoryHiddenId').val())"/>
	</div>
</div>
