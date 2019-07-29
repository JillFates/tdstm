// Angular
import {Component, Input, Output, EventEmitter} from '@angular/core';
import {EventRowType, TaskCategoryCell} from './../../model/event.model';

@Component({
	selector: 'tds-bundle-steps',
	templateUrl: 'bundle-steps.component.html'
})
export class BundleStepsComponent {
	// Array<Array<TaskCategoryCell>
	@Input() bundleSteps: any;
	@Input() taskCategories: Array<Array<TaskCategoryCell>> = [];
	@Output() changeTab: EventEmitter<number> = new EventEmitter<number>();

	public colSize: number;
	public showFrom: number;
	public elementsToShow: number;
	public RowType = EventRowType;
	public categories = [
		'Category',
		'Estimated Start',
		'Estimated Finish',
		'Actual Start',
		'Actual Finish',
	];

	constructor() {
		this.setInitialConfiguration();
	}

	/**
	 * If showFrom start indicator is valid decrease its value
	*/
	public onBack(): void {
		if (this.showFrom > 0) {
			this.showFrom -= 1;
		}
	}

	/**
	 * Set the initial configuration to determine how many elements to show
	*/
	private setInitialConfiguration(): void {
		this.colSize = 2;
		this.showFrom = 0;
		this.elementsToShow = 6;
	}

	/**
	 * Increase the showFrom indicator one value just if doing this doesn't cause an index overflow
	*/
	public onNext(): void {
		if (this.bundleSteps &&  (this.showFrom + this.elementsToShow + 1) <= this.bundleSteps.columnsLength)  {
			this.showFrom += 1;
		}
	}

	/**
	 * Based on showFrom index indicator and elements to show
	 * extract from the array the items to be displayed
 	 * @param {any} row  Row containing all the available columns
	 * @returns {Array<any>} Array containing the columns covered by the interval defined
	*/
	public getColumns(row: any): any {
		if (!this.bundleSteps) {
			return [];
		}

		return row.slice(this.showFrom, this.showFrom + this.elementsToShow);
	}

	/**
	 * Reset the initial configuration and notify to the host component
	 * about a change tabe event
 	 * @param {any} selectedEvent Change event info
	*/
	public onChangeTab(selecteEvent: any): void {
		this.setInitialConfiguration();
		this.changeTab.emit(this.bundleSteps.moveBundleList[selecteEvent.index].id);
	}
}
