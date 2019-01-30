package test.helper

import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class MoveBundleTestHelper {


    MoveBundle createBundle(Project project, String name = null, Boolean useForPlanning = true) {
        if (StringUtil.isBlank(name)) {
            name = RandomStringUtils.randomAlphabetic(10)
        }

        String workflowCode = RandomStringUtils.randomAlphabetic(10)

        MoveBundle bundle = new MoveBundle([name:name, project:project, workflowCode: workflowCode, useForPlanning: useForPlanning])
        bundle.save(failOnError:true, flush: true)

        return bundle

    }

    /**
     * Create a bundle if not exists from given name for E2EProjectSpec to persist at server DB
     * @param: name
     * @param: project
     * @param: event
     * @param: useForPlanning defaulted true
     * @returm the bundle
     */
    MoveBundle createBundle(String name, Project project, MoveEvent event, Boolean useForPlanning = true) {
        MoveBundle bundle = MoveBundle.findWhere([name: name, project: project])
        if (!bundle){
            String workflowCode = RandomStringUtils.randomAlphabetic(10)
            bundle = new MoveBundle([name:name, project:project, moveEvent: event,  workflowCode: workflowCode, useForPlanning: useForPlanning])
            bundle.save(flush: true, failOnError:true)
        } else if (!bundle.moveEvent){
            bundle.moveEvent = event
            bundle.save(ffailOnError:true, flush: true)
        }
        return bundle
    }

}