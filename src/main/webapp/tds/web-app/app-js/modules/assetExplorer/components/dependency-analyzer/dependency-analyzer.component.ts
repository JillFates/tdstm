import {Component, ComponentFactoryResolver, OnInit} from '@angular/core';
import {UserContextService} from '../../../auth/service/user-context.service';
import {DialogService, GridModel, ModalSize} from 'tds-component-library';
import {FA_ICONS} from '../../../../shared/constants/fontawesome-icons';
import {DependencyAnalyzerService} from './service/dependency-analyzer.service';
import {DependencyAnalyzerDataModel, DependencyBundleModel} from './model/dependency-analyzer-data.model';
import {TagService} from '../../../assetTags/service/tag.service';
import {RegenerateComponent} from './components/regenerate/regenerate.component';
import {DependencyGroupStatusComponent} from './components/dependency-group-status-modal/dependency-group-status.component';
import {RegenerateProgressDialogComponent} from './components/regenerate-progress-dialog/regenerate-progress-dialog.component';

@Component({
	selector: 'tds-dependency-analyzer',
	templateUrl: 'dependency-analyzer.component.html'
})
export class DependencyAnalyzerComponent implements OnInit {

	private userContext: any;
	public gridModel: GridModel;
	public showOnlyWIP = false;
	public icons = FA_ICONS;
	public selectedBundle;
	public teamHighlights$;
	public selectedTags: number[];
	public allMoveBundles;
	public dependencyStatus;
	public dependencyType;
	public allTags;
	public columns = [];
	public classes = [];
	public gridData = [];
	public showBottomGrid = false;
	public keepTabContent = true;
	public selectedColumn = '';
	public selectedData;
	public planningBundles;
	public tagMatch = 'ANY';
	public isAssigned = false;
	public depGrpCrt;
	public defaultBundleItem = {name: 'All Planning', id: null};

	constructor(
		private userContextService: UserContextService,
		private dependencyAnalyzerService: DependencyAnalyzerService,
		private tagService: TagService,
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
	) {
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		this.getInitialData();
	}

	/**
	 * Gets the initial data to be displayed on the grid
	 * Fills the Bundle dropdowns and the tagsList
	 * * */
	getInitialData() {
		this.dependencyAnalyzerService.getDependencyAnalyzerData().subscribe(( res: DependencyAnalyzerDataModel) => {
			this.allMoveBundles = res.allMoveBundles;
			this.planningBundles = res.planningBundles;
			this.setData(res);
		});
		this.tagService.getTags().subscribe((res: any) => {
			this.allTags = res.data;
		});
	}

	/**
	 * Converts the data that comes from the BE into usable row for the table
	 * @param res: DependencyAnalyzerDataModel
	 * Set the initial data empty to make sure no old data is present
	 * */
	setData(res) {
		this.isAssigned = res.isAssigned;
		this.cleanObjects();
		this.dependencyType = res.dependencyType;
		this.dependencyStatus = res.dependencyStatus;
		this.depGrpCrt = res.depGrpCrt;
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

	/**
	 * Sets the content of the lists to empty
	 * */
	cleanObjects() {
		delete this.gridData;
		delete this.columns;
		delete this.classes;
		delete this.dependencyType;
		delete this.dependencyStatus;
		delete this.depGrpCrt;
	}

	/**
	 * Shows information modal for groups
	 * ie: what the color of the column means
	 * */
	openGroupInfoModal(event) {
		// Open informative modal here
		try {
			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: DependencyGroupStatusComponent,
				data: {},
				modalConfiguration: {
					title: '',
					draggable: true,
					modalSize: ModalSize.MD
				}
			});
		} catch (error) {
			console.error(error);
		}
	}

	/**
	 * When a column is clicked, this will add the css to the row
	 * this will also set the selected data to the electedData variable
	 * @param event: HTML reference to the clicked column
	 * @param index: number the index of the clicked column
	 * */
	columnClicked(event, index) {
		this.clearHeaders();
		let tmp = event.target as HTMLElement;
		tmp.parentElement.classList.add('active-column-header');
		this.showBottomGrid = true;
		this.selectedColumn = index;
		this.selectedData = this.getDataFromIndex(index);
		this.addClassToSelectedColumn(index);
	}

	/**
	 * Adds the border class to the selected Column
	 * @param index: number index of the column that will get the selected class
	 * */
	addClassToSelectedColumn(index) {
		this.clearAllCells();
		let cells = document.querySelectorAll('td:nth-child(' + (index + 1) + ')');
		for (let i = 0 ; i < cells.length ; i++) {
			// if (cells[i].textContent.trim() > '') {
				if (i === (cells.length - 1)) {
					cells[i].classList.add('active-column');
					cells[i].classList.add('last-active-cell');
				} else {
					cells[i].classList.add('active-column');
				}
			// }
		}
	}

	/**
	 * This only clears the headers of the table from the selected class
	 * but not the table cells
	 * */
	clearHeaders() {
		let headers = document.querySelectorAll('th');
		for (let i = 0; i < headers.length; i++) {
			headers[i].classList.remove('active-column-header');
		}
	}

	/**
	 * Clears all the cell of the table from the selected class
	 * */
	clearAllCells() {
		let cells = document.querySelectorAll('td');
		for (let i = 0; i < cells.length; i++) {
			cells[i].classList.remove('active-column');
			cells[i].classList.remove('last-active-cell');
		}
	}

	/**
	 * Gets all the data when a column is selected
	 * @param index: number
	 * @return object with all the data of the selected item
	 * */
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

	/**
	 * Triggers when a bundle is selected
	 * @param event: object contains the selected bundle
	 * */
	onBundleSelect(event) {
		this.selectedBundle = event.id;
		this.getFilteredData();
	}

	/**
	 * Triggers when a filter is being used
	 * filter being: Bundles, Tags, and Show only work in progress dependencies
	 * */
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

	/**
	 * When a tag o multiple tags are selected, the filters data is set and
	 * the filtered data method is activated to retrieve new information.
	 * @event event: Object which contains the info that comes from the selected data
	 * */
	onAssetTagChange(event) {
		this.tagMatch = event.operator;
		this.selectedTags = event.tags.map( x => x.id);
		this.getFilteredData();
		// request data here too
	}

	/**
	 * Activates when the user only wants to see the work in progress data
	 * */
	onShowOnlyWIPChange(event) {
		this.clearAllCells();
		this.clearHeaders();
		this.getFilteredData();
	}

	/**
	 * Refreshes the data of the table
	 * */
	onRefeshData() {
		this.getInitialData();
	}

	/**
	 * These will be used for whoever works on the tabs (which could also include me)
	 * */
	onTabSelect(event) {
		// console.log(event);
	}

	/**
	 * Open the regenerate Dialog to select the different dependency type and status
	 * and save selected list as default too
	 * When the regenerate buttons is clicked inside the modal, the getInitialData is called
	 * to retrieve the changes of the regenerate.
	 * */
	regenerate() {
		// open modal
		try {
			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: RegenerateComponent,
				data: {
					dependencyType: this.dependencyType,
					dependencyStatus: this.dependencyStatus,
					depGrpCrt: this.depGrpCrt
				},
				modalConfiguration: {
					title: 'Dependency Grouping Control',
					draggable: true,
					modalSize: ModalSize.MD
				}
			}).subscribe((data: any) => {
				if (data.dependencyType) {
					let regenString = '';
					data.dependencyType.forEach( el => {
						regenString += 'connection=' + el + '&';
					});
					data.dependencyStatus.forEach( el => {
						regenString += 'status=' + el + '&';
					});
					regenString += 'saveDefault=' + (data.saveDefault ? '1&' : '0&');
					regenString += 'bundle=' + (this.selectedBundle ? this.selectedBundle : '');
					this.dependencyAnalyzerService.regenerateData(regenString).subscribe( (res: any) => {
						const regenKey = res.data.key;
						try {
							this.dialogService.open({
								componentFactoryResolver: this.componentFactoryResolver,
								component: RegenerateProgressDialogComponent,
								data: {
									regenKey
								},
								modalConfiguration: {
									title: 'Generating Dependency Groups',
									draggable: true,
									modalSize: ModalSize.MD,
								}
							}).subscribe((data: any) => {
								this.getFilteredData();
							});
						} catch (error) {
							console.error(error);
						}
					});
				}
			});
		} catch (error) {
			console.error(error);
		}
	}
}
