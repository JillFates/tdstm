databaseChangeLog = {
	changeSet(author: 'tpelletier', id: 'TM-11077-1') {
		comment('Change recipe context to be text')

		modifyDataType(columnName: 'context', newDataType: 'text', tableName: 'recipe')
	}


	changeSet(author: 'tpelletier', id: 'TM-11077-2') {
		comment('Change recipe context to a json structure')

		preConditions(onFail: 'MARK_RAN') {
			not columnExists(tableName: 'recipe', columnName: 'default_asset_id')
		}

		grailsChange {
			change {
				def recipes = sql.rows("SELECT recipe_id, default_asset_id, context from recipe")
				String sqlStatement
				recipes.each { recipe ->

					switch (recipe.context.trim()) {
						case 'Bundle':
							if (recipe.default_asset_id) {
								Long eventId = sql.firstRow("SELECT move_event_id from move_bundle where move_bundle_id = $recipe.default_asset_id")[0]
								sqlStatement = """UPDATE recipe set context = '{"eventId": $eventId, "tag":[], "tagMatch":"ANY"}' where recipe_id = $recipe.recipe_id;"""
							} else{
								sqlStatement = """UPDATE recipe set context = '{"tag":[], "tagMatch":"ANY"}' where recipe_id = $recipe.recipe_id;"""
							}

							break
						case 'Event':
							if (recipe.default_asset_id) {
								sqlStatement = """UPDATE recipe set context = '{"eventId": $recipe.default_asset_id, "tag":[], "tagMatch":"ANY"}' where recipe_id = $recipe.recipe_id;"""
							}else{
								sqlStatement = """UPDATE recipe set context = '{"tag":[], "tagMatch":"ANY"}' where recipe_id = $recipe.recipe_id;"""
							}
							break
						default:
							sqlStatement = """UPDATE recipe set context = '{}' where recipe_id = $recipe.recipe_id;"""
					}

					sql.executeUpdate(sqlStatement)
				}
			}
		}
	}


	changeSet(author: 'tpelletier', id: 'TM-11077-3') {
		comment('drop default_asset_id')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'recipe', columnName: 'default_asset_id')
		}

		dropColumn(tableName: 'recipe', columnName: 'default_asset_id')
	}

	changeSet(author: 'tpelletier', id: 'TM-11077-4') {
		comment('Change recipe context to be json')

		modifyDataType(columnName: 'context', newDataType: 'json', tableName: 'recipe')
	}
}