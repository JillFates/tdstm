import {Component} from '@angular/core';
import {LoginService} from '../../service/login.service';
import {LoginInfoModel} from '../../model/login-info.model';

@Component({
	selector: 'tds-login',
	templateUrl: 'login.component.html',
})

export class LoginComponent {

	public loginInfo: LoginInfoModel = new LoginInfoModel();

	constructor(private loginService: LoginService) {
		this.getLoginInfo();
	}

	private getLoginInfo(): void {
		this.loginService.getLoginInfo().subscribe((response: any) => {
			this.loginInfo = response;
		});
	}

}