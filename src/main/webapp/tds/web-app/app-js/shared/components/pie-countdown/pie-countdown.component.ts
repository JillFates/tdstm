import {Component} from '@angular/core';

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
					[defaultItem]="defaultTimerOption"
					[data]="timerOptions"
					[valueField]="'value'"
					(valueChange)="onSelectedTimerOption($event)"
					[textField]="'name'">
				</kendo-dropdownlist>

				<div class="pie-countdown-timer" [ngClass]="selectedTimer.value"></div>
			</div>
		</div>
	`
})
export class PieCountdownComponent {
	public defaultTimerOption = {
		value: '', name: 'Manual'
	};
	public selectedTimer = this.defaultTimerOption;

	public timerOptions = [
		{value: 'timer-30-seconds', name: '30 Sec'},
		{value: 'timer-60-seconds', name: '1 Min'},
		{value: 'timer-120-seconds', name: '2 Min'},
		{value: 'timer-300-seconds', name: '5 Min'},
		{value: 'timer-600-seconds', name: '10 Min'}
	];

	onSelectedTimerOption(timerOption: any): void {
		this.selectedTimer = {value : '', name: ''};
		setTimeout(() => this.selectedTimer = timerOption, 0);
	}

	constructor() {
		console.log('on constructor');
	}
}