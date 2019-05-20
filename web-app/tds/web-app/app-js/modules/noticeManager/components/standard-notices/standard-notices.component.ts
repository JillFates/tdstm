// Angular
import {Component, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
import {Observable} from 'rxjs';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Model
import {NoticeModel, Notices} from '../../model/notice.model';
import {NoticeCommonComponent} from './../notice-common'

@Component({
	selector: 'tds-standard-notices',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/standard-notices/standard-notices.component.html'
})
export class StandardNoticesComponent extends NoticeCommonComponent implements OnInit {
	private notices: NoticeModel[];
	acceptAgreement: boolean;

	constructor(
		protected model: Notices,
		protected activeDialog: UIActiveDialogService,
		protected noticeService: NoticeService,
		protected sanitizer: DomSanitizer) {
			super(sanitizer);
	}

	ngOnInit() {
		this.notices = this.model.notices;
	}

	/**
	 * Set the flag to dont show the notice any more
	*/
	onAccept() {
		const updates = this.notices
			.filter((notice) => notice.notShowAgain)
			.map((notice) => this.noticeService.setAcknowledge(notice.id));

		if (updates.length) {
			Observable.forkJoin(updates)
				.subscribe((results) => this.activeDialog.close());
		} else {
			this.activeDialog.close();
		}
	}

	/**
	 * Close the current active dialog
	*/
	onCancel() {
		this.activeDialog.close();
	}

}
