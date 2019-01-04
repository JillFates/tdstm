import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170228 TM-6085 fix enum bad assing") {
		comment('Fix `Type` value assigned to `Method` field in licensed_client')

		def SINGLE_PROJECT = License.Type.SINGLE_PROJECT
		def MAX_SERVERS = License.Method.MAX_SERVERS
		sql("UPDATE `licensed_client` set `method`='${MAX_SERVERS}' where `method`='${SINGLE_PROJECT}'")
	}
}
