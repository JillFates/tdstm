// Angular
import {Component, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Model
import {NoticeModel, StandardNotices} from '../../model/notice.model';

@Component({
	selector: 'tds-standard-notices',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/standard-notices/standard-notices.component.html'
})
export class StandardNoticesComponent implements OnInit {
	private modelNotices: NoticeModel[];
	private currentNoticeIndex: number;
	acceptAgreement: boolean;

	constructor(
		private model: StandardNotices,
		private activeDialog: UIActiveDialogService,
		private noticeService: NoticeService,
		private sanitizer: DomSanitizer) {
	}

	ngOnInit() {
		const mandatory = this.model.notices.filter((notice) => notice.needAcknowledgement);
		const regular = this.model.notices.map((notice: NoticeModel) => {
			return {...notice, notShowAgain: false};
		});

		this.modelNotices = mandatory.concat(regular) ;
		this.currentNoticeIndex = 0;
	}

	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	getCurrentNotice() {
		return this.modelNotices[this.currentNoticeIndex];
	}

	protected onCancel() {
		this.activeDialog.dismiss();
	}

	private resetAgreements() {
		this.acceptAgreement = false;
		this.getCurrentNotice().notShowAgain = false;
	}

	sanitizeHTML(html: string) {
		return this.sanitizer.bypassSecurityTrustHtml(html);
	}

	protected onAccept() {
		/*
		this.noticeService.setAcknowledge(this.modelNotices[this.currentNoticeIndex].id)
		.subscribe(() => this.activeDialog.close(true),
			(err) => console.error(err));
		*/

		/*
		// handle save don't show again
		if ((this.currentNoticeIndex + 1) >= this.modelNotices.length) {
			// this.activeDialog.close(true);
			this.noticeService.setAcknowledge(this.modelNotices[this.currentNoticeIndex].id)
			.subscribe(() => this.activeDialog.close(true),
				(err) => console.error(err));
		}
		*/

		if ((this.currentNoticeIndex + 1) >= this.modelNotices.length) {
			this.activeDialog.dismiss();
		} else {
			this.resetAgreements();
			this.currentNoticeIndex += 1;
		}
	}
}
