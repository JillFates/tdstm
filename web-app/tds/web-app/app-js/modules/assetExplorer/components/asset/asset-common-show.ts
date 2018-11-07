import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependecyService} from '../../service/dependecy.service';
import {HostListener, OnInit} from '@angular/core';
import {DIALOG_SIZE, DOMAIN, KEYSTROKE} from '../../../../shared/model/constants';
import {TagModel} from '../../../assetTags/model/tag.model';
import {AssetEditComponent} from './asset-edit.component';
import {AssetShowComponent} from './asset-show.component';
import {AssetDependencyComponent} from '../asset-dependency/asset-dependency.component';
import {PreferenceService} from '../../../../shared/services/preference.service';

declare var jQuery: any;

export class AssetCommonShow implements OnInit {

	protected userDateFormat: string;
	protected userTimeZone: string;
	protected mainAsset;
	protected assetTags: Array<TagModel>;

	constructor(
		protected activeDialog: UIActiveDialogService,
		protected dialogService: UIDialogService,
		protected assetService: DependecyService,
		protected prompt: UIPromptService,
		protected assetExplorerService: AssetExplorerService,
		protected notifierService: NotifierService,
		protected preferenceService: PreferenceService) {
			jQuery('[data-toggle="popover"]').popover();
			this.userDateFormat = this.preferenceService.getUserDateFormatForMomentJS();
			this.userTimeZone = this.preferenceService.getUserTimeZone();
	}

	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Initiates The Injected Component
	 */
	ngOnInit(): void {
		jQuery('[data-toggle="popover"]').popover();
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
}