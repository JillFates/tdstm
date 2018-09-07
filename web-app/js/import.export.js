function importExportPreference($me,forWhom){
    var isChecked = $me.is(":checked")
    jQuery.ajax({
        url:contextPath+'/assetEntity/setImportPreferences',
        data:{'value':isChecked, 'preference':forWhom}
    });
}
