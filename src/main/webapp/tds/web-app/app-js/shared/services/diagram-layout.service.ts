import {Injectable} from '@angular/core';
import {IGraphTask} from '../../modules/taskManager/model/graph-task.model';
import {ILinkPath} from '../components/diagram-layout/model/diagram-layout.model';

type FullGraphCache = { data: IGraphTask[], linksPath: ILinkPath[] };

@Injectable({
	providedIn: 'root'
})
export class DiagramLayoutService {
	private fullGraph: FullGraphCache;

	setFullGraphCache(data: FullGraphCache): void {
		this.fullGraph = Object.assign({}, data);
	}

	getFullGraphCache(): FullGraphCache {
		return Object.assign({}, this.fullGraph);
	}

	clearFullGraphCache(): void {
		this.fullGraph = null;
	}
}
