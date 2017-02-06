package net.transitionmanager.service

import grails.converters.JSON
import grails.transaction.Transactional
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project

@Transactional
class ApiActionService {

    private List<Map> store = [
            [id:-1, name: 'Action one'],
            [id:-2, name: 'Action two'],
            [id:-3, name: 'Action three'],
            [id:-4, name: 'Action four']
    ]

    ApiAction find(Long id){
        return ApiAction.get(id)
    }

    ApiAction findOrCreateApiAction(Long id, Project project) {
        ApiAction apiAction = find(id)
        if(!apiAction){
            Map apiStored = store.find{ it.id == id }

            if(apiStored) {
                //Creating and Filling Demo data
                apiAction = new ApiAction()
                apiAction.name = apiStored.name
                apiAction.project = project
                apiAction.agentClass = AgentClass.AWS
                apiAction.description = "the description"
                apiAction.agentMethod = "daMethod"
                apiAction.methodParams = (([
                        [
                                param:'assetId',
                                desc: 'The unique id to reference the asset',
                                type:'string',
                                context: "ASSET",
                                property: 'id',
                                value: 'user def value'
                        ],[
                                param: 'assetId 2',
                                desc: 'The unique id to reference the asset 2',
                                type:'string',
                                context: "ASSET",
                                property: 'id 2',
                                value: 'user def value 2'
                        ]
                ]) as JSON).toString()
                if(!apiAction.save()){
                    log.error(apiAction.errors)
                }
            }
        }
        return apiAction
    }

    List<Map> list(){
        List<Map> list = ApiAction.findAll().collect{
            [
                    id:it.id,
                    name: it.name
            ]
        }

        list.addAll(store)
        return list
    }
}
