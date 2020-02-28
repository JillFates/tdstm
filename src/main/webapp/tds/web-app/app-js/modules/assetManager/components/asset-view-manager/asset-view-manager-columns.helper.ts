import { GridColumnModel } from '../../../../shared/model/data-list-grid.model';

let columns: GridColumnModel[] = [];

/**
 * Add a column to the columns collection
 +
 * @param column Column to add
 * @returns void
 */
function addColumn(column: GridColumnModel) {
	columns.push(column);
}

/**
 * Handle View Manager columns
 */
export const AssetViewManagerColumnsHelper = {
	/**
	 * Create the columns used by the view manager
	 +
	 * @returns {GridColumnModel[]}
	 */
	createColumns: (dateFormat): GridColumnModel[] => {
		columns = [];
		addColumn({
			property: 'isFavorite',
			label: 'ASSET_EXPLORER.INDEX.FAVORITE',
			resizable: false,
			type: 'boolean',
			width: 120,
			cellClass: ['text-center']
		});
		addColumn({
			property: 'name',
			label: 'ASSET_EXPLORER.INDEX.NAME',
			resizable: true,
			type: 'text',
			width: 250,
			filterable: true,
			cellClass: ['link-text']
		});
		addColumn({
			property: 'createdBy',
			label: 'ASSET_EXPLORER.INDEX.CREATED_BY',
			resizable: true,
			type: 'text',
			filterable: true,
			width: 130
		});
		addColumn({
			property: 'createdOn',
			label: 'ASSET_EXPLORER.INDEX.CREATED_ON',
			resizable: true,
			type: 'date',
			filterable: true,
			format: dateFormat,
			width: 150
		});
		addColumn({
			property: 'updatedOn',
			label: 'ASSET_EXPLORER.INDEX.UPDATED_ON',
			resizable: true,
			type: 'date',
			filterable: true,
			format: dateFormat,
			width: 150
		});
		addColumn({
			property: 'isShared',
			label: 'ASSET_EXPLORER.INDEX.SHARED',
			resizable: false,
			type: 'boolean',
			width: 100,
			cellClass: ['text-center']
		});
		addColumn({
			property: 'isSystem',
			label: 'ASSET_EXPLORER.INDEX.SYSTEM',
			resizable: false,
			type: 'boolean',
			width: 110,
			cellClass: ['text-center']
		});
		return columns;
	},
	/**
	 * Set the property format to date type items only
	 +
	 * @param format  User preference DateFormat
	 * @returns {GridColumnModel[]}
	 */
	setFormatToDateColumns(format: string): GridColumnModel[] {
		columns = columns.map((column: GridColumnModel) => {
			return (column.type === 'date') ? ({ ...column, format }) : column;
		});
		return columns;
	}
};
