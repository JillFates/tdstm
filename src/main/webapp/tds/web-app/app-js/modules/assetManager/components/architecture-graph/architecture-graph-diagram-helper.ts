import {Layout, Link, Node} from 'gojs';
import {
	ITdsContextMenuOption
} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {IconModel, IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {IGraphTask} from '../../../taskManager/model/graph-task.model';
import {ILinkPath} from '../../../taskManager/components/neighborhood/neighborhood.component';

export class ArchitectureGraphDiagramHelper {

	/**
	 * generate model to be used by diagram with task specific data
	 **/
	static data(data?: any): IDiagramData {
		const nodeDataArr = [];
		const linksPath = [];

		const dataCopy = data.slice();

		dataCopy.map((t: IGraphTask) => {
			t.key = t.taskNumber;
			nodeDataArr.push(t);
		});
		dataCopy
			.forEach((task: IGraphTask) => linksPath.push(...this.getLinksPath(task)));
		// console.log('tasks found', this.tasks.length, 'tasks dependencies', linksPath);
		return {
			nodeDataArray: nodeDataArr,
			linkDataArray: linksPath,
			ctxMenuOptions: null,
			nodeTemplate: null,
			linkTemplate: null,
			currentUserId: null
		}
	}

	/**
	 * Load events to fill events dropdown
	 **/
	static getLinksPath(asset: any): ILinkPath[] {
		const t = Object.assign({}, asset);
		if (t.successors) {
			return t.successors.map(dep => ({
				from: t.taskNumber,
				to: dep
			}));
		}
		return [];
	}

	static currentUser(): Node {
		return null;
	}

	static nodeTemplate(): Node {
		return null;
	}

	static linkTemplate(): Link {
		return null;
	}

	static layout(): Layout {
		return null;
	}

	static lowScaleNodeTemplate(): Node {
		return null;
	}

	static mediumScaleNodeTemplate(): Node {
		return null;
	}

	static contextMenuOptions(): ITdsContextMenuOption {
		return null;
	}

	static icons(): IconModel {
		return null;
	}

	static ctxMenuActionDispatched(action: string): void {
		// TODO
	}

	static nodeClicked(): void {
		// TODO
	}

	static animationFinished(): void {
		// TODO
	}

}
