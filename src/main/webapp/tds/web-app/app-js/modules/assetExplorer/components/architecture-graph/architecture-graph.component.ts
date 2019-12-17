import {Component, OnInit} from '@angular/core';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
// import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {ReplaySubject} from 'rxjs';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {ArchitectureGraphDiagramHelper} from './architecture-graph-diagram-helper';
import {Diagram, Layout, Link} from 'gojs';
import {UserContextService} from '../../../auth/service/user-context.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {ActivatedRoute} from '@angular/router';
import {IArchitectureGraphParams} from '../../model/url-params.model';

import {ArchitectureGraphService} from './service/architecture-graph.service';

@Component({
	selector: 'tds-architecture-graph',
	templateUrl: './architecture-graph.component.html'
})
export class ArchitectureGraphComponent implements OnInit {
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
	public levelsUp;
	public levelsDown;
	public showCycles;
	public appLbl;
	public labelOffset;
	public assetClasses;


	constructor(
		// private architectureGraphService: ArchitectureGraphService,
		private userContextService: UserContextService,
		private activatedRoute: ActivatedRoute,
		private architectureGraphService: ArchitectureGraphService
	) {
		this.activatedRoute.queryParams.subscribe((data: IArchitectureGraphParams) => this.urlParams = data);
		// this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		if (this.urlParams && this.urlParams.assetId) {
			console.log('params', this.urlParams);
			// this.loadDiagramData(this.urlParams)
		}

		this.architectureGraphService.getArchitectureGraphPreferences().subscribe( (res: any) => {
			console.log('res', res);
			this.dataForSelect = Object.keys(res.assetClassesForSelect).map(function(key) {
				return [key, res.assetClassesForSelect[key]];
			});

			this.levelsUp = +res.graphPrefs.levelsUp;
			this.levelsDown = +res.graphPrefs.levelsDown;
			this.showCycles = res.graphPrefs.showCycles;
			this.appLbl = res.graphPrefs.appLbl;
			this.labelOffset = res.graphPrefs.labelOffset;
			this.assetClasses = res.graphPrefs.assetClasses;
			// this.dataForSelect2 = JSON.parse(res.assetClassesForSelect2);
			// let newJson = res.assetClassesForSelect2.replace(/([a-zA-Z0-9]+?):/g, '"$1":');
			// newJson = newJson.replace(/'/g, '');
			// newJson = newJson.replace(/"/g, '');
			// // newJson = newJson.replace('""Filter": All Classes"', '"Filter: All Classes"');
			// console.log(newJson);

			// this.dataForSelect2 = JSON.parse(newJson);
			console.log('this.dataForSelect', this.dataForSelect);
			// this.graphPreferences = res;
		});

	}

	loadDiagramData(params?: IArchitectureGraphParams): void {
	// 	this.architectureGraphService.getAssetDetails(params.assetId, params.levelsUp, params.levelsDown)
	// 		.subscribe(res => {
	// 			const diagramHelper = new ArchitectureGraphDiagramHelper();
	// 			this.data$.next(diagramHelper.diagramData({
	// 				rootAsset: params.assetId,
	// 				currentUserId: this.userContext.user.id,
	// 				data: res,
	// 				extras: {
	// 					diagramOpts: {
	// 						allowZoom: true
	// 					},
	// 					isExpandable: true,
	// 					initialExpandLevels: 2
	// 				}
	// 			}));
	// 		});
	// }
	}
}
