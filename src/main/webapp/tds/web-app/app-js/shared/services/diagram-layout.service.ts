import {Injectable} from '@angular/core';
import {IGraphTask} from '../../modules/taskManager/model/graph-task.model';
import {ILinkPath} from '../components/diagram-layout/model/legacy-diagram-layout.model';

type FullGraphCache = { requestId: number, isMoveEvent: boolean, data: IGraphTask[], linksPath: ILinkPath[] };

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

	getRequestId(): number {
		return this.fullGraph.requestId;
	}

	isCacheFromMoveEvent(): boolean {
		return this.fullGraph.isMoveEvent;
	}

	clearFullGraphCache(): void {
		this.fullGraph = null;
	}
}
