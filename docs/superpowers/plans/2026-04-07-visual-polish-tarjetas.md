# Mejoras Visuales en Tarjetas (Sprint 6) — Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Mejorar la presentación visual de las tarjetas de transacciones, historial y escaparate con diferenciación de roles, estilo para canceladas y truncado de textos.

**Architecture:** Solo cambios de presentación (XML + Adapter bind). No se toca lógica de negocio, navegación, TTS ni valoraciones. HistorialAdapter reutiliza `item_transaccion.xml`, por lo que los cambios en ese layout afectan a ambas pantallas — el Adapter debe manejar condicionalmente los nuevos elementos.

**Tech Stack:** Kotlin, XML layouts, MaterialCardView, ConstraintLayout, Android Paint flags.

---

## Hallazgos del análisis previo

1. **`item_transaccion.xml`** ya tiene `maxLines="2"` + `ellipsize="end"` en `tvTituloTransaccion` — la Tarea 3 NO necesita tocarlo.
2. **`item_servicio.xml`** ya tiene `maxLines="2"` + `ellipsize="end"` en `tvTituloServicio` — la Tarea 3 NO necesita tocarlo.
3. **No existe `item_historial.xml`** — `HistorialAdapter` reutiliza `item_transaccion.xml`. La Tarea 3 queda reducida a verificar que ya está hecho (no hay cambios).
4. `TransaccionUI.rol` ya contiene el string "COMPRADOR" o "VENDEDOR".
5. `HistorialItem.esVendedor` + `transaccion.estado` proporcionan todo lo necesario para la Tarea 2.

---

## File Structure

| Archivo | Acción | Tarea |
|---------|--------|-------|
| `app/src/main/res/layout/item_transaccion.xml` | Modificar: añadir View lateral de color + chip de rol | T1 |
| `app/src/main/java/.../transaccion/TransaccionAdapter.kt` | Modificar: bind del color lateral + chip según rol | T1 |
| `app/src/main/java/.../historial/HistorialAdapter.kt` | Modificar: ocultar chip/borde de T1 + estilo cancelada | T2 |
| `app/src/main/res/values/colors.xml` | Modificar: añadir colores para comprador/vendedor/cancelada | T1, T2 |
| `app/src/main/res/values/strings.xml` | Modificar: añadir strings "Comprador"/"Vendedor"/"Sin cargo" | T1, T2 |

---

### Task 1: Diferenciación visual comprador/vendedor en transacciones

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/layout/item_transaccion.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt`

**Estrategia:** Añadir una `View` estrecha (4dp) al lado izquierdo del `ConstraintLayout` como indicador de color. Convertir el `tvRol` existente en un chip visual con fondo redondeado y color. En el Adapter, setear el color del borde lateral y del chip según `item.rol`.

- [ ] **Step 1: Añadir colores y strings**

En `colors.xml`, añadir:
```xml
<!-- Roles en transacciones -->
<color name="rol_comprador">#FFE3F2FD</color>       <!-- azul claro fondo -->
<color name="rol_comprador_text">#FF1565C0</color>   <!-- azul oscuro texto -->
<color name="rol_comprador_borde">#FF42A5F5</color>  <!-- azul borde lateral -->
<color name="rol_vendedor">#FFE8F5E9</color>         <!-- verde claro fondo -->
<color name="rol_vendedor_text">#FF2E7D32</color>    <!-- verde oscuro texto -->
<color name="rol_vendedor_borde">#FF66BB6A</color>   <!-- verde borde lateral -->
```

En `strings.xml`, añadir en la sección de Transacciones:
```xml
<string name="rol_comprador">Comprador</string>
<string name="rol_vendedor">Vendedor</string>
```

- [ ] **Step 2: Modificar item_transaccion.xml**

Envolver el contenido del `MaterialCardView` en un `ConstraintLayout` exterior que contenga:
1. Una `View` con `android:id="@+id/viewBordeLateral"`, width=4dp, height=0dp, constraída top/bottom del parent, start del parent. Fondo redondeado solo en esquinas izquierdas.
2. El `ConstraintLayout` existente pasa a tener `layout_marginStart="0dp"` (el borde queda fuera del padding).

Cambiar `tvRol` para que parezca un chip:
- Añadir `android:background="@drawable/bg_chip_rol"` (un shape drawable con corners 12dp y fondo sólido).
- Añadir `android:paddingHorizontal="8dp"`, `android:paddingVertical="2dp"`.

Crear drawable `bg_chip_rol.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="12dp" />
    <solid android:color="@color/rol_comprador" />
</shape>
```

Enfoque simplificado para el borde lateral: en lugar de una View separada fuera del padding, usar `app:strokeWidth` y `app:strokeColor` en el `MaterialCardView` — PERO esto pone borde en los 4 lados. Mejor opción: añadir la `View` de 4dp DENTRO del ConstraintLayout existente, posicionada absolutamente a la izquierda con margin negativo para compensar el contentPadding, o eliminar el `contentPadding` del card y manejar el padding internamente.

**Solución final para el borde lateral:**
- Quitar `app:contentPadding="16dp"` del `MaterialCardView`.
- Añadir `android:padding="16dp"` al `ConstraintLayout` interior.
- Añadir `View` con `id=viewBordeLateral`, width=4dp, height=match_parent, sin padding, constraída al start del ConstraintLayout con margin negativo de -16dp para que quede pegada al borde izquierdo del card.

Alternativa más limpia: NO usar borde lateral. Usar el `strokeColor` del MaterialCardView que ya cambiará según rol + el chip. Esto es más sencillo y Material Design friendly.

**Solución definitiva (más limpia):**
- Usar `app:strokeWidth="2dp"` + `app:strokeColor` programáticamente en el `MaterialCardView`.
- Además, tintar el `cardBackgroundColor` con un color muy sutil (azul/verde claro al 10%).
- El chip `tvRol` con fondo, texto coloreado y texto "Comprador"/"Vendedor".

Layout final de `item_transaccion.xml`: cambiar el `MaterialCardView` para añadir `app:strokeWidth="0dp"` como default (se seteará en código). Añadir `android:id="@+id/cardTransaccion"` al `MaterialCardView` para poder referenciarlo desde el Adapter.

```xml
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardTransaccion"
    ...
    app:strokeWidth="2dp"
    app:strokeColor="@android:color/transparent">
```

Para `tvRol`, cambiar a:
```xml
<TextView
    android:id="@+id/tvRol"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="@drawable/bg_chip_rol"
    android:paddingHorizontal="8dp"
    android:paddingVertical="2dp"
    android:textSize="11sp"
    android:textStyle="bold"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    tools:text="Comprador" />
```

- [ ] **Step 3: Crear drawable bg_chip_rol.xml**

Crear `app/src/main/res/drawable/bg_chip_rol.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="12dp" />
    <solid android:color="#FFE3F2FD" />
</shape>
```

El color del `solid` se cambiará programáticamente con `GradientDrawable.setColor()`.

- [ ] **Step 4: Modificar TransaccionAdapter.kt**

En `bind()`, después de setear `tvRol.text`, añadir lógica de color:

```kotlin
// Chip de rol con color
val esComprador = item.rol == "COMPRADOR"
tvRol.text = if (esComprador)
    itemView.context.getString(R.string.rol_comprador)
else
    itemView.context.getString(R.string.rol_vendedor)

val chipBg = tvRol.background as? android.graphics.drawable.GradientDrawable
if (esComprador) {
    chipBg?.setColor(itemView.context.getColor(R.color.rol_comprador))
    tvRol.setTextColor(itemView.context.getColor(R.color.rol_comprador_text))
    cardView.strokeColor = itemView.context.getColor(R.color.rol_comprador_borde)
    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.rol_comprador))
} else {
    chipBg?.setColor(itemView.context.getColor(R.color.rol_vendedor))
    tvRol.setTextColor(itemView.context.getColor(R.color.rol_vendedor_text))
    cardView.strokeColor = itemView.context.getColor(R.color.rol_vendedor_borde)
    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.rol_vendedor))
}
```

Añadir al ViewHolder:
```kotlin
private val cardView: MaterialCardView = itemView as MaterialCardView
```

Imports necesarios:
```kotlin
import android.graphics.drawable.GradientDrawable
import com.google.android.material.card.MaterialCardView
```

- [ ] **Step 5: Verificar que HistorialAdapter no se rompe**

`HistorialAdapter` también usa `item_transaccion.xml` y accede a `tvRol`. Actualmente setea `tvRol.text = "GANADAS"/"GASTADAS"` y le pone color. No accede al `cardView` ni al background del chip. Necesita actualizarse para:
1. Setear el background del chip `tvRol` con `GradientDrawable.setColor()` según `esVendedor`.
2. Resetear `strokeColor` y `cardBackgroundColor` a valores neutrales (blanco + transparente) para que no hereden colores de tarjetas recicladas.

En `HistorialAdapter.bind()`, añadir:
```kotlin
val cardView = itemView as MaterialCardView
val chipBg = tvRol.background as? GradientDrawable

// Resetear estilos del card para historial (no usa borde de rol)
cardView.strokeColor = android.graphics.Color.TRANSPARENT
cardView.setCardBackgroundColor(android.graphics.Color.WHITE)

// Chip de rol mantiene colores de ganadas/gastadas
chipBg?.setColor(colorHoras and 0x33FFFFFF or 0x11000000) // versión sutil del color
```

Simplificación: en historial, el chip ya muestra "GANADAS"/"GASTADAS" con color. Solo necesitamos resetear el card y darle fondo al chip con un tono sutil del mismo color.

- [ ] **Step 6: Build y commit**

```bash
./gradlew assembleDebug
git add app/src/main/res/values/colors.xml \
        app/src/main/res/values/strings.xml \
        app/src/main/res/layout/item_transaccion.xml \
        app/src/main/res/drawable/bg_chip_rol.xml \
        app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt \
        app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt
git commit -m "feat: diferenciación visual comprador/vendedor en transacciones"
```

---

### Task 2: Tarjetas canceladas en gris con texto tachado en historial

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt`

**Estrategia:** En `HistorialAdapter.bind()`, detectar `item.transaccion.estado == CANCELADA` y aplicar:
- Fondo gris claro al `cardView`.
- `Paint.STRIKE_THRU_TEXT_FLAG` al texto de horas.
- Reemplazar "+X/-X h" por "Sin cargo" en gris.
- Color gris secundario en título y fecha.
- Resetear estos estilos para items no cancelados (reciclaje de ViewHolder).

- [ ] **Step 1: Añadir colores y strings para canceladas**

En `colors.xml`:
```xml
<!-- Estado cancelada -->
<color name="cancelada_fondo">#FFF5F5F5</color>
<color name="cancelada_texto">#FF9E9E9E</color>
```

En `strings.xml`, sección Historial:
```xml
<string name="historial_sin_cargo">Sin cargo</string>
```

- [ ] **Step 2: Modificar HistorialAdapter.kt**

En `bind()`, después de la lógica actual, añadir bloque condicional:

```kotlin
val esCancelada = item.transaccion.estado == EstadoTransaccion.CANCELADA

if (esCancelada) {
    // Fondo gris claro
    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.cancelada_fondo))

    // Horas: "Sin cargo" en gris, con tachado
    tvHoras.text = itemView.context.getString(R.string.historial_sin_cargo)
    tvHoras.setTextColor(itemView.context.getColor(R.color.cancelada_texto))
    tvHoras.paintFlags = tvHoras.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

    // Título y fecha en gris secundario
    tvTitulo.setTextColor(itemView.context.getColor(R.color.cancelada_texto))
    tvFecha.setTextColor(itemView.context.getColor(R.color.cancelada_texto))
    tvRol.setTextColor(itemView.context.getColor(R.color.cancelada_texto))
} else {
    // Resetear estilos para ViewHolder reciclado
    tvHoras.paintFlags = tvHoras.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
    tvTitulo.setTextColor(0xFF212121.toInt()) // text primary por defecto
    tvFecha.setTextColor(0xFF212121.toInt())
    // cardView y tvRol ya se resetean arriba
}
```

Import necesario:
```kotlin
import com.example.vecindapp.domain.model.EstadoTransaccion
```

**IMPORTANTE:** El bloque `else` (reset) es crítico porque `RecyclerView` reutiliza ViewHolders. Sin él, una tarjeta completada podría mostrar estilos de cancelada si recicla un ViewHolder que antes mostraba una cancelada.

- [ ] **Step 3: Build y commit**

```bash
./gradlew assembleDebug
git add app/src/main/res/values/colors.xml \
        app/src/main/res/values/strings.xml \
        app/src/main/java/com/example/vecindapp/ui/historial/HistorialAdapter.kt
git commit -m "feat: tarjetas canceladas con estilo visual diferenciado"
```

---

### Task 3: Limitar textos largos con ellipsis en tarjetas

**Files:**
- Verify: `app/src/main/res/layout/item_servicio.xml`
- Verify: `app/src/main/res/layout/item_transaccion.xml`

**Resultado del análisis:** Ambos layouts YA tienen `android:maxLines="2"` y `android:ellipsize="end"` en sus TextViews de título. No existe `item_historial.xml` — el historial reutiliza `item_transaccion.xml`. **No hay cambios que hacer.**

- [ ] **Step 1: Verificar que los atributos existen**

Confirmar en `item_servicio.xml` líneas 41-42:
```xml
android:maxLines="2"
android:ellipsize="end"
```

Confirmar en `item_transaccion.xml` líneas 53-54:
```xml
android:maxLines="2"
android:ellipsize="end"
```

- [ ] **Step 2: Build de verificación**

```bash
./gradlew assembleDebug
```

Si compila correctamente, no se necesita commit — no hay cambios. Informar al usuario que esta tarea ya estaba implementada.

---

## Notas de implementación

- **Reciclaje de ViewHolder:** Cada `bind()` DEBE resetear todos los estilos visuales que modifica condicionalmente. Si seteas color/paintFlags/background en un caso, resétealos en el otro.
- **Colores hardcodeados vs recursos:** El código actual usa colores hardcodeados (0xFFF59E0B, etc.). Las nuevas adiciones usan recursos de `colors.xml` para los colores nuevos, manteniendo consistencia con lo existente para los que ya estaban.
- **MaterialCardView.strokeColor:** Se setea programáticamente. El XML define `app:strokeWidth="2dp"` como default; si no se quiere borde, se setea `strokeColor` a `Color.TRANSPARENT`.
