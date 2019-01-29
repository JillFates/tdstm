import {Component, OnInit} from '@angular/core';
import {SingleNoteModel} from './model/single-note.model';
import {ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `single-note`,
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/single-note/single-note.component.html',
	styles: []
})
export class SingleNoteComponent extends UIExtraDialog implements  OnInit {
	public modalType = ModalType;
	private note: string;

	constructor(public singleNoteModel: SingleNoteModel, public promptService: UIPromptService) {
		super('#single-note-component');
	}

	ngOnInit(): void {
		this.note = '';
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.note.trim() !== '';
	}

	/**
	 * Change to Edit view
	 */
	protected onEdit(): void {
		this.singleNoteModel.modal.title = 'Edit Comment';
		this.singleNoteModel.modal.type = ModalType.EDIT;
	}

	protected onSave(): void {
		this.close(this.note);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
	}
}