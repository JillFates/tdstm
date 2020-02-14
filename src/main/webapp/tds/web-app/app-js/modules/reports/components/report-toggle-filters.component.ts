import {Component, EventEmitter, Input, Output} from '@angular/core';
import { HeaderActionButtonData } from 'tds-component-library';

@Component({
	selector: 'tds-report-toggle-filters',
	styles: [`
		.buttons-container {
			display: flex;
		}
	`],
	template: `
		<div class="row">
			<div class="col-sm-12">
				<div class="pull-right buttons-container">
                    <tds-button-custom
                            isIconButton="true"
                            icon="filter"
                            tooltip="Show/Hide Filters"
                            [style.visibility]="disabled ? 'hidden' : 'visible'"
                            (click)="onToggle()">
                    </tds-button-custom>
                    <tds-grid-header-action-buttons
                            (refresh)="onReload()"
                            [actionButtons]="headerActionButtons">
                    </tds-grid-header-action-buttons>
				</div>
			</div>
		</div>
	`})
export class ReportToggleFiltersComponent {
	@Input('hideFilters') hideFilters = false;
	@Input('disabled') disabled = false;
	@Output('toggle') toggleEmitter = new EventEmitter<any>();
	@Output('reload') refreshEmitter = new EventEmitter<any>();
	public headerActionButtons: HeaderActionButtonData[] = [];

	onToggle(): void {
		this.toggleEmitter.emit(this.hideFilters);
	}

	onReload(): void {
		this.refreshEmitter.emit(null);
	}

}
