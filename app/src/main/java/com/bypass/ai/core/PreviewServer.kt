package com.bypass.ai.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

object PreviewServer {
    private var serverSocket: ServerSocket? = null
    var rootDirectory: File? = null
    var isRunning = false
        private set
    var port = 8080

    fun start(rootDir: File) {
        if (isRunning) stop()
        rootDirectory = rootDir
        try {
            serverSocket = ServerSocket(port)
            isRunning = true
            thread {
                while (isRunning) {
                    try {
                        val client = serverSocket?.accept()
                        client?.let { handleRequest(it) }
                    } catch (e: Exception) {
                        // socket closed
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isRunning = false
        serverSocket?.close()
        serverSocket = null
    }

    private fun handleRequest(client: Socket) {
        try {
            val input = client.getInputStream().bufferedReader()
            val requestLine = input.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size >= 2) {
                var path = parts[1]
                if (path == "/") path = "/index.html"
                if (path.contains("?")) path = path.substringBefore("?")
                val file = File(rootDirectory, path.removePrefix("/"))
                val out = client.getOutputStream()
                if (file.exists() && !file.isDirectory) {
                    val mime = guessMime(path)
                    val header = "HTTP/1.1 200 OK\r\nContent-Type: $mime\r\nConnection: close\r\n\r\n"
                    out.write(header.toByteArray())
                    file.inputStream().use { it.copyTo(out) }
                } else {
                    val header = "HTTP/1.1 404 Not Found\r\nConnection: close\r\n\r\n"
                    out.write(header.toByteArray())
                }
                out.flush()
                client.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun guessMime(path: String): String {
        return when {
            path.endsWith(".html") -> "text/html"
            path.endsWith(".css") -> "text/css"
            path.endsWith(".js") -> "application/javascript"
            path.endsWith(".json") -> "application/json"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".jpg") -> "image/jpeg"
            path.endsWith(".svg") -> "image/svg+xml"
            else -> "text/plain"
        }
    }
}
