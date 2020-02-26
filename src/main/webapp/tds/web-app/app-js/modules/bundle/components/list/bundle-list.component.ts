// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Store
import {Store} from '@ngxs/store';
import {UserContextState} from '../../../auth/state/user-context.state';
// Actions
import {SetBundle} from '../../action/bundle.actions';
// Component
import {BundleViewEditComponent} from '../view-edit/bundle-view-edit.component';
// Model
import {ActionType} from '../../../dataScript/model/data-script.model';
import {BundleColumnModel, BundleModel} from '../../model/bundle.model';
import {
	ColumnHeaderData, DialogConfirmAction, DialogService,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData, ModalSize
} from 'tds-component-library';
import {Permission} from '../../../../shared/model/permission.model';
import {EventModel} from '../../../event/model/event.model';
import {ProviderModel} from '../../../provider/model/provider.model';
// Component
import {EventCreateComponent} from '../../../event/components/create/event-create.component';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {BundleService} from '../../service/bundle.service';
// Other
import {CellClickEvent} from '@progress/kendo-angular-grid';

@Component({
	selector: `bundle-list`,
	templateUrl: 'bundle-list.component.html',
})
export class BundleListComponent implements OnInit {
	public gridRowActions: GridRowAction[];

	public headerActions: HeaderActionButtonData[];

	public gridSettings: GridSettings = {
		defaultSort: [{field: 'title', dir: 'asc'}],
		sortSettings: {mode: 'single'},
		selectableSettings: {enabled: true, mode: 'single'},
		filterable: true,
		pageable: true,
		resizable: true,
	};

	protected columnModel: ColumnHeaderData[];

	protected gridModel: GridModel;
	protected dateFormat = '';

	private bundleToOpen: string;
	private bundleOpen = false;

	@ViewChild(GridComponent, {static: false}) gridComponent: GridComponent;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private bundleService: BundleService,
		private translateService: TranslatePipe,
		private store: Store,
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
				onClick: this.onCreateBundle,
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

		this.columnModel = new BundleColumnModel(
			this.dateFormat
		).columns;

		this.gridModel.columnModel = this.columnModel;
	}

	public async cellClick(event: CellClickEvent): Promise<void> {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			await this.openBundle(event.dataItem, ActionType.EDIT, false);
		}
	}

	public loadData = async (): Promise<BundleModel[]> => {
		try {
			return await this.bundleService.getBundles().toPromise();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public onCreateBundle = async (): Promise<void> => {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: EventCreateComponent,
				data: {},
				modalConfiguration: {
					title: 'Bundle Create',
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
	private onEdit = async (dataItem: ProviderModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				await this.openBundle(dataItem, ActionType.EDIT, true);
			}
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * On Delete Event
	 */
	public onDelete = async (dataItem: EventModel): Promise<void> => {
		try {
			if (this.isEditAvailable()) {
				const confirmation = await this.dialogService.confirm(
					'Confirmation Required',
					'WARNING: Deleting this bundle will remove any teams and any related step data'
				).toPromise();
				if (confirmation.confirm === DialogConfirmAction.CONFIRM) {
					this.bundleService.deleteBundle(dataItem.id).toPromise();
					await this.gridComponent.reloadData();
					setTimeout(() => {
						// If the Delete Item is the one selected, remove it from the Storage
						const bundle = this.store.selectSnapshot(UserContextState.getUserBundle);
						if (bundle.id === dataItem.id) {
							this.store.dispatch(new SetBundle(null));
						}
					});
				}
			}
		} catch (error) {
			console.error(error);
		}
	}

	public async openBundle(bundle: any, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			if (bundle.name) {
				this.store.dispatch(new SetBundle({id: bundle.id, name: bundle.name}));
			}
			this.bundleOpen = true;
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: BundleViewEditComponent,
				data: {
					eventId: bundle.id,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: 'Bundle',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).toPromise();
			this.bundleOpen = false;
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	}

	/**
	 * Determine if the user has the permission to edit bundles
	 */
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.BundleEdit);
	}

	/**
	 * Determine if the user has the permission to create bundles
	 */
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.BundleCreate);
	}
}
