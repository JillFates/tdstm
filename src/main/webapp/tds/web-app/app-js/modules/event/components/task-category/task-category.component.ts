// Angular
import {
	Component,
	Input,
	Output,
	EventEmitter,
	HostListener,
	ViewChild,
	ElementRef,
	OnInit,
	AfterContentInit, AfterViewInit
} from '@angular/core';
import {EventRowType} from './../../model/event.model';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'tds-task-category',
	templateUrl: 'task-category.component.html'
})
export class TaskCategoryComponent implements OnInit, AfterViewInit {
	@Input() taskCategories: any;
	@Input() scrollPosition: number;
	@Output() changeTab: EventEmitter<number> = new EventEmitter<number>();
	@ViewChild('categoryContainer', {static: false}) categoryContainer: ElementRef;

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
		'Estimated Start',
		'Estimated Completion',
		'Actual Start',
		'Actual Completion',
	];

	constructor(private preferenceService: PreferenceService) {
	}

	ngOnInit() {
		this.setInitialConfiguration();
		this.removeGeneralCategory();
	}

	/**
	* Removes the "General" Category from the taskCategories structure
	* so it doesn't show up in the Categories table.
	* See TM-17688
	*/
	removeGeneralCategory() {
		// find the index in the taskCategories structure that corresponds to the "general" category
		let index = this.taskCategories.tasks[0].findIndex( elem => elem.text === 'general');
		// remove all rows of the "general" category
		this.taskCategories.tasks.map( elem => elem.splice(index, 1));
	}

	ngAfterViewInit() {
		if (this.categoryContainer) {
			this.categoryContainer.nativeElement.scrollLeft = this.currentScroll;
		}
	}

	public handleScroll(e): void {
		this.currentScroll = e.srcElement.scrollLeft;
	}

	/**
	 * Set the initial configuration to determine how many elements to show
	*/
	private setInitialConfiguration(): void {
		this.colSize = 2;
		this.currentScroll = this.scrollPosition;

		this.preferenceService.getPreferences(PREFERENCES_LIST.CURR_TZ, PREFERENCES_LIST.CURRENT_DATE_FORMAT)
		.subscribe((preferences) => {
			this.userTimeZone =  preferences.CURR_TZ;
			this.dateFormat = preferences.CURR_DT_FORMAT || this.preferenceService.getUserDateFormat();
			this.dateTimeFormat = `${this.dateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}`;
		});
	}
}
