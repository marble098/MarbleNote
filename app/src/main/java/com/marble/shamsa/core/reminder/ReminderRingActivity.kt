package com.marble.shamsa.core.reminder

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.marble.shamsa.R
import com.marble.shamsa.core.data.ReminderRepository
import com.marble.shamsa.core.design.ShamsaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderRingActivity : AppCompatActivity() {
    @Inject lateinit var repository: ReminderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(ReminderScheduler.EXTRA_ID) ?: run { finish(); return }
        setContent {
            ShamsaTheme {
                var title by remember { mutableStateOf(getString(R.string.ring_title)) }
                val scope = rememberCoroutineScope()
                LaunchedEffect(id) { repository.get(id)?.let { title = it.title } }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface.copy(alpha = .97f)) {
                    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Rounded.Alarm, null, Modifier.size(88.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { scope.launch { repository.complete(id); finish() } }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.complete)) }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { scope.launch { repository.snooze(id); finish() } }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.snooze)) }
                    }
                }
            }
        }
    }
}
