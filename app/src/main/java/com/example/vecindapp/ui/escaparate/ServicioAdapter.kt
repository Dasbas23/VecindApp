package com.example.vecindapp.ui.escaparate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vecindapp.R
import com.example.vecindapp.data.entities.Servicio

/**
 * Adapter del RecyclerView para mostrar las tarjetas de servicios en el escaparate.
 *
 * Utiliza [ListAdapter] con [DiffUtil] para calcular las diferencias entre
 * listas de forma eficiente: solo se redibujan las tarjetas que han cambiado,
 * en lugar de refrescar toda la lista.
 *
 * Cada tarjeta muestra: título, categoría, coste en horas y pictograma ARASAAC.
 * Al pulsar una tarjeta se ejecuta [onServicioClick]. Al mantener pulsada se
 * ejecuta [onServicioLongClick] si está definido.
 *
 * @property onServicioClick     Lambda ejecutado al pulsar una tarjeta.
 * @property onServicioLongClick Lambda ejecutado al mantener pulsada una tarjeta
 *                               (opcional). Debe devolver `true` para consumir el evento.
 *
 * @see EscaparateFragment
 * @see EscaparateViewModel
 */
class ServicioAdapter(
    private val onServicioClick: (Servicio) -> Unit,
    private val onServicioLongClick: ((Servicio) -> Boolean)? = null
) : ListAdapter<Servicio, ServicioAdapter.ServicioViewHolder>(ServicioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        return ServicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    /**
     * ViewHolder que representa una tarjeta individual de servicio.
     *
     * Mantiene las referencias a las vistas del layout [R.layout.item_servicio]
     * para evitar llamadas repetidas a `findViewById`.
     */
    inner class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloServicio)
        private val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaServicio)
        private val tvCoste: TextView = itemView.findViewById(R.id.tvCosteServicio)
        private val ivPictograma: ImageView = itemView.findViewById(R.id.ivPictograma)

        /**
         * Vincula los datos de un [Servicio] a las vistas de la tarjeta.
         *
         * @param servicio Objeto [Servicio] a mostrar.
         */
        fun bind(servicio: Servicio) {
            tvTitulo.text = servicio.titulo
            tvCategoria.text = servicio.categoria.name
            tvCoste.text = itemView.context.getString(
                R.string.formato_coste_horas,
                servicio.costeHoras
            )

            // TODO: Cargar pictograma ARASAAC desde assets usando Glide
            // Glide.with(itemView.context)
            //     .load("file:///android_asset/pictogramas/${servicio.pictogramaId}.png")
            //     .into(ivPictograma)

            itemView.setOnClickListener { onServicioClick(servicio) }
            if (onServicioLongClick != null) {
                itemView.setOnLongClickListener { onServicioLongClick.invoke(servicio) }
            } else {
                itemView.setOnLongClickListener(null)
            }
        }
    }

    /**
     * Callback de [DiffUtil] para calcular diferencias entre dos listas de servicios.
     *
     * Compara por ID (identidad del objeto) y por contenido completo
     * (para detectar si un servicio ha sido editado).
     */
    class ServicioDiffCallback : DiffUtil.ItemCallback<Servicio>() {

        /** Dos items representan el mismo servicio si comparten PK. */
        override fun areItemsTheSame(oldItem: Servicio, newItem: Servicio): Boolean =
            oldItem.idServicio == newItem.idServicio

        /** El contenido ha cambiado si cualquier campo difiere. */
        override fun areContentsTheSame(oldItem: Servicio, newItem: Servicio): Boolean =
            oldItem == newItem
    }
}