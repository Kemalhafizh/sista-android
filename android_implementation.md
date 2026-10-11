# Rencana Implementasi Android SISTA: SMA Islam Sultan Agung 1 Semarang

> **Baca bagian "Status Saat Ini" dulu.** Sejarah FASE 1–77 ada di [`docs/archive/android_implementation_fase_1-77.md`](docs/archive/android_implementation_fase_1-77.md). Arsip itu hanya untuk referensi; sebagian layarnya sudah dihapus.
> **Alur kerja per fitur** (siapa, kapan, disetujui siapa) ada di `sistem-terpadu/docs/ALUR_KERJA.md`. Setiap layar mengikuti alur di sana.
> Rencana backend pasangannya ada di `sistem-terpadu/implementation_plan.md`.

---

## 📍 STATUS SAAT INI (diperbarui 11 Oktober 2026)

### Peran aplikasi
Android adalah **pintu kelas dan rumah**: guru di kelas, siswa, dan orang tua. Pekerjaan meja (TU, waka, kepala sekolah) ada di web. Pimpinan memakai aplikasi terutama untuk **menyetujui** (kotak masuk persetujuan, FASE 81). Aplikasi **belum rilis**.

### Angka setelah debloat (FASE 78)
| Layar (`*Screen`) | Entri `Screen` | Baris Kotlin `src/main` | APK debug |
|---:|---:|---:|---:|
| 75 | 77 | ±60.700 | 67,5 MB |

### Fitur aktif per peran
- **Siswa:** beranda, jadwal, scan QR kelas (satu-satunya cara presensi), nilai, rapor, remedial, CBT, e-learning, tagihan, perpustakaan, kalender, BK, lapor perundungan, riwayat UKS, kedisiplinan, ekskul, prestasi, Mutabaah, Tahsin, evaluasi guru, pengumuman, notifikasi.
- **Orang tua:** dashboard anak, detail & progres, aktivitas & presensi per mapel anak, chat wali kelas, tagihan, pengumuman.
- **Guru:** sesi kelas & QR, presensi, jurnal, penilaian & input nilai, remedial, bank soal & ujian, pengawas CBT, chat orang tua, review Tahsin.
- **BK/UKS/Admin:** konseling, kunjungan UKS, manajemen sesi kelas & koreksi presensi.
- **Umum:** login (+ sidik jari HP), profil, pengaturan (bahasa, aksesibilitas, keamanan, notifikasi), SPMB, PDF & unduhan.
- **Widget:** jadwal, presensi per mapel, SPP, jadwal sholat (masih kosong, lihat 79.4).

### Sudah DIHAPUS (jangan dihidupkan lagi)
Presensi GPS, QR identitas, pendaftaran wajah, AI tutor & esai, gamifikasi, pindai dokumen, tanda tangan digital, analitik eksekutif, laporan diagnostik, SOS (diganti Lapor Perundungan), tryout UTBK.

### Keputusan pemilik produk yang berlaku
| Tanggal | Keputusan |
|---|---|
| 5 Okt 2026 | Presensi siswa hanya QR per mapel. Pegawai memakai mesin sidik jari (bukan aplikasi) |
| 11 Okt 2026 | Full Kurikulum Merdeka: siswa kelas X mengisi **survei peminatan** mapel pilihan (FASE 80). Survei hanya memetakan minat; rombel XI dirancang dan diplot Waka Kurikulum di web |
| 11 Okt 2026 | Evaluasi guru oleh siswa dipertahankan. Widget jadwal sholat **dipertahankan** |

### Aturan main
1. **CLAUDE.md** berlaku penuh: tidak ada angka karangan, aksi harus tersimpan di server, akses dari `capabilities`, tiga bahasa, dan screenshot untuk setiap layar.
2. **Tidak ada layar tanpa endpoint dan tanpa alur** di `ALUR_KERJA.md`.
3. **Tidak ada fitur baru di luar roadmap sampai go-live.**
4. PR yang mengikuti perubahan API menautkan PR pasangannya di `sistem-terpadu`.

### Roadmap
| Fase | Isi | Status | Pasangan backend |
|---|---|---|---|
| 77 | Sesi kelas & presensi QR per mapel | ✅ (arsip) | 117 |
| 78 | Debloat | ✅ | 119 |
| **79** | **Siap rilis:** token terenkripsi, build rilis R8, konfigurasi lingkungan, signing, Firebase sekolah, widget sholat, kode mati | ⏳ **Berikutnya**, bisa langsung jalan paralel | 119.12 (endpoint jadwal sholat) |
| **80** | Sisi Android FASE 120: survei peminatan, kelas tahun depan, riwayat rombel | ⏳ Setelah API 120.4 tersedia | 120 |
| 81 | Sisi Android FASE 121: ajukan izin/sakit anak, rapor terbit, kotak masuk persetujuan pimpinan | 📝 Garis besar | 121 |
| 82 | Rilis: Play Console, kebijakan privasi, jalur internal → pilot Jan 2027 | 📝 Garis besar | 123 |

---

## ✂️ FASE 78: DEBLOAT APLIKASI, HANYA FITUR YANG DIPAKAI SEKOLAH `[SELESAI 11 Okt 2026 — widget presensi butuh sistem-terpadu#47 ter-deploy]`

> **Tanggal Rancangan:** 5 Oktober 2026
> **Pelaksana:** Agent Frontend/Mobile (repo `sista-android`). Pasangannya di backend: **FASE 119** di `sistem-terpadu/implementation_plan.md`.
> **Prioritas:** P0. Kerjakan sebelum fitur baru apa pun.
> **Prinsip:** Setiap layar harus dipakai siswa, ortu, guru, atau staf SMA Islam Sultan Agung 1 secara rutin. Kalau tidak, layarnya dihapus.

### 78.0: Keputusan Pemilik Produk (5 Okt 2026)

| Topik | Keputusan |
|---|---|
| Status produksi | Belum rilis, belum ada pengguna. Layar dan kode **dihapus permanen**, bukan disembunyikan |
| Presensi siswa | **Hanya QR per mapel** (`StudentSessionQrScan`, FASE 77). Hapus GPS/geofence, QR identitas dinamis, dan pendaftaran wajah |
| Presensi guru/karyawan | Memakai mesin sidik jari di sekolah. Aplikasi **tidak** menyediakan check-in pegawai |
| Gamifikasi | Gimmick, hapus seluruhnya |
| Pendidikan inklusi | Dipertahankan di backend (di bawah BK). Belum ada layar Android, jadi tidak perlu ditambah sekarang |

### 78.1: Layar yang DIHAPUS

Hapus layar beserta ViewModel, repository/API service, model, entri `Screen`, rute navigasi, entri `FeatureCatalog` (dan kunci kapabilitasnya), string di **tiga bahasa** (`values`, `values-en`, `values-ar`), screenshot Roborazzi, test, serta entri `debt`/`allowed` di test arsitektur yang menunjuk ke file terhapus.

| Layar | Kunci kapabilitas | Alasan |
|---|---|---|
| `GeofenceAttendanceScreen` | `attendance.gps` | Presensi siswa cukup QR per mapel |
| `DynamicQrScreen` | `attendance.id_qr` | QR gerbang tidak dipakai |
| `FaceEnrollmentScreen`, rute `Screen.FaceBiometric` | `attendance.face_enroll` | Tidak dipakai, dan data biometrik wajah anak di bawah umur berisiko besar (UU PDP) |
| `AiTutorScreen` | `student.ai_tutor` | Gimmick, butuh biaya API LLM bulanan |
| rute `Screen.AiEssayGrader` + komposabelnya | `student.ai_essay` | Gimmick, penilaian tetap oleh guru |
| `GamificationDashboardScreen`, `LeaderboardScreen`, `BadgeCollectionScreen` | `student.gamification` | Gimmick, tujuan tidak jelas |
| `DocumentScannerScreen` | `documents.scan` | Tidak ada alur administrasi yang membutuhkannya |
| `SignatureScreen` (`DigitalSignature`) | `documents.signature` | Sama |
| `ExecutiveAnalyticsScreen` | `admin.executive` | Analitik pimpinan cukup di web |
| `DiagnosticReportScreen` | — | Tidak berguna bagi pengguna sekolah |
| `QrScannerScreen` mode identitas | `scanner.identity` | QR identitas dihapus. **Mode acara (`scanner.event`) tetap** |

Ikut dihapus:
- Permission `ACCESS_FINE_LOCATION` dan `ACCESS_COARSE_LOCATION` di `AndroidManifest.xml`, serta dependensi `play-services-location` (bila tidak ada pemakai lain).
- Test E2E `e2e/.../AttendanceGpsFlowE2ETest.kt` dan bagian `AppiumTestBase` yang khusus GPS.
- **Tetap dipertahankan:** `USE_BIOMETRIC` (buka kunci login dengan sidik jari HP), `CAMERA` (scan QR), dan `RECORD_AUDIO` (Tahsin).

### 78.2: Layar yang DIUBAH

- **`AntiBullyingSosScreen`** (status sekarang `NOT_READY`) diubah menjadi form sederhana **"Lapor Perundungan"**: isi kejadian, waktu, tempat, opsi anonim, lalu kirim ke endpoint lapor perundungan dari FASE 119 PR 4. Tanpa tombol SOS, tanpa lokasi GPS. Tampilkan pesan server apa adanya. Screenshot: kosong, terisi, terkirim, error. Layar ini hanya diaktifkan setelah endpoint backend ada (CLAUDE.md aturan 2).
- **Menu presensi siswa** di Home/ServicesHub: satu pintu saja, "Scan QR Kelas" (`StudentSessionQrScan`).
- **Widget:** `AttendanceWidgetProvider` dipertahankan bila datanya dari presensi QR per mapel. Hapus bagian yang bergantung pada GPS bila ada.

### 78.3: Fitur yang DIPERTAHANKAN (inti)

- **Siswa:** jadwal, scan QR kelas, nilai, rapor, remedial, CBT, e-learning (materi & tugas), tagihan, perpustakaan, kalender & acara, BK, riwayat UKS, kedisiplinan, ekstrakurikuler, prestasi, Mutabaah, Tahsin, pengumuman, notifikasi.
- **Orang tua:** dashboard anak, detail & progres anak, aktivitas anak, chat dengan wali kelas, tagihan anak, pengumuman.
- **Guru:** sesi kelas hari ini & QR sesi, presensi kelas, jurnal mengajar, penilaian harian & input nilai, remedial, bank soal & buat ujian, pengawas CBT, chat ortu, review Tahsin.
- **BK/UKS/Admin:** dasbor konseling, form sesi konseling, kunjungan UKS, manajemen sesi kelas & koreksi presensi.
- **Umum:** login, profil, pengaturan (bahasa, keamanan, notifikasi), SPMB, PDF viewer & riwayat unduhan, update in-app.
- `AccessibilitySettings` digabung ke halaman Pengaturan, tidak berdiri sendiri.

### 78.4: Menunggu Keputusan Pemilik Produk (jangan disentuh di FASE 78)

- ~~`UtbkTryOutScreen` (tryout UTBK + rekomendasi PTN).~~ **Diputuskan dipotong (11 Okt 2026, sistem-terpadu `implementation_plan.md` 119.11).** Daftar PTN dan passing grade-nya hanya isian seeder, bukan data resmi, dan server tidak punya halaman untuk membuat tryout. Layar, `UtbkViewModel`, `UtbkApiService`, `UtbkRepository`, `UtbkModels`, rute `utbk_tryout`, dan kunci `student.utbk` dihapus. Endpoint server `api/v1/utbk/*` dihapus sesudah PR ini di-merge. Persiapan UTBK tetap lewat CBT sekolah (jenis `try_out`).
- `TeacherEvaluationScreen` (evaluasi guru oleh siswa): **dipertahankan** (11 Okt 2026, sistem-terpadu 119.11). Siswa hanya menilai guru yang mengajar di kelasnya.
- `PrayerTimeWidgetProvider` (widget jadwal sholat): **dipertahankan** (keputusan pemilik produk 11 Okt 2026). Saat ini widget kosong karena belum ada sumber data; diisi di FASE 79.4.

### 78.5: Urutan & Koordinasi dengan Backend

1. **PR A (Android duluan):** hapus layar 78.1. Pastikan aplikasi mengabaikan kunci kapabilitas yang tidak dikenal dari server (cek `FeatureCatalog`/`CapabilitiesRepository`), supaya urutan merge dengan backend tidak membuat crash.
2. **Backend FASE 119 PR 3** menghapus endpoint pemakainya setelah PR A di-merge. Tautkan kedua PR satu sama lain.
3. **PR B:** ubah `AntiBullyingSosScreen` menjadi "Lapor Perundungan" setelah endpoint FASE 119 PR 4 tersedia.

### 78.6: Kriteria Selesai

- [x] `./gradlew assembleDebug testDebugUnitTest lintDebug` hijau, screenshot Roborazzi direkam ulang dan di-commit (PR A).
- [x] Tidak ada lagi kelas, string, atau rute untuk layar 78.1. `LocalizationTest` dan `HardcodedUiTextTest` hijau, dan daftar `debt` menyusut 12 file.
- [x] `FeatureCatalogTest` hijau tanpa kunci yang dihapus, ditambah test bahwa kunci yang tidak dikenal diabaikan.
- [x] APK tidak lagi meminta izin lokasi (`aapt2 dump permissions`, dan `tools:node="remove"` di manifest).
- [x] PR B: "Lapor Perundungan" mengirim ke `POST counseling/bullying-reports` (sistem-terpadu#44), dengan screenshot kosong, terisi, terkirim, error, gelap, en, ar.
- [x] 78.3: ukuran teks, ramah disleksia, dan kontras tinggi ada langsung di halaman Pengaturan; rute `accessibility_settings` dihapus.
- [x] 78.2: widget presensi membaca presensi QR per mapel hari ini (`student/class-session/attendance`, `parent/child/{uuid}/class-session-attendance`, sistem-terpadu#47).
- [x] Bagian ini diperbarui menjadi `[SELESAI]`.

### 78.7: Hasil PR A (10 Okt 2026)

| | Sebelum | Sesudah |
|---|---|---|
| Entri `Screen` | 90 | 77 |
| Komposabel `*Screen` | 89 | 78 |
| Baris Kotlin `src/main` | 65.860 | 60.688 |
| APK debug | 69,5 MB | 67,5 MB |

Catatan pelaksanaan:
- **Kunci kapabilitas yang tidak dikenal** sudah diabaikan sejak awal (menu Layanan hanya menampilkan kunci yang ada di `FeatureCatalog.ENTRY`). Aturan itu dipindah dari composable ke `FeatureCatalog.menuFeatures()` dan diuji, termasuk server yang masih mengirim kunci yang dihapus dan server yang sudah mengirim kunci baru.
- **`FaceEnrollmentScreen` ternyata tidak merekam wajah.** Isinya pengaturan masuk dengan sidik jari dan proteksi rapor/ujian. Layar dan rute `FaceBiometric` dihapus sesuai 78.1, API `iot/face/enroll` dihapus, tetapi kedua sakelar sidik jari dipindah ke Pengaturan → Keamanan (`BiometricSettingsViewModel`), karena `USE_BIOMETRIC` dipertahankan.
- **Ikut dihapus karena hanya dipakai layar 78.1:** `GeoUtils` dan data `campus` dari `mobile/config`, antrean offline cek-in GPS, `DocumentApiService`/`DocumentRepository`, `GamificationApiService`/`GamificationRepository`, `AiApiService`/`AiRepository`, endpoint `analytics/executive/kpi`, `mobile/attendance/{gps-checkin,dynamic-qr,verify-qr}`, ringkasan `gamification` di model beranda kontekstual, mode scanner "Presensi QR", dan `play-services-location`.
- **Pintu presensi siswa** (FAB beranda dan deep link `attendance`) sekarang membuka `StudentSessionQrScan`.
- Widget presensi tidak lagi diperbarui dari cek-in GPS. Sumber datanya (`student/attendance`) belum diganti ke presensi per mapel; itu bagian 78.2.

### 78.8: Hasil PR B (11 Okt 2026)

- `AntiBullyingSosScreen` (tombol SOS, hitung mundur, koordinat) dihapus. Penggantinya `BullyingReportScreen` di rute `bullying_report`, dibuka oleh kunci kapabilitas `counseling.bullying_report` (semua peran yang masuk). Deep link `sos`, `emergency`, dan `bullying` diarahkan ke sana.
- Isian: cerita kejadian (wajib, maks. 5.000 karakter), tanggal (hari ini/kemarin/pilih, tidak boleh di masa depan), tempat (maks. 255), jenis (kode `BullyingReport::TYPES` dari server), dan sakelar anonim yang menyala secara bawaan. Aturannya ada di `BullyingReportRules` dan mengikuti validasi `BullyingReportApiController::store`, sehingga form tidak mengirim sesuatu yang pasti ditolak.
- Layar baru menyatakan "terkirim" setelah server menjawab 201, lalu menampilkan pesan server, nomor tiket, dan status dari server apa adanya. Bila gagal, pesan server ditampilkan dan isian tetap ada.
- Tanpa GPS, tanpa SOS, dan tanpa angka atau nama karangan.

### 78.9: Aksesibilitas digabung ke Pengaturan (11 Okt 2026)

- `AccessibilitySettingsScreen` dan rute `accessibility_settings` dihapus. Kartu ukuran teks serta sakelar ramah disleksia dan kontras tinggi (`AccessibilityControls.kt`) kini ada di bagian "Bahasa & aksesibilitas" pada halaman Pengaturan, sehingga perubahannya langsung terlihat di halaman yang sama. Kartu "Pratinjau" ikut dihapus karena halaman Pengaturan sendiri sudah jadi pratinjaunya.
- `TypeSafeNavigationTest` memastikan `accessibility_settings` dan `antibullying_sos` tidak muncul lagi.

### 78.10: Catatan widget presensi (78.2)

`AttendanceWidgetProvider` membaca `GET student/attendance` (dan `parent/child/{uuid}/attendance` untuk orang tua). Kedua endpoint itu membaca tabel `attendances`, yaitu presensi harian dari input guru, `AttendanceService`, dan sinkronisasi offline, **bukan** presensi QR per mapel (`session_attendances`, FASE 117). Belum ada endpoint siswa atau orang tua untuk presensi sesi. Agar widget memakai presensi QR per mapel, sistem-terpadu perlu endpoint baru lebih dulu (CLAUDE.md aturan 2). Bagian GPS sudah hilang sejak PR A.

**Selesai (11 Okt 2026):** sistem-terpadu#47 menambah `GET student/class-session/attendance` dan `GET parent/child/{uuid}/class-session-attendance` (`ClassSessionService::dayAttendanceFor`). Widget sekarang menyimpan `LessonAttendanceSnapshot` (pelajaran hari itu dan status tiap sesi) dan menampilkan:
- "Hadir di N dari M pelajaran", dihitung dari yang sudah tercatat (hadir dan telat dianggap hadir);
- "Belum absen: <mapel>" untuk kelas yang sedang berjalan dan belum di-scan (`awaiting_scan`), bukan "Alpa";
- "Belum ada pelajaran yang dimulai hari ini" untuk hari tanpa sesi;
- "Belum tercatat hari ini" bila data terakhir dari hari lain.

Data dimuat di Beranda siswa, setelah scan QR berhasil, dan saat orang tua membuka anak. Snapshot presensi harian versi lama (kunci `attendance`) tidak dibaca lagi.

---

## 🚀 FASE 79: SIAP RILIS `[RANCANGAN BARU]`

> **Tanggal rancangan:** 11 Oktober 2026
> **Pelaksana:** Agent Frontend/Mobile. **Tidak bergantung** pada FASE 120, jadi kerjakan paralel selama backend mengerjakan 119.12 dan 120. Hanya 79.4 yang butuh endpoint backend.
> **Mengapa sekarang:** temuan verifikasi PM 11 Okt 2026. Semuanya menghalangi pilot Januari 2027, dan perbaikannya makin mahal bila menunggu ada pengguna.

### 79.1: Token Login Terenkripsi
- **Masalah:** `SessionManager.kt:19` menyimpan `auth_token` di DataStore **tanpa enkripsi**. HP siswa sering dipinjam atau di-root, dan token itu membuka data anak dan keluarga.
- **Kerjakan:** simpan token terenkripsi dengan kunci di Android Keystore (Tink AEAD + DataStore, atau setara yang tidak *deprecated*). Saat pembaruan aplikasi, token lama yang polos dibaca sekali, ditulis ulang terenkripsi, lalu dihapus. Bila dekripsi gagal (kunci hilang setelah restore), sesi dibersihkan dan pengguna login ulang, tanpa crash.
- **Test:** migrasi polos → terenkripsi, token tidak pernah muncul polos di file preferensi, dan dekripsi gagal → logout bersih.

### 79.2: Build Rilis dengan R8
- **Masalah:** `minifyEnabled false`, sehingga build rilis belum pernah diuji dengan R8.
- **Kerjakan:** `minifyEnabled true` dan `shrinkResources true`. Aturan keep untuk model yang diparse **Gson lewat refleksi** (penyebab crash rilis yang paling umum), Retrofit, Hilt, Room, dan kotlinx-serialization. Tambahkan job CI `assembleRelease` (ditandatangani kunci debug di CI) agar build rilis yang rusak ketahuan di PR, bukan di HP pengguna.
- **Verifikasi:** jalankan APK rilis di emulator. Login, beranda tiap peran, scan QR, CBT, dan tagihan harus terbuka tanpa crash.

### 79.3: Konfigurasi Lingkungan
- **Masalah:** `Constants.DEFAULT_BASE_URL = "http://127.0.0.1:8000/api/v1/"`.
- **Kerjakan:** `BASE_URL` dan `REVERB_HOST` lewat `buildConfigField` per lingkungan (`debug`, `staging`, `release`). Build rilis **gagal** bila URL produksi tidak diisi, bukan diam-diam jatuh ke 127.0.0.1. Cleartext hanya boleh di debug (`network_security_config` per build type).

### 79.4: Widget Jadwal Sholat (dipertahankan, tetapi kini kosong)
- **Keadaan:** `PrayerTimeWidgetProvider` hanya menampilkan "belum tersedia" karena belum ada sumber data. Ini sudah jujur, tetapi tidak berguna.
- **Kerjakan (bersama backend 119.12 butir 6):** widget dan kartu beranda membaca `GET prayer-times?date=` dari server, yang menghitung jadwal untuk **koordinat sekolah** dengan parameter **Kemenag RI**. Satu sumber untuk web dan aplikasi, jadi tidak ada jadwal berbeda antar HP. Data di-cache per hari untuk offline. Bila server mengirim `null` (koordinat belum diatur), widget tetap menampilkan "belum tersedia".
- Ketuk widget membuka Mutabaah (sudah ada).

### 79.5: Signing, Versi & Firebase Milik Sekolah
- **Signing:** `signingConfig` rilis dibaca dari variabel lingkungan/secret CI. Keystore **tidak** masuk repo (CLAUDE.md). Simpan cadangan keystore dan password di tempat aman milik sekolah. Kehilangan keystore berarti tidak bisa memperbarui aplikasi di Play Store.
- **Versi:** `versionCode` naik otomatis (misalnya nomor run CI), dan `versionName` mengikuti rilis.
- **Firebase:** `app/google-services.json` saat ini milik proyek `device-streaming-…`, proyek otomatis dari Android Studio, bukan proyek sekolah. Buat proyek Firebase atas **akun Google sekolah**, ganti file itu, dan samakan kredensial FCM backend (`FCM_CREDENTIALS`). Batasi API key ke package `com.sultanagung1.sista` dan SHA-1 sertifikat rilis. Pastikan Crashlytics tidak mengirim nama atau NISN siswa di log.

### 79.6: Kode Mati & CI
- Hapus `core/wearable/WearableDataLayer.kt` (sinkron Wear OS yang tidak dipanggil siapa pun).
- Periksa dan hapus bila memang tidak dipakai: `core/telemetry/SulaoneTelemetryHub.kt`, `core/monitoring/PerformanceTracer.kt`, `core/monitoring/AnalyticsTracker.kt`, `core/notification/NotificationRouter.kt`. Ini hasil pindai cepat PM, jadi verifikasi dulu.
- Workflow `e2e_pipeline.yml` (Appium) dibatalkan **14 dari 14** run karena butuh server yang belum ada. Matikan jadwal malamnya, sisakan `workflow_dispatch` sampai server staging ada (FASE 82).

### 79.7: Kriteria Selesai
- [ ] `./gradlew assembleDebug assembleRelease testDebugUnitTest lintDebug` hijau di CI, dan screenshot direkam ulang bila UI berubah.
- [ ] APK rilis lolos uji asap 79.2 di emulator.
- [ ] Tidak ada token polos di penyimpanan, dibuktikan oleh test.
- [ ] Build rilis tanpa URL produksi gagal dengan pesan jelas.
- [ ] Widget sholat menampilkan jadwal dari server, atau "belum tersedia" bila `null`.
- [ ] Bagian ini diperbarui menjadi `[SELESAI]` dengan ukuran APK rilis.

---

## 🎓 FASE 80: SISI ANDROID FASE 120: SURVEI PEMINATAN & RIWAYAT ROMBEL `[RANCANGAN BARU]`

> **Pelaksana:** Agent Frontend/Mobile, setelah sistem-terpadu FASE 120 PR 3 (API 120.4) di-merge.
> **Alur:** `ALUR_KERJA.md` A3 (survei peminatan), A2 (rombel).

### 80.1: Siswa Kelas X: "Survei Peminatan"
- Survei ini **hanya memetakan minat**. Waka Kurikulum yang merancang rombel XI dan memplot siswa di web; aplikasi tidak menampilkan kuota, peringkat peluang, atau tebakan rombel.
- Muncul di beranda dan menu Akademik hanya bila kapabilitas `student.elective_survey` aktif, yang dikirim server selama ada survei untuk siswa itu.
- Isi: daftar mapel pilihan yang ditawarkan, aturan jumlah pilihan dari server ("pilih 4 mapel"), dan peringkat bila survei berperingkat (`ranked`). Siswa bisa mengubah pilihan selama status `dibuka`. Tampilkan tenggat `closes_at` (Asia/Jakarta).
- Simpan ke server (`PUT …/choices`). Bila ditolak (survei ditutup, atau jumlah pilihan di luar batas), tampilkan pesan server dan bertindak berdasarkan `error_code`, bukan teks.
- Setelah diumumkan: kartu "Kelas kamu tahun depan: XI-3", berisi susunan mapelnya dari `GET student/next-classroom`. Sebelum itu server mengirim `null` dan kartu tidak tampil.
- Screenshot: tidak ada survei, dibuka belum mengisi, sudah mengisi, ditutup menunggu pengumuman, diumumkan, dan error. Ditambah `en-` dan `ar-ldrtl-`.

### 80.2: Orang Tua
- Di detail anak: mapel yang dipilih anak (baca saja) dan kelas tahun depan setelah diumumkan (`parent.elective_survey`).

### 80.3: Riwayat Rombel (opsional, di profil siswa)
- Daftar TA → rombel → status (`naik/tinggal/lulus/mutasi`) dari `GET …/enrollment-history`.

### 80.4: Penyesuaian Akibat Model Rombel Baru
- Semua layar yang menampilkan nama kelas tetap membaca dari server. Tidak ada lagi label jurusan IPA/IPS. Bila ada string `IPA`/`IPS` di `res/values*`, hapus.
- Layar guru dan wali kelas yang memilih kelas memakai `classroomId` dari server untuk TA aktif. Rombel bernama sama di tahun lalu tidak boleh tertukar (CLAUDE.md: id, bukan nama).
- **Tidak ada bulan atau tanggal kegiatan yang ditanam di aplikasi.** Setiap tahun sekolah menyusun Kalender Akademik sendiri. Contohnya, kartu survei peminatan muncul karena server mengirim kapabilitasnya, bukan karena bulannya April. Layar Kalender menampilkan label kegiatan kunci dari server (kode `milestone`) apa adanya, dan bertindak berdasarkan kode, bukan teks.

### 80.5: Kriteria Selesai
- [ ] Alur 80.1 berjalan ujung ke ujung terhadap server staging/lokal dengan seeder demo FASE 120.
- [ ] `FeatureCatalogTest`, `LocalizationTest`, dan `HardcodedUiTextTest` hijau. Screenshot lengkap.
- [ ] Bagian ini diperbarui menjadi `[SELESAI]`.

---

## FASE 81: SISI ANDROID FASE 121 `[GARIS BESAR]`
- **Orang tua:** ajukan izin/sakit anak (tanggal, alasan, foto surat dokter), lalu lihat status persetujuan wali kelas.
- **Wali kelas:** setujui/tolak izin di aplikasi.
- **Rapor terbit:** notifikasi dan PDF rapor yang sudah disahkan untuk orang tua dan siswa.
- **Pimpinan:** kotak masuk "Menunggu Persetujuan Saya" (setujui/tolak/kembalikan dengan catatan).

## FASE 82: RILIS `[GARIS BESAR]`
- Play Console atas akun sekolah, kebijakan privasi (UU PDP, pengguna di bawah umur), dan formulir Data Safety (data: identitas, presensi, nilai; kamera untuk QR; mikrofon untuk Tahsin).
- Jalur internal testing → pilot Januari 2027 (1–2 rombel) → produksi Juli 2027.
- Server staging untuk menghidupkan lagi pipeline E2E.
