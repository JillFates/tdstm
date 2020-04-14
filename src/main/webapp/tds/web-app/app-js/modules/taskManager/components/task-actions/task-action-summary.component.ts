import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import { Component } from '@angular/core';
import { TaskDetailModel } from '../../model/task-detail.model';
import { TaskService } from '../../service/task.service';

@Component({
	template: `
		<div tds-autofocus
			 tds-handle-escape
				 id="task-action-summary"
				 class="task-action-summary modal fade in"
				 (escPressed)="cancelCloseDialog()"
				 data-backdrop="static"
				 tabindex="-1"
				 role="dialog">
			<div class="modal-dialog modal-md" role="document">
				<div class="tds-modal-content with-box-shadow resizable tds-angular-component-content">
					<div class="modal-header">
						<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog($event)">
							<clr-icon aria-hidden="true" shape="close"></clr-icon>
						</button>
						<h4 class="modal-title">Action Summary</h4>
					</div>
					<div class="modal-body">
						<div class="modal-body-container">
								<div class="box-body">
									<div class="action-info">
										<div *ngIf="taskActionSummary && !taskActionSummary.isRemote" class="row">
											<div class="col-md-2">
												<label>Method:</label>
											</div>
											<div class="col-md-10">
												{{taskActionSummary.method}}
											</div>
										</div>
										<div class="row">
											<div class="col-md-2">
												<label>Description:</label>
											</div>
											<div class="col-md-10">
											<textarea type="text"
																readonly
																class="form-control"
																[(ngModel)]="taskActionSummary.description">
											</textarea>
											</div>
										</div>
										<div class="row">
											<div class="col-md-2">
												<label>Type:</label>
											</div>
											<div class="col-md-10">
												{{taskActionSummary.type}}
											</div>
										</div>
										<div class="row">
											<div class="col-md-2">
												<label>Is Remote:</label>
											</div>
											<div class="col-md-10">
												<clr-checkbox-wrapper class="inline">
													<input
														clrCheckbox
														type="checkbox"
														[checked]="taskActionSummary.isRemote"
														[disabled]="true"
													/>
                								</clr-checkbox-wrapper>
											</div>
										</div>
										<div *ngIf="taskActionSummary && taskActionSummary.isRemote" class="row">
											<div class="col-md-2">
												<label>Script:</label>
											</div>
											<div class="col-md-10">
											<textarea type="text"
																readonly
																class="form-control"
																[(ngModel)]="taskActionSummary.script">
											</textarea>
											</div>
										</div>
									</div>
									<div class="row param-table">
										<div class="col-md-12">
											<div class="row header">
												<div class="col-sm-3">Parameter Name</div>
												<div class="col-sm-3">Context</div>
												<div class="col-sm-3">Field Name</div>
												<div class="col-sm-3">Value</div>
											</div>
											<div class="row" *ngFor="let param of taskActionSummary.methodParams">
												<div class="col-sm-3">{{param.paramName}}</div>
												<div class="col-sm-3">{{param.context}}</div>
												<div class="col-sm-3">{{param.fieldName}}</div>
												<div class="col-sm-3">{{taskActionSummary.methodParamsValues[param.paramName]}}</div>
											</div>
										</div>
									</div>
								</div>
						</div>
					</div>
					<div class="modal-footer form-group-center">
						<tds-button (click)="cancelCloseDialog($event)" icon="ban" title="Cancel">Cancel</tds-button>
					</div>
				</div>
			</div>
		</div>

	`,
	selector: 'tds-task-action-summary',
	styles: []
})
export class TaskActionSummaryComponent extends UIExtraDialog {
	taskActionSummary: any;
	constructor(private taskDetailModel: TaskDetailModel, private taskService: TaskService) {
		super('#task-action-summary');
		this.taskActionSummary = {methodParams: []};
		this.taskService.getTaskActionSummary(this.taskDetailModel.id).subscribe(result => {
			this.taskActionSummary = result;
		});
	}

	/**
	 * Closes current modal.
	 */
	public cancelCloseDialog(): void {
		this.dismiss();
	}
}
