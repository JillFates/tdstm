import {
	Component,
	OnInit
} from '@angular/core';

import {AlertType} from '../../../../shared/model/alert.model';

import {
	SafeHtml
} from '@angular/platform-browser';

import {
	Observable,
} from 'rxjs';

import {
	pathOr,
} from 'ramda';

import {ReportComponent} from '../report.component';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ReportsService} from '../../service/reports.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

declare var jQuery: any;

@Component({
	selector: 'tds-event-checklist',
	template: `
		<div class="content body">
			<tds-report-toggle-filters [hideFilters]="hideFilters" (toggle)="toggleFilters($event)"></tds-report-toggle-filters>
			<section class="box-body">
				<form class="formly form-horizontal">
					<div class="box box-primary">
						<div class="box-header"></div>
						<div class="box-body">
							<div class="filters-wrapper" [hidden]="hideFilters">
								<div class="form-group row">
									<label class="col-sm-1 control-label">Events</label>
									<div class="col-sm-3">
										<kendo-dropdownlist
											name="event"
											class="form-control"
											[data]="model.events"
											[textField]="'text'"
											[valueField]="'id'"
											[(ngModel)]="model.defaultEvent">
										</kendo-dropdownlist>
									</div>
								</div>
								<div class="form-group row">
									<div class="col-sm-offset-1 col-sm-3">
										<tds-button-custom
												class="btn-primary"
												(click)="onGenerateReport()"
												title="Generate"
												tooltip="Generate report"
												[icon]="'table'">
										</tds-button-custom>
									</div>
								</div>
								<hr />
							</div>
							<div class="pre-event-checklist">
								<div [innerHTML]="html"></div>
							</div>
						</div>
					</div>
				</form>
			</section>
		</div>
	`
})
export class PreEventCheckListSelectorComponent extends ReportComponent implements OnInit {
	model = {
		events: [],
		defaultEvent: {id: null, text: ''}
	};
	html: SafeHtml;
	isReportFailing: boolean;

	constructor(
		dialogService: UIDialogService,
		reportsService: ReportsService,
		private notifierService: NotifierService,
		private translatePipe: TranslatePipe) {
			super(reportsService, dialogService);
	}

	ngOnInit() {
		const commonCalls = [this.reportsService.getEvents(), this.reportsService.getDefaults()];

		Observable.forkJoin(commonCalls)
			.subscribe((results) => {
				const [events, defaults] = results;
				this.model.events = events.map((item) => ({id: item.id.toString(), text: item.name}));
				this.model.defaultEvent.id = pathOr(null, ['preferences', 'TASK_CREATE_EVENT'], defaults);
				if (this.model.defaultEvent.id === 'null' || this.model.defaultEvent.id === null) {
						this.model.defaultEvent.id = this.model.events[0].id;
				}
			})
	}

	/**
	 * Call the endpoint to generate the pre-event-checklist report
	 */
	onGenerateReport(): void {
		this.isReportFailing = false;

		this.reportsService.getPreventsCheckList(this.model.defaultEvent.id)
			.subscribe((content) => {
				let errorMessage = 'Unknown error';
				this.hideFilters = true;
				try {
					const errorResponse = JSON.parse(content);
					if (errorResponse && errorResponse.errors && errorResponse.errors.length) {
						errorMessage = errorResponse.errors.shift();
					}

					this.isReportFailing = true;
					this.notifierService.broadcast({
						name: AlertType.DANGER,
						message: errorMessage
					});
				} catch (error) {
					console.log(error.message || error);
					errorMessage = '';
				}

				this.html = (errorMessage) ? this.reportsService.getSafeHtml('') : this.reportsService.getSafeHtml(content);
			});
	}
}
