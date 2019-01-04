def jdbcTemplate = com.tdsops.common.grails.ApplicationContextHolder.getBean('jdbcTemplate')

databaseChangeLog = {
	changeSet(author: "lokanada", id: "20140507 TM-2688-1") {
		comment('move recipes from moveEvent to recipe tables')
		grailsChange {
			change {
				def recipeList = sql.rows(""" select project_id as id, name as name, runbook_recipe as recipe from move_event
										      where length(ifnull(runbook_recipe,'')) > 200 """)
				
				insertRecord(recipeList, 'Recipe' , jdbcTemplate)
			}
		}
	
	}
	
	changeSet(author: "lokanada", id: "20140507 TM-2688-2") {
		comment('move recipes from moveEvent to recipe_version tables')
		grailsChange {
			change {
				def recipeList = sql.rows(""" select project_id as id, name as name, runbook_recipe as recipe from move_event
										      where length(ifnull(runbook_recipe,'')) > 200 """)
				
				insertRecord(recipeList, 'RecipeVersion' , jdbcTemplate)
			}
		}
	
	}
	changeSet(author: "lokanada", id: "20140507 TM-2688-3") {
		comment('update the recipe_version_id with the recipe_id in recipe table')
		grailsChange {
			change {
				jdbcTemplate.update("update recipe r left join recipe_version rv on rv.recipe_id=r.recipe_id set r.released_version_id = rv.recipe_version_id");
			}
		}
	
	}
	
	//TODO: once verified whether all the records are migrated successfully, then I will uncomment the following lines 
	//      and need to remove the fields runbookStatus,runbookVersion,runbookRecipe in the MoveEvent.grrovy as well.
	/*changeSet(author: "lokanada", id: "20140507 TM-2688-4") {
		comment('Drop "runbook_status","runbook_version","runbook_recipe" column from the move_event table')
		preConditions(onFail:'MARK_RAN') {
			not {
				sqlCheck(expectedResult:'0', """ SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'tdstm' AND TABLE_NAME = 'move_event'
												 AND COLUMN_NAME in ( 'runbook_status','runbook_version','runbook_recipe');""")
			}
		}
		sql(" ALTER TABLE move_event DROP runbook_status, DROP runbook_version, DROP runbook_recipe ")
	}*/
}

def insertRecord(recipeList, domain, jdbcTemplate){
	recipeList.each{
		if(domain=='Recipe'){
			jdbcTemplate.update(""" insert into recipe(name,description,context,project_id,date_created,last_updated,archived,version)
									values ('${it.name}','','Event',${it.id},UTC_TIMESTAMP,UTC_TIMESTAMP,false,0) """);
		}else{
			def recipeId = Recipe.findByNameAndProject(it.name,Project.get(it.id))?.id
			def trimmedRecipe = it.recipe.toString().replaceAll('"', '\'');
			jdbcTemplate.update(""" insert into recipe_version(source_code,version_number,created_by_id,recipe_id,date_created,last_updated,version)
									values ("${trimmedRecipe}",0,100,${recipeId},UTC_TIMESTAMP,UTC_TIMESTAMP,0) """);
		}
	}
}
