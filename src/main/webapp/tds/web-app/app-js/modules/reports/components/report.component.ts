// Angular
import {SafeHtml} from '@angular/platform-browser';
import {ComponentFactoryResolver} from '@angular/core';
// Model
import {DialogService, ModalSize} from 'tds-component-library';
// Module
import {AssetExplorerModule} from '../../assetExplorer/asset-explorer.module';
// Service
import {ReportsService} from '../service/reports.service';
// Component
import {AssetShowComponent} from '../../assetExplorer/components/asset/asset-show.component';

export abstract class ReportComponent {
	// Shared variables
	hideFilters = false;
	reportResult: SafeHtml;
	loadingLists = false;
	generatedReport = false;

	constructor(
		protected componentFactoryResolver: ComponentFactoryResolver,
		protected reportsService: ReportsService,
		protected dialogService: DialogService
	) {
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
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetShowComponent,
			data: {
				assetId: assetId,
				assetClass: assetClass,
				assetExplorerModule: AssetExplorerModule
			},
			modalConfiguration: {
				title: 'Asset',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe();
	}

	abstract onGenerateReport();
}
