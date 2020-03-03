import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {Observable, ReplaySubject} from 'rxjs';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {Diagram, Layout, Link, Spot} from 'gojs';
import {UserContextService} from '../../../auth/service/user-context.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {ActivatedRoute} from '@angular/router';
import {IArchitectureGraphParams} from '../../model/url-params.model';
import {ArchitectureGraphDiagramHelper} from './architecture-graph-diagram-helper';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
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
	@ViewChild('graph', {static: false}) graph: any;
	private comboBoxSearchModel: ComboBoxSearchModel;
	private comboBoxSearchResultModel: ComboBoxSearchResultModel;
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
	public assetClass: any = {id: 'ALL', value: 'All Classes'};
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
	public toggleFullScreen = true;

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
			value: 'Device',
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
			value: 'DEVICE',
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
		this.getAssetList = this.listAssets.bind(this);
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
	 *
	 * @param searchParams Based on the model params executes the search to get the corresponding assets list
	 * @returns {Observable<any>}
	 */
	listAssets(searchParams: ComboBoxSearchModel): Observable<ComboBoxSearchResultModel> {
		const params = {...searchParams};
		params.value = '';
		return this.assetExplorerService.getAssetListForComboBox(params);
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
		});
	}

	/**
	 *  Sets the selected asset ID for retrieving it's graph data
	 */
	onAssetSelected(event) {
		if (event) {
			this.assetId = event.id;
			this.loadData();
		}
	}

	/**
	 * Loads the asset data once an item has been clicked
	 * */
	loadData() {
		this.architectureGraphService
			.getArchitectureGraphData(this.assetId, this.levelsUp, this.levelsDown, this.mode)
				.subscribe( (res: any) => {
					this.currentNodesData = res;
					this.updateNodeData(this.currentNodesData, false);
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
	 * Add the item to the datasource if this parameter doesn't exists on collection
	 * @param model Item to add
	 */
	addToDataSource(model: any): void {
		if (model && model.id && !this.datasource.find((item) => item.id === model.id)) {
			this.datasource.push(model);
		}
	}

	goFullScreen() {
		this.toggleFullScreen = !this.toggleFullScreen;
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
		this.toggleGraphLabel(index);
		this.updateNodeData(this.currentNodesData, false);
	}

	/**
	 * Check/uncheck the value for an specific graph label
	 * @param index  Index of the array to be modified
	 */
	toggleGraphLabel(index: number): void {
		this.graphLabels[index].checked = !this.graphLabels[index].checked;
	}

	/**
	 * Iterate over the current selected categories, in case the category is not selected
	 * remove the node names for that categories
	 * @param data graph data and configuration
	 * @returns the clone of graph data modified
	 */
	removeNodeNamesForNotSelectedCategories(data: any): any {
		let clonedNodes = JSON.parse(JSON.stringify(data));

		const categories = this.getSelectedCategories(); // this.categories || [];
		clonedNodes.nodes.forEach(node => {
			// Clear the node name if the assetClass is not included on the categories selected
			if (!categories.includes(node.assetClass)) {
				node.name = '';
			}
		});
		// }
		return clonedNodes;
	}

	/**
	 * Returns an array containing just the current selected categories
	 */
	getSelectedCategories(): any[] {
		return this.graphLabels
			.filter( label => label.checked)
			.map( label => label.value.toUpperCase());
	}

	/**
	 * 	Sends new data to the graph when updating labels
	 *  @param data graph data and configuration
	 *  @param iconsOnly if show the labels or not
	 */
	updateNodeData(data, iconsOnly) {
		const clonedData = this.removeNodeNamesForNotSelectedCategories(data);
		console.log(clonedData.nodes);
		console.log(iconsOnly);

		const diagramHelper = new ArchitectureGraphDiagramHelper();
		this.data$.next(diagramHelper.diagramData({
			rootAsset: this.assetId,
			currentUserId: 1,
			data: clonedData,
			iconsOnly: iconsOnly,
			extras: {
				diagramOpts: {
					initialAutoScale: Diagram.UniformToFill,
					contentAlignment: Spot.Center,
					allowZoom: true,
				},
				isExpandable: false
			}
		}));
	}

	/**
	 * Generate the graph with the current data
	 */
	regenerateGraph() {
		this.graph.showFullGraphBtn = false;
		if (this.assetId) {
			this.loadData();
		}
	}

	/**
	 * Save the preferences changed on the control panel
	*/
	savePreferences() {
		if (this.categories) {
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
	}

	/**
	 * Gets the default graph preferences from Backend
	 * */
	resetDefaults() {
		this.getArchitectureGraphPreferences();
		this.resetLabels();
	}

	onDiagramAnimationFinished() {
		this.graph.showFullGraphBtn = false;
	}

	viewFullGraphFromCache() {
		this.graph.showFullGraphBtn = false;
	}

	resetLabels() {
		this.graphLabels.forEach( el => {
			el.checked = false;
		});
		this.updateNodeData(this.currentNodesData, true);
	}

}
