// Angular
import {Component, Input, Output, EventEmitter} from '@angular/core';
import {EventRowType} from './../../model/event.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'tds-task-category',
	templateUrl: 'task-category.component.html'
})
export class TaskCategoryComponent {
	@Input() taskCategories: any;
	@Output() changeTab: EventEmitter<number> = new EventEmitter<number>();

	public userTimeZone = '';
	public dateFormat = '';
	public dateTimeFormat = '';
	public colSize: number;
	public showFrom: number;
	public elementsToShow: number;
	public RowType = EventRowType;
	public DateUtils = DateUtils;
	public categories = [
		'Category',
		'Percent completed',
		'Task completed',
		'Estimated Start',
		'Estimated Completion',
		'Actual Start',
		'Actual Completion',
	];

	constructor(private preferenceService: PreferenceService) {
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

		this.preferenceService.getUserDatePreferenceAsKendoFormat().subscribe((dateFormat) => {
			this.userTimeZone = this.preferenceService.getUserTimeZone();
			this.dateFormat = this.preferenceService.getUserCurrentDateFormatOrDefault(); // this.preferenceService.getUserDateFormat();
			this.dateTimeFormat = `${this.dateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}`;
		});
	}

	/**
	 * Increase the showFrom indicator one value just if doing this doesn't cause an index overflow
	*/
	public onNext(): void {
		if (this.hasTasks &&  (this.showFrom + this.elementsToShow + 1) <= this.taskCategories.columns)  {
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
		if (!this.hasTasks) {
			return [];
		}

		return row.slice(this.showFrom, this.showFrom + this.elementsToShow);
	}

	/**
	 * Return a boolean indicating if there are task present
	*/
	private hasTasks(): boolean {
		return this.taskCategories && this.taskCategories.tasks;
	}
}
