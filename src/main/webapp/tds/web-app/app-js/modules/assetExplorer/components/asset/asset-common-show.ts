import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependecyService} from '../../service/dependecy.service';
import {HostListener, OnInit} from '@angular/core';
import {DIALOG_SIZE, DOMAIN, KEYSTROKE} from '../../../../shared/model/constants';
import {TagModel} from '../../../assetTags/model/tag.model';
import {AssetShowComponent} from './asset-show.component';
import {AssetDependencyComponent} from '../asset-dependency/asset-dependency.component';
import {AssetCommonHelper} from './asset-common-helper';
import {WindowService} from '../../../../shared/services/window.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {AssetEditComponent} from './asset-edit.component';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {
	SupportDependentsOnViewColumnsModel,
	SupportOnColumnsModel
} from '../../../../shared/components/supports-depends/model/support-on-columns.model';

declare var jQuery: any;

export class AssetCommonShow implements OnInit {

	protected userDateFormat: string;
	protected userTimeZone: string;
	protected mainAsset;
	protected currentShowAsset = {asset: {moveBundleId: 0}};
	protected assetType: DOMAIN;
	protected assetTags: Array<TagModel>;
	protected isHighField = AssetCommonHelper.isHighField;
	public ignoreDoubleClickClasses =
		['btn', 'clickableText', 'table-responsive', 'task-comment-component', 'view-dependencies'];

	public gridSupportsData: DataGridOperationsHelper;
	public gridDependenciesData: DataGridOperationsHelper;
	public showFilterDep = false;
	public showFilterSup = false;

	private supportOnColumnModel: SupportOnColumnsModel;
	private dependentOnColumnModel: SupportOnColumnsModel;

	constructor(
		protected activeDialog: UIActiveDialogService,
		protected dialogService: UIDialogService,
		protected assetService: DependecyService,
		protected prompt: UIPromptService,
		protected assetExplorerService: AssetExplorerService,
		protected notifierService: NotifierService,
		protected userContextService: UserContextService,
		protected windowService: WindowService,
		public metadata: any) {
			jQuery('[data-toggle="popover"]').popover();
			this.userContextService.getUserContext()
				.subscribe((userContext: UserContextModel) => {
					this.userDateFormat = userContext.dateFormat;
					this.userTimeZone = userContext.timezone;
				});

			this.gridSupportsData = new DataGridOperationsHelper(this.metadata.supports, [{
				dir: 'asc',
				field: 'name'
			}], {mode: 'single', checkboxOnly: false}, {useColumn: 'id'}, 25);

			this.gridDependenciesData = new DataGridOperationsHelper(this.metadata.dependents, [{
				dir: 'asc',
				field: 'name'
			}], {mode: 'single', checkboxOnly: false}, {useColumn: 'id'}, 25);

			this.supportOnColumnModel = new SupportDependentsOnViewColumnsModel();
			this.dependentOnColumnModel = new SupportDependentsOnViewColumnsModel();

			this.currentShowAsset = metadata.asset;
	}

	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Show the asset edit view
	 */
	showAssetEditView(): Promise<any> {
		const componentParameters = [
			{ provide: 'ID', useValue: this.mainAsset },
			{ provide: 'ASSET', useValue: this.assetType}
		];

		return this.dialogService
			.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.LG);
	}

	public showFilterSupports(): void {
		if (this.showFilterSup) {
			this.showFilterSup = false;
			this.gridSupportsData.clearAllFilters(this.supportOnColumnModel.columns);
		} else {
			this.showFilterSup = true;
		}
	}

	showFilterDependents(): void {
		if (this.showFilterDep) {
			this.showFilterDep = false;
			this.gridDependenciesData.clearAllFilters(this.dependentOnColumnModel.columns);
		} else {
			this.showFilterDep = true;
		}
	}

	/**
	 * Initiates The Injected Component
	 */
	ngOnInit(): void {
		jQuery('[data-toggle="popover"]').popover();
	}

	/**
	 * Handle dobule click events over the view
	 * @return The corresponding asset edit view based on the main asset and asset type
	 */
	onDoubleClick(): Promise<any> {
		return this.showAssetEditView();
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	showAssetDetailView(assetClass: string, id: number) {
		this.dialogService.replace(AssetShowComponent, [
				{ provide: 'ID', useValue: id },
				{ provide: 'ASSET', useValue: assetClass }],
			DIALOG_SIZE.LG);
	}

	showDependencyView(assetId: number, dependencyAsset: number) {
		this.assetService.getDependencies(assetId, dependencyAsset)
			.subscribe((result) => {
				this.dialogService.extra(AssetDependencyComponent, [
					{ provide: 'ASSET_DEP_MODEL', useValue: result }])
					.then(res => console.log(res))
					.catch(res => console.log(res));
			}, (error) => console.log(error));
	}

	/**
	 allows to delete the application assets
	 */
	onDeleteAsset() {

		this.prompt.open('Confirmation Required',
			'You are about to delete the selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel',
			'OK', 'Cancel')
			.then( success => {
				if (success) {
					this.assetExplorerService.deleteAssets([this.mainAsset.toString()]).subscribe( res => {
						if (res) {
							this.notifierService.broadcast({
								name: 'reloadCurrentAssetList'
							});
							this.activeDialog.dismiss();
						}
					}, (error) => console.log(error));
				}
			})
			.catch((error) => console.log(error));
	}

	getGraphUrl(): string {
		return `/tdstm/assetEntity/architectureViewer?assetId=${this.mainAsset}&level=2`;
	}

	openGraphUrl() {
		this.windowService.getWindow().open(this.getGraphUrl(), '_blank');
	}

	/**
	 * Calculate the class for the Move Bundle
	 * @returns {string}
	 */
	public getMoveBundleClass(dataItem: any, currentShowAsset: any): string {
		if (dataItem.moveBundle.id !== currentShowAsset && currentShowAsset.moveBundleId && dataItem.status === 'Validated') {
			return 'bundle-dep-no-valid';
		} else {
			return 'cell-template dep-' + dataItem.status;
		}
	}
}
