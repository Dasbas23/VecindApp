package com.example.vecindapp.ui.historial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vecindapp.R
import com.example.vecindapp.data.entities.Transaccion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para la lista de transacciones completadas en el historial.
 *
 * Muestra cada transacción en modo solo lectura (sin botones de acción),
 * indicando si el usuario ganó o gastó horas y la fecha de finalización.
 *
 * @property usuarioActualId ID del usuario actual para determinar el rol.
 */
class HistorialAdapter(
    private val usuarioActualId: Int = 1
) : ListAdapter<Transaccion, HistorialAdapter.HistorialViewHolder>(HistorialDiffCallback()) {

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

        private val tvRol: TextView = itemView.findViewById(R.id.tvRol)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoTransaccion)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloTransaccion)
        private val tvHoras: TextView = itemView.findViewById(R.id.tvHorasTransaccion)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaTransaccion)
        private val llBotones: LinearLayout = itemView.findViewById(R.id.llBotones)

        fun bind(transaccion: Transaccion) {
            val esVendedor = transaccion.idVendedorFk == usuarioActualId
            val rol = if (esVendedor) "GANADAS" else "GASTADAS"
            val signo = if (esVendedor) "+" else "-"

            tvRol.text = rol
            tvRol.setTextColor(
                if (esVendedor) 0xFF10B981.toInt() else 0xFFEF4444.toInt()
            )

            tvEstado.text = transaccion.estado.name
            tvEstado.setTextColor(0xFF10B981.toInt()) // Verde para completada

            tvTitulo.text = itemView.context.getString(
                R.string.formato_historial_horas,
                signo,
                transaccion.horasTransferidas
            )

            tvHoras.text = itemView.context.getString(
                R.string.formato_coste_horas,
                transaccion.horasTransferidas
            )

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = sdf.format(Date(transaccion.timestamp))

            // Ocultar botones: el historial es solo lectura
            llBotones.visibility = View.GONE
        }
    }

    class HistorialDiffCallback : DiffUtil.ItemCallback<Transaccion>() {
        override fun areItemsTheSame(oldItem: Transaccion, newItem: Transaccion): Boolean =
            oldItem.idTransaccion == newItem.idTransaccion

        override fun areContentsTheSame(oldItem: Transaccion, newItem: Transaccion): Boolean =
            oldItem == newItem
    }
}