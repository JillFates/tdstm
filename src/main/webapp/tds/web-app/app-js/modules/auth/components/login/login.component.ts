// Angular
import {Component, OnInit} from '@angular/core';
// NGXS
import {Select, Store} from '@ngxs/store';
// Service
import {LoginService} from '../../service/login.service';
// Models
import {AuthorityOptions, ILoginModel, LoginInfoModel} from '../../model/login-info.model';
import {Login} from '../../action/login.actions';
import {Observable} from 'rxjs';
import {UserContextModel} from '../../model/user-context.model';
import {Router} from '@angular/router';
import {RouterUtils} from '../../../../shared/utils/router.utils';
import {WindowService} from '../../../../shared/services/window.service';

@Component({
	selector: 'tds-login',
	templateUrl: 'login.component.html',
})

export class LoginComponent implements OnInit {

	@Select(state => state) userContext$: Observable<any>;

	public loginInfo: LoginInfoModel = new LoginInfoModel();
	public authorityOptions = AuthorityOptions;
	public loginModel: ILoginModel = {
		authority: '',
		username: '',
		password: ''
	};
	public defaultAuthorityItem: string;

	constructor(
		private loginService: LoginService,
		private store: Store,
		private router: Router,
		private windowService: WindowService) {
	}

	ngOnInit(): void {
		this.loginService.getLoginInfo().subscribe((response: any) => {
			this.loginInfo = response;
			this.setFocus();
		});

		this.store.select(state => state.TDSApp.userContext).subscribe((userContext: UserContextModel) => {
			if (userContext && userContext.notices && userContext.notices.redirectUrl) {
				if (RouterUtils.isAngularRoute(userContext.notices.redirectUrl)) {
					this.router.navigate(RouterUtils.getAngularRoute(userContext.notices.redirectUrl));
				} else {
					this.windowService.getWindow().location.href = userContext.notices.redirectUrl;
				}
			}
		});
	}

	/**
	 * After the page has loaded, init the Focus to the proper element
	 */
	private setFocus(): void {
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
	}

	/**
	 * Dispatch Action Login
	 */
	public onLogin(): void {
		this.store.dispatch(new Login({username: this.loginModel.username, password: this.loginModel.password}));
	}
}
