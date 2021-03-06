package test.helper

import grails.gorm.transactions.Transactional
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.task.Recipe
import net.transitionmanager.task.RecipeVersion

@Transactional
class RecipeTestHelper {

    /**
     * Create a recipe if not exists from given name for E2EProjectSpec to persist at server DB
     * @param: name
     * @param: project
     * @param: person
     * @param: sourceCode not required
     * @returm the recipe
     */
    Recipe createRecipe(String name, Project project, Person person, String sourceCode = null){
        Recipe existingRecipe = Recipe.findWhere([name: name, project: project])
        if (!existingRecipe) {
            Recipe recipe = new Recipe(name: name, description: '', context: '{}', project: project).save(flush: true)
            if (sourceCode) {
                new RecipeVersion(recipe: recipe, sourceCode: sourceCode, versionNumber: 0, createdBy: person).save(flush: true)
            } else {
                new RecipeVersion(recipe: recipe, createdBy: person).save(flush: true)
            }
            return recipe
        }
        return existingRecipe
    }
}
