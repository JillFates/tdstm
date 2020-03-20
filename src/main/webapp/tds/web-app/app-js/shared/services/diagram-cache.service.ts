import {Injectable} from '@angular/core';
import {IGraphTask, ILinkPath} from '../../modules/taskManager/model/graph-task.model';

type FullGraphCache = { requestId: number, isMoveEvent: boolean, data: IGraphTask[], linksPath: ILinkPath[], extras?: any };

@Injectable({
	providedIn: 'root'
})
export class DiagramCacheService {
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
