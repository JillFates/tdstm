// Angular
import {Component, ViewChild, OnInit, ElementRef} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model
import {AssetComment} from './model/asset-coment.model';
import {TranslatePipe} from '../../pipes/translate.pipe';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';

@Component({
	selector: 'dependent-comment',
	template: `
        <div class="dependent-comment-component">
            <form name="dependentForm" role="form" data-toggle="validator" #dependentForm='ngForm'>
                <div class="box-body">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
								<textarea
                                        #dependentComment
                                        clrTextarea
                                        required
                                        rows="15"
                                        id="dependentComment"
                                        name="dependentComment"
                                        class="form-control"
                                        [(ngModel)]="assetCommentModel.comment"
                                ></textarea>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
	`,
})
export class DependentCommentComponent extends Dialog implements OnInit {
	@ViewChild('dependentForm', {static: false}) dependentForm: NgForm;
	@ViewChild('dependentComment', {static: false}) dependentComment: ElementRef;

	public assetCommentModel: AssetComment = null;

	constructor(
		private translatePipe: TranslatePipe,
		private dialogService: DialogService,
	) {
		super();
	}

	ngOnInit(): void {
		this.assetCommentModel = this.data.assetComment;

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => true,
			disabled: () => !this.dependentForm.dirty || !this.dependentForm.valid,
			type: DialogButtonType.ACTION,
			action: this.onUpdateComment.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		setTimeout(() => {
			super.onSetUpFocus(this.dependentComment);
		});
	}

	/**
	 * On EscKey Pressed close the dialog.
	 */
	public onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	public cancelCloseDialog(): void {
		if (this.dependentForm.dirty) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe(
				(data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
	}

	public onUpdateComment(): void {
		super.onAcceptSuccess(this.assetCommentModel);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		super.onCancelClose();
	}

}
