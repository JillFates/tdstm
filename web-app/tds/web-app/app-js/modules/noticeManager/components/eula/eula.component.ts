// Angular
import {Component} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
// Service
import {NoticeService} from '../../service/notice.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Kendo
// Model
import {NoticeModel} from '../../model/notice.model';

@Component({
	selector: 'tds-eula',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/eula/eula.component.html'
})
export class EULAComponent {
	private dataSignature: string;
	protected model: NoticeModel;
	protected acceptAgreement = false;

	constructor(
		model: NoticeModel,
		public activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService,
		private noticeService: NoticeService,
		private promptService: UIPromptService,
		protected sanitizer: DomSanitizer) {

		this.model = {...model};
		this.dataSignature = JSON.stringify(this.model);
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
		return this.dataSignature !== JSON.stringify(this.model);
	}

	protected onBack() {
		this.activeDialog.dismiss();
	}

	protected onNext() {
		this.noticeService.setAcknowledge(this.model.id)
			.subscribe(() => this.activeDialog.close(true),
				(err) => console.error(err));
	}

}
