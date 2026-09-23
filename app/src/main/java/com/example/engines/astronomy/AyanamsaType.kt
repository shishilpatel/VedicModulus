package com.example.engines.astronomy

import swisseph.SweConst

enum class AyanamsaType(val sweConst: Int, val displayName: String) {
    LAHIRI(SweConst.SE_SIDM_LAHIRI, "Lahiri (Chitrapaksha)"),
    RAMAN(SweConst.SE_SIDM_RAMAN, "B.V. Raman"),
    KP(SweConst.SE_SIDM_KRISHNAMURTI, "Krishnamurti (KP)"),
    PUSHYA(SweConst.SE_SIDM_TRUE_PUSHYA, "True Pushya")
}
