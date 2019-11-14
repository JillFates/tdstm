import { Component, Input } from '@angular/core';

@Component({
	selector: 'tds-task-status-indicator',
	template: `
		<div [ngSwitch]="propertyName">
			<span *ngSwitchCase="'status'">
				<span [ngClass]="'status status-' + value" [ngSwitch]="value">
					<clr-icon *ngSwitchCase="'Ready'" shape="thumbs-up" class="is-solid"></clr-icon>
					<clr-spinner *ngSwitchCase="'Started'" clrInline class="static"></clr-spinner>
					<clr-icon *ngSwitchCase="'Hold'" shape="pause" class="is-solid"></clr-icon>
					<clr-icon *ngSwitchCase="'Completed'" shape="check" class="is-solid"></clr-icon>
					<clr-icon *ngSwitchDefault shape="minus" class="is-solid"></clr-icon>
				</span>
				{{ value }}
			</span>
			<span *ngSwitchDefault>{{ value }}</span>
		</div>
	`,
})
export class TaskStatusIndicatorComponent {
	@Input() propertyName: string;
	@Input() value: string;
}
