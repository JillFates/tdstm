package test.helper

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.SettingService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.jdbc.core.JdbcTemplate

class ApplicationTestHelper {

	Application createApplication(AssetClass assetClass, Project project, MoveBundle moveBundle) {
		Application application = new Application(
			project: project,
			moveBundle: moveBundle,
			assetClass: assetClass,
			assetName: 'Test AssetEntity-' + RandomStringUtils.randomAlphabetic(10)
		)

		application.save(flush: true, failOnError: true)

		return application
	}

}
