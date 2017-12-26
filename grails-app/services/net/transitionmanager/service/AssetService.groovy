package net.transitionmanager.service

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.Project

class AssetService {

    def customDomainService
    def securityService

    /**
     * Used to set default values for custom fields on the given asset
     * @param asset - the asset to set the default custom fields values into
     * @param fieldSpec - the list of custom fields
     */
    void setCustomDefaultValues(AssetEntity asset) {

        String domain = asset.assetClass.toString().toUpperCase()

        // The asset has to have a project assigned to it to make this all work correctly
        assert asset.project

        // Get list of ALL custom fields
        Map fieldSpecs = customDomainService.customFieldSpecs(asset.project, domain)
        List fieldList = fieldSpecs[domain].fields

        // Loop over the fields and set the default value appropriately
        for (field in fieldList) {
            if ( ! StringUtil.isBlank(field.default) ) {
                asset[field.field] = field.default
            }
        }
    }

   /**
    * Used to delete asset dependencies in bulk with a list of dependency ids for a specific project
    * @param project - the project that the asset dependencies should belong to
    * @param dependencyIds - list of ids for which dependencies are requested to be deleted
    * @return a count of the number of records that are deleted
    */
   String bulkDeleteDependencies(Project project, List<String> dependencyIds) {

      // if no ids given don't process anything.
      if (dependencyIds.isEmpty()) {
         return "0 $type records were deleted"
      }

      List<Long> depIds = []
      dependencyIds.each { v ->
         Long id = NumberUtil.toPositiveLong(v, -1)
         if (id > 0) {
            depIds << id
         }
      }
      log.debug "bulkDeleteDependencies: $depIds to be deleted"
      int count = depIds.size()

      // Now make sure that the ids are associated to the project
      List<Long> validatedDepIds =
              AssetDependency.where {
                 asset.project == project
                 dependent.project == project
                 id in depIds
              }
              .projections { property 'id' }
                      .list()

      if (count != validatedDepIds.size()) {
         List<Long> idsNotInProject = depIds - validatedDepIds
         log.warn "bulkDeleteDependencies called with dependency ids not assigned to project ($depIds)"
      }

      if (validatedDepIds.size() > 0) {
            count = AssetDependency.where {
               id in validatedDepIds
            }.deleteAll()
      } else {
         count = 0
      }

      return "$count record${count==1? ' was' : 's were'} deleted"
   }
}
