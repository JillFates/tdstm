import {
	ChangeDetectorRef,
	Component,
	OnInit,
	OnDestroy,
} from '@angular/core';

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

interface ComponentState {
}

@Component({
	selector: 'tds-event-checklist',
	template: `
		<div style="width: 30%">
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
			<div>Output:</div>
			<div>
				<input type="radio" id="output" name="output" checked>
				<label for="output">Web</label>
			</div>
			<div>
				<!--<tds-button-custom tooltip="Create Comment" icon="comment-o" *ngIf="!isComment" (click)="createComment(dataItem, rowIndex)"></tds-button-custom>-->
				<tds-button-custom tooltip="Generate report" icon="check-double" ></tds-button-custom>
			</div>
		</div>
	`
})
export class PreEventCheckListSelectorComponent implements OnInit, OnDestroy {
	protected state: ComponentState;
	protected model = {
		events: [],
		defaultEvent: {id: null, text: ''}
	};

	constructor(
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
}
