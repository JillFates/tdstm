// Angular
import {Component} from '@angular/core';
// Service
import {LoginService} from '../../service/login.service';
// Models
import {AuthorityOptions, ILoginModel, LoginInfoModel} from '../../model/login-info.model';

@Component({
	selector: 'tds-login',
	templateUrl: 'login.component.html',
})

export class LoginComponent {

	public loginInfo: LoginInfoModel = new LoginInfoModel();
	public authorityOptions = AuthorityOptions;
	public loginModel: ILoginModel = {
		authority: '',
		username: '',
		password: ''
	};

	constructor(private loginService: LoginService) {
		this.getLoginInfo();
	}

	private loadFocus(): void {
		let selector = '.username';
		if (this.loginInfo && this.loginInfo.config) {
			if (this.loginInfo.config.authorityPrompt === this.authorityOptions.SELECT) {
				selector = '.k-dropdown-wrap';
			} else if (this.loginInfo.config.authorityPrompt === this.authorityOptions.PROMPT) {
				selector = '.authority';
			}
		}
		let inputField: HTMLElement = <HTMLElement>document.querySelectorAll(selector)[0];
		if (inputField) {
			inputField.focus();
		}
	}

	private getLoginInfo(): void {
		this.loginService.getLoginInfo().subscribe((response: any) => {
			this.loginInfo = response;
			this.loadFocus();
		});
	}

	public onLogin(): void {
		console.log('Execute Login');
	}
}