package com.jamesward.forcegradleplugin

import com.sforce.soap.metadata.AsyncResult
import com.sforce.soap.metadata.DeployOptions
import com.sforce.soap.metadata.DeployResult
import com.sforce.soap.metadata.MetadataConnection
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ForceMetadataDeployTask extends DefaultTask {

    /**
     *
     * Deploys the metadata specified in the src/main/salesforce/unpackaged dir
     *
     */
    @TaskAction
    def run() {
        File srcDir = new File(project.file(ForcePlugin.srcPath), "unpackaged")

        String username = project.force.username
        String password = project.force.password

        MetadataConnection metadataConnection = ForceMetadataUtil.createMetadataConnection(username, password, ForceMetadataUtil.LOGIN_URL)

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()

        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)
        addFileToZip(zipOutputStream, srcDir.absolutePath, "")

        DeployOptions deployOptions = new DeployOptions()

        AsyncResult asyncResult = metadataConnection.deploy(byteArrayOutputStream.toByteArray(), deployOptions)

        zipOutputStream.close()
        byteArrayOutputStream.close()

        ForceMetadataUtil.waitForResult(metadataConnection, asyncResult.id, 60, 1000)

        DeployResult deployResult = metadataConnection.checkDeployStatus(asyncResult.id, true)

        if (deployResult.errorMessage != null) {
            throw new Exception(deployResult.errorMessage)
        }
    }

    private static void addFileToZip(ZipOutputStream zOut, String path, String base) throws IOException {
        File f = new File(path)
        String entryName = base + f.name
        ZipEntry zipEntry = new ZipEntry(entryName)

        zOut.putNextEntry(zipEntry)

        if (f.file) {
            zOut.write(f.bytes)
            zOut.closeEntry()
        } else {
            zOut.closeEntry()
            for(child in f.listFiles()) {
                addFileToZip(zOut, child.absolutePath, entryName + "/")
            }
        }
    }

}
