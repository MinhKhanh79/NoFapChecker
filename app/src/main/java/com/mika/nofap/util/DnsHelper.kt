package com.mika.nofap.util

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.mika.nofap.IUserService
import com.mika.nofap.service.UserService
import rikka.shizuku.Shizuku
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object DnsHelper {
    private const val TAG = "DnsHelper"

    private var userService: IUserService? = null

    private val serviceArgs = Shizuku.UserServiceArgs(ComponentName("com.mika.nofap", UserService::class.java.name))
        .daemon(false)
        .processNameSuffix("privileged_dns")
        .debuggable(true)

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int) {
        Shizuku.requestPermission(requestCode)
    }

    private suspend fun ensureServiceConnected(): Boolean {
        if (userService != null && userService?.asBinder()?.isBinderAlive == true) return true
        if (!isShizukuAvailable()) return false

        return suspendCoroutine { continuation ->
            val resumed = AtomicBoolean(false)
            val conn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    if (resumed.compareAndSet(false, true)) {
                        userService = IUserService.Stub.asInterface(binder)
                        continuation.resume(true)
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    userService = null
                }

                override fun onBindingDied(name: ComponentName?) {
                    if (resumed.compareAndSet(false, true)) {
                        continuation.resume(false)
                    }
                }
            }
            try {
                Shizuku.bindUserService(serviceArgs, conn)
            } catch (e: Exception) {
                if (resumed.compareAndSet(false, true)) {
                    continuation.resume(false)
                }
            }
        }
    }

    suspend fun enableAdultFilter(hostname: String): Boolean {
        if (!ensureServiceConnected()) return false
        return try {
            val res1 = userService?.runShellCommand("settings put global private_dns_mode hostname") ?: -1
            val res2 = userService?.runShellCommand("settings put global private_dns_specifier $hostname") ?: -1
            res1 == 0 && res2 == 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable DNS", e)
            false
        }
    }

    suspend fun disableAdultFilter(): Boolean {
        if (!ensureServiceConnected()) return false
        return try {
            val res = userService?.runShellCommand("settings put global private_dns_mode off") ?: -1
            res == 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable DNS", e)
            false
        }
    }

    suspend fun isAdultFilterActive(expectedHostname: String): Boolean {
        if (!ensureServiceConnected()) return false
        return try {
            val mode = userService?.getShellOutput("settings get global private_dns_mode")
            val specifier = userService?.getShellOutput("settings get global private_dns_specifier")
            Log.d(TAG, "Current DNS: mode=$mode, specifier=$specifier")
            mode == "hostname" && specifier == expectedHostname
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check DNS status", e)
            false
        }
    }
}
