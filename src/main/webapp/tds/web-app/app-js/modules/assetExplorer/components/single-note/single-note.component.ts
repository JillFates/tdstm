import { Component, OnInit } from '@angular/core';
import { SingleNoteModel } from './model/single-note.model';
import { ModalType } from '../../../../shared/model/constants';
import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: `single-note`,
	template: `
		<div tds-handle-escape
				 (escPressed)="cancelCloseDialog()"
				 class="single-note-component modal fade in allow-modal-background"
				 id="single-note-component"
				 data-backdrop="static"
				 tabindex="-1"
				 role="dialog">
			<div class="modal-dialog modal-md" role="document">
				<div class="tds-modal-content has-side-nav with-box-shadow resizable" [style.width.px]="500">
					<div class="modal-header">
						<button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
							<clr-icon aria-hidden="true" shape="close"></clr-icon>
						</button>
						<h4 class="modal-title">{{singleNoteModel.modal.title}}</h4>
					</div>
					<div class="modal-body">
						<div class="modal-body-container">
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
					</div>
					<div class="modal-sidenav form-group-center">
						<nav class="btn-link">
							<tds-button-save (click)="onSave()"
															 *ngIf="singleNoteModel.modal.type !== modalType.VIEW"
															 [disabled]="note.trim() === ''">
							</tds-button-save>
							<tds-button-cancel (click)="cancelCloseDialog()"></tds-button-cancel>
						</nav>
					</div>
				</div>
			</div>
		</div>

	`,
	styles: []
})
export class SingleNoteComponent extends UIExtraDialog implements OnInit {
	public modalType = ModalType;
	private note: string;

	constructor(
		public singleNoteModel: SingleNoteModel,
		public promptService: UIPromptService,
		private translatePipe: TranslatePipe,
	) {
		super('#single-note-component');
	}

	ngOnInit(): void {
		this.note = '';
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
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
		this.close(this.note);
	}
}
