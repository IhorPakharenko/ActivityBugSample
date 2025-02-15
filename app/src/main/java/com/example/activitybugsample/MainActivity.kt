package com.example.activitybugsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.activitybugsample.ui.theme.ActivityBugSampleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ActivityBugSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BugSample(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BugSample(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        val fakeId by remember { flow {
            repeat(1000) {
                emit(it)
                delay(1000)
            }
        }
        }.collectAsState(initial = 0)
        Box(Modifier.height(16.dp))
        // i is always updated
        NoKeyComposable(fakeId)
        Box(Modifier.height(16.dp))
        // i is always updated
        WorkaroundComposable(fakeId)
        Box(Modifier.height(16.dp))
        // i is updated unless Text is wrapped in Container
        BugComposable(fakeId)
    }
}

@Composable
private fun NoKeyComposable(i: Int) {
    Text(text = "$i")
    InlineContainer {
        Text(text = "$i")
    }
    Container {
        Text(text = "$i")
    }
    Container {
        Text(text = "$i")
    }
}

@Composable
private fun BugComposable(i: Int) {
    key(i) {
        // Even an empty key body causes this bug
    }
    Text(text = "$i")
    InlineContainer {
        Text(text = "$i")
    }
    Container {
        Text(text = "$i")
    }
    Container {
        Text(text = "$i")
    }
}

@Composable
private fun WorkaroundComposable(i: Int) {
    var iState by remember { mutableStateOf(i) }
    SideEffect {
        iState = i
    }
    key(iState) {

    }
    Text(text = "$i")
    InlineContainer {
        Text(text = "$i")
    }
    Container {
        Text(text = "$i")
    }
    Container {
        Text(text = "$iState")
    }
}

// Will not update i if key is present
@Composable
private fun Container(content: @Composable () -> Unit) {
    content()
}

// Will always update i
@Composable
inline fun InlineContainer(content: @Composable () -> Unit) {
    content()
}