import net.transitionmanager.command.assetentity.SetImportPreferencesCommand
import spock.lang.Specification

class SetImportPreferencesCommandSpec extends Specification {

    void 'test command object validation'() {

        when: 'valid values are used'
            SetImportPreferencesCommand command = new SetImportPreferencesCommand()
            command.preference = 'ImportApplication'
            command.value = true

        then: 'validation succeeds'
            command.validate()

        when: 'invalid values are used'
            command = new SetImportPreferencesCommand()
            command.preference = 'INVALID'
            command.value = true

        then: 'validation fails'
            !command.validate()
    }
}
