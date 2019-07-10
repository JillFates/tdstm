// Angular
import {Component, Input, Output, EventEmitter} from '@angular/core';
import {EventRowType} from './../../model/event.model';

@Component({
	selector: 'tds-bundle-steps',
	templateUrl: 'bundle-steps.component.html'
})
export class BundleStepsComponent {
	@Input() bundleSteps: any;
	@Output() changeTab: EventEmitter<number> = new EventEmitter<number>();

	public colSize: number;
	public showFrom: number;
	public elementsToShow: number;
	public RowType = EventRowType;

	constructor() {
		console.log('On constructor');
		this.setInitialConfiguration();
	}

	onBack(): void {
		if (this.showFrom > 0) {
			this.showFrom -= 1;
		}
	}

	setInitialConfiguration(): void {
		this.colSize = 1;
		this.showFrom = 0;
		this.elementsToShow = 9;
	}

	onNext(): void {
		if (this.bundleSteps &&  (this.showFrom + this.elementsToShow + 1) <= this.bundleSteps.columnsLength)  {
			this.showFrom += 1;
		}
	}

	getColumns(row: any): any {
		if (!this.bundleSteps) {
			return [];
		}

		return row.slice(this.showFrom, this.showFrom + this.elementsToShow);
	}

	onChangeTab(selecteEvent: any): void {
		this.setInitialConfiguration();
		this.changeTab.emit(this.bundleSteps.moveBundleList[selecteEvent.index].id);
	}
}
