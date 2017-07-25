/**
 * TM-6779 Easy path to fix reference to attributeSet property on EavEntity that is breaking import of new assets
 */
databaseChangeLog = {
    changeSet(author: "slopez", id: "20170725 TM-6779 remove attributeSet reference in AssetEntity") {
        comment("Allow nulls on attribute_set_id eav_entity table to avoid integrity reference issues with EavAttributeSet")

        sql("""
			ALTER TABLE `eav_entity` MODIFY COLUMN `attribute_set_id` BIGINT(20) NULL;
		""")
    }
}
