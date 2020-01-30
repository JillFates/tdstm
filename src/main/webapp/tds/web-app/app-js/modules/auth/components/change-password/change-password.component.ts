import {Component, OnInit} from '@angular/core';
import {LoginService} from '../../service/login.service';
import {Router} from '@angular/router';
import {APP_STATE_KEY} from '../../../../shared/providers/localstorage.provider';
import {Logout} from '../../action/login.actions';
import {Store} from '@ngxs/store';
import {UserContextModel} from '../../model/user-context.model';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {FormBuilder, FormControl, FormGroup, ValidatorFn} from '@angular/forms';

function passwordMatchValidator(password: string): ValidatorFn {
	return (control: FormControl) => {
		if (!control || !control.parent) {
			return null;
		}

		return control.parent.get(password).value === control.value ? null : { mismatch: true };
	};
}

@Component({
	selector: 'tds-change-password',
	templateUrl: 'change-password.component.html',
})

export class ChangePasswordComponent implements OnInit {

	public hidePasswordFields = true;
	public passwordChangeModel = {
		newPassword: '',
		confirmPassword: '',
		containsUsername: false,
		atLeastMinimumLength: false,
		hasLowercaseChars: false,
		hasUppercaseChars: false,
		hasNumericChars: false,
		hasNonAlphanumericChars: false,
		meetsCompositionRequirements: false
	};
	public userContext: UserContextModel = null;
	public error: any;
	public email: '';
	changePasswordForm: FormGroup;

	constructor(
		private loginService: LoginService,
		private router: Router,
		private store: Store,
		private notifierService: NotifierService,
		private fb: FormBuilder) {
		this.changePasswordForm = this.fb.group({
			newPassword: ['', []],
			confirmPassword: ['', [
				passwordMatchValidator('newPassword')
			]]
		});
	}

	ngOnInit(): void {
		this.store.select(state => state.TDSApp.userContext).subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
		this.disableGlobalNotification(true);
	}

	/**
	 * Change Password
	 */
	public onPasswordChange(): void {
		this.loginService.updatePassword(this.passwordChangeModel.newPassword, this.passwordChangeModel.confirmPassword).subscribe((res: any) => {
			if (!res && !res.data.success) {
				this.error = 'An error occurred, please try again later.';
			} else if (res.errors && res.errors.length > 0) {
				this.error = res.errors[0];
			} else {
				this.disableGlobalNotification(true);
				this.router.navigate(['project', 'list'], { queryParams: { show: this.userContext.project.id }});
			}
		});
	}

	/**
	 * Validate the Password
	 */
	public validatePassword() {
		let password = this.passwordChangeModel.newPassword;
		if (password != undefined) {
			this.passwordChangeModel.containsUsername = password.includes(this.userContext.user.username);
			this.passwordChangeModel.atLeastMinimumLength = (password.length > 7);
			this.passwordChangeModel.hasLowercaseChars = /[a-z]/.test(password);
			this.passwordChangeModel.hasUppercaseChars = /[A-Z]/.test(password);
			this.passwordChangeModel.hasNumericChars = /[0-9]/.test(password);
			this.passwordChangeModel.hasNonAlphanumericChars = /[~!@#$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.?\/]/.test(password);
			let strengthScore = (this.passwordChangeModel.hasLowercaseChars ? 1 : 0) +
				(this.passwordChangeModel.hasUppercaseChars ? 1 : 0) +
				(this.passwordChangeModel.hasNumericChars ? 1 : 0) +
				(this.passwordChangeModel.hasNonAlphanumericChars ? 1 : 0) +
				(!this.passwordChangeModel.containsUsername ? 1 : 0);
			this.passwordChangeModel.meetsCompositionRequirements = (strengthScore >= 5);
		}
	}

	/**
	 * Kill the session and send user back to the Login
	 */
	public backToLogin(): void {
		localStorage.removeItem(APP_STATE_KEY);
		this.store.dispatch(new Logout());
		this.router.navigate(['/auth/login']);
	}

	/**
	 * Disable global notification
	 * @param disable
	 */
	private disableGlobalNotification(disable: boolean): void {
		this.notifierService.broadcast({
			name: 'alertTypeDisable',
			disable: disable
		});
	}
}
