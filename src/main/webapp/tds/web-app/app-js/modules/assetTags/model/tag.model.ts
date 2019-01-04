const TAG_DEFAULT_COLOR = 'White';

export class TagModel {
	id: number;
	name: string;
	description: string;
	color: string;
	css: string;
	dateCreated: Date;
	lastModified?: Date;
	assets?: number;
	dependencies?: number;
	tasks?: number;

	constructor() {
		this.color = TAG_DEFAULT_COLOR;
	}
}