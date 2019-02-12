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
		<div>Event CheckList</div>
	`
})
export class PreEventCheckListSelectorComponent implements OnInit, OnDestroy {
	protected state: ComponentState;

	constructor(
		private route: ActivatedRoute,
		private changeDetectorRef: ChangeDetectorRef,
		private translatePipe: TranslatePipe,
		private reportsService: ReportsService) {
	}

	ngOnInit() {
		// on init
		this.reportsService.getEvents()
			.subscribe((results) => {
				console.log('Events');
				console.log(results);
			})
	}

	/**
	 * Emit the destroy event to complete and close all current observables
	 */
	ngOnDestroy() {
		// on destroy
	}
}
