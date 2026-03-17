package com.example.vecindapp.domain.model

/**
 * Nivel del vecino — se calcula según el número de intercambios completados.
 */
enum class NivelVecino {
    NOVATO,      // 0-2 intercambios
    ACTIVO,      // 2-4
    VETERANO,    // 5-7
    REFERENTE    // 8+
}

/**
 * Categorías de servicios disponibles en el escaparate. **Implementar en vías futuras
 */
enum class CategoriaServicio {
    HOGAR,
    TECNOLOGIA,
    EDUCACION,
    COMPANIA,
    RECADOS,
    OTROS
}

/**
 * Estado del ciclo de vida de un servicio publicado.
 */
enum class EstadoServicio {
    ACTIVO,
    RESERVADO,
    COMPLETADO,
    CADUCADO
}

/**
 * Estado del ciclo de vida de una transacción.
 */
enum class EstadoTransaccion {
    PENDIENTE,
    ACEPTADA,
    COMPLETADA,
    CANCELADA
}
