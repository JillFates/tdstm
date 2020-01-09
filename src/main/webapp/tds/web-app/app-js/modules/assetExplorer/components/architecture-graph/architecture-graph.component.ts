import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {ReplaySubject} from 'rxjs';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {ArchitectureGraphDiagramHelper} from './architecture-graph-diagram-helper';
import {Diagram, Layout, Link} from 'gojs';
import {UserContextService} from '../../../auth/service/user-context.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {ActivatedRoute} from '@angular/router';
import {IArchitectureGraphParams} from '../../model/url-params.model';
import {AssetCommonDiagramHelper} from '../asset/asset-common-diagram.helper';

// import {ArchitectureGraphService} from './service/architecture-graph.service';

@Component({
	selector: 'tds-architecture-graph',
	templateUrl: './architecture-graph.component.html'
})
export class ArchitectureGraphComponent implements OnInit {
	public showControlPanel = true;

	data$: ReplaySubject<IDiagramData> = new ReplaySubject(1);
	ctxOpts:  ITdsContextMenuOption;
	diagramLayout$: ReplaySubject<Layout> = new ReplaySubject(1);
	linkTemplate$: ReplaySubject<Link> = new ReplaySubject(1);
	userContext: UserContextModel;
	urlParams: IArchitectureGraphParams;

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

	constructor(
		// private architectureGraphService: ArchitectureGraphService,
		private userContextService: UserContextService,
		private activatedRoute: ActivatedRoute,
		private architectureGraphService: ArchitectureGraphService
	) {
		this.activatedRoute.queryParams.subscribe((data: IArchitectureGraphParams) => this.urlParams = data);
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		if (this.urlParams && this.urlParams.assetId) {
			console.log('params', this.urlParams);
			this.loadDiagramData(this.urlParams);
		} else {
			this.architectureGraphService.getArchitectureGraphPreferences().subscribe( (res: any) => {
				console.log('res', res);
				this.dataForSelect = res.assetClassesForSelect;
				this.dataForSelect2 = res.assetClassesForSelect2;

				this.assetId = res.assetId || 144762;
				this.levelsUp = +res.graphPrefs.levelsUp;
				this.levelsDown = +res.graphPrefs.levelsDown;
				this.showCycles = res.graphPrefs.showCycles;
				this.appLbl = res.graphPrefs.appLbl;
				this.labelOffset = res.graphPrefs.labelOffset;
				this.assetClasses = res.graphPrefs.assetClasses;
			});
		}
	}

	onAssetSelected(event) {
		console.log('asset selected', event);
		this.architectureGraphService.getArchitectureGraphData(this.assetId, this.levelsUp, this.levelsDown, this.mode)
			.subscribe( (res: any) => {
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
						isExpandable: false
					}
				}));
				// this.dataForGraph = res;
				// console.log('graph data', res);
				// const diagramHelper = new ArchitectureGraphDiagramHelper();
				// const n = diagramHelper.diagramData({
				// 	rootAsset: this.assetId,
				// 	currentUserId: this.userContext.user.id,
				// 	data: res,
				// 	extras: {
				// 		diagramOpts: {
				// 			allowZoom: true
				// 		},
				// 		isExpandable: true,
				// 		initialExpandLevel: 2
				// 	}
				// });
				// this.data$.next(n);
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
		}
	}

	addLevelsUp() {
		this.levelsUp++;
	}

	addLevelsDown() {
		this.levelsDown++;
	}

	extractLevelsDown() {
		if (this.levelsDown > 0) {
			this.levelsDown--;
		}
	}

}
