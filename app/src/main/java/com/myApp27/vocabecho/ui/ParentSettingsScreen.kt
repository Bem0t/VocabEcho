package com.myApp27.vocabecho.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myApp27.vocabecho.R
import com.myApp27.vocabecho.ui.parent.ParentSettingsViewModel

private enum class IntervalOption(val label: String, val daysValue: Int) {
    MIN5("Сегодня", 0),
    DAY1("1 день", 1),
    DAY3("3 дня", 3)
}

@Composable
fun ParentSettingsScreen(
    onBack: () -> Unit
) {
    val vm: ParentSettingsViewModel = viewModel()
    val state by vm.state.collectAsState()

    // текущие выбранные варианты (по строковым полям из VM)
    val againSel = remember(state.againDays) { daysToOption(state.againDays) }
    val hardSel = remember(state.hardDays) { daysToOption(state.hardDays) }
    val easySel = remember(state.easyDays) { daysToOption(state.easyDays) }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.bg_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок в капсуле
            HeaderPill(text = "⚙️ Настройки")

            Spacer(Modifier.height(18.dp))

            // Центральная карточка
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEFFFFFFF)),
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(0.9f)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingRow(
                        emoji = "😣",
                        title = "Снова",
                        selected = againSel,
                        onSelect = { opt ->
                            vm.onAgainChanged(opt.daysValue.toString())
                            vm.save()
                        }
                    )

                    SettingRow(
                        emoji = "🙂",
                        title = "Сложно",
                        selected = hardSel,
                        onSelect = { opt ->
                            vm.onHardChanged(opt.daysValue.toString())
                            vm.save()
                        }
                    )

                    SettingRow(
                        emoji = "😄",
                        title = "Легко",
                        selected = easySel,
                        onSelect = { opt ->
                            vm.onEasyChanged(opt.daysValue.toString())
                            vm.save()
                        }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // (опционально) кнопка назад — если нужна в твоей навигации
            BackPill(text = "⬅️ Назад", onClick = onBack)
        }
    }
}

@Composable
private fun HeaderPill(text: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0B4AA2)
        )
    }
}

@Composable
private fun BackPill(text: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF)),
        modifier = Modifier
            .widthIn(max = 220.dp)
            .fillMaxWidth(0.6f)
            .height(46.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .clickableNoRipple(onClick)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0B4AA2)
            )
        }
    }
}

@Composable
private fun SettingRow(
    emoji: String,
    title: String,
    selected: IntervalOption,
    onSelect: (IntervalOption) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EEF8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B4AA2),
                    modifier = Modifier.weight(1f)
                )

                // выбранный интервал справа
                IntervalBadge(text = selected.label)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IntervalChip(
                    text = IntervalOption.MIN5.label,
                    selected = selected == IntervalOption.MIN5,
                    onClick = { onSelect(IntervalOption.MIN5) },
                    modifier = Modifier.weight(1f)
                )
                IntervalChip(
                    text = IntervalOption.DAY1.label,
                    selected = selected == IntervalOption.DAY1,
                    onClick = { onSelect(IntervalOption.DAY1) },
                    modifier = Modifier.weight(1f)
                )
                IntervalChip(
                    text = IntervalOption.DAY3.label,
                    selected = selected == IntervalOption.DAY3,
                    onClick = { onSelect(IntervalOption.DAY3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IntervalBadge(text: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0B4AA2)
        )
    }
}

@Composable
private fun IntervalChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Color(0xFFE2F3E6) else Color(0xFFFFFFFF)
    val fg = if (selected) Color(0xFF1E8E3E) else Color(0xFF0B4AA2)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = modifier
            .height(36.dp)
            .shadow(if (selected) 6.dp else 3.dp, RoundedCornerShape(14.dp))
            .clickableNoRipple(onClick)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontWeight = FontWeight.ExtraBold,
                color = fg
            )
        }
    }
}

private fun daysToOption(daysText: String): IntervalOption {
    val days = daysText.toIntOrNull() ?: 1
    return when (days) {
        0 -> IntervalOption.MIN5
        1 -> IntervalOption.DAY1
        3 -> IntervalOption.DAY3
        else -> IntervalOption.DAY1
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    clickable(
        indication = null,
        interactionSource = MutableInteractionSource()
    ) { onClick() }
