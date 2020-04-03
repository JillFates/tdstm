import { Component, OnInit} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
import {GridModel} from 'tds-component-library';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DependencyAnalyzerService} from './service/dependency-analyzer.service';
import {DependencyAnalyzerDataModel, DependencyBundleModel} from './model/dependency-analyzer-data.model';
import {TagService} from '../../../assetTags/service/tag.service';
declare var jQuery: any;

@Component({
	selector: 'tds-dependency-analyzer',
	templateUrl: 'dependency-analyzer.component.html'
})
export class DependencyAnalyzerComponent implements OnInit {
	private userContext: any;
	public gridModel: GridModel;
	public showOnlyWIP;
	icons = FA_ICONS;
	selectedBundle;
	teamHighlights$;
	selectedTags: number[];
	allMoveBundles;
	dependencyStatus;
	dependencyType;
	allTags;
	gridStats;
	dependencyConsoleList: DependencyBundleModel[];
	gridData = [];
	finalData = [];

	constructor(
		private userContextService: UserContextService,
		private dependencyAnalyzerService: DependencyAnalyzerService,
		private tagService: TagService
	) {
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		this.getInitialData();
	}

	getInitialData() {
		this.dependencyAnalyzerService.getDependencyAnalyzerData().subscribe(( res: DependencyAnalyzerDataModel) => {
			this.allMoveBundles = res.allMoveBundles;
			this.dependencyType = res.dependencyType;
			this.dependencyStatus = res.dependencyStatus;
			this.dependencyConsoleList = res.dependencyConsoleList;
			this.gridStats = res.gridStats;

			// "dependencyBundle": 0,
			// 			// 	"appCount": 278,
			// 			// 	"serverCount": 36,
			// 			// 	"vmCount": 4,
			// 			// 	"dbCount": 14,
			// 			// 	"storageCount": 52,
			// 			// 	"statusClass": "depGroupConflict"

			this.gridData.push(
				[
					'Application',
					'Servers Physical',
					'Servers Virtual',
					'Databases',
					'Storage (all)'
				]
			);
			// console.log('entries', Object.keys(this.gridStats));
			this.gridData.push([this.gridStats['app'][0], this.gridStats['server'][0], this.gridStats['vm'][0], this.gridStats['db'][0], this.gridStats['storage'][0]]);
			this.gridData.push([this.gridStats['app'][0] - this.gridStats['app'][1], this.gridStats['server'][0] - this.gridStats['server'][1], this.gridStats['vm'][0] - this.gridStats['vm'][1],
				this.gridStats['db'][0] - this.gridStats['db'][1], this.gridStats['storage'][0] - this.gridStats['storage'][1]]);
			this.gridData.push([this.gridStats['app'][1], this.gridStats['server'][1], this.gridStats['vm'][1], this.gridStats['db'][1], this.gridStats['storage'][1]]);

			for (let item of this.dependencyConsoleList) {
				this.gridData.push([item.appCount, item.serverCount, item.vmCount, item.dbCount, item.storageCount]);
			}
			console.log(this.gridData);
			this.finalData = this.getPivotArray(this.gridData, 0, 1, 2);
			console.log(this.finalData);

		});
		this.tagService.getTags().subscribe((res: any) => {
			this.allTags = res.data;
		});
	}

	getPivotArray(dataArray) {
		// Code from https://techbrij.com
		let result = {}, ret = [];
		let newCols = [];

		for (let i = 0; i < dataArray.length; i++) {
			for (let j = 0; j < dataArray[i].length; j++) {
				if (ret[j]) {
					ret[j].push(dataArray[i][j]);
				} else {
					ret[j] = [dataArray[i][j]];
				}
			}
		}
		return ret;
	}

	onBundleSelect(event) {
		console.log('bundle selected: ', event);
	}

	onAssetTagChange(event) {
		console.log(event);
	}

	onShowOnlyWIPChange(event) {
		console.log('show only work in progress');
	}

	onRefeshData() {
		this.getInitialData();
	}

	cellClick(event) {
		console.log(' on cell clicked');
	}

	highlightByTeam(event) {
		console.log('something');
	}

	refreshDiagram() {
		console.log('refresh diagram');
	}
}
