function importExportPreference($me,forWhom){
    var isChecked = $me.is(":checked")
    jQuery.ajax({
        url:contextPath+'/assetEntity/setImportPerferences',
        data:{'value':isChecked, 'preference':forWhom}
    });
}
