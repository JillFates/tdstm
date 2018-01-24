package com.tdsops.etl

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.domain.Project

class AssetClassQueryHelper {

    static AssetClass assetClassFor (ETLDomain domain) {
        AssetClass assetClass = null

        switch (domain) {
            case ETLDomain.Application:
                assetClass = AssetClass.APPLICATION
                break
            case ETLDomain.Device:
                assetClass = AssetClass.DEVICE
                break
            case ETLDomain.Database:
                assetClass = AssetClass.DATABASE
                break
            case ETLDomain.Storage:
                assetClass = AssetClass.STORAGE
                break
        }
        assetClass
    }

    static List<? extends AssetEntity> where (Project project, ETLDomain domain, Map<String, ?> fieldsSpec) {

        String hqlWhere = hqlWhere(fieldsSpec)

        AssetClass assetClass = assetClassFor(domain)

        String hql = """
            select AE
              from AssetEntity AE
             where  AE.project = :project and $hqlWhere  
        """.stripIndent()

        AssetEntity.executeQuery(hql, [project: project] + hqlParams(fieldsSpec))
    }

    static String hqlWhere (Map<String, ?> fieldsSpec) {
        fieldsSpec.keySet().collect { String field ->
            " ${transformations[field].property} = :${transformations[field].namedParamter}\n".toString()
        }.join(' and ')
    }

    static Map<String, ?> hqlParams (Map<String, ?> fieldsSpec) {
        fieldsSpec.collectEntries { String key, def value ->
            [("${transformations[key].namedParamter}".toString()): transformations[key].transform(value?.toString())]
        }
    }

    //TODO: Review this with John. Where I can put those commons configurations?
    private static final Map<String, Map> transformations = [
            "id"          : [
                    property     : "AE.id",
                    namedParamter: "id",
                    join         : "",
                    transform    : { String value -> Long.parseLong(value) }
            ],
            "moveBundle"  : [
                    property     : "AE.moveBundle.name",
                    namedParamter: "moveBundleName",
                    join         : "left outer join AE.moveBundle",
                    transform    : { String value -> value?.trim() }
            ],
            "project"     : [
                    property     : "AE.project.description",
                    namedParamter: "projectDescription",
                    join         : "left outer join AE.project",
                    transform    : { String value -> value?.trim() }
            ],
            "manufacturer": [
                    property     : "AE.manufacturer.name",
                    namedParamter: "manufacturerName",
                    join         : "left outer join AE.manufacturer",
                    transform    : { String value -> value?.trim() }
            ],
            "sme"         : [
                    property     : "AE.sme.firstName",
                    namedParamter: "smeFirstName",
                    join         : "left outer join AE.sme",
                    transform    : { String value -> value?.trim() }
            ],
            "sme2"        : [
                    property     : "AE.sme2.firstName",
                    namedParamter: "sme2FirstName",
                    join         : "left outer join AE.sme2",
                    transform    : { String value -> value?.trim() }
            ],
            "model"       : [
                    property     : "AE.model.modelName",
                    namedParamter: "modelModelName",
                    join         : "left outer join AE.model",
                    transform    : { String value -> value?.trim() }
            ],
            "appOwner"    : [
                    property     : "AE.appOwner.firstName",
                    namedParamter: "appOwnerFirstName",
                    join         : "left outer join AE.appOwner",
                    transform    : { String value -> value?.trim() }
            ]
    ].withDefault { String key ->
        [
                property     : "AE." + key,
                namedParamter: key,
                join         : "",
                transform    : { String value -> value?.trim() }]
    }


}