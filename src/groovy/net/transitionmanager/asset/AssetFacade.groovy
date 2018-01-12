package net.transitionmanager.asset

import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import org.codehaus.groovy.grails.exceptions.InvalidPropertyException
import org.springframework.util.ClassUtils

/**
 * This class provides the ability to inspect and interact with the Asset that maybe associated with the action.
 * It wraps the actual Asset class that is associated with the action and provide some functionality
 * that the Reaction Script developer can utilize with a number of restrictions.
 *  - Unable to modify properties when in read-only mode
 *  - Unable to invoke GORM methods like save, delete
 *  - Unable to access relation type objects directly (e.g. Project or Model)
 */
class AssetFacade {
    private AssetEntity asset
    private Map<String, ?> fieldSettings
    private boolean readonly = false

    AssetFacade(AssetEntity asset, Map<String, ?> fieldSettings, boolean readonly) {
        this.asset = asset
        this.fieldSettings = fieldSettings
        setReadonly(readonly)
    }

    /**
     * Get asset property determines if the property is a java type to return its value, if the property
     * is not a java type it returns its toString representation
     * @param name - the property name
     * @return
     */
    Object getProperty(String name) {
        String propertyName = getAssetPropertyOrCustomFieldName(name)
        Object value = asset.getProperty(propertyName)
        if (ClassUtils.isPrimitiveOrWrapper(value.class)) {
            return value
        } else {
            return value.toString()
        }
    }

    /**
     * Set asset property value if the property is a java type or fail with read only exception
     * @param name
     * @param value
     */
    void setProperty(String name, Object value) {
        if (name != 'readonly') {
            checkReadonly(name)
            if (value instanceof String || ClassUtils.isPrimitiveOrWrapper(value.class)) {
                asset.setProperty(getAssetPropertyOrCustomFieldName(name), value)
            } else {
                raiseReadOnlyPropertyException(name)
            }
        }
    }

    /**
     * Get an asset property field or a custom field name
     * @param name
     * @return
     */
    private String getAssetPropertyOrCustomFieldName(String name) {
        Object assetProperty = null
        try {
            assetProperty = GormUtil.getDomainProperty(asset, name)
        } catch (InvalidPropertyException e) {
            // swallow this one
        }
        if (Objects.nonNull(assetProperty)) {
            return name
        } else {
            return getAssetCustomFieldName(name)
        }
    }

    /**
     * Get an asset custom field name from the field specification or fail with missing property exception
     * @param name
     * @return
     */
    private String getAssetCustomFieldName(String name) {
        if (Objects.nonNull(fieldSettings)) {
            Map<String, ?> fieldSpec = fieldSettings[asset.assetClass.toString()].fields.find { field ->
                field.field == name || field.label == name
            }
            if (Objects.nonNull(fieldSpec)) {
                return fieldSpec.field
            } else {
                raiseMissingPropertyException(name)
            }
        }  else {
            raiseMissingPropertyException(name)
        }
        return null
    }

    void setReadonly(boolean value) {
        readonly = value
    }

    boolean isReadonly() {
        return readonly
    }

    boolean isaApplication() {
        return asset.isaApplication()
    }

    boolean isaBlade() {
        return asset.isaBlade()
    }

    boolean isaChassis() {
        return asset.isaChassis()
    }

    boolean isaDatabase() {
        return asset.isaDatabase()
    }

    boolean isaDevice() {
        return asset.isaDevice()
    }

    boolean isaLogicalType() {
        return asset.isaLogicalType()
    }

    boolean isaStorage() {
        return asset.isaStorage()
    }

    boolean isaVM() {
        return asset.isaVM()
    }

    private checkReadonly(String name) {
        if (isReadonly()) {
            raiseReadOnlyPropertyException(name)
        }
    }

    private void raiseReadOnlyPropertyException(String name) {
        throw new ReadOnlyPropertyException(name, AssetFacade.class.name)
    }

    private void raiseMissingPropertyException(String property) {
        throw new MissingPropertyException("No such property: " + property)
    }
}
