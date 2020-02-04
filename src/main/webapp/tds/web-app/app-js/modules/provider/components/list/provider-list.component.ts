// Angular
import {
	Component,
	ComponentFactoryResolver,
	ElementRef,
	HostListener,
	OnDestroy,
	OnInit,
	Renderer2,
} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Services
import {ProviderService} from '../../service/provider.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
// Components
import {ProviderViewEditComponent} from '../view-edit/provider-view-edit.component';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ProviderAssociatedComponent} from '../provider-associated/provider-associated.component';
// Models
import {ActionType, COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {ProviderColumnModel, ProviderModel} from '../../model/provider.model';
import {GRID_DEFAULT_PAGE_SIZE, GRID_DEFAULT_PAGINATION_OPTIONS} from '../../../../shared/model/constants';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {ProviderAssociatedModel} from '../../model/provider-associated.model';
import {Permission} from '../../../../shared/model/permission.model';
// Kendo
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {ReplaySubject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {DialogService, HeaderActionButtonData, ModalSize} from 'tds-component-library';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'provider-list',
	templateUrl: 'provider-list.component.html'
})
export class ProviderListComponent implements OnInit, OnDestroy {
	public headerActionButtons: HeaderActionButtonData[];

	protected gridColumns: any[];
	private selectableSettings: SelectableSettings = { mode: 'single', checkboxOnly: true};
	public dataGridOperationsHelper: DataGridOperationsHelper;
	private initialSort: any = [{
		dir: 'asc',
		field: 'name'
	}];
	private checkboxSelectionConfig = null;

	public pageSize = GRID_DEFAULT_PAGE_SIZE;
	public defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	public providerColumnModel = null;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public actionType = ActionType;
	public selectedRows = [];
	public dateFormat = '';
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	protected showFilters = false;
	public disabledClearFilters: any;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private oldDialogService: UIDialogService,
		private permissionService: PermissionService,
		private providerService: ProviderService,
		private prompt: UIPromptService,
		private route: ActivatedRoute,
		private elementRef: ElementRef,
		private renderer: Renderer2,
		private userContext: UserContextService,
		private translateService: TranslatePipe
	) {

		this.dataGridOperationsHelper = new DataGridOperationsHelper(
			this.route.snapshot.data['providers'],
			this.initialSort,
			this.selectableSettings,
			this.checkboxSelectionConfig,
			this.pageSize);
	}

	ngOnInit() {
		this.disabledClearFilters = this.onDisableClearFilter.bind(this);

		this.headerActionButtons = [
			{
				icon: 'plus',
				iconClass: 'is-solid',
				title: this.translateService.transform('GLOBAL.CREATE'),
				disabled: !this.isCreateAvailable(),
				show: true,
				onClick: this.onCreateProvider.bind(this),
			},
		];

		this.userContext
			.getUserContext()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = DateUtils.translateDateFormatToKendoFormat(
					userContext.dateFormat
				);
				this.providerColumnModel = new ProviderColumnModel(
					`{0:${this.dateFormat}}`
				);
				this.gridColumns = this.providerColumnModel.columns.filter(
					column => column.type !== 'action'
				);
			});
	}

	protected onCreateProvider(): void {
		let providerModel: ProviderModel = {
			name: '',
			description: '',
			comment: '',
		};
		this.openProviderDialogViewEdit(providerModel, ActionType.CREATE);
	}

	/**
	 * Select the current element and open the Edit Dialog
	 * @param dataItem
	 */
	protected onEdit(dataItem: any): void {
		this.openProviderDialogViewEdit(dataItem, ActionType.EDIT);
	}

	/**
	 * Delete the selected Provider
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.providerService
			.deleteContext(dataItem.id)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((result: any) => {
				this.oldDialogService
					.extra(
						ProviderAssociatedComponent,
						[
							{
								provide: ProviderAssociatedModel,
								useValue: result,
							},
						],
						false,
						false
					)
					.then((toDelete: any) => {
						if (toDelete) {
							this.providerService
								.deleteProvider(dataItem.id)
								.subscribe(
									result => {
										this.reloadData();
									},
									err => console.log(err)
								);
						}
					})
					.catch(error => console.log('Closed'));
			});
	}

	/**
	 * Catch the Selected Row
	 * @param {any} dataItem
	 */
	protected cellClick(dataItem: any): void {
		this.selectRow(dataItem.id);
		this.openProviderDialogViewEdit(dataItem, ActionType.VIEW);
	}

	protected reloadData(): void {
		this.providerService
			.getProviders()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				result => {
					this.dataGridOperationsHelper.reloadData(result);
					setTimeout(
						() => this.forceDisplayLastRowAddedToGrid(),
						100
					);
				},
				err => console.log(err)
			);
	}

	/**
	 * This work as a temporary fix.
	 * TODO: talk when Jorge Morayta get's back to do a proper/better fix.
	 */
	private forceDisplayLastRowAddedToGrid(): void {
		const lastIndex = this.dataGridOperationsHelper.gridData.data.length - 1;
		let target = this.elementRef.nativeElement.querySelector(
			`tr[data-kendo-grid-item-index="${lastIndex}"]`
		);
		this.renderer.setStyle(target, 'height', '36px');
	}

	/**
	 * Open The Dialog to Create, View or Edit the Provider
	 * @param {ProviderModel} providerModel
	 * @param {number} actionType
	 */
	private openProviderDialogViewEdit(providerModel: ProviderModel, actionType: number): void {
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: ProviderViewEditComponent,
			data: {
				providerModel: providerModel,
				actionType: actionType
			},
			modalConfiguration: {
				title: 'Provider Detail',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe((result: any) => {
			this.reloadData();
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

	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		this.unsubscribeOnDestroy$.next();
		this.unsubscribeOnDestroy$.complete();
	}

	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderCreate);
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderDelete);
	}

	protected isUpdateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderUpdate);
	}

	protected toggleFilter(): void {
		this.showFilters = !this.showFilters;
	}

	/**
	 * Returns the number of current selected filters
	 */
	protected filterCount(): number {
		return this.dataGridOperationsHelper.getFilterCounter();
	}

	/**
	 * Clear all filters
	 */
	protected clearAllFilters(): void {
		this.showFilters = false;
		this.dataGridOperationsHelper.clearAllFilters(this.gridColumns);
	}

	/**
	 * Disable clear filters
	 */
	protected onDisableClearFilter(): boolean {
		return this.filterCount() === 0;
	}

}
