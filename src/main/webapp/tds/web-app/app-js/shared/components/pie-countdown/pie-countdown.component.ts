import {Component, Input, OnInit, EventEmitter, Output} from '@angular/core';
import {CountDownItem} from './model/pie-countdown.model';

import {PreferenceService, PREFERENCES_LIST} from '../../services/preference.service';

@Component({
	selector: 'tds-pie-countdown',
	template: `
		<div class="pie-countdown">
			<div class="pie-countdown-container">
				<span class="glyphicon glyphicon-refresh refresh" aria-hidden="true" (click)="notifyTimeout()"></span>
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
	public timerOptions: Array<CountDownItem> = [
		{seconds: 0, description: 'Manual'},
		{seconds: 30, description: '30 Sec'},
		{seconds: 60, description: '1 Min'},
		{seconds: 120, description: '2 Min'},
		{seconds: 300, description: '5 Min'},
		{seconds: 600, description: '10 Min'}
	];

	constructor(private preferenceService: PreferenceService) {
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
				this.setCurrentInterval(this.selectedTimerOption.seconds * 1000);
			});
	}

	/**
	 * Set the current interval function based on the current selected unit time
 	 * @param {number} milliseconds Milliseconds value
	*/
	private setCurrentInterval(milliseconds: number): void {
		if (this.interval) {
			clearInterval(this.interval);
		}
		if (milliseconds) {
			this.interval =  setInterval(() => this.notifyTimeout(), milliseconds);
		}
	}

	/**
	 * selecting a timer clear the previous interval and set the new one
 	 * @param {any} timerOption current timer option selected
	*/
	onSelectedTimerOption(timerOption: any): void {
		this.preferenceService.setPreference(PREFERENCES_LIST.EVENTDB_REFRESH, timerOption.seconds)
			.subscribe(() => {
				this.selectedTimerOption = {seconds : 0, description: ''};
				setTimeout(() => {
					this.selectedTimerOption = timerOption;
					this.setCurrentInterval(this.selectedTimerOption.seconds * 1000);
				}, 0)
			});
	}

	/**
	 * Based on the current timer option selected, get the css corresponding class
	 * @returns {string} Corresponding css class
	*/
	getTimerClass(): string {
		const seconds = this.selectedTimerOption && this.selectedTimerOption.seconds || '';

		return seconds ? `timer-${seconds}-seconds` : '';
	}

	/**
	 * Notify to the host component about a countdown timeout event
	*/
	notifyTimeout(): void {
		this.timeout.emit();
	}
}