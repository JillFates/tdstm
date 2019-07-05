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
	showTo = 4;

	constructor() {
		this.categories = [
			'',
			'Tasks',
			'Planned Start',
			'Planned Completion',
			'Actual Start',
			'Actual Completion'
		];

		this.steps.push([
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
		]);

		this.steps.push([
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '0%', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''}
		]);

		this.steps.push([
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '0 (of 30)', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''}
		]);

		this.steps.push([
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '09/30/2013 6:59 PM', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''}
		]);

		this.steps.push([
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '07/07/2020 3:00 AM', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''}
		]);

		this.steps.push([
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''}
		]);

		this.steps.push([
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''},
			{text: '', classes: ''}
		]);
	}
}
