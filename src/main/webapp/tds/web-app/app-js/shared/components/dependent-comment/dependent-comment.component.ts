import {Component, ViewChild, ElementRef} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import {AssetComment} from './model/asset-coment.model';

@Component({
	selector: 'dependent-comment',
	template: `
        <div class="modal fade in" id="dependent-comment-component" data-backdrop="static" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-md" role="document">
                <div class="tds-modal-content resizable" [style.width.px]="500">
                    <div class="modal-header">
						<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()">
							<clr-icon aria-hidden="true" shape="close"></clr-icon>
						</button>
                        <h4 class="modal-title">Comment for {{assetCommentModel.dialogTitle}}</h4>
                    </div>
                    <div class="modal-body">
                        <div class="modal-body-container">
                            <form name="dependentForm" role="form" data-toggle="validator" #dependentForm='ngForm' class="form-horizontal left-alignment">
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
                    <div class="modal-footer form-group-center">
						<tds-button theme="primary" (click)="onUpdateComment()" icon="floppy">Save</tds-button>
						<tds-button (click)="cancelCloseDialog()" icon="ban" data-dismiss="modal">Cancel</tds-button>
                    </div>
                </div>
            </div>
        </div>
	`,
	styles: [`
			div.modal-body,
			div.box-body
			{
				padding-bottom: 0px;
            }
			div.modal-title {
                width: 453px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
			}
	`]
})
export class DependentCommentComponent extends UIExtraDialog {

	constructor(
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
		this.dismiss();
	}

	public onUpdateComment(): void {
		this.close(this.assetCommentModel);
	}

}