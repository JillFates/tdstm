// Angular
import {Component, OnInit} from '@angular/core';
// NGXS
import {Select, Store} from '@ngxs/store';
// Service
import {LoginService} from '../../service/login.service';
// Models
import {AuthorityOptions, IFormLoginModel, LoginInfoModel} from '../../model/login-info.model';
import {Login, LoginInfo} from '../../action/login.actions';
import {UserContextModel} from '../../model/user-context.model';
import {Router} from '@angular/router';
import {RouterUtils} from '../../../../shared/utils/router.utils';
import {WindowService} from '../../../../shared/services/window.service';

@Component({
	selector: 'tds-login',
	templateUrl: 'login.component.html',
})

export class LoginComponent implements OnInit {
	/**
	 * Redirect user is taking place, by default is turned off
	 */
	public redirectUser = false;
	/**
	 * Shows error in the UI
	 */
	public errMessage = '';
	/**
	 * Retrieve the Information of the login and notices, including errors
	 */
	public loginInfo: LoginInfoModel = new LoginInfoModel();
	/**
	 * Auth Configuration to hide or show elements in the UI
	 */
	public authorityOptions = AuthorityOptions;
	/**
	 * Login Form
	 */
	public loginModel: IFormLoginModel = {
		authority: '',
		username: '',
		password: ''
	};
	/**
	 * For Auh label to show
	 */
	public defaultAuthorityItem: string;

	constructor(
		private loginService: LoginService,
		private store: Store,
		private router: Router,
		private windowService: WindowService) {
	}

	/**
	 * Get the Login Information and prepare the store subscriber to redirect the user on a Success Login
	 */
	ngOnInit(): void {

		// Get Login Information
		this.loginService.getLoginInfo().subscribe((response: any) => {
			this.loginInfo = response;
			this.store.dispatch(new LoginInfo({buildVersion: this.loginInfo.buildVersion}));
			this.setFocus();
		});

		// On success we hide the form and the user is redirected to the saved URL
		this.store.select(state => state.TDSApp.userContext).subscribe((userContext: UserContextModel) => {
			if (userContext && userContext.notices && userContext.notices.redirectUrl) {
				this.redirectUser = true;
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
	 * this to initialize the Auth configuration
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
		if (this.loginModel.username === '' || this.loginModel.password === '') {
			this.errMessage = 'Username and password are required';
		} else {
			this.store.dispatch(new Login({username: this.loginModel.username, password: this.loginModel.password}));
		}
	}
}
