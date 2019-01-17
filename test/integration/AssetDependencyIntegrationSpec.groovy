import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import test.helper.AssetEntityTestHelper

class AssetDependencyIntegrationSpec extends IntegrationSpec {

	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()
	ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()
	Project project
	MoveBundle moveBundle

	def setup() {
		project = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
	}

	def '1. test AssetDependency.asset field cannot be null'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			AssetDependency assetDependency = new AssetDependency(
				asset: null,
				dependent: device
			)

		then:
			!assetDependency.validate(['asset'])
			assetDependency.errors['asset'].code == 'nullable'
	}

	def '2. test AssetDependency.dependent cannot be null'() {
		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)

		when:
			AssetDependency assetDependency = new AssetDependency(
				asset: device,
				dependent: null,
			)

		then:
			!assetDependency.validate(['dependent'])
			assetDependency.errors['dependent'].code == 'nullable'
	}

	void '3. test AssetDependency.asset and AssetDependency.dependent cannot be same domain'() {
		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
		when:
			AssetDependency assetDependency = new AssetDependency(
				asset: device,
				dependent: device,
			)

		then:
			assetDependency.validate(['asset'])

		and:
			!assetDependency.validate(['dependent'])
			assetDependency.errors['dependent'].code == 'invalid.dependent'
	}

}
