package com.save.protect.helper

import android.os.Environment
import android.util.Log
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class Logcat {
    companion object {
        const val TAG: String = "PROTECT"

        fun d(message: String) {
            Log.d(TAG, buildLogMessage(message))
        }


        fun e(message: String) {
            Log.e(TAG, buildLogMessage(message))
        }


        fun i(message: String) {
            Log.i(TAG, buildLogMessage(message))
        }


        fun w(message: String, exception: Exception?) {
            Log.w(TAG, buildLogMessage(message))
        }


        fun v(message: String) {
            Log.v(TAG, buildLogMessage(message))
        }


        private fun buildLogMessage(message: String): String {

//            try {
//                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//
//
//                for(signature in packageInfo.signatures) {
//                    val md = MessageDigest.getInstance("SHA")
//                    md.update(signature.toByteArray())
//                    Logcat.d("KeyHash: "+ Base64.encodeToString(md.digest(), Base64.DEFAULT))
//
//                }
//            } catch (e: PackageManager.NameNotFoundException) {
//                e.printStackTrace()
//            }

            val ste = Thread.currentThread().stackTrace[4]
            val sb = StringBuilder()
            sb.append("[")
            sb.append(ste.fileName)
            sb.append("] ")
            sb.append(ste.methodName)
            sb.append(" #")
            sb.append(ste.lineNumber)
            sb.append(": ")
            sb.append(message)

            // writeLog(sb.toString())
            return sb.toString()
        }

        private fun writeLog(str: String) {
            var result = 0
            val dirPath = Environment.getExternalStorageDirectory().absolutePath + "/log/$TAG/"
            val file = File(dirPath)

            if (!file.exists())
                file.mkdirs()

            val nowDate =
                SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))

            val savefile = File("$dirPath$nowDate.txt")
            try {
                val nowTime =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis()))

                val bfw = BufferedWriter(FileWriter("$dirPath$nowDate.txt", true))
                bfw.write("++ Time: $nowTime\n")
                bfw.write(str)
                bfw.write("\n")
                bfw.flush()
                bfw.close()
            } catch (e: FileNotFoundException) {
                result = 1
                e.printStackTrace()
            } catch (e: IOException) {
                result = 1
                e.printStackTrace()
            }

        }
    }


}