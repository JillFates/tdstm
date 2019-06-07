import {Component, Input, OnInit} from '@angular/core';
import {CountDownItem} from './model/pie-countdown.model';

import {PreferenceService, PREFERENCES_LIST} from '../../services/preference.service';

@Component({
	selector: 'tds-pie-countdown',
	template: `
		<div class="pie-countdown">
			<div class="pie-countdown-container">
				<tds-button-custom
					icon="refresh"
					title="Refresh"
					isIconButton="true"
					class="component-action-reload pull-righ"
					(click)="onReload()">
				</tds-button-custom>

				<kendo-dropdownlist
					[(ngModel)]="selectedTimerOption"
					[data]="timerOptions"
					[valueField]="'seconds'"
					(valueChange)="onSelectedTimerOption($event)"
					[textField]="'description'">
				</kendo-dropdownlist>

				<div
					class="pie-countdown-timer"
					[ngClass]="getTimerClass()">
				</div>
			</div>
		</div>
	`
})
export class PieCountdownComponent implements OnInit {
	@Input() refreshEverySeconds = 0;
	public selectedTimerOption: CountDownItem = null;

	constructor(
		private preferenceService: PreferenceService,
	) {
		console.log('on constructor');
	}

	/**
	 * On input changes set the corresponding refresh event parameter
	 */
	ngOnInit() {
		this.preferenceService.getPreferences(PREFERENCES_LIST.EVENTDB_REFRESH)
			.subscribe((preferences: any[]) => {
				this.selectedTimerOption =  {
					seconds: parseInt(preferences[PREFERENCES_LIST.EVENTDB_REFRESH] || '0', 10),
					description: ''
				};
			});
	}

	public timerOptions: Array<CountDownItem> = [
		{seconds: 0, description: 'Manual'},
		{seconds: 30, description: '30 Sec'},
		{seconds: 60, description: '1 Min'},
		{seconds: 120, description: '2 Min'},
		{seconds: 300, description: '5 Min'},
		{seconds: 600, description: '10 Min'}
	];

	onSelectedTimerOption(timerOption: any): void {
		this.selectedTimerOption = {seconds : 0, description: ''};
		setTimeout(() => this.selectedTimerOption = timerOption, 0);
	}

	getTimerClass(): string {
		const seconds = this.selectedTimerOption && this.selectedTimerOption.seconds || '';

		return seconds ? `timer-${seconds}-seconds` : '';
	}
}