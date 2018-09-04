/**
 * Some views for the Assets were created by DB Scripts, however they given ID's are
 * the same across all environments
 */

/**
 * Contains the Static IDs across all environments
 */
export enum ASSET_ENTITY_MENU {
	All_ASSETS = 1,
	All_DATABASES = 2,
	All_DEVICE = 3,
	All_SERVERS = 4,
	All_STORAGE_PHYSICAL = 5,
	All_STORAGE_VIRTUAL = 6,
	All_APPLICATIONS = 7
}

export enum ASSET_MENU_CSS_TREE {
	PARENT_MENU = 'menu-parent-assets',
	CHILD_MENU = 'menu-parent-assets-asset-explorer',
	CHILD_CLASS = 'assets-asset-explorer-child',
}