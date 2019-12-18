// Angular
import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable, forkJoin} from 'rxjs';
import 'hammerjs';

// Services
import {InsightService} from '../../service/insight.service';
// Components

// Model

import {ActivatedRoute} from '@angular/router';

@Component({
	selector: 'insight-dashboard',
	templateUrl: 'insight-dashboard.component.html'
})

export class InsightDashboardComponent implements OnInit {

	public insightData: any;

	public dataForProviders: any;
	public dataTopTags: any;
	public dataApplicationsGroupedByDependencies: any;
	public dataAssetsByOsAndEnvironment: any;
	public dataDevicesByEvent: any;
	public dataAssetsByProvidersAndAssetsType: any;
	public assetsByOs: any;
	public currentOsSelection: any;

	private showAssetsByVendorTable = false;
	private showTopTags = false;
	private showDependenciesByVendor = false;
	private showApplicationGroupedByDependencies = false;
	private showDataByProviders = false;
	private showDevicesByEvent = false;
	private showAssetsByProvider = false;
	private showAssetsByOs = false;

	constructor(
		private route: ActivatedRoute,
		private insightService: InsightService
	) {
	}

	ngOnInit() {
		this.populateData();
	}

	/**
	 * Call the endpoints required to populate the initial data
	 */
	private populateData(): void {

		const insightDataForProvidersReq = this.insightService.getInsightDataForProviders();
		const insightDataTopTagsReq = this.insightService.getInsightDataTopTags();
		const insightDataForBlastRadiusReq = this.insightService.getInsightDataForBlastRadious();
		const insightDataForAssetsByOsAndEnvironmentReq = this.insightService.getInsightDataForAssetsByOsAndEnviroment();
		const insightDataForDevicesByEventReq = this.insightService.getInsightDataForDevicesByEvent();
		const insightDataForAssetsByProviderAndAssetTypeReq = this.insightService.getInsightDataForAssetsByProviderAndAssetType();

		forkJoin([
			insightDataForProvidersReq,
			insightDataTopTagsReq,
			insightDataForBlastRadiusReq,
			insightDataForAssetsByOsAndEnvironmentReq,
			insightDataForDevicesByEventReq,
			insightDataForAssetsByProviderAndAssetTypeReq])
			.subscribe(responseList => {
				this.dataForProviders = responseList[0].assetsAndDependenciesByProvider;
				this.dataTopTags = responseList[1].topTags;
				this.dataApplicationsGroupedByDependencies = responseList[2].applicationsGroupedByDependencies;
				this.dataAssetsByOsAndEnvironment = responseList[3].assetsByOsAndEnvironment;
				// this.splitAssetsByOsData(this.dataAssetsByOsAndEnvironment);
				this.dataDevicesByEvent = responseList[4].devicesByEvent;
				this.dataAssetsByProvidersAndAssetsType = responseList[5].AssetsByProviderAndAssetType;
			});
	}

	toggleTableFor(chartName) {
		switch (chartName) {
			case 'ASSETS_BY_VENDOR': {
				this.showAssetsByVendorTable = !this.showAssetsByVendorTable;
				break;
			}
			case 'DEPENDENCIES_BY_VENDOR': {
				this.showDependenciesByVendor = !this.showDependenciesByVendor;
				break;
			}
			case 'TOP_TAGS': {
				this.showTopTags = !this.showTopTags;
				break;
			}
			case 'DATA_BY_PROVIDERS': {
				this.showDataByProviders = !this.showDataByProviders;
				break;
			}
			case 'DEVICES_BY_EVENT': {
				this.showDevicesByEvent = !this.showDevicesByEvent;
				break;
			}
			case 'ASSETS_BY_PROVIDERS_AND_ASSET_TYPE': {
				this.showAssetsByProvider = !this.showAssetsByProvider;
				break;
			}
			case 'ASSETS_BY_OS_AND_ENVIRONMENT': {
				this.showAssetsByOs = !this.showAssetsByOs;
				break;
			}
		}
	}

	public labelContent(e: any): string {
		return e.category;
	}

	public dataForCategoriesChart(rawData) {
		let retVal = [];
		if (rawData) {
			for (let col of rawData) {
				retVal.push(col.Name);
			}
		}
		return retVal;
	}

	public dataForAssetsByProvidersChart(rawData) {
		let retVal = [];
		if (rawData) {
			for (let col of rawData) {
				retVal.push(col['Asset Type'] + '\n' + col.Provider);
			}
		}
		return retVal;
	}

	public dataForProviderChartDependencies(rawData) {
		let retVal = [];
		if (rawData) {
			for (let row of rawData) {
				retVal.push(row.Dependencies);
			}
		}
		return retVal;
	}

	public dataForProviderChartAssets(rawData) {
		let retVal = [];
		if (rawData) {
			for (let row of rawData) {
				retVal.push(row.Assets);
			}
		}
		return retVal;
	}

	public dataForAssetsChart(rawData) {
		let retVal = [];
		if (rawData) {
			for (let row of rawData) {
				retVal.push([row.Total, row.Processed, row.Pending, row.Errors]);
			}
		}
		return retVal;
	}

	dataForAssetsChartTotal(rawData) {
		let retVal = [];
		if (rawData) {
			for (let row of rawData) {
				retVal.push(row.Total);
			}
		}
		return retVal;
	}

	dataForAssetsChartProcessed(rawData) {
		let retVal = [];
		if (rawData) {
			for (let row of rawData) {
				retVal.push(row.Processed);
			}
		}
		return retVal;
	}

	dataForAssetsChartPending(rawData) {
		let retVal = [];
		if (rawData) {
			for (let row of rawData) {
				retVal.push(row.Pending);
			}
		}
		return retVal;
	}

	dataForAssetsChartErrors(rawData) {
		let retVal = [];
		if (rawData) {
			for (let row of rawData) {
				retVal.push(row.Errors);
			}
		}
		return retVal;
	}

	splitAssetsByOsData(rawData) {
		this.assetsByOs = rawData.reduce((h, obj) => {
			h[obj.Environment] = (h[obj.Environment] || []).concat(obj);
			return h;
		}, {});
		return Object.keys(this.assetsByOs);
	}

	osChange(event) {
		this.currentOsSelection = this.assetsByOs[event];
	}
}
