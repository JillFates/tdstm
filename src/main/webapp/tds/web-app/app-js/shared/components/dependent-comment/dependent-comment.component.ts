import {Component, ViewChild, ElementRef} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import {AssetComment} from './model/asset-coment.model';

@Component({
	selector: 'dependent-comment',
	template: `
        <div class="modal fade in" id="dependent-comment-component" data-backdrop="static" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-md" role="document">
                <div class="modal-content resizable" [style.width.px]="500">
                    <div class="modal-header">
                        <button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
                            <span aria-hidden="true">×</span>
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
                        <button type="button" (click)="onUpdateComment()" class="btn btn-primary pull-left"><span
                                class="fa fa-fw fa-floppy-o"></span> Save
                        </button>
                        <button (click)="cancelCloseDialog()" type="button" class="btn btn-default pull-right">
                            <span class="glyphicon glyphicon-ban-circle"></span>
                            <span>Cancel</span>
                        </button>
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
	onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	protected onUpdateComment(): void {
		this.close(this.assetCommentModel);
	}

}