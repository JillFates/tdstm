package net.transitionmanager.command

/**
 * The DataviewUserParamsCommand represents the various parameters that make up the properties
 * that the user can override in the view.
 */

class DataviewUserParamsCommand implements CommandObject {

    String sortDomain
    String sortProperty
    String sortOrder = 'a'
    Boolean justPlanning
    Boolean forExport

    int offset = 0
    int limit = 25

    /*
        {
          "filters": {
            "domains": [
              "common",
              "application",
              "database",
              "device",
              "storage"
            ],
            "columns": [
              {
                "domain": "common",
                "edit": false,
                "filter": "",
                "label": "Name",
                "locked": true,
                "property": "assetName",
                "width": 220
              }
            ],
            "named": "physicalServer,toValidate",
            "extra": [
              {
                "domain": "common",
                "filter": "FOO",
                "property": "assetName"
              }
            ]
          }
        }
     */
    // TODO: improve filters using a more complex command object.
    Map<String, List> filters

    static constraints = {
        sortDomain blank: false, inList: ['common', 'application', 'database', 'device', 'storage']
        sortProperty blank: false
        sortOrder blank: false, inList: ['a', 'd']
        offset min: 0
        limit min: 0
        justPlanning nullable: true
        forExport nullable: true
    }
}