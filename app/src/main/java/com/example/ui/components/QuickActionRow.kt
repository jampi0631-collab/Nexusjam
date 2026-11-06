package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexusBorderCyan
import com.example.ui.theme.NexusCyan
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusTextSecondary

data class QuickAction(
    val id: String,
    val icon: String,
    val label: String,
    val prompt: String
)

@Composable
fun QuickActionRow(
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickAction("status", "⚡", "Status do Sistema", "Apresente um relatório de status do sistema e prontidão operacional."),
        QuickAction("plan", "🎯", "Plano Estratégico", "Elabore um plano estratégico tático para acelerar meus objetivos principais."),
        QuickAction("productivity", "🧠", "Produtividade Tática", "Como posso otimizar minha rotina de trabalho hoje para máximo rendimento?"),
        QuickAction("ideas", "💡", "Ideias Inovadoras", "Sintetize 3 ideias de vanguarda tecnológica e automação inteligente."),
        QuickAction("security", "🛡️", "Diretriz de Segurança", "Quais as melhores práticas defensivas para proteger dados e fluxos de trabalho?")
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        items(actions, key = { it.id }) { action ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NexusSurfaceDark.copy(alpha = 0.9f))
                    .border(1.dp, NexusBorderCyan, RoundedCornerShape(20.dp))
                    .clickable { onActionSelected(action.prompt) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("quick_action_${action.id}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = action.icon,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = NexusCyan,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
