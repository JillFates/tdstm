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
import {NgForm} from '@angular/forms';

declare var jQuery: any;

@Component({
	selector: 'tds-architecture-graph',
	templateUrl: './architecture-graph.component.html'
})
export class ArchitectureGraphComponent implements OnInit {
	// References
	@ViewChild('graph', {static: false}) graph: any;
	@ViewChild('archGraphForm', { static: false }) protected form: NgForm;

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
	public selectedAsset = null;

	public assetItem;
	private TAG_APPLICATION = 'appLbl';
	private TAG_DATABASE = 'dbLbl';
	private TAG_PHYSICAL_SERVER = 'svrLbl';
	private TAG_VIRTUAL_SERVER = 'svrLbl';
	private TAG_STORAGE_LOGICAL = 'slLbl';
	private TAG_STORAGE_DEVICE = 'slpLbl';
	private TAG_NETWORK_LOGICAl = 'netLbl';
	private TAG_OTHER_DEVICES = 'oLbl';

	public graphLabels = [
		{
			icon: 'application',
			label: 'Application',
			value: 'APPLICATION',
			tagLabel: this.TAG_APPLICATION,
			checked: false
		},
		{
			icon: 'database',
			label: 'Database',
			value: 'DATABASE',
			tagLabel: this.TAG_DATABASE,
			checked: false
		},
		{
			icon: 'serverPhysical',
			label: 'Physical Server',
			value: 'physical server',
			tagLabel: this.TAG_PHYSICAL_SERVER,
			checked: false
		},
		{
			icon: 'serverVirtual',
			label: 'Virtual Server',
			value: 'Device',
			tagLabel: this.TAG_VIRTUAL_SERVER,
			checked: false
		},
		{
			icon: 'storageLogical',
			label: 'Logical Storage',
			value: 'Logical Storage',
			tagLabel: this.TAG_STORAGE_LOGICAL,
			checked: false
		},
		{
			icon: 'storagePhysical',
			label: 'Storage Device',
			value: 'Storage Device',
			tagLabel: this.TAG_STORAGE_DEVICE,
			checked: false
		},
		{
			icon: 'networkLogical',
			label: 'Network Device',
			value: 'Network Device',
			tagLabel: this.TAG_NETWORK_LOGICAl,
			checked: false
		},
		{
			icon: 'other',
			label: 'Other Device',
			value: 'DEVICE',
			tagLabel: this.TAG_OTHER_DEVICES,
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
			this.loadData(true);
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
		this.architectureGraphService
			.getArchitectureGraphPreferences()
			.subscribe((res: any) => {
				this.dataForSelect = res.assetClassesForSelect;
				this.levelsUp = +res.graphPrefs.levelsUp;
				this.levelsDown = +res.graphPrefs.levelsDown;
				this.showCycles = res.graphPrefs.showCycles;
				this.appLbl = res.graphPrefs.appLbl;
				this.labelOffset = res.graphPrefs.labelOffset;
				this.assetClasses = res.graphPrefs.assetClasses;
				// this.assetId = res.graphPrefs.assetClass;
				this.asset = res.graphPrefs.selectedAsset;
				this.assetClass = res.graphPrefs.assetClass;

				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_APPLICATION);
				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_DATABASE);
				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_PHYSICAL_SERVER);
				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_VIRTUAL_SERVER);
				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_STORAGE_LOGICAL);
				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_STORAGE_DEVICE);
				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_NETWORK_LOGICAl);
				this.markAsPreferenceChecked(res.graphPrefs, this.TAG_OTHER_DEVICES);
		});
	}

	/**
	 * Based on the preference value, check/uncheck the label item
	 * @param preferences current preference array
	 * @param label  Label of the preference being evaluated
	 */
	markAsPreferenceChecked(preferences: any, label: string): void {
		if (preferences[label] === 'true') {
			this.graphLabels.forEach((item: any) => {
				if (item.tagLabel === label) {
					item.checked = true;
				}
			});
		}
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
			this.selectedAsset = event;
			this.form.controls['assetClass'].markAsDirty();
			this.loadData();
		}
	}

	/**
	 * Loads the asset data once an item has been clicked
	 * Optionally accepts a flag to set the current selected asset
	 * @param setInitialAsset When true set the initial value for the selected asset
	 * */
	loadData(setInitialAsset = false): void {
		if (this.assetId) {
			this.architectureGraphService
				.getArchitectureGraphData(this.assetId, this.levelsUp, this.levelsDown, this.mode)
				.subscribe( (res: any) => {
					this.currentNodesData = res;
					this.updateNodeData(this.currentNodesData, false);
					if (setInitialAsset && res && res.nodes) {
						const selectedAsset = res.nodes.find((item: any) => item.id === res.assetId);
						if (selectedAsset) {
							this.asset = {
								id: selectedAsset.id,
								text: selectedAsset.name,
							};
						}
					}
				});
		}
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
			this.form.controls.levelsUp.markAsDirty();
			this.loadData();
		}
	}

	/**
	 * Adds the levels up of the graph and reload the data
	 */
	addLevelsUp() {
		this.levelsUp++;
		this.form.controls.levelsUp.markAsDirty();
		this.loadData();
	}

	/**
	 * Reduces the levels down of the graph and reload the data
	 */
	addLevelsDown() {
		this.levelsDown++;
		this.form.controls.levelsDown.markAsDirty();
		this.loadData();
	}

	/**
	 * Adds the levels down of the graph and reload the data
	 */
	extractLevelsDown() {
		if (this.levelsDown > 0) {
			this.levelsDown--;
			this.form.controls.levelsDown.markAsDirty();
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
		if (this.assetId) {
			this.updateNodeData(this.currentNodesData, false);
		}
	}

	/**
	 * Iterate over the current selected categories, in case the category is not selected
	 * remove the node names for that categories
	 * @param data graph data and configuration
	 * @returns the clone of graph data modified
	 */
	removeNodeNamesForNotSelectedCategories(data: any): any {
		let clonedNodes = JSON.parse(JSON.stringify(data));

		const categories = this.getSelectedCategories() // this.categories || [];
					.map( label => label.value.toUpperCase());

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
			.filter( label => label.checked);
	}

	/**
	 * 	Sends new data to the graph when updating labels
	 *  @param data graph data and configuration
	 *  @param iconsOnly if show the labels or not
	 */
	updateNodeData(data, iconsOnly) {
		const clonedData = this.removeNodeNamesForNotSelectedCategories(data);
		const diagramHelper = new ArchitectureGraphDiagramHelper();

		this.data$.next(diagramHelper.diagramData({
			rootAsset: this.assetId,
			currentUserId: 1,
			data: clonedData,
			iconsOnly: iconsOnly,
			extras: {
				initialAutoScale: Diagram.UniformToFill,
				allowZoom: true
			}
		}));
	}

	/**
	 * Generate the graph with the current data
	 */
	regenerateGraph() {
		this.loadData();
		this.graph.showFullGraphBtn = false;
		this.graph.nodeMove = false;
	}

	/**
	 * Save the preferences changed on the control panel
	*/
	savePreferences() {
		const valueData = {
			assetClass: this.assetClass,
			assetName: 'text1',
			levelsUp: this.levelsUp,
			levelsDown: this.levelsDown,
			showCycles: this.showCycles,
			selectedAsset: this.selectedAsset,
			// appLbl: this.categories.length > 0
			// assetClass: this.assetId || 'ALL',
		};

		const selectedLabels = this.getSelectedCategories()
			.map((item: any) => {
				valueData[item.tagLabel] = 'true';
			});

		this.preferenceService
			.setPreference('ARCH_GRAPH', JSON.stringify({...valueData, ...selectedLabels}))
			.subscribe( res => {
				this.resetForm();
				if (res.status === 'success') {
					console.log('Preferences saved correctly');
				}
			})
	}

	/**
	 * Reset the form controls int the pristine state
	 */
	private resetForm() {
		for (let name in this.form.controls) {
			if (name) {
				this.form.controls[name].markAsPristine();
			}
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
