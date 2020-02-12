import {AfterContentInit, Component, ElementRef, OnInit, Renderer2, ViewChild} from '@angular/core';
import {CompositeFilterDescriptor, process, State} from '@progress/kendo-data-query';
// Store
import {Store} from '@ngxs/store';
// Actions
import {SetBundle} from '../../action/bundle.actions';
import {GRID_DEFAULT_PAGE_SIZE, GRID_DEFAULT_PAGINATION_OPTIONS} from '../../../../shared/model/constants';
import {ActionType, COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {CellClickEvent, GridDataResult} from '@progress/kendo-angular-grid';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute} from '@angular/router';
import {BundleService} from '../../service/bundle.service';
import {BundleColumnModel, BundleModel} from '../../model/bundle.model';
import {BooleanFilterData, DefaultBooleanFilterData} from '../../../../shared/model/data-list-grid.model';
import {BundleCreateComponent} from '../create/bundle-create.component';
import {BundleViewEditComponent} from '../view-edit/bundle-view-edit.component';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';
import {
	ColumnHeaderData,
	GridComponent,
	GridModel,
	GridRowAction,
	GridSettings,
	HeaderActionButtonData
} from 'tds-component-library';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { Permission } from '../../../../shared/model/permission.model';
import {EventsService} from '../../../event/service/events.service';
import {EventColumnModel, EventModel} from '../../../event/model/event.model';
import {EventCreateComponent} from '../../../event/components/create/event-create.component';
import {SetEvent} from '../../../event/action/event.actions';
import {EventViewEditComponent} from '../../../event/components/view-edit/event-view-edit.component';

declare var jQuery: any;

@Component({
	selector: `bundle-list`,
	templateUrl: 'bundle-list.component.html',
})
export class BundleListComponent implements OnInit {
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
	protected bundleDetailsShown = false;

	@ViewChild(GridComponent, { static: false }) gridComponent: GridComponent;
	constructor(
		private dialogService: UIDialogService,
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
			await this.openBundle(event.dataItem.id);
		}
	}

	public loadData = async (): Promise<BundleModel[]> => {
		try {
			let data = await this.bundleService.getBundles().toPromise();
			if (this.route.snapshot.queryParams['show']) {
				let { id } = data.find((bundle: any) => {
					return bundle.id === parseInt(this.route.snapshot.queryParams['show'], 0)
				});
				if (!this.bundleDetailsShown) {
					setTimeout(() => {
						this.openBundle(id);
						this.bundleDetailsShown = true;
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
			await this.openBundle(dataItem.id);
			await this.gridComponent.reloadData();
		} catch (error) {
			if (error) {
				console.error(error);
			}
		}
	};

	public onCreateBundle = async (): Promise<void> => {
		try {
			await this.dialogService.open(EventCreateComponent, []);
			await this.gridComponent.reloadData();
		} catch (error) {
			if ( error ) {
				console.error(error);
			}
		}
	};

	public async openBundle(id: number): Promise<void> {
		try {
			this.store.dispatch(new SetBundle({ id: id, name: name }));
			await this.dialogService.open(BundleViewEditComponent, [
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
