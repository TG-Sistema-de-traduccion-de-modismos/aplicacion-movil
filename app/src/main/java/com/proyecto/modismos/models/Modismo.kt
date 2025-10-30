package com.proyecto.modismos.models

data class Modismo(
    val palabra: String,
    val tipo: String,
    val definiciones: List<String>,
    val sinonimos: List<String>,
    val ejemplos: List<Ejemplo> = emptyList(),
    var isExpanded: Boolean = false
)
