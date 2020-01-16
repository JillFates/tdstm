/**
 * Model to use when using the ComboBox Search Result Model
 */
export class ComboBoxSearchResultModel {
	public total?: number;
	public result?: any[];
	public page?: number;
	public results?: any[];
}

export const RESULT_PER_PAGE = 24;