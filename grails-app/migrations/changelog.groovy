databaseChangeLog = {

	include file: '20121206-fix-staff-system-roles.groovy'
	include file: '20121210-fix-models-manu-nulldates.groovy'
	include file: '20121211-add-projectStaff-permission.groovy'
	include file: '20121218-normalize-aka-columns.groovy'	
	include file: '20121214-drop-aka.groovy'
	include file: '20121218-update-version-on-manu-model.groovy'
	include file: '20121218-add-indexes-model-tables.groovy'
	include file: '20121224-cleanup-excel-templates.groovy'
	include file: '20130109-alter-validation-column.groovy'
	include file: '20130111-add-roletypes.groovy'
	include file: '20130114-fix-roletypes.groovy'
	include file: '20130125-add-entity-validation.groovy'
	include file: '20130125-update-plantatus-validation.groovy'
	include file: '20130213-add-model-usize.groovy'
	
}
