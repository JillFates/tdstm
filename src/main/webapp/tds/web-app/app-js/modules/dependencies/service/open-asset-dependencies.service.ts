// Angular
import {ComponentFactoryResolver, Injectable} from '@angular/core';
// Model
import {DependecyService} from '../../assetExplorer/service/dependecy.service';
import {DialogService, ModalSize} from 'tds-component-library';
// Component
import {AssetShowComponent} from '../../assetExplorer/components/asset/asset-show.component';
import {AssetDependencyComponent} from '../../assetExplorer/components/asset-dependency/asset-dependency.component';
// Other
import {Observable} from 'rxjs';
import {
	switchMap
} from 'rxjs/operators';

export interface AssetDependency {
	id: number;
	class: string;
	dependentId: number;
	dependentClass: string
}

@Injectable()
export class OpenAssetDependenciesService {
	constructor(
		private dialogService: DialogService,
		private componentFactoryResolver: ComponentFactoryResolver,
		private dependencyService: DependecyService) {
	}

	/**
	 * Get the handler function to handle the opening of asset or asset dependencies
	 * @param allowedAssets List of assets allowed to be opened
	 */
	getOpenAssetsHandler(allowedAssets: string[]) {
		return (targetAsset: string, assetDependency: AssetDependency) => {
			const asset = this.getAssetForOpen(allowedAssets, targetAsset, assetDependency);
			return asset.getParams
				.pipe((switchMap((results: any) =>
					Observable.from(
						this.dialogService.open({
							componentFactoryResolver: this.componentFactoryResolver,
							component: asset.component,
							data: results.data,
							modalConfiguration: asset.modalConfiguration
						})
					)
				)))
		}
	}

	/**
	 * Based upon the target asset, get the object that represents an asset along with the parameters
	 * required to open it in a view
	 * @param allowedAssets List of assets allowed to be opened
	 * @param targetAsset name of the target asset to open up
	 * @param assetDependency object containing the asset and dependent asset info
	 */
	private getAssetForOpen(allowedAssets: string[], targetAsset: string, assetDependency: AssetDependency): any {
		const [assetName, dependentName, type] = allowedAssets;

		const assetParameters = {
			[assetName]: {
				component: AssetShowComponent,
				getParams: Observable.of({
					data: {
						assetId: assetDependency.id,
						assetClass: assetDependency.class
					}
				}),
				modalConfiguration: {
					title: 'Asset',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-asset-modal-dialog'
				}
			},
			[dependentName]: {
				component: AssetShowComponent,
				getParams: Observable.of({
					data: {
						assetId: assetDependency.dependentId,
						assetClass: assetDependency.dependentClass
					}
				}),
				modalConfiguration: {
					title: 'Asset',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-asset-modal-dialog'
				}
			},
			[type]: {
				component: AssetDependencyComponent,
				getParams: this.dependencyService.getDependencies(assetDependency.id, assetDependency.dependentId)
					.pipe(
						switchMap((result: any) =>
							Observable.of({
								data: {
									assetDependency: result
								}
							})
						)
					),
				modalConfiguration: {
					title: 'Dependency Detail',
					draggable: true,
					modalSize: ModalSize.MD,
				}
			}
		};

		return assetParameters[targetAsset] || null;
	}
}
