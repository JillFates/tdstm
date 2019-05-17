// Angular
import {Component, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
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

	constructor(
		protected model: Notices,
		protected activeDialog: UIActiveDialogService,
		protected noticeService: NoticeService,
		protected sanitizer: DomSanitizer) {
			super(sanitizer);
	}

	ngOnInit() {
		this.notices = this.model.notices.filter((notice) => notice.needAcknowledgement);
		this.currentNoticeIndex = 0;
	}

	getCurrentNotice() {
		return this.notices[this.currentNoticeIndex];
	}

	onCancel() {
		this.activeDialog.dismiss();
	}

	onAccept() {
		this.noticeService
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
