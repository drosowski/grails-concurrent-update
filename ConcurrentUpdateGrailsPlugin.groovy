import com.rosowski.grails.plugin.IncludeVersionListener
import com.rosowski.grails.plugin.ConcurrentUpdate

class ConcurrentUpdateGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/domain/com/rosowski/grails/plugin/CheckedClass.groovy",
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = "Daniel Rosowski"
    def authorEmail = "daniel.rosowski@gmx.de"
    def title = "Plugin provides annotation ConcurrentUpdate which adds custom validator for version check."
    def description = '''\\
This plugin provides an annotation ConcurrentUpdate. When annotating a class, the constraints closure is extended
with a custom validator which checks if the version of the edited domain class has been changed since loading it from
the database. Additionally the plugin adds a BindEventListener which sets the version property in the autobinding
process.
'''

    // URL to the plugin's documentation
    def documentation = "http://github.com/drosowski/grails-concurrent-update"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
		includeVersionListener(IncludeVersionListener)
    }

    def doWithDynamicMethods = { ctx ->
        for (domainClass in application.domainClasses) {
			if(domainClass.clazz.getAnnotation(ConcurrentUpdate.class) != null) {
              domainClass.metaClass.isLocked = false
            }
		}
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
