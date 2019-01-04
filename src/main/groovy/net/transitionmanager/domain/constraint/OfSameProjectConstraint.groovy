package net.transitionmanager.domain.constraint

import org.codehaus.groovy.grails.validation.AbstractConstraint
import org.springframework.validation.Errors

/**
 * A custom constraint that makes sure that a property has the same project id as the
 * parent/target object.
 */
class OfSameProjectConstraint extends AbstractConstraint  {
	static final NAME = 'ofSameProject'
	static final String OF_SAME_PROJECT_CODE_MESSAGE_CODE = "of.same.project.constraint.message"

	/**
	 * Processes the validation of the target domain making sure that it's project is the same
	 * as the propertyValue's project.
	 *
	 * @param target The parent domain that has a project that you want to make sure is the same as
	 * the project on the child propertyValue.
	 * @param propertyValue The child domain that has a reference to a project that you want to
	 * check against the project on the parent/target domain.
	 * @param errors An object that holds errors from the constraint that gets populated if the target
	 * project doesn't equal the propertyValue project.
	 */
	protected void processValidate(Object target, Object propertyValue, Errors errors) {
		if(parameter && target?.project?.id != propertyValue?.project?.id){
			super.rejectValue(target,errors,OF_SAME_PROJECT_CODE_MESSAGE_CODE, 'OF_SAME_PROJECT_CODE_MESSAGE_CODE', [])
		}
	}

	/**
	 * This sets the parameter of the constraint which is just a simple enabling flag.
	 *
	 * @param param The parameter to set.
	 */
	void setParameter(Object param) {
		if (!(param instanceof Boolean)) {
			throw new IllegalArgumentException("Parameter for constraint [$name] of property [$constraintPropertyName] of class [$constraintOwningClass] must be a boolean")
		}

		super.setParameter(param)
	}

	/**
	 * This checks to see that the constraint supports the object that it set on. It is some what limited
	 * as it only checks on the property it is set on, but not the parent object.
	 *
	 * @param type The class of the property that the constraint is on.
	 *
	 * @return true is the type is not null, and has the property project.
	 */
	boolean supports(Class type) {
		return type != null && type.newInstance().hasProperty('project')
	}

	/**
	 * Gets the name of the constraint.
	 *
	 * @return the name of the constraint.
	 */
	String getName() {
		return NAME
	}
}
