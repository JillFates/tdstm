import {Component, OnInit} from '@angular/core';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {ReplaySubject} from 'rxjs';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {ArchitectureGraphDiagramHelper} from './architecture-graph-diagram-helper';
import {Layout, Link} from 'gojs';
import {UserContextService} from '../../../auth/service/user-context.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {ActivatedRoute} from '@angular/router';
import {IArchitectureGraphParams} from '../../model/url-params.model';

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

	constructor(
		private architectureGraphService: ArchitectureGraphService,
		private userContextService: UserContextService,
		private activatedRoute: ActivatedRoute
	) {
		this.activatedRoute.queryParams.subscribe((data: IArchitectureGraphParams) => this.urlParams = data);
		this.userContextService.getUserContext().subscribe(res => this.userContext = res)
	}

	ngOnInit(): void {
		if (this.urlParams && this.urlParams.assetId) {
			this.loadDiagramData(this.urlParams)
		}
	}

	loadDiagramData(params?: IArchitectureGraphParams): void {
		this.architectureGraphService.getAssetDetails(params.assetId, params.levelsUp, params.levelsDown)
			.subscribe(res => {
				this.data$.next(ArchitectureGraphDiagramHelper.diagramData(params.assetId, this.userContext.user.id, res));
			});
	}
}
