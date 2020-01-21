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
import {AssetCommonDiagramHelper} from '../asset/asset-common-diagram.helper';
import {DiagramLayoutComponent} from '../../../../shared/components/diagram-layout/diagram-layout.component';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ComboBoxComponent} from '@progress/kendo-angular-dropdowns';
import {
	ComboBoxSearchResultModel,
	RESULT_PER_PAGE
} from '../../../../shared/components/combo-box/model/combobox-search-result.model';
import {ASSET_ICONS} from '../../model/asset-icon.constant';

declare var jQuery: any;

@Component({
	selector: 'tds-architecture-graph',
	templateUrl: './architecture-graph.component.html'
})
export class ArchitectureGraphComponent implements OnInit {
	// References
	@ViewChild('dropdownFooter', {static: false}) dropdownFooter: ElementRef;
	@ViewChild('innerComboBox', {static: false}) innerComboBox: ComboBoxComponent;
	@ViewChild('graph', {static: false}) graph: DiagramLayoutComponent;
	private comboBoxSearchModel: ComboBoxSearchModel;
	private comboBoxSearchResultModel: ComboBoxSearchResultModel;
	private searchOnScroll = true;
	private reloadOnOpen = false;
	private firstChange = true;
	public datasource: any[] = [{id: '', text: ''}];
	public showControlPanel = true;
	public data$: ReplaySubject<IDiagramData> = new ReplaySubject(1);
	public ctxOpts:  ITdsContextMenuOption;
	public diagramLayout$: ReplaySubject<Layout> = new ReplaySubject(1);
	public linkTemplate$: ReplaySubject<Link> = new ReplaySubject(1);
	public userContext: UserContextModel;
	public urlParams: IArchitectureGraphParams;

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
			value: 'application',
			checked: false
		},
		{
			icon: 'database',
			label: 'Database',
			value: 'database',
			checked: false
		},
		{
			icon: 'server',
			label: 'Physical Server',
			value: 'physical_server',
			checked: false
		},
		{
			icon: 'virtualServer',
			label: 'Virtual Server',
			value: 'virtual_server',
			checked: false
		},
		{
			icon: 'logicalStorage',
			label: 'Logical Storage',
			value: 'Logical Storage',
			checked: false
		},
		{
			icon: 'storage',
			label: 'Storage Device',
			value: 'Storage Device',
			checked: false
		},
		{
			icon: 'device',
			label: 'Network Device',
			value: 'Network Device',
			checked: false
		},
		{
			icon: 'device',
			label: 'Other Device',
			value: 'Other Device',
			checked: false
		}
	];

	constructor(
		private userContextService: UserContextService,
		private activatedRoute: ActivatedRoute,
		private architectureGraphService: ArchitectureGraphService,
		private sanitized: DomSanitizer
	) {
		this.activatedRoute.queryParams.subscribe((data: IArchitectureGraphParams) => this.urlParams = data);
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		// If it comes from asset explorer
		if (this.urlParams && this.urlParams.assetId) {
			console.log('params', this.urlParams);
			this.loadDiagramData(this.urlParams);
		}
		this.getArchitectureGraphPreferences();
		this.initSearchModel();
		this.loadAssetsForDropDown();
	}

	getArchitectureGraphPreferences() {
		this.architectureGraphService.getArchitectureGraphPreferences().subscribe( (res: any) => {
			console.log('res call 2', res);
			this.dataForSelect = res.assetClassesForSelect;
			this.assetId = res.assetId;
			this.levelsUp = +res.graphPrefs.levelsUp;
			this.levelsDown = +res.graphPrefs.levelsDown;
			this.showCycles = res.graphPrefs.showCycles;
			this.appLbl = res.graphPrefs.appLbl;
			this.labelOffset = res.graphPrefs.labelOffset;
			this.assetClasses = res.graphPrefs.assetClasses;
		});
	}

	loadAssetsForDropDown() {
		this.architectureGraphService.getAssetsForArchitectureGraph(this.comboBoxSearchModel.query, this.comboBoxSearchModel.value, this.comboBoxSearchModel.maxPage, this.comboBoxSearchModel.currentPage, 'ALL').subscribe((res: any) => {
			console.log('data for asset select', res);
			this.comboBoxSearchResultModel = res;
			const result = (this.comboBoxSearchResultModel.results || []);
			result.forEach((item: any) => this.addToDataSource(item));
			if (this.searchOnScroll && this.comboBoxSearchResultModel.total > RESULT_PER_PAGE) {
				this.calculateLastElementShow();
			}
		});
	}

	onAssetSelected(event) {
		console.log('asset selected', event);
		if (event) {
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
				console.log('response from arch data:', res);
				const diagramHelper = new AssetCommonDiagramHelper();
				this.data$.next(diagramHelper.diagramData({
					rootAsset: this.assetId,
					currentUserId: 1,
					data: res,
					iconsOnly: false,
					extras: {
						diagramOpts: {
							autoScale: Diagram.Uniform,
							allowZoom: false
						},
						isExpandable: false
					}
				}));
				this.graph.showFullGraphBtn = false;
			});
	}

	loadDiagramData(params?: IArchitectureGraphParams): void {
		this.architectureGraphService.getAssetDetails(params.assetId, this.levelsUp, this.levelsDown)
			.subscribe(res => {
				console.log('response assets details', res);
				const diagramHelper = new AssetCommonDiagramHelper();
				this.data$.next(diagramHelper.diagramData({
					rootAsset: this.assetId,
					currentUserId: 1,
					data: res,
					iconsOnly: true,
					extras: {
						diagramOpts: {
							autoScale: Diagram.Uniform,
							allowZoom: false
						},
						isExpandable: false,
						initialExpandLevel: 2
					}
				}));
			});
	}

	toggleControlPanel() {
		this.showControlPanel = !this.showControlPanel;
	}

	extractLevelsUp() {
		if (this.levelsUp > 0) {
			this.levelsUp--;
			this.loadData();
		}
	}

	addLevelsUp() {
		this.levelsUp++;
		this.loadData();
	}

	addLevelsDown() {
		this.levelsDown++;
		this.loadData();
	}

	extractLevelsDown() {
		if (this.levelsDown > 0) {
			this.levelsDown--;
			this.loadData();
		}
	}

	toggleShowCycles() {
		this.showCycles = !this.showCycles;
		console.log('this.showCycles', this.showCycles);
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

	zoomGraphOut() {
		this.graph.zoomOut();
	}

	zoomGraphIn() {
		this.graph.zoomIn();
	}

	/**
	 * Generate the item template for the task items to show in the format taskId : Description
	 * @event dataItem contains the task item information
	 * @returns {string}
	 */
	protected innerTemplateTaskItem(dataItem: any): string {
		return dataItem.text;
	}

	public onSelectionChange(value: any): void {
		console.log('selection changed: ', value);
		// this.selectionChangeonAssetSelected.emit(value);
	}

	/**
	 * Filter is being executed on Server and Client Side
	 * @param filter
	 */
	public onFilterChange(filter: any): void {
		console.log('on filter change', filter);
		// load more with search of terms for assets
		if (filter !== '') {
			this.initSearchModel();
			this.comboBoxSearchModel.currentPage = 1;
			this.comboBoxSearchModel.query = filter;
			this.getNewResultSet();
		} else if (!filter) {
			// reload initial data
			this.initSearchModel();
			this.loadAssetsForDropDown();
		}
	}

	/**
	 * Populate the Datasource with a new Complete Set
	 */
	private getNewResultSet(): void {
		this.architectureGraphService.getAssetsForArchitectureGraphWithSearch(this.comboBoxSearchModel.query).subscribe((res: ComboBoxSearchResultModel) => {
			this.comboBoxSearchResultModel = res;
			this.datasource = this.comboBoxSearchResultModel.results;
			if (this.searchOnScroll && this.comboBoxSearchResultModel.total > RESULT_PER_PAGE) {
				this.calculateLastElementShow();
			}
		});
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
				// this.getResultSet();
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
	 * On Open we emit the value if the parents needs to implements something
	 * but we call the resource on the Rest to get the list of values.
	 */
	public onOpen(): void {
		// At open the first time, we need to get the list of items to show based on the selected element
		if (this.reloadOnOpen || this.firstChange || !this.comboBoxSearchModel) {
			this.firstChange = false;
			this.datasource = [];
			this.initSearchModel();
			this.loadAssetsForDropDown();
		} else {
			this.calculateLastElementShow();
		}
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
		this.showLegend = !this.showLegend;
	}

	onAssetFilterChange(event) {
		console.log('asset type filter change', event);
		// do crazy logic here
	}

	updateGraphLabels(index) {
		this.graphLabels[index].checked = !this.graphLabels[index].checked;
		// TODO: filter nodes for removing labels on graph
		let categories = this.graphLabels.filter( label => label.checked).map( label => label.value.toUpperCase());
		console.log(categories);
		if (categories.length > 0) {
			this.graph.diagram.commit(d => d.nodes.filter(node => categories.includes(node.data.assetClass)).map(n => n.data.name = ''));
		} else {
			// TODO: add all labels back
			console.log('put back all labels');
		}
	}
}
