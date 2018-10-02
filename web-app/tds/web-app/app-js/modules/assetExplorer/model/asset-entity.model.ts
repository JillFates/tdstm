/**
 * Some views for the Assets were created by DB Scripts, however the given ID's are
 * the same across all environments
 */

/**
 * Used to avoid the hardcoded reference for Show, Create and Edit of all Assets
 */
export enum ASSET_ENTITY_DIALOG_TYPES {
	APPLICATION = 'APPLICATION',
	DATABASE = 'DATABASE',
	DEVICE = 'DEVICE',
	STORAGE = 'STORAGE',
}
