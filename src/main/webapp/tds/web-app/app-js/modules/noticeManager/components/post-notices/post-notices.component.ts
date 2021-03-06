// Angular
import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
// Service
import {WindowService} from '../../../../shared/services/window.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {NoticeService} from '../../service/notice.service';
import {UserContextService} from '../../../auth/service/user-context.service';
// Model
import {NoticeModel, Notices} from '../../model/notice.model';
// Components
import {StandardNoticesComponent} from '../standard-notices/standard-notices.component';
import {MandatoryNoticesComponent} from '../mandatory-notices/mandatory-notices.component';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {PostNoticesManagerService} from '../../../auth/service/post-notices-manager.service';

@Component({
	selector: 'tds-notice-post-notices',
	template: '<div></div>'
})
export class PostNoticesComponent implements OnInit {
	private postNotices: NoticeModel[] = [];
	private redirectUri: string;
	private baseUri = '/tdstm';
	private signOutUri = `${this.baseUri}/auth/signOut`;

	/**
	 * @constructor
	 * @param {NoticeService} noticeService
	 */
	constructor(
		private dialogService: UIDialogService,
		private noticeService: NoticeService,
		private windowService: WindowService,
		protected userContextService: UserContextService,
		private postNoticesManager: PostNoticesManagerService,
		private router: Router) {
	}

	/**
	 * Get the post notices and extract the redirectUri value
	*/
	ngOnInit() {
		this.userContextService.getUserContext()
		.subscribe((context: UserContextModel) => {
			const redirect = (context.postNotices && context.postNotices.redirectUri) || '';
			this.redirectUri = redirect.startsWith('/') ? `${this.baseUri}${redirect}` : `${redirect}`;

			if (this.redirectUri) {
				this.postNotices = context.postNotices.notices.map((notice: NoticeModel) => {
					notice.sequence = notice.sequence || 0;
					return notice;
				});
				this.showNotices();
			}
		});
	}

	/**
	 * First show the standard notices, then show the mandatory
	 * Because the bootstrap modal is necessary a delay among them
	*/
	showNotices(): void {
		const hasStandardNotices = this.filterPostNotices(false).length > 0;

		this.showMandatoryNotices()
			.then(() => {
				if (hasStandardNotices) {
					setTimeout(() => {
						this.showStandardNotices()
							.then(() => {
								this.postNoticesManager.notifyContinue()
									.subscribe(() => this.navigateTo(this.redirectUri))
							})
							.catch((error) => this.navigateTo(this.redirectUri));
					}, 600);
				} else {
					this.postNoticesManager.notifyContinue()
						.subscribe(() => this.navigateTo(this.redirectUri))
				}
			})
			.catch(() => {
				// navigate throught the window service because the route is not handled
				// by the angular router
				this.navigateTo(this.signOutUri);
			});
	}

	/**
	 * Filter post notices
	 * @param {Boolean} mandatory True for get mandatory, False for get Standard
	*/
	filterPostNotices(mandatory: boolean): any[] {
		return this.postNotices
			.filter((notice) => mandatory ? notice.needAcknowledgement : !notice.needAcknowledgement)
			.map((notice: NoticeModel) => {
				return {...notice, notShowAgain: false};
			});
	}

	/**
	 * Open the view to show standard notices
	*/
	showStandardNotices() {
		const notices = this.filterPostNotices(false);

		return notices.length ? this.dialogService
				.open(StandardNoticesComponent, [ {provide: Notices, useValue: {notices: notices}}])
				:
				Promise.resolve(true);
	}

	/**
	 * Open the view to show mandatory notices
	*/
	showMandatoryNotices() {
		const notices = this.filterPostNotices(true);

		return notices.length ? this.dialogService
				.open(MandatoryNoticesComponent, [ {provide: Notices, useValue: {notices: notices}}])
				:
				Promise.resolve(true);
	}

	/**
	 *  Navigate to specific url
	 * Because it could be a grails route not handled by Angular the navigation should be done through window service
	*/
	private navigateTo(uri: string) {
		this.windowService.navigateTo(uri);
	}
}
