import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { CountDownItem } from './model/pie-countdown.model';

import {
	PreferenceService,
	PREFERENCES_LIST,
} from '../../services/preference.service';

/**
 * Also known as timer component.
 */
@Component({
	selector: 'tds-pie-countdown',
	template: `
		<div class="pie-countdown">
			<div class="pie-countdown-container">
                <tds-button-custom icon="sync"
                                   *ngIf="!hideRefresh"
                                   [displayLabel]="false" tooltip="Refresh"
                                   (click)="onManualUpdate()">
                </tds-button-custom>
				<kendo-dropdownlist
					[(ngModel)]="selectedTimerOption"
					[data]="timerOptions"
					[valueField]="'seconds'"
					(valueChange)="onSelectedTimerOption($event)"
					[textField]="'description'"
				>
				</kendo-dropdownlist>
				<div
					*ngIf="
						selectedTimerOption && selectedTimerOption.seconds > 0
					"
					class="pie-countdown-timer"
					[ngClass]="getTimerClass()"
				></div>
			</div>
		</div>
	`,
})
export class PieCountdownComponent implements OnInit {
	@Output() timeout: EventEmitter<void> = new EventEmitter<void>();
	@Output() valueChange: EventEmitter<any> = new EventEmitter<any>();
	@Input() refreshEverySeconds = 0;
	@Input() hideRefresh = false;
	@Input() refreshPreference: string;
	@Input() customOptions: Array<CountDownItem>;
	public selectedTimerOption: CountDownItem = null;
	private interval: any;
	public timerOptions: Array<CountDownItem> = [
		{ seconds: 0, description: 'Manual' },
		{ seconds: 30, description: '30 Sec' },
		{ seconds: 60, description: '1 Min' },
		{ seconds: 120, description: '2 Min' },
		{ seconds: 300, description: '5 Min' },
		{ seconds: 600, description: '10 Min' },
	];

	constructor(private preferenceService: PreferenceService) {}

	/**
	 * On input changes set the corresponding refresh event parameter
	 */
	ngOnInit() {
		if (this.customOptions) {
			this.timerOptions = this.customOptions;
		}
		let refreshPref = PREFERENCES_LIST.EVENTDB_REFRESH;
		if (this.refreshPreference) {
			refreshPref = this.refreshPreference;
		}
		this.preferenceService
			.getPreferences(refreshPref)
			.subscribe((preferences: any[]) => {
				this.selectedTimerOption = {
					seconds: parseInt(preferences[refreshPref] || '0', 10),
					description: '',
				};
				this.setCurrentInterval(this.selectedTimerOption.seconds);
				this.valueChange.emit(this.selectedTimerOption);
			});
	}

	/**
	 * Set the current interval function based on the current selected unit time
	 * @param {number} seconds Seconds value
	 */
	private setCurrentInterval(seconds: number): void {
		if (this.interval) {
			clearInterval(this.interval);
		}
		if (seconds) {
			this.interval = setInterval(
				() => this.notifyTimeout(),
				seconds * 1000
			);
		}
	}

	/**
	 * selecting a timer clear the previous interval and set the new one
	 * @param {any} timerOption current timer option selected
	 */
	onSelectedTimerOption(timerOption: any): void {
		let refreshPref = PREFERENCES_LIST.EVENTDB_REFRESH;
		if (this.refreshPreference) {
			refreshPref = this.refreshPreference;
		}
		this.preferenceService
			.setPreference(refreshPref, timerOption.seconds)
			.subscribe(() => {
				/* saved preference */
			});
		this.selectedTimerOption = { seconds: 0, description: '' };
		setTimeout(() => {
			this.selectedTimerOption = timerOption;
			this.setCurrentInterval(this.selectedTimerOption.seconds);
		}, 0);
		this.valueChange.emit(timerOption);
	}

	/**
	 * Based on the current timer option selected, get the css corresponding class
	 * @returns {string} Corresponding css class
	 */
	getTimerClass(): string {
		const seconds =
			(this.selectedTimerOption && this.selectedTimerOption.seconds) ||
			'';
		return seconds ? `timer-${seconds}-seconds` : '';
	}

	/**
	 * Notify to the host component about a countdown timeout event
	 */
	notifyTimeout(): void {
		this.timeout.emit();
	}

	/**
	 * Handle update started by the user
	 * Grab the reference to the current refresh setting, reset it and restore the reference
	 */
	onManualUpdate() {
		const currentSelected = this.selectedTimerOption;
		this.onSelectedTimerOption(this.timerOptions[0]);
		setTimeout(() => {
			this.onSelectedTimerOption(currentSelected);
			this.notifyTimeout();
		}, 100);
	}
}
