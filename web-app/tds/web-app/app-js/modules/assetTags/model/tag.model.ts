const TAG_DEFAULT_COLOR = 'White';

export class TagModel {
	id: number;
	name: string;
	description: string;
	assets: number;
	dependencies: number;
	tasks: number;
	color: string;
	css: string;
	dateCreated: Date;
	lastModified: Date;

	constructor() {
		this.color = TAG_DEFAULT_COLOR;
	}
}