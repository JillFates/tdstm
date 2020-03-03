package net.transitionmanager.etl

import com.tdsops.etl.LocalVariableFacade
import com.tdssrc.grails.NumberUtil

class NumberUtilForETL extends NumberUtil {

    /**
     * @see NumberUtil#isLong(java.lang.Object) method
     */
    static boolean isLong(LocalVariableFacade localVariable) {
        return NumberUtil.isLong(localVariable.wrappedObject)
    }

    /**
     * @see NumberUtil#isFloat(java.lang.Object) method
     */
    static boolean isFloat(LocalVariableFacade localVariable) {
        return NumberUtil.isFloat(localVariable.wrappedObject)
    }

    /**
     * @see NumberUtil#isDouble(java.lang.Object) method
     */
    static boolean isDouble(LocalVariableFacade localVariable) {
        return NumberUtil.isDouble(localVariable.wrappedObject)
    }

    /**
     * @see NumberUtil#isInteger(java.lang.Object) method
     */
    static boolean isInteger(LocalVariableFacade localVariable) {
        return NumberUtil.isInteger(localVariable.wrappedObject)
    }

    /**
     * @see NumberUtil#isPositiveLong(java.lang.Object) method
     */
    static boolean isPositiveLong(LocalVariableFacade localVariable) {
        return NumberUtil.isPositiveLong(localVariable.wrappedObject)
    }

    /**
     * @see NumberUtil#toLong(java.lang.Object, java.lang.Long) method
     */
    static boolean toLong(LocalVariableFacade localVariable, Long defVal = null) {
        return NumberUtil.toLong(localVariable.wrappedObject, defVal)
    }

    /**
     * @see NumberUtil#toFloat(java.lang.Object, java.lang.Float) method
     */
    static boolean toFloat(LocalVariableFacade localVariable, Long defVal = null) {
        return NumberUtil.toFloat(localVariable.wrappedObject, defVal)
    }

    /**
     * @see NumberUtil#toBigDecimal(java.lang.Object) method
     */
    static boolean toBigDecimal(LocalVariableFacade localVariable, Long defVal = null) {
        return NumberUtil.toBigDecimal(localVariable.wrappedObject, defVal)
    }

    /**
     * @see NumberUtil#toLongNumber(java.lang.Object, java.lang.Long) method
     */
    static boolean toLongNumber(LocalVariableFacade localVariable, Long defVal = null) {
        return NumberUtil.toLongNumber(localVariable.wrappedObject, defVal)
    }


    /**
     * @see NumberUtil#toPositiveLong(java.lang.Object, java.lang.Long) method
     */
    static boolean toPositiveLong(LocalVariableFacade localVariable, Long defVal = null) {
        return NumberUtil.toPositiveLong(localVariable.wrappedObject, defVal)
    }

    /**
     * @see NumberUtil#toInteger(java.lang.Object, java.lang.Integer) method
     */
    static boolean toInteger(LocalVariableFacade localVariable, Integer defVal = null) {
        return NumberUtil.toInteger(localVariable.wrappedObject, defVal)
    }

    /**
     * @see NumberUtil#toPositiveInteger(java.lang.Object, java.lang.Integer) method
     */
    static boolean toPositiveInteger(LocalVariableFacade localVariable, Integer defVal = null) {
        return NumberUtil.toPositiveInteger(localVariable.wrappedObject, defVal)
    }

    /**
     * @see NumberUtil#isaNumber(java.lang.Object) method
     */
    static boolean isaNumber(LocalVariableFacade localVariable, Integer defVal = null) {
        return NumberUtil.isaNumber(localVariable.wrappedObject)
    }


    /**
     * @see NumberUtil#toDoubleNumber(java.lang.Object) method
     */
    static boolean toDoubleNumber(LocalVariableFacade localVariable, Double defVal = null) {
        return NumberUtil.toDoubleNumber(localVariable.wrappedObject, defVal)
    }
}
