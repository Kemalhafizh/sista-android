package com.sultanagung1.sista.core.feature

enum class FeatureFlagKey(val keyName: String, val defaultEnabled: Boolean, val description: String) {
    CBT_EXAM_ENABLED("cbt_exam_enabled", true, "Akses ujian CBT anti-cheat"),
    AI_SOCRATIC_TUTOR_ENABLED("ai_socratic_tutor_enabled", true, "Asisten bimbingan belajar Sultan AI"),
    AI_ESSAY_GRADER_ENABLED("ai_essay_grader_enabled", true, "Koreksi esai otomatis berbasis NLP AI"),
    BLOCKCHAIN_PASSPORT_ENABLED("blockchain_passport_enabled", false, "Paspor Ijazah Digital Web3 (Nonaktif)"),
    SMART_GATE_BIOMETRICS_ENABLED("smart_gate_biometrics_enabled", true, "Proteksi Sidik Jari m-Banking & Keystore Vault"),
    TAHSIN_RECORDER_ENABLED("tahsin_recorder_enabled", true, "Setoran audio tahsin & tahfidz Qur'an"),
    ONLINE_PAYMENT_VA_ENABLED("online_payment_va_enabled", true, "Pembayaran SPP online Virtual Account"),
    IN_APP_PDF_VIEWER_ENABLED("in_app_pdf_viewer_enabled", true, "Penampil dokumen & Rapor KKTP resmi"),
    EMERGENCY_SOS_PANIC_ENABLED("emergency_sos_panic_enabled", true, "Tombol SOS darurat anti-bullying"),
    WEARABLE_COMPANION_ENABLED("wearable_companion_enabled", true, "Sinkronisasi smartwatch Wear OS")
}

data class FeatureFlagState(
    val flags: Map<String, Boolean> = FeatureFlagKey.values().associate { it.keyName to it.defaultEnabled }
)
