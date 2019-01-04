export class DialogModel {
	name: string;
	component: any;
	onClose?: void;
	params: Array<any>;
	lazyLoad: boolean;
}