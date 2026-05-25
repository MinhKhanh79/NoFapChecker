package com.mika.nofap.service

import com.mika.nofap.IUserService
import java.io.IOException

class UserService : IUserService.Stub() {
    override fun destroy() {
        System.exit(0)
    }

    override fun runShellCommand(cmd: String): Int {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            process.waitFor()
            process.exitValue()
        } catch (e: Exception) {
            -1
        }
    }

    override fun getShellOutput(cmd: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            output.trim()
        } catch (e: Exception) {
            ""
        }
    }
}
