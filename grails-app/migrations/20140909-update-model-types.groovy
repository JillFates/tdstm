
import java.awt.event.ItemEvent;

/**
 * Add bulk delete permissions
 */
import org.apache.commons.lang3.text.WordUtils;

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140909 TM-3253-1") {
		comment('Update asset types')
		
		grailsChange {
			change {
				def nonModificableTypes = ['CRAC', 'KVM', 'NAS', 'PDU', 'SAN', 'UPS', 'VM', 'VPN'];
				def sequenceList = sql.rows("""select model_id as id, asset_type as type from model""")

				sequenceList.each{
					if (!nonModificableTypes.contains(it.type)) {
						def newType = WordUtils.capitalizeFully(it.type)
						sql.execute("UPDATE model SET asset_type = ${newType} where model_id = ${it.id}")
					}
				}
			}
		}
	}
}
