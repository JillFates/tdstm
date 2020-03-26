// Angular
import {Component, Input, OnInit} from '@angular/core';
// Model
import { SingleNoteModel } from './model/single-note.model';
import { ModalType } from '../../../../shared/model/constants';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Service
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
// Other
import * as R from 'ramda';

@Component({
	selector: `single-note`,
	template: `
		<div class="single-note-component">
			<form name="dependentForm" role="form" data-toggle="validator" #dependentForm='ngForm'>
				<div class="box-body">
					<div class="row">
						<div class="col-md-12">
							<div class="form-group single-component">
						<textarea rows="8" id="singleNote" name="singleNote" class="form-control"
											[(ngModel)]="note"
											*ngIf="singleNoteModel.modal.type !== modalType.VIEW" tds-autofocus>
						</textarea>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>
	`,
	styles: []
})
export class SingleNoteComponent extends Dialog implements OnInit {
	@Input() data: any;

	public modalType = ModalType;
	private note: string;
	public singleNoteModel: SingleNoteModel;

	constructor(
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
	) {
		super();
	}

	ngOnInit(): void {
		this.singleNoteModel = R.clone(this.data.singleNoteModel);
		this.note = '';

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.singleNoteModel.modal.type !== this.modalType.VIEW,
			disabled: () => this.note.trim() === '',
			type: DialogButtonType.ACTION,
			action: this.onSave.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
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
		this.singleNoteModel.modal.title = 'Note Edit';
		this.singleNoteModel.modal.type = ModalType.EDIT;
	}

	protected onSave(): void {
		super.onAcceptSuccess({ note: this.note });
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
