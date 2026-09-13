import java.net.URL
import java.net.HttpURLConnection
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.io.BufferedReader

fun main() {
    val apiKey = System.getenv("GEMINI_API_KEY") ?: return println("No key")
    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("x-goog-api-key", apiKey)
    conn.doOutput = true
    
    val out = OutputStreamWriter(conn.outputStream)
    out.write("""{"contents":[{"parts":[{"text":"Hello"}]}]}""")
    out.close()
    
    try {
        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println(line)
        }
        reader.close()
    } catch (e: Exception) {
        val errReader = BufferedReader(InputStreamReader(conn.errorStream))
        var line: String?
        while (errReader.readLine().also { line = it } != null) {
            println("ERR: $line")
        }
        errReader.close()
    }
}
