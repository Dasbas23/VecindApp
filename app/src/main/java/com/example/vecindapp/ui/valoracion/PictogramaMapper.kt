package com.example.vecindapp.ui.valoracion

import android.content.Context
import com.example.vecindapp.R

/**
 * Mapeo centralizado de tags de pictogramas ARASAAC a sus recursos.
 *
 * Usado por [ValoracionBottomSheetFragment] (para TTS al seleccionar) y por
 * [DetalleValoracionBottomSheet] (para visualización en el historial),
 * evitando duplicar la misma lógica en dos clases.
 *
 * ## Tags soportados
 * - Bien: `bien_excelente`, `bien_amable`, `bien_puntual`
 * - Regular: `regular_normal`, `regular_mejorable`, `regular_lento`
 * - Mal: `mal_impuntual`, `mal_desagradable`, `mal_no_realizado`
 */
object PictogramaMapper {

    /**
     * Devuelve la descripción legible de un pictograma dado su tag interno.
     *
     * @param context Contexto para acceder a los recursos de strings.
     * @param tag     Tag interno del pictograma (p. ej. `"bien_excelente"`).
     * @return Descripción localizada (p. ej. `"Excelente"`), o el propio [tag] si no se reconoce.
     */
    fun obtenerDescripcion(context: Context, tag: String): String = when (tag) {
        "bien_excelente"    -> context.getString(R.string.desc_pictograma_bien1)
        "bien_amable"       -> context.getString(R.string.desc_pictograma_bien2)
        "bien_puntual"      -> context.getString(R.string.desc_pictograma_bien3)
        "regular_normal"    -> context.getString(R.string.desc_pictograma_regular1)
        "regular_mejorable" -> context.getString(R.string.desc_pictograma_regular2)
        "regular_lento"     -> context.getString(R.string.desc_pictograma_regular3)
        "mal_impuntual"     -> context.getString(R.string.desc_pictograma_mal1)
        "mal_desagradable"  -> context.getString(R.string.desc_pictograma_mal2)
        "mal_no_realizado"  -> context.getString(R.string.desc_pictograma_mal3)
        else                -> tag
    }

    /**
     * Devuelve el recurso drawable correspondiente a un tag de pictograma.
     *
     * @param tag Tag interno del pictograma.
     * @return ID del recurso drawable, o [android.R.drawable.ic_menu_help] si no se reconoce.
     */
    fun obtenerDrawable(tag: String): Int = when (tag) {
        "bien_excelente"    -> R.drawable.bien_excelente
        "bien_amable"       -> R.drawable.bien_amable
        "bien_puntual"      -> R.drawable.bien_puntual
        "regular_normal"    -> R.drawable.regular_ok
        "regular_mejorable" -> R.drawable.regular_mejorable
        "regular_lento"     -> R.drawable.regular_lento
        "mal_impuntual"     -> R.drawable.mal_impuntual
        "mal_desagradable"  -> R.drawable.mal_desagradable
        "mal_no_realizado"  -> R.drawable.mal_no_realizado
        else                -> android.R.drawable.ic_menu_help
    }
}
