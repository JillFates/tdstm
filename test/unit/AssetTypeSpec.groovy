import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import spock.lang.Specification


class AssetTypeSpec extends Specification {

	def '01. Test getDefaultAssetTypeForDomain'() {
		expect: 'the assettype for a given asset has the expected value'
			asset.assetType == assetType
		where: 'asset is an Application, a Database, a Logical Storage or an AssetEntity'
			asset               |   assetType
			new Application()   |   AssetType.APPLICATION.name
			new Database()      |   AssetType.DATABASE.name
			new Files()         |   'Logical Storage'
			new AssetEntity()   |   AssetType.SERVER.name
	}

}