import { Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
import {GridModel} from 'tds-component-library';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DependencyAnalyzerService} from './service/dependency-analyzer.service';
import {DependencyAnalyzerDataModel} from './model/dependency-analyzer-data.model';
import {TagService} from '../../../assetTags/service/tag.service';
import {TagModel} from '../../../assetTags/model/tag.model';
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
		});
		this.tagService.getTags().subscribe((res: any) => {
			this.allTags = res.data;
		});
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
