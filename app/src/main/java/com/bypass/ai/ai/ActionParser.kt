package com.bypass.ai.ai

data class AiAction(
    val type: String,
    val path: String? = null,
    val content: String? = null,
    val command: String? = null
)

object ActionParser {
    fun parseResponse(response: String): Pair<String, List<AiAction>> {
        val actions = mutableListOf<AiAction>()
        var textBody = response
        
        val actionRegex = "(?s)<action>(.*?)</action>".toRegex()
        
        actionRegex.findAll(response).forEach { matchResult ->
            val actionBlock = matchResult.groupValues[1]
            val lines = actionBlock.lines()
            var type: String? = null
            var path: String? = null
            var content: String? = null
            var command: String? = null
            
            var readingContent = false
            val contentBuilder = StringBuilder()
            
            for (line in lines) {
                if (readingContent) {
                    contentBuilder.append(line).append("\n")
                    continue
                }
                
                when {
                    line.startsWith("type=") -> type = line.substringAfter("type=").trim()
                    line.startsWith("path=") -> path = line.substringAfter("path=").trim()
                    line.startsWith("command=") -> command = line.substringAfter("command=").trim()
                    line.startsWith("content=") -> {
                        readingContent = true
                        val initialContent = line.substringAfter("content=")
                        if (initialContent.isNotEmpty()) {
                            contentBuilder.append(initialContent).append("\n")
                        }
                    }
                }
            }
            
            if (readingContent) {
                content = contentBuilder.toString().trimEnd()
            }
            
            if (type != null) {
                actions.add(AiAction(type, path, content, command))
            }
        }
        
        textBody = textBody.replace(actionRegex, "").trim()
        
        return Pair(textBody, actions)
    }
}
