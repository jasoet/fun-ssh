/*
 * Copyright (C)2018 - Deny Prasetyo <jasoet87@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package id.jasoet.funssh

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelDirectTCPIP
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelForwardedTCPIP
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.File
import java.io.File.separator
import java.util.Properties

typealias Ssh = JSch

/**
 * Enum for SSH Channel Type.
 */
enum class ChannelType(val type: String) {
    SESSION("session"),
    SHELL("shell"),
    EXEC("exec"),
    X11("x11"),
    AGENT_FORWARDING("auth-agent@openssh.com"),
    DIRECT_TCP_IP("direct-tcpip"),
    FORWARDED_TCP_IP("forwarded-tcpip"),
    SFTP("sftp"),
    SUBSYSTEM("subsystem");
}

private fun homeDir(): String {
    return System.getProperty("java.home")
}

private fun Session.setConfig(propertyMap: Map<String, String>) {
    val config = Properties().apply {
        put("StrictHostKeyChecking", "no")
        putAll(propertyMap)
    }

    this.setConfig(config)
}

private inline fun <reified T : Channel> Session.openChannel(): T {
    return when (T::class) {
        ChannelShell::class -> this.openChannel(ChannelType.SHELL) as T
        ChannelExec::class -> this.openChannel(ChannelType.EXEC) as T
        ChannelDirectTCPIP::class -> this.openChannel(ChannelType.DIRECT_TCP_IP) as T
        ChannelForwardedTCPIP::class -> this.openChannel(ChannelType.FORWARDED_TCP_IP) as T
        ChannelSftp::class -> this.openChannel(ChannelType.SFTP) as T
        else -> throw IllegalArgumentException("Unsupported Class ${T::class.simpleName}")
    }
}

private val defaultKeyLocation = "${homeDir()}$separator.ssh${separator}id_rsa"
private val defaultKnownHostLocation = "${homeDir()}$separator.ssh${separator}known_hosts"
private const val DEFAULT_SSH_PORT = 22

/**
 * Create [Ssh] instance that will be used to create SSH Session.
 *
 * @param privateKeyLocation private key location. If empty, will use id_rsa private key from home directory.
 * @param passphrase private key passphrase.
 * @param knownHostLocation known_hosts location, If empty, will use known_hosts from home directory.
 * @param customize function that can be used to customize ssh object after creation.
 * @return [Ssh] instance.
 * @since 1.0.0
 */
fun createSsh(
    privateKeyLocation: String = defaultKeyLocation,
    passphrase: String = "",
    knownHostLocation: String = defaultKnownHostLocation,
    customize: (Ssh) -> Unit = {}
): Ssh {
    val ssh = Ssh()

    if (File(privateKeyLocation).exists()) {
        if (passphrase.isNotBlank()) {
            ssh.addIdentity(privateKeyLocation, passphrase)
        } else {
            ssh.addIdentity(privateKeyLocation)
        }
    }

    if (File(knownHostLocation).exists()) {
        ssh.setKnownHosts(knownHostLocation)
    }

    customize(ssh)
    return ssh
}


/**
 * Create [Session].
 *
 * @param host SSH host.
 * @param username SSH username.
 * @param password optional SSH password, will use private key auth if not defined.
 * @param port SSH port, 22 as default.
 * @param daemonThread set whether this session is daemon thread or user thread. JVM exits when the only
 * threads running are all daemon threads.
 * @param properties additional properties for [Session].
 * @param customize function to customize [Session] creation.
 * @return [Session] instance.
 * @receiver [Ssh] instance.
 * @since 1.0.0
 */
fun Ssh.createSession(
    host: String,
    username: String,
    password: String = "",
    port: Int = DEFAULT_SSH_PORT,
    daemonThread: Boolean = false,
    properties: Map<String, String> = emptyMap(),
    customize: (Session) -> Unit = {}
): Session {
    val session = this.getSession(username, host, port)
    session.setDaemonThread(daemonThread)
    session.setConfig(properties)
    if (password.isNotBlank()) {
        session.setPassword(password)
    }
    customize(session)
    return session
}


/**
 * Connect to SSH Session, execute the operation and close the connection.
 * This function similar with [kotlin.io.use]
 *
 * @param timeout set session timeout
 * @param operation operation that return [T]
 * @receiver [Session]
 * @return [T] result from [operation]
 * @since 1.0.0
 */
suspend fun <T> Session.use(timeout: Int = 0, operation: suspend Session.() -> T): T {
    try {
        if (!this.isConnected) {
            this.connect(timeout)
        }
        return operation(this)
    } finally {
        this.disconnect()
    }
}

/**
 * Open the SSH Session Channel. Must be invoked inside connected session.
 *
 * @receiver [Session]
 * @param type [ChannelType] channel type
 * @return [Channel]
 * @since 1.0.0
 */
fun Session.openChannel(type: ChannelType): Channel {
    return this.openChannel(type.type)
}

/**
 * Connect to channel, execute [operation] and return [T].
 * Similar with [kotlin.io.use]
 *
 * @param timeout
 * @param operation operation that executed inside connected channel
 * @return [T]  result from the [operation]
 * @since 1.0.0
 */
suspend fun <T> Channel.use(timeout: Int = 0, operation: suspend Channel.() -> T): T {
    try {
        if (!this.isConnected) {
            this.connect(timeout)
        }
        return operation(this)
    } finally {
        this.disconnect()
    }
}
