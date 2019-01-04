
import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.RackService

/**
 * Used tp set default model to rack that don't have a model associated
 */

databaseChangeLog = {

	def rackService = new RackService()

	/**
	 * Used tp set default model to rack that don't have a model associated
	 * 
	 */
	changeSet(author: "dscarpa", id: "20141111 TM-3550-1") {
		comment('Used tp set default model to rack that dont have a model associated')
		grailsChange {
			change {
				def model = rackService.getDefaultRackModel()
				sql.execute("UPDATE rack SET model_id=${model.id}, manufacturer_id=${model.manufacturer.id} WHERE model_id IS NULL;")
			 }
		}
	}

}
