package com.proyecto.modismos.models

data class Modismo(
    val palabra: String,
    val tipo: String,
    val definiciones:  List<String>,
    val sinonimos: List<String>,
    var isExpanded: Boolean = false
)