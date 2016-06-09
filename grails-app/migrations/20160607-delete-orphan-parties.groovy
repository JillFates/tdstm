databaseChangeLog = {
	changeSet(author: "arecordon", id: "20160607-TM-4565") {
		comment('Deletes orphan parties.')
		sql("DELETE party FROM party LEFT OUTER JOIN person  ON person_id=party_id  WHERE person_id IS NULL AND party_type_id='PERSON'")
	}
}