import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
	selector: 'tds-report-toggle-filters',
	template: `
		<div class="row">
			<div class="col-sm-12 text-right">
				<tds-button-custom
					isIconButton="true"
					icon="filter"
					tooltip="Show/Hide Filters"
					[style.visibility]="disabled ? 'hidden' : 'visible'"
					(click)="onToggle()">
				</tds-button-custom>
			</div>
		</div>
	`})
export class ReportToggleFiltersComponent {

	@Input('hideFilters') hideFilters = false;
	@Input('disabled') disabled = false;
	@Output('toggle') toggleEmitter = new EventEmitter<any>();

	onToggle(): void {
		this.toggleEmitter.emit(this.hideFilters);
	}

}