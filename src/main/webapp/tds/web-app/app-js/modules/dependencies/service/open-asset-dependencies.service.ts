import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

import {
	switchMap
} from 'rxjs/operators';
import {AssetShowComponent} from '../../assetExplorer/components/asset/asset-show.component';
import {DIALOG_SIZE} from '../../../shared/model/constants';
import {AssetDependencyComponent} from '../../assetExplorer/components/asset-dependency/asset-dependency.component';
import {UIDialogService} from '../../../shared/services/ui-dialog.service';
import {DependecyService} from '../../assetExplorer/service/dependecy.service';

export interface AssetDependency {
	id: number;
	class: string;
	dependentId: number;
	dependentClass: string
}

@Injectable()
export class OpenAssetDependenciesService {
	constructor(
		private dialog: UIDialogService,
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
				.pipe((switchMap((results) => Observable.from(asset.open(asset.component, results, ...asset.extraParams)))))
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
		const open = this.dialog.open.bind(this.dialog);
		const extra = this.dialog.extra.bind(this.dialog);

		const assetParameters = {
			[assetName]: {
				component: AssetShowComponent,
				getParams: Observable.of([
					{ provide: 'ID', useValue: assetDependency.id },
					{ provide: 'ASSET', useValue: assetDependency.class }
				]),
				extraParams: [DIALOG_SIZE.LG],
				open
			},
			[dependentName]: {
				component: AssetShowComponent,
				getParams: Observable.of([
					{ provide: 'ID', useValue: assetDependency.dependentId },
					{ provide: 'ASSET', useValue: assetDependency.dependentClass }
				]),
				extraParams: [DIALOG_SIZE.LG],
				open
			},
			[type]: {
				component: AssetDependencyComponent,
				getParams: this.dependencyService
					.getDependencies(assetDependency.id, assetDependency.dependentId)
					.pipe(
						switchMap((result: any) => Observable.of([{provide: 'ASSET_DEP_MODEL', useValue: result}]))
					),
				extraParams: [true],
				open: extra
			}
		};

		return assetParameters[targetAsset] || null;
	}
}