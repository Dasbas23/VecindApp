# Valoración unidireccional (solo Comprador) — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corregir el flujo de valoración para que solo el Comprador pueda valorar tras una transacción COMPLETADA. El Vendedor marca como completado y su ciclo termina.

**Architecture:** Cambios quirúrgicos en 4 ficheros — una condición en ViewModel, una cláusula SQL en DAO, eliminación de código muerto en Fragment/ViewModel (auto-popup de valoración al completar), y actualización de KDoc afectado.

**Tech Stack:** Kotlin, Room, MVVM, StateFlow

---

## Diagnóstico del problema

### Regla de negocio correcta (unidireccional)

```
Vendedor → realiza servicio → marca COMPLETADA → FIN (no valora)
Comprador → recibe servicio → ve botón "Valorar" → valora al Vendedor
```

### Puntos rotos en el código actual

| # | Fichero | Línea(s) | Problema |
|---|---------|----------|----------|
| 1 | `TransaccionViewModel.kt` | 106 | `puedeValorar` no filtra por rol — ambos (comprador y vendedor) pueden valorar |
| 2 | `TransaccionDao.kt` | 96-108 | `getConteoNotificaciones` cuenta COMPLETADA sin valorar para AMBOS participantes — infla el badge del vendedor |
| 3 | `TransaccionFragment.kt` | 176-205 | `observarTransaccionCompletada()` abre el BottomSheet de valoración tras completar — pero quien completa es SIEMPRE el vendedor (línea 100: `puedeCompletar = esVendedor && ...`) |
| 4 | `TransaccionViewModel.kt` | 63-65, 198, 211-213 | `_transaccionCompletada` StateFlow + `limpiarTransaccionCompletada()` — quedan como código muerto tras eliminar el auto-popup |

### Ficheros que NO necesitan cambios

| Fichero | Razón |
|---------|-------|
| `TransaccionAdapter.kt` | Solo renderiza lo que `puedeValorar` dicte — al corregir el ViewModel, el Adapter funciona correctamente |
| `TransaccionUI.kt` | El campo `puedeValorar: Boolean` es agnóstico al rol — solo transporta el flag |
| `ValoracionBottomSheetFragment.kt` | Recibe `valoradorId`/`valoradoId` como argumentos — no decide quién puede valorar |
| `DetalleServicioFragment.kt` | Solo muestra "Ver Valoración" si ya existe — no permite crear nuevas valoraciones |
| `DetalleServicioViewModel.kt` | `buscarValoracion()` busca cualquier valoración existente — correcto |
| `MainViewModel.kt` | Consume `getConteoNotificaciones()` del repositorio — al corregir la query del DAO, el badge se arregla en cascada |
| `TransaccionRepository.kt` / `TransaccionRepositoryImpl.kt` | Son pass-through del DAO — no añaden lógica propia |

---

## Task 1: Restringir `puedeValorar` al Comprador en el ViewModel

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionViewModel.kt:106`

- [ ] **Step 1: Modificar la condición `puedeValorar`**

En `enriquecerTransaccion()`, línea 106, cambiar:

```kotlin
// ANTES (roto): ambos pueden valorar
val puedeValorar = transaccion.estado == EstadoTransaccion.COMPLETADA && !yaValoradaPorUsuario
```

Por:

```kotlin
// DESPUÉS (correcto): solo el comprador puede valorar
val puedeValorar = !esVendedor
        && transaccion.estado == EstadoTransaccion.COMPLETADA
        && !yaValoradaPorUsuario
```

La variable `esVendedor` ya existe en la línea 95: `val esVendedor = transaccion.idVendedorFk == usuarioActualId`. Cuando `esVendedor == true`, `puedeValorar` siempre será `false`.

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionViewModel.kt
git commit -m "fix(valoracion): restringir puedeValorar exclusivamente al comprador

El vendedor ya no ve el botón Valorar en transacciones COMPLETADAS.
Solo el comprador (quien recibe el servicio) puede valorar."
```

---

## Task 2: Corregir la query del badge de notificaciones en el DAO

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/data/db/TransaccionDao.kt:96-108`

- [ ] **Step 1: Restringir la cláusula COMPLETADA al comprador**

En `getConteoNotificaciones()`, líneas 96-108, cambiar:

```kotlin
// ANTES (roto): cuenta COMPLETADA sin valorar para AMBOS participantes
@Query("""
    SELECT COUNT(*) FROM transaccion t
    WHERE (t.id_comprador_fk = :usuarioId OR t.id_vendedor_fk = :usuarioId)
    AND (
        t.estado = 'PENDIENTE'
        OR (t.estado = 'COMPLETADA' AND NOT EXISTS (
            SELECT 1 FROM valoracion v
            WHERE v.id_transaccion_fk = t.id_transaccion
            AND v.id_valorador_fk = :usuarioId
        ))
    )
""")
fun getConteoNotificaciones(usuarioId: Int): Flow<Int>
```

Por:

```kotlin
// DESPUÉS (correcto): COMPLETADA sin valorar solo cuenta para el comprador
@Query("""
    SELECT COUNT(*) FROM transaccion t
    WHERE (t.id_comprador_fk = :usuarioId OR t.id_vendedor_fk = :usuarioId)
    AND (
        t.estado = 'PENDIENTE'
        OR (t.estado = 'COMPLETADA'
            AND t.id_comprador_fk = :usuarioId
            AND NOT EXISTS (
                SELECT 1 FROM valoracion v
                WHERE v.id_transaccion_fk = t.id_transaccion
                AND v.id_valorador_fk = :usuarioId
            ))
    )
""")
fun getConteoNotificaciones(usuarioId: Int): Flow<Int>
```

La única diferencia es la línea nueva `AND t.id_comprador_fk = :usuarioId` dentro del bloque `COMPLETADA`. Esto asegura que el vendedor nunca ve "pendiente de valorar" en su badge.

- [ ] **Step 2: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/data/db/TransaccionDao.kt
git commit -m "fix(dao): badge solo cuenta valoraciones pendientes del comprador

La query getConteoNotificaciones ahora exige id_comprador_fk = usuarioId
en la cláusula COMPLETADA, evitando inflar el badge del vendedor."
```

---

## Task 3: Eliminar el auto-popup de valoración al completar

Cuando el vendedor pulsa "Completar", el código actual abre automáticamente el BottomSheet de valoración. Esto es incorrecto: el vendedor NO debe valorar. Eliminamos este flujo completo.

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionViewModel.kt:63-65, 198, 211-213`
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionFragment.kt:66, 176-205`

- [ ] **Step 1: Eliminar `_transaccionCompletada` del ViewModel**

En `TransaccionViewModel.kt`, eliminar las líneas 63-65:

```kotlin
// ELIMINAR estas 3 líneas:
/** Transacción recién completada (para abrir el BottomSheet de valoración). */
private val _transaccionCompletada = MutableStateFlow<TransaccionUI?>(null)
val transaccionCompletada: StateFlow<TransaccionUI?> = _transaccionCompletada
```

- [ ] **Step 2: Eliminar la emisión en `completarTransaccion()`**

En `TransaccionViewModel.kt`, línea 198, eliminar:

```kotlin
// ELIMINAR esta línea dentro de completarTransaccion():
_transaccionCompletada.value = item
```

- [ ] **Step 3: Eliminar `limpiarTransaccionCompletada()`**

En `TransaccionViewModel.kt`, eliminar las líneas 211-213:

```kotlin
// ELIMINAR este método completo:
fun limpiarTransaccionCompletada() {
    _transaccionCompletada.value = null
}
```

- [ ] **Step 4: Eliminar `observarTransaccionCompletada()` del Fragment**

En `TransaccionFragment.kt`, eliminar la llamada en `onViewCreated` (línea 66):

```kotlin
// ELIMINAR esta línea:
observarTransaccionCompletada()
```

Y eliminar el método completo (líneas 176-205):

```kotlin
// ELIMINAR este método completo:
private fun observarTransaccionCompletada() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.transaccionCompletada.collect { item ->
                if (item != null) {
                    val sesion = SesionUsuario(requireContext())
                    val miId = sesion.obtenerUsuarioId()
                    val valoradoId = if (item.transaccion.idVendedorFk == miId) {
                        item.transaccion.idCompradorFk
                    } else {
                        item.transaccion.idVendedorFk
                    }

                    val bottomSheet = ValoracionBottomSheetFragment.newInstance(
                        transaccionId = item.transaccion.idTransaccion,
                        valoradorId = miId,
                        valoradoId = valoradoId
                    )
                    bottomSheet.onDismissCallback = {
                        viewModel.cargarTransacciones()
                    }
                    bottomSheet.show(childFragmentManager, "valoracion")

                    viewModel.limpiarTransaccionCompletada()
                }
            }
        }
    }
}
```

- [ ] **Step 5: Eliminar imports muertos en Fragment**

Verificar si el import de `ValoracionBottomSheetFragment` sigue siendo necesario (sí lo es, porque `onValorar` en `configurarRecyclerView` todavía lo usa). El import de `SesionUsuario` también sigue vivo (se usa en `onValorar`). No hay imports que eliminar.

- [ ] **Step 6: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionViewModel.kt \
       app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionFragment.kt
git commit -m "refactor(transaccion): eliminar auto-popup de valoración al completar

El vendedor ya no recibe el BottomSheet de valoración tras completar.
Se elimina el StateFlow _transaccionCompletada y su observer, que quedan
como código muerto con la regla unidireccional."
```

---

## Task 4: Actualizar KDoc afectado por el cambio de regla

**Files:**
- Modify: `app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionViewModel.kt` (KDoc de clase, líneas 26-39)
- Modify: `app/src/main/java/com/example/vecindapp/data/db/TransaccionDao.kt` (KDoc de `getConteoNotificaciones`, líneas 85-94)

- [ ] **Step 1: Actualizar KDoc de `TransaccionViewModel`**

Reemplazar el bloque de flujo de estados (líneas 26-39) por:

```kotlin
/**
 * ViewModel para la pantalla de transacciones del usuario.
 *
 * Carga todas las transacciones donde el usuario participa (como
 * comprador o vendedor), las enriquece con datos de presentación
 * ([TransaccionUI]) y expone acciones para gestionar su ciclo de vida.
 *
 * ## Flujo de estados de una transacción
 * ```
 * PENDIENTE → ACEPTADA → COMPLETADA
 *     ↓          ↓
 *  CANCELADA  CANCELADA
 * ```
 *
 * ## Regla de valoración (unidireccional)
 * - El **Vendedor** completa la transacción. Su ciclo termina aquí.
 * - El **Comprador** es el único que puede valorar tras COMPLETADA.
 *
 * ## Lógica al COMPLETAR (operación atómica)
 * 1. Debitar horas del comprador.
 * 2. Acreditar horas al vendedor.
 * 3. Incrementar contador de intercambios de ambos.
 * 4. Cambiar estado de transacción a COMPLETADA.
 * 5. Cambiar estado del servicio a COMPLETADO.
 *
 * @property transaccionRepository Repositorio de transacciones.
 * @property servicioRepository    Repositorio de servicios.
 * @property usuarioRepository     Repositorio de usuarios.
 *
 * @see TransaccionFragment
 * @see TransaccionAdapter
 */
```

- [ ] **Step 2: Actualizar KDoc de `getConteoNotificaciones` en DAO**

Reemplazar el KDoc (líneas 85-94) por:

```kotlin
/**
 * Cuenta las transacciones que requieren atención del usuario:
 * - Estado PENDIENTE (por aceptar/cancelar) — cualquier participante.
 * - Estado COMPLETADA sin valoración — **solo si el usuario es el comprador**.
 *
 * Devuelve un [Flow] reactivo que se actualiza automáticamente
 * cuando cambian las tablas `transaccion` o `valoracion`.
 *
 * @param usuarioId ID del usuario activo.
 * @return [Flow] con el número de transacciones pendientes de atención.
 */
```

- [ ] **Step 3: Actualizar tabla KDoc del Adapter**

En `TransaccionAdapter.kt`, líneas 26-31, reemplazar la tabla:

```kotlin
 * | Estado    | Vendedor              | Comprador        |
 * |-----------|-----------------------|------------------|
 * | PENDIENTE | Aceptar / Rechazar    | Cancelar         |
 * | ACEPTADA  | Completar             | Cancelar         |
 * | COMPLETADA| (sin botones)         | Valorar          |
 * | CANCELADA | (sin botones)         | (sin botones)    |
```

- [ ] **Step 4: Verificar que compila**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionViewModel.kt \
       app/src/main/java/com/example/vecindapp/data/db/TransaccionDao.kt \
       app/src/main/java/com/example/vecindapp/ui/transaccion/TransaccionAdapter.kt
git commit -m "docs(valoracion): actualizar KDoc con regla unidireccional

Refleja que solo el comprador puede valorar y que el badge no cuenta
valoraciones pendientes para el vendedor."
```

---

## Task 5: Verificación funcional

- [ ] **Step 1: Build completo**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Ejecutar tests**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (o sin regresiones)

- [ ] **Step 3: Verificación manual en emulador**

Instalar con `./gradlew installDebug` y verificar estos escenarios:

| Escenario | Acción | Resultado esperado |
|-----------|--------|--------------------|
| Vendedor con transacción COMPLETADA | Abre pestaña Transacciones | NO ve botón "Valorar" |
| Comprador con transacción COMPLETADA sin valorar | Abre pestaña Transacciones | VE botón "Valorar" |
| Comprador ya valoró | Abre pestaña Transacciones | NO ve botón "Valorar" |
| Vendedor completa transacción | Pulsa "Completar" | Ve Snackbar de éxito, NO se abre BottomSheet de valoración |
| Badge del vendedor | Completa transacción | Badge NO sube por "valoración pendiente" |
| Badge del comprador | Transacción completada sin valorar | Badge SÍ cuenta esa transacción |

---

## Resumen de cambios por fichero

| Fichero | Cambio | Líneas |
|---------|--------|--------|
| `TransaccionViewModel.kt` | Añadir `!esVendedor` a `puedeValorar` | 106 |
| `TransaccionViewModel.kt` | Eliminar `_transaccionCompletada`, emisión, y `limpiarTransaccionCompletada()` | 63-65, 198, 211-213 |
| `TransaccionDao.kt` | Añadir `AND t.id_comprador_fk = :usuarioId` en cláusula COMPLETADA | 101 |
| `TransaccionFragment.kt` | Eliminar `observarTransaccionCompletada()` y su llamada | 66, 176-205 |
| `TransaccionViewModel.kt` | Actualizar KDoc de clase | 19-45 |
| `TransaccionDao.kt` | Actualizar KDoc de `getConteoNotificaciones` | 85-94 |
| `TransaccionAdapter.kt` | Actualizar tabla KDoc | 26-31 |
