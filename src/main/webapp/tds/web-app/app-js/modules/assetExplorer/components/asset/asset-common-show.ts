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
import {process, State} from '@progress/kendo-data-query';
import {DataStateChangeEvent, GridDataResult} from '@progress/kendo-angular-grid';

declare var jQuery: any;

export class AssetCommonShow implements OnInit {

	protected userDateFormat: string;
	protected userTimeZone: string;
	protected mainAsset;
	protected assetType: DOMAIN;
	protected assetTags: Array<TagModel>;
	protected isHighField = AssetCommonHelper.isHighField;
	public ignoreDoubleClickClasses =
		['btn', 'clickableText', 'table-responsive', 'task-comment-component'];

	public supports: State = {
		skip: 0,
		take: 5,

		// Initial filter descriptor
		filter: {
			logic: 'and',
			filters: [{ field: 'ProductName', operator: 'contains', value: 'Chef' }]
		}
	};

	public gridSupportsData: GridDataResult = process([], this.supports);

	constructor(
		protected activeDialog: UIActiveDialogService,
		protected dialogService: UIDialogService,
		protected assetService: DependecyService,
		protected prompt: UIPromptService,
		protected assetExplorerService: AssetExplorerService,
		protected notifierService: NotifierService,
		protected userContextService: UserContextService,
		protected windowService: WindowService) {
			jQuery('[data-toggle="popover"]').popover();
			this.userContextService.getUserContext()
				.subscribe((userContext: UserContextModel) => {
					this.userDateFormat = userContext.dateFormat;
					this.userTimeZone = userContext.timezone;
				});
	}

	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	public dataStateChange(state: DataStateChangeEvent): void {
		this.supports = state;
		this.gridSupportsData = process([], this.supports);
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
}
