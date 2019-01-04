import net.transitionmanager.domain.Manufacturer
import spock.lang.Specification

class MySqlCustomDialectSpec extends Specification {

	void 'Test custom MySQL dialect concat_ws'() {
		when: 'Executing a HQL query with concst_ws, with a separator of a comma, and the id and name columns'
			List<Map> results = Manufacturer.executeQuery("""
				SELECT new map( concat_ws(',', m.id, m.name) as values)
				from Manufacturer m 
			""", [], [max: 1])
		then: 'results in a column that is the two column values separated by a comma '
			results.size() == 1
			results[0].values == '748,21ST CENTURY'
	}

	void 'Test custom MySQL dialect json_object'() {
		when: 'Executing a HQL query with son_object of the id and name columns'
			List<Map> results = Manufacturer.executeQuery("""
				SELECT new map( json_object('id', m.id, 'name', m.name) as json)
				from Manufacturer m 
			""", [], [max: 1])
		then: 'results in a column that has a json map of the name and id column'
			results.size() == 1
			results[0].json == '{"id": 748, "name": "21ST CENTURY"}'
	}

	void 'Test custom MySQL dialect group_concat'() {
		when: 'Executing a HQL query with group_concat of two joined fileds'
			List<Map> results = Manufacturer.executeQuery("""
						SELECT new map(
							ma.id as id,
							ma.name as name,
							group_concat(m.modelName, ',', m.assetType) as models
							)
						from Manufacturer ma
						join ma.models m
						where m.assetType = 'NAS'
						group by ma.id
					""", [], [max: 1])
		then: 'results from a joined table are returned as a string with a comma separated list'
			results.size() == 1
			results[0].name == 'Cisco'
			results[0].id == 58
			results[0].models == 'IronPort S170,NAS'
	}

	void 'Test custom MySQL dialect combining group_concast and json_object'() {
		when: 'Executing a HQL query with group_concat and Json_object'
			List<Map> results = Manufacturer.executeQuery("""
					SELECT new map(
						ma.id as id, 
						ma.name as name,
						CONCAT(
							'[',
							if(
								m.id,
								group_concat(
									json_object('name', m.modelName, 'type', m.assetType)
								),
								''
							), 
							']'
						) as json) 
					from Manufacturer ma 
					join ma.models m
					where m.assetType = 'NAS'
					group by ma.id
				""", [], [max: 1])
		then: 'results from a joined table are returned as a JSON string in one column'
			results.size() == 1
			results[0].name == 'Cisco'
			results[0].id == 58
			results[0].json == '[{"name": "IronPort S170", "type": "NAS"}]'
	}
}
