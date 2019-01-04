databaseChangeLog = {
	
	//This changeset will remove Remove orphaned records from asset_dependency table
	changeSet(author: "lokanada", id: "20140701 TM-2933-1") {
		comment('Remove orphaned records from asset_dependency table')
		sql("""
				delete x 
				from asset_dependency as x, 
					(select ad.asset_dependency_id as id 
						from asset_dependency ad 
						left outer join asset_entity e on e.asset_entity_id = ad.asset_id
						where e.asset_entity_id is null
					) as deps_to_del
				where x.asset_dependency_id = deps_to_del.id ;
			""")
		
		sql("""
				delete x
				from asset_dependency as x,
					(select ad.asset_dependency_id as id
						from asset_dependency ad
						left outer join asset_entity e on e.asset_entity_id = ad.dependent_id
						where e.asset_entity_id is null
					) as deps_to_del
				where x.asset_dependency_id = deps_to_del.id;
		    """)
	}
}
