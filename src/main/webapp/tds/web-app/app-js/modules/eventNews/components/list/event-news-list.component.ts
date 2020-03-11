// Angular
import {AfterViewInit, Component, ComponentFactoryResolver, ElementRef, OnInit, ViewChild} from '@angular/core';
// Services
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {EventNewsService} from '../../service/event-news.service';
import {EventsService} from '../../../event/service/events.service';
// Components
import {EventNewsViewEditComponent} from '../view-edit/event-news-view-edit.component';
// Models
import {CellClickEvent} from '@progress/kendo-angular-grid';
import {Permission} from '../../../../shared/model/permission.model';
import {EventNewsColumnModel, EventNewsModel} from '../../model/event-news.model';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {EventModel} from '../../../event/model/event.model';
import {
	ColumnHeaderData,
	DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData,
	ModalSize
} from 'tds-component-library';

@Component({
	selector: 'event-news-list',
	templateUrl: 'event-news-list.component.html'
})

export class EventNewsListComponent implements AfterViewInit, OnInit {
	private eventId: number;
	private bundleId: number;
	private viewFilter: string;

	@ViewChild('headerDropdowns', {static: false}) headerDropdowns: ElementRef;
	protected headerEventData: Promise<EventModel[]>;
	protected headerEventBundleData: Promise<any>;

	protected gridData: Promise<any>;
	public gridRowActions: GridRowAction[];
	public headerActions: HeaderActionButtonData[];

	public gridSettings: GridSettings = {
		defaultSort: [{ field: 'cratedAt', dir: 'asc' }],
		sortSettings: { mode: 'single' },
		filterable: true,
		selectableSettings: {enabled: true, mode: 'single'},
		pageable: true,
		resizable: true,
	};

	protected columnModel: ColumnHeaderData[];

	protected gridModel: GridModel;
	private dateFormat = '';

	@ViewChild(GridComponent, { static: false }) gridComponent: GridComponent;
	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private eventNewsService: EventNewsService,
		private eventService: EventsService,
		private translateService: TranslatePipe,
		private elementRef: ElementRef
	) {
	}

	/**
	 * Initialize the grid settings.
	 */
	async ngOnInit() {
		this.headerEventData = this.loadHeaderData();
		this.headerEventData.then(eventList => {
			this.eventId = eventList[0].id;
			this.gridComponent.reloadData();
			// Insert the dropdown controls on the grid toolbar
			this.elementRef.nativeElement
				.querySelector('kendo-grid-toolbar div div')
				.insertAdjacentElement('afterbegin', this.headerDropdowns.nativeElement);
		}).finally();

		this.gridRowActions = [{
			name: 'Edit',
			show: true,
			disabled: !this.isEditAvailable(),
			onClick: this.onEdit,
		}];

		this.headerActions = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateEventNews,
			},
		];

		this.gridModel = {
			columnModel: this.columnModel,
			gridRowActions: this.gridRowActions,
			gridSettings: this.gridSettings,
			headerActionButtons: this.headerActions,
			loadData: this.loadData,
		};

		this.dateFormat = await this.preferenceService.getUserDatePreferenceAsKendoFormat().toPromise();

		this.columnModel = new EventNewsColumnModel(this.dateFormat).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	async ngAfterViewInit() {
		this.headerEventBundleData = this.loadHeaderEventBundleData(this.eventId);
	}

	private onCreateEventNews = async (): Promise<void> => {
		try {
			let eventNewsModel: EventNewsModel = {
				moveEventId: this.eventId,
				commentType: 'news',
			};
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: EventNewsViewEditComponent,
				data: {
					eventNewsModel: eventNewsModel,
					actionType: ActionType.CREATE,
					openFromList: false
				},
				modalConfiguration: {
					title: 'Event News',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	};

	private isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderCreate);
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openEvent(event.dataItem, ActionType.VIEW);
		}
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	private onEdit = async (dataItem: EventNewsModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openEvent(dataItem, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}
	};

	public loadData = async (): Promise<EventNewsModel[]> => {
		try {
			let data = (this.eventId && this.eventId > 0)
				? await this.eventNewsService.getNewsFromEvent(this.eventId, this.bundleId, this.viewFilter).toPromise()
				: [];
			return (data.length > 0) ? data : [];
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public loadHeaderData = async(): Promise<EventModel[]> => {
		try {
			return await this.eventService.getEvents().toPromise();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public loadHeaderEventBundleData = async(eventId: number): Promise<EventModel[]> => {
		try {
			return await this.eventService.getListBundles(eventId).toPromise();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public openView = async (dataItem: EventNewsModel): Promise<void> => {
		try {
			await this.openEvent(dataItem, ActionType.VIEW);
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public async openEvent(eventNewsModel: EventNewsModel, actionType: ActionType,  openFromList = false): Promise<void> {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: EventNewsViewEditComponent,
				data: {
					eventNewsModel: eventNewsModel,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Event News',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			await this.gridComponent.reloadData();
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * Determine if the user has the permission to edit events
	 */
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.EventEdit);
	}

	public onEventListChange(event: any): void {
		this.headerEventBundleData = this.loadHeaderEventBundleData(event.target.value);
		this.eventId = event.target.value;
		this.gridComponent.reloadData().finally();
	}

	public onBundleListChange(event: any): void {
		this.bundleId = event.target.value;
		this.gridComponent.reloadData().finally();
	}

	public onChangeViewFilter(event: any): void {
		this.viewFilter = event.target.value;
		this.gridComponent.reloadData().finally();
	}
}