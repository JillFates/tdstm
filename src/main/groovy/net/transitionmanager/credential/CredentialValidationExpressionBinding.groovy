package net.transitionmanager.credential

/**
 * Used by the CredentialValidationExpression class to perform the binding for the DSL
 * parsing/run process.
 */
class CredentialValidationExpressionBinding extends Binding {

    /**
     *
     * This DSL does not have any variables as such each of the command parameters we are just
	 * going to return what the compiler is expecting to be a variable name as the content of the
	 * variable.
     *
     * @param name
     * @return See the constructor
     */
    @Override
    Object getVariable(String name) {
        return name
    }

}
