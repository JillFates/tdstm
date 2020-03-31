/**
 * Represents the model response properties that task.service -> getTaskActionInfo() response contains.
 * Specifically handles the api GET call response from /ws/task/getInfoForActionBar/{taskId}
 */
export interface TaskActionInfoModel {
	category: string,
	predecessors: number,
	assignedTo: string,
	assignedToName: string,
	apiActionId: number,
	apiActionCompletedAt: any,
	apiActionInvokedAt: any,
	status: string,
	successors: number,
	invokeButton?: {
		disabled: boolean,
		tooltipText: string,
		label: string
	},
	instructionsLinkURL: string,
	instructionsLinkLabel: string
}
