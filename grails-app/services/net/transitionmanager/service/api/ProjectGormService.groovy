package net.transitionmanager.service.api

import grails.transaction.Transactional
import net.transitionmanager.domain.Project

@Transactional(readOnly = true)
class ProjectGormService {

    List<Map> findAllIdAndNameAndProjectCodeProjections(Map params) {
        def c = Project.createCriteria()
        c.list(params) {
            projections {
                property('id')
                property('name')
                property('projectCode')
            }
        }.collect { [id: it[0], name: it[1] , projectCode: it[2]] } as List<Map>
    }

    List<Map> findAllIdAndNameAndProjectCodeAndDescriptionProjections(Map params) {
        def c = Project.createCriteria()
        c.list(params) {
            projections {
                property('id')
                property('name')
                property('projectCode')
                property('description')
            }
        }.collect { [id: it[0], name: it[1] , projectCode: it[2], description: it[3]] } as List<Map>
    }
}

