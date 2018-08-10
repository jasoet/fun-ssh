package id.jasoet.funssh

import java.io.InputStream
import java.util.Properties

internal object SshConfig {
    private val propertiesFile by lazy {
        val inputFile: InputStream? = javaClass.getResourceAsStream("/ssh_config.properties")
        Properties().apply {
            if (inputFile != null) {
                load(inputFile)
            }
        }
    }
    val host: String by lazy {
        val sshHost = System.getenv("SSH_HOST") ?: propertiesFile.getProperty("HOST")
        sshHost ?: throw IllegalArgumentException("No Property Found!")
    }

    val username by lazy {
        val sshUsername = System.getenv("SSH_USERNAME") ?: propertiesFile.getProperty("USERNAME")
        sshUsername ?: throw IllegalArgumentException("No Property Found!")
    }

    val password by lazy {
        val sshPassword = System.getenv("SSH_PASSWORD") ?: propertiesFile.getProperty("PASSWORD")
        sshPassword ?: throw IllegalArgumentException("No Property Found!")
    }
}