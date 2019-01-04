
// Function to retrieve a field.
function retrieve_field(obj) {
	var cookie = '', real_value = '';
	cookie = document.cookie;
	var objType = new String(obj.type);
	if (obj.name)
		var objName = new String(obj.name);
	else
		var objName = new String(obj[0].name);
	var offset_start = cookie.indexOf(objName + '=[');
	if (offset_start == -1) return 1;
	var offset_start_length = objName.length + 2;
	offset_start = offset_start + offset_start_length;
	var offset_end = cookie.indexOf(']', offset_start);
	real_value = cookie.substring(offset_start, offset_end);
	switch(objType.toLowerCase()) {
		case "checkbox" :
			if (real_value == '1') obj.checked = 1
			else obj.checked = 0
			break;
		case "undefined" :
			obj[real_value].checked = 1;
			break;
		case "select-one" :
			obj.selectedIndex = real_value;
			break;
		case "select-multiple" :
			for (var i = 0; i < obj.options.length; i++) {
				if ((real_value.indexOf('+' + i)) > -1)
					obj.options[i].selected = 1;
				else
					obj.options[i].selected = 0;
			}
			break;
		default :
			obj.value = real_value;
			break;
	}
	return 1;
}
