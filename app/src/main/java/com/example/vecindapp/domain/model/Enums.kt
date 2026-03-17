package com.example.vecindapp.domain.model

/**
 * Nivel de reputación de un vecino dentro de la comunidad.
 *
 * Se calcula automáticamente en función del número de intercambios
 * completados por el usuario. Cada nivel desbloquea mayor visibilidad
 * y confianza dentro del banco de tiempo.
 *
 * @see com.example.vecindapp.data.entities.Usuario.calcularNivel
 */
enum class NivelVecino {
    /** De 0 a 2 intercambios completados. */
    NOVATO,
    /** De 3 a 5 intercambios completados. */
    ACTIVO,
    /** De 6 a 8 intercambios completados. */
    VETERANO,
    /** 9 o más intercambios completados. */
    REFERENTE
}

/**
 * Categorías disponibles para clasificar los servicios del escaparate.
 *
 * Cada servicio publicado debe pertenecer a una única categoría,
 * lo que facilita el filtrado en el RecyclerView del escaparate.
 * @since **Implementar en vías futuras
 */
enum class CategoriaServicio {
    /** Tareas domésticas: limpieza, bricolaje, jardinería, etc. */
    HOGAR,
    /** Soporte técnico: ordenadores, móviles, redes, etc. */
    TECNOLOGIA,
    /** Clases particulares, idiomas, ayuda con deberes, etc. */
    EDUCACION,
    /** Acompañamiento, paseos, conversación, etc. */
    COMPANIA,
    /** Hacer la compra, recoger paquetes, trámites, etc. */
    RECADOS,
    /** Cualquier servicio que no encaje en las anteriores. */
    OTROS
}

/**
 * Estado del ciclo de vida de un servicio publicado.
 *
 * Un servicio nace como [ACTIVO], puede pasar a [RESERVADO] cuando
 * alguien lo solicita, y termina como [COMPLETADO] o [CADUCADO].
 */
enum class EstadoServicio {
    /** Visible en el escaparate y disponible para solicitar. */
    ACTIVO,
    /** Alguien lo ha solicitado y está pendiente de completarse. */
    RESERVADO,
    /** La transacción asociada se ha completado con éxito. */
    COMPLETADO,
    /** Ha superado su fecha de caducidad sin ser solicitado. */
    CADUCADO
}

/**
 * Estado del ciclo de vida de una transacción entre dos vecinos.
 *
 * Representa el flujo: [PENDIENTE] → [ACEPTADA] → [COMPLETADA].
 * En cualquier punto puede pasar a [CANCELADA].
 */
enum class EstadoTransaccion {
    /** Creada pero aún no aceptada por el vendedor. */
    PENDIENTE,
    /** Aceptada por el vendedor, servicio en curso. */
    ACEPTADA,
    /** Servicio realizado y horas transferidas. */
    COMPLETADA,
    /** Cancelada por cualquiera de las dos partes. */
    CANCELADA
}
