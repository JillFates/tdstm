import {Component, ViewChild, ElementRef} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import {AssetComment} from './model/asset-coment.model';
import {UIPromptService} from '../../directives/ui-prompt.directive';
import {NgForm} from '@angular/forms';
import {TranslatePipe} from '../../pipes/translate.pipe';

@Component({
	selector: 'dependent-comment',
	template: `
        <div class="modal fade in" id="dependent-comment-component"
             tds-handle-escape (escPressed)="cancelCloseDialog()"
             data-backdrop="static" tabindex="-1" role="dialog">
            <div class="modal-dialog tds-modal-content with-box-shadow has-side-nav modal-md" role="document">
                    <div class="modal-header">
                        <tds-button-custom
                                class="close"
                                [icon]="'close'"
                                (click)="cancelCloseDialog()"
                                [displayLabel]="false"
                                [flat]="true">
                        </tds-button-custom>
                        <div class="modal-title-container">
                            <div class="modal-title">
                                Comment for {{assetCommentModel.dialogTitle}}
                            </div>
                        </div>
                    </div>
                    <div class="modal-body">
                        <div class="modal-body-container">
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
                    </div>
                    <div class="modal-sidenav btn-link">
	                    <tds-button-edit class="selected-button"></tds-button-edit>
						<tds-button-save (click)="onUpdateComment()" [disabled]="dependentForm && !dependentForm.dirty"></tds-button-save>
						<tds-button-cancel (click)="cancelCloseDialog()" data-dismiss="modal"></tds-button-cancel>
                    </div>
            </div>
        </div>
	`,
	styles: [`
			div.modal-title {
                width: 453px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
			}
	`]
})
export class DependentCommentComponent extends UIExtraDialog {
	@ViewChild('dependentForm', {static: false}) dependentForm: NgForm;

	constructor(
		private translatePipe: TranslatePipe,
		private promptService: UIPromptService,
		public assetCommentModel: AssetComment) {
		super('#dependent-comment-component');
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
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
	}

	public onUpdateComment(): void {
		this.close(this.assetCommentModel);
	}

}
