// Angular
import {Component, ComponentFactoryResolver} from '@angular/core';
// Model
import {DIALOG_SIZE, DOMAIN} from '../../../../shared/model/constants';
import {DeviceModel} from './model-device/model/device-model.model';
import {DeviceManufacturer} from './manufacturer/model/device-manufacturer.model';
import {Permission} from '../../../../shared/model/permission.model';
// Service
import {DependecyService} from '../../service/dependecy.service';
import {ModelService} from '../../service/model.service';
import {ManufacturerService} from '../../service/manufacturer.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {WindowService} from '../../../../shared/services/window.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {DialogService} from 'tds-component-library';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';
// Component
import {AssetEditComponent} from '../asset/asset-edit.component';
import {ModelDeviceShowComponent} from './model-device/components/model-device-show/model-device-show.component';
import {ManufacturerShowComponent} from './manufacturer/components/manufacturer-show/manufacturer-show.component';
import {AssetCommonShow} from '../asset/asset-common-show';
// Other
import { forkJoin } from 'rxjs';

export function DeviceShowComponent(template, modelId: number, metadata: any, parentDialog: any) {
	@Component({
		selector: `tds-device-show`,
		template: template
	})
	class DeviceShowComponent extends AssetCommonShow {
		protected manufacturerName: string;
		protected modelName: string;

		constructor(
			componentFactoryResolver: ComponentFactoryResolver,
			dialogService: DialogService,
			assetService: DependecyService,
			private modelService: ModelService,
			private manufacturerService: ManufacturerService,
			assetExplorerService: AssetExplorerService,
			notifierService: NotifierService,
			userContextService: UserContextService,
			private permissionService: PermissionService,
			windowService: WindowService,
			architectureGraphService: ArchitectureGraphService
		) {
			super(
				componentFactoryResolver,
				dialogService,
				assetService,
				assetExplorerService,
				notifierService,
				userContextService,
				windowService,
				architectureGraphService,
				parentDialog,
				metadata
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

		// /**
		//  * Open up the model show view
		//  * @param {string} modelId
		//  * @param {string} manufacturerId
		//  */
		// showModel(modelId: string, manufacturerId: string): void {
		// 	if (this.modelName === null) {
		// 		return;
		// 	}
		// 	forkJoin(
		// 		this.modelService.getModelAsJSON(modelId),
		// 		this.manufacturerService.getDeviceManufacturer(manufacturerId)
		// 	).subscribe((results: any) => {
		// 		const [deviceModel, deviceManufacturer] = results;
		// 		this.dialogService.extra(ModelDeviceShowComponent,
		// 			[UIDialogService,
		// 				{
		// 					provide: DeviceModel,
		// 					useValue: deviceModel
		// 				},
		// 				{
		// 					provide: DeviceManufacturer,
		// 					useValue: deviceManufacturer
		// 				}
		// 			], false, false)
		// 		.then((result) => {
		// 			if (result === null) {
		// 				this.modelName = null;
		// 			} else {
		// 				this.modelName = result.modelName;
		// 			}
		// 		}).catch((error) => console.log(error));
		// 	});
		// }
		//
		// showManufacturer(id: string): void {
		// 	this.manufacturerService.getDeviceManufacturer(id)
		// 		.subscribe((deviceManufacturer: DeviceManufacturer) => {
		// 			this.dialogService.extra(ManufacturerShowComponent,
		// 				[UIDialogService,
		// 					{
		// 						provide: DeviceManufacturer,
		// 						useValue: deviceManufacturer
		// 					}
		// 				], false, false)
		// 				.then((result) => {
		// 					if (result) {
		// 						this.manufacturerName = result.name;
		// 					}
		// 				}).catch((error) => console.log(error));
		// 		});
		// }
		//
		// showAssetEditView() {
		// 	this.dialogService.replace(AssetEditComponent, [
		// 			{ provide: 'ID', useValue: this.mainAsset },
		// 			{ provide: 'ASSET', useValue: DOMAIN.DEVICE }],
		// 		DIALOG_SIZE.XXL);
		// }

		protected isManufacturerLinkAvailable(): boolean {
			return this.permissionService.hasPermission(Permission.ManufacturerView);
		}

		protected isModelLinkAvailable(): boolean {
			return this.permissionService.hasPermission(Permission.ModelView);
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
