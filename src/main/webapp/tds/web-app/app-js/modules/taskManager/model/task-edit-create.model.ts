export const YesNoList = ['Yes', 'No'];
export const PriorityList = [1, 2, 3, 4, 5];

export interface ITask {
	id: string | number;
	originalId: string | number;
	taskId: string | number;
	taskNumber: string | number ,
	category: string;
	status: string;
	desc: string;
	model: {
		id: string | number;
		text: string;
	}
}

export const  TaskStatus = {
	HOLD: 	    'Hold',
	PLANNED:    'Planned',
	READY:      'Ready',
	PENDING:    'Pending',
	STARTED:    'Started',
	COMPLETED:  'Completed',
	TERMINATED: 'Terminated'
};
