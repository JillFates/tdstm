<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Show Manufacturer</title>
		<script type="text/javascript">
			$(document).ready(function() {
				$("#editManufacturerView").dialog({ autoOpen: false })
			})
		</script>
	</head>
	<body>
		<div class="body">
			<div class="dialog">
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name">Name:</td>
							<td valign="top" class="value">${fieldValue(bean:manufacturerInstance, field:'name')}</td>
						</tr>
						<tr>
							<td valign="top" class="name">AKA:</td>
							<td valign="top" class="value">${manuAlias}</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Description:</td>
							<td valign="top" class="value">${fieldValue(bean:manufacturerInstance, field:'description')}</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Corporate Name:</td>
							<td valign="top" class="value">${fieldValue(bean:manufacturerInstance, field:'corporateName')}</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Corporate Location:</td>
							<td valign="top" class="value">${fieldValue(bean:manufacturerInstance, field:'corporateLocation')}</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">Website:</td>
							<td valign="top" class="value">${fieldValue(bean:manufacturerInstance, field:'website')}</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${manufacturerInstance?.id}" />
					<span class="button"><input type="button" class="edit" value="Edit" onclick="showOrEditModelManuDetails('manufacturer',${manufacturerInstance?.id},'Manufacturer','edit','Edit')" /></span>
					<span class="button"><input class="create" type="button" value="Merge into" onclick="showMergeDialog(${manufacturerInstance?.id},'${manufacturerInstance?.name}')"/></span>
					<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
				</g:form>
			</div>
			<div id="editManufacturerView" style="display: none;"></div>
		</div>
		<script>
			currentMenuId = "#adminMenu";
			$('.menu-list-manufacturers').addClass('active');
			$('.menu-parent-admin').addClass('active');
		</script>
	</body>
</html>
