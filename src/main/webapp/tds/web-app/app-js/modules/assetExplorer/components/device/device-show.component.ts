import {Component} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {DependecyService} from '../../service/dependecy.service';
import {DIALOG_SIZE, DOMAIN} from '../../../../shared/model/constants';
import {AssetEditComponent} from '../asset/asset-edit.component';
import {DeviceModel} from './model-device/model/device-model.model';
import {DeviceManufacturer} from './manufacturer/model/device-manufacturer.model';
import {ModelDeviceShowComponent} from './model-device/components/model-device-show/model-device-show.component';
import {ManufacturerShowComponent} from './manufacturer/components/manufacturer-show/manufacturer-show.component';
import {ModelService} from '../../service/model.service';
import {ManufacturerService} from '../../service/manufacturer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetModalModel} from '../../model/asset-modal.model';
import {AssetCloneComponent} from '../asset-clone/asset-clone.component';
import {CloneCLoseModel} from '../../model/clone-close.model';
import {AssetCommonShow} from '../asset/asset-common-show';
import {WindowService} from '../../../../shared/services/window.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import { forkJoin } from 'rxjs';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';

export function DeviceShowComponent(template, modelId: number, metadata: any) {
	@Component({
		selector: `tds-device-show`,
		template: template
	})
	class DeviceShowComponent extends AssetCommonShow {
		protected manufacturerName: string;
		protected modelName: string;

		constructor(
			activeDialog: UIActiveDialogService,
			dialogService: UIDialogService,
			assetService: DependecyService,
			private modelService: ModelService,
			private manufacturerService: ManufacturerService,
			prompt: UIPromptService,
			assetExplorerService: AssetExplorerService,
			notifierService: NotifierService,
			userContextService: UserContextService,
			private permissionService: PermissionService,
			windowService: WindowService,
			architectureGraphService: ArchitectureGraphService
		) {
			super(
				activeDialog,
				dialogService,
				assetService,
				prompt,
				assetExplorerService,
				notifierService,
				userContextService,
				windowService,
				architectureGraphService
			);
			this.mainAsset = modelId;
			this.assetTags = metadata.assetTags;
			this.loadThumbnailData(this.mainAsset);
		}

		getManufacturer(manufacturerName): string {
			if (this.manufacturerName === null) {
				this.manufacturerName = manufacturerName;
			}
			return this.manufacturerName;
		}

		/**
		 * Open up the model show view
		 * @param {string} modelId
		 * @param {string} manufacturerId
		 */
		showModel(modelId: string, manufacturerId: string): void {
			if (this.modelName === null) {
				return;
			}
			forkJoin(
				this.modelService.getModelAsJSON(modelId),
				this.manufacturerService.getDeviceManufacturer(manufacturerId)
			).subscribe((results: any) => {
				const [deviceModel, deviceManufacturer] = results;
				this.dialogService.extra(ModelDeviceShowComponent,
					[UIDialogService,
						{
							provide: DeviceModel,
							useValue: deviceModel
						},
						{
							provide: DeviceManufacturer,
							useValue: deviceManufacturer
						}
					], false, false)
				.then((result) => {
					if (result === null) {
						this.modelName = null;
					} else {
						this.modelName = result.modelName;
					}
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

		showAssetEditView() {
			this.dialogService.replace(AssetEditComponent, [
					{ provide: 'ID', useValue: this.mainAsset },
					{ provide: 'ASSET', useValue: DOMAIN.DEVICE }],
				DIALOG_SIZE.XXL);
		}

		protected isManufacturerLinkAvailable(): boolean {
			return this.permissionService.hasPermission(Permission.ManufacturerView);
		}

		protected isModelLinkAvailable(): boolean {
			return this.permissionService.hasPermission(Permission.ModelView);
		}

		/**
		 * Allows to clone an application asset
		 */
		onCloneAsset(): void {

			const cloneModalModel: AssetModalModel = {
				assetType: DOMAIN.DEVICE,
				assetId: this.mainAsset
			}
			this.dialogService.extra(AssetCloneComponent, [
				{provide: AssetModalModel, useValue: cloneModalModel}
			], false, false).then( (result: CloneCLoseModel)  => {

				if (result.clonedAsset && result.showEditView) {
					const componentParameters = [
						{ provide: 'ID', useValue: result.assetId },
						{ provide: 'ASSET', useValue: DOMAIN.DEVICE }
					];

					this.dialogService
						.replace(AssetEditComponent, componentParameters, DIALOG_SIZE.XLG);
				} else if (!result.clonedAsset && result.showView) {
					this.showAssetDetailView(DOMAIN.DEVICE, result.assetId);
				}
			})
				.catch( error => console.log('error', error));
		}

		/**
		 * Return the current model name or the default one
		 * @param {string} defaultName
		 * @returns {string}
		 */
		getModelName(defaultName: string): string {
			if (this.modelName === null) {
				return '';
			}

			return this.modelName || defaultName;
		}
	}
	return DeviceShowComponent;
}
