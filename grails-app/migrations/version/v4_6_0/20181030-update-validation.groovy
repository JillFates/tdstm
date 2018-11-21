package version.v4_6_0

/*
 * Update validation
 * Discovery -> Unknown
 * DependencyReview/DependencyScan -> Validated
 * BundleReady -> PlanReady
 */
databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20181030 TM-12229") {
		comment("Set the validation='Unknown' for Discovery and 'Validated' for DependencyReview/DependencyScan, and 'PlanReady' for BundleReady.")
		sql("UPDATE asset_entity SET validation='Unknown' WHERE (validation = 'Discovery')")
		sql("UPDATE asset_entity SET validation='Validated' WHERE (validation = 'DependencyReview' OR validation ='DependencyScan')")
		sql("UPDATE asset_entity SET validation='PlanReady' WHERE (validation = 'BundleReady')")
	}
}