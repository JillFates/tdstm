package net.transitionmanager.asset

import grails.testing.services.ServiceUnitTest
import net.transitionmanager.imports.DataTransferBatch
import net.transitionmanager.imports.DataTransferValue
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import spock.lang.Specification


class AssetEntityAttributeLoaderServiceSpec extends Specification implements ServiceUnitTest<AssetEntityAttributeLoaderService> {
    void 'TM-17469: test findAndValidateAsset check new asset Storage.assetType is equals of new object Files().assetType'() {
        setup:
            Project project = Mock()
            UserLogin userLogin = Mock()
            Class clazz = Files
            Long assetId = null
            DataTransferBatch dataTransferBatch = null
            List<DataTransferValue> dtvList = [new DataTransferValue(hasError: false)]
		    Integer errorCount = 0
		    Integer errorConflictCount = 0
		    List<String> ignoredAssets = null
		    Integer rowNum = 0
		    List<Map<String, ?>> fieldSpecs = null

        when:
            AssetEntity asset =  service.findAndValidateAsset(project, userLogin, clazz, assetId, dataTransferBatch, dtvList, errorCount, errorConflictCount, ignoredAssets, rowNum, fieldSpecs)

        then:
            asset.class == Files
            asset.assetType == new Files().assetType

    }
}
