package net.transitionmanager.integration
/**
 * This class is used for binding context in every Api Action script processed.
 */
class ApiActionScriptBinding extends Binding {

    ApiActionScriptBinding (ApiActionScriptProcessor apiActionProcessor, Map vars = [:]) {
        this.variables.putAll([
                SC: ReactionHttpStatus,
//                *: apiActionProcessor.metaClass.methods.collectEntries {
//                    [(it.name): InvokerHelper.getMethodPointer(apiActionProcessor, it.name)]
//                },
                * : vars
        ])
    }

    /**
     * Custom lookup variable. If a variable isn't found it throws an exception
     * @param name
     * @return
     */
    @Override
    Object getVariable (String name) {

        if (variables == null)
            throw new MissingPropertyException('There is not variables bound in this script context')

        Object result = variables.get(name)

        if (result == null && !variables.containsKey(name)) {
            throw new MissingPropertyException('There is no property with name: ' + name + ' bound in this script context')
//            result = name
        }

        return result
    }

}
