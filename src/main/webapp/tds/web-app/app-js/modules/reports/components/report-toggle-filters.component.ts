import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
	selector: 'tds-report-toggle-filters',
	template: `
		<div class="row" [ngStyle]="{'color': !hideFilters ? 'lightgrey' : 'inherit'}">
			<div class="col-sm-12 text-right">
				<tds-button-custom
					isIconButton="true"
					icon="cog"
					tooltip="Show/Hide Filters"
					(click)="onToggle()">
				</tds-button-custom>
			</div>
		</div>
	`})
export class ReportToggleFiltersComponent {

	@Input('hideFilters') hideFilters = false;
	@Output('toggle') toggleEmitter = new EventEmitter<any>();

	onToggle(): void {
		this.toggleEmitter.emit(this.hideFilters);
	}

}
