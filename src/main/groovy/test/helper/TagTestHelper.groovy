package test.helper

import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag

@Transactional
class TagTestHelper {

    /**
     * Create a tag if not exists from given Map for E2EProjectSpec to persist at server DB
     * @param: [REQUIRED] etlData = [name: String, description: String, color: String]
     * @param: project
     * @returm the tag
     */
    Tag createTag(Project project, Map tagData) {
        Tag existingTag = Tag.findWhere([name: tagData.name, project: project])
        if (!existingTag) {
            Tag tag = new Tag(
                    name: tagData.name,
                    description: tagData.description,
                    color: tagData.color,
                    project: project
            )
            tag.save(flush: true)
            return tag
        } else {
            return existingTag
        }
    }
}
