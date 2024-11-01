package com.example.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nfcreader.ui.theme.NfcreaderTheme

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var tagContent by mutableStateOf("Place NFC tag near the device")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not supported on this device", Toast.LENGTH_LONG).show()
            return
        }

        setContent {
            NfcreaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = tagContent,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // Check if launched with an NFC intent
        intent?.let { processNfcIntent(it) }
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Toast.makeText(this, "NFC intent received", Toast.LENGTH_SHORT).show() // Debugging message
        processNfcIntent(intent)
    }

    private fun processNfcIntent(intent: Intent) {
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                readFromTag(tag)
            } else {
                tagContent = "NFC tag is empty or unreadable"
            }
        }
    }

    private fun readFromTag(tag: Tag?) {
        try {
            val ndef = Ndef.get(tag)
            ndef?.connect()
            val ndefMessage = ndef?.ndefMessage
            ndef?.close()

            tagContent = if (ndefMessage != null) {
                String(ndefMessage.records[0].payload).drop(3) // Drop the language code byte
            } else {
                "Empty NFC tag"
            }
        } catch (e: Exception) {
            tagContent = "Error reading NFC tag"
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "NFC Tag Content: $name",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NfcreaderTheme {
        Greeting("Hello NFC!")
    }
}
