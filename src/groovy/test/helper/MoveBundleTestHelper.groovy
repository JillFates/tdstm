package test.helper

import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.MoveBundle
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

}