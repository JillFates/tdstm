import {
	ChangeDetectionStrategy,
	Component,
	ElementRef,
	EventEmitter,
	HostListener,
	Input,
	Output,
	Renderer2,
	ViewChild
} from '@angular/core';
import {ReplaySubject} from 'rxjs';
import {distinctUntilChanged, filter, skip, takeUntil} from 'rxjs/operators';
import {IGraphTask} from '../../model/graph-task.model';
import {
	ITaskHighlightOption,
	ITaskHighlightQuery,
	TaskHighlightQueryModel
} from '../../model/task-highlight-filter.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {TaskService} from '../../service/task.service';
import {Collision} from '@progress/kendo-angular-popup';
import {AssetTagSelectorComponent} from '../../../../shared/components/asset-tag-selector/asset-tag-selector.component';

@Component({
	selector: 'task-highlight-filter',
	template: `
      <div class="highlight-filter-container">
		      <div	class="disp-table-cell">
              <button #anchor class="clr-btn advanced-filter-btn" (click)="togglePopup()">
                  <fa-icon [icon]="icons.faFilter"></fa-icon>
              </button>
              <kendo-popup
				              [collision]="collision"
                      [anchor]="anchor"
                      [ngClass]="'highlight-filter-popup'"
                      *ngIf="show">
		              <div class="popup-option">
                    <label class="popup-label">Assigned Person:</label>
                    <kendo-combobox
                            [data]="highlightOptions?.persons"
                            [(ngModel)]="filterQueryObj.assignedPersonId"
                            [valueField]="'id'"
                            [valuePrimitive]="true"
                            [textField]="'name'"
                            #personsCombobox>
                    </kendo-combobox>
		              </div>
                  <div class="popup-option">
	                  <label class="popup-label">Designated Team:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.teams"
					                  [(ngModel)]="filterQueryObj.teamCode"
	                          [valueField]="'id'"
                            [valuePrimitive]="true"
	                          [textField]="'name'"
					                  #teamCombobox>
	                  </kendo-combobox>
                  </div>
		              <div class="popup-option">
	                  <label class="popup-label">Application Owner or SMEs:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.ownersAndSmes"
					                  [(ngModel)]="filterQueryObj.ownerSmeId"
	                          [valueField]="'id'"
                            [valuePrimitive]="true"
	                          [textField]="'name'"
					                  #ownerCombobox>
	                  </kendo-combobox>
		              </div>
                  <div class="popup-option">
	                  <label class="popup-label">Environment of Assets:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.environments"
					                  [(ngModel)]="filterQueryObj.environment"
	                          [valueField]="'name'"
                            [valuePrimitive]="true"
	                          [textField]="'name'"
					                  #environmentCombobox>
	                  </kendo-combobox>
                  </div>
                  <div class="popup-option">
	                  <label class="popup-label">Tags assigned to Assets:</label>
	                  <tds-asset-tag-selector
					                  [class]="'highlight-tag-list-container'"
					                  [tagList]="highlightOptions?.tags"
					                  (valueChange)="onTagValueChange($event)"
					                  #assetTagSelector
	                  ></tds-asset-tag-selector>
                  </div>
                  <div class="popup-option">
                      <label class="popup-label">Critical Path Mode:</label>
                      <kendo-combobox
                              [data]="['', 'Baseline', 'Realtime']"
                              [(ngModel)]="filterQueryObj.criticalPathMode"
                              #environmentCombobox>
                      </kendo-combobox>
                  </div>
		              <br/>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
				                      [(ngModel)]="cyclicalP"
                              (change)="updateCyclicalPathProp($event)"
                      />
                      <span class="highlight-checkmark"></span>
                      Cyclical Paths
                  </label>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
                              [(ngModel)]="withActions"
                              (change)="updateWithActionProp($event)"
                      />
                      <span class="highlight-checkmark"></span>
                      With Actions
                  </label>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
                              [(ngModel)]="withTmd"
				                      (change)="updateWithTmdActionProp($event)"
                      />
                      <span class="highlight-checkmark"></span>
                      With Actions requiring TMD
                  </label>
		              <div class="popup-controls">
                      <button class="btn" (click)="tasksByQuery()" [disabled]="!isValid()">Search</button>
                      <button class="btn" (click)="clearForm()">Clear</button>
		              </div>
              </kendo-popup>
		      </div>
          <div class="disp-table-cell">
              <input
                      type="text"
                      class="form-control highlight-filter-control"
                      placeholder="Highlight filter"
                      [(ngModel)]="filterText"
                      (ngModelChange)="onTextFilterChange()"
                      [disabled]="!filterQueryObj"
                      #highlightFilterText/>
          </div>
          <div class="disp-table-cell">
              <fa-icon
                      [icon]="icons.faWindowClose"
                      (click)="clearTextFilter()"
                      [ngClass]="['clear-icon-button']"
                      [hidden]="!filterText || filterText.length < 1"></fa-icon>
          </div>
      </div>`,
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class TaskHighlightFilter {
	@Input('highlightOptions') highlightOptions: ITaskHighlightOption;
	@Input('eventId') eventId: number;
	@Input('viewUnpublished') viewUnpublished: boolean;
	@Output() filteredTasks: EventEmitter<IGraphTask[]> = new EventEmitter();
	@Output() clearFilters: EventEmitter<void> = new EventEmitter();
	@ViewChild('highlightFilterText', {static: false}) highlightFilterText: ElementRef<HTMLElement>;
	@ViewChild('highlightFilterContainer', {static: false}) highlightFilterContainer: ElementRef<HTMLElement>;
	@ViewChild('assetTagSelector', {static: false}) assetTagSelector: AssetTagSelectorComponent;
	collision: Collision = { horizontal: 'flip', vertical: 'flip'};
	icons = FA_ICONS;
	filterText: string;
	textFilter: ReplaySubject<string> = new ReplaySubject<string>(1);
	unsubscribe$: ReplaySubject<void> = new ReplaySubject(1);
	teamHighlights$: ReplaySubject<any[]> = new ReplaySubject<any[]>(1);
	tags: any;
	filterQueryObj: ITaskHighlightQuery = new TaskHighlightQueryModel();
	show: boolean;
	cyclicalP: boolean;
	withActions: boolean;
	withTmd: boolean;

	constructor(private taskService: TaskService, private renderer: Renderer2) {
		this.subscribeToHighlightFilter();
	}

	/**
	 * Highlight filter subscription
	 **/
	subscribeToHighlightFilter(): void {
		this.textFilter
			.pipe(
				takeUntil(this.unsubscribe$),
				skip(2),
				distinctUntilChanged(),
				filter(d => d && d.length > 2)
			).subscribe(text => this.tasksByQuery(text));
	}

	/**
	 * search tasks by query
	 * @param text
	 */
	tasksByQuery(text?: string): void {

		const query = {
			...this.filterQueryObj,
			eventId: this.eventId,
			viewUnpublished: this.viewUnpublished ? 1 : 0
		};

		if (text || this.filterText) {
			query.taskText = text || this.filterText;
		}

		if (this.tags && this.tags.tagIds) {
			query.tagIds = this.tags.tagIds;
			query.tagMatch = this.tags.operator || 'ANY';
		}

		this.taskService.findTasksByQuery(query)
			.subscribe(res => {
				const data = (res.body && res.body.data) && res.body.data.taskIds;
				this.filteredTasks.emit(data);
			});
	}

	/**
	 * When highlight filter change update search
	 **/
	highlightFilterChange(): void {
		this.textFilter.next(this.filterText);
	}

	/**
	 * Clear text filter
	 */
	clearTextFilter(): void {
		if (!this.filterText) { return; }
		this.highlightFilterText.nativeElement.nodeValue = '';
		this.filterText = '';
		this.textFilter.next(null);
	}

	/**
	 * toggle popup visibility
	 */
	togglePopup(): void {
		this.show = !this.show;
	}

	/**
	 * update tags on change
	 * @param event
	 */
	onTagValueChange(event: any): void {
		this.tags = {
			tagIds: event.tags.map(t => t.id),
			operator: event.operator
		};
	}

	/**
	 * Hides the filter popup
	 */
	hideHighlightFilterPopup(): void {
		this.show = false;
	}

	/**
	 * on text filter change, search matching tasks
	 */
	onTextFilterChange(): void {
		this.textFilter.next(this.filterText);
	}

	/**
	 * Clear form data
	 */
	clearForm(): void {
		this.filterQueryObj = new TaskHighlightQueryModel();
		this.filterText = '';
		this.cyclicalP = false;
		this.withActions = false;
		this.withTmd = false;
		this.assetTagSelector.clearTags();
		this.clearFilters.emit();
	}

	/**
	 * set cyclical property value on query obj
	 * @param value
	 */
	updateCyclicalPathProp(value: any): void {
		if (this.filterQueryObj) {
			this.filterQueryObj.cyclicalPath = value.target.checked ? 1 : 0;
		}
	}

	/**
	 * set Action property value on query obj
	 * @param value
	 */
	updateWithActionProp(value: any): void {
		if (this.filterQueryObj) {
			this.filterQueryObj.withActions = value.target.checked ? 1 : 0;
		}
	}

	/**
	 * set TMD Action property value on query obj
	 * @param value
	 */
	updateWithTmdActionProp(value: any): void {
		if (this.filterQueryObj) {
			this.filterQueryObj.withTmdActions = value.target.checked ? 1 : 0;
		}
	}

	private isValid(): boolean {
		const values = [];
		Object.keys(this.filterQueryObj)
			.forEach(k => {
				const v = this.filterQueryObj[k];
				if (k === 'assignedPersonId' && typeof v === 'number') {
					values.push(v);
				} else if (v) {
					values.push(v);
				}
			});
		return this.filterText && this.filterText.length > 2
			|| values.length > 0
			|| (this.tags && this.tags.tagIds) && this.tags.tagIds.length > 0;
	}

	@HostListener('window:beforeunload', ['$event'])
	ngOnDestroy(): void {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}