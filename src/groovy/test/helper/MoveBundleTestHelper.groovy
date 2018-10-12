package test.helper

import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import org.apache.commons.lang.RandomStringUtils

class MoveBundleTestHelper {


    MoveBundle createBundle(Project project, String name = null, Boolean useForPlanning = true) {
        if (StringUtil.isBlank(name)) {
            name = RandomStringUtils.randomAlphabetic(10)
        }

        String workflowCode = RandomStringUtils.randomAlphabetic(10)

        MoveBundle bundle = new MoveBundle([name:name, project:project, workflowCode: workflowCode, useForPlanning: useForPlanning])
        bundle.save(failOnError:true)

        return bundle

    }

    MoveBundle createBundle(String name, Project project, MoveEvent event, Boolean useForPlanning = true) {
        MoveBundle bundle = MoveBundle.findWhere([name: name, project: project])
        if (!bundle){
            String workflowCode = RandomStringUtils.randomAlphabetic(10)
            bundle = new MoveBundle([name:name, project:project, moveEvent: event,  workflowCode: workflowCode, useForPlanning: useForPlanning])
            bundle.save(flush: true, failOnError:true)
        } else if (!bundle.moveEvent){
            bundle.moveEvent = event
            bundle.save(flush: true)
        }
        return bundle
    }

}