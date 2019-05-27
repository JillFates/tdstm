import {Component} from '@angular/core';
import {LoginService} from '../../service/login.service';
import {LoginInfoModel} from '../../model/login-info.model';

@Component({
	selector: 'tds-forget-password',
	templateUrl: 'forgot-password.component.html',
})

export class ForgotPasswordComponent {

	public loginInfo: LoginInfoModel = new LoginInfoModel();
	public error: any;
	public passwordSent = false;
	public email: '';

	constructor(private loginService: LoginService) {
	}

	/**
	 * Request Password Recovery by sending email
	 */
	public onSendEmail(): void {
		this.validateFields()
			.then(() => {
				this.loginService.forgotPassword(this.email).subscribe((data: any) => {
					if (!data.success) {
						this.error = 'An error occurred, please try again later.';
						return;
					}
					this.passwordSent = true;
				});
			})
			.catch(err => this.passwordSent = false);
	}

	/**
	 *  Validates fields
	 */
	public validateFields(): Promise<boolean> {
		const emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,63})+$/;

		return new Promise((resolve, reject) => {
			this.error = '';
			if ((this.email && !emailExp.test(this.email)) || !this.email) {
				this.error = 'Not a valid e-mail address';
			}
			return this.error && this.error.length > 0 ? reject(new Error('Error validating fields')) : resolve(true);
		});
	}

}