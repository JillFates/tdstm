import {Component, OnInit, ViewChild} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
import {GridModel} from 'tds-component-library';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DependencyAnalyzerService} from './service/dependency-analyzer.service';
import {DependencyAnalyzerDataModel, DependencyBundleModel} from './model/dependency-analyzer-data.model';
import {TagService} from '../../../assetTags/service/tag.service';
import { GridComponent } from '@progress/kendo-angular-grid';
declare var jQuery: any;

@Component({
	selector: 'tds-dependency-analyzer',
	templateUrl: 'dependency-analyzer.component.html'
})
export class DependencyAnalyzerComponent implements OnInit {

	private userContext: any;
	public gridModel: GridModel;
	public showOnlyWIP = false;
	icons = FA_ICONS;
	selectedBundle;
	teamHighlights$;
	selectedTags: number[];
	allMoveBundles;
	dependencyStatus;
	dependencyType;
	allTags;
	columns = [];
	classes = [];
	dependencyConsoleList: DependencyBundleModel[];
	gridData = [];
	showBottomGrid = false;
	keepTabContent = true;
	selectedColumn = '';
	selectedData;
	planningBundles;
	tagMatch = 'ANY';
	isAssigned = false;
	public defaultBundleItem = {name: 'All Planning', id: null};

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
			this.planningBundles = res.planningBundles;
			this.dependencyType = res.dependencyType;
			this.dependencyStatus = res.dependencyStatus;
			this.setData(res);
		});
		this.tagService.getTags().subscribe((res: any) => {
			this.allTags = res.data;
		});
	}

	setData(res) {
		this.isAssigned = res.isAssigned;
		this.gridData = [];
		this.columns = [];
		this.classes = [];
		this.columns.push('Groups');
		this.classes.push('');
		this.columns = this.columns.concat(res.dependencyConsole.group);
		this.classes = this.classes.concat(res.dependencyConsole.statusClass);
		this.gridData.push(['Application', ...res.dependencyConsole.application]);
		this.gridData.push(['Servers Physical', ...res.dependencyConsole.serversPhysical]);
		this.gridData.push(['Servers Virtual', ...res.dependencyConsole.serversVirtual]);
		this.gridData.push(['Databases', ...res.dependencyConsole.databases]);
		this.gridData.push(['Storage', ...res.dependencyConsole.storage]);
	}

	openGroupInfoModal(event) {
		// Open informative modal here
	}

	columnClicked(event, index) {
		let headers = document.querySelectorAll('th');
		for (let i = 0; i < headers.length; i++) {
			headers[i].classList.remove('active-column-header');
		}
		let tmp = event.target as HTMLElement;
		tmp.parentElement.classList.add('active-column-header');
		this.showBottomGrid = true;
		this.selectedColumn = index;
		this.selectedData = this.getDataFromIndex(index);
		this.addClassToSelectedColumn(index);
	}
	addClassToSelectedColumn(index) {
		// remove class from others if present
		let cells = document.querySelectorAll('td');
		for (let i = 0; i < cells.length; i++) {
			cells[i].classList.remove('active-column');
			cells[i].classList.remove('last-active-cell');
		}

		cells = document.querySelectorAll('td:nth-child(' + (index + 1) + ')');
		for (let i = 0 ; i < cells.length ; i++) {
			if (cells[i].textContent.trim() > '') {
				if (i === (cells.length - 1)) {
					cells[i].classList.add('active-column');
					cells[i].classList.add('last-active-cell');
				} else {
					cells[i].classList.add('active-column');
				}
			}
		}
}

	getDataFromIndex(index) {
		const retVal = {};
		retVal['group'] = this.columns[index];
		retVal['application'] = this.gridData[0][index] || 0;
		retVal['serversPhysical'] = this.gridData[1][index] || 0;
		retVal['serversVirtual'] = this.gridData[2][index] || 0;
		retVal['databases'] = this.gridData[3][index] || 0;
		retVal['storage'] = this.columns[4][index] || 0;
		return retVal;
	}

	onBundleSelect(event) {
		this.selectedBundle = event.id;
		this.getFilteredData();
	}

	getFilteredData() {
		this.dependencyAnalyzerService.getFilteredData({
			bundle: this.selectedBundle || '',
			tagIds: this.selectedTags || [],
			tagMatch: this.tagMatch,
			assignedGroup: this.isAssigned ? 1 : 0
		}).subscribe( (data: DependencyAnalyzerDataModel) => {
			this.setData(data);
		})
	}

	onAssetTagChange(event) {
		this.tagMatch = event.operator;
		this.selectedTags = event.tags.map( x => x.id);
		this.getFilteredData();
		// request data here too
	}

	onShowOnlyWIPChange(event) {
		this.getFilteredData();
	}

	onRefeshData() {
		this.getInitialData();
	}

	refreshDiagram() {
		// console.log('refresh diagram');
	}

	onTabSelect(event) {
		// console.log(event);
	}
}
