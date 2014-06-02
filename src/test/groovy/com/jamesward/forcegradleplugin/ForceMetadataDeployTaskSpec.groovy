package com.jamesward.forcegradleplugin

import org.gradle.api.Project
import org.gradle.process.internal.ExecException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


class ForceMetadataDeployTaskSpec extends Specification {

    def 'can add task to project'() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        def task = project.task('forceMetadataDeploy', type: ForceMetadataDeployTask)

        then:
        task instanceof ForceMetadataDeployTask
    }

    def 'deploy works'() {
        setup:
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: ForcePlugin)
        project.force.username = System.properties.forceUsername
        project.force.password = System.properties.forcePassword

        File srcDir = project.file(ForcePlugin.srcPath)

        File unpackagedDir = new File(srcDir, "unpackaged")
        unpackagedDir.mkdirs()

        File packageXml = new File(unpackagedDir, "package.xml")
        packageXml.createNewFile()
        packageXml.write("""<?xml version="1.0" encoding="UTF-8"?>
<Package xmlns="http://soap.sforce.com/2006/04/metadata">
    <types>
    <members>Quote</members>
        <name>Settings</name>
    </types>
    <version>29.0</version>
</Package>""")

        File settingsDir = new File(unpackagedDir, "settings")
        settingsDir.mkdirs()
        File quoteSettings = new File(settingsDir, "Quote.settings")
        quoteSettings.createNewFile()
        quoteSettings.write("""<?xml version="1.0" encoding="UTF-8"?>
<QuoteSettings xmlns="http://soap.sforce.com/2006/04/metadata">
    <enableQuote>true</enableQuote>
</QuoteSettings>""")

        when:
        def task = project.tasks.forceMetadataDeploy
        task.run()

        then:

        notThrown ExecException
    }

}