// Angular
import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
// Service
import {WindowService} from '../../../../shared/services/window.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {NoticeService} from '../../service/notice.service';
import {SortUtils} from '../../../../shared/utils/sort.utils';
// Model
import {NoticeModel, Notices, PostNoticeResponse } from '../../model/notice.model';
// Components
import {StandardNoticesComponent} from '../standard-notices/standard-notices.component';
import {MandatoryNoticesComponent} from '../mandatory-notices/mandatory-notices.component';

@Component({
	selector: 'tds-notice-post-notices',
	template: '<div></div>'
})
export class PostNoticesComponent implements OnInit {
	private postNotices: NoticeModel[] = [];
	private redirectUri: string;
	private baseUri = '/tdstm'
	private signOutUri = `${this.baseUri}/auth/signOut`

	/**
	 * @constructor
	 * @param {NoticeService} noticeService
	 */
	constructor(
		private dialogService: UIDialogService,
		private noticeService: NoticeService,
		private windowService: WindowService,
		private router: Router) {
	}

	ngOnInit() {
		this.noticeService.getPostNotices()
			.subscribe((response: PostNoticeResponse) => {
				this.redirectUri = `${this.baseUri}${response.redirectUri}`;
				this.postNotices = response.notices.map((notice: NoticeModel) => {
					notice.sequence = notice.sequence || 0;
					return notice;
				});

				this.showNotices();
			});
	}

	showNotices(): void {
		this.showMandatoryNotices()
			.then(() => {
				setTimeout(() => {
					this.showStandardNotices()
						.then(() => {
							this.navigateTo(this.redirectUri);
						})
						.catch((error) => console.log(error));
				}, 200);
			})
			.catch(() => {
				// throught the window service because the route is not handled
				// by the angular router
				this.navigateTo(this.signOutUri);
			});
	}

	filterPostNotices(mandatory: boolean): any[] {
		return this.postNotices
			.filter((notice) => mandatory ? notice.needAcknowledgement : !notice.needAcknowledgement)
			.map((notice: NoticeModel) => {
				return {...notice, notShowAgain: false};
			})
			.sort((a, b) => SortUtils.compareByProperty(a, b, 'sequence'));

	}

	showStandardNotices() {
		const notices = this.filterPostNotices(false);

		return notices.length ? this.dialogService
				.open(StandardNoticesComponent, [ {provide: Notices, useValue: {notices: notices}}])
				:
				Promise.resolve(true);
	}

	showMandatoryNotices() {
		const notices = this.filterPostNotices(true);

		return notices.length ? this.dialogService
				.open(MandatoryNoticesComponent, [ {provide: Notices, useValue: {notices: notices}}])
				:
				Promise.resolve(true);
	}
	private navigateTo(uri: string) {
		// Navigate throught the window service because that routes are not handled
		// by the angular router
		this.windowService.getWindow().location.assign(uri);
	}
}