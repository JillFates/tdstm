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
	private dataSignature: string;
	protected modelNotices: NoticeModel[];
	protected currentNoticeIndex: number;

	constructor(
		model: StandardNotices,
		public activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService,
		private noticeService: NoticeService,
		private promptService: UIPromptService,
		protected sanitizer: DomSanitizer) {

		this.modelNotices = model.notices.concat([])
			.map((notice: NoticeModel) => {
				return {...notice, notShowAgain: false};
			});

		this.dataSignature = JSON.stringify(this.modelNotices);
	}

	ngOnInit() {
		this.currentNoticeIndex = 0;
	}

	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.dismiss();
		}
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.modelNotices);
	}

	protected onBack() {
		if (this.currentNoticeIndex > 0) {
			this.currentNoticeIndex -= 1;
		}
		// this.activeDialog.dismiss();
	}

	protected onNext() {
		// handle save don't show again
		if ((this.currentNoticeIndex + 1) >= this.modelNotices.length) {
			this.activeDialog.close(true);
		}

		this.currentNoticeIndex += 1;
	}
}
