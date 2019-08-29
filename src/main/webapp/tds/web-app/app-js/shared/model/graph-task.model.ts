export interface IGraphTask {
	id?: number | string;
	key?: number | string;
	taskNumber?: number | string;
	label?: string;
	style?: string;
	color?: string;
	category?: string;
	fillcolor?: string;
	fontcolor?: string;
	fontsize?: string;
	status?: string;
	tooltip?: string;
	successors?: number[];
	name?: string;
	icon?: string;
}
