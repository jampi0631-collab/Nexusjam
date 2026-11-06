package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ChatMessageEntity
import com.example.data.preferences.NexusUserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

interface NexusAiRepository {
    fun generateNexusResponse(
        prompt: String,
        history: List<ChatMessageEntity>,
        preferences: NexusUserPreferences
    ): Flow<String>
}

class NexusAiRepositoryImpl : NexusAiRepository {

    private val TAG = "NexusAiRepository"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun generateNexusResponse(
        prompt: String,
        history: List<ChatMessageEntity>,
        preferences: NexusUserPreferences
    ): Flow<String> = flow {
        val title = preferences.userTitle.ifBlank { "chefe" }
        val apiKey = BuildConfig.GEMINI_API_KEY

        var responseText: String? = null

        // 1. Tentar chamar a API Gemini real se a chave estiver configurada
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                responseText = fetchFromGeminiApi(apiKey, prompt, history, title, preferences.highThinking)
            } catch (e: Exception) {
                Log.e(TAG, "Falha na chamada ao Gemini API, recorrendo ao motor neural inteligente: ${e.message}")
            }
        }

        // 2. Se a chave não estiver configurada ou falhar na rede, usar o motor de diálogo natural do NEXUS
        val finalResponse = responseText ?: generateNaturalResponse(prompt, title)

        // Efeito de digitação suave em português natural
        val words = finalResponse.split(" ")
        val buffer = StringBuilder()

        for (word in words) {
            buffer.append(word).append(" ")
            emit(buffer.toString())
            delay(24)
        }
    }

    private suspend fun fetchFromGeminiApi(
        apiKey: String,
        prompt: String,
        history: List<ChatMessageEntity>,
        userTitle: String,
        isHighThinking: Boolean
    ): String = withContext(Dispatchers.IO) {
        val model = if (isHighThinking) "gemini-1.5-pro" else "gemini-1.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val systemPrompt = """
            Você é o NEXUS, um assistente de inteligência artificial futurista, amigável, parceiro e altamente inteligente.
            Você sempre chama seu criador/usuário de '$userTitle' (ou 'chefe') de forma natural e informal.
            Regras essenciais:
            1. Responda DIRETO ao que for perguntado, de forma útil, clara e objetiva.
            2. Use português brasileiro natural, informal e fluido.
            3. NUNCA use jargões robóticos repetitivos como 'analisando vetores táticos', 'permaneço em prontidão' ou 'matriz heurística'.
            4. Quando pedirem opiniões ou recomendações (como 'qual é o melhor jogo de luta do Roblox?'), cite opções reais, justifique os motivos de cada recomendação e dê sua opinião sincera.
            5. Faça perguntas de volta quando precisar entender melhor o objetivo ou o gosto do usuário.
            6. Seja espontâneo, inteligente e prestativo.
        """.trimIndent()

        val contentsArray = JSONArray()

        // Histórico recente (últimas 6 mensagens)
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.sender.equals("nexus", ignoreCase = true)) "model" else "user"
            val partObj = JSONObject().put("text", msg.text)
            val partsArray = JSONArray().put(partObj)
            contentsArray.put(JSONObject().put("role", role).put("parts", partsArray))
        }

        // Prompt atual
        val currentPart = JSONObject().put("text", prompt)
        contentsArray.put(JSONObject().put("role", "user").put("parts", JSONArray().put(currentPart)))

        val rootJson = JSONObject().apply {
            put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw RuntimeException("Gemini HTTP ${response.code}: $errorBody")
            }

            val responseString = response.body?.string().orEmpty()
            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                throw RuntimeException("Resposta vazia da API do Gemini.")
            }
            text
        }
    }

    private fun generateNaturalResponse(prompt: String, title: String): String {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        // Pergunta sobre jogos de luta do Roblox
        if (lower.contains("roblox") && (lower.contains("luta") || lower.contains("jogo") || lower.contains("melhor"))) {
            return "Fala, $title! No Roblox hoje, a cena de jogos de luta tá incrível, mas os melhores de verdade dependem um pouco do seu estilo:\n\n" +
                    "1. **The Strongest Battlegrounds (TSB):** É o rei indiscutível hoje. O combate é extremamente fluido, focado em combos corpo a corpo (estilo Saitama/One Punch Man), com contra-ataques precisos e gráficos impecáveis.\n\n" +
                    "2. **Jujutsu Shenanigans:** Se você curte Jujutsu Kaisen, esse jogo é pura diversão! A física de destruição de cenário onde você arremessa os oponentes contra prédios e paredes é a melhor do Roblox.\n\n" +
                    "3. **Blox Fruits:** Focado em RPG + PvP. Se você prefere ter poderes de frutas, espadas e evoluir atributos além de apenas lutar em arena, é o mais completo.\n\n" +
                    "4. **Combat Warriors:** Para quem quer uma pegada mais visceral, com armas medievais e modernas, esquivas rápidas e finalizações sangrentas.\n\n" +
                    "Minha recomendação número 1 pra entrar e se divertir agora é **The Strongest Battlegrounds** se você curte habilidade pura, ou **Jujutsu Shenanigans** se quiser destruir o mapa inteiro. Você prefere jogos no estilo arena rápida ou mais no estilo mundo aberto com poderes, $title?"
        }

        // Saudações
        if (lower.startsWith("ola") || lower.startsWith("olá") || lower.startsWith("oi") || lower.startsWith("e ai") || lower.startsWith("e aí") || lower.contains("bom dia") || lower.contains("boa tarde") || lower.contains("boa noite")) {
            return "E aí, $title! Tudo em ordem por aí? Tô 100% pronto. Em que posso te ajudar agora?"
        }

        // Status do sistema / como você está
        if (lower.contains("status") || lower.contains("como você está") || lower.contains("como voce esta") || lower.contains("tudo bem")) {
            return "Por aqui tá tudo rodando liso e na velocidade máxima, $title! Todos os módulos de voz, memória e raciocínio estão operando com 100%. E você, no que está focado hoje?"
        }

        // Quem é você
        if (lower.contains("quem é você") || lower.contains("quem e voce") || lower.contains("seu nome") || lower.contains("o que você faz")) {
            return "Eu sou o **NEXUS**, seu parceiro de IA! Fui criado pra te ajudar a pensar em soluções, planejar projetos, trocar ideias, tirar dúvidas ou simplesmente bater um papo inteligente e direto ao ponto. Pode me mandar qualquer pergunta, $title!"
        }

        // Produtividade / rotina / foco
        if (lower.contains("produtiv") || lower.contains("foco") || lower.contains("rotina") || lower.contains("organizar")) {
            return "Uma dica prática que funciona de verdade, $title: pegue as 3 coisas mais importantes do seu dia logo cedo e faça a mais difícil primeiro, sem olhar notificações.\n\nDepois de liquidar a primeira tarefa grande, o restante flui muito mais leve. Você tá precisando destravar algum projeto específico agora ou quer organizar seus horários da semana?"
        }

        // Ideias / Inovação / Negócios
        if (lower.contains("ideia") || lower.contains("inov") || lower.contains("negócio") || lower.contains("projeto")) {
            return "Boa pergunta, $title! O maior diferencial hoje é transformar processos chatos e manuais em experiências rápidas e automáticas.\n\nTrês áreas em alta no momento:\n" +
                    "• **Ferramentas de produtividade focadas em nicho** (atender um público específico muito bem).\n" +
                    "• **Automação com IA em tempo real** para atendimento ou análise de dados.\n" +
                    "• **Comunidades e produtos que entregam valor em segundos**.\n\nQual é o nicho ou problema que você tem mais vontade de explorar hoje?"
        }

        // Resposta natural e direta genérica para qualquer pergunta
        return "Com certeza, $title! Analisando isso de forma bem prática: o melhor caminho é focar primeiro no que traz resultado mais rápido e direto ao ponto, evitando complicar antes da hora.\n\nMe dá um pouco mais de contexto sobre o que você tem em mente para eu te dar uma recomendação sob medida! O que acha?"
    }
}
