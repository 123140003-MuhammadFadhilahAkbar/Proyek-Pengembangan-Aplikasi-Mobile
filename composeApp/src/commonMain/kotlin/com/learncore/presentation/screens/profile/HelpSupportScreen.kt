package com.learncore.presentation.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class FaqItem(val question: String, val answer: String)

private val FAQ_ITEMS = listOf(
    FaqItem(
        "Apa itu Matriks Eisenhower?",
        "Matriks Eisenhower adalah kerangka kerja produktivitas yang mengategorikan tugas ke dalam 4 kuadran berdasarkan urgensi dan tingkat kepentingan: Lakukan Pertama (mendesak & penting), Jadwalkan (penting tapi tidak mendesak), Delegasikan (mendesak tapi tidak penting), dan Eliminasi (tidak mendesak maupun penting)."
    ),
    FaqItem(
        "Bagaimana cara kerja Pengatur Waktu Pomodoro?",
        "Teknik Pomodoro membagi pekerjaan ke dalam sesi fokus (bawaan 25 menit) yang dipisahkan oleh istirahat singkat (bawaan 5 menit). Selesaikan satu sesi, ambil istirahat, lalu ulangi. Anda dapat menyesuaikan durasi ini di Profil → Preferensi Pomodoro."
    ),
    FaqItem(
        "Apakah data saya dicadangkan ke cloud?",
        "Tidak. LearnCore menyimpan semua tugas dan preferensi Anda secara lokal di perangkat menggunakan SQLite dan DataStore. Data Anda tidak pernah keluar dari perangkat Anda."
    ),
    FaqItem(
        "Bagaimana cara menggunakan Asisten AI?",
        "Ketuk tab AI di navigasi bawah. Anda dapat meminta bantuan untuk memprioritaskan tugas, memahami kuadran Eisenhower, atau tips produktivitas umum. Asisten ini menggunakan Gemini untuk memberikan jawaban."
    ),
    FaqItem(
        "Apakah saya bisa menghapus tugas secara permanen?",
        "Ya. Buka layar detail tugas dan gunakan opsi hapus. Tugas yang dihapus akan langsung dihapus saat itu juga dan tidak dapat dipulihkan."
    ),
    FaqItem(
        "Bagaimana cara mengubah durasi sesi Pomodoro?",
        "Buka halaman Profil, lalu pilih Preferensi Pomodoro. Di sana Anda dapat mengatur durasi sesi kerja, durasi istirahat pendek, dan durasi istirahat panjang sesuai kebutuhan Anda."
    ),
    FaqItem(
        "Apa perbedaan tiap kuadran Eisenhower?",
        "Kuadran 1 (Lakukan Pertama): tugas mendesak & penting, selesaikan segera. Kuadran 2 (Jadwalkan): penting tapi tidak mendesak, rencanakan waktunya. Kuadran 3 (Delegasikan): mendesak tapi tidak penting, bisa diserahkan ke orang lain. Kuadran 4 (Eliminasi): tidak mendesak & tidak penting, pertimbangkan untuk dihapus."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bantuan & Dukungan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner versi aplikasi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "LearnCore",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Versi 1.0.0 • Dibangun dengan Kotlin Multiplatform",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Label seksi FAQ
            SectionLabel("Pertanyaan yang Sering Diajukan")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    FAQ_ITEMS.forEachIndexed { index, faq ->
                        FaqRow(faq = faq)
                        if (index < FAQ_ITEMS.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }

            // Seksi tentang aplikasi
            SectionLabel("Tentang Aplikasi")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "LearnCore adalah aplikasi produktivitas yang menggabungkan Matriks Eisenhower dan teknik Pomodoro untuk membantu Anda fokus pada hal yang benar-benar penting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun FaqRow(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200))
        ) {
            Text(
                text = faq.answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}