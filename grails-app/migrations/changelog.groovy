databaseChangeLog = {

	include file: '20121206-fix-staff-system-roles.groovy'
	include file: '20121210-fix-models-manu-nulldates.groovy'
	include file: '20121211-add-projectStaff-permission.groovy'
	include file: '20121218-normalize-aka-columns.groovy'	
	include file: '20121214-drop-aka.groovy'
	include file: '20121218-update-version-on-manu-model.groovy'
	include file: '20121218-add-indexes-model-tables.groovy'
	
}
