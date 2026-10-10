# FASE 74.1/74.3 — E2E, ADB Stress & Memory/ANR Budget Suite

## Status jujur

Ditulis dan ditinjau dengan hati-hati terhadap kelas dan layar Sulaone yang
sungguh ada (lihat referensi file di tiap test), **tapi belum pernah
dijalankan** — sandbox tempat modul ini dibuat tidak memiliki Appium server,
emulator/device Android, `adb`, maupun `ANDROID_HOME`. Perlakukan run
pertama sebagai *shakedown run*, bukan sebagai suite yang sudah terbukti
hijau.

Roadmap aslinya (`android_implementation.md`, FASE 74) menyebut "Appium MCP,
ADB MCP, dan Proxyman MCP" — ketiganya tidak tersedia sebagai tool di sesi
manapun sejauh ini. Modul ini adalah versi nyata dan bisa dijalankan dari
maksud yang sama, memakai Appium/adb asli secara langsung (bukan lewat MCP),
supaya tidak berhenti di level dokumen rancangan saja.

## Kenapa modul terpisah, bukan bagian dari build utama

`e2e/` punya `settings.gradle` sendiri dan **sengaja tidak** di-`include`
dari `../settings.gradle` root. Suite ini butuh Appium server + emulator/
device yang sudah menjalankan APK debug — menaruh dependency
`appium-java-client`/`selenium` di classpath utama akan menambah risiko
resolusi dependency untuk setiap `./gradlew build` biasa, padahal suite ini
memang cuma bisa jalan di job CI berbasis device atau mesin developer yang
sudah pasang Appium.

## Prasyarat untuk benar-benar menjalankannya

1. Android SDK + emulator (atau device fisik) yang sudah menyala, dengan APK
   debug (`:app:assembleDebug`) ter-install.
2. Appium server jalan di `http://127.0.0.1:4723` (atau set `APPIUM_URL`).
3. `adb` ada di PATH.
4. Environment variable berikut (skenario akan gagal cepat dengan pesan
   jelas kalau tidak diset — bukan diam-diam pakai data palsu):
   - `E2E_STUDENT_ID`, `E2E_STUDENT_PASSWORD` — akun siswa nyata yang sudah
     di-seed di backend target.
   - `E2E_CBT_EXAM_TOKEN` — token 6 digit dari ujian yang sedang aktif
     (lihat `UtbkMobileDataSeeder`/seeder CBT yang relevan di sisi backend
     `sistem-terpadu` untuk menyiapkan data ujian aktif).
   - `ANDROID_E2E_UDID` (opsional) — kalau ada lebih dari satu device
     terhubung.

## Menjalankan

```bash
cd e2e
./gradlew test
```

## Kenapa `testTag`s baru muncul di kode app, bukan cuma di sini

Appium (lewat UiAutomator2) membaca *native accessibility tree*, bukan
semantics tree internal Compose. Sebelum FASE 74 ini, hampir semua layar
punya `contentDescription = null` di ikon-ikonnya dan nol `Modifier.testTag`
— artinya elemen interaktif kunci (tombol presensi, input token CBT, opsi
jawaban) tidak bisa ditarget driver eksternal sama sekali. Untuk membuat
FASE 74.1 benar-benar bisa dieksekusi (bukan cuma naskah test yang tidak
mungkin lolos), ditambahkan:

- `testTagsAsResourceId = true` di root Compose (`MainActivity.kt`, hanya
  build debug) — supaya `testTag` muncul sebagai `resource-id` asli di
  accessibility tree yang dibaca Appium/`adb shell uiautomator dump`.
- `testTag` pada: role tab login, field identifier/password, tombol login,
  `home_screen_root`, input token CBT, tombol
  mulai ujian, tiap opsi jawaban (`cbt_option_A`..`E`), tombol
  Selanjutnya/Kumpulkan, tombol konfirmasi submit, dan dialog hasil submit
  (online vs offline).

## Yang TIDAK diklaim di sini

- Kredensial test dan token ujian harus disediakan dari data nyata yang
  sudah di-seed — skrip ini tidak dan tidak boleh membuat data palsu untuk
  meloloskan dirinya sendiri.
- Skenario presensi GPS (`AttendanceGpsFlowE2ETest`, `mock_location.sh`)
  dihapus di FASE 78: presensi siswa sekarang hanya lewat scan QR kelas.
- `memory_anr_budget_check.sh` mem-parsing `dumpsys gfxinfo framestats`
  berdasar nama kolom header (bukan index tetap) karena formatnya berbeda
  antar versi Android — kalau di device target ternyata kolom
  `INTENDED_VSYNC`/`FRAME_COMPLETED` tidak ada, skrip akan melapor gagal
  parsing secara eksplisit, bukan diam-diam melaporkan "0 dropped frame".
