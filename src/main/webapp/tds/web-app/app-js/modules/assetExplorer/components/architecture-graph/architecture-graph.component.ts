import {
	Component,
	ComponentFactoryResolver,
	HostListener,
	OnInit,
	ViewChild
} from '@angular/core';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {Observable, ReplaySubject} from 'rxjs';
import {tap} from 'rxjs/operators';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {Diagram, Layout, Link, Node} from 'gojs';
import {UserContextService} from '../../../auth/service/user-context.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {ActivatedRoute} from '@angular/router';
import {IArchitectureGraphParams} from '../../model/url-params.model';
import {ArchitectureGraphDiagramHelper} from './architecture-graph-diagram-helper';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {DomSanitizer} from '@angular/platform-browser';
import {
	ComboBoxSearchResultModel
} from '../../../../shared/components/combo-box/model/combobox-search-result.model';
import {ASSET_ICONS} from '../../model/asset-icon.constant';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NgForm} from '@angular/forms';
import {PermissionService} from '../../../../shared/services/permission.service';
import {AssetShowComponent} from '../asset/asset-show.component';
import {AssetExplorerModule} from '../../asset-explorer.module';
import {DialogService, ModalSize} from 'tds-component-library';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetActionEvent} from '../../model/asset-action-event.constant';
import {TDSComboBoxComponent} from '../../../../shared/components/combo-box/combobox.component';
import {AssetEditComponent} from '../asset/asset-edit.component';

declare var jQuery: any;

@Component({
	selector: 'tds-architecture-graph',
	templateUrl: './architecture-graph.component.html'
})
export class ArchitectureGraphComponent implements OnInit {
	// References
	@ViewChild('graph', {static: false}) graph: any;
	@ViewChild('archGraphForm', { static: false }) protected form: NgForm;
	@ViewChild('assetComboBox', { static: false }) assetComboBox: TDSComboBoxComponent;

	private comboBoxSearchModel: ComboBoxSearchModel;
	private comboBoxSearchResultModel: ComboBoxSearchResultModel;
	public datasource: any[] = [{id: '', text: ''}];
	public showControlPanel = true;
	public data$: ReplaySubject<IDiagramData> = new ReplaySubject(1);
	public ctxOpts:  ITdsContextMenuOption;
	public diagramLayout$: ReplaySubject<Layout> = new ReplaySubject(1);
	public linkTemplate$: ReplaySubject<Link> = new ReplaySubject(1);
	public userContext: UserContextModel;
	public urlParams: IArchitectureGraphParams;
	private currentNodesData;
	public categories;
	private readonly initialAssetClass = {id: 'ALL', value: 'All Classes'};
	public assetClass: any;
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
	public taskCycles: number[][];

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
			value: 'application',
			tagLabel: this.TAG_APPLICATION,
			checked: false
		},
		{
			icon: 'database',
			label: 'Database',
			value: 'database',
			tagLabel: this.TAG_DATABASE,
			checked: false
		},
		{
			icon: 'serverPhysical',
			label: 'Physical Server',
			value: 'serverPhysical',
			tagLabel: this.TAG_PHYSICAL_SERVER,
			checked: false
		},
		{
			icon: 'serverVirtual',
			label: 'Virtual Server',
			value: 'serverVirtual',
			tagLabel: this.TAG_VIRTUAL_SERVER,
			checked: false
		},
		{
			icon: 'storageLogical',
			label: 'Logical Storage',
			value: 'storage',
			tagLabel: this.TAG_STORAGE_LOGICAL,
			checked: false
		},
		{
			icon: 'storagePhysical',
			label: 'Storage Device',
			value: 'storagePhysical',
			tagLabel: this.TAG_STORAGE_DEVICE,
			checked: false
		},
		{
			icon: 'networkLogical',
			label: 'Network Device',
			value: 'networkLogical',
			tagLabel: this.TAG_NETWORK_LOGICAl,
			checked: false
		},
		{
			icon: 'other',
			label: 'Other Device',
			value: 'other',
			tagLabel: this.TAG_OTHER_DEVICES,
			checked: false
		}
	];
	unsubscribe$: ReplaySubject<void> = new ReplaySubject(1);

	constructor(
		private userContextService: UserContextService,
		private activatedRoute: ActivatedRoute,
		private architectureGraphService: ArchitectureGraphService,
		private assetExplorerService: AssetExplorerService,
		private sanitized: DomSanitizer,
		private preferenceService: PreferenceService,
		private dialogService: DialogService,
		private notifierService: NotifierService,
		private permissionService: PermissionService,
		private componentFactoryResolver: ComponentFactoryResolver
	) {
		this.activatedRoute.queryParams.subscribe((data: IArchitectureGraphParams) => this.urlParams = data);
		this.getAssetList = this.listAssets.bind(this);
		this.userContextService.getUserContext().subscribe(res => this.userContext = res);
		this.assetClass = this.initialAssetClass;
	}

	ngOnInit(): void {
		this.showCycles = false;
		let assetId = null;
		let levelsUp = null;
		let levelsDown = null;

		// If it comes from asset explorer
		if (this.urlParams && this.urlParams.assetId) {
			this.assetId = parseInt(this.urlParams.assetId.toString(), 10);
			assetId = this.assetId;
			levelsUp = this.urlParams.levelsUp;
			levelsDown = this.urlParams.levelsDown;
			this.refreshData(true);
		}
		this.getArchitectureGraphPreferences(assetId, levelsUp, levelsDown);
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
	getArchitectureGraphPreferences(assetId = null, levelsUp = null, levelsDown = null) {
		this.architectureGraphService
			.getArchitectureGraphPreferences()
			.subscribe((res: any) => {
				this.dataForSelect = res.assetClassesForSelect;

				this.levelsUp = levelsUp === null ? this.levelsUp = +res.graphPrefs.levelsUp  : levelsUp;
				this.levelsDown = levelsDown === null ? this.levelsDown = +res.graphPrefs.levelsDown  : levelsDown;

				this.showCycles = res.graphPrefs.showCycles;
				this.appLbl = res.graphPrefs.appLbl;
				this.labelOffset = res.graphPrefs.labelOffset;
				this.assetClasses = res.graphPrefs.assetClasses;
				if (assetId === null) {
					this.assetClass = res.graphPrefs.assetClass || this.initialAssetClass;
					this.selectedAsset = res.graphPrefs.selectedAsset;
					this.assetId = this.selectedAsset && this.selectedAsset.id || '';
				} else {
					this.assetClass = this.initialAssetClass;
				}
				this.refreshData(false);

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
	 *  When asset is cleared out reset the current assets selected
	 */
	onAssetSelected(event) {
		if (event) {
			this.assetId = event.id;
			this.selectedAsset = event;
			// this.asset = event;
			this.refreshData(false);
		} else {
			// reset assets selected
			this.assetId = null;
			this.selectedAsset = {id: '', text: ''};
			// this.asset = this.selectedAsset;
		}
		this.form.controls['assetClass'].markAsDirty();
	}

	/**
	 * Loads the asset data once an item has been clicked
	 * Optionally accepts a flag to set the current selected asset
	 * @param setInitialAsset When true set the initial value for the selected asset
	 * */
	loadData(setInitialAsset = false): Observable<any> {
		if (this.assetId) {
			return this.architectureGraphService
				.getArchitectureGraphData(this.assetId, this.levelsUp, this.levelsDown, this.mode, this.showCycles)
				.pipe(
					tap((res: any) => {
						this.currentNodesData = res;
						this.taskCycles = res.cycles || [];
						const noLabelChecked = !this.graphLabels.filter(l => l.checked) || this.graphLabels.filter(l => l.checked).length < 1;
						this.updateNodeData(this.currentNodesData, noLabelChecked);
						if (setInitialAsset && res && res.nodes) {
							const selectedAsset = res.nodes.find((item: any) => item.id === res.assetId);
							if (selectedAsset) {
								this.selectedAsset = {
									id: selectedAsset.id,
									text: selectedAsset.name,
								};
							}
						}
					})
				)
		} else {
			return Observable.of(null);
		}
	}

	/**
	 * highlight nodes by cycles
	 **/
	highlightCycles(): void {
		if ((this.taskCycles && this.taskCycles.length > 0) && this.showCycles) {
			const cycles = [];
			this.taskCycles.forEach(arr => cycles.push(...arr));
			this.graph.highlightNodes((n: Node) => cycles && cycles.includes(n.data.id), false);
		} else {
			this.graph.clearHighlights();
		}
	}

	/**
	 * Update the info changing the state for the show cycles checkbox
	 */
	onToggleShowCycles() {
		this.refreshData();
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
			this.refreshData();
		}
	}

	/**
	 * Refresh the data getting the information from the endpoint
	 * * @param setInitialAsset Allow set passing the initial asset to standout
	 */
	refreshData(setInitialAsset = false): void {
		this.loadData(setInitialAsset)
			.subscribe()
	}

	/**
	 * Adds the levels up of the graph and reload the data
	 */
	addLevelsUp() {
		if (this.levelsUp < 10) {
			this.levelsUp++;
			this.form.controls.levelsUp.markAsDirty();
			this.refreshData();
		}
	}

	/**
	 * Reduces the levels down of the graph and reload the data
	 */
	addLevelsDown() {
		if (this.levelsDown < 10) {
			this.levelsDown++;
			this.form.controls.levelsDown.markAsDirty();
			this.refreshData();
		}
	}

	/**
	 * Adds the levels down of the graph and reload the data
	 */
	extractLevelsDown() {
		if (this.levelsDown > 0) {
			this.levelsDown--;
			this.form.controls.levelsDown.markAsDirty();
			this.refreshData();
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
	 * @param checked value
	 * @param index of the selected checkbox
	 */
	updateGraphLabels(checked: boolean, index: any) {
		const noLabelChecked = !this.graphLabels.filter(l => l.checked) || this.graphLabels.filter(l => l.checked).length < 1;
		if (this.assetId) {
			this.updateNodeData(this.currentNodesData, noLabelChecked);
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

		const nodes = clonedNodes.nodes || [];
		nodes.forEach(node => {
			// Clear the node name if the assetClass is not included on the categories selected
			if (node.assetClass === 'DEVICE' /*|| node.assetClass === 'STORAGE'*/) {
				const type = node.type.toLowerCase();
				if (!categories.includes('SERVERVIRTUAL') &&
					ArchitectureGraphDiagramHelper.isDeviceVirtualServer(type)) {
					node.name = '';
				} else if (!categories.includes('STORAGEPHYSICAL') &&
					ArchitectureGraphDiagramHelper.isDeviceStorage(type)) {
					node.name = '';
				} else if (!categories.includes('NETWORKLOGICAL') &&
					ArchitectureGraphDiagramHelper.isDeviceNetwork(type)) {
					node.name = '';
				} else if (!categories.includes('SERVERPHYSICAL') &&
					ArchitectureGraphDiagramHelper.isDeviceServer(type)) {
					node.name = '';
				} else if (!categories.includes('OTHER') && type === 'other') {
					node.name = '';
				} else {
					console.log('Not found');
				}
			} else {
				if (!categories.includes(node.assetClass)) {
					node.name = '';
				}
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
		const diagramHelper = new ArchitectureGraphDiagramHelper(
			this.permissionService,
			{
				currentUser: this.userContext && this.userContext.person,
				taskCount: this.currentNodesData && this.currentNodesData.length,
				cycles: this.taskCycles || []
			});

		this.data$.next(diagramHelper.diagramData({
			rootAsset: this.assetId,
			currentUserId: (this.userContext && this.userContext.person) && this.userContext.person.id,
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
		this.refreshData();
		this.graph.showFullGraphBtn = false;
		this.graph.nodeMove = false;
	}

	/**
	 * Save the preferences changed on the control panel
	*/
	savePreferences() {
		const valueData = {
			assetClass: this.assetClass,
			levelsUp: this.levelsUp,
			levelsDown: this.levelsDown,
			showCycles: this.showCycles,
		};

		if (this.selectedAsset && this.selectedAsset.id) {
			valueData['selectedAsset'] = this.selectedAsset;
		}

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

	/**
	 * Subscribe to events with the notifierService
	 */
	onActionDispatched(data: any): void {
		switch (data.name) {
			case AssetActionEvent.EDIT:
				this.onAssetEdit({name: data.name, asset: data.node});
				break;
			case AssetActionEvent.VIEW:
				this.onAssetView({name: data.name, asset: data.node});
				break;
			case AssetActionEvent.GRAPH:
				this.onGraph(data.node);
				break;
		}
	}

	/**
	 * Show the asset edit modal.
	 * @param data: any
	 */
	onAssetEdit(data: any): void {
		const asset = data.asset;
		if (asset  && asset.id) {
			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: AssetEditComponent,
				data: {
					assetId: asset.id,
					assetClass: asset.assetClass,
					onCloseOpenDetailView: false,
					assetExplorerModule: AssetExplorerModule
				},
				modalConfiguration: {
					title: 'Asset',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-asset-modal-dialog'
				}
			}).subscribe(update => {
				if (update && update.data) {
					this.refreshData();
				}
			});
		}
	}

	/**
	 * Show the asset detail modal.
	 * @param data: any
	 */
	onAssetView(data: any): void {
		const asset = data.asset;
		if (asset  && asset.id) {
			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: AssetShowComponent,
				data: {
					assetId: asset.id,
					assetClass: asset.assetClass,
					assetExplorerModule: AssetExplorerModule
				},
				modalConfiguration: {
					title: 'Asset',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-asset-modal-dialog'
				}
			}).subscribe(update => {
				if (update && update.data) {
					this.refreshData()
				}
			});
		}
	}

	/**
	 * Show the asset popup detail.
	 * @param data: ITaskEvent
	 */
	onGraph(data: any): void {
		const asset = data;
		const assetDataSource = this.assetComboBox && this.assetComboBox.datasource;
		if (assetDataSource.length > 1 && assetDataSource.includes(a => a.id === asset.id)) {
			this.assetId = asset.id;
			this.selectedAsset = asset;
			this.refreshData();
			this.form.controls['assetClass'].markAsDirty();
		} else {
			this.assetId = asset.id;
			this.refreshData(true);
			this.form.controls['assetClass'].markAsDirty();
		}
	}

	/**
	 * Asset node double click handler
	 * @param asset
	 */
	onNodeDoubleClick(asset: any): void {
		this.onGraph(asset);
	}

	@HostListener('window:beforeunload', ['$event'])
	ngOnDestroy(): void {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}

}
