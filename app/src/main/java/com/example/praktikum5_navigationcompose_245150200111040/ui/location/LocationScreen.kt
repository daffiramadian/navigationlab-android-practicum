package com.example.praktikum5_navigationcompose_245150200111040.ui.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    uiState: LocationUiState,
    onRequestLocation: () -> Unit,
    onPermissionDenied: (Boolean) -> Unit,
    onPermissionRestored: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = hasLocationPermission(context)
                hasPermission = granted
                if (granted) {
                    onPermissionRestored()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val granted = coarse || fine || hasLocationPermission(context)
        hasPermission = granted
        if (granted) {
            onRequestLocation()
        } else {
            val activity = context as? Activity
            val canShowRationale = activity != null && (
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            onPermissionDenied(!canShowRationale)
        }
    }

    val launchPermissionDialog = {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lokasi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (uiState) {
                LocationUiState.Idle,
                LocationUiState.Loading,
                is LocationUiState.Error -> {
                    Button(
                        onClick = {
                            if (hasPermission) onRequestLocation() else launchPermissionDialog()
                        },
                        enabled = uiState !is LocationUiState.Loading
                    ) {
                        Text(
                            if (uiState is LocationUiState.Error && hasPermission) {
                                "Perbarui Lokasi"
                            } else {
                                "Ambil Lokasi Saya"
                            }
                        )
                    }

                    when (uiState) {
                        LocationUiState.Idle -> Text("Lokasi belum diminta")
                        LocationUiState.Loading -> CircularProgressIndicator()
                        is LocationUiState.Error -> Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        else -> Unit
                    }
                }

                is LocationUiState.Success -> {
                    val accessLabel = if (uiState.location.isPrecise) "Precise" else "Approximate"
                    val updatedTime = remember(uiState.location.updatedAtMillis) {
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date(uiState.location.updatedAtMillis))
                    }

                    Text(
                        text = "Jenis Akses: $accessLabel",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Latitude: ${uiState.location.latitude}")
                    Text("Longitude: ${uiState.location.longitude}")
                    Text("Accuracy: ${uiState.location.accuracy ?: 0f} m")
                    Text("Terakhir diperbarui: $updatedTime")

                    Button(onClick = {
                        if (hasPermission) onRequestLocation() else launchPermissionDialog()
                    }) {
                        Text("Perbarui Lokasi")
                    }
                }

                is LocationUiState.PermissionDenied -> {
                    Text(
                        text = "Izin lokasi ditolak. Aplikasi memerlukan izin lokasi (Approximate atau Precise) untuk mengambil posisi saat ini.",
                        color = MaterialTheme.colorScheme.error
                    )

                    if (uiState.permanentlyDenied) {
                        Text(
                            text = "Dialog izin tidak lagi ditampilkan oleh sistem karena ditolak permanen. Aktifkan izin Location melalui Pengaturan Aplikasi.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(onClick = launchPermissionDialog) {
                        Text("Minta Izin Lagi")
                    }

                    if (uiState.permanentlyDenied) {
                        OutlinedButton(onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }) {
                            Text("Buka Pengaturan")
                        }
                    }
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
