import org.codehaus.groovy.grails.validation.AbstractConstraint
import org.springframework.validation.Errors


// The constraint is based upon the following posts:
//http://www.zorched.net/2008/01/25/build-a-custom-validator-in-grails-with-a-plugin/
//http://www.zorched.net/2008/01/26/custom-validators-in-grails-in-a-single-app/

class RefCodeConstraint extends AbstractConstraint {
	
	private static final String DEFAULT_NOT_REF_CODE_MESSAGE_CODE = "default.not.refCode.message";
	public static final String REF_CODE_CONSTRAINT = "refCode";
	
	static refCodeService 
	
	private String domainType;
	
	public void setParameter(Object constraintParameter) {
		if(!(constraintParameter instanceof String))
			throw new IllegalArgumentException("Parameter for constraint ["
			+REF_CODE_CONSTRAINT+"] of property ["
			+constraintPropertyName+"] of class ["
			+constraintOwningClass+"] must be a string value");
		
		this.domainType = ((String)constraintParameter);
		super.setParameter(constraintParameter);
	}
	
	protected void processValidate(Object target, Object propertyValue, Errors errors) {
		if (! validRefCode(target, propertyValue)) {
			def args = (Object[]) [constraintPropertyName, constraintOwningClass,
					propertyValue, domainType]
			super.rejectValue(target, errors, DEFAULT_NOT_REF_CODE_MESSAGE_CODE,
					"not." + REF_CODE_CONSTRAINT, args);
		}
	}
	
	boolean supports(Class type) {
		return type != null && String.class.isAssignableFrom(type);
	}
	
	String getName() {
		return REF_CODE_CONSTRAINT;
	}
	
	boolean validRefCode(target, propertyValue) {
		refCodeService.validate(domainType, propertyValue)
	}
}
