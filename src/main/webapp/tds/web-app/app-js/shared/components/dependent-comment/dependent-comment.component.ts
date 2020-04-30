import {Component, ViewChild, ElementRef, OnInit} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import {AssetComment} from './model/asset-coment.model';
import {UIPromptService} from '../../directives/ui-prompt.directive';
import {NgForm} from '@angular/forms';
import {TranslatePipe} from '../../pipes/translate.pipe';
import {Dialog, DialogButtonModel, DialogButtonType} from 'tds-component-library';

@Component({
	selector: 'dependent-comment',
	template: `
        <div id="dependent-comment-component">
			<form name="dependentForm" role="form" data-toggle="validator" #dependentForm='ngForm'>
				<div class="box-body">
					<div class="row">
						<div class="col-md-12">
							<div class="form-group">
								<textarea rows="15" id="dependentComment" name="dependentComment" class="form-control" [(ngModel)]="assetCommentModel.comment"></textarea>
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

	public assetCommentModel: AssetComment = new AssetComment();

	constructor(
		private translatePipe: TranslatePipe,
		private promptService: UIPromptService,
	) {
		super();
	}

	ngOnInit(): void {
		this.assetCommentModel = this.data.assetComment;

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => true,
			disabled: () => !this.dependentForm && !this.dependentForm.dirty,
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
	}

	/**
	 * On EscKey Pressed close the dialog.
	 */
	public onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	public cancelCloseDialog(): void {
		if (this.dependentForm.dirty) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
				.then(confirm => {
					if (confirm) {
						super.onCancelClose();
					}
				})
				.catch((error) => console.log(error));
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
