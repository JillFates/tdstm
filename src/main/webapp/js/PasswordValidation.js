var PasswordValidation = (function ($) {
	
	// public functions
	var public = {};
	
	
	public.passwordMatch = function (password, element, regex, noMatch) {
		var returnVal = 0;
		color = '#cc0000';
		text = '';

		if (!(element.attr('id') == 'usernameRequirementId') == (password.match(regex) != null)){
			color = '#00aa00';
			text = ' OK';
			returnVal = 1;
		} else {
			noMatch.item = element;
		}
		element.css('color', color);
		element.find('.ok').html(text);
		return returnVal;
	}

    public.confirmPasswordMatch = function (password, retypedPassword, element, noMatch) {
        var returnVal = 0;
        color = '#cc0000';
        text = '';

        if ((password == retypedPassword)){
            color = '#00aa00';
            text = ' OK';
            returnVal = 1;
        } else {
          //  noMatch.item = element;
        }
        element.css('color', color);
        element.find('.ok').html(text);
        return returnVal;
    }
	
	public.checkPassword = function (field) {
		var requirements = 0;
		var password = field.value;
		var username = '';
		if ($('#prefUsernameId').size() > 0) 
			username = $('#prefUsernameId').val().toLowerCase();
		if (field.id != "newPasswordId" && $('#username').size() > 0)
			username = $('#username').val().toLowerCase();
		
		var noMatch = {item:null};
		var score = 0;
		var parent = $(field).parent().parent().parent();
		var requiredLength = parent.find('#lengthRequirementId').attr('size');
		score += public.passwordMatch(password.toLowerCase(), parent.find('#usernameRequirementId'), new RegExp('.*' + username + '.*'), noMatch);
		score += public.passwordMatch(password, parent.find('#lengthRequirementId'), RegExp('.{' + requiredLength + '}'), noMatch);
		requirements += public.passwordMatch(password, parent.find('#lowercaseRequirementId'), /[a-z]+/, noMatch);
		requirements += public.passwordMatch(password, parent.find('#uppercaseRequirementId'), /[A-Z]+/, noMatch);
		requirements += public.passwordMatch(password, parent.find('#numericRequirementId'), /[0-9]+/, noMatch);
		requirements += public.passwordMatch(password, parent.find('#symbolRequirementId'), /.*[~!@#\$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.\?\/].*/, noMatch);
		if (public.passwordMatch(requirements + "", parent.find('#passwordRequirementsId'), /[3-4]+/, noMatch) == 1){
			score++;
			if(noMatch.item != null)
				noMatch.item.css('color', "#555555");
		}
		
		return (score == 3);
	}

    public.confirmPassword = function (passwordField, retypedPasswordField) {
        var result = 0;
        var password = passwordField.value;
        var retypedPassword = retypedPasswordField.value;
        var parent = $(retypedPasswordField).parent().parent().parent();
        var element = parent.find('#retypedPasswordMatchRequirementId');
        result = public.confirmPasswordMatch(password.toLowerCase(), retypedPassword.toLowerCase(), element);
        return result;
    }
	
	public.togglePasswordVisibility = function (box) {
		var newState = "text";
		if (box.checked) {
			newState = 'password';
		}
		var parent = $(box).parent().parent().parent()
		parent.find('.passwordField').attr('type', newState);
	}
	
	// return the public object to make the public functions accessable
	return public;
	
})(jQuery);