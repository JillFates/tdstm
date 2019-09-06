/**
 * Represents the model response properties that task.service -> getTaskActionInfo() response contains.
 * Specifically handles the api GET call response from /ws/task/getInfoForActionBar/{taskId}
 */
export interface TaskActionInfoModel {
	predecessors: number,
	successors: number,
	assignedTo: string,
	apiActionId: number,
	apiActionCompletedAt: any,
	apiActionInvokedAt: any,
	category: string,
	invokeButton?: {
		disabled: boolean,
		tooltipText: string,
		label: string
	}
}
