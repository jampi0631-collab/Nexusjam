/**
 * NEXUS AI - Core Client Application
 * Compatível com Cloudflare Workers Static Assets & Web Speech APIs
 */

// Estado da Aplicação
const state = {
  core: 'IDLE', // 'IDLE', 'LISTENING', 'THINKING', 'SPEAKING'
  isThinking: false,
  isListening: false,
  isSpeaking: false,
  audioRms: 0,
  preferences: {
    userTitle: 'chefe',
    autoSpeak: true,
    speechRate: 1.0,
    speechPitch: 1.0,
    highThinking: true,
    personalityPreset: 'Estratégico & Tático'
  },
  messages: []
};

// Elementos DOM
const dom = {
  canvas: document.getElementById('energyCoreCanvas'),
  telemetryState: document.getElementById('telemetryState'),
  hudStatusChip: document.getElementById('hudStatusChip'),
  coreSubtext: document.getElementById('coreSubtext'),
  telemTitle: document.getElementById('telemTitle'),
  stopSpeechBtn: document.getElementById('stopSpeechBtn'),
  chatMessages: document.getElementById('chatMessages'),
  chatContainer: document.getElementById('chatContainer'),
  thinkingIndicator: document.getElementById('thinkingIndicator'),
  thinkingText: document.getElementById('thinkingText'),
  commandInput: document.getElementById('commandInput'),
  sendBtn: document.getElementById('sendBtn'),
  micBtn: document.getElementById('micBtn'),
  micIcon: document.getElementById('micIcon'),
  toggleAudioBtn: document.getElementById('toggleAudioBtn'),
  audioIcon: document.getElementById('audioIcon'),
  openClearModalBtn: document.getElementById('openClearModalBtn'),
  openSettingsModalBtn: document.getElementById('openSettingsModalBtn'),
  settingsModal: document.getElementById('settingsModal'),
  closeSettingsBtn: document.getElementById('closeSettingsBtn'),
  savePreferencesBtn: document.getElementById('savePreferencesBtn'),
  clearModal: document.getElementById('clearModal'),
  closeClearBtn: document.getElementById('closeClearBtn'),
  cancelClearBtn: document.getElementById('cancelClearBtn'),
  confirmClearBtn: document.getElementById('confirmClearBtn'),
  noticeToggle: document.getElementById('noticeToggle'),
  noticeBanner: document.getElementById('noticeBanner'),
  // Form fields
  prefUserTitle: document.getElementById('prefUserTitle'),
  prefAutoSpeak: document.getElementById('prefAutoSpeak'),
  prefHighThinking: document.getElementById('prefHighThinking'),
  prefSpeechRate: document.getElementById('prefSpeechRate'),
  speechRateVal: document.getElementById('speechRateVal'),
  prefSpeechPitch: document.getElementById('prefSpeechPitch'),
  speechPitchVal: document.getElementById('speechPitchVal'),
  presetOptions: document.getElementById('presetOptions')
};

// -------------------------------------------------------------
// Inicialização
// -------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
  loadPreferences();
  loadHistory();
  initCanvasCore();
  initSpeechRecognition();
  bindEvents();
  renderMessages();
});

// -------------------------------------------------------------
// Persistência em LocalStorage
// -------------------------------------------------------------
function loadPreferences() {
  try {
    const saved = localStorage.getItem('nexus_preferences');
    if (saved) {
      state.preferences = { ...state.preferences, ...JSON.parse(saved) };
    }
  } catch (e) {
    console.error('Erro ao ler preferências:', e);
  }
  updatePreferencesUI();
}

function savePreferences() {
  try {
    localStorage.setItem('nexus_preferences', JSON.stringify(state.preferences));
  } catch (e) {
    console.error('Erro ao salvar preferências:', e);
  }
  updatePreferencesUI();
}

function updatePreferencesUI() {
  dom.telemTitle.textContent = state.preferences.userTitle || 'chefe';
  dom.audioIcon.textContent = state.preferences.autoSpeak ? '🔊' : '🔇';

  dom.prefUserTitle.value = state.preferences.userTitle || 'chefe';
  dom.prefAutoSpeak.checked = state.preferences.autoSpeak;
  dom.prefHighThinking.checked = state.preferences.highThinking;
  dom.prefSpeechRate.value = state.preferences.speechRate;
  dom.speechRateVal.textContent = state.preferences.speechRate + 'x';
  dom.prefSpeechPitch.value = state.preferences.speechPitch;
  dom.speechPitchVal.textContent = state.preferences.speechPitch + 'x';

  document.querySelectorAll('.preset-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.preset === state.preferences.personalityPreset);
  });
}

function loadHistory() {
  try {
    const saved = localStorage.getItem('nexus_history');
    if (saved) {
      state.messages = JSON.parse(saved);
    }
  } catch (e) {
    console.error('Erro ao carregar histórico:', e);
  }

  if (state.messages.length === 0) {
    const title = state.preferences.userTitle || 'chefe';
    state.messages.push({
      id: Date.now(),
      sender: 'nexus',
      text: `NEXUS online e com 100% de integridade nos sistemas. Às suas ordens, ${title}. Como posso auxiliá-lo estrategicamente hoje?`,
      timestamp: Date.now()
    });
    saveHistory();
  }
}

function saveHistory() {
  try {
    localStorage.setItem('nexus_history', JSON.stringify(state.messages));
  } catch (e) {
    console.error('Erro ao salvar histórico:', e);
  }
}

// -------------------------------------------------------------
// Núcleo de Energia (Canvas 60fps)
// -------------------------------------------------------------
let canvasCtx = null;
let rotation = 0;
let counterRotation = 0;
let pulsePhase = 0;

function initCanvasCore() {
  if (!dom.canvas) return;
  canvasCtx = dom.canvas.getContext('2d');
  requestAnimationFrame(renderEnergyCore);
}

function setCoreState(newState) {
  state.core = newState;
  const labels = {
    IDLE: 'CORE // ONLINE',
    LISTENING: 'AUDIO // ESCUTANDO',
    THINKING: 'SÍNTESE // PENSANDO',
    SPEAKING: 'VOZ // TRANSMITINDO'
  };
  const subtext = {
    IDLE: `MATRIZ PRONTA // ${state.preferences.userTitle.toUpperCase()}`,
    LISTENING: 'CAPTANDO ONDA DE VOZ PT-BR',
    THINKING: 'PROCESSANDO MATRIZ QUÂNTICA',
    SPEAKING: 'SÍNTESE VOCAL ATIVA'
  };

  dom.telemetryState.textContent = labels[newState] || labels.IDLE;
  dom.hudStatusChip.textContent = labels[newState] || labels.IDLE;
  dom.coreSubtext.textContent = subtext[newState] || subtext.IDLE;

  const colorClass = newState.toLowerCase();
  dom.telemetryState.style.color = newState === 'THINKING' ? 'var(--nexus-indigo)' : (newState === 'LISTENING' ? 'var(--nexus-emerald)' : 'var(--nexus-cyan)');
}

function renderEnergyCore() {
  if (!canvasCtx || !dom.canvas) return;
  const ctx = canvasCtx;
  const width = dom.canvas.width;
  const height = dom.canvas.height;
  const cx = width / 2;
  const cy = height / 2;
  const baseR = Math.min(cx, cy) * 0.85;

  ctx.clearRect(0, 0, width, height);

  // Velocidade de rotação conforme estado
  const speed = state.core === 'THINKING' ? 0.05 : (state.core === 'SPEAKING' ? 0.025 : 0.012);
  rotation += speed;
  counterRotation -= speed * 1.3;
  pulsePhase += state.core === 'THINKING' ? 0.08 : 0.04;

  const pulse = Math.sin(pulsePhase) * 0.12 + 1;
  const primaryColor = state.core === 'THINKING' ? '#818cf8' : (state.core === 'LISTENING' ? '#10b981' : '#00f0ff');
  const secondaryColor = state.core === 'THINKING' ? '#00f0ff' : '#38bdf8';

  // 1. Onda Expansiva
  const waveScale = (Math.sin(pulsePhase * 0.8) + 1) * 0.5;
  ctx.save();
  ctx.beginPath();
  ctx.arc(cx, cy, baseR * (0.8 + waveScale * 0.3), 0, Math.PI * 2);
  ctx.strokeStyle = primaryColor;
  ctx.globalAlpha = (1 - waveScale) * 0.4;
  ctx.lineWidth = 2;
  ctx.stroke();
  ctx.restore();

  // 2. Núcleo Radiante Central
  const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, baseR * 0.5 * pulse);
  grad.addColorStop(0, '#ffffff');
  grad.addColorStop(0.3, primaryColor);
  grad.addColorStop(0.7, secondaryColor);
  grad.addColorStop(1, 'transparent');

  ctx.fillStyle = grad;
  ctx.beginPath();
  ctx.arc(cx, cy, baseR * 0.5 * pulse, 0, Math.PI * 2);
  ctx.fill();

  // 3. Anel Giroscópico Intermediário com Nós Orbitais (Contra-rotação)
  ctx.save();
  ctx.translate(cx, cy);
  ctx.rotate(counterRotation);

  const midR = baseR * 0.65;
  ctx.beginPath();
  ctx.arc(0, 0, midR, 0, Math.PI * 2);
  ctx.strokeStyle = secondaryColor;
  ctx.globalAlpha = 0.6;
  ctx.lineWidth = 1.8;
  ctx.stroke();

  // 4 nós orbitais
  for (let i = 0; i < 4; i++) {
    const angle = (i * Math.PI) / 2;
    const nx = Math.cos(angle) * midR;
    const ny = Math.sin(angle) * midR;

    ctx.beginPath();
    ctx.arc(nx, ny, 3.5, 0, Math.PI * 2);
    ctx.fillStyle = '#ffffff';
    ctx.fill();

    ctx.beginPath();
    ctx.arc(nx, ny, 6, 0, Math.PI * 2);
    ctx.strokeStyle = primaryColor;
    ctx.lineWidth = 1.5;
    ctx.stroke();
  }
  ctx.restore();

  // 4. Anel Segmentado Externo com Ranhuras Tecnológicas (Rotação)
  ctx.save();
  ctx.translate(cx, cy);
  ctx.rotate(rotation);

  const outerR = baseR * 0.92;
  const segments = 6;
  const sweep = (Math.PI * 2) / segments;

  for (let i = 0; i < segments; i++) {
    ctx.beginPath();
    ctx.arc(0, 0, outerR, i * sweep + 0.15, (i + 1) * sweep - 0.15);
    ctx.strokeStyle = primaryColor;
    ctx.globalAlpha = 0.85;
    ctx.lineWidth = 3;
    ctx.lineCap = 'round';
    ctx.stroke();
  }

  // Marcas de telemetria
  const ticks = 24;
  for (let i = 0; i < ticks; i++) {
    const angle = (i * Math.PI * 2) / ticks;
    const r1 = outerR - (i % 3 === 0 ? 8 : 4);
    const r2 = outerR + 2;

    ctx.beginPath();
    ctx.moveTo(Math.cos(angle) * r1, Math.sin(angle) * r1);
    ctx.lineTo(Math.cos(angle) * r2, Math.sin(angle) * r2);
    ctx.strokeStyle = secondaryColor;
    ctx.globalAlpha = i % 3 === 0 ? 0.8 : 0.35;
    ctx.lineWidth = i % 3 === 0 ? 2 : 1;
    ctx.stroke();
  }
  ctx.restore();

  requestAnimationFrame(renderEnergyCore);
}

// -------------------------------------------------------------
// Voz: Text to Speech (TTS) & Speech Recognition (STT)
// -------------------------------------------------------------
let recognition = null;

function initSpeechRecognition() {
  const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRec) {
    console.warn('Web Speech API indisponível neste navegador.');
    return;
  }

  recognition = new SpeechRec();
  recognition.lang = 'pt-BR';
  recognition.continuous = false;
  recognition.interimResults = false;

  recognition.onstart = () => {
    state.isListening = true;
    dom.micBtn.classList.add('active');
    setCoreState('LISTENING');
  };

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    if (transcript) {
      dom.commandInput.value = transcript;
      sendMessage(transcript);
    }
  };

  recognition.onerror = (event) => {
    console.warn('Erro de fala:', event.error);
    stopListening();
  };

  recognition.onend = () => {
    stopListening();
  };
}

function toggleListening() {
  if (state.isListening) {
    stopListening();
  } else {
    startListening();
  }
}

function startListening() {
  if (!recognition) {
    alert('Reconhecimento de voz não suportado neste navegador. Use a entrada de texto.');
    return;
  }
  stopSpeaking();
  try {
    recognition.start();
  } catch (e) {
    console.error('Falha ao iniciar reconhecimento:', e);
  }
}

function stopListening() {
  state.isListening = false;
  dom.micBtn.classList.remove('active');
  if (!state.isThinking && !state.isSpeaking) {
    setCoreState('IDLE');
  }
  try {
    recognition?.stop();
  } catch (e) {}
}

function speakText(text) {
  if (!('speechSynthesis' in window)) return;
  stopSpeaking();

  const clean = text.replace(/[*#_`]/g, '').replace(/\[NEXUS.*?\]/g, 'Nexus').trim();
  const utterance = new SpeechSynthesisUtterance(clean);
  utterance.lang = 'pt-BR';
  utterance.rate = state.preferences.speechRate || 1.0;
  utterance.pitch = state.preferences.speechPitch || 1.0;

  utterance.onstart = () => {
    state.isSpeaking = true;
    dom.stopSpeechBtn.style.display = 'block';
    setCoreState('SPEAKING');
  };

  utterance.onend = () => {
    state.isSpeaking = false;
    dom.stopSpeechBtn.style.display = 'none';
    if (!state.isThinking && !state.isListening) {
      setCoreState('IDLE');
    }
  };

  utterance.onerror = () => {
    state.isSpeaking = false;
    dom.stopSpeechBtn.style.display = 'none';
    setCoreState('IDLE');
  };

  window.speechSynthesis.speak(utterance);
}

function stopSpeaking() {
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
  state.isSpeaking = false;
  dom.stopSpeechBtn.style.display = 'none';
  if (!state.isThinking && !state.isListening) {
    setCoreState('IDLE');
  }
}

// -------------------------------------------------------------
// Envio e Processamento de Mensagens
// -------------------------------------------------------------
async function sendMessage(overrideText) {
  const text = (overrideText || dom.commandInput.value).trim();
  if (!text || state.isThinking) return;

  dom.commandInput.value = '';
  stopSpeaking();

  const userTitle = state.preferences.userTitle || 'chefe';

  // Inserir mensagem do usuário
  const userMsg = {
    id: Date.now(),
    sender: 'user',
    text: text,
    timestamp: Date.now()
  };
  state.messages.push(userMsg);
  saveHistory();
  renderMessages();

  // Ativar estado de pensamento
  state.isThinking = true;
  setCoreState('THINKING');
  dom.thinkingIndicator.style.display = 'flex';
  dom.thinkingText.textContent = state.preferences.highThinking ? 'Análise quântica de alta profundidade...' : 'Sintetizando resposta tática...';
  scrollToBottom();

  try {
    const response = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        prompt: text,
        history: state.messages,
        preferences: state.preferences
      })
    });

    const data = await response.json();
    const replyText = data.text || 'Diretriz processada, mas nenhuma resposta emitida.';

    // Criar placeholder para mensagem do NEXUS
    const nexusMsg = {
      id: Date.now() + 1,
      sender: 'nexus',
      text: '',
      timestamp: Date.now()
    };
    state.messages.push(nexusMsg);
    saveHistory();
    renderMessages();

    // Efeito de digitação tática progressiva
    await streamTypewriter(nexusMsg.id, replyText);

    state.isThinking = false;
    dom.thinkingIndicator.style.display = 'none';
    setCoreState('IDLE');

    // Fala automática caso ativa
    if (state.preferences.autoSpeak && replyText) {
      speakText(replyText);
    }

  } catch (err) {
    console.error('Erro na comunicação com o worker:', err);
    state.isThinking = false;
    dom.thinkingIndicator.style.display = 'none';
    setCoreState('IDLE');

    state.messages.push({
      id: Date.now() + 1,
      sender: 'nexus',
      text: `[NEXUS // ALERTA TÁTICO]\n\nFalha ao sintetizar resposta pela rede: ${err.message}. Verifique a rota do Worker.`,
      timestamp: Date.now()
    });
    saveHistory();
    renderMessages();
  }
}

async function streamTypewriter(msgId, fullText) {
  const words = fullText.split(' ');
  let current = '';

  for (const word of words) {
    current += word + ' ';
    const target = state.messages.find(m => m.id === msgId);
    if (target) {
      target.text = current;
      renderMessages();
      scrollToBottom();
    }
    await new Promise(r => setTimeout(r, 20));
  }
  saveHistory();
}

// -------------------------------------------------------------
// Renderização do Chat
// -------------------------------------------------------------
function renderMessages() {
  dom.chatMessages.innerHTML = '';
  const userTitle = state.preferences.userTitle || 'chefe';

  state.messages.forEach(msg => {
    const isUser = msg.sender === 'user';
    const timeStr = new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const tag = isUser ? `[${userTitle.toUpperCase()} // DIRETRIZ]` : '[NEXUS // SYS_OK]';

    const card = document.createElement('div');
    card.className = `chat-msg ${isUser ? 'user' : 'nexus'}`;

    card.innerHTML = `
      <div class="msg-header">
        <span class="msg-tag">${tag}</span>
        <span class="msg-time">${timeStr}</span>
      </div>
      <div class="msg-bubble">
        <div class="msg-content">${escapeHtml(msg.text)}</div>
        <div class="msg-actions">
          <button class="msg-btn copy-btn" title="Copiar texto">📋</button>
          <button class="msg-btn speak-btn" title="Ouvir em voz alta">🔊</button>
        </div>
      </div>
    `;

    card.querySelector('.copy-btn').addEventListener('click', () => {
      navigator.clipboard.writeText(msg.text);
    });

    card.querySelector('.speak-btn').addEventListener('click', () => {
      speakText(msg.text);
    });

    dom.chatMessages.appendChild(card);
  });
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function scrollToBottom() {
  dom.chatContainer.scrollTop = dom.chatContainer.scrollHeight;
}

// -------------------------------------------------------------
// Bind de Eventos
// -------------------------------------------------------------
function bindEvents() {
  dom.sendBtn.addEventListener('click', () => sendMessage());
  dom.micBtn.addEventListener('click', toggleListening);
  dom.stopSpeechBtn.addEventListener('click', stopSpeaking);

  dom.commandInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  // Alternar Áudio Automático
  dom.toggleAudioBtn.addEventListener('click', () => {
    state.preferences.autoSpeak = !state.preferences.autoSpeak;
    savePreferences();
  });

  // Modal de Limpeza
  dom.openClearModalBtn.addEventListener('click', () => {
    dom.clearModal.style.display = 'flex';
  });
  dom.closeClearBtn.addEventListener('click', () => {
    dom.clearModal.style.display = 'none';
  });
  dom.cancelClearBtn.addEventListener('click', () => {
    dom.clearModal.style.display = 'none';
  });
  dom.confirmClearBtn.addEventListener('click', () => {
    stopSpeaking();
    state.messages = [];
    loadHistory();
    renderMessages();
    dom.clearModal.style.display = 'none';
  });

  // Modal de Preferências
  dom.openSettingsModalBtn.addEventListener('click', () => {
    updatePreferencesUI();
    dom.settingsModal.style.display = 'flex';
  });
  dom.closeSettingsBtn.addEventListener('click', () => {
    dom.settingsModal.style.display = 'none';
  });

  dom.savePreferencesBtn.addEventListener('click', () => {
    state.preferences.userTitle = dom.prefUserTitle.value.trim() || 'chefe';
    state.preferences.autoSpeak = dom.prefAutoSpeak.checked;
    state.preferences.highThinking = dom.prefHighThinking.checked;
    state.preferences.speechRate = parseFloat(dom.prefSpeechRate.value);
    state.preferences.speechPitch = parseFloat(dom.prefSpeechPitch.value);
    savePreferences();
    renderMessages();
    dom.settingsModal.style.display = 'none';
  });

  // Sliders
  dom.prefSpeechRate.addEventListener('input', (e) => {
    dom.speechRateVal.textContent = e.target.value + 'x';
  });
  dom.prefSpeechPitch.addEventListener('input', (e) => {
    dom.speechPitchVal.textContent = e.target.value + 'x';
  });

  // Presets
  dom.presetOptions.addEventListener('click', (e) => {
    const btn = e.target.closest('.preset-btn');
    if (!btn) return;
    document.querySelectorAll('.preset-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    state.preferences.personalityPreset = btn.dataset.preset;
  });

  // Quick Action Chips
  document.querySelectorAll('.quick-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const prompt = chip.dataset.prompt;
      if (prompt) {
        sendMessage(prompt);
      }
    });
  });

  // Notice Banner
  dom.noticeToggle.addEventListener('click', () => {
    dom.noticeBanner.classList.toggle('open');
  });
}
