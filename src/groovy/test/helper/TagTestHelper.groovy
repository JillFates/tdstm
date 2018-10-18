package test.helper

import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag

class TagTestHelper {

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