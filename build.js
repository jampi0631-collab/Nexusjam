import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const publicDir = path.join(__dirname, 'public');
const distDir = path.join(__dirname, 'dist');

console.log('⚡ [NEXUS AI BUILD] Iniciando compilação de assets estáticos...');

// 1. Garantir que o diretório ./dist exista
if (!fs.existsSync(distDir)) {
  fs.mkdirSync(distDir, { recursive: true });
}

// 2. Copiar arquivos de public/ para dist/
const filesToCopy = ['index.html', 'styles.css', 'app.js'];

for (const file of filesToCopy) {
  const src = path.join(publicDir, file);
  const dest = path.join(distDir, file);
  if (fs.existsSync(src)) {
    fs.copyFileSync(src, dest);
    console.log(`✓ Copiado: ${file} -> dist/${file}`);
  } else {
    console.warn(`! Arquivo não encontrado: ${src}`);
  }
}

// 3. Copiar ícone do NEXUS se disponível
const iconCandidates = [
  path.join(__dirname, 'app/src/main/res/drawable/nexus_icon_1793980497411.jpg'),
  path.join(__dirname, 'app/src/main/res/mipmap-xxxhdpi/ic_launcher.png')
];

let iconCopied = false;
for (const iconPath of iconCandidates) {
  if (fs.existsSync(iconPath)) {
    fs.copyFileSync(iconPath, path.join(distDir, 'favicon.ico'));
    fs.copyFileSync(iconPath, path.join(distDir, 'nexus_icon.jpg'));
    console.log(`✓ Ícone copiado para dist/favicon.ico`);
    iconCopied = true;
    break;
  }
}

console.log('✨ [NEXUS AI BUILD] Build concluído com sucesso em ./dist!');
