import {Component, HostListener, OnInit} from '@angular/core';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { AssetShowComponent } from '../asset/asset-show.component';
import { AssetDependencyComponent } from '../asset-dependency/asset-dependency.component';
import { DependecyService } from '../../service/dependecy.service';
import {DIALOG_SIZE, DOMAIN, KEYSTROKE} from '../../../../shared/model/constants';
import {AssetEditComponent} from '../asset/asset-edit.component';
import {TagService} from '../../../assetTags/service/tag.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {TagModel} from '../../../assetTags/model/tag.model';
import { DeviceModel } from './model-device/model/device-model.model';
import { DeviceManufacturer } from './manufacturer/model/device-manufacturer.model';
import { ModelDeviceShowComponent } from './model-device/components/model-device-show/model-device-show.component';
import { ManufacturerShowComponent } from './manufacturer/components/manufacturer-show/manufacturer-show.component';
import {ModelService} from '../../service/model.service';
import {ManufacturerService} from '../../service/manufacturer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';

declare var jQuery: any;

export function DeviceShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `device-show`,
		template: template
	}) class DeviceShowComponent implements OnInit {
		mainAsset = modelId;
		protected assetTags: Array<TagModel> = metadata.assetTags;
		public manufacturerName: string;

		constructor(
			private activeDialog: UIActiveDialogService,
			private dialogService: UIDialogService,
			private assetService: DependecyService,
			private modelService: ModelService,
			private manufacturerService: ManufacturerService,
			private prompt: UIPromptService,
			private assetsExplorerService: AssetExplorerService,
			private notifierService: NotifierService) {
			this.manufacturerName = null;
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

		getManufacturer(manufacturerName): string {
			if (this.manufacturerName === null) {
				this.manufacturerName = manufacturerName;
			}
			return this.manufacturerName;
		}

		showAssetDetailView(assetClass: string, id: number) {
			this.dialogService.replace(AssetShowComponent, [
				{ provide: 'ID', useValue: id },
				{ provide: 'ASSET', useValue: assetClass }],
				DIALOG_SIZE.XLG);
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

		showAssetEditView() {
			this.dialogService.replace(AssetEditComponent, [
					{ provide: 'ID', useValue: this.mainAsset },
					{ provide: 'ASSET', useValue: DOMAIN.DEVICE }],
				DIALOG_SIZE.XLG);
		}

		showModel(id: string): void {
			this.modelService.getModelAsJSON(id)
				.subscribe((deviceModel: DeviceModel) => {
					this.dialogService.extra(ModelDeviceShowComponent,
						[UIDialogService,
							{
								provide: DeviceModel,
								useValue: deviceModel
							}
						], false, false)
						.then((result) => {
							console.log(result);
						}).catch((error) => console.log(error));
				});
		}

		showManufacturer(id: string): void {
			this.manufacturerService.getDeviceManufacturer(id)
				.subscribe((deviceManufacturer: DeviceManufacturer) => {

					this.dialogService.extra(ManufacturerShowComponent,
						[UIDialogService,
							{
								provide: DeviceManufacturer,
								useValue: deviceManufacturer
							}
						], false, false)
						.then((result) => {
							if (result) {
								this.manufacturerName = result.name;
							}
						}).catch((error) => console.log(error));
				});
		}

		/**
		 allows to delete the application assets
		 */
		onDeleteAsset() {

			this.prompt.open('Confirmation Required',
				'You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel',
				'Yes', 'Cancel')
				.then( success => {
					if (success) {
						this.assetsExplorerService.deleteAssets([this.mainAsset.toString()]).subscribe( res => {
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

		/**
		 * Allows to clone an device asset
		 */
		onCloneAsset(): void {
			console.log('Will come clone implementation');
		}

		getGraphUrl(): string {
			return `/tdstm/assetEntity/architectureViewer?assetId=${this.mainAsset}&level=2`;
		}

	}
	return DeviceShowComponent;
}