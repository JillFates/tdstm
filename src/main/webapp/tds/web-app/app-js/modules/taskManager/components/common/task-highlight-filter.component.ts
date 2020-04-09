import {
	Component,
	ElementRef,
	EventEmitter,
	HostListener,
	Input,
	Output, Renderer2,
	ViewChild
} from '@angular/core';
import {ReplaySubject} from 'rxjs';
import {distinctUntilChanged, skip, takeUntil} from 'rxjs/operators';
import {IGraphTask} from '../../model/graph-task.model';
import {ITaskHighlightOption, ITaskHighlightQuery} from '../../model/task-highlight-filter.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {TaskService} from '../../service/task.service';
import {Collision} from '@progress/kendo-angular-popup';

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
                            [(value)]="person"
                            [valueField]="'name'"
                            [textField]="'name'"
                            #personsCombobox>
                    </kendo-combobox>
		              </div>
                  <div class="popup-option">
	                  <label class="popup-label">Designated Team:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.teams"
					                  [(value)]="selectedTeam"
	                          [valueField]="'name'"
	                          [textField]="'name'"
					                  #teamCombobox>
	                  </kendo-combobox>
                  </div>
		              <div class="popup-option">
	                  <label class="popup-label">Application Owner or SMEs:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.ownersAndSmes"
					                  [(value)]="appOwner"
	                          [valueField]="'name'"
	                          [textField]="'name'"
					                  #ownerCombobox>
	                  </kendo-combobox>
		              </div>
                  <div class="popup-option">
	                  <label class="popup-label">Environment of Assets:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.environments"
					                  [(value)]="environment"
	                          [valueField]="'name'"
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
	                  ></tds-asset-tag-selector>
                  </div>
                  <div class="popup-option">
                      <label class="popup-label">Critical Path Mode:</label>
                      <kendo-combobox
                              [data]="['', 'Baseline', 'Realtime']"
                              [(value)]="criticalPathMode"
                              #environmentCombobox>
                      </kendo-combobox>
                  </div>
		              <br/>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
				                      [(ngModel)]="highlightOptions.showCycles"/>
                      <span class="highlight-checkmark"></span>
                      Cyclical Paths
                  </label>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
				                      [(ngModel)]="highlightOptions.withActions"/>
                      <span class="highlight-checkmark"></span>
                      With Actions
                  </label>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
				                      [(ngModel)]="highlightOptions.withTmdActions"/>
                      <span class="highlight-checkmark"></span>
                      With Actions requiring TMD
                  </label>
		              <div class="popup-option">
                      <button class="btn"(click)="tasksByQuery()">Search</button>
		              </div>
              </kendo-popup>
		      </div>
          <div class="disp-table-cell">
              <input
                      type="text"
                      class="form-control highlight-filter-control"
                      placeholder="Highlight filter"
                      [(ngModel)]="filterText"
                      (keydown)="onTextFilterChange()"
                      #highlightFilterText
              />
          </div>
          <div class="disp-table-cell">
              <fa-icon
                      [icon]="icons.faWindowClose"
                      (click)="clearTextFilter()"
                      [ngClass]="['clear-icon-button']"
                      [hidden]="!filterText || filterText.length < 1"></fa-icon>
          </div>
      </div>`
})
export class TaskHighlightFilter {
	@Input('highlightOptions') highlightOptions: ITaskHighlightOption;
	@Input('eventId') eventId: number;
	@Input('viewUnpublished') viewUnpublished: boolean;
	@Output() filteredTasks: EventEmitter<IGraphTask[]> = new EventEmitter();
	@ViewChild('highlightFilterText', {static: false}) highlightFilterText: ElementRef<HTMLElement>;
	@ViewChild('highlightFilterContainer', {static: false}) highlightFilterContainer: ElementRef<HTMLElement>;
	collision: Collision = { horizontal: 'flip', vertical: 'fit'};
	icons = FA_ICONS;
	filterText: string;
	textFilter: ReplaySubject<string> = new ReplaySubject<string>(1);
	unsubscribe$: ReplaySubject<void> = new ReplaySubject(1);
	teamHighlights$: ReplaySubject<any[]> = new ReplaySubject<any[]>(1);
	person: any;
	selectedTeam: any;
	appOwner: any;
	tags: any;
	environment: any;
	filterQueryObj: ITaskHighlightQuery;
	show: boolean;
	criticalPathMode: string;

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
				skip(3),
				distinctUntilChanged()
			).subscribe(text => this.tasksByQuery(text));
	}

	tasksByQuery(text?: string): void {
		this.filterQueryObj = {
			eventId: this.eventId,
			viewUnpublished: this.viewUnpublished ? 1 : 0,
			taskText: text || this.filterText || '',
			assignedPersonId: this.person && this.person.id || '',
			teams: this.selectedTeam && this.selectedTeam.name || '',
			ownerSmeId: this.appOwner && this.appOwner.id || '',
			environments: this.environment && this.environment.name || '',
			tagIds: this.tags && this.tags.tagIds || [],
			tagMatch: this.tags && this.tags.operator,
			criticalPathMode: this.criticalPathMode,
			cyclicalPath: this.highlightOptions && this.highlightOptions.showCycles ? '1' : '0',
			withActions: this.highlightOptions && this.highlightOptions.withActions ? '1' : '0',
			withTmdActions: this.highlightOptions && this.highlightOptions.withTmdActions ? '1' : '0'
		};
		this.taskService.findTasksByQuery(this.filterQueryObj)
			.subscribe(res => {
				const data = res.body && res.body.data.taskIds;
				if (data) {
					this.filteredTasks.emit(data);
				}
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

	togglePopup(): void {
		this.show = !this.show;
	}

	onTagValueChange(event: any): void {
		this.tags = {
			tagIds: event.tags.map(t => t.id),
			operator: event.operator
		};
	}

	hideHighlightFilterPopup(): void {
		this.show = false;
	}

	onTextFilterChange(): void {
		this.textFilter.next(this.filterText);
	}

	@HostListener('window:beforeunload', ['$event'])
	ngOnDestroy(): void {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}