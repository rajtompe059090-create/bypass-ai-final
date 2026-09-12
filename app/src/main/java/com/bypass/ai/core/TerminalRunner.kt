package com.bypass.ai.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object TerminalRunner {
    suspend fun runCommand(command: String, workingDir: File): String = withContext(Dispatchers.IO) {
        try {
            val parts = command.split(" ")
            val process = ProcessBuilder(parts)
                .directory(workingDir)
                .redirectErrorStream(true)
                .start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString()
        } catch (e: Exception) {
            "Error: ${e.message}\n(Note: Android environments have limited shell command support)"
        }
    }
}
