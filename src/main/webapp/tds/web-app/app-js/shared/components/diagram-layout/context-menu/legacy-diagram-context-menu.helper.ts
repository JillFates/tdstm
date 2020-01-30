import {ContainerComp, IDiagramContextMenuField} from '../model/legacy-diagram-context-menu.model';
import {TaskValidationHelper} from '../../../../modules/taskManager/components/common/task-validation.helper';
import {IGraphTask, TASK_OPTION_LABEL} from '../../../../modules/taskManager/model/graph-task.model';

export class LegacyDiagramContextMenuHelper {

	constructor() { /* Constructor */ }

	static validate(containerComp: string, option: IDiagramContextMenuField, data: IGraphTask, currentUser: any): boolean {
		switch (containerComp) {
			case ContainerComp.NEIGHBORHOOD:
				return this.neighborhoodValidation(option, data, currentUser);
			case ContainerComp.ARCHITECTURE_GRAPH:
				return this.architectureGraphValidation(option, data, currentUser);
			default:
				break;
		}
	}

	static neighborhoodValidation(option: IDiagramContextMenuField, data: IGraphTask, currentUser: any): boolean {
		switch (option.label) {
			case TASK_OPTION_LABEL.RESET:
				return TaskValidationHelper.shouldDisplayResetButton(data);
			case TASK_OPTION_LABEL.INVOKE:
				return TaskValidationHelper.shouldDisplayInvokeButton(data);
			case TASK_OPTION_LABEL.NEIGHBORHOOD:
				return TaskValidationHelper.shouldDisplayNeighborhoodButton(data);
			case TASK_OPTION_LABEL.ASSET_DETAILS:
				return TaskValidationHelper.shouldDisplayAssetDetailsButton(data);
			case TASK_OPTION_LABEL.ASSIGN_TO_ME:
				return TaskValidationHelper.shouldDisplayAssignToMeButton(data, currentUser.username);
			default:
				return !TaskValidationHelper.hasStatus(option.status, data);
		}
	}

	static architectureGraphValidation(option: IDiagramContextMenuField, data: IGraphTask, currentUser: any): boolean {
		return false;
	}
}
