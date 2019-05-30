// Angular
import {Component, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserPostNoticesContextService} from '../../../user/service/user-post-notices-context.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Model
import {NoticeModel, Notices} from '../../model/notice.model';
import {NoticeCommonComponent} from './../notice-common'

@Component({
	selector: 'tds-mandatory-notices',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/mandatory-notices/mandatory-notices.component.html'
})
export class MandatoryNoticesComponent extends NoticeCommonComponent implements OnInit {
	private notices: NoticeModel[];
	private currentNoticeIndex: number;
	private postNoticesManager;

	constructor(
		protected model: Notices,
		protected activeDialog: UIActiveDialogService,
		protected noticeService: NoticeService,
		protected userContextService: UserPostNoticesContextService,
		protected sanitizer: DomSanitizer) {
			super(sanitizer);
	}

	ngOnInit() {
		this.userContextService.getUserPostNoticesContext()
		.subscribe((context) => {
			this.postNoticesManager = context.postNoticesManager;
			this.notices = this.model.notices.filter((notice) => notice.needAcknowledgement);
			this.currentNoticeIndex = 0;
		});
	}

	/**
	 * Get the notice which the current index is pointing out
	*/
	getCurrentNotice() {
		return this.notices[this.currentNoticeIndex];
	}

	/**
	 * On cancel dismiss the active dialog
	*/
	onCancel() {
		this.activeDialog.dismiss();
	}

	/**
	 * On accept call the endpoint to marck the notice to not be shown again
	*/
	onAccept() {
		this.postNoticesManager
			.setAcknowledge(this.notices[this.currentNoticeIndex].id)
				.subscribe(() => {
					if ((this.currentNoticeIndex + 1) >= this.notices.length) {
						this.activeDialog.close();
					} else {
						this.currentNoticeIndex += 1;
					}
				}, (err) => console.error(err));
	}
}
