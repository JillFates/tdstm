package net.transitionmanager.etl

import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.LocalVariableFacade
import com.tdssrc.grails.StringUtil
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * This class extends {@link StringUtil} adding extra behaviour validating
 * ETL variables. Following the example:
 * <pre>
 *  extract 'url' set url
 *  if (StringUtil.isNotBlank(url)) { //
 *      ...
 * </pre>
 * @see ETLProcessor#defaultCompilerConfiguration()
 */
class StringUtilForETL extends StringUtil {

    static boolean isArray(Object args){
        return (Object[]).isAssignableFrom(args.getClass())
    }
    static def $static_methodMissing(String name, Object args) {
        if (args && isArray(args) && args[0] instanceof LocalVariableFacade) {
            args[0] = ((LocalVariableFacade) args[0]).wrappedObject
            return InvokerHelper.invokeStaticMethod(StringUtil, name, args)
        }
        throw new MissingMethodException(name, StringUtil, args)
    }
}
