package net.transitionmanager.service

import grails.transaction.Transactional
import net.transitionmanager.domain.ApiAction

@Transactional
class ApiActionService {

    private List<Map> store = [
            [id:1, name: 'Action one'],
            [id:2, name: 'Action two'],
            [id:3, name: 'Action three'],
            [id:4, name: 'Action four']
    ]

    ApiAction findApiAction(String id) {
        return store.find{ it.id == id }
    }

    List<Map> list(){
        store
    }
}
