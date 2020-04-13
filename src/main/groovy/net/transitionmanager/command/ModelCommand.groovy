package net.transitionmanager.command

import net.transitionmanager.manufacturer.Manufacturer

class ModelCommand implements CommandObject {

    Long id
    String assetType
    Integer bladeCount
    String bladeHeight
    Integer bladeLabelCount
    Integer bladeRows
    Integer cpuCount
    String cpuType
    Double depth
    String description
    Date endOfLifeDate
    String endOfLifeStatus
    Double height
    String layoutStyle
    Manufacturer manufacturer
    Double memorySize
    String modelFamily
    String modelName
    String modelStatus
    Float powerDesign
    Float powerNameplate
    String powerType
    Float powerUse
    String productLine
    Boolean roomObject
    String sourceURL
    Integer sourceTDS
    Integer sourceTDSVersion
    Double storageSize
    Integer useImage
    Integer usize
    Double weight
    Integer width

    Map aka
    Map connectors

    static constraints = {
        id nullable: true
        manufacturer nullable: false
        assetType nullable: true
        bladeCount nullable: true
        bladeHeight nullable: true
        bladeLabelCount nullable: true
        bladeRows nullable: true
        cpuCount nullable: true
        cpuType nullable: true
        depth nullable: true
        description nullable: true
        endOfLifeDate nullable: true
        endOfLifeStatus nullable: true
        height nullable: true
        layoutStyle nullable: true
        memorySize nullable: true
        modelFamily nullable: true
        modelName nullable: true
        powerType nullable: true, inList: ['Watts', 'Amps']
        powerNameplate nullable: false
        powerDesign nullable: false
        powerUse nullable: false
        modelStatus nullable: true
        productLine nullable: true
        roomObject nullable: true
        sourceURL nullable: true
        sourceTDS nullable: true
        sourceTDSVersion nullable: true
        storageSize nullable: true
        useImage nullable: true
        usize nullable: true
        weight nullable: true
        width nullable: true
    }
}
