# NavigationLab: Navigation, Architecture, REST API, Coroutines & Flow, DataStore, Room & Location

Repository ini berisi proyek praktikum mata kuliah **Pemrograman Aplikasi Perangkat Bergerak (CIF62041)**, Fakultas Ilmu Komputer, Universitas Brawijaya.

NavigationLab dikembangkan bertahap melalui Modul Praktikum 05-10: navigasi, arsitektur berlapis, REST API, Kotlin Coroutines dan Flow, penyimpanan data lokal (DataStore & Room), serta Location Services dengan runtime permission.

## Modul Praktikum

### Modul 05 - Navigation Compose
Navigasi antarhalaman dengan empat destination: Home, Detail, Profile, dan About. Navigasi dikelola dengan `NavController` dan `popBackStack()`.

### Modul 06 - Layered Architecture dan MVVM
Memisahkan aplikasi menjadi UI Layer, ViewModel, Repository, dan Data Source. Data masih in-memory.

```text
UI Layer -> ViewModel -> Repository -> StudentLocalDataSource
```

### Modul 07 - REST API dan Retrofit
Mengganti data in-memory dengan REST API DummyJSON menggunakan Retrofit dan Gson. Komunikasi asynchronous masih memakai callback `Call.enqueue()`.

### Modul 08 - Kotlin Coroutines dan Flow
Refactoring callback menjadi Coroutines dan Flow.

- API service, remote data source, dan repository menggunakan `suspend function`.
- Operasi asynchronous dijalankan lewat `viewModelScope.launch`.
- UI state dikelola dengan `MutableStateFlow` dan `StateFlow`.
- State dikonsumsi dengan `collectAsStateWithLifecycle()`.
- Pencarian reaktif dengan `debounce(500)`, `distinctUntilChanged()`, dan `collectLatest()`.
- Fitur Refresh dan Retry mengikuti query aktif.
- `CancellationException` dilempar kembali agar pembatalan tidak dianggap error.

### Modul 09 - Penyimpanan Data Lokal dengan DataStore dan Room
Menambahkan dua bentuk penyimpanan lokal tanpa mengubah arsitektur UI - ViewModel - Repository.

- **Preferences DataStore** menyimpan `lastSearchQuery` sehingga query pencarian terakhir dipulihkan setelah aplikasi ditutup dan dibuka kembali.
- **Room** (Entity, DAO, Database) menyimpan daftar mahasiswa favorit.
- Plugin **KSP** digunakan sebagai annotation processor Room.
- `FavoriteRepository` menyembunyikan DAO dari ViewModel dan UI.
- `DetailViewModel` mengamati status favorit lewat `Flow`, sehingga tombol favorit berubah reaktif.
- `FavoritesScreen` menampilkan seluruh favorit dari Room dan ikut berubah otomatis saat favorit dihapus dari Detail.
- Satu preferensi tambahan di DataStore (`sortAscending`) untuk urutan daftar favorit, tetap tersimpan setelah restart.

**Alur arsitektur Modul 09:**

```text
Compose UI
    | collectAsStateWithLifecycle()
    v
ViewModel (Home / Detail / Favorites)
    | viewModelScope.launch, StateFlow
    v
Repository
    |-- UserPreferencesRepository --> Preferences DataStore
    |-- FavoriteRepository --------> Room (DAO + AppDatabase)
    |-- StudentRepository ---------> Remote Data Source --> DummyJSON
```

**Pembagian peran penyimpanan:**

| Penyimpanan | Digunakan untuk | Alasan |
|---|---|---|
| Preferences DataStore | `lastSearchQuery`, `sortAscending` | Data kecil berbentuk key-value |
| Room | Mahasiswa favorit | Data terstruktur, perlu query dan observasi perubahan |

### Modul 10 - Location Services dan Permission
Menambahkan fitur foreground location one-shot tanpa background location, continuous tracking, maupun Maps API.

- **Foreground Runtime Permission**: `ACCESS_COARSE_LOCATION` dan `ACCESS_FINE_LOCATION` diminta lewat `ActivityResultContracts.RequestMultiplePermissions` dan diperbarui saat hasil dialog maupun `ON_RESUME`.
- **Location Repository**: `DefaultLocationRepository` memvalidasi izin aktual, memilih priority (`PRIORITY_HIGH_ACCURACY` vs `PRIORITY_BALANCED_POWER_ACCURACY`), memanggil `FusedLocationProviderClient.getCurrentLocation(...).await()`, dan melempar ulang `CancellationException`.
- **ViewModel & StateFlow**: `LocationViewModel` mengelola `LocationUiState` (`Idle`, `Loading`, `Success`, `PermissionDenied`, `Error`) dengan guard saat `Loading` untuk mencegah request ganda.
- **UI Lokasi**: `LocationScreen` menampilkan label akses (`Precise`/`Approximate`), koordinat, accuracy (meter), waktu pembaruan (`HH:mm:ss`), tombol `Perbarui Lokasi`, `Minta Izin Lagi`, serta `Buka Pengaturan` untuk penolakan permanen.

## Fitur Aplikasi

- Daftar mahasiswa dari REST API dan detail berdasarkan ID.
- Halaman Profile dan About.
- Loading indicator, error state, Coba Lagi, dan Refresh.
- Pencarian profil otomatis (debounce) dengan query terakhir tersimpan.
- Tombol Simpan/Hapus Favorit pada Detail.
- Halaman Favorit yang reaktif dan dapat diurutkan.
- Data favorit dan preferensi tetap ada setelah aplikasi direstart.
- Halaman Lokasi untuk membaca koordinat terkini secara one-shot beserta status `Precise`/`Approximate`, accuracy, waktu pembaruan, dan penanganan izin ditolak.

## Struktur Proyek

```text
data/
├── location/
│   ├── DeviceLocation.kt
│   ├── LocationRepository.kt
│   └── DefaultLocationRepository.kt
├── model/
│   └── Student.kt
├── preferences/
│   ├── UserPreferencesDataStore.kt
│   └── UserPreferencesRepository.kt
├── local/
│   ├── FavoriteStudentEntity.kt
│   ├── FavoriteStudentDao.kt
│   └── AppDatabase.kt
├── repository/
│   ├── StudentRepository.kt
│   ├── DefaultStudentRepository.kt
│   └── FavoriteRepository.kt
└── remote/
    ├── StudentApiService.kt
    ├── RetrofitClient.kt
    ├── StudentRemoteDataSource.kt
    └── dto/
        ├── UserDto.kt
        └── UserResponse.kt

ui/
├── home/
│   ├── HomeUiState.kt
│   └── HomeViewModel.kt
├── detail/
│   ├── DetailUiState.kt
│   └── DetailViewModel.kt
├── favorites/
│   ├── FavoritesUiState.kt
│   └── FavoritesViewModel.kt
├── location/
│   ├── LocationUiState.kt
│   ├── LocationViewModel.kt
│   └── LocationScreen.kt
└── screen/
    ├── HomeScreen.kt
    ├── DetailScreen.kt
    ├── FavoritesScreen.kt
    ├── ProfileScreen.kt
    └── AboutScreen.kt
```

## REST API

API yang digunakan adalah [DummyJSON](https://dummyjson.com/). Base URL: `https://dummyjson.com/`

| Kebutuhan | Endpoint |
|---|---|
| Daftar mahasiswa | `GET /users?limit=10` |
| Detail mahasiswa | `GET /users/{id}` |
| Pencarian profil | `GET /users/search?q={query}` |

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Lingkungan Pengembangan dan Dependensi

| Komponen | Teknologi |
|---|---|
| IDE | Android Studio |
| Bahasa | Kotlin |
| UI Toolkit | Jetpack Compose |
| Navigation | Navigation Compose |
| Architecture | Layered Architecture / MVVM |
| Asynchronous | Kotlin Coroutines, Flow / StateFlow |
| HTTP Client | Retrofit + Gson |
| Penyimpanan | Preferences DataStore, Room |
| Lokasi & Permission | Fused Location Provider, Coroutines Play Services, Activity Result API |
|