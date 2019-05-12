import {ReportsService} from '../service/reports.service';
import {SafeHtml} from '@angular/platform-browser';
import {UIDialogService} from '../../../shared/services/ui-dialog.service';
import {AssetShowComponent} from '../../assetExplorer/components/asset/asset-show.component';
import {AssetExplorerModule} from '../../assetExplorer/asset-explorer.module';
import {DIALOG_SIZE} from '../../../shared/model/constants';

export abstract class ReportComponent {
	// Shared variables
	hideFilters = false;
	reportResult: SafeHtml;
	loadingLists = false;

	constructor(protected reportsService: ReportsService, protected dialogService: UIDialogService) {
		// Silence is golden.
	}

	/**
	 * Activate/Inactivate the show/hide filter flag.
	 */
	toggleFilters(): void {
		this.hideFilters = !this.hideFilters;
	}

	/**
	 * Asset name link handler
	 * @param event: any
	 */
	protected onAssetLinkClick(event: any): void {
		if (event.target) {
			const {assetClass, assetId} = event.target.dataset;
			if (assetClass && assetId) {
				this.onOpenLinkAsset(assetId, assetClass);
			}
		}
	}

	/**
	 * Show the asset
	 * @param assetId: number
	 * @param assetClass: string
	 */
	private onOpenLinkAsset(assetId: number, assetClass: string) {
		this.dialogService.open(AssetShowComponent,
			[UIDialogService,
				{ provide: 'ID', useValue: assetId },
				{ provide: 'ASSET', useValue: assetClass },
				{ provide: 'AssetExplorerModule', useValue: AssetExplorerModule }
			], DIALOG_SIZE.LG).then(result => {
			// Do nothing
		}).catch(result => {
			// Do nothing
		});
	}

	abstract onGenerateReport();

	mapConflictsResults(data: any, entity: string,  bundleList: any[]): any {
		return data == null ? [] : data[`${entity}List`]
		.map((item: any) => {
			let bundleName = data.moveBundle.name;
			if (!bundleName) {
				let currentBundle = bundleList.find((current) => current.id === item[entity].moveBundle.id.toString());
				bundleName = (currentBundle) ? currentBundle.name : '';
			}

			return {
				entity: {
					id: item[entity].id,
					name: item[entity].assetName,
					assetClass: item[entity].assetClass.name
				},
				header: item.header ? ` - ${item.header}` : '',
				bundle: {
					id: data.moveBundle.id,
					name: bundleName
				},
				supports: item.supportsList
					.map((support: any) => {
						return {
							type: support.type,
							class: support.asset.assetClass,
							name: support.asset.name ,
							frequency: support.dataFlowFreq,
							bundle:  support.asset.moveBundle,
							status: support.status
						};
					}),
				dependencies: item.dependsOnList
				.map((dependency: any) => {
					return {
						type: dependency.type,
						class: dependency.dependent.assetClass,
						name: dependency.dependent.name,
						frequency: dependency.dataFlowFreq,
						bundle: dependency.dependent.moveBundle,
						status: dependency.status
					};
				})
			}
		});
	}
}
