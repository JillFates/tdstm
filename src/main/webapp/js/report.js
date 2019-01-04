
function disableGenerateButton(formName){
	$("#"+formName+"Button").attr('disabled','disabled');
}

function changeSmeSelect(bundle,forWhom){
	jQuery.ajax({
		url: contextPath+'/reports/generateSmeByBundle',
		data: {'bundle':bundle,'forWhom':forWhom},
		type:'POST',
		success: function(data) {
			console.log("success1");
			$("#smeAndAppOwnerTbody").html(data)
		}
	});
}