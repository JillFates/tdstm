databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-9929-01') {
		comment('Delete Recipes with no Recipe Versions')
		sql("""DELETE FROM recipe WHERE recipe_id NOT IN(SELECT distinct(recipe_id) FROM recipe_version)""")
	}

}
