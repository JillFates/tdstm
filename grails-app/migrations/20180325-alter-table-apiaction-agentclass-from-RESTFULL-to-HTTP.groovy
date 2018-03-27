databaseChangeLog = {
	changeSet(author: 'OLUNA', id: 'TM-9286') {
		comment('rename agent_class RESTFULL to HTTP')
		sql("UPDATE api_action set agent_class='HTTP' where agent_class='RESTFULL'")
	}
}
