// Angular
import {Component, ElementRef, Inject, OnInit, Renderer2} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Services
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
// Components
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Models
import {COLUMN_MIN_WIDTH, ActionType} from '../../../dataScript/model/data-script.model';
import {GRID_DEFAULT_PAGINATION_OPTIONS, GRID_DEFAULT_PAGE_SIZE} from '../../../../shared/model/constants';
import {UserContextModel} from '../../../security/model/user-context.model';
// Kendo
import {CompositeFilterDescriptor, State, process} from '@progress/kendo-data-query';
import {CellClickEvent, GridDataResult, PageChangeEvent} from '@progress/kendo-angular-grid';
import {EventColumnModel, EventModel} from '../../model/event.model';
import {EventService} from '../../service/event.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {EventCreateComponent} from '../create/event-create.component';
import {EventViewEditComponent} from '../view-edit/event-view-edit.component';

declare var jQuery: any;

@Component({
	selector: 'event-list',
	templateUrl: 'event-list.component.html',
})
export class EventListComponent implements OnInit {
	protected gridColumns: any[];

	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	public skip = 0;
	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public eventColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public gridData: GridDataResult;
	public resultSet: EventModel[];
	public selectedRows = [];
	public dateFormat = '';

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private eventService: EventService,
		private prompt: UIPromptService,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private renderer: Renderer2,
		private preferenceService: PreferenceService) {
		this.state.take = this.pageSize;
		this.state.skip = this.skip;
		this.resultSet = this.route.snapshot.data['events'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.eventColumnModel = new EventColumnModel(`{0:${dateFormat}}`);
				this.gridColumns = this.eventColumnModel.columns.filter((column) => column.type !== 'action');
			})
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
	}

	protected onFilter(column: any): void {
		const root = this.eventService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.eventService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	/* protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			this.selectRow(event['dataItem'].id);
			this.openEventDialogViewEdit(event['dataItem'], ActionType.VIEW);
		}
	}*/

	protected reloadData(): void {
		this.eventService.getEvents().subscribe(
			(result) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
				setTimeout(() => this.forceDisplayLastRowAddedToGrid() , 100);
			},
			(err) => console.log(err));
	}

	/**
	 * This work as a temporary fix.
	 * TODO: talk when Jorge Morayta get's back to do a proper/better fix.
	 */
	private forceDisplayLastRowAddedToGrid(): void {
		const lastIndex = this.gridData.data.length - 1;
		let target = this.elementRef.nativeElement.querySelector(`tr[data-kendo-grid-item-index="${lastIndex}"]`);
		this.renderer.setStyle(target, 'height', '36px');
	}

	private openEventDialogCreate(): void {
		this.dialogService.open(EventCreateComponent, []).then(result => {
			// update the list to reflect changes, it keeps the filter
			this.reloadData();
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	private selectRow(dataItemId: number): void {
		this.selectedRows = [];
		this.selectedRows.push(dataItemId);
	}

	/**
	 * Make the entire header clickable on Grid
	 * @param event: any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
	}

	protected showEvent(id): void {
		this.dialogService.open(EventViewEditComponent,
			[{provide: 'id', useValue: id}]).then(result => {
			this.reloadData();
		}).catch(result => {
			this.reloadData();
		});
	}

	/**
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		this.gridData = process(this.resultSet, this.state);
		// Adjusting the locked column(s) height to prevent cut-off issues.
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}
}