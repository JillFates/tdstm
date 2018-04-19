/**
 * Model to use when using the ComboBox Search Model
 */
export class ComboBoxSearchModel {
	public query?: string;
	public value?: string;
	public maxPage?: number;
	public currentPage?: number;
	// To Pass any extra param required
	public metaParam?: any;
}