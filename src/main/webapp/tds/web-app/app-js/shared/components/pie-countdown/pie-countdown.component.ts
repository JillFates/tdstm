import {Component, Input, OnInit, EventEmitter, Output} from '@angular/core';
import {CountDownItem} from './model/pie-countdown.model';

import {PreferenceService, PREFERENCES_LIST} from '../../services/preference.service';

@Component({
	selector: 'tds-pie-countdown',
	template: `
		<div class="pie-countdown">
			<div class="pie-countdown-container">
				<span class="glyphicon glyphicon-refresh refresh" aria-hidden="true" (click)="onReload()"></span>

				<kendo-dropdownlist
					[(ngModel)]="selectedTimerOption"
					[data]="timerOptions"
					[valueField]="'seconds'"
					(valueChange)="onSelectedTimerOption($event)"
					[textField]="'description'">
				</kendo-dropdownlist>

				<div class="pie-countdown-timer" [ngClass]="getTimerClass()"></div>
			</div>
		</div>
	`
})
export class PieCountdownComponent implements OnInit {
	@Output() timeout: EventEmitter<void> = new EventEmitter<void>();
	@Input() refreshEverySeconds = 0;
	public selectedTimerOption: CountDownItem = null;
	private interval: any;

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
				// this.interval =  setInterval(() => this.notifyTimeout(), this.selectedTimerOption.seconds * 1000);
				this.setCurrentInterval(this.selectedTimerOption.seconds * 1000);
			});
	}

	private setCurrentInterval(milliseconds: number): void {
		if (this.interval) {
			clearInterval(this.interval);
			this.interval = null;
		}
		if (milliseconds) {
			this.interval =  setInterval(() => this.notifyTimeout(), milliseconds);
		}
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
		setTimeout(() => {
			this.selectedTimerOption = timerOption;
			this.setCurrentInterval(this.selectedTimerOption.seconds * 1000);
		}, 0);

	}

	getTimerClass(): string {
		const seconds = this.selectedTimerOption && this.selectedTimerOption.seconds || '';

		return seconds ? `timer-${seconds}-seconds` : '';
	}

	notifyTimeout(): void {
		console.log('----Time out----');
		this.timeout.emit();
	}
}