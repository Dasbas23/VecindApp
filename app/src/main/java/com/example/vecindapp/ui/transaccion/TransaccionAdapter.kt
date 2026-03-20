package com.example.vecindapp.ui.transaccion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vecindapp.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter del RecyclerView para mostrar las transacciones del usuario.
 *
 * Cada tarjeta muestra el servicio asociado, el rol del usuario
 * (comprador/vendedor), el estado, las horas y botones de acción
 * que varían según el contexto:
 *
 * | Estado    | Vendedor              | Comprador        |
 * |-----------|-----------------------|------------------|
 * | PENDIENTE | Aceptar / Rechazar    | Cancelar         |
 * | ACEPTADA  | Completar             | Cancelar         |
 * | COMPLETADA| (sin botones)         | (sin botones)    |
 * | CANCELADA | (sin botones)         | (sin botones)    |
 *
 * @property onAceptar   Lambda al pulsar "Aceptar".
 * @property onCompletar Lambda al pulsar "Completar".
 * @property onCancelar  Lambda al pulsar "Cancelar" o "Rechazar".
 */
class TransaccionAdapter(
    private val onAceptar: (TransaccionUI) -> Unit,
    private val onCompletar: (TransaccionUI) -> Unit,
    private val onCancelar: (TransaccionUI) -> Unit
) : ListAdapter<TransaccionUI, TransaccionAdapter.TransaccionViewHolder>(TransaccionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return TransaccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder para una tarjeta de transacción.
     */
    inner class TransaccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvRol: TextView = itemView.findViewById(R.id.tvRol)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoTransaccion)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloTransaccion)
        private val tvHoras: TextView = itemView.findViewById(R.id.tvHorasTransaccion)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaTransaccion)
        private val llBotones: LinearLayout = itemView.findViewById(R.id.llBotones)
        private val btnPositivo: MaterialButton = itemView.findViewById(R.id.btnPositivo)
        private val btnNegativo: MaterialButton = itemView.findViewById(R.id.btnNegativo)

        /**
         * Vincula los datos de una [TransaccionUI] a las vistas.
         */
        fun bind(item: TransaccionUI) {
            tvRol.text = item.rol
            tvEstado.text = item.estado.name
            tvTitulo.text = item.tituloServicio
            tvHoras.text = itemView.context.getString(
                R.string.formato_coste_horas,
                item.horas
            )

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = sdf.format(Date(item.timestamp))

            // Colorear estado
            tvEstado.setTextColor(
                when (item.estado) {
                    com.example.vecindapp.domain.model.EstadoTransaccion.PENDIENTE -> 0xFFF59E0B.toInt()
                    com.example.vecindapp.domain.model.EstadoTransaccion.ACEPTADA -> 0xFF3B82F6.toInt()
                    com.example.vecindapp.domain.model.EstadoTransaccion.COMPLETADA -> 0xFF10B981.toInt()
                    com.example.vecindapp.domain.model.EstadoTransaccion.CANCELADA -> 0xFFEF4444.toInt()
                }
            )

            // Configurar botones según permisos
            configurarBotones(item)
        }

        /**
         * Muestra u oculta los botones de acción según el rol y estado.
         */
        private fun configurarBotones(item: TransaccionUI) {
            val tieneAcciones = item.puedeAceptar || item.puedeCompletar || item.puedeCancelar

            if (!tieneAcciones) {
                llBotones.visibility = View.INVISIBLE
                return
            }

            llBotones.visibility = View.VISIBLE

            // Botón positivo: Aceptar o Completar
            when {
                item.puedeAceptar -> {
                    btnPositivo.visibility = View.VISIBLE
                    btnPositivo.text = itemView.context.getString(R.string.btn_aceptar)
                    btnPositivo.setOnClickListener { onAceptar(item) }
                }
                item.puedeCompletar -> {
                    btnPositivo.visibility = View.VISIBLE
                    btnPositivo.text = itemView.context.getString(R.string.btn_completar)
                    btnPositivo.setOnClickListener { onCompletar(item) }
                }
                else -> {
                    btnPositivo.visibility = View.INVISIBLE
                }
            }

            // Botón negativo: Cancelar
            if (item.puedeCancelar) {
                btnNegativo.visibility = View.VISIBLE
                btnNegativo.text = itemView.context.getString(R.string.btn_cancelar)
                btnNegativo.setOnClickListener { onCancelar(item) }
            } else {
                btnNegativo.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * DiffUtil para comparar transacciones.
     */
    class TransaccionDiffCallback : DiffUtil.ItemCallback<TransaccionUI>() {

        override fun areItemsTheSame(oldItem: TransaccionUI, newItem: TransaccionUI): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TransaccionUI, newItem: TransaccionUI): Boolean =
            oldItem == newItem
    }
}