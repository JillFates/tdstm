import {Component, OnInit} from '@angular/core';
import {SingleNoteModel} from './model/single-note.model';
import {ModalType} from '../../../../shared/model/constants';
import {UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `single-note`,
	template: `
        <div tds-handle-escape (escPressed)="cancelCloseDialog()" class="single-note-component modal fade in" id="single-note-component" data-backdrop="static" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-md" role="document">
                <div class="modal-content resizable" [style.width.px]="500">
                    <div class="modal-header">
                        <button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                        <h4 class="modal-title">{{singleNoteModel.modal.title}}</h4>
                    </div>
                    <div class="modal-body">
                        <div class="modal-body-container">
                            <form name="dependentForm" role="form" data-toggle="validator" #dependentForm='ngForm' class="form-horizontal left-alignment">
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
                    </div>
                    <div class="modal-footer form-group-center">
                        <tds-button-save class="btn-primary pull-left"
                                         (click)="onSave()"
                                         *ngIf="singleNoteModel.modal.type !== modalType.VIEW"
                                         [disabled]="note.trim() === ''" >
                        </tds-button-save>
                        <tds-button-cancel class="pull-right"
                                           (click)="cancelCloseDialog()">
                        </tds-button-cancel>
                    </div>
                </div>
            </div>
        </div>

	`,
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
	public cancelCloseDialog(): void {
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