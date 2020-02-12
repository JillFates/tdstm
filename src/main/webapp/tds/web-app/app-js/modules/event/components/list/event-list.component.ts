// Angular
import {
	Component,
	OnInit,
	ViewChild
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
// Store
import { Store } from '@ngxs/store';
// Actions
import { SetEvent } from '../../action/event.actions';
// Services
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { Permission } from '../../../../shared/model/permission.model';
import { EventsService } from '../../service/events.service';
import {
	PreferenceService
} from '../../../../shared/services/preference.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
// Components
import {
	ColumnHeaderData,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData
} from 'tds-component-library';
import {
	CellClickEvent
} from '@progress/kendo-angular-grid';
import { EventColumnModel, EventModel } from '../../model/event.model';
import { EventCreateComponent } from '../create/event-create.component';
import { EventViewEditComponent } from '../view-edit/event-view-edit.component';

@Component({
	selector: 'event-list',
	templateUrl: 'event-list.component.html',
})
export class EventListComponent implements OnInit {
	public gridRowActions: GridRowAction[];

	public headerActions: HeaderActionButtonData[];

	public gridSettings: GridSettings = {
		defaultSort: [{ field: 'title', dir: 'asc' }],
		sortSettings: { mode: 'single' },
		filterable: true,
		pageable: true,
		resizable: true,
	};

	protected columnModel: ColumnHeaderData[];

	protected gridModel: GridModel;
	protected dateFormat = '';
	protected eventDetailsShown = false;

	@ViewChild(GridComponent, { static: false }) gridComponent: GridComponent;
	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private eventService: EventsService,
		private translateService: TranslatePipe,
		private store: Store,
		private route: ActivatedRoute
	) {
	}

	/**
	 * Initialize the grid settings.
	 */
	async ngOnInit() {
		this.gridRowActions = [{
			name: 'View',
			show: true,
			disabled: false,
			onClick: this.openView
		}];

		this.headerActions = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateEvent,
			}];

		this.gridModel = {
			columnModel: this.columnModel,
			gridRowActions: this.gridRowActions,
			gridSettings: this.gridSettings,
			headerActionButtons: this.headerActions,
			showDataReloadButton: false,
			loadData: this.loadData,
		};

		this.dateFormat = await this.preferenceService
			.getUserDatePreferenceAsKendoFormat()
			.toPromise();

		this.columnModel = new EventColumnModel(
			this.dateFormat
		).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openEvent(event.dataItem.id);
		}
	}

	public loadData = async (): Promise<EventModel[]> => {
		try {
			let data = await this.eventService.getEventsForList().toPromise();
			if (this.route.snapshot.queryParams['show']) {
				let { id } = data.find((bundle: any) => {
					return bundle.id === parseInt(this.route.snapshot.queryParams['show'], 0)
				});
				if (!this.eventDetailsShown) {
					setTimeout(() => {
						this.openEvent(id);
						this.eventDetailsShown = true;
					});
				}
			}
			return data;
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openView = async (dataItem: EventModel): Promise<void> => {
		try {
			await this.openEvent(dataItem.id);
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public onCreateEvent = async (): Promise<void> => {
		try {
			await this.dialogService.open(EventCreateComponent, []);
			await this.gridComponent.reloadData();
		} catch (error) {
			if ( error ) {
				console.error(error);
			}
		}
	};

	public async openEvent(id: number): Promise<void> {
		try {
			this.store.dispatch(new SetEvent({ id: id, name: name }));
			await this.dialogService.open(EventViewEditComponent, [
				{
					provide: 'id',
					useValue: id,
				}
			]);
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	}

	/**
	 * Determine if the user has the permission to edit events
	 */
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.EventEdit);
	}

	/**
	 * Determine if the user has the permission to create events
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.EventCreate);
	}
}
