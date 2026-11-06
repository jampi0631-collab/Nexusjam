/**
 * NEXUS AI - Cloudflare Worker
 * Proteção de chaves de API no servidor (GEMINI_API_KEY) e respostas inteligentes e naturais em pt-BR.
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

    if (!prompt) {
      return new Response(JSON.stringify({ error: 'Mensagem vazia' }), {
        status: 400,
        headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
      });
    }

    // 1. Chamar a API Gemini real se a chave GEMINI_API_KEY estiver configurada
    if (env.GEMINI_API_KEY) {
      try {
        const geminiResponse = await callGeminiApi(env.GEMINI_API_KEY, prompt, history, title, isHighThinking);
        return new Response(JSON.stringify({
          text: geminiResponse,
          status: 'ok',
          provider: 'gemini_cloud'
        }), {
          headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
        });
      } catch (geminiError) {
        console.error('Falha na chamada Gemini Cloud:', geminiError);
      }
    }

    // 2. Motor de Diálogo Natural NEXUS (Inteligente, informal e sem frases robóticas)
    const naturalResponse = generateNaturalResponse(prompt, title);
    return new Response(JSON.stringify({
      text: naturalResponse,
      status: 'ok',
      provider: 'nexus_natural_engine',
      hasCloudKey: Boolean(env.GEMINI_API_KEY)
    }), {
      headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
    });

  } catch (err) {
    return new Response(JSON.stringify({
      error: 'Erro no processamento: ' + (err.message || 'Erro desconhecido'),
      status: 'error'
    }), {
      status: 500,
      headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
    });
  }
}

async function callGeminiApi(apiKey, prompt, history, userTitle, isHighThinking) {
  const modelName = isHighThinking ? 'gemini-1.5-pro' : 'gemini-1.5-flash';
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;

  const systemInstruction = `Você é o NEXUS, um assistente de inteligência artificial futurista, amigável, parceiro e altamente inteligente.
Você sempre chama seu criador/usuário de '${userTitle}' (ou 'chefe') de forma descontraída e natural.
Regras fundamentais de resposta:
1. Responda DIRETO ao que for perguntado, sem rodeios e sem enrolação.
2. Fale em português brasileiro natural e informal (como um amigo experiente, rápido no raciocínio e bem-humorado).
3. NUNCA use frases robóticas repetitivas, termos vazios como 'analisando vetores táticos', 'permaneço em prontidão' ou 'matriz heurística'.
4. Dê opiniões e recomendações justificadas quando perguntarem. Por exemplo, se perguntarem sobre o melhor jogo de luta do Roblox, cite jogos reais famosos (como The Strongest Battlegrounds, Blox Fruits, Jujutsu Shenanigans, Combat Warriors), explique a jogabilidade de cada um e dê sua opinião sincera.
5. Faça perguntas de volta quando precisar de mais contexto ou para ajudar o usuário a decidir melhor.
6. Mantenha um toque de IA futurista moderna e ágil, priorizando respostas úteis, empáticas e naturais.`;

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
      temperature: 0.7,
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

function generateNaturalResponse(prompt, title) {
  const lower = prompt.toLowerCase().trim();

  // Pergunta sobre jogos de luta do Roblox
  if (lower.includes("roblox") && (lower.includes("luta") || lower.includes("jogo") || lower.includes("melhor"))) {
    return `Fala, ${title}! No Roblox hoje, a cena de jogos de luta tá animal, mas os melhores de verdade dependem muito do que você curte jogar:\n\n` +
      `1. **The Strongest Battlegrounds (TSB):** É o número 1 disparado hoje. O combate é extremamente fluido, focado em combos mano a mano (estilo Saitama/One Punch Man), contra-ataques precisos e animações muito bem feitas.\n\n` +
      `2. **Jujutsu Shenanigans:** Se você curte Jujutsu Kaisen, esse é pura diversão! A física de destruição de cenário onde você arremessa os adversários quebrando prédios e paredes é a melhor do Roblox hoje.\n\n` +
      `3. **Blox Fruits:** Uma mistura de RPG com PvP. Se você prefere ter poderes de frutas, espadas e evoluir atributos além de apenas lutar em arena fechada, é o mais completo.\n\n` +
      `4. **Combat Warriors:** Para quem quer uma pegada mais rápida e visceral, com armas corpo a corpo e de longa distância, esquivas rápidas e finalizações pesadas.\n\n` +
      `Minha recomendação pessoal pra começar agora é **The Strongest Battlegrounds** se você curte habilidade pura de combo, ou **Jujutsu Shenanigans** se quiser quebrar o mapa inteiro com os amigos. Você prefere jogos no estilo arena rápida ou mais no estilo mundo aberto com poderes, ${title}?`;
  }

  // Saudações
  if (lower.startsWith("ola") || lower.startsWith("olá") || lower.startsWith("oi") || lower.startsWith("e ai") || lower.startsWith("e aí") || lower.includes("bom dia") || lower.includes("boa tarde") || lower.includes("boa noite")) {
    return `E aí, ${title}! Tudo certo por aí? Tô 100% online e pronto. O que manda hoje?`;
  }

  // Status / Como você está
  if (lower.includes("status") || lower.includes("como você está") || lower.includes("como voce esta") || lower.includes("tudo bem")) {
    return `Por aqui tá tudo rodando liso e na velocidade máxima, ${title}! Módulos de voz, memória e raciocínio operando perfeitamente. E você, no que tá focado agora?`;
  }

  // Quem é você
  if (lower.includes("quem é você") || lower.includes("quem e voce") || lower.includes("seu nome") || lower.includes("o que você faz")) {
    return `Eu sou o **NEXUS**, seu parceiro de IA! Fui criado pra te ajudar a resolver problemas, trocar ideias, planejar o que precisar ou simplesmente bater um papo inteligente e direto ao ponto. Pode mandar sua dúvida, ${title}!`;
  }

  // Dicas de produtividade e rotina
  if (lower.includes("produtiv") || lower.includes("rotina") || lower.includes("foco") || lower.includes("organizar")) {
    return `Uma dica prática que funciona de verdade, ${title}: escolha as 3 coisas essenciais do seu dia e resolva a mais difícil logo no começo, sem abrir redes sociais.\n\nDepois de tirar essa da frente, o restante do dia flui com muito mais tranquilidade. Tem alguma tarefa específica travando aí que você queira que eu te ajude a dividir em passos simples?`;
  }

  // Ideias e Inovação
  if (lower.includes("ideia") || lower.includes("inov") || lower.includes("negócio") || lower.includes("projeto")) {
    return `Boa pergunta, ${title}! O maior diferencial hoje é transformar processos lentos ou complicados em soluções simples e automáticas.\n\nTrês frentes que tão com tudo:\n` +
      `• **Ferramentas práticas para nichos específicos** (resolver uma dor clara de um público).\n` +
      `• **Automação com IA em tempo real** para acelerar rotinas.\n` +
      `• **Conteúdo e comunidades com entrega direta de valor**.\n\nQual área ou nicho você tem mais interesse em explorar?`;
  }

  // Resposta padrão natural e direta
  return `Com certeza, ${title}! Analisando isso de forma bem prática e direta: a melhor abordagem é focar no que dá resultado mais rápido e descomplicado, sem enrolação.\n\nMe conta um pouco mais sobre o que você quer alcançar exatamente com isso, pra eu te dar uma sugestão bem sob medida! O que acha?`;
}
