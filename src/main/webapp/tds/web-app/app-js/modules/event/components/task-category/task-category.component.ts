// Angular
import {Component, Input, Output, EventEmitter, HostListener, ViewChild, ElementRef} from '@angular/core';
import {EventRowType} from './../../model/event.model';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'tds-task-category',
	templateUrl: 'task-category.component.html'
})
export class TaskCategoryComponent {
	@Input() taskCategories: any;
	@Output() changeTab: EventEmitter<number> = new EventEmitter<number>();
	@ViewChild('categoryContainer') categoryContainer: ElementRef;

	public userTimeZone = '';
	public dateFormat = '';
	public dateTimeFormat = '';
	public colSize: number;
	public currentScroll: number;
	public RowType = EventRowType;
	public DateUtils = DateUtils;
	public categories = [
		'Category',
		'Percent completed',
		'Task completed',
		'Earliest Estimated Start',
		'Latest Estimated Completion',
		'Actual Start',
		'Actual Completion',
	];

	constructor(private preferenceService: PreferenceService) {
		this.setInitialConfiguration();
	}

	public handleScroll(e): void {
		this.currentScroll = e.srcElement.scrollLeft;
	}

	/**
	 * Sets the current position of the scrollbar for the category list
	 * @param scroll - The scroll position to start at.
	 */
	public setContainerScroll(scroll: number): void {
		if(this.categoryContainer) {
			this.categoryContainer.nativeElement.scrollLeft = scroll;
		}
	}

	/**
	 * Set the initial configuration to determine how many elements to show
	*/
	private setInitialConfiguration(): void {
		this.colSize = 2;

		this.preferenceService.getPreferences(PREFERENCES_LIST.CURR_TZ, PREFERENCES_LIST.CURRENT_DATE_FORMAT)
		.subscribe((preferences) => {
			this.userTimeZone =  preferences.CURR_TZ;
			this.dateFormat = preferences.CURR_DT_FORMAT || this.preferenceService.getUserDateFormat();
			this.dateTimeFormat = `${this.dateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}`;
		});
	}
}
