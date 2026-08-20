package com.example.bookapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class TaziehIndexItem(
    val roleId: Long,
    val roleTitle: String,
    val firstVerse: String
)

/**
 * فهرست کل یک تعزیه: عنوان هر نقش به همراه بیت اول شعرش، برای اینکه کارگردان
 * یا کاربر بتواند سریع ببیند این تعزیه شامل چه نقش‌هایی است و با یک لمس
 * مستقیم به همان نقطه از متن برود.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaziehIndexScreen(
    taziehTitle: String,
    items: List<TaziehIndexItem>,
    onItemClick: (TaziehIndexItem) -> Unit,
    onExportPdf: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("فهرست: $taziehTitle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = onExportPdf) {
                        Icon(Icons.Filled.Share, contentDescription = "خروجی PDF کل تعزیه")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    onClick = { onItemClick(item) }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(item.roleTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (item.firstVerse.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                item.firstVerse,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
