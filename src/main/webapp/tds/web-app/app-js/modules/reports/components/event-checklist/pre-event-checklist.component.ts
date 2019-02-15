import {
	ChangeDetectorRef,
	Component,
	OnInit
} from '@angular/core';

import {
	DomSanitizer,
	SafeHtml
} from '@angular/platform-browser';

import {
	Observable,
} from 'rxjs';

import {
	pathOr,
} from 'ramda';

import {ActivatedRoute} from '@angular/router';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ReportsService} from '../../service/reports.service';

declare var jQuery: any;

@Component({
	selector: 'tds-event-checklist',
	template: `
		<div class="pre-event-checklist">
			<div class="report-controls">
				<div style="width: 20%">
					<div>
						<kendo-dropdownlist
								name="event"
								class="form-control"
								[data]="model.events"
								[textField]="'text'"
								[valueField]="'id'"
								[(ngModel)]="model.defaultEvent">
						</kendo-dropdownlist>
					</div>
					<div class="actions">
						<div>
							<div>Output:</div>
							<div>
								<input type="radio" id="output" name="output" checked>
								<label for="output">Web</label>
							</div>
						</div>
						<div>
							<tds-button-custom
									class="btn-primary"
									(click)="onGenerateReport(model.defaultEvent.id)"
									title="Generate"
									tooltip="Generate report"
									icon="check-square">
							</tds-button-custom>
						</div>
					</div>
				</div>
			</div>
			<div [innerHTML]="html"></div>
		</div>
	`
})
export class PreEventCheckListSelectorComponent implements OnInit {
	protected model = {
		events: [],
		defaultEvent: {id: null, text: ''}
	};
	protected html: SafeHtml;

	constructor(
		private sanitizer: DomSanitizer,
		private route: ActivatedRoute,
		private changeDetectorRef: ChangeDetectorRef,
		private translatePipe: TranslatePipe,
		private reportsService: ReportsService) {
	}

	ngOnInit() {
		const commonCalls = [this.reportsService.getEvents(), this.reportsService.getDefaults()];

		// on init
		Observable.forkJoin(commonCalls)
			.subscribe((results) => {
				const [events, defaults] = results;
				this.model.events = events.map((item) => ({id: item.id.toString(), text: item.name}));
				this.model.defaultEvent.id = pathOr(null, ['preferences', 'TASK_CREATE_EVENT'], defaults);
			})
	}

	/**
	 * Call the endpoint to generate the pre-event-checklist report
	 * @param {string} eventId Report id to generate
	 */
	onGenerateReport(eventId: string): void {
		this.reportsService.getPreventsCheckList(eventId)
			.subscribe((content) => {
				this.html =  this.sanitizer.bypassSecurityTrustHtml(content);
			});
	}
}
