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
							sqlStatement = """UPDATE recipe set context = '{"bundleId": [$recipe.default_asset_id], "tag":[], "and":true}' where recipe_id = $recipe.recipe_id;"""
							break
						case 'Event':
							sqlStatement = """UPDATE recipe set context = '{"eventId": $recipe.default_asset_id, "tag":[], "and":true}' where recipe_id = $recipe.recipe_id;"""
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
}