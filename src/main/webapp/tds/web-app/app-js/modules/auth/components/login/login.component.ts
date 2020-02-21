// Angular
import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
// NGXS
import {Select, Store} from '@ngxs/store';
import {Login, LoginInfo} from '../../action/login.actions';
import {UserContextState} from '../../state/user-context.state';
// Service
import {LoginService} from '../../service/login.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PostNoticesManagerService} from '../../service/post-notices-manager.service';
import {RouterUtils} from '../../../../shared/utils/router.utils';
import {WindowService} from '../../../../shared/services/window.service';
import {PageService} from '../../service/page.service';
import {APP_STATE_KEY} from '../../../../shared/providers/localstorage.provider';
// Components
import { MandatoryNoticesComponent } from '../../../noticeManager/components/mandatory-notices/mandatory-notices.component';
import { StandardNoticesComponent } from '../../../noticeManager/components/standard-notices/standard-notices.component';
import {SelectProjectModalComponent} from '../../../project/components/select-project-modal/select-project-modal.component';
// Models
import {AuthorityOptions, IFormLoginModel, LoginInfoModel} from '../../model/login-info.model';
import {UserContextModel} from '../../model/user-context.model';
import {NoticeModel, Notices} from '../../../noticeManager/model/notice.model';
// Others
import {Observable} from 'rxjs';
import {map, withLatestFrom} from 'rxjs/operators';

@Component({
	selector: 'tds-login',
	templateUrl: 'login.component.html',
})
export class LoginComponent implements OnInit {
	/**
	 * Prepare the state to be able to auto - unsubscribe
	 */
	@Select(state => state.TDSApp.userContext) userContext$: Observable<any>;
	/**
	 * Current User Context Model
	 */
	public userContextModel: UserContextModel = null;
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
	 * To show loader and disable the login button
	 */
	public onLoginProgress = false;

	/**
	 * Holds the state of the login Button
	 */
	public loginState = 'default';

	/**
	 * For Auh label to show
	 */
	public defaultAuthorityItem: string;

	isSelectPrompt: boolean;

	constructor(
		private loginService: LoginService,
		private store: Store,
		private router: Router,
		private notifierService: NotifierService,
		private dialogService: UIDialogService,
		private pageService: PageService,
		private postNoticesManager: PostNoticesManagerService,
		private windowService: WindowService) {
	}

	/**
	 * Get the Login Information and prepare the store subscriber to redirect the user on a Success Login
	 */
	ngOnInit(): void {
		let loginRequests = [
			this.pageService.updateLastPage().catch(e => Observable.of({successful: false})),
			this.loginService.getLoginInfo()
		];
		Observable.forkJoin(loginRequests).pipe(
			map(([successToSaveLastPage, loginInfo]) => {
				// If session is still active, redirect user to his last page saved
				if (successToSaveLastPage.successful) {
					this.getCurrentUserSnapshot();
				} else {
					// If not, we ensure the session start from scratch
					this.destroyInitialSession();
					// Get Login Information
					this.loginInfo = loginInfo;
					this.store.dispatch(new LoginInfo({buildVersion: this.loginInfo.buildVersion}));
					this.setFocus();
				}
			})
		).subscribe();
	}

	/**
	 * Validate the current status of the User Context
	 */
	private getCurrentUserSnapshot(): void {
		// If the session has already a value, redirect the user
		this.userContextModel = this.store.selectSnapshot(UserContextState.getUserContext);
		if (this.userContextModel !== null) {
			this.validateLogin(this.userContextModel);
		}
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
					this.isSelectPrompt = true;
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
		if (this.onLoginProgress) {
			return;
		}
		this.errMessage = '';
		if (this.loginModel.username === '' || this.loginModel.password === '') {
			this.errMessage = 'Username and password are required';
		} else {
			if (this.isSelectPrompt && this.loginModel.authority === this.defaultAuthorityItem) {
				this.errMessage = 'Please select a domain';
				return;
			}
			this.onLoginProgress = true;
			this.store.dispatch(
				new Login({
					username: this.loginModel.username,
					password: this.loginModel.password,
					authority: (this.loginModel.authority !== this.defaultAuthorityItem) ? this.loginModel.authority : undefined
				})
			).pipe(
				withLatestFrom(this.userContext$)
			).subscribe(
				([_, userContext]) => this.validateLogin(userContext)
			);
		}
	}

	/**
	 * Validates and redirect the User
	 * Also invoke the Notices if they are available
	 * @param userContext
	 */
	private validateLogin(userContext: UserContextModel): void {
		this.onLoginProgress = false;
		this.userContextModel = userContext;

		if (this.userContextModel.alternativeProjects && this.userContextModel.alternativeProjects.length > 0) {
			setTimeout(() => {
				this.dialogService.open(SelectProjectModalComponent, [{
					provide: Array,
					useValue: this.userContextModel.alternativeProjects
				}]).then(result => {
					if (result.success) {
						setTimeout(() => {
							this.getCurrentUserSnapshot();
						}, 1000);
					}
				});
			});
		} else if (this.userContextModel.notices && this.userContextModel.notices.redirectUrl) {
			if (this.userContextModel.postNotices && this.userContextModel.postNotices.length > 0) {
				this.showNotices();
			} else {
				this.navigateTo();
			}
		} else if (this.userContextModel.error) {
			this.loginState = 'default';
			// An error has occurred
			this.notifierService.broadcast({
				name: 'stopLoader',
			});
			this.errMessage = this.userContextModel.error;
		}
	}

	/**
	 * First show the standard notices, then show the mandatory
	 * Because the bootstrap modal is necessary a delay among them
	 */
	private showNotices(): void {
		const hasStandardNotices = this.filterPostNotices(false).length > 0;

		this.showMandatoryNotices()
			.then(() => {
				if (hasStandardNotices) {
					setTimeout(() => {
						this.showStandardNotices()
							.then(() => {
								this.postNoticesManager.notifyContinue()
									.subscribe(
										() => this.navigateTo(),
										() => {
											this.navigateTo();
										});
							})
							.catch((error) => this.navigateTo());
					}, 600);
				} else {
					this.postNoticesManager.notifyContinue()
						.subscribe(
							() => this.navigateTo(),
							() => {
								this.navigateTo();
							});
				}
			})
			.catch(() => {
				this.destroyInitialSession();
			});
	}

	/**
	 * Open the view to show standard notices
	 */
	private showStandardNotices() {
		const notices = this.filterPostNotices(false);

		return notices.length ? this.dialogService.open(StandardNoticesComponent, [{
			provide: Notices,
			useValue: {notices: notices}
		}]) : Promise.resolve(true);
	}

	/**
	 * Open the view to show mandatory notices
	 */
	private showMandatoryNotices() {
		const notices = this.filterPostNotices(true);

		return notices.length ? this.dialogService
				.open(MandatoryNoticesComponent, [{provide: Notices, useValue: {notices: notices}}])
			:
			Promise.resolve(true);
	}

	/**
	 * Filter post notices
	 * @param {Boolean} mandatory True for get mandatory, False for get Standard
	 */
	private filterPostNotices(mandatory: boolean): any[] {
		return this.userContextModel.postNotices
			.filter((notice) => mandatory ? notice.needAcknowledgement : !notice.needAcknowledgement)
			.map((notice: NoticeModel) => {
				return {...notice, notShowAgain: false};
			});
	}

	/**
	 * Navigate to specific url
	 * Because it could be a grails route not handled by Angular the navigation should be done through window service
	 */
	private navigateTo() {
		this.redirectUser = true;
		if (RouterUtils.isAngularRoute(this.userContextModel.notices.redirectUrl)) {
			let routeObject = RouterUtils.getAngularRoute(this.userContextModel.notices.redirectUrl);
			this.router.navigate(routeObject.path, {queryParams: routeObject.queryString});
		} else {
			this.windowService.getWindow().location.href = RouterUtils.getLegacyRoute(this.userContextModel.notices.redirectUrl);
		}
	}

	/**
	 * Due issues on Legacy Page, need to close the session every time you land on the login page
	 * Or after you decide to cancel a mandatory Notice
	 */
	private destroyInitialSession() {
		localStorage.removeItem(APP_STATE_KEY);
	}
}
