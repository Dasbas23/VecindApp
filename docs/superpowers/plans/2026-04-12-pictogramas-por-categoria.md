# Pictogramas automáticos por categoría — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Asignar automáticamente el pictograma de cada servicio según su categoría mediante un Mapper centralizado, eliminando la dependencia del campo `pictogramaId` de la entidad.

**Architecture:** Un `object CategoriaMapper` en `ui/common/` centraliza el mapeo `CategoriaServicio → @DrawableRes Int`. El `ServicioAdapter` y `DetalleServicioFragment` lo consumen para pintar el `ImageView`. La columna `pictogramaId` de Room se mantiene intacta (sin migración) pero se marca como deprecated.

**Tech Stack:** Kotlin, Android Resources (`@DrawableRes`), Room (sin cambios de esquema)

---

## Estructura de archivos

| Acción  | Archivo                                                                 | Responsabilidad                           |
|---------|-------------------------------------------------------------------------|-------------------------------------------|
| Crear   | `app/src/main/java/com/example/vecindapp/ui/common/CategoriaMapper.kt` | Mapeo exhaustivo categoría → drawable     |
| Modificar | `app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt:65-74` | Usar mapper en `bind()`              |
| Modificar | `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt:174-177` | Usar mapper en `pintarDetalle()` |
| Modificar | `app/src/main/java/com/example/vecindapp/data/entities/Servicio.kt:69`  | Marcar `pictogramaId` como deprecated    |

---

### Task 1: Crear CategoriaMapper

**Files:**
- Crear: `app/src/main/java/com/example/vecindapp/ui/common/CategoriaMapper.kt`

- [ ] **Step 1: Crear el archivo CategoriaMapper.kt**

```kotlin
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
```

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/common/CategoriaMapper.kt
git commit -m "feat(ui): crear CategoriaMapper para mapeo categoría → drawable"
```

---

### Task 2: Integrar CategoriaMapper en ServicioAdapter

**Files:**
- Modificar: `app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt:65-74`

- [ ] **Step 1: Eliminar bloque TODO de Glide y añadir llamada al mapper**

En el método `bind()` de `ServicioViewHolder` (líneas 65-74), reemplazar el bloque comentado:

```kotlin
// TODO: Cargar pictograma ARASAAC desde assets usando Glide
// Glide.with(itemView.context)
//     .load("file:///android_asset/pictogramas/${servicio.pictogramaId}.png")
//     .into(ivPictograma)
```

Por:

```kotlin
ivPictograma.setImageResource(CategoriaMapper.obtenerDrawable(servicio.categoria))
```

Añadir el import en la cabecera del archivo:

```kotlin
import com.example.vecindapp.ui.common.CategoriaMapper
```

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/escaparate/ServicioAdapter.kt
git commit -m "feat(escaparate): usar CategoriaMapper para pictogramas en tarjetas"
```

---

### Task 3: Integrar CategoriaMapper en DetalleServicioFragment

**Files:**
- Modificar: `app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt:174-177`

- [ ] **Step 1: Añadir pictograma en pintarDetalle()**

En el método `pintarDetalle(servicio: Servicio)` (línea 174), después de `servicioActual = servicio` y antes de `tvTitulo.text = ...`, añadir:

```kotlin
ivPictograma.setImageResource(CategoriaMapper.obtenerDrawable(servicio.categoria))
```

Añadir el import en la cabecera del archivo:

```kotlin
import com.example.vecindapp.ui.common.CategoriaMapper
```

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/servicio/Detalleserviciofragment.kt
git commit -m "feat(detalle): usar CategoriaMapper para pictograma en detalle de servicio"
```

---

### Task 4: Marcar pictogramaId como deprecated en la entidad Servicio

**Files:**
- Modificar: `app/src/main/java/com/example/vecindapp/data/entities/Servicio.kt:69`

- [ ] **Step 1: Añadir comentario de deprecación**

Encima de la línea 69 (`val pictogramaId: String`), añadir:

```kotlin
    // Deprecated: El pictograma ahora se determina dinámicamente por la categoría usando CategoriaMapper.
```

La columna NO se elimina para evitar migraciones de Room.

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit final**

```bash
git add app/src/main/java/com/example/vecindapp/data/entities/Servicio.kt
git commit -m "feat(ui): pictogramas automáticos por categoría de servicio usando CategoriaMapper"
```
