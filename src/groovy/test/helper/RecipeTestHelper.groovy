package test.helper

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Recipe
import net.transitionmanager.domain.RecipeVersion

class RecipeTestHelper {

    Recipe createRecipe(String name, Project project, Person person, String sourceCode = null){
        Recipe existingRecipe = Recipe.findWhere([name: name, project: project])
        if (!existingRecipe) {
            Recipe recipe = new Recipe(name: name, context: '{}', project: project)
            recipe.save(flush: true)
            if (sourceCode) {
                RecipeVersion version = new RecipeVersion(recipe: recipe, sourceCode: sourceCode, createdBy: person)
                version.save(flush: true)
            }
            return recipe
        }
        return existingRecipe
    }
}