import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {Observable, ReplaySubject} from 'rxjs';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {Diagram, Layout, Link} from 'gojs';
import {UserContextService} from '../../../auth/service/user-context.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {ActivatedRoute} from '@angular/router';
import {IArchitectureGraphParams} from '../../model/url-params.model';
import {ArchitectureGraphDiagramHelper} from './architecture-graph-diagram-helper';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ComboBoxComponent} from '@progress/kendo-angular-dropdowns';
import {
	ComboBoxSearchResultModel,
	RESULT_PER_PAGE
} from '../../../../shared/components/combo-box/model/combobox-search-result.model';
import {ASSET_ICONS} from '../../model/asset-icon.constant';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';

declare var jQuery: any;

@Component({
	selector: 'tds-architecture-graph',
	templateUrl: './architecture-graph.component.html'
})
export class ArchitectureGraphComponent implements OnInit {
	// References
	@ViewChild('dropdownFooter', {static: false}) dropdownFooter: ElementRef;
	@ViewChild('innerComboBox', {static: false}) innerComboBox: ComboBoxComponent;
	@ViewChild('graph', {static: false}) graph: any;
	private comboBoxSearchModel: ComboBoxSearchModel;
	private comboBoxSearchResultModel: ComboBoxSearchResultModel;
	private searchOnScroll = true;
	private currentAssetFilterText: string;
	public datasource: any[] = [{id: '', text: ''}];
	public showControlPanel = false;
	public data$: ReplaySubject<IDiagramData> = new ReplaySubject(1);
	public ctxOpts:  ITdsContextMenuOption;
	public diagramLayout$: ReplaySubject<Layout> = new ReplaySubject(1);
	public linkTemplate$: ReplaySubject<Link> = new ReplaySubject(1);
	public userContext: UserContextModel;
	public urlParams: IArchitectureGraphParams;
	private currentNodesData;
	public categories;
	public assetClass: any = {id: '', value: ''};
	public asset: any = {id: '', text: ''};
	protected getAssetList: Function;

	public assetList;
	public graphPreferences;
	public dataForSelect;
	public dataForSelect2;
	public levelsUp = 0;
	public levelsDown = 1;
	public showCycles;
	public appLbl;
	public labelOffset;
	public assetClasses;
	public assetId;
	public mode = 'assetId';
	public dataForGraph;
	public showLabels = false;
	public showLegend = false;
	public assetIconsPath = ASSET_ICONS;

	public assetItem;

	public graphLabels = [
		{
			icon: 'application',
			label: 'Application',
			value: 'APPLICATION',
			checked: false
		},
		{
			icon: 'database',
			label: 'Database',
			value: 'DATABASE',
			checked: false
		},
		{
			icon: 'serverPhysical',
			label: 'Physical Server',
			value: 'physical server',
			checked: false
		},
		{
			icon: 'serverVirtual',
			label: 'Virtual Server',
			value: 'virtual server',
			checked: false
		},
		{
			icon: 'storageLogical',
			label: 'Logical Storage',
			value: 'Logical Storage',
			checked: false
		},
		{
			icon: 'storagePhysical',
			label: 'Storage Device',
			value: 'Storage Device',
			checked: false
		},
		{
			icon: 'networkLogical',
			label: 'Network Device',
			value: 'Network Device',
			checked: false
		},
		{
			icon: 'other',
			label: 'Other Device',
			value: 'Device',
			checked: false
		}
	];

	constructor(
		private userContextService: UserContextService,
		private activatedRoute: ActivatedRoute,
		private architectureGraphService: ArchitectureGraphService,
		private assetExplorerService: AssetExplorerService,
		private sanitized: DomSanitizer,
		private preferenceService: PreferenceService
	) {
		this.activatedRoute.queryParams.subscribe((data: IArchitectureGraphParams) => this.urlParams = data);
		this.getAssetList = this.assetExplorerService.getAssetListForComboBox.bind(this.assetExplorerService);
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		// If it comes from asset explorer
		if (this.urlParams && this.urlParams.assetId) {
			this.assetId = this.urlParams.assetId;
			this.loadData();
		}
		this.getArchitectureGraphPreferences();
		this.initSearchModel();
		this.loadAssetsForDropDown();
	}

	/**
	 * A call to the Architecture graph service for getting the default graph data for the current user
	 */
	getArchitectureGraphPreferences() {
		this.architectureGraphService.getArchitectureGraphPreferences().subscribe( (res: any) => {
			this.dataForSelect = res.assetClassesForSelect;
			this.levelsUp = +res.graphPrefs.levelsUp;
			this.levelsDown = +res.graphPrefs.levelsDown;
			this.showCycles = res.graphPrefs.showCycles;
			this.appLbl = res.graphPrefs.appLbl;
			this.labelOffset = res.graphPrefs.labelOffset;
			this.assetClasses = res.graphPrefs.assetClasses;
		});
	}

	/**
	 * Loads assets for dropdown when you are scrolling on the list
	 */
	loadAssetsForDropDown() {
		this.assetExplorerService.getAssetListForComboBox(this.comboBoxSearchModel)
			.subscribe((res: any) => {
				this.comboBoxSearchResultModel = res;
				const result = (this.comboBoxSearchResultModel.results || []);
				result.forEach((item: any) => this.addToDataSource(item));
				if (this.searchOnScroll && this.comboBoxSearchResultModel.total > RESULT_PER_PAGE) {
					this.calculateLastElementShow();
				}
		});
	}

	/**
	 *  Sets the selected asset ID for retrieving it's graph data
	 */
	onAssetSelected(event) {
		if (event) {
			this.graphLabels.forEach(item => {
				item.checked = false;
			});
			this.showLabels = false;
			this.assetId = event.id;
			this.loadData();
		}
	}

	/**
	 * Loads the asset data once an item has been clicked
	 * */
	loadData() {
		this.architectureGraphService.getArchitectureGraphData(this.assetId, this.levelsUp, this.levelsDown, this.mode)
			.subscribe( (res: any) => {
				this.currentNodesData = res;
				this.updateNodeData(this.currentNodesData, true);
			});
	}

	/**
	 * Shows/hides the control panel
	 */
	toggleControlPanel() {
		if (this.showLegend) {
			this.showLegend = !this.showLegend;
		}
		this.showControlPanel = !this.showControlPanel;
	}

	/**
	 * Reduces the levels up of the graph and reload the data
	 */
	extractLevelsUp() {
		if (this.levelsUp > 0) {
			this.levelsUp--;
			this.loadData();
		}
	}

	/**
	 * Adds the levels up of the graph and reload the data
	 */
	addLevelsUp() {
		this.levelsUp++;
		this.loadData();
	}

	/**
	 * Reduces the levels down of the graph and reload the data
	 */
	addLevelsDown() {
		this.levelsDown++;
		this.loadData();
	}

	/**
	 * Adds the levels down of the graph and reload the data
	 */
	extractLevelsDown() {
		if (this.levelsDown > 0) {
			this.levelsDown--;
			this.loadData();
		}
	}

	toggleShowCycles() {
		this.showCycles = !this.showCycles;
		const cycles = [];
		// TODO: get cycle references from BE
		// this.data$.subscribe( res => {
		// 	console.log('subscribed to data', res);
		// });
		// this.data$.forEach(arr => cycles.push(...arr));
		// this.graph.highlightNodesByCycle(cycles);
	}

	toggleShowLabels() {
		this.showLabels = !this.showLabels;
	}

	/**
	 * Generate the item template for the task items to show in the format taskId : Description
	 * @event dataItem contains the task item information
	 * @returns {string}
	 */
	protected innerTemplateTaskItem(dataItem: any): string {
		return dataItem.text;
	}

	/**
	 * Keep listening if the element show is the last one
	 * @returns {any}
	 */
	private calculateLastElementShow(): any {
		setTimeout(() => {
			if (this.dropdownFooter && this.dropdownFooter.nativeElement) {
				let nativeElement = this.dropdownFooter.nativeElement;
				let scrollContainer = jQuery(nativeElement.parentNode).find('.k-list-scroller');
				jQuery(scrollContainer).off('scroll');
				jQuery(scrollContainer).on('scroll', (element) => {
					this.onLastElementShow(element.target);
				});
			}
		}, 800);
	}

	/**
	 * Calculate the visible height + pixel scrolled = total height
	 * If Result Set Per Page is less than the max total of result found, continue scrolling
	 * @param element
	 */
	private onLastElementShow(element: any): void {
		if (element.offsetHeight + element.scrollTop === element.scrollHeight) {
			if ((RESULT_PER_PAGE * this.comboBoxSearchResultModel.page) <= this.comboBoxSearchResultModel.total) {
				this.comboBoxSearchModel.currentPage++;
				console.log('this.comboBoxSearchModel', this.comboBoxSearchModel);
				this.loadAssetsForDropDown();
			}
		}
	}

	/**
	 * The Search model is being separated from the model attached to the comboBox
	 */
	private initSearchModel(): void {
		this.comboBoxSearchModel = {
			query: '',
			currentPage: 1,
			metaParam: '',
			value: '',
			maxPage: 25
		};
	}

	/**
	 * Search for matching text of current comboBox filter
	 * @param {any} dataItem
	 * @returns {SafeHtml}
	 */
	public comboBoxInnerSearch(dataItem: any): SafeHtml {
		if (!dataItem.text) {
			dataItem.text = '';
		}
		const regex = new RegExp(this.innerComboBox.text, 'i');
		const text =  (this.innerTemplateTaskItem) ? this.innerTemplateTaskItem(dataItem) : dataItem.text;

		const transformedText = text.replace(regex, `<b>$&</b>`);

		return this.sanitized.bypassSecurityTrustHtml(transformedText);
	}

	/**
	 * Add the item to the datasource if this parameter doesn't exists on collection
	 * @param model Item to add
	 */
	addToDataSource(model: any): void {
		if (model && model.id && !this.datasource.find((item) => item.id === model.id)) {
			this.datasource.push(model);
		}
	}

	goFullScreen() {
		console.log('go full screen');
	}

	toggleLegend() {
		if (this.showControlPanel) {
			this.showControlPanel = !this.showControlPanel;
		}
		this.showLegend = !this.showLegend;
	}

	/**
	 * shows or hides the labels of the asset on the graph based on what's selected on the labels checkboxes
	 * @param index of the selected checkbox
	 */
	updateGraphLabels(index) {
		this.graphLabels[index].checked = !this.graphLabels[index].checked;
		// TODO: filter nodes for removing labels on graph
		this.categories = this.graphLabels.filter( label => label.checked).map( label => label.value.toUpperCase());
		if (this.categories.length > 0 && this.assetId) {
			let tempNodesData = Object.assign({}, this.currentNodesData);
			tempNodesData.nodes.forEach( node => {
				if (!this.categories.includes(node.assetClass)) {
					node.name = '';
				}
			});
			this.updateNodeData(tempNodesData, false);
		} else {
			this.categories = [];
			this.updateNodeData(this.currentNodesData, true);
		}
	}

	/**
	 * 	Sends new data to the graph when updating labels
	 *  @param data graph data and configuration
	 *  @param iconsOnly if show the labels or not
	 */
	updateNodeData(data, iconsOnly) {
		const diagramHelper = new ArchitectureGraphDiagramHelper();
		this.data$.next(diagramHelper.diagramData({
			rootAsset: this.assetId,
			currentUserId: 1,
			data: data,
			iconsOnly: iconsOnly,
			extras: {
				diagramOpts: {
					autoScale: Diagram.Uniform,
					allowZoom: true
				},
				isExpandable: false
			}
		}));
	}

	/**
	 * Generate the graph with the current data
	 */
	regenerateGraph() {
		this.loadData();
	}

	/**
	 * Save the preferences changed on the control panel
	*/
	savePreferences() {
		const valueData = {
			assetClass: 'ALL',
			levelsUp: this.levelsUp,
			levelsDown: this.levelsDown,
			showCycles: this.showCycles,
			appLbl: this.categories.length > 0
		};
		this.preferenceService.setPreference('ARCH_GRAPH', JSON.stringify(valueData)).subscribe( res => {
			if (res.status === 'success') {
				console.log('Preferences saved correctly');
			}
		})
	}

	/**
	 * Gets the default graph preferences from Backend
	 * */
	resetDefaults() {
		this.getArchitectureGraphPreferences();
	}

	goBackToNormalGraph() {
		console.log('go back to normal');
	}

}
