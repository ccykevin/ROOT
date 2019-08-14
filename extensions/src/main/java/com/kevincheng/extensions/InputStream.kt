package com.kevincheng.extensions

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

fun InputStream.writeToDisk(directory: File, fileName: String): File? {
    try {
        when {
            !directory.exists() -> directory.mkdirs()
        }
        val file = File(directory, fileName)
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            outputStream = FileOutputStream(file)

            loop@ while (true) {
                when (val data = read(fileReader)) {
                    -1 -> break@loop
                    else -> outputStream.write(fileReader, 0, data)
                }
            }
            outputStream.flush()

            return file
        } catch (e: IOException) {
            return null
        } finally {
            close()
            outputStream?.close()
        }
    } catch (e: IOException) {
        return null
    }
}