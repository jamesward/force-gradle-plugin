package com.jamesward.forcegradleplugin

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import spock.lang.Specification


class ForcePluginSpec extends Specification {

    def 'ForcePlugin adds the forceMetadataFetch Task'() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'force'

        then:
        project.tasks.forceMetadataFetch instanceof ForceMetadataFetchTask
    }

    def 'extensions exist'() {
        setup:
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: ForcePlugin)

        expect:
        project.extensions.getByName("force") instanceof ForcePluginExtension
    }

    def 'unpackagedComponents works'() {
        setup:
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: ForcePlugin)

        when:
        project.force.unpackagedComponents = ["foo": "bar"]

        then:
        project.force.unpackagedComponents.foo == "bar"
    }

}
