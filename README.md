# 📚 LearnCore - Aplikasi untuk fokus pembelajaran

![CI](https://github.com/123140003-MuhammadFadhilahAkbar/Proyek-Pengembangan-Aplikasi-Mobile/actions/workflows/ci.yml/badge.svg)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-4285F4?style=flat&logo=jetpackcompose&logoColor=white)

---

## 📱 Deskripsi Aplikasi

**LearnCore** adalah aplikasi mobile produktivitas cerdas yang dirancang khusus untuk membantu pelajar dan profesional mengoptimalkan sesi belajar serta mengelola tugas secara efisien. Dengan menggabungkan teknik manajemen waktu yang teruji dan kecerdasan buatan, LearnCore hadir sebagai asisten pribadi yang mengatasi kebiasaan menunda pekerjaan dan menjaga fokus pengguna agar tetap berada pada performa puncak.

---

LearnCore membantu pengguna memprioritaskan pekerjaan dan belajar secara lebih efektif. Dengan visualisasi Matriks Eisenhower interaktif, pengguna dapat melihat secara langsung tugas mana yang harus dikerjakan duluan. Dikombinasikan dengan Pomodoro Timer dan AI Assistant berbasis Gemini, LearnCore hadir sebagai teman belajar yang cerdas dan personal.

> Focus. Prioritize. Learn Smarter.

Aplikasi mobile multiplatform (Android-first) yang membantu mahasiswa dan pelajar untuk **mengelola tugas secara cerdas** menggunakan **Matriks Eisenhower**, dilengkapi dengan **Pomodoro Timer**, **AI Learning Assistant**, dan analisis produktivitas berbasis data lokal.

---

## 🎬 Demo Aplikasi

[![Demo LearnCore](https://img.youtube.com/vi/a_slLEvTmy4/maxresdefault.jpg)](https://youtu.be/a_slLEvTmy4?si=LT1NuH7F5IQcbZ3I)

> 🔼 *Klik gambar di atas untuk menonton demonstrasi lengkap aplikasi LearnCore di YouTube.*

---

## 👥 Tim
 
<table>
  <tr>
    <th>Foto</th>
    <th>Nama</th>
    <th>NIM</th>
    <th>Role</th>
    <th>GitHub</th>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/6c3d81c5-1885-450b-9839-9d9fc984e17d" width="60" height="60" style="border-radius:50%" alt="Fadhilah"/></td>
    <td>Muhammad Fadhilah Akbar</td>
    <td>123140003</td>
    <td>Lead & Android Dev</td>
    <td align="center"><a href="https://github.com/123140003-MuhammadFadhilahAkbar">@BaarzJo</a></td>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/41cd27f7-aeff-4f1e-9aa1-5e79f3e55557" width="60" height="60" style="border-radius:50%" alt="Sigit"/></td>
    <td>Sigit Kurnia Hartawan</td>
    <td>123140033</td>
    <td>FE Dev & QA</td>
    <td align="center"><a href="https://github.com/SigitKurnia05">@Mr. git</a></td>
  </tr>
</table>

**Mata Kuliah:** IF25-22017 Pengembangan Aplikasi Mobile
 
**Dosen:** Pak Habib ([@mh4Scripts](https://github.com/mh4Scripts))
 
**Institut:** Institut Teknologi Sumatera (ITERA)


---

## ✨ Fitur

### Minimum (Wajib)

- [ ] **Dashboard & Eisenhower Matrix** — Visualisasi prioritas tugas aktif dalam grid 2×2 interaktif; klik kuadran untuk langsung filter daftar tugas
- [ ] **Task List & CRUD** — Tambah, lihat, edit, dan hapus tugas dengan kategori prioritas Eisenhower; navigasi ke halaman detail menggunakan argument passing
- [ ] **Pomodoro Mode** — Timer hitung mundur dengan Circular Progress Indicator; pilih tugas aktif yang sedang dikerjakan; kontrol Play/Pause/Reset
- [ ] **Profile & Settings** — Input nama & status pengguna; kustomisasi durasi Pomodoro via Slider; toggle notifikasi deadline; toggle Dark/Light Mode
- [ ] **AI Learning Assistant** — Chat interaktif dengan Gemini API; prompt cepat (chip suggestion) untuk analisis produktivitas; kirim data statistik Room ke AI untuk evaluasi
- [ ] **Navigasi Multi-Screen** — 5 layar utama: Dashboard, Task List/Detail, Pomodoro, Profile/Settings, AI Chat
- [ ] **State Management** — MVVM + StateFlow untuk semua UI state
- [ ] **Minimal 10 unit tests** + 3 UI tests, coverage > 50%
- [ ] **Koin DI** — Dependency injection setup

### Bonus (Target)

- [ ] **AI Integration (+10%)** — Integrasi Gemini API untuk saran belajar kontekstual dan analisis produktivitas personal
- [ ] **Offline First (+5%)** — Cache data tugas dan riwayat di Room Database; aplikasi tetap berfungsi tanpa internet
- [ ] **Dark Mode (+5%)** — Support tema gelap/terang dengan Material 3
- [ ] **Animations (+5%)** — Animasi transisi antar screen, animasi timer Pomodoro, animasi indikator kuadran Eisenhower
- [ ] **CI/CD (+5%)** — Automated build dan test dengan GitHub Actions

**Total target bonus: +30%**

---

## 🖥️ Rancangan 5 Screen Utama

### 1. Dashboard / Home
Layar utama dengan **Eisenhower Matrix Card** interaktif grid 2×2 berisi titik-titik berwarna mewakili tugas aktif per kuadran. Klik kuadran menavigasi ke Task List yang sudah terfilter. Tersedia kartu statistik (tugas selesai, waktu fokus) dan tombol FAB untuk langsung ke Pomodoro.

### 2. Task List, CRUD & Detail Page
Daftar tugas untuk filter 4 kuadran secara manual. Setiap item memiliki checkbox dan indikator warna prioritas. FAB untuk tambah tugas via BottomSheet. Halaman Detail menampilkan deskripsi lengkap, tenggat, opsi ubah kuadran, dan tombol hapus tugas.

### 3. Pomodoro Mode
UI bersih minim gangguan. Lingkaran besar sebagai timer utama dengan warna dinamis (merah/oranye = kerja, hijau/biru = istirahat). Dropdown pemilih tugas aktif dari database. Tombol Play/Pause dan Reset. State tracker menampilkan progres siklus (misal: "Sesi 1 dari 4").

### 4. Profile & Settings
Header profil dengan placeholder icon Material 3. Slider kustomisasi durasi Pomodoro. Toggle notifikasi deadline lokal. Switch Dark/Light Mode.

### 5. AI Learning Assistant
UI chat interaktif mirip aplikasi messaging. Chip suggestion untuk prompt cepat: "Pecah tugasku hari ini", "Beri ringkasan materi", "Analisa produktivitasku". Saat analisis diminta, data statistik dari Room dikirim ke Gemini untuk evaluasi dan saran peningkatan.

---

## 📸 Preview Tampilan Aplikasi

| Dashboard | Task List | Pomodoro |
|:---------:|:---------:|:--------:|
| <img src="https://github.com/user-attachments/assets/536ec5fc-59f0-4227-8e37-18f8cf2d1ba4" width="180"/> | <img src="https://github.com/user-attachments/assets/12715f94-6909-480a-b09a-c24b40dbc08d" width="180"/> | <img src="https://github.com/user-attachments/assets/aa3db5df-cb67-4f1b-b452-0dcbf41094a8" width="180"/> |
| Eisenhower Matrix interaktif | CRUD tugas & filter kuadran | Timer fokus circular |

| Profile & Settings | AI Learning Assistant |
|:-----------------:|:---------------------:|
| <img src="https://github.com/user-attachments/assets/977c108a-8ca8-4430-aa14-5cf9101d8a02" width="180"/> | <img src="https://github.com/user-attachments/assets/58d94618-fc87-4f8a-8341-e276698dc34b" width="180"/> |
| Kustomisasi & Dark Mode | Chat dengan Gemini AI |

---

## 🏗️ Arsitektur

Menggunakan **Clean Architecture + MVVM** sesuai panduan mata kuliah.

```
┌─────────────────────────────────────────────────┐
│              PRESENTATION LAYER                 │
│   Screens (Composable) ◄──► ViewModel           │
│             (StateFlow / UDF Pattern)           │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│               DOMAIN LAYER                      │
│   Use Cases ◄──► Repository Interfaces          │
│           (Pure Kotlin, no framework)           │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│                DATA LAYER                       │
│   Repository Impl                               │
│   ├── Remote: Ktor + Gemini API (AI)            │
│   └── Local:  SQLDelight Database (tasks, stats)│
└─────────────────────────────────────────────────┘
```

### Struktur Folder

```
composeApp/src/
├── androidMain/kotlin/com/learncore/
│   ├── android/
│   │   ├── LearnCoreApplication.kt
│   │   └── MainActivity.kt
│   ├── core/
│   │   ├── di/AndroidModule.kt
│   │   ├── network/
│   │   │   ├── ApiConfig.android.kt
│   │   │   └── NetworkMonitorImpl.kt
│   │   ├── notification/
│   │   │   ├── DeadlineNotificationReceiver.kt
│   │   │   ├── DeadlineSchedulerImpl.kt
│   │   │   └── PomodoroNotifierImpl.kt
│   │   └── util/DatabaseDriverFactory.android.kt
│   ├── data/local/datastore/DataStoreFactory.android.kt
│   └── presentation/screens/
│       ├── profile/ProfilePhotoSection.android.kt
│       └── tasks/PlatformDateTimePicker.android.kt
│
├── commonMain/kotlin/com/learncore/
│   ├── App.kt
│   ├── core/
│   │   ├── di/AppModule.kt
│   │   ├── network/
│   │   │   ├── ApiConfig.kt
│   │   │   ├── HttpClientFactory.kt
│   │   │   └── NetworkMonitor.kt
│   │   ├── notification/
│   │   │   ├── DeadlineScheduler.kt
│   │   │   └── PomodoroNotifier.kt
│   │   └── util/DatabaseDriverFactory.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── datastore/
│   │   │   │   ├── DataStoreFactory.kt
│   │   │   │   └── UserPreferences.kt
│   │   │   └── entity/TaskMapper.kt
│   │   ├── remote/
│   │   │   ├── api/GeminiService.kt
│   │   │   └── dto/GeminiDto.kt
│   │   └── repository/
│   │       ├── AIRepositoryImpl.kt
│   │       └── TaskRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/Task.kt
│   │   ├── repository/Repositories.kt
│   │   └── usecase/TaskUseCases.kt
│   ├── presentation/
│   │   ├── components/SharedComponents.kt
│   │   ├── navigation/
│   │   │   ├── AppNavHost.kt
│   │   │   └── Routes.kt
│   │   ├── screens/
│   │   │   ├── ai/
│   │   │   │   ├── AIAssistantScreen.kt
│   │   │   │   └── AIAssistantViewModel.kt
│   │   │   ├── dashboard/
│   │   │   │   ├── DashboardScreen.kt
│   │   │   │   └── DashboardViewModel.kt
│   │   │   ├── pomodoro/
│   │   │   │   ├── PomodoroScreen.kt
│   │   │   │   └── PomodoroViewModel.kt
│   │   │   ├── profile/
│   │   │   │   ├── AccountEditScreen.kt
│   │   │   │   ├── HelpSupportScreen.kt
│   │   │   │   ├── ProfilePhotoSection.kt
│   │   │   │   ├── ProfileScreen.kt
│   │   │   │   └── ProfileViewModel.kt
│   │   │   └── tasks/
│   │   │       ├── AddEditTaskScreen.kt
│   │   │       ├── AddEditTaskViewModel.kt
│   │   │       ├── PlatformDateTimePicker.kt
│   │   │       ├── TaskDetailScreen.kt
│   │   │       ├── TaskDetailViewModel.kt
│   │   │       ├── TaskListScreen.kt
│   │   │       └── TaskListViewModel.kt
│   │   └── theme/Theme.kt
│   └── sqldelight/com/learncore/data/local/
│       ├── LearnCore.sq
│       └── migrations/
│           ├── 1.sqm
│           └── 2.sqm
│
├── commonTest/kotlin/com/learncore/
│   ├── domain/model/
│   │   ├── ProductivityStatsTest.kt
│   │   └── TaskModelTest.kt
│   ├── domain/usecase/TaskUseCasesTest.kt
│   └── presentation/
│       ├── AddEditTaskViewModelTest.kt
│       ├── DashboardViewModelTest.kt
│       ├── PomodoroViewModelTest.kt
│       ├── TaskDetailViewModelTest.kt
│       └── TaskListViewModelTest.kt
│
└── androidTest/kotlin/com/learncore/
    └── TaskCardUiTest.kt
```

---

## 🛠️ Tech Stack

<details>
<summary><b>Lihat daftar teknologi lengkap</b></summary>

| Komponen | Teknologi |
|----------|-----------|
| **Framework** | Kotlin Multiplatform, Compose Multiplatform |
| **Architecture** | MVVM, Clean Architecture, Repository Pattern |
| **Async** | Coroutines, Flow, StateFlow |
| **Networking** | Ktor Client + Kotlinx Serialization |
| **AI** | Google Gemini API (`gemini-2.5-flash`) |
| **Local Storage** | SQLDelight (tasks, Pomodoro sessions, stats) |
| **Preferences** | DataStore (tema, durasi timer, onboarding flag) |
| **DI** | Koin |
| **Notifications** | WorkManager (deadline reminders) |
| **Testing** | kotlin.test, MockK, Turbine, Compose Test |
| **CI/CD** | GitHub Actions |

</details>

---

## 🗂️ Sprint Plan

<details>
<summary><b>Lihat jadwal sprint lengkap</b></summary>

| Sprint | Minggu | Target | PIC |
|--------|--------|--------|-----|
| **Sprint 1** | W11 | Planning, repo setup, CI/CD, README | Seluruh Anggota |
| **Sprint 2** | W12 | Dashboard + Eisenhower Matrix UI, navigasi 5 screen | Anggota 1 |
| **Sprint 2** | W12 | Room Database setup, Task CRUD & domain model | Anggota 2 |
| **Sprint 3** | W13 | Pomodoro Timer screen + state management | Anggota 1 |
| **Sprint 3** | W13 | Gemini AI integration + AI Chat screen | Anggota 2 |
| **Sprint 4** | W14 | Dark mode, animasi, UI polish | Anggota 1 |
| **Sprint 4** | W14 | Unit tests & UI tests, bug fixes | Anggota 2 |
| **Sprint 5** | W15 | Final fixes, dokumentasi, demo prep | Seluruh Anggota |
| **UAS** | W16 | Demo Day 🎉 | Seluruh Anggota |

</details>

---

## 🚀 Setup & Cara Menjalankan

### Prerequisites

- Android Studio Ladybug (2024.2.1) atau lebih baru
- JDK 17+
- Gradle 8.x

### Langkah Setup

1. **Clone repository**
   ```bash
   git clone https://github.com/123140003-MuhammadFadhilahAkbar/Proyek-Pengembangan-Aplikasi-Mobile.git
   cd Proyek-Pengembangan-Aplikasi-Mobile
   ```

2. **Setup `local.properties`**
   ```bash
   cp local.properties.example local.properties
   # Edit local.properties dan isi:
   # GEMINI_API_KEY=your_key_here
   ```
   Dapatkan Gemini API key gratis di: https://aistudio.google.com/

3. **Build project**
   ```bash
   ./gradlew build
   # Atau hanya APK debug:
   ./gradlew :composeApp:assembleDebug
   ```

4. **Run di Android**
   ```bash
   ./gradlew :composeApp:installDebug
   ```

### Menjalankan Tests

```bash
./gradlew allTests
./gradlew :composeApp:testDebugUnitTest
```

---

## 📡 API Reference

### Gemini API

- **Model:** `gemini-2.5-flash` (gratis tier)
- **Kegunaan:** Generate saran belajar kontekstual, analisis produktivitas berdasarkan data statistik pengguna

---

## 📝 Logika Prioritas Eisenhower

Setiap tugas dikategorikan ke dalam salah satu dari 4 kuadran:

| Kuadran | Label | Tindakan | Warna |
|---------|-------|----------|-------|
| Q1 | Mendesak & Penting | **Do First** — kerjakan segera | Merah |
| Q2 | Tidak Mendesak & Penting | **Schedule** — rencanakan waktu | Biru |
| Q3 | Mendesak & Tidak Penting | **Delegate** — delegasikan | Kuning |
| Q4 | Tidak Mendesak & Tidak Penting | **Eliminate** — pertimbangkan ulang | Abu-abu |

---

## 📄 Lisensi

MIT License — dibuat untuk keperluan pembelajaran Pengembangan Aplikasi Mobile ITERA.
