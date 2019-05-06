package com.tdsops.tm.domain

import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.exception.RecipeException
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil

/**
 * This class helps validating a recipe's syntax by providing a set of methods
 * that evaluate individual attributes.
 */
class RecipeHelper {


    /**
     * Used to validate the syntax for a given docLink value.
     *  Possible scenarios:
     *      !) #custom1 : indirect reference to a string field of AssetEntity.
     *      2) http://www.somedomain.com (valid Instructions Link)
     *      3) Label | http://somedomain.com (valid Instructions Link)
     */
    static String validateDocLinkSyntax(String docLink) {
        String errorMsg = null
        if (docLink) {
            if (docLink.startsWith("#")) {
                Class type = GormUtil.getDomainPropertyType(AssetEntity, docLink.substring(1))
                if (!type) {
                    errorMsg = "Invalid indirect reference ${docLink}."
                } else if (type != java.lang.String){
                    errorMsg = "Indirect reference ${docLink} is of the wrong type. It should be a string."
                }
            } else {
                if (!HtmlUtil.isMarkupURL(docLink)) {
                    errorMsg = "Invalid markup ${docLink}."
                }
            }
        }
        return errorMsg
    }

    /**
     * Used to resolve the docLink value.
     * @param value : string with the value.
     * @param asset : AssetEntity instance used to lookup the link in case of an indirect reference.
     * @return
     */
    static String resolveDocLink(String value, AssetEntity asset = null) {
        String docLink = null
        String errorMsg
        if (value) {
            // Checks if it's an indirect reference.
            if (value.startsWith("#")) {
                // If no asset was provided, we can't lookup the docLink. Report the error.
                if (!asset) {
                    errorMsg = "Indirect reference ${value}, but no Asset provided."
                } else {
                    try {
                        // Retrieves the value for the indirect reference, which might be null.
                        def indirectValue = AssetEntityHelper.getIndirectPropertyRef(asset, value)
                        if (indirectValue) {
                            // Checks that the value of the indirect reference is a string.
                            if (indirectValue instanceof String) {
                                // If it's a valid markup.
                                if (HtmlUtil.isMarkupURL(indirectValue)) {
                                    docLink = indirectValue
                                } else {
                                    errorMsg = "${value} references a field that doesn't have a valid markup (${indirectValue})."
                                }
                            } else {
                                errorMsg = "${value} references a field that is not a string."
                            }
                        }
                    } catch (RuntimeException re) {
                        errorMsg = "${value} references an invalid Asset field."
                    }
                }
            // If it doesn't begin with '#', it has to be a valid markup.
            } else {
                if (HtmlUtil.isMarkupURL(value)) {
                    docLink = value
                } else {
                    errorMsg = "${value} is not a valid markup."
                }
            }
        }
        // Throw an exception if an error was detected.
        if (errorMsg) {
            throw new RecipeException(errorMsg)
        }
        return docLink
    }

}
