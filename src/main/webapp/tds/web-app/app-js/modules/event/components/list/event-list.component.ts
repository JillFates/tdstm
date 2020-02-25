// Angular
import {
	AfterContentInit,
	Component, ComponentFactoryResolver, OnDestroy,
	OnInit,
	ViewChild
} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
// Store
import {Store} from '@ngxs/store';
// Model
import {EventColumnModel, EventModel} from '../../model/event.model';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
import {ActionType} from '../../../dataScript/model/data-script.model';
// Actions
import {SetEvent} from '../../action/event.actions';
// Services
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';
import {EventsService} from '../../service/events.service';
import {
	PreferenceService
} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Components
import {EventCreateComponent} from '../create/event-create.component';
import {EventViewEditComponent} from '../view-edit/event-view-edit.component';
// Other
import {
	CellClickEvent
} from '@progress/kendo-angular-grid';

@Component({
	selector: 'event-list',
	templateUrl: 'event-list.component.html',
})
export class EventListComponent implements OnInit, AfterContentInit, OnDestroy {
	public gridRowActions: GridRowAction[];

	public headerActions: HeaderActionButtonData[];

	public gridSettings: GridSettings = {
		defaultSort: [{field: 'title', dir: 'asc'}],
		sortSettings: {mode: 'single'},
		filterable: true,
		pageable: true,
		resizable: true,
	};

	protected columnModel: ColumnHeaderData[];

	protected gridModel: GridModel;
	protected dateFormat = '';

	private navigationSubscription;
	private eventToOpen: string;
	private eventOpen = false;

	@ViewChild(GridComponent, {static: false}) gridComponent: GridComponent;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private eventsService: EventsService,
		private eventService: EventsService,
		private translateService: TranslatePipe,
		private store: Store,
		private router: Router,
		private route: ActivatedRoute
	) {
	}

	/**
	 * Initialize the grid settings.
	 */
	async ngOnInit() {

		this.gridRowActions = [
			{
				name: 'Edit',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.onEdit,
			},
			{
				name: 'Delete',
				show: true,
				disabled: !this.isEditAvailable(),
				onClick: this.onDelete,
			},
		];

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

	ngAfterContentInit() {
		this.eventToOpen = this.route.snapshot.queryParams['show'];
		// The following code Listen to any change made on the route to reload the page
		this.navigationSubscription = this.router.events.subscribe((event: any) => {
			if (event && event.state && event.state && event.state.url.indexOf('/event/list') !== -1) {
				this.eventToOpen = event.state.root.queryParams.show;
			}
			if (event instanceof NavigationEnd && this.eventToOpen && this.eventToOpen.length && !this.eventOpen) {
				this.openEvent({id: parseInt(this.eventToOpen, 10)}, ActionType.VIEW);
			}
		});

		this.route.queryParams.subscribe(params => {
			if (this.eventToOpen && !this.eventOpen) {
				setTimeout(() => {
					this.openEvent({id: parseInt(this.eventToOpen, 10)}, ActionType.VIEW);
				});
			}
		});
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openEvent(event.dataItem, ActionType.EDIT, false);
		}
	}

	public loadData = async (): Promise<EventModel[]> => {
		try {
			let data = await this.eventService.getEventsForList().toPromise();
			return data;
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	/**
	 * On Delete Event
	 */
	public onDelete = async (dataItem: EventModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				const confirmation = await this.dialogService.confirm(
					'Confirmation Required',
					'WARNING: Are you sure you want to delete this event?'
				).toPromise();
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					this.eventsService.deleteEvent(dataItem.id).toPromise();
					await this.gridComponent.reloadData();
					setTimeout(() => {
						this.store.dispatch(new SetEvent(null));
					});
				}
			}
		} catch (error) {
			console.error(error);
		}
	}

	public onCreateEvent = async (): Promise<void> => {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: EventCreateComponent,
				data: {},
				modalConfiguration: {
					title: 'Event Create',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	private onEdit = async (dataItem: EventModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openEvent(dataItem, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}
	}

	public async openEvent(event: any, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			if (event.name) {
				this.store.dispatch(new SetEvent({id: event.id, name: event.name}));
			}
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: EventViewEditComponent,
				data: {
					eventId: event.id,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Event',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
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

	/**
	 * Ensure the listener is not available after moving away from this component
	 */
	ngOnDestroy(): void {
		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}
	}
}
