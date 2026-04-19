# Transacciones: chip de rol + canceladas tachadas en Historial — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hacer que la memoria deje de mentir. (1) Pintar el chip de rol "Comprador" (azul) / "Vendedor" (verde) en las tarjetas de la pestaña Transacciones y (2) aplicar el tratamiento visual de transacción cancelada (tarjeta oscurecida + texto tachado con `Paint.STRIKE_THRU_TEXT_FLAG` + leyenda "Sin cargo") a las tarjetas de la pestaña "Canceladas" del Historial.

**Architecture:** Dos cambios quirúrgicos de UI sobre los `ListAdapter` existentes (`TransaccionAdapter`, `HistorialAdapter`). Todos los colores/strings ya existen en `colors.xml` / `strings.xml`; solo faltan dos drawables de chip y lógica en `bind()`. Sin tocar ViewModels, entidades ni repositorios. Sin tocar la lógica de botones de acción de TransaccionAdapter (explícitamente fuera de alcance por indicación del usuario).

**Tech Stack:** Kotlin + Android XML layouts + Material Components (`MaterialCardView`), `android.graphics.Paint`.

**Contexto previo (qué hay ya):**

- `colors.xml` ya define `rol_comprador`, `rol_comprador_text`, `rol_vendedor`, `rol_vendedor_text`, `cancelada_fondo`, `cancelada_texto`.
- `strings.xml` ya define `rol_comprador` = "Comprador", `rol_vendedor` = "Vendedor", `historial_sin_cargo` = "Sin cargo".
- `item_transaccion.xml` ya tiene `tvRol` con `background="@drawable/bg_chip_rol"` (shape neutro azul claro) — solo se usa, nunca se reasigna por rol.
- `TransaccionUI.rol` es el String "COMPRADOR" / "VENDEDOR" (generado en `TransaccionViewModel.kt:96`).
- `HistorialAdapter` ya reutiliza `item_transaccion.xml` pero reusa `tvRol` para mostrar "GANADAS"/"GASTADAS" — **no vamos a tocar ese comportamiento**, solo añadir el tratamiento de cancelada.
- Raíz de `item_transaccion.xml` es `MaterialCardView` con `id=cardTransaccion` → `itemView` del ViewHolder es directamente ese MaterialCardView (puede castearse).

---

## File Structure

**Nuevos archivos:**

- `app/src/main/res/drawable/bg_chip_comprador.xml` — fondo azul para el chip de rol Comprador.
- `app/src/main/res/drawable/bg_chip_vendedor.xml` — fondo verde para el chip de rol Vendedor.

**Archivos a modificar:**

- `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt` — en `bind()`, seleccionar drawable + color de texto + label localizado según `item.rol`.
- `app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt` — en `bind()`, aplicar o resetear tratamiento de cancelada (strike-through + "Sin cargo" + dim).

**Archivos NO tocar:**

- `TransaccionViewModel.kt`, `TransaccionUI.kt` (el string del rol ya está bien en mayúsculas para la lógica — solo mapeamos a label legible en el adapter).
- `HistorialViewModel.kt`, `HistorialItem`.
- `bg_chip_rol.xml` (queda como fallback neutral que Historial seguirá usando).
- Botones de acción de `TransaccionAdapter.configurarBotones()` (explícitamente fuera de alcance).

**Nota sobre tests:** El proyecto no tiene infraestructura de tests instrumentados / Espresso (solo existe `ExampleUnitTest.kt` como stub). Añadir una dependencia de Robolectric/Espresso solo para verificar cambios de color de chip sería YAGNI. La verificación será manual (install + check visual). Se describe cómo reproducir cada caso en los pasos "Verify".

---

### Task 1: Crear drawable `bg_chip_comprador.xml`

**Files:**
- Create: `app/src/main/res/drawable/bg_chip_comprador.xml`

- [ ] **Step 1: Crear el drawable**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="12dp" />
    <solid android:color="@color/rol_comprador" />
    <stroke android:width="1dp" android:color="@color/rol_comprador_borde" />
</shape>
```

- [ ] **Step 2: Verificar sintaxis compilando**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (si falla por otro motivo ajeno, el fallo de este archivo se vería como "Error parsing drawable bg_chip_comprador").

---

### Task 2: Crear drawable `bg_chip_vendedor.xml`

**Files:**
- Create: `app/src/main/res/drawable/bg_chip_vendedor.xml`

- [ ] **Step 1: Crear el drawable**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="12dp" />
    <solid android:color="@color/rol_vendedor" />
    <stroke android:width="1dp" android:color="@color/rol_vendedor_borde" />
</shape>
```

- [ ] **Step 2: Verificar sintaxis compilando**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

---

### Task 3: Pintar chip de rol en `TransaccionAdapter`

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt`

Añadimos un helper que, según el String del rol, pone (a) el drawable de fondo, (b) el color del texto y (c) el label legible ("Comprador"/"Vendedor") desde strings.xml. Se llama al principio de `bind()`. NO se toca `configurarBotones()`.

- [ ] **Step 1: Añadir imports necesarios**

En la parte superior de `TransaccionAdapter.kt`, junto al resto de imports, añadir:

```kotlin
import androidx.core.content.ContextCompat
```

(El resto de imports ya existen.)

- [ ] **Step 2: Añadir método `pintarChipRol` dentro de `TransaccionViewHolder`**

Ubicación: dentro de `inner class TransaccionViewHolder`, justo después del método `bind()` y antes de `configurarBotones()`. El método completo:

```kotlin
/**
 * Pinta el chip de rol según el rol del usuario en la transacción.
 * - "COMPRADOR" → fondo azul + texto azul oscuro + label "Comprador".
 * - "VENDEDOR"  → fondo verde + texto verde oscuro + label "Vendedor".
 */
private fun pintarChipRol(rol: String) {
    val ctx = itemView.context
    when (rol) {
        "COMPRADOR" -> {
            tvRol.setBackgroundResource(R.drawable.bg_chip_comprador)
            tvRol.setTextColor(ContextCompat.getColor(ctx, R.color.rol_comprador_text))
            tvRol.text = ctx.getString(R.string.rol_comprador)
        }
        "VENDEDOR" -> {
            tvRol.setBackgroundResource(R.drawable.bg_chip_vendedor)
            tvRol.setTextColor(ContextCompat.getColor(ctx, R.color.rol_vendedor_text))
            tvRol.text = ctx.getString(R.string.rol_vendedor)
        }
        else -> {
            tvRol.setBackgroundResource(R.drawable.bg_chip_rol)
            tvRol.setTextColor(ContextCompat.getColor(ctx, R.color.texto_ink))
            tvRol.text = rol
        }
    }
}
```

- [ ] **Step 3: Sustituir la línea `tvRol.text = item.rol` en `bind()` por la llamada al helper**

Localizar en `TransaccionAdapter.kt` dentro de `fun bind(item: TransaccionUI)` la línea actual:

```kotlin
tvRol.text = item.rol
```

Reemplazarla por:

```kotlin
pintarChipRol(item.rol)
```

No se toca nada más del `bind()` (ni `tvEstado`, ni `configurarBotones`, ni el click listener).

- [ ] **Step 4: Compilar e instalar**

Run: `./gradlew installDebug`
Expected: BUILD SUCCESSFUL + app instalada.

- [ ] **Step 5: Verificación visual manual**

1. Abrir la app, iniciar sesión con el usuario semilla (el que ofrece servicios).
2. Publicar un servicio desde el escaparate (si no hay uno activo) y solicitarlo desde el OTRO usuario (cerrar sesión, logarse como el segundo).
3. Ir a la pestaña **Transacciones**:
   - Como el usuario **solicitante** → tarjeta con chip **azul** y label **"Comprador"** arriba-izquierda.
   - Como el usuario **que ofrece** (cerrar sesión y logarse como el otro) → tarjeta con chip **verde** y label **"Vendedor"** arriba-izquierda.
4. En ambos casos el resto de la tarjeta (estado, título, horas, fecha, botones) queda **igual** que antes.

Expected: chip visible con el color del rol en ambos casos.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/drawable/bg_chip_comprador.xml \
        app/src/main/res/drawable/bg_chip_vendedor.xml \
        app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt
git commit -m "feat(ui): chip coloreado por rol (Comprador azul / Vendedor verde) en tarjetas de Transacciones"
```

---

### Task 4: Tratamiento visual de transacciones canceladas en `HistorialAdapter`

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt`

En `HistorialAdapter.bind()`:

1. Si `item.transaccion.estado == CANCELADA`:
   - Oscurecer la tarjeta (`cardBackgroundColor = cancelada_fondo`, `alpha = 0.75f`).
   - Activar `Paint.STRIKE_THRU_TEXT_FLAG` en `tvTitulo` y `tvHoras`.
   - Sobrescribir `tvHoras.text` con "Sin cargo" (`R.string.historial_sin_cargo`) y recolorearlo a `cancelada_texto`.
2. Si NO está cancelada (caso normal):
   - Resetear explícitamente (la reutilización de ViewHolder lo exige): `alpha = 1f`, `cardBackgroundColor = blanco_tarjetas`, limpiar flag de tachado.

**Importante:** el reset es obligatorio. Sin él, al hacer scroll un ViewHolder que mostró una cancelada se reutiliza y arrastra el tachado + fondo gris al siguiente completada que le toque.

- [ ] **Step 1: Añadir imports necesarios**

En la parte superior de `HistorialAdapter.kt`, junto al resto de imports, añadir:

```kotlin
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.google.android.material.card.MaterialCardView
```

- [ ] **Step 2: Añadir método `aplicarEstiloCancelada` dentro de `HistorialViewHolder`**

Ubicación: dentro de `inner class HistorialViewHolder`, justo después del método `bind()` (al final del ViewHolder). Método completo:

```kotlin
/**
 * Aplica o retira el tratamiento visual de transacción cancelada:
 * - Tarjeta oscurecida (fondo gris + alpha 0.75).
 * - Texto del título tachado.
 * - Horas sustituidas por "Sin cargo" tachado en gris.
 *
 * Se llama SIEMPRE en bind() para resetear correctamente al reciclarse
 * el ViewHolder en un ítem no cancelado.
 */
private fun aplicarEstiloCancelada(item: HistorialItem) {
    val ctx = itemView.context
    val card = itemView as MaterialCardView
    val cancelada = item.transaccion.estado == EstadoTransaccion.CANCELADA

    if (cancelada) {
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.cancelada_fondo))
        card.alpha = 0.75f

        tvTitulo.paintFlags = tvTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        tvHoras.text = ctx.getString(R.string.historial_sin_cargo)
        tvHoras.setTextColor(ContextCompat.getColor(ctx, R.color.cancelada_texto))
        tvHoras.paintFlags = tvHoras.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.blanco_tarjetas))
        card.alpha = 1f

        tvTitulo.paintFlags = tvTitulo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        tvHoras.paintFlags = tvHoras.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        // tvHoras.text y su color los vuelve a fijar bind() cada vez antes de llamar aquí.
    }
}
```

- [ ] **Step 3: Llamar a `aplicarEstiloCancelada` al final de `bind()`**

Localizar el final de `fun bind(item: HistorialItem)` en `HistorialAdapter.kt`. Justo antes del bloque `itemView.setOnClickListener { onItemClick?.invoke(item) }` (es decir, después de `llBotones.visibility = View.GONE`), añadir:

```kotlin
aplicarEstiloCancelada(item)
```

El `bind()` resultante queda (para referencia del orden correcto):

```kotlin
fun bind(item: HistorialItem) {
    val signo = if (item.esVendedor) "+" else "-"
    val colorHoras = if (item.esVendedor) 0xFF10B981.toInt() else 0xFFEF4444.toInt()

    tvRol.text = if (item.esVendedor) "GANADAS" else "GASTADAS"
    tvRol.setTextColor(colorHoras)

    tvEstado.text = item.transaccion.estado.name
    tvEstado.setTextColor(
        if (item.transaccion.estado.name == "COMPLETADA") 0xFF10B981.toInt()
        else 0xFF9CA3AF.toInt()
    )

    tvTitulo.text = item.tituloServicio

    tvHoras.text = signo + TtsHelper.formatearCosteHumano(item.transaccion.horasTransferidas)
    tvHoras.setTextColor(colorHoras)

    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    tvFecha.text = sdf.format(Date(item.transaccion.timestamp))

    llBotones.visibility = View.GONE

    aplicarEstiloCancelada(item)

    itemView.setOnClickListener {
        onItemClick?.invoke(item)
    }
}
```

**Observación clave:** `aplicarEstiloCancelada` va **después** de fijar `tvHoras.text` y `tvHoras.setTextColor` en `bind()`. Esto es intencional: si es cancelada, el helper los sobrescribe con "Sin cargo" + gris; si no, el helper deja los valores que acaba de fijar `bind()` y solo limpia el flag de tachado. El reset del flag de tachado debe ocurrir SIEMPRE que no sea cancelada, aunque el ViewHolder fuese nuevo.

- [ ] **Step 4: Compilar e instalar**

Run: `./gradlew installDebug`
Expected: BUILD SUCCESSFUL + app instalada.

- [ ] **Step 5: Verificación visual manual**

Preparación de datos — necesitamos al menos 1 cancelada y 1 completada para poder comparar y validar el reset de reciclaje.

1. Iniciar sesión. Crear un servicio desde el usuario A, solicitarlo desde el usuario B.
2. Como usuario A, **cancelar** la transacción en la pestaña Transacciones → debería aparecer en Historial → tab "Canceladas".
3. Crear un SEGUNDO servicio, solicitarlo, aceptarlo y **completarlo** con valoración → aparecerá en tab "Completadas".
4. Ir a **Historial → tab "Canceladas"**:
   - Tarjeta con fondo **gris claro**, alpha reducida (se ve apagada).
   - Título del servicio **tachado** (línea horizontal atravesándolo).
   - Donde antes salía `-1h` o `+2h` ahora pone **"Sin cargo"** en gris, también tachado.
   - Estado "CANCELADA" sigue en gris (como antes).
   - Chip lateral "GASTADAS/GANADAS" con su color (no es objetivo del cambio).
5. Ir a **tab "Completadas"**:
   - Las tarjetas aparecen **sin oscurecer**, título **sin tachar**, horas con su `+` o `-` en verde/rojo como siempre.
6. **Test crítico de reciclaje:** volver a "Canceladas" y hacer scroll (si hay más de una cancelada) o navegar fuera y volver; después ir a "Completadas". Confirmar que ninguna completada arrastra estilo de cancelada.

Expected: tratamiento visible solo en el tab Canceladas; ningún bleed entre tabs.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt
git commit -m "feat(ui): tratamiento visual de transacciones canceladas en Historial (tachado + 'Sin cargo' + tarjeta atenuada)"
```

---

## Self-review

- **Cobertura del spec:**
  - "chip coloreado por rol" → Task 1+2+3.
  - "no tocar el cálculo dinámico de botones" → explícitamente fuera de alcance en el plan; `configurarBotones()` no se toca.
  - "canceladas tachadas con STRIKE_THRU_TEXT_FLAG + leyenda Sin cargo en Historial tab canceladas" → Task 4.
- **Placeholders:** ninguno. Todo código mostrado íntegro.
- **Consistencia de nombres:** `pintarChipRol`, `aplicarEstiloCancelada`, `tvRol`, `tvTitulo`, `tvHoras`, `R.color.rol_comprador`, `R.color.rol_vendedor`, `R.color.cancelada_fondo`, `R.color.cancelada_texto`, `R.string.rol_comprador`, `R.string.rol_vendedor`, `R.string.historial_sin_cargo` — todos ya existen en el repo (verificado: `colors.xml:22-31`, `strings.xml:60-61,84`).
- **Riesgo de reciclaje:** mitigado explícitamente con la rama `else` de `aplicarEstiloCancelada` y la llamada incondicional desde `bind()`.
