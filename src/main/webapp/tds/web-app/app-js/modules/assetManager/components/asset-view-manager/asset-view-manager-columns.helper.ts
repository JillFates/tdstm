import { GridColumnModel } from '../../../../shared/model/data-list-grid.model';

// PRIVATE -------------------------------------------

const DEFAULT_COLUMN_NAME = 'createdOn';
let columns: GridColumnModel[] = [];

/**
 * Given a name return the corresponding column searching by property
 +
 * @param name Property Name
 * @returns {GridColumnModel}
 */
function getColumnByName(name): GridColumnModel {
	return columns.find((column: GridColumnModel) => column.property === name);
}

/**
 * Add a column to the columns collection
 +
 * @param column Column to add
 * @returns void
 */
function addColumn(column: GridColumnModel) {
	columns.push(column);
}

// PUBLIC -------------------------------------------

/**
 * Handle View Manager columns
 */
export const AssetViewManagerColumnsHelper = {
	/**
	 * Create the columns used by the view manager
	 +
	 * @returns {GridColumnModel[]}
	 */
	createColumns: (): GridColumnModel[] => {
		columns = [];

		addColumn({ property: 'isFavorite', label: 'ASSET_EXPLORER.INDEX.FAVORITE', sort: { isSorting: false, isAscending: false  }, type: 'boolean', width: 100 });
		addColumn({ property: 'name', label: 'ASSET_EXPLORER.INDEX.NAME', sort: { isSorting: false, isAscending: false  }, type: 'string', width: 100 });
		addColumn({ property: 'createdBy', label: 'ASSET_EXPLORER.INDEX.CREATED_BY', sort: { isSorting: false, isAscending: false  }, type: 'string', width: 100 });
		addColumn({ property: 'createdOn', label: 'ASSET_EXPLORER.INDEX.CREATED_ON', sort: { isSorting: false, isAscending: true  }, type: 'date', format: null, width: 100 });
		addColumn({ property: 'updatedOn', label: 'ASSET_EXPLORER.INDEX.UPDATED_ON', sort: { isSorting: false, isAscending: true  }, type: 'date', format: null, width: 100 });
		addColumn({ property: 'isShared', label: 'ASSET_EXPLORER.INDEX.SHARED',  sort: { isSorting: false,  isAscending: false  }, type: 'boolean', width: 100 });
		addColumn({ property: 'isSystem', label: 'ASSET_EXPLORER.INDEX.SYSTEM', sort: { isSorting: false,  isAscending: false  }, type: 'boolean', width: 100 });

		return columns;
	},

	/**
	 * Mark a column as sorted, mark the rest of columns as unsorted
	 +
	 * @param columnName  column Name
	 * @returns {GridColumnModel[]}
	 */
	setColumnAsSorted(columnName: string): GridColumnModel[] {
		if (!columnName) {
			columnName = DEFAULT_COLUMN_NAME;
		}

		const restartedSort = { sort: { isSorting: false, isAscending: false } };

		columns = columns.map((column: GridColumnModel) => {
			const sort = (column.property === columnName) ? { sort: {isSorting: true, isAscending: !column.sort.isAscending } } : restartedSort ;
			return Object.assign({}, column, sort);
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
			return (column.type === 'date') ? ({...column, format}) : column;
		});

		return columns;
	},
	/**
	 * Get the current sorted column, otherwise return the default column
	 +
	 * @returns {GridColumnModel}
	 */
	getCurrentSortedColumnOrDefault(): GridColumnModel {
		const match: GridColumnModel =  columns.find((column: GridColumnModel) => column.sort.isSorting);

		return match || getColumnByName(DEFAULT_COLUMN_NAME);
	}

};
