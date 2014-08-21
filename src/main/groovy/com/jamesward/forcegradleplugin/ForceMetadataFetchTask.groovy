package com.jamesward.forcegradleplugin

import com.sforce.soap.metadata.AsyncResult
import com.sforce.soap.metadata.MetadataConnection
import com.sforce.soap.metadata.PackageTypeMembers
import com.sforce.soap.metadata.RetrieveRequest
import com.sforce.soap.metadata.RetrieveResult
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ForceMetadataFetchTask extends DefaultTask {

    /**
     *
     * Fetches the metadata specified in the build config and saves it into src/main/salesforce
     *
     */
    @TaskAction
    def run() {
        File destDir = project.file(ForcePlugin.srcPath)

        String username = project.force.username
        String password = project.force.password

        String loginUrl = project.force.sandbox ? ForceMetadataUtil.SANDBOX_URL : ForceMetadataUtil.LOGIN_URL

        MetadataConnection metadataConnection = ForceMetadataUtil.createMetadataConnection(username, password, loginUrl)

        PackageTypeMembers[] packageTypeMembersList = project.force.unpackagedComponents.collect { String k, String v ->
            def packageTypeMembers = new PackageTypeMembers()
            packageTypeMembers.name = k
            packageTypeMembers.members = [v]
            return packageTypeMembers
        }

        com.sforce.soap.metadata.Package unpackaged = new com.sforce.soap.metadata.Package()
        unpackaged.types = packageTypeMembersList

        RetrieveRequest retrieveRequest = new RetrieveRequest()
        retrieveRequest.apiVersion = ForceMetadataUtil.API_VERSION
        retrieveRequest.unpackaged = unpackaged

        AsyncResult asyncResult = metadataConnection.retrieve(retrieveRequest)

        ForceMetadataUtil.waitForResult(metadataConnection, asyncResult.id, 60, 1000)

        RetrieveResult result = metadataConnection.checkRetrieveStatus(asyncResult.id)

        for (message in result.messages) {
            logger.error(message.problem)
        }

        ZipInputStream zipFileStream = new ZipInputStream(new ByteArrayInputStream(result.zipFile))

        ZipEntry zipEntry = null

        while ((zipEntry = zipFileStream.nextEntry) != null) {
            String[] fileParts = zipEntry.name.split("/").reverse()
            String fileName = fileParts.head()
            String dirs = fileParts.tail().reverse().join("/")

            File dir = new File(destDir, dirs)
            dir.mkdirs()

            File newFile = new File(dir, fileName)
            newFile.createNewFile()

            FileOutputStream fout = new FileOutputStream(newFile)
            for (int c = zipFileStream.read(); c != -1; c = zipFileStream.read()) {
                fout.write(c)
            }

            zipFileStream.closeEntry()
            fout.close()
        }

        zipFileStream.close()
    }

}
