// Angular
import {Component, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
import {Observable} from 'rxjs';
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
	private notices: NoticeModel[];
	private currentNoticeIndex: number;
	acceptAgreement: boolean;

	constructor(
		private model: StandardNotices,
		private activeDialog: UIActiveDialogService,
		private noticeService: NoticeService,
		private sanitizer: DomSanitizer) {
	}

	ngOnInit() {
		this.notices = this.model.notices;

		this.currentNoticeIndex = 0;
	}

	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	// TODO move to base
	protected onCancel() {
		this.activeDialog.dismiss();
	}

	// TODO move to base
	sanitizeHTML(html: string) {
		return this.sanitizer.bypassSecurityTrustHtml(html);
	}

	protected onAccept() {
		const updates = this.notices
			.filter((notice) => notice.notShowAgain)
			.map((notice) => this.noticeService.setAcknowledge(notice.id));

		Observable.forkJoin(updates)
			.subscribe((results) => this.activeDialog.dismiss());
	}

}
