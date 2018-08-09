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

import com.jcraft.jsch.ChannelExec
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.InputStream

internal fun InputStream?.asString(): String {
    return if (this != null) {
        this.use {
            IOUtils.toString(it, Charsets.UTF_8)
        }
    } else {
        ""
    }
}

/**
 * Create SSH Session, create Exec Channel and execute [String] command.
 *
 * @receiver command to be executed
 * @return [String] remote command result
 */
suspend fun <T> String.executeRemote(
    host: String,
    username: String,
    password: String = "",
    privateKeyLocation: String = defaultKeyLocation,
    passphrase: String = "",
    knownHostLocation: String = defaultKnownHostLocation,
    port: Int = DEFAULT_SSH_PORT,
    operation: (BufferedReader) -> T
): T {
    return createSession(
        host = host,
        username = username,
        password = password,
        privateKeyLocation = privateKeyLocation,
        passphrase = passphrase,
        knownHostLocation = knownHostLocation,
        port = port
    ).use {
        val exec = it.createChannel<ChannelExec>()
        exec.setCommand(this)
        exec.setErrStream(System.err)
        exec.use {
            operation(it.inputStream.buffered().bufferedReader())
        }
    }
}

/**
 * Create SSH Session, create Exec Channel and execute [String] command.
 *
 * @receiver command to be executed
 * @return [String] remote command result
 */
suspend fun String.executeRemoteAsString(
    host: String,
    username: String,
    password: String = "",
    privateKeyLocation: String = defaultKeyLocation,
    passphrase: String = "",
    knownHostLocation: String = defaultKnownHostLocation,
    port: Int = DEFAULT_SSH_PORT
): String {
    return createSession(
        host = host,
        username = username,
        password = password,
        privateKeyLocation = privateKeyLocation,
        passphrase = passphrase,
        knownHostLocation = knownHostLocation,
        port = port
    ).use {
        val exec = it.createChannel<ChannelExec>()
        exec.setCommand(this)
        exec.setErrStream(System.err)
        exec.use {
            it.inputStream.asString()
        }
    }
}
