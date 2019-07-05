// Angular
import {Component, Input} from '@angular/core';

enum RowType {
	Header,
	Percents,
	Tasks,
	PlannedStart,
	PlannedCompletion,
	ActualStart,
	ActualCompletion
}

@Component({
	selector: 'tds-bundle-steps',
	templateUrl: 'bundle-steps.component.html'
})
export class BundleStepsComponent {
	headers = [];
	percents = [];
	steps = [];
	categories = [];
	colSize = 2;
	showFrom = 0;
	elementsToShow = 5;
	headerRow = [];

	constructor() {
		this.categories = [
			'',
			'Percents',
			'Tasks',
			'Planned Start',
			'Planned Completion',
			'Actual Start',
			'Actual Completion'
		];

		this.headerRow = [
			{text: 'Ready', classes: ''},
			{text: 'BackupDone', classes: ''},
			{text: 'Release', classes: ''},
			{text: 'Staged', classes: ''},
			{text: 'Powered On', classes: ''},
			{text: 'Completed', classes: ''},
			{text: 'Powered Down', classes: ''},
			{text: 'Unracking', classes: ''},
			{text: 'Reracking', classes: ''},
			{text: 'Powered On', classes: ''},
			{text: 'App Startup', classes: ''}
		];

		this.steps.push(this.headerRow);

		this.steps.push([
			{text: 'p 1', classes: ''},
			{text: 'p 2', classes: ''},
			{text: 'p 3', classes: ''},
			{text: 'p 4', classes: ''},
			{text: 'p 5', classes: ''},
			{text: 'p 6', classes: ''},
			{text: 'p 7', classes: ''},
			{text: 'p 8', classes: ''},
			{text: 'p 9', classes: ''},
			{text: 'p 10', classes: ''},
			{text: 'p 11', classes: ''}
		]);

		this.steps.push([
			{text: 't a', classes: ''},
			{text: 't b', classes: ''},
			{text: 't c', classes: ''},
			{text: 't d', classes: ''},
			{text: 't e', classes: ''},
			{text: 't f', classes: ''},
			{text: 't g', classes: ''},
			{text: 't h', classes: ''},
			{text: 't i', classes: ''},
			{text: 't j', classes: ''},
			{text: 't k', classes: ''}
		]);

		this.steps.push([
			{text: 'ps a', classes: ''},
			{text: 'ps b', classes: ''},
			{text: 'ps c', classes: ''},
			{text: 'ps d', classes: ''},
			{text: 'ps e', classes: ''},
			{text: 'ps f', classes: ''},
			{text: 'ps g', classes: ''},
			{text: 'ps h', classes: ''},
			{text: 'ps i', classes: ''},
			{text: 'ps j', classes: ''},
			{text: 'ps k', classes: ''}
		]);

		this.steps.push([
			{text: 'pc a', classes: ''},
			{text: 'pc b', classes: ''},
			{text: 'pc c', classes: ''},
			{text: 'pc d', classes: ''},
			{text: 'pc e', classes: ''},
			{text: 'pc f', classes: ''},
			{text: 'pc g', classes: ''},
			{text: 'pc h', classes: ''},
			{text: 'pc i', classes: ''},
			{text: 'pc j', classes: ''},
			{text: 'pc k', classes: ''}
		]);

		this.steps.push([
			{text: 'as a', classes: ''},
			{text: 'as b', classes: ''},
			{text: 'as c', classes: ''},
			{text: 'as d', classes: ''},
			{text: 'as e', classes: ''},
			{text: 'as f', classes: ''},
			{text: 'as g', classes: ''},
			{text: 'as h', classes: ''},
			{text: 'as i', classes: ''},
			{text: 'as j', classes: ''},
			{text: 'as k', classes: ''}
		]);

		this.steps.push([
			{text: 'ac a', classes: ''},
			{text: 'ac b', classes: ''},
			{text: 'ac c', classes: ''},
			{text: 'ac d', classes: ''},
			{text: 'ac e', classes: ''},
			{text: 'ac f', classes: ''},
			{text: 'ac g', classes: ''},
			{text: 'ac h', classes: ''},
			{text: 'ac i', classes: ''},
			{text: 'ac j', classes: ''},
			{text: 'ac k', classes: ''}
		]);
	}

	onBack(): void {
		if (this.showFrom > 0) {
			this.showFrom -= 1;
		}
	}

	onNext(): void {
		if ( (this.showFrom + this.elementsToShow + 1) <= this.headerRow.length)  {
			this.showFrom += 1;
		}
	}

	getColumns(row: any): any {
		return row.slice(this.showFrom, this.showFrom + this.elementsToShow);
	}
}
