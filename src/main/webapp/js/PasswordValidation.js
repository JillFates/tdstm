var PasswordValidation = (function ($) {
	
	// public functions
    const public = {};
    let submitDisabled = true;
    let passwordsMatch = false;

    public.passwordMatch = function (password, element, regex, noMatch) {
        let shape = 'times';
        let className = 'is-error';
        let returnVal = 0;

        if (!(element[0].id == 'usernameRequirementId') == (password.match(regex) != null)){
			shape = 'check';
			className = 'is-success';
			returnVal = 1;
		} else {
			noMatch.item = element;
		}
        element[0].className = className;
        element[0].setAttribute('shape', shape);
		return returnVal;
	}

    public.confirmPasswordMatch = function (password, retypedPassword, element, noMatch) {
        let returnVal = 0;
        let color = '#cc0000';

        if ((password === retypedPassword)){
            color = '#00aa00';
            returnVal = 1;
        } else {
          //  noMatch.item = element;
        }
        element[0].style.cssText = 'color: ' + color;
        return returnVal;
    };
	
	public.checkPassword = function (field) {
        let requirements = 0;
        const password = field.value;
        let username = '';
        if ($('#prefUsernameId').size() > 0)
			username = $('#prefUsernameId').val().toLowerCase();
		if (field.id != "newPasswordId" && $('#username').size() > 0)
			username = $('#username').val().toLowerCase();

        const noMatch = {item: null};
        let score = 0;
        const parent = $('form');
        const requiredLength = parent.find('#lengthRequirementId').attr('min-size');
        score += public.passwordMatch(password.toLowerCase(), parent.find('#usernameRequirementId'), new RegExp('.*' + username + '.*'), noMatch);
		score += public.passwordMatch(password, parent.find('#lengthRequirementId'), RegExp('.{' + requiredLength + '}'), noMatch);
		requirements += public.passwordMatch(password, parent.find('#lowercaseRequirementId'), /[a-z]+/, noMatch);
		requirements += public.passwordMatch(password, parent.find('#uppercaseRequirementId'), /[A-Z]+/, noMatch);
		requirements += public.passwordMatch(password, parent.find('#numericRequirementId'), /[0-9]+/, noMatch);
		requirements += public.passwordMatch(password, parent.find('#symbolRequirementId'), /.*[~!@#\$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.\?\/].*/, noMatch);
		if (public.passwordMatch(requirements + "", parent.find('#passwordRequirementsId'), /[3-4]+/, noMatch) == 1){
			score++;
		}

		submitDisabled = (score < 3);
		document.getElementById('resetPasswordSubmitButton').disabled = (submitDisabled || !passwordsMatch);
		
		return (score === 3);
	}

    public.confirmPassword = function (passwordField, retypedPasswordField) {
        let result = 0;
        const password = passwordField.value;
        const retypedPassword = retypedPasswordField.value;
        const parent = $('form');
        const element = parent.find('#retypedPasswordMatchRequirementId');
        result = public.confirmPasswordMatch(password.toLowerCase(), retypedPassword.toLowerCase(), element);
        passwordsMatch = (result === 1);
        document.getElementById('resetPasswordSubmitButton').disabled = (submitDisabled || !passwordsMatch);
        return result;
    };

	// return the public object to make the public functions accessable
	return public;
	
})(jQuery);