// Angular
import {Component, OnInit} from '@angular/core';
// Service
import {LoginService} from '../../service/login.service';
// Models
import {AuthorityOptions, ILoginModel, LoginInfoModel} from '../../model/login-info.model';

@Component({
	selector: 'tds-login',
	templateUrl: 'login.component.html',
})

export class LoginComponent implements OnInit {

	public loginInfo: LoginInfoModel = new LoginInfoModel();
	public authorityOptions = AuthorityOptions;
	public loginModel: ILoginModel = {
		authority: '',
		username: '',
		password: ''
	};
	public defaultAuthorityItem: string;

	constructor(private loginService: LoginService) {
	}

	ngOnInit(): void {
		this.loginService.getLoginInfo().subscribe((response: any) => {
			this.loginInfo = response;
			setTimeout(() => {
				let selector = '.username';
				if (this.loginInfo && this.loginInfo.config) {
					if (this.loginInfo.config.authorityPrompt === this.authorityOptions.SELECT) {
						this.defaultAuthorityItem = `Select ${this.loginInfo.config.authorityLabel}`;
						this.loginModel.authority = this.defaultAuthorityItem;
						selector = '.k-dropdown-wrap';
					} else if (this.loginInfo.config.authorityPrompt === this.authorityOptions.PROMPT) {
						selector = '.authority';
					} else if (this.loginInfo.config.authorityPrompt === this.authorityOptions.HIDDEN) {
						this.loginModel.authority = this.loginInfo.config.authorityName;
					}
				}
				let inputField: HTMLElement = <HTMLElement>document.querySelectorAll(selector)[0];
				if (inputField) {
					inputField.focus();
				}
			});
		});
	}

	public onLogin(): void {
		console.log('Execute Login');
	}
}