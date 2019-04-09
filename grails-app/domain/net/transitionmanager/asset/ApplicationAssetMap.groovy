package net.transitionmanager.asset

/**
 * Represents the relationship between applications and assets for a given company.  It is a Many to Many
 * relationship since servers can run multiple applications and applications can run on multiple machines.
 */
class ApplicationAssetMap {

	Application application
	AssetEntity asset

	// TODO : Create unique index on application_id + asset_id
}
