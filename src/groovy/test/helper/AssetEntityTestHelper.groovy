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
                assetName: 'Test AssetEntity-' + RandomStringUtils.randomAlphabetic(10)

        )

		assetEntity.save(flush: true, failOnError: true)

        return assetEntity
    }

}
