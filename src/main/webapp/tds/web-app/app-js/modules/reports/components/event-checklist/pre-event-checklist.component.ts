import {
	ChangeDetectorRef,
	Component,
	OnInit,
	OnDestroy,
	ViewEncapsulation
} from '@angular/core';

import {
	DomSanitizer,
	SafeHtml
} from '@angular/platform-browser';

import {
	BehaviorSubject,
	Observable,
	Subject
} from 'rxjs';

import {
	map,
	mergeMap,
	scan,
	switchMap,
	takeUntil,
	withLatestFrom,
} from 'rxjs/operators';
import {
	clone,
	compose,
	pathOr,
} from 'ramda';

import {ActivatedRoute} from '@angular/router';
import {State as GridState} from '@progress/kendo-data-query';
import {GridComponent} from '@progress/kendo-angular-grid';
import {
	GridDataResult,
	DataStateChangeEvent
} from '@progress/kendo-angular-grid';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ReportsService} from '../../service/reports.service';

declare var jQuery: any;

@Component({
	selector: 'tds-event-checklist',
	// encapsulation: ViewEncapsulation.None,
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
export class PreEventCheckListSelectorComponent implements OnInit, OnDestroy {
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
	 * Emit the destroy event to complete and close all current observables
	 */
	ngOnDestroy() {
		// on destroy
	}

	onGenerateReport(eventId: string) {
		console.log('Generating event:', eventId);
		this.reportsService.getPreventsCheckList(eventId)
			.subscribe((content) => {
				console.log('The result is');
				console.log(content);
				console.log('---------------');
				this.html =  this.sanitizer.bypassSecurityTrustHtml(content);
			});
	}
}
