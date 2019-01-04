package com.tdsops.common.security.spring

import groovy.transform.CompileStatic
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.RuntimeBeanReference
import org.springframework.beans.factory.support.GenericBeanDefinition

/**
 * Retains the existing property values but changes the bean implementation class to a custom subclass,
 * and in some cases also adds extra bean dependencies.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class SecurityBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		updateClassAndAddProperties beanFactory, 'authenticationProcessingFilter', TdsLocalAuthenticationFilter,
				'securityService'

		updateClassAndAddProperties beanFactory, 'authenticationFailureHandler', TdsAuthenticationFailureHandler,
				'auditService', 'grailsLinkGenerator', 'messageSource', 'securityService'

		updateClassAndAddProperties beanFactory, 'authenticationSuccessHandler', TdsAuthenticationSuccessHandler,
				'auditService', 'securityService', 'userPreferenceService', 'userService'

		updateClassAndAddProperties beanFactory, 'objectDefinitionSource', TdsAnnotationFilterInvocationDefinition
	}

	private void updateClassAndAddProperties(ConfigurableListableBeanFactory beanFactory, String beanName,
	                                         Class newClass, String... refPropertyNames) {

		GenericBeanDefinition beanDefinition = (GenericBeanDefinition)beanFactory.getBeanDefinition(beanName)
		beanDefinition.beanClass = newClass
		for (String refName in refPropertyNames) {
			beanDefinition.propertyValues.addPropertyValue refName, new RuntimeBeanReference(refName)
		}
	}
}
