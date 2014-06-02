package com.jamesward.forcegradleplugin

import org.gradle.api.Project
import org.gradle.process.internal.ExecException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


class ForceMetadataFetchTaskSpec extends Specification {

    def 'can add task to project'() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        def task = project.task('forceMetadataFetch', type: ForceMetadataFetchTask)

        then:
        task instanceof ForceMetadataFetchTask
    }

    def 'fetch works'() {
        setup:
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: ForcePlugin)
        project.force.username = System.properties.forceUsername
        project.force.password = System.properties.forcePassword
        project.force.unpackagedComponents = ["Settings": "Quote"]

        when:
        def task = project.tasks.forceMetadataFetch
        task.run()

        then:
        def packageXml = project.file("src/main/salesforce/unpackaged/package.xml").text
        packageXml.contains("<members>Quote</members>")
        packageXml.contains("<name>Settings</name>")

        def settingsXml = project.file("src/main/salesforce/unpackaged/settings/Quote.settings").text
        settingsXml.contains("<enableQuote>true</enableQuote>")

        notThrown ExecException
    }

}