import { Component, Input } from '@angular/core';

@Component({
	selector: 'tds-task-status-indicator',
	template: `
		<span [ngSwitch]="propertyName">
			<span *ngSwitchCase="'status'">
				<span [ngClass]="'status status-' + value" [ngSwitch]="value">
					<fa-icon *ngSwitchCase="'Ready'" [icon]="['fas', 'thumbs-up']"></fa-icon>
					<fa-icon *ngSwitchCase="'Started'" [icon]="['fas', 'circle-notch']"></fa-icon>
					<fa-icon *ngSwitchCase="'Hold'" [icon]="['fas', 'pause']"></fa-icon>
					<fa-icon *ngSwitchCase="'Completed'" [icon]="['fas', 'check']"></fa-icon>
					<fa-icon *ngSwitchCase="'Pending'" [icon]="['fas', 'hourglass-start']"></fa-icon>
					<fa-icon *ngSwitchCase="'Planned'" [icon]="['fas', 'square']"></fa-icon>
				</span>
				{{ displayLabel ? value : '' }}
			</span>
			<span *ngSwitchDefault [title]="value">{{ value }}</span>
		</span>
	`,
})
export class TaskStatusIndicatorComponent {
	@Input() propertyName: string;
	@Input() value: string;
	@Input() displayLabel = true;
}
