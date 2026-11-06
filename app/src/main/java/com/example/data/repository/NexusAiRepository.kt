package com.example.data.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.preferences.NexusUserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale

interface NexusAiRepository {
    fun generateNexusResponse(
        prompt: String,
        history: List<ChatMessageEntity>,
        preferences: NexusUserPreferences
    ): Flow<String>
}

class NexusAiRepositoryImpl : NexusAiRepository {

    override fun generateNexusResponse(
        prompt: String,
        history: List<ChatMessageEntity>,
        preferences: NexusUserPreferences
    ): Flow<String> = flow {
        val title = preferences.userTitle.ifBlank { "chefe" }
        val isHighThinking = preferences.highThinking
        val preset = preferences.personalityPreset

        // Simulate thinking latency depending on high-thinking mode
        if (isHighThinking) {
            delay(1200) // Deep neural simulation
        } else {
            delay(600)
        }

        val fullResponse = craftStrategicResponse(prompt, title, preset, isHighThinking)

        // Stream output in progressive character tokens for futuristic terminal typing effect
        val words = fullResponse.split(" ")
        val buffer = StringBuilder()

        for (word in words) {
            buffer.append(word).append(" ")
            emit(buffer.toString())
            delay(28) // Rapid cyberpunk typing cadence
        }
    }

    private fun craftStrategicResponse(
        prompt: String,
        title: String,
        preset: String,
        isHighThinking: Boolean
    ): String {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        val header = when {
            isHighThinking -> "[NEXUS // MATRIZ QUANTICA ATIVA]\n\n"
            else -> "[NEXUS // RESPOSTA TÁTICA]\n\n"
        }

        val thinkingFooter = if (isHighThinking) {
            "\n\n⚙️ *Parâmetros avaliados:* Análise multidimensional, probabilidade de sucesso: 98.4%, telemetria neural estável."
        } else ""

        return when {
            lower.contains("status") || lower.contains("sistema") || lower.contains("relatório") || lower.contains("relatorio") -> {
                "$header" +
                "Saudações, $title! Todos os módulos primários do NEXUS estão operando com 100% de integridade.\n\n" +
                "📊 **Telemetria do Sistema:**\n" +
                "• **Núcleo de Energia:** Estável (Frequência 432 THz - Fluxo Plasmático 99.8%)\n" +
                "• **Rede Neural:** Otimizada para raciocínio tático ($preset)\n" +
                "• **Canal de Voz:** Síntese em Português Brasileiro (pt-BR) pronta\n" +
                "• **Banco de Dados:** Memória persistente indexada via Room\n" +
                "• **Status da Missão:** Aguardando suas coordenadas estratégicas, $title." +
                thinkingFooter
            }

            lower.contains("ola") || lower.contains("olá") || lower.contains("oi") || lower.contains("bom dia") || lower.contains("boa tarde") || lower.contains("boa noite") -> {
                "$header" +
                "Às suas ordens, $title. Como posso potencializar suas decisões estratégicas hoje?\n\n" +
                "Estou pronto para formular planos, sintetizar relatórios táticos, revisar ideias ou auxiliá-lo no comando de suas operações." +
                thinkingFooter
            }

            lower.contains("quem é você") || lower.contains("quem e voce") || lower.contains("seu nome") || lower.contains("o que é") || lower.contains("o que e") -> {
                "$header" +
                "Eu sou **NEXUS**, seu assistente de inteligência artificial futurista de alta patente.\n\n" +
                "Fui projetado para ser seu braço direito estratégico: analítico, calculista, leal e extremamente educado. Minha missão central é servir ao meu criador — você, $title — fornecendo lógica avançada, suporte verbal e gestão integrada de objetivos." +
                thinkingFooter
            }

            lower.contains("plano") || lower.contains("estratégia") || lower.contains("estrategia") || lower.contains("projeto") || lower.contains("meta") -> {
                "$header" +
                "Entendido, $title. Analisando as variáveis de '$prompt' através da matriz heurística:\n\n" +
                "🎯 **1. Diagnóstico Primário:**\n" +
                "Identificação de pontos de alavancagem rápida e neutralização de riscos periféricos.\n\n" +
                "⚡ **2. Plano de Ação Estruturado:**\n" +
                "• *Fase Alfa:* Mapeamento de recursos e definição do objetivo central.\n" +
                "• *Fase Beta:* Execução sincronizada em pequenos blocos de alta tração.\n" +
                "• *Fase Gama:* Validação métrica contínua e correção de rumo.\n\n" +
                "🛡️ **3. Diretriz do NEXUS:**\n" +
                "Priorize o vetor de maior impacto primeiro, $title. Deseja que eu elabore o cronograma de contingência?" +
                thinkingFooter
            }

            lower.contains("ideia") || lower.contains("inov") || lower.contains("criativ") -> {
                "$header" +
                "Conectando sinapses conceituais, $title. Eis três diretrizes de alto calibre para você:\n\n" +
                "💡 **Proposta A (Vanguarda Tecnológica):** Automatize a coleta de dados de rotina para liberar 80% do seu foco criativo.\n" +
                "💡 **Proposta B (Diferencial Assimétrico):** Crie soluções modulares onde cada componente alimenta o seguinte em sinergia.\n" +
                "💡 **Proposta C (Impacto Imediato):** Simplifique a interface de entrega — complexidade nos bastidores, elegância na ponta.\n\n" +
                "Qual desses vetores devemos aprofundar, $title?" +
                thinkingFooter
            }

            lower.contains("ajuda") || lower.contains("comandos") || lower.contains("o que você faz") || lower.contains("o que voce faz") -> {
                "$header" +
                "Posso operar em múltiplas frentes, $title:\n\n" +
                "1. **Comando de Voz & Áudio:** Falo em tempo real e estou preparado para transcrição por microfone.\n" +
                "2. **Análise Tática:** Reestruturação de problemas complexos em etapas executáveis.\n" +
                "3. **Memória Persistente:** Histórico gravado localmente com backup seguro e opção de purga.\n" +
                "4. **Ajuste de Parâmetros:** Controle de velocidade de voz, tom e modo de raciocínio profundo.\n\n" +
                "Basta digitar ou ditar sua próxima ordem, $title." +
                thinkingFooter
            }

            else -> {
                "$header" +
                "Recebido e decodificado, $title. Sobre a sua solicitação:\n\n" +
                "\"$prompt\"\n\n" +
                "Analisando todos os vetores táticos relevantes, recomendo avançarmos com firmeza. Minha matriz sintetizou que a abordagem mais eficiente é manter a clareza de execução, mitigar atritos e focar no resultado final pretendido.\n\n" +
                "Permaneço em prontidão para os próximos passos ou para detalhar qualquer subseção desta diretriz, $title." +
                thinkingFooter
            }
        }
    }
}
