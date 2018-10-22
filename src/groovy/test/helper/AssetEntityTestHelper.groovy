package test.helper

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.SettingService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.jdbc.core.JdbcTemplate

class AssetEntityTestHelper {

    AssetEntity createAssetEntity(AssetClass assetClass, Project project, MoveBundle moveBundle) {
        AssetEntity assetEntity = new AssetEntity(
                project: project,
                moveBundle: moveBundle,
                assetClass: assetClass,
                assetName: 'Test AssetEntity-' + RandomStringUtils.randomAlphabetic(10),
				retireDate: new Date()
        )

		assetEntity.save(flush: true, failOnError: true)

        return assetEntity
    }

    AssetEntity createAssetEntity(Map assetData, Project project, MoveBundle moveBundle) {
        AssetEntity existingAssetEntity = AssetEntity.findWhere([assetName: assetData.name, project: project])
        if(!existingAssetEntity) {
            AssetEntity assetEntity
            AssetEntity.withTransaction {
                assetEntity = new AssetEntity(
                    project: project,
                    moveBundle: moveBundle,
                    assetClass: assetData.assetClass,
                    assetName: assetData.name
                )
                assetEntity.save(flush: true)
            }
            return assetEntity
        } else {
            return existingAssetEntity
        }
    }
}
