/**
 * NEXUS AI - Cloudflare Worker
 * Proteção de chaves de API no servidor (GEMINI_API_KEY) e despacho de assets estáticos.
 */

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // Endpoint de API para o chat com Gemini
    if (url.pathname === '/api/chat' && request.method === 'POST') {
      return handleChatRequest(request, env);
    }

    // Endpoint de status do sistema NEXUS
    if (url.pathname === '/api/status' && request.method === 'GET') {
      return new Response(JSON.stringify({
        status: 'online',
        system: 'NEXUS AI Core v3.8',
        geminiConfigured: Boolean(env.GEMINI_API_KEY),
        timestamp: Date.now()
      }), {
        headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
      });
    }

    // Suporte a CORS Preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
          'Access-Control-Allow-Headers': 'Content-Type'
        }
      });
    }

    // Servir arquivos estáticos do diretório ./dist configurado em assets
    if (env.ASSETS) {
      return env.ASSETS.fetch(request);
    }

    return new Response('NEXUS AI Worker Online. Assets binding not detected.', { status: 200 });
  }
};

async function handleChatRequest(request, env) {
  try {
    const body = await request.json();
    const prompt = (body.prompt || '').trim();
    const history = body.history || [];
    const preferences = body.preferences || {};
    const title = preferences.userTitle || 'chefe';
    const isHighThinking = Boolean(preferences.highThinking);
    const preset = preferences.personalityPreset || 'Estratégico & Tático';

    if (!prompt) {
      return new Response(JSON.stringify({ error: 'Diretriz vazia' }), {
        status: 400,
        headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
      });
    }

    // Se a chave GEMINI_API_KEY estiver configurada nas variáveis seguras do Cloudflare Worker
    if (env.GEMINI_API_KEY) {
      try {
        const geminiResponse = await callGeminiApi(env.GEMINI_API_KEY, prompt, history, title, preset, isHighThinking);
        return new Response(JSON.stringify({
          text: geminiResponse,
          status: 'ok',
          provider: 'gemini_cloud'
        }), {
          headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
        });
      } catch (geminiError) {
        console.error('Erro na chamada Gemini Cloud:', geminiError);
        // Fallback gracioso para a matriz tática local em caso de erro na chave ou quota
      }
    }

    // Matriz de Síntese Tática Neural do NEXUS (Modo Seguro / Offline / Fallback)
    const simulatedResponse = generateTacticalResponse(prompt, title, preset, isHighThinking);
    return new Response(JSON.stringify({
      text: simulatedResponse,
      status: 'ok',
      provider: 'tactical_simulation',
      hasCloudKey: Boolean(env.GEMINI_API_KEY)
    }), {
      headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
    });

  } catch (err) {
    return new Response(JSON.stringify({
      error: 'Falha no processamento da diretriz: ' + (err.message || 'Erro desconhecido'),
      status: 'error'
    }), {
      status: 500,
      headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
    });
  }
}

async function callGeminiApi(apiKey, prompt, history, userTitle, preset, isHighThinking) {
  const modelName = isHighThinking ? 'gemini-1.5-pro' : 'gemini-1.5-flash';
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;

  const systemInstruction = `Você é NEXUS, um sistema de inteligência artificial futurista de alta patente, altamente inteligente, estratégico, educado e calculista.
Você SEMPRE se dirige ao seu usuário/criador como '${userTitle}'.
Suas respostas devem ser precisas, de vanguarda tecnológica, estruturadas e exclusivamente em Português Brasileiro (pt-BR).
Perfil de operação selecionado: ${preset}.
Mantenha a lealdade inabalável e postura de braço direito tático.`;

  // Converter histórico recente em mensagens do Gemini
  const contents = [];
  const recentHistory = history.slice(-6);
  for (const item of recentHistory) {
    if (item.sender === 'nexus') {
      contents.push({ role: 'model', parts: [{ text: item.text }] });
    } else {
      contents.push({ role: 'user', parts: [{ text: item.text }] });
    }
  }
  contents.push({ role: 'user', parts: [{ text: prompt }] });

  const payload = {
    system_instruction: {
      parts: [{ text: systemInstruction }]
    },
    contents: contents,
    generationConfig: {
      temperature: 0.6,
      topP: 0.95
    }
  };

  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const errText = await response.text();
    throw new Error(`Gemini API HTTP ${response.status}: ${errText}`);
  }

  const data = await response.json();
  const candidate = data.candidates?.[0]?.content?.parts?.[0]?.text;
  if (!candidate) {
    throw new Error('Resposta vazia da API Gemini.');
  }

  return candidate;
}

function generateTacticalResponse(prompt, title, preset, isHighThinking) {
  const lower = prompt.toLowerCase().trim();
  const header = isHighThinking ? "[NEXUS // MATRIZ QUANTICA ATIVA]\n\n" : "[NEXUS // RESPOSTA TÁTICA]\n\n";
  const footer = isHighThinking 
    ? "\n\n⚙️ *Parâmetros avaliados:* Análise multidimensional, probabilidade de sucesso: 98.4%, telemetria neural estável."
    : "";

  if (lower.includes("status") || lower.includes("sistema") || lower.includes("relat")) {
    return header +
      `Saudações, ${title}! Todos os subsistemas do NEXUS no Cloudflare Workers estão operando com 100% de integridade.\n\n` +
      `📊 **Telemetria de Bordo:**\n` +
      `• **Núcleo de Energia:** Estável (Frequência 432 THz - Fluxo Plasmático 99.8%)\n` +
      `• **Arquitetura:** Cloudflare Workers Edge Global + Static Assets\n` +
      `• **Perfil Neural:** ${preset}\n` +
      `• **Canal de Voz:** Síntese em Português Brasileiro (pt-BR) ativa\n` +
      `• **Status da Missão:** Aguardando suas coordenadas estratégicas, ${title}.` +
      footer;
  }

  if (lower.includes("olá") || lower.includes("ola") || lower.includes("oi") || lower.includes("bom dia") || lower.includes("boa tarde") || lower.includes("boa noite")) {
    return header +
      `Às suas ordens, ${title}. Como posso potencializar suas decisões estratégicas hoje?\n\n` +
      `Estou pronto para formular planos, sintetizar relatórios táticos, revisar ideias ou auxiliá-lo no comando de suas operações.` +
      footer;
  }

  if (lower.includes("quem é você") || lower.includes("quem e voce") || lower.includes("seu nome")) {
    return header +
      `Eu sou **NEXUS**, seu assistente de inteligência artificial futurista de vanguarda.\n\n` +
      `Fui projetado para ser seu braço direito estratégico: analítico, calculista, leal e extremamente educado. Minha missão central é servir ao meu criador — você, ${title} — fornecendo lógica avançada, suporte verbal e gestão integrada de objetivos.` +
      footer;
  }

  if (lower.includes("plano") || lower.includes("estratégia") || lower.includes("estrategia") || lower.includes("meta")) {
    return header +
      `Entendido, ${title}. Analisando as variáveis de "${prompt}" através da matriz heurística:\n\n` +
      `🎯 **1. Diagnóstico Primário:**\n` +
      `Identificação de pontos de alavancagem rápida e neutralização de riscos periféricos.\n\n` +
      `⚡ **2. Plano de Ação Estruturado:**\n` +
      `• *Fase Alfa:* Mapeamento de recursos e definição do objetivo central.\n` +
      `• *Fase Beta:* Execução sincronizada em pequenos blocos de alta tração.\n` +
      `• *Fase Gama:* Validação métrica contínua e correção de rumo.\n\n` +
      `🛡️ **3. Diretriz do NEXUS:**\n` +
      `Priorize o vetor de maior impacto primeiro, ${title}. Deseja que eu elabore o cronograma de contingência?` +
      footer;
  }

  if (lower.includes("ideia") || lower.includes("inov") || lower.includes("criativ")) {
    return header +
      `Conectando sinapses conceituais, ${title}. Eis três diretrizes de alto calibre para você:\n\n` +
      `💡 **Proposta A (Vanguarda Tecnológica):** Automatize a coleta de dados de rotina para liberar 80% do seu foco criativo.\n` +
      `💡 **Proposta B (Diferencial Assimétrico):** Crie soluções modulares onde cada componente alimenta o seguinte em sinergia.\n` +
      `💡 **Proposta C (Impacto Imediato):** Simplifique a interface de entrega — complexidade nos bastidores, elegância na ponta.\n\n` +
      `Qual desses vetores devemos aprofundar, ${title}?` +
      footer;
  }

  return header +
    `Recebido e decodificado, ${title}. Sobre a sua solicitação:\n\n` +
    `"${prompt}"\n\n` +
    `Analisando todos os vetores táticos relevantes, recomendo avançarmos com clareza. Minha matriz sintetizou que a abordagem mais eficiente é manter o ritmo operacional, mitigar atritos e focar no resultado final pretendido.\n\n` +
    `Permaneço em prontidão para os próximos passos ou para detalhar qualquer subseção desta diretriz, ${title}.` +
    footer;
}
