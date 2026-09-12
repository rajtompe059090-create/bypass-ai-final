import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.bypass.ai.*
import java.io.File
import kotlin.system.exitProcess

fun main() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(GenerateContentRequest::class.java)
    
    val req = GenerateContentRequest(
        contents = listOf(Content(role = "user", parts = listOf(Part("create a calculator app")))),
        systemInstruction = Content(role = "system", parts = listOf(Part("system rules here")))
    )
    
    val json = adapter.toJson(req)
    println("MOSHI OUTPUT: $json")
    if (json.contains("create a calculator app") && json.contains("system rules here")) {
        println("SERIALIZATION OK")
    } else {
        println("SERIALIZATION FAILED")
        exitProcess(1)
    }
}
