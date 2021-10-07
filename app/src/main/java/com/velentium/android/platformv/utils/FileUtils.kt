package com.velentium.android.platformv.utils

import android.util.Log
import java.io.File
import java.io.FileOutputStream

@Suppress("MemberVisibilityCanBePrivate")
object FileUtils {
    const val TAG: String = "FileUtils"

    /**
     * @return true if [atPath] exists, false otherwise
     */
    fun fileExists(atPath: String): Boolean =
        File(atPath).exists()

    /**
     * Attempts to move [fromFile] to [toFile].
     */
    fun move(fromFile: File, toFile: File, overwrite: Boolean = true): Boolean {
        if (!fromFile.exists()) {
            Log.e(TAG, "File does not exist at: ${fromFile.absolutePath}")
            return false
        }
        val copied = fromFile.copyTo(toFile, overwrite)
        return copied.exists() && fromFile.delete()
    }

    /**
     * Attempts to move [filePath] to [toPath].
     */
    fun move(filePath: String,
             toPath: String,
             overwrite: Boolean = true): Boolean {
        val fromFile = File(filePath)
        val toFile = File(toPath)
        return move(fromFile, toFile, overwrite)
    }

    /**
     * Attempts to save the [data] to the specified [toPath].
     */
    fun saveData(data: ByteArray,
                 toPath: String,
                 overwrite: Boolean = true): Boolean {
        return saveData(
            data = data,
            file = File(toPath),
            overwrite = overwrite
        )
    }

    /**
     * Attempts to save the [data] to the specified [file].
     */
    fun saveData(data: ByteArray,
                 file: File,
                 overwrite: Boolean = true): Boolean {
        if (!overwrite && file.exists()) {
            Log.e(TAG, "File already exists at path: ${file.absolutePath}")
            return false
        }
        if (!file.exists() && !file.createNewFile()) {
            Log.e(TAG, "Failed to create file at path: ${file.absolutePath}")
            return false
        }
        try {
            FileOutputStream(file, false).use { fos ->
                fos.write(data)
            }
        } catch(ex: Throwable) {
            Log.e(TAG, "Failed to write data to path: ${file.absolutePath}")
            file.delete()
            return false
        }
        return file.exists()
    }

    /**
     * @return The list of files at [path] that match [filter].
     */
    fun fileListAtPath(path: String, filter: ((File) -> Boolean)? = null): List<File> {
        return fileListAtPath(File(path), filter)
    }

    /**
     * @return The list of files at [filePath] that match [filter].
     */
    fun fileListAtPath(filePath: File, filter: ((File) -> Boolean)? = null): List<File> {
        if (!filePath.exists()) {
            return listOf()
        }
        val files = filter?.let { fileFilter ->
            filePath.listFiles(fileFilter)
        } ?: filePath.listFiles()
        return if (files.isEmpty()) listOf() else files.toList()
    }
}