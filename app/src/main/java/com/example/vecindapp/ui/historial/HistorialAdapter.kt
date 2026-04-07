package com.example.vecindapp.ui.historial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.GradientDrawable
import com.example.vecindapp.R
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para la lista de transacciones del historial.
 *
 * Muestra cada transacción en modo solo lectura (sin botones de acción),
 * indicando el título del servicio, si el usuario ganó o gastó horas
 * (con signo +/-  en verde/rojo) y la fecha.
 */
class HistorialAdapter(
    private val usuarioActualId: Int,
    private val onItemClick: ((Int) -> Unit)? = null
) : ListAdapter<HistorialItem, HistorialAdapter.HistorialViewHolder>(HistorialDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que reutiliza el layout item_transaccion pero
     * oculta los botones y muestra datos de solo lectura.
     */
    inner class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val tvRol: TextView = itemView.findViewById(R.id.tvRol)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoTransaccion)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloTransaccion)
        private val tvHoras: TextView = itemView.findViewById(R.id.tvHorasTransaccion)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaTransaccion)
        private val llBotones: LinearLayout = itemView.findViewById(R.id.llBotones)

        fun bind(item: HistorialItem) {
            val signo = if (item.esVendedor) "+" else "-"
            val colorHoras = if (item.esVendedor) 0xFF10B981.toInt() else 0xFFEF4444.toInt()

            // Resetear estilos del card (puede estar reciclado)
            cardView.strokeColor = android.graphics.Color.TRANSPARENT
            cardView.setCardBackgroundColor(android.graphics.Color.WHITE)

            // Rol: GANADAS (verde) o GASTADAS (rojo) con chip
            tvRol.text = if (item.esVendedor) "GANADAS" else "GASTADAS"
            tvRol.setTextColor(colorHoras)
            val chipBg = tvRol.background as? GradientDrawable
            chipBg?.setColor(if (item.esVendedor) 0x2010B981 else 0x20EF4444)

            // Estado con color neutro (puede ser COMPLETADA o CANCELADA)
            tvEstado.text = item.transaccion.estado.name
            tvEstado.setTextColor(
                if (item.transaccion.estado.name == "COMPLETADA") 0xFF10B981.toInt()
                else 0xFF9CA3AF.toInt()
            )

            // Título del servicio (no el signo)
            tvTitulo.text = item.tituloServicio

            // Horas con signo y color
            tvHoras.text = itemView.context.getString(
                R.string.formato_historial_horas,
                signo,
                item.transaccion.horasTransferidas
            )
            tvHoras.setTextColor(colorHoras)

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = sdf.format(Date(item.transaccion.timestamp))

            // Historial es solo lectura
            llBotones.visibility = View.GONE

            // Add click listener
            itemView.setOnClickListener {
                onItemClick?.invoke(item.transaccion.idTransaccion)
            }
        }
    }

    class HistorialDiffCallback : DiffUtil.ItemCallback<HistorialItem>() {
        override fun areItemsTheSame(oldItem: HistorialItem, newItem: HistorialItem): Boolean =
            oldItem.transaccion.idTransaccion == newItem.transaccion.idTransaccion

        override fun areContentsTheSame(oldItem: HistorialItem, newItem: HistorialItem): Boolean =
            oldItem == newItem
    }
}
