import {TaskStatus} from '../../../../modules/taskManager/model/task-edit-create.model';
import {IGraphTask} from '../../../../modules/taskManager/model/graph-task.model';

export class TaskValidationHelper {

	/**
	 * Validate if this task status equals the button status
	 **/
	static hasStatus(status: string, data: IGraphTask): boolean {
		if (!status) { return false; }
		if (status === 'neighborhood' && data.successors) {
			return data.successors.length < 1;
		}
		return data.status.toLowerCase() === TaskStatus.COMPLETED
			|| data.status.toLowerCase() === status.toLowerCase();
	}

	/**
	 * Validate if reset button should appear
	 **/
	static shouldDisplayResetButton(data: IGraphTask): boolean {
		return (data.status && data.status.toLowerCase() === TaskStatus.HOLD.toLowerCase() && data.isAutomatic);
	}

	/**
	 * Validate if Invoke button should appear
	 **/
	static shouldDisplayInvokeButton(data: IGraphTask): boolean {
		return (data.status && data.status.toLowerCase() === TaskStatus.STARTED.toLowerCase() && data.isAutomatic);
	}

	/**
	 * Validate if Neighborhood button should appear
	 **/
	static shouldDisplayNeighborhoodButton(data: IGraphTask): boolean {
		return data.successors.length > 0 || data.predecessors.length > 0;
	}

	/**
	 * Validate if AssetDetails button should appear
	 **/
	static shouldDisplayAssetDetailsButton(data: IGraphTask): boolean {
		return data.assetEntity && !!data.assetEntity.id;
	}

	/**
	 * Validate if AssignToMe button should appear
	 **/
	static shouldDisplayAssignToMeButton(data: IGraphTask, currentUser: string): boolean {
		return data.assignedTo !== currentUser;
	}
}
