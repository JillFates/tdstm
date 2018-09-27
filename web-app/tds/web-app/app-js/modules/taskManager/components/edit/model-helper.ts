import {DateUtils} from "../../../../shared/utils/date.utils";

export const YesNoList = ['Yes', 'No'];

/**
 * Extract only the model fields used by the view
 */
export function extractModel(detail: any, userTimeZone: string): any {
	const assetComment = detail['assetComment'] || {};
	const durationScale = assetComment.durationScale && assetComment.durationScale.name || null;
	const [yes, no] = YesNoList;

	// add empty default value
	detail.categories = (detail.categories || []);
	detail.categories.push('');

	return  {
		id: assetComment.id,
		note: '',
		duration: assetComment.duration,
		taskSpec: assetComment.taskSpec,
		taskNumber: assetComment.taskNumber,
		hardAssigned: Boolean(assetComment.hardAssigned === 1) ? yes : no,
		sendNotification: Boolean(assetComment.sendNotification) ? yes : no,
		durationScale,
		durationParts: DateUtils.getDurationParts(assetComment.duration, durationScale),
		durationLocked: assetComment.durationLocked,
		actualStart: detail.atStart ? detail.atStart : '',
		actualFinish: detail.dtResolved ? detail.dtResolved : '',
		dueDate: assetComment.dueDate ? new Date(DateUtils.formatUserDateTime(userTimeZone, assetComment.dueDate)) : '',
		estimatedStartUTC: assetComment.estStart,
		estimatedFinishUTC: assetComment.estFinish,
		estimatedStart: assetComment.estStart ? new Date(DateUtils.formatUserDateTime(userTimeZone, assetComment.estStart)) : '',
		estimatedFinish: assetComment.estFinish ? new Date(DateUtils.formatUserDateTime(userTimeZone, assetComment.estFinish))  : '',
		instructionLink: detail.instructionLink === '|' ? '' : detail.instructionLink,
		priority: assetComment.priority,
		assetName: detail.assetName,
		comment:  assetComment.comment || '',
		assetClass: {id: detail.assetClass, text: ''},
		assetClasses: Object.keys(detail.assetClasses || {}).map((key: string) => ({id: key, text: detail.assetClasses[key]}) ),
		status: assetComment.status,
		statusList: [],
		personList: [],
		teamList: [],
		predecessorList: (detail.predecessorList || [])
			.map((item) => ({id: item.id, desc: `${item.taskNumber}: ${item.desc}`, model: {id: item.id, text: `${item.taskNumber}: ${item.desc}` }})),
		successorList: (detail.successorList || [])
			.map((item) => ({id: item.id, desc: `${item.taskNumber}: ${item.desc}`, model: {id: item.id, text: `${item.taskNumber}: ${item.desc}` }})),
		apiActionList: (detail.apiActionList || []).map((action) => ({id: action.id, text: action.name})),
		categoriesList: detail.categories.sort(),
		eventList: (detail.eventList || []).map((event) => ({id: event.id, text: event.name})),
		priorityList: [1, 2, 3, 4, 5],
		asset: {id: detail.assetId, text: detail.assetName},
		assignedTo: {id : (assetComment.assignedTo && assetComment.assignedTo.id) || null, text: detail.assignedTo},
		assignedTeam: {id: assetComment.role, text: detail.roles},
		event: {id: (assetComment.moveEvent && assetComment.moveEvent.id) || null, text: detail.eventName},
		category: assetComment.category,
		apiAction: {id: detail.apiAction && detail.apiAction.id || '', text: detail.apiAction && detail.apiAction.name || ''},
		assetComment
	}
}

export function getPayloadForUpdate(model): any {
	return  {
		assetClass: model.assetClass.id,
		assetEntity: model.asset.id,
		assetType: 'Application', /* ? */
		assignedTo: model.assignedTo.id,
		category: model.category,
		apiAction: model.apiAction.id,
		actionInvocable: '',
		actionMode: '',
		comment: model.comment,
		commentFromId: '',
		commentId: model.id,
		commentType: 'issue',
		deletePredId: '',  /* ? */
		dueDate: model.dueDate ? model.dueDate.toISOString() : '',
		duration: DateUtils.convertDurationPartsToMinutes(model.durationParts),
		durationScale: model.durationScale,
		estFinish: model.estimatedFinish ? model.estimatedFinish.toISOString() : '',
		estStart: model.estimatedStart ? model.estimatedStart.toISOString() : '',
		forWhom: '',
		hardAssigned: model.hardAssigned === 'No' ? 0 : 1,
		sendNotification: model.sendNotification === 'No' ? 0 : 1,
		isResolved: 0, /* ? */
		instructionsLink: model.instructionLink,
		manageDependency: 1, /* ? */
		moveEvent: model.event.id,
		mustVerify: 0, /* ? */
		override: 0, /* ? */
		predCount: -1, /* ? */
		predecessorCategory: '', /* ? */
		prevAsset: '', /* ? */
		priority: model.priority, /* ? */
		resolution: '', /* ? */
		role: model.assignedTeam.id,
		status: model.status,
		// taskDependency: '3002_233386', /* ? */
		// taskSuccessor: '3012_233398', /* ? */
		workflowTransition: '', /* ? */
		canEdit: true, /* ? */
		durationLocked: model.durationLocked,
		durationText: `${model.durationParts.days} days ${model.durationParts.hours} hrs ${model.durationParts.minutes} mins`,
		taskNumber: model.taskNumber,
		note: model.note,
		id: model.id,
		apiActionId: model.apiAction.id || 0,
		deletedPreds: ''
	};
}
