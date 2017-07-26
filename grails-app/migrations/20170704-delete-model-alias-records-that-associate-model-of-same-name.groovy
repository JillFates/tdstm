/**
 * @author David Ontiveros
 * TM-6627
 */
databaseChangeLog = {
    changeSet(author: "dontiveros", id: "20170704 TM-6627 delete duplicate model Alias records") {
        comment('Create a migration script to delete Model Alias records that are associate to model of the same name')

        sql("""
			delete from model_alias where id in (
            	select id from
            		( select ma.id as id
            		from model m
            		join model_alias ma on ma.manufacturer_id = m.manufacturer_id and ma.name=m.name
            		where ma.model_id = m.model_id) as alias_dups
            );
		""")
    }
}

