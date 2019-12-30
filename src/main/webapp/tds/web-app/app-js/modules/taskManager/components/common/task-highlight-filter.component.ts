import {
	Component,
	ElementRef,
	EventEmitter,
	HostListener,
	Input,
	OnChanges,
	Output,
	SimpleChanges,
	ViewChild
} from '@angular/core';
import {ReplaySubject} from 'rxjs';
import {distinctUntilChanged, skip, takeUntil} from 'rxjs/operators';
import {IGraphTask} from '../../model/graph-task.model';
import {ITaskHighlightOption, ITaskHighlightQuery} from '../../model/task-highlight-filter.model';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {TaskService} from '../../service/task.service';

@Component({
	selector: 'task-highlight-filter',
	template: `
      <div class="highlight-filter-container">
		      <div	class="disp-table-cell">
              <button #anchor class="clr-btn advanced-filter-btn" (click)="togglePopup()">
                  <fa-icon [icon]="icons.faFilter"></fa-icon>
              </button>
              <kendo-popup
                      [anchor]="anchor"
                      (anchorViewportLeave)="show = false"
                      [ngClass]="'highlight-filter-popup'"
                      *ngIf="show">
		              <div class="popup-option">
                    <label class="popup-label">Assigned Person:</label>
                    <kendo-combobox
                            [data]="highlightOptions?.persons"
                            [(value)]="persons"
                            [valueField]="'name'"
                            [textField]="'name'"
                            (valueChange)="tasksByQuery()"
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
	                          (valueChange)="tasksByQuery()"
					                  #teamCombobox>
	                  </kendo-combobox>
                  </div>
		              <div class="popup-option">
	                  <label class="popup-label">Application Owner or SMEs:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.ownerAndSmes"
					                  [(value)]="appOwner"
	                          [valueField]="'name'"
	                          [textField]="'name'"
	                          (valueChange)="tasksByQuery()"
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
	                          (valueChange)="tasksByQuery()"
					                  #environmentCombobox>
	                  </kendo-combobox>
                  </div>
                  <div class="popup-option">
	                  <label class="popup-label">Tags assigned to Assets:</label>
	                  <kendo-combobox
					                  [data]="highlightOptions?.environments"
					                  [(value)]="environment"
	                          [valueField]="'name'"
	                          [textField]="'name'"
	                          (valueChange)="tasksByQuery()"
					                  #environmentCombobox>
	                  </kendo-combobox>
                  </div>
		              <br/>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
				                      [(ngModel)]="highlightOptions.showCycles"
				                      (ngModelChange)="tasksByQuery()" />
                      <span class="checkmark"></span>
                      Cyclical Paths
                  </label>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
				                      [(ngModel)]="highlightOptions.withActions"
				                      (ngModelChange)="tasksByQuery()" />
                      <span class="checkmark"></span>
                      With Actions
                  </label>

                  <label class="task-chk-container">
                      <input
				                      type="checkbox"
				                      [(ngModel)]="highlightOptions.withTmdActions"
				                      (ngModelChange)="tasksByQuery()" />
                      <span class="checkmark"></span>
                      With Actions requiring TMD
                  </label>
              </kendo-popup>
		      </div>
          <div class="disp-table-cell">
              <input
                      type="text"
                      class="form-control highlight-filter-control"
                      placeholder="Highlight filter"
                      [(ngModel)]="filterText"
                      (ngModelChange)="highlightFilterChange()"
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
      </div>`,
})
export class TaskHighlightFilter {
	@Input('highlightOptions') highlightOptions: ITaskHighlightOption;
	@Output() filteredTasks: EventEmitter<IGraphTask[]> = new EventEmitter();
	@ViewChild('highlightFilterText', {static: false}) highlightFilterText: ElementRef<HTMLElement>;
	icons = FA_ICONS;
	filterText: string;
	textFilter: ReplaySubject<string> = new ReplaySubject<string>(1);
	unsubscribe$: ReplaySubject<void> = new ReplaySubject(1);
	teamHighlights$: ReplaySubject<any[]> = new ReplaySubject<any[]>(1);
	persons: any;
	selectedTeam: any;
	appOwner: any;
	tag: any;
	environment: any;
	filterQueryObj: ITaskHighlightQuery;
	show: boolean;

	constructor(private taskService: TaskService) {
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
				distinctUntilChanged()
			).subscribe(this.tasksByQuery);
	}

	tasksByQuery(text?: string): void {
		this.filterQueryObj = {
			text: text || this.filterText,
			persons: this.persons && this.persons.name,
			teams: this.selectedTeam && this.selectedTeam.name,
			ownerAndSmes: this.appOwner && this.appOwner.name,
			environments: this.environment && this.environment.name,
			tag: this.tag && this.tag.name
		};
		this.taskService.findTasksByQuery(this.filterQueryObj)
			.subscribe(res => {
				const data = res.body && res.body.data;
				if (data) {
					this.filteredTasks.emit(data.map(d => ({key: d})));
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

	@HostListener('window:beforeunload', ['$event'])
	ngOnDestroy(): void {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}
}