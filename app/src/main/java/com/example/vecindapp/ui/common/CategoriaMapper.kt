package com.example.vecindapp.ui.common

import androidx.annotation.DrawableRes
import com.example.vecindapp.R
import com.example.vecindapp.domain.model.CategoriaServicio

/**
 * Mapea cada [CategoriaServicio] al recurso drawable de su pictograma ARASAAC.
 *
 * Centraliza la lógica de asignación de imagen para que ni el Adapter
 * ni los Fragments tengan que conocer los IDs de drawable directamente.
 */
object CategoriaMapper {

    /**
     * Devuelve el recurso drawable correspondiente a la categoría dada.
     *
     * @param categoria Categoría del servicio.
     * @return ID del recurso drawable (`@DrawableRes`).
     */
    @DrawableRes
    fun obtenerDrawable(categoria: CategoriaServicio): Int = when (categoria) {
        CategoriaServicio.RECADOS    -> R.drawable.ic_recados
        CategoriaServicio.COMPANÍA   -> R.drawable.ic_compania
        CategoriaServicio.EDUCACION  -> R.drawable.ic_educacion
        CategoriaServicio.TECNOLOGÍA -> R.drawable.ic_tecnologia
        CategoriaServicio.HOGAR      -> R.drawable.ic_hogar
        CategoriaServicio.OTROS      -> R.drawable.ic_otros
    }
}
