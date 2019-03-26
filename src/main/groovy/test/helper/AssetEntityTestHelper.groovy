package test.helper

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class AssetEntityTestHelper {

    AssetEntity createAssetEntity(AssetClass assetClass, Project project, MoveBundle moveBundle) {
        AssetEntity assetEntity = new AssetEntity(
                project: project,
                moveBundle: moveBundle,
                assetClass: assetClass,
                assetName: 'Test AssetEntity-' + RandomStringUtils.randomAlphabetic(10),
				retireDate: new Date()
        )

		assetEntity.save(flush: true)

        return assetEntity
    }

}
