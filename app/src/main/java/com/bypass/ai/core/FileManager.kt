package com.bypass.ai.core

import android.content.Context
import java.io.File

class FileManager(private val context: Context) {
    private val rootDir: File = context.getExternalFilesDir(null) ?: context.filesDir
    val workspaceDir = File(rootDir, "BypassProjects").apply {
        if (!exists()) mkdirs()
    }

    fun listProjects(): List<File> {
        return workspaceDir.listFiles()?.filter { it.isDirectory }?.toList() ?: emptyList()
    }

    fun createProject(name: String): File? {
        val projectDir = File(workspaceDir, name)
        if (!projectDir.exists()) {
            return if (projectDir.mkdirs()) projectDir else null
        }
        return null
    }

    fun listFiles(dir: File): List<File> {
        return dir.listFiles()?.toList()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
    }

    fun readFile(file: File): String? {
        return try {
            if (file.exists() && file.isFile) file.readText() else null
        } catch (e: Exception) {
            null
        }
    }

    fun writeFile(file: File, content: String): Boolean {
        return try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteFile(file: File): Boolean {
        return try {
            if (file.isDirectory) file.deleteRecursively() else file.delete()
        } catch (e: Exception) {
            false
        }
    }
}
