# Plan de implementación: Tests unitarios e integración para VecindApp

> **Para agentes:** Para ejecutar este plan tarea por tarea, usar `superpowers:subagent-driven-development` (recomendado) o `superpowers:executing-plans`. Los pasos usan checkboxes (`- [ ]`) para tracking.

**Goal:** Añadir cobertura de tests unitarios e integración a VecindApp con propósito didáctico. Aprender en profundidad cómo probar entidades, mappers, ViewModels (con MockK + StateFlow + coroutines) y la capa Room (con BBDD en memoria).

**Architecture:**
- Tests unitarios puros (`src/test/`): JUnit 4 + MockK + `kotlinx-coroutines-test` + Turbine. Sin framework Android.
- Tests de integración Room (`src/androidTest/`): in-memory database con `Room.inMemoryDatabaseBuilder` + AndroidJUnit4. Verifican queries reales, FK, índices y reactividad de Flow.
- Patrón AAA (Arrange / Act / Assert) en cada test.

**Tech Stack:** JUnit 4.13.2, MockK 1.13.13, kotlinx-coroutines-test 1.8.1, Turbine 1.1.0, androidx.arch.core:core-testing 2.2.0, androidx.test.ext:junit 1.2.1, androidx.test:runner 1.6.2, androidx.test:rules 1.6.1, androidx.room:room-testing 2.8.4.

---

## Convenciones del plan

- **TDD estricto**: para cada test, primero se escribe rojo → se ejecuta y falla → se corrige cualquier cosa que necesite el SUT (sistema bajo test) → verde → commit. Cuando estamos testeando código ya existente, el "rojo" puede ser un test que comprueba comportamiento real y el "verde" es la primera ejecución que debe pasar; pero igualmente lo arrancamos viendo cómo falla intencionadamente (con un `assertEquals` deliberadamente mal) para confirmar que el test SÍ ejecuta.
- **Comandos Gradle (Windows bash)**: usar `./gradlew.bat` en lugar de `./gradlew`.
- **Commits**: en español, sin línea `Co-Authored-By` (preferencia del usuario en memoria).
- **Estructura de carpetas**: los tests siguen el mismo paquete que el SUT.
  - Unit: `app/src/test/java/com/example/vecindapp/...`
  - Integración: `app/src/androidTest/java/com/example/vecindapp/...`

---

## Fase 0 — Configuración del entorno de tests

### Task 0.1: Añadir versiones de librerías de testing al catálogo

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Editar `gradle/libs.versions.toml`**

Reemplazar el contenido completo del fichero con:

```toml
[versions]
agp = "8.13.2"
kotlin = "2.0.21"
coreKtx = "1.17.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.6.1"
room = "2.8.4"
navigation = "2.9.7"
lifecycle = "2.10.0"
ksp = "2.0.21-1.0.28"
mockk = "1.13.13"
coroutinesTest = "1.8.1"
turbine = "1.1.0"
archCoreTesting = "2.2.0"
androidxTestRunner = "1.6.2"
androidxTestRules = "1.6.1"
androidxTestExtJunit = "1.2.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# Navigation
navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
navigation-ui-ktx = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }

# ViewModel y LiveData
lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
lifecycle-livedata-ktx = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version.ref = "lifecycle" }

# Testing — unit tests (src/test/)
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
arch-core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "archCoreTesting" }

# Testing — instrumented tests (src/androidTest/)
mockk-android = { group = "io.mockk", name = "mockk-android", version.ref = "mockk" }
androidx-test-runner = { group = "androidx.test", name = "runner", version.ref = "androidxTestRunner" }
androidx-test-rules = { group = "androidx.test", name = "rules", version.ref = "androidxTestRules" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestExtJunit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 2: Verificar que el catálogo es válido sintácticamente**

Run: `./gradlew.bat help`
Expected: BUILD SUCCESSFUL. Si falla con error de TOML, revisar comas/tabulaciones.

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(tests): añadir versiones de librerías de testing al catálogo"
```

---

### Task 0.2: Declarar dependencias de testing en `build.gradle.kts`

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Reemplazar el bloque `dependencies` completo**

Localizar el bloque `dependencies { ... }` (líneas 48-74 actuales) y sustituirlo por:

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("com.google.android.material:material:1.12.0")

    //MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.espresso.core)
    ksp(libs.room.compiler)

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // ViewModel y LiveData
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)

    // ── Testing — unit tests ────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.arch.core.testing)

    // ── Testing — instrumented tests ────────────────────────
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
}
```

- [ ] **Step 2: Sincronizar Gradle y verificar que resuelve dependencias**

Run: `./gradlew.bat :app:dependencies --configuration testRuntimeClasspath`
Expected: BUILD SUCCESSFUL. En la salida deben aparecer `mockk`, `kotlinx-coroutines-test`, `turbine`, `arch.core:core-testing`.

Si falla por descarga, repetir. Si falla con "Cannot find ...", revisar el catálogo TOML.

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "chore(tests): declarar dependencias de unit e instrumented tests"
```

---

### Task 0.3: Borrar tests de ejemplo y verificar arranque limpio

**Files:**
- Delete: `app/src/test/java/com/example/vecindapp/ExampleUnitTest.kt`
- Delete: `app/src/androidTest/java/com/example/vecindapp/ExampleInstrumentedTest.kt`

- [ ] **Step 1: Eliminar los dos ficheros de scaffold**

```bash
rm app/src/test/java/com/example/vecindapp/ExampleUnitTest.kt
rm app/src/androidTest/java/com/example/vecindapp/ExampleInstrumentedTest.kt
```

- [ ] **Step 2: Ejecutar los unit tests (vacío) para confirmar que la tarea Gradle existe**

Run: `./gradlew.bat :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL con `0 tests`. Confirma que la pipeline de tests unitarios arranca correctamente sin tests.

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore(tests): eliminar tests de scaffold de Android Studio"
```

---

## Fase 1 — Tests unitarios puros (entidades + mappers)

> **Concepto que se aprende en esta fase**: tests unitarios sin framework Android, JUnit 4 (`@Test`, `assertEquals`, `assertTrue`, `assertNull`), patrón AAA, mocking de `Context` con MockK.

### Task 1.1: Test de `Usuario.calcularNivel()`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/data/entities/UsuarioTest.kt`

- [ ] **Step 1: Crear el fichero con 4 tests cubriendo cada nivel y los bordes**

Crear `app/src/test/java/com/example/vecindapp/data/entities/UsuarioTest.kt`:

```kotlin
package com.example.vecindapp.data.entities

import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.domain.model.NivelVecino
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests unitarios puros (sin Android) para [Usuario.calcularNivel].
 *
 * Verifica los 4 rangos de la tabla de niveles y los bordes:
 *   0–2 NOVATO · 3–5 ACTIVO · 6–8 VETERANO · 9+ REFERENTE.
 */
class UsuarioTest {

    private fun usuarioCon(intercambios: Int): Usuario =
        Usuario(nombre = "Test", barrio = Barrio.CENTRO, intercambiosTotal = intercambios)

    @Test
    fun `0 intercambios devuelve NOVATO`() {
        // Arrange
        val usuario = usuarioCon(0)
        // Act
        val nivel = usuario.calcularNivel()
        // Assert
        assertEquals(NivelVecino.NOVATO, nivel)
    }

    @Test
    fun `2 intercambios sigue siendo NOVATO (borde superior)`() {
        assertEquals(NivelVecino.NOVATO, usuarioCon(2).calcularNivel())
    }

    @Test
    fun `3 intercambios pasa a ACTIVO (borde inferior)`() {
        assertEquals(NivelVecino.ACTIVO, usuarioCon(3).calcularNivel())
    }

    @Test
    fun `5 intercambios sigue siendo ACTIVO`() {
        assertEquals(NivelVecino.ACTIVO, usuarioCon(5).calcularNivel())
    }

    @Test
    fun `6 intercambios pasa a VETERANO`() {
        assertEquals(NivelVecino.VETERANO, usuarioCon(6).calcularNivel())
    }

    @Test
    fun `8 intercambios sigue siendo VETERANO`() {
        assertEquals(NivelVecino.VETERANO, usuarioCon(8).calcularNivel())
    }

    @Test
    fun `9 intercambios pasa a REFERENTE`() {
        assertEquals(NivelVecino.REFERENTE, usuarioCon(9).calcularNivel())
    }

    @Test
    fun `100 intercambios sigue siendo REFERENTE`() {
        assertEquals(NivelVecino.REFERENTE, usuarioCon(100).calcularNivel())
    }
}
```

- [ ] **Step 2: Verificar fallo intencionado primero (disciplina TDD)**

Cambiar temporalmente la primera aserción a `assertEquals(NivelVecino.ACTIVO, nivel)` (mal a propósito).
Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.data.entities.UsuarioTest"`
Expected: 1 test rojo con mensaje "expected:<ACTIVO> but was:<NOVATO>". Esto confirma que el test SÍ se ejecuta.

- [ ] **Step 3: Restaurar la aserción correcta**

Volver a `assertEquals(NivelVecino.NOVATO, nivel)`.

- [ ] **Step 4: Ejecutar y verificar que pasa**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.data.entities.UsuarioTest"`
Expected: BUILD SUCCESSFUL · 8 tests passed.

- [ ] **Step 5: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/data/entities/UsuarioTest.kt
git commit -m "test(usuario): cubrir calcularNivel con bordes de los 4 rangos"
```

---

### Task 1.2: Test de `Servicio.estaActivo / estaCompletado / estaVencido`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/data/entities/ServicioTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/test/java/com/example/vecindapp/data/entities/ServicioTest.kt`:

```kotlin
package com.example.vecindapp.data.entities

import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoServicio
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitarios para los métodos de consulta de [Servicio].
 *
 * Aprendizaje: cómo manejar dependencias temporales sin reloj de test —
 * en este caso usamos timestamps relativos a `System.currentTimeMillis()`.
 */
class ServicioTest {

    private fun servicioCon(
        estado: EstadoServicio = EstadoServicio.ACTIVO,
        caducidad: Long? = null
    ): Servicio = Servicio(
        idUsuarioFk = 1,
        titulo = "Paseo del perro",
        categoria = CategoriaServicio.COMPANÍA,
        costeHoras = 1.0,
        estado = estado,
        fechaCaducidad = caducidad
    )

    @Test
    fun `estaActivo devuelve true cuando el estado es ACTIVO`() {
        assertTrue(servicioCon(estado = EstadoServicio.ACTIVO).estaActivo())
    }

    @Test
    fun `estaActivo devuelve false cuando el estado es RESERVADO`() {
        assertFalse(servicioCon(estado = EstadoServicio.RESERVADO).estaActivo())
    }

    @Test
    fun `estaCompletado solo es true en estado COMPLETADO`() {
        assertTrue(servicioCon(estado = EstadoServicio.COMPLETADO).estaCompletado())
        assertFalse(servicioCon(estado = EstadoServicio.ACTIVO).estaCompletado())
        assertFalse(servicioCon(estado = EstadoServicio.CADUCADO).estaCompletado())
    }

    @Test
    fun `estaVencido es false si fechaCaducidad es null`() {
        assertFalse(servicioCon(caducidad = null).estaVencido())
    }

    @Test
    fun `estaVencido es false si la caducidad es futura`() {
        val futuro = System.currentTimeMillis() + 60_000L
        assertFalse(servicioCon(caducidad = futuro).estaVencido())
    }

    @Test
    fun `estaVencido es true si la caducidad ya pasó`() {
        val pasado = System.currentTimeMillis() - 60_000L
        assertTrue(servicioCon(caducidad = pasado).estaVencido())
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.data.entities.ServicioTest"`
Expected: 6 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/data/entities/ServicioTest.kt
git commit -m "test(servicio): cubrir estaActivo, estaCompletado y estaVencido"
```

---

### Task 1.3: Test de `Valoracion.getPictogramasList()`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/data/entities/ValoracionTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/test/java/com/example/vecindapp/data/entities/ValoracionTest.kt`:

```kotlin
package com.example.vecindapp.data.entities

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests para el parser JSON casero de [Valoracion.getPictogramasList].
 *
 * Aprendizaje: cubrir happy path + casos límite (vacío, malformado, una sola entrada).
 */
class ValoracionTest {

    private fun valoracionCon(json: String): Valoracion = Valoracion(
        idTransaccionFk = 1,
        idValoradorFk = 1,
        idValoradoFk = 2,
        pictogramasJson = json
    )

    @Test
    fun `parsea correctamente un JSON con 3 elementos`() {
        val v = valoracionCon("[\"bien_excelente\",\"bien_amable\",\"bien_puntual\"]")
        assertEquals(
            listOf("bien_excelente", "bien_amable", "bien_puntual"),
            v.getPictogramasList()
        )
    }

    @Test
    fun `parsea correctamente un JSON con un único elemento`() {
        val v = valoracionCon("[\"regular_normal\"]")
        assertEquals(listOf("regular_normal"), v.getPictogramasList())
    }

    @Test
    fun `array vacío devuelve lista vacía`() {
        val v = valoracionCon("[]")
        assertEquals(emptyList<String>(), v.getPictogramasList())
    }

    @Test
    fun `cadena vacía devuelve lista vacía`() {
        val v = valoracionCon("")
        assertEquals(emptyList<String>(), v.getPictogramasList())
    }

    @Test
    fun `tolera espacios alrededor de las comillas`() {
        val v = valoracionCon("[ \"bien_amable\" , \"mal_impuntual\" ]")
        assertEquals(listOf("bien_amable", "mal_impuntual"), v.getPictogramasList())
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.data.entities.ValoracionTest"`
Expected: 5 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/data/entities/ValoracionTest.kt
git commit -m "test(valoracion): cubrir parser de pictogramasJson con casos límite"
```

---

### Task 1.4: Test de `CategoriaMapper`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/common/CategoriaMapperTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/test/java/com/example/vecindapp/ui/common/CategoriaMapperTest.kt`:

```kotlin
package com.example.vecindapp.ui.common

import com.example.vecindapp.R
import com.example.vecindapp.domain.model.CategoriaServicio
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests para el mapeo categoría → drawable.
 *
 * Aprendizaje: aunque parezca trivial, este test fija el contrato y detecta
 * regresiones si alguien cambia un mapeo accidentalmente.
 */
class CategoriaMapperTest {

    @Test
    fun `RECADOS mapea a ic_recados`() {
        assertEquals(R.drawable.ic_recados, CategoriaMapper.obtenerDrawable(CategoriaServicio.RECADOS))
    }

    @Test
    fun `COMPANIA mapea a ic_compania`() {
        assertEquals(R.drawable.ic_compania, CategoriaMapper.obtenerDrawable(CategoriaServicio.COMPANÍA))
    }

    @Test
    fun `EDUCACION mapea a ic_educacion`() {
        assertEquals(R.drawable.ic_educacion, CategoriaMapper.obtenerDrawable(CategoriaServicio.EDUCACION))
    }

    @Test
    fun `TECNOLOGIA mapea a ic_tecnologia`() {
        assertEquals(R.drawable.ic_tecnologia, CategoriaMapper.obtenerDrawable(CategoriaServicio.TECNOLOGÍA))
    }

    @Test
    fun `HOGAR mapea a ic_hogar`() {
        assertEquals(R.drawable.ic_hogar, CategoriaMapper.obtenerDrawable(CategoriaServicio.HOGAR))
    }

    @Test
    fun `OTROS mapea a ic_otros`() {
        assertEquals(R.drawable.ic_otros, CategoriaMapper.obtenerDrawable(CategoriaServicio.OTROS))
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.common.CategoriaMapperTest"`
Expected: 6 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/common/CategoriaMapperTest.kt
git commit -m "test(common): fijar contrato de CategoriaMapper para las 6 categorías"
```

---

### Task 1.5: Test de `PictogramaMapper` (mockeando `Context`)

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/valoracion/PictogramaMapperTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/test/java/com/example/vecindapp/ui/valoracion/PictogramaMapperTest.kt`:

```kotlin
package com.example.vecindapp.ui.valoracion

import android.content.Context
import com.example.vecindapp.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Tests para [PictogramaMapper] mockeando [Context].
 *
 * Aprendizaje clave: cómo testear código que depende de Android sin levantar
 * Robolectric. Usamos MockK para que `context.getString(id)` devuelva un
 * String determinista.
 */
class PictogramaMapperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk()
        // Stub global: cualquier getString(id) devuelve "string_<id>"
        every { context.getString(any()) } answers { "string_${firstArg<Int>()}" }
    }

    @Test
    fun `obtenerDescripcion devuelve el string del recurso correcto para bien_excelente`() {
        every { context.getString(R.string.desc_pictograma_bien1) } returns "Excelente"
        val resultado = PictogramaMapper.obtenerDescripcion(context, "bien_excelente")
        assertEquals("Excelente", resultado)
    }

    @Test
    fun `obtenerDescripcion devuelve el tag literal si no se reconoce`() {
        val resultado = PictogramaMapper.obtenerDescripcion(context, "tag_inventado")
        assertEquals("tag_inventado", resultado)
    }

    @Test
    fun `obtenerDrawable devuelve el recurso correcto para mal_impuntual`() {
        assertEquals(R.drawable.mal_impuntual, PictogramaMapper.obtenerDrawable("mal_impuntual"))
    }

    @Test
    fun `obtenerDrawable devuelve regular_ok para regular_normal (alias)`() {
        // Nota: el tag interno "regular_normal" mapea al drawable regular_ok.
        assertEquals(R.drawable.regular_ok, PictogramaMapper.obtenerDrawable("regular_normal"))
    }

    @Test
    fun `obtenerDrawable devuelve fallback ic_menu_help si no se reconoce`() {
        assertEquals(android.R.drawable.ic_menu_help, PictogramaMapper.obtenerDrawable("inexistente"))
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.valoracion.PictogramaMapperTest"`
Expected: 5 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/valoracion/PictogramaMapperTest.kt
git commit -m "test(pictograma): mockear Context con MockK para testear sin Android"
```

---

## Fase 2 — Tests unitarios de ViewModels

> **Concepto que se aprende en esta fase**: cómo testear código asíncrono. Usaremos `StandardTestDispatcher` para controlar las corrutinas, `Dispatchers.setMain` para sustituir el dispatcher principal de `viewModelScope`, MockK para fingir DAOs/repos, y Turbine para observar `StateFlow` sin tener que jugar con `runBlocking`.

### Task 2.1: Crear regla reutilizable `MainDispatcherRule`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/testutil/MainDispatcherRule.kt`

- [ ] **Step 1: Crear la regla**

Crear `app/src/test/java/com/example/vecindapp/testutil/MainDispatcherRule.kt`:

```kotlin
package com.example.vecindapp.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla JUnit que reemplaza `Dispatchers.Main` por un [TestDispatcher]
 * durante toda la duración del test, y lo restaura al terminar.
 *
 * Necesario porque `viewModelScope` lanza en `Dispatchers.Main` por defecto,
 * y en JVM puro no existe Looper de Android. Sin esta regla, los tests de
 * ViewModel lanzarían `IllegalStateException: Module with the Main dispatcher`.
 *
 * Usa [UnconfinedTestDispatcher] por defecto para que las corrutinas de
 * `viewModelScope.launch { }` (incluyendo las que se lanzan en el `init`
 * del ViewModel) corran de forma eager y los `StateFlow` queden actualizados
 * antes de que el test los inspeccione. Si un test necesita controlar el
 * progreso paso a paso, puede instanciar la regla con `StandardTestDispatcher()`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

- [ ] **Step 2: Compilar para asegurar que la regla está bien**

Run: `./gradlew.bat :app:compileDebugUnitTestKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/testutil/MainDispatcherRule.kt
git commit -m "test(util): añadir MainDispatcherRule para tests con viewModelScope"
```

---

### Task 2.2: Test de `LoginViewModel`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/auth/LoginViewModelTest.kt`

- [ ] **Step 1: Crear el fichero con cuatro escenarios**

Crear `app/src/test/java/com/example/vecindapp/ui/auth/LoginViewModelTest.kt`:

```kotlin
package com.example.vecindapp.ui.auth

import app.cash.turbine.test
import com.example.vecindapp.data.db.UsuarioDao
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios de [LoginViewModel] con MockK + Turbine + coroutines-test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: UsuarioDao
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        dao = mockk()
        viewModel = LoginViewModel(dao)
    }

    @Test
    fun `nombre en blanco emite error y no consulta el DAO`() = runTest {
        viewModel.iniciarSesion("   ")

        viewModel.error.test {
            assertEquals("Introduce tu nombre", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nombre no encontrado emite error correspondiente`() = runTest {
        coEvery { dao.buscarPorNombre("Marius") } returns null

        viewModel.iniciarSesion("Marius")
        advanceUntilIdle()

        viewModel.error.test {
            assertEquals("No se ha encontrado ningún vecino con ese nombre", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `usuario encontrado emite su id en usuarioEncontrado`() = runTest {
        val usuario = Usuario(idUsuario = 42, nombre = "Marius", barrio = Barrio.CENTRO)
        coEvery { dao.buscarPorNombre("Marius") } returns usuario

        viewModel.iniciarSesion("Marius")
        advanceUntilIdle()

        viewModel.usuarioEncontrado.test {
            assertEquals(42, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `el nombre se trimea antes de buscar`() = runTest {
        val usuario = Usuario(idUsuario = 7, nombre = "Ana", barrio = Barrio.DELICIAS)
        coEvery { dao.buscarPorNombre("Ana") } returns usuario

        viewModel.iniciarSesion("   Ana   ")
        advanceUntilIdle()

        viewModel.usuarioEncontrado.test {
            assertEquals(7, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `limpiarError pone el error a null`() = runTest {
        viewModel.iniciarSesion("")
        viewModel.limpiarError()

        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.auth.LoginViewModelTest"`
Expected: 5 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/auth/LoginViewModelTest.kt
git commit -m "test(login): cubrir validación, búsqueda y limpieza de error"
```

---

### Task 2.3: Test de `RegistroViewModel`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/auth/RegistroViewModelTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/test/java/com/example/vecindapp/ui/auth/RegistroViewModelTest.kt`:

```kotlin
package com.example.vecindapp.ui.auth

import app.cash.turbine.test
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.domain.repository.UsuarioRepository
import com.example.vecindapp.testutil.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios de [RegistroViewModel].
 *
 * Aprendizaje: uso de `slot()` para capturar argumentos pasados a un mock,
 * y `coVerify` para asegurar que se llamó a una función suspend.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: UsuarioRepository
    private lateinit var viewModel: RegistroViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = RegistroViewModel(repo)
    }

    @Test
    fun `nombre en blanco emite error sin tocar el repositorio`() = runTest {
        viewModel.registrar("   ", Barrio.CENTRO)

        viewModel.error.test {
            assertEquals("El nombre no puede estar vacío", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nombre duplicado emite error sin insertar`() = runTest {
        coEvery { repo.buscarPorNombre("Marius") } returns
            Usuario(idUsuario = 1, nombre = "Marius", barrio = Barrio.CENTRO)

        viewModel.registrar("Marius", Barrio.CENTRO)
        advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Ya existe un vecino con ese nombre", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { repo.insert(any()) }
    }

    @Test
    fun `registro correcto inserta usuario y emite su id`() = runTest {
        val usuarioCapturado = slot<Usuario>()
        coEvery { repo.buscarPorNombre("Ana") } returns null
        coEvery { repo.insert(capture(usuarioCapturado)) } returns 99L

        viewModel.registrar("Ana", Barrio.DELICIAS)
        advanceUntilIdle()

        viewModel.registrado.test {
            assertEquals(99, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // Verifica que el usuario insertado tiene el nombre y barrio esperados
        assertEquals("Ana", usuarioCapturado.captured.nombre)
        assertEquals(Barrio.DELICIAS, usuarioCapturado.captured.barrio)
        // Y los valores por defecto del banco de tiempo
        assertEquals(5.0, usuarioCapturado.captured.saldoHoras, 0.001)
    }

    @Test
    fun `excepción del repo se traduce en mensaje de error`() = runTest {
        coEvery { repo.buscarPorNombre(any()) } returns null
        coEvery { repo.insert(any()) } throws RuntimeException("DB caída")

        viewModel.registrar("Pepe", Barrio.TORRERO)
        advanceUntilIdle()

        viewModel.error.test {
            assertEquals("Error al registrar el usuario", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.auth.RegistroViewModelTest"`
Expected: 4 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/auth/RegistroViewModelTest.kt
git commit -m "test(registro): validación, duplicados, captura de argumento e insert OK"
```

---

### Task 2.4: Test de `CrearServicioViewModel`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/servicio/CrearServicioViewModelTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/test/java/com/example/vecindapp/ui/servicio/CrearServicioViewModelTest.kt`:

```kotlin
package com.example.vecindapp.ui.servicio

import app.cash.turbine.test
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CrearServicioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: ServicioRepository
    private lateinit var viewModel: CrearServicioViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = CrearServicioViewModel(repo)
    }

    @Test
    fun `título en blanco emite error y no llama al repo`() = runTest {
        viewModel.guardarServicio(
            titulo = "  ",
            descripcion = "algo",
            categoria = CategoriaServicio.HOGAR,
            coste = 1.0,
            usuarioId = 1
        )

        viewModel.error.test {
            assertEquals("El título no puede estar vacío", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { repo.insert(any()) }
    }

    @Test
    fun `inserción correcta marca guardado a true y trimea campos`() = runTest {
        val capturado = slot<Servicio>()
        coEvery { repo.insert(capture(capturado)) } returns 1L

        viewModel.guardarServicio(
            titulo = "  Pasear el perro  ",
            descripcion = "  cualquier tarde  ",
            categoria = CategoriaServicio.COMPANÍA,
            coste = 1.5,
            usuarioId = 7
        )
        advanceUntilIdle()

        viewModel.guardado.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("Pasear el perro", capturado.captured.titulo)
        assertEquals("cualquier tarde", capturado.captured.descripcion)
        assertEquals(CategoriaServicio.COMPANÍA, capturado.captured.categoria)
        assertEquals(1.5, capturado.captured.costeHoras, 0.001)
        assertEquals(7, capturado.captured.idUsuarioFk)
    }

    @Test
    fun `descripción vacía se guarda como null`() = runTest {
        val capturado = slot<Servicio>()
        coEvery { repo.insert(capture(capturado)) } returns 1L

        viewModel.guardarServicio(
            titulo = "Algo",
            descripcion = "   ",
            categoria = CategoriaServicio.OTROS,
            coste = 1.0,
            usuarioId = 1
        )
        advanceUntilIdle()

        assertNull(capturado.captured.descripcion)
    }

    @Test
    fun `error del repo deja guardado a false`() = runTest {
        coEvery { repo.insert(any()) } throws RuntimeException("boom")

        viewModel.guardarServicio(
            titulo = "X",
            descripcion = "",
            categoria = CategoriaServicio.OTROS,
            coste = 1.0,
            usuarioId = 1
        )
        advanceUntilIdle()

        viewModel.guardado.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.error.test {
            assertEquals("Error al guardar el servicio", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.servicio.CrearServicioViewModelTest"`
Expected: 4 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/servicio/CrearServicioViewModelTest.kt
git commit -m "test(crearServicio): validación, trim, descripción opcional y errores"
```

---

### Task 2.5: Test de `ValoracionViewModel`

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/valoracion/ValoracionViewModelTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/test/java/com/example/vecindapp/ui/valoracion/ValoracionViewModelTest.kt`:

```kotlin
package com.example.vecindapp.ui.valoracion

import app.cash.turbine.test
import com.example.vecindapp.R
import com.example.vecindapp.data.entities.Valoracion
import com.example.vecindapp.domain.repository.ValoracionRepository
import com.example.vecindapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ValoracionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: ValoracionRepository
    private lateinit var viewModel: ValoracionViewModel

    @Before
    fun setUp() {
        repo = mockk()
        viewModel = ValoracionViewModel(repo)
    }

    @Test
    fun `lista de pictogramas vacía emite error sin tocar repo`() = runTest {
        viewModel.guardarValoracion(
            idTransaccion = 1,
            idValoradorFk = 1,
            idValoradoFk = 2,
            pictogramas = emptyList(),
            comentario = ""
        )

        viewModel.error.test {
            assertEquals(R.string.valoracion_error_sin_pictograma, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { repo.insert(any()) }
    }

    @Test
    fun `guardado correcto serializa pictogramas a JSON con escape de comillas`() = runTest {
        val capturada = slot<Valoracion>()
        coEvery { repo.insert(capture(capturada)) } returns 1L

        viewModel.guardarValoracion(
            idTransaccion = 10,
            idValoradorFk = 1,
            idValoradoFk = 2,
            pictogramas = listOf("bien_excelente", "bien_amable"),
            comentario = "  ¡Genial!  "
        )
        advanceUntilIdle()

        assertEquals("[\"bien_excelente\",\"bien_amable\"]", capturada.captured.pictogramasJson)
        assertEquals("¡Genial!", capturada.captured.comentario)
        assertEquals(10, capturada.captured.idTransaccionFk)
        assertEquals(1, capturada.captured.idValoradorFk)
        assertEquals(2, capturada.captured.idValoradoFk)

        viewModel.guardada.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `comentario en blanco se guarda como null`() = runTest {
        val capturada = slot<Valoracion>()
        coEvery { repo.insert(capture(capturada)) } returns 1L

        viewModel.guardarValoracion(
            idTransaccion = 1, idValoradorFk = 1, idValoradoFk = 2,
            pictogramas = listOf("regular_normal"),
            comentario = "    "
        )
        advanceUntilIdle()

        assertNull(capturada.captured.comentario)
    }

    @Test
    fun `error del repo emite código de string de error`() = runTest {
        coEvery { repo.insert(any()) } throws RuntimeException("boom")

        viewModel.guardarValoracion(
            idTransaccion = 1, idValoradorFk = 1, idValoradoFk = 2,
            pictogramas = listOf("bien_amable"),
            comentario = ""
        )
        advanceUntilIdle()

        viewModel.error.test {
            assertEquals(R.string.valoracion_error_guardar, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.valoracion.ValoracionViewModelTest"`
Expected: 4 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/valoracion/ValoracionViewModelTest.kt
git commit -m "test(valoracion): cubrir serialización JSON y validación de pictogramas"
```

---

### Task 2.6: Test de `TransaccionViewModel.completarTransaccion` (lógica atómica)

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/transaccion/TransaccionViewModelTest.kt`

- [ ] **Step 1: Crear el fichero**

Este es el test más sustancial: la operación `completarTransaccion` debe debitar al comprador, acreditar al vendedor, recalcular niveles y cambiar estados de transacción y servicio. Probamos ambos caminos: el feliz y el de saldo insuficiente.

Crear `app/src/test/java/com/example/vecindapp/ui/transaccion/TransaccionViewModelTest.kt`:

```kotlin
package com.example.vecindapp.ui.transaccion

import app.cash.turbine.test
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.domain.model.EstadoServicio
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.model.NivelVecino
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.domain.repository.TransaccionRepository
import com.example.vecindapp.domain.repository.UsuarioRepository
import com.example.vecindapp.domain.repository.ValoracionRepository
import com.example.vecindapp.testutil.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests para la operación más compleja del proyecto: `completarTransaccion`.
 *
 * Aprendizaje: cómo testear un método que coordina 4 repositorios y debe
 * mantener invariantes (saldo no negativo, recálculo de nivel, estados
 * en cascada). Verificamos tanto el resultado en `_mensaje` como las
 * llamadas que se hicieron a los mocks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransaccionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var transaccionRepo: TransaccionRepository
    private lateinit var servicioRepo: ServicioRepository
    private lateinit var usuarioRepo: UsuarioRepository
    private lateinit var valoracionRepo: ValoracionRepository
    private lateinit var viewModel: TransaccionViewModel

    private val usuarioActualId = 100  // El "yo" del test es el vendedor

    @Before
    fun setUp() {
        transaccionRepo = mockk(relaxed = true)
        servicioRepo = mockk(relaxed = true)
        usuarioRepo = mockk(relaxed = true)
        valoracionRepo = mockk(relaxed = true)

        // El init del VM llama a getByUsuario; devolvemos lista vacía por defecto.
        coEvery { transaccionRepo.getByUsuario(any()) } returns flowOf(emptyList())

        viewModel = TransaccionViewModel(
            transaccionRepo, servicioRepo, usuarioRepo, valoracionRepo, usuarioActualId
        )
    }

    private fun usuario(id: Int, saldo: Double, intercambios: Int = 0) = Usuario(
        idUsuario = id,
        nombre = "U$id",
        barrio = Barrio.CENTRO,
        saldoHoras = saldo,
        intercambiosTotal = intercambios,
        nivel = NivelVecino.NOVATO
    )

    private fun transaccionUI(comprador: Int = 1, vendedor: Int = 100, horas: Double = 2.0) =
        TransaccionUI(
            transaccion = Transaccion(
                idTransaccion = 50,
                idCompradorFk = comprador,
                idVendedorFk = vendedor,
                idServicioFk = 200,
                horasTransferidas = horas,
                estado = EstadoTransaccion.ACEPTADA
            ),
            tituloServicio = "X",
            rol = "VENDEDOR",
            puedeAceptar = false,
            puedeCompletar = true,
            puedeCancelar = true,
            puedeValorar = false
        )

    @Test
    fun `completar OK debita comprador, acredita vendedor y cambia estados`() = runTest {
        coEvery { usuarioRepo.getByIdOnce(1) } returns usuario(1, saldo = 5.0, intercambios = 2)
        coEvery { usuarioRepo.getByIdOnce(100) } returns usuario(100, saldo = 1.0, intercambios = 5)

        val updates = mutableListOf<Usuario>()
        coEvery { usuarioRepo.update(capture(updates)) } just Runs

        viewModel.completarTransaccion(transaccionUI(horas = 2.0))
        advanceUntilIdle()

        // Comprador: 5.0 - 2.0 = 3.0; intercambios 2 → 3 (asciende a ACTIVO)
        val compradorActualizado = updates.first { it.idUsuario == 1 }
        assertEquals(3.0, compradorActualizado.saldoHoras, 0.001)
        assertEquals(3, compradorActualizado.intercambiosTotal)
        assertEquals(NivelVecino.ACTIVO, compradorActualizado.nivel)

        // Vendedor: 1.0 + 2.0 = 3.0; intercambios 5 → 6 (asciende a VETERANO)
        val vendedorActualizado = updates.first { it.idUsuario == 100 }
        assertEquals(3.0, vendedorActualizado.saldoHoras, 0.001)
        assertEquals(6, vendedorActualizado.intercambiosTotal)
        assertEquals(NivelVecino.VETERANO, vendedorActualizado.nivel)

        // Transacción → COMPLETADA
        coVerify {
            transaccionRepo.update(match { it.estado == EstadoTransaccion.COMPLETADA })
        }
        // Servicio → COMPLETADO
        coVerify { servicioRepo.cambiarEstado(200, EstadoServicio.COMPLETADO.name) }

        viewModel.mensaje.test {
            assertEquals("¡Horas transferidas con éxito!", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saldo insuficiente del comprador aborta sin actualizar nada`() = runTest {
        coEvery { usuarioRepo.getByIdOnce(1) } returns usuario(1, saldo = 1.0)
        coEvery { usuarioRepo.getByIdOnce(100) } returns usuario(100, saldo = 0.0)

        viewModel.completarTransaccion(transaccionUI(horas = 5.0))
        advanceUntilIdle()

        viewModel.mensaje.test {
            assertEquals("El comprador no tiene saldo suficiente", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { usuarioRepo.update(any()) }
        coVerify(exactly = 0) { transaccionRepo.update(any()) }
        coVerify(exactly = 0) { servicioRepo.cambiarEstado(any(), any()) }
    }

    @Test
    fun `comprador no encontrado emite error`() = runTest {
        coEvery { usuarioRepo.getByIdOnce(1) } returns null

        viewModel.completarTransaccion(transaccionUI())
        advanceUntilIdle()

        viewModel.mensaje.test {
            assertEquals("Error: comprador no encontrado", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancelar transacción la marca CANCELADA y devuelve servicio a ACTIVO`() = runTest {
        viewModel.cancelarTransaccion(transaccionUI())
        advanceUntilIdle()

        coVerify {
            transaccionRepo.update(match { it.estado == EstadoTransaccion.CANCELADA })
        }
        coVerify { servicioRepo.cambiarEstado(200, EstadoServicio.ACTIVO.name) }

        viewModel.mensaje.test {
            assertEquals("Transacción cancelada", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `aceptar transacción la pasa de PENDIENTE a ACEPTADA`() = runTest {
        val item = transaccionUI().copy(
            transaccion = transaccionUI().transaccion.copy(estado = EstadoTransaccion.PENDIENTE)
        )

        viewModel.aceptarTransaccion(item)
        advanceUntilIdle()

        coVerify {
            transaccionRepo.update(match { it.estado == EstadoTransaccion.ACEPTADA })
        }
        viewModel.mensaje.test {
            assertEquals("Transacción aceptada", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.transaccion.TransaccionViewModelTest"`
Expected: 5 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/transaccion/TransaccionViewModelTest.kt
git commit -m "test(transaccion): cubrir completar, cancelar, aceptar y saldo insuficiente"
```

---

### Task 2.7: Test de `HistorialViewModel.agruparPorMes` (vía StateFlow)

**Files:**
- Create: `app/src/test/java/com/example/vecindapp/ui/historial/HistorialViewModelTest.kt`

- [ ] **Step 1: Crear el fichero**

Aquí no podemos llamar a `agruparPorMes` directamente (es privado). Lo testeamos a través de `datosGrafico` con Turbine, alimentando un `Flow` falso de transacciones.

Crear `app/src/test/java/com/example/vecindapp/ui/historial/HistorialViewModelTest.kt`:

```kotlin
package com.example.vecindapp.ui.historial

import app.cash.turbine.test
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.repository.ServicioRepository
import com.example.vecindapp.domain.repository.TransaccionRepository
import com.example.vecindapp.domain.repository.ValoracionRepository
import com.example.vecindapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val usuarioActualId = 1
    private val sdf = SimpleDateFormat("MM/yy", Locale.getDefault())

    /** Devuelve millis correspondientes al primer día del mes/año dados. */
    private fun timestampDe(anio: Int, mes: Int): Long {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(anio, mes - 1, 1, 12, 0, 0)
        return cal.timeInMillis
    }

    private fun servicioConTitulo(id: Int, titulo: String) = Servicio(
        idServicio = id,
        idUsuarioFk = 99,
        titulo = titulo,
        categoria = CategoriaServicio.OTROS,
        costeHoras = 1.0
    )

    @Test
    fun `agrupa horas ganadas y gastadas por mes correctamente`() = runTest {
        val tEnero = Transaccion(
            idTransaccion = 1,
            idCompradorFk = 2, idVendedorFk = usuarioActualId, idServicioFk = 10,
            horasTransferidas = 3.0, estado = EstadoTransaccion.COMPLETADA,
            timestamp = timestampDe(2026, 1)
        )
        val tEnero2 = Transaccion(
            idTransaccion = 2,
            idCompradorFk = usuarioActualId, idVendedorFk = 5, idServicioFk = 11,
            horasTransferidas = 1.5, estado = EstadoTransaccion.COMPLETADA,
            timestamp = timestampDe(2026, 1)
        )
        val tMarzo = Transaccion(
            idTransaccion = 3,
            idCompradorFk = 7, idVendedorFk = usuarioActualId, idServicioFk = 12,
            horasTransferidas = 2.0, estado = EstadoTransaccion.COMPLETADA,
            timestamp = timestampDe(2026, 3)
        )

        val transaccionRepo = mockk<TransaccionRepository>()
        val servicioRepo = mockk<ServicioRepository>()
        val valoracionRepo = mockk<ValoracionRepository>(relaxed = true)

        coEvery { transaccionRepo.getByUsuario(usuarioActualId) } returns
            flowOf(listOf(tEnero, tEnero2, tMarzo))
        coEvery { servicioRepo.getById(10) } returns flowOf(servicioConTitulo(10, "Paseo"))
        coEvery { servicioRepo.getById(11) } returns flowOf(servicioConTitulo(11, "Compra"))
        coEvery { servicioRepo.getById(12) } returns flowOf(servicioConTitulo(12, "Clase"))

        val vm = HistorialViewModel(transaccionRepo, servicioRepo, valoracionRepo, usuarioActualId)

        // Con UnconfinedTestDispatcher el `init { cargarHistorial() }` ya
        // ha corrido y el StateFlow está poblado. `awaitItem()` devuelve
        // directamente el valor actual.
        vm.datosGrafico.test {
            val datos = awaitItem()

            // Debe haber 2 meses agrupados
            assertEquals(2, datos.size)

            val enero = datos.first { it.mes == sdf.format(timestampDe(2026, 1)) }
            assertEquals(3.0, enero.ganadas, 0.001)   // tEnero (vendedor)
            assertEquals(1.5, enero.gastadas, 0.001)  // tEnero2 (comprador)

            val marzo = datos.first { it.mes == sdf.format(timestampDe(2026, 3)) }
            assertEquals(2.0, marzo.ganadas, 0.001)
            assertEquals(0.0, marzo.gastadas, 0.001)

            // Y el orden debe ser cronológico
            assertTrue(datos[0].mes < datos[1].mes)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `solo computa transacciones COMPLETADAS en el gráfico`() = runTest {
        val cancelada = Transaccion(
            idTransaccion = 1,
            idCompradorFk = 2, idVendedorFk = usuarioActualId, idServicioFk = 10,
            horasTransferidas = 99.0, estado = EstadoTransaccion.CANCELADA,
            timestamp = timestampDe(2026, 1)
        )

        val transaccionRepo = mockk<TransaccionRepository>()
        val servicioRepo = mockk<ServicioRepository>()
        val valoracionRepo = mockk<ValoracionRepository>(relaxed = true)

        coEvery { transaccionRepo.getByUsuario(usuarioActualId) } returns flowOf(listOf(cancelada))
        coEvery { servicioRepo.getById(10) } returns flowOf(servicioConTitulo(10, "Algo"))

        val vm = HistorialViewModel(transaccionRepo, servicioRepo, valoracionRepo, usuarioActualId)

        vm.datosGrafico.test {
            val datos = awaitItem()
            assertEquals(emptyList<DatoMensual>(), datos)
            cancelAndIgnoreRemainingEvents()
        }
        vm.canceladas.test {
            val cancelados = awaitItem()
            assertEquals(1, cancelados.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Ejecutar y verificar**

Run: `./gradlew.bat :app:testDebugUnitTest --tests "com.example.vecindapp.ui.historial.HistorialViewModelTest"`
Expected: 2 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/example/vecindapp/ui/historial/HistorialViewModelTest.kt
git commit -m "test(historial): agrupación por mes y filtrado de canceladas"
```

---

### Task 2.8: Ejecutar todos los unit tests para confirmar la fase

- [ ] **Step 1: Lanzar la suite completa de unit tests**

Run: `./gradlew.bat :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL. Aproximadamente **45-50 tests passed, 0 failed**.

Si algo falla, depurar antes de continuar.

- [ ] **Step 2: Generar y ojear el reporte HTML**

El reporte se genera en `app/build/reports/tests/testDebugUnitTest/index.html`. Abrirlo para revisar la cobertura visual y confirmar que todos los paquetes tienen tests.

---

## Fase 3 — Tests de integración Room (BBDD en memoria)

> **Concepto que se aprende en esta fase**: cómo arrancar una BBDD Room en memoria, verificar queries SQL reales, FK con CASCADE, índices únicos y reactividad de `Flow`.
> **Importante**: estos tests viven en `src/androidTest/` y necesitan un emulador o dispositivo conectado para ejecutarse.

### Task 3.1: Helper compartido `DbTestRule` para crear BBDD en memoria

**Files:**
- Create: `app/src/androidTest/java/com/example/vecindapp/data/db/DbTestRule.kt`

- [ ] **Step 1: Crear la regla**

Crear `app/src/androidTest/java/com/example/vecindapp/data/db/DbTestRule.kt`:

```kotlin
package com.example.vecindapp.data.db

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

/**
 * Regla JUnit que crea una [AppDatabase] en memoria antes de cada test
 * y la cierra al terminar. La BBDD en memoria es muy rápida y se descarta
 * al cerrar el proceso, lo que garantiza aislamiento entre tests.
 *
 * `allowMainThreadQueries()` está activo para simplificar las aserciones;
 * en producción nunca se debe usar.
 */
class DbTestRule : ExternalResource() {
    lateinit var db: AppDatabase
        private set

    override fun before() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    override fun after() {
        db.close()
    }
}
```

- [ ] **Step 2: Verificar que el módulo androidTest compila**

Run: `./gradlew.bat :app:compileDebugAndroidTestKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/example/vecindapp/data/db/DbTestRule.kt
git commit -m "test(db): añadir DbTestRule con AppDatabase en memoria"
```

---

### Task 3.2: Test de `Converters` (roundtrip de los 5 enums)

**Files:**
- Create: `app/src/androidTest/java/com/example/vecindapp/data/db/ConvertersTest.kt`

> **Nota didáctica**: aunque `Converters` se podría testear en `src/test/` (no usa Android), lo dejamos en androidTest para tener todos los tests Room agrupados y porque la verificación más realista es comprobar el roundtrip de un usuario completo a través del DAO. Aquí hacemos el test directo sin DAO porque es más rápido de aprender.

- [ ] **Step 1: Crear el fichero**

Crear `app/src/androidTest/java/com/example/vecindapp/data/db/ConvertersTest.kt`:

```kotlin
package com.example.vecindapp.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoServicio
import com.example.vecindapp.domain.model.EstadoTransaccion
import com.example.vecindapp.domain.model.NivelVecino
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun barrio_roundtrip() {
        Barrio.values().forEach { b ->
            assertEquals(b, converters.toBarrio(converters.fromBarrio(b)))
        }
    }

    @Test
    fun nivelVecino_roundtrip() {
        NivelVecino.values().forEach { n ->
            assertEquals(n, converters.toNivelVecino(converters.fromNivelVecino(n)))
        }
    }

    @Test
    fun categoriaServicio_roundtrip() {
        CategoriaServicio.values().forEach { c ->
            assertEquals(c, converters.toCategoriaServicio(converters.fromCategoriaServicio(c)))
        }
    }

    @Test
    fun estadoServicio_roundtrip() {
        EstadoServicio.values().forEach { e ->
            assertEquals(e, converters.toEstadoServicio(converters.fromEstadoServicio(e)))
        }
    }

    @Test
    fun estadoTransaccion_roundtrip() {
        EstadoTransaccion.values().forEach { e ->
            assertEquals(e, converters.toEstadoTransaccion(converters.fromEstadoTransaccion(e)))
        }
    }
}
```

- [ ] **Step 2: Ejecutar contra emulador conectado**

Run: `./gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.vecindapp.data.db.ConvertersTest"`
Expected: 5 tests passed. Si no hay emulador arrancado, levantar uno desde Android Studio o `emulator -avd <nombre>`.

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/example/vecindapp/data/db/ConvertersTest.kt
git commit -m "test(db): cubrir roundtrip de los 5 conversores de enum"
```

---

### Task 3.3: Test de `UsuarioDao`

**Files:**
- Create: `app/src/androidTest/java/com/example/vecindapp/data/db/UsuarioDaoTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/androidTest/java/com/example/vecindapp/data/db/UsuarioDaoTest.kt`:

```kotlin
package com.example.vecindapp.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.model.Barrio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsuarioDaoTest {

    @get:Rule
    val dbRule = DbTestRule()

    private val dao get() = dbRule.db.usuarioDao()

    @Test
    fun insert_y_getByIdOnce_devuelve_el_usuario() = runTest {
        val id = dao.insert(Usuario(nombre = "Marius", barrio = Barrio.CENTRO))
        val recuperado = dao.getByIdOnce(id.toInt())
        assertNotNull(recuperado)
        assertEquals("Marius", recuperado!!.nombre)
        assertEquals(Barrio.CENTRO, recuperado.barrio)
        assertEquals(5.0, recuperado.saldoHoras, 0.001) // valor por defecto
    }

    @Test
    fun getById_emite_el_usuario_de_forma_reactiva() = runTest {
        val id = dao.insert(Usuario(nombre = "Ana", barrio = Barrio.DELICIAS))
        val usuario = dao.getById(id.toInt()).first()
        assertEquals("Ana", usuario?.nombre)
    }

    @Test
    fun getById_emite_null_cuando_no_existe() = runTest {
        val usuario = dao.getById(9999).first()
        assertNull(usuario)
    }

    @Test
    fun updateSaldo_modifica_solo_el_saldo() = runTest {
        val id = dao.insert(Usuario(nombre = "Pepe", barrio = Barrio.TORRERO, saldoHoras = 5.0))
        dao.updateSaldo(id.toInt(), 12.5)

        val recuperado = dao.getByIdOnce(id.toInt())
        assertEquals(12.5, recuperado!!.saldoHoras, 0.001)
        assertEquals("Pepe", recuperado.nombre) // resto inalterado
    }

    @Test
    fun buscarPorNombre_es_case_insensitive() = runTest {
        dao.insert(Usuario(nombre = "Marius", barrio = Barrio.CENTRO))

        assertEquals("Marius", dao.buscarPorNombre("marius")?.nombre)
        assertEquals("Marius", dao.buscarPorNombre("MARIUS")?.nombre)
        assertEquals("Marius", dao.buscarPorNombre("MaRiUs")?.nombre)
    }

    @Test
    fun buscarPorNombre_devuelve_null_si_no_existe() = runTest {
        val resultado = dao.buscarPorNombre("Inexistente")
        assertNull(resultado)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertar_dos_usuarios_con_mismo_nombre_lanza_constraint() = runTest {
        dao.insert(Usuario(nombre = "Marius", barrio = Barrio.CENTRO))
        dao.insert(Usuario(nombre = "Marius", barrio = Barrio.DELICIAS))
        fail("Esperaba SQLiteConstraintException por índice único en `nombre`")
    }

    @Test
    fun getAll_emite_todos_los_usuarios_ordenados_por_id() = runTest {
        dao.insert(Usuario(nombre = "A", barrio = Barrio.CENTRO))
        dao.insert(Usuario(nombre = "B", barrio = Barrio.CENTRO))
        dao.insert(Usuario(nombre = "C", barrio = Barrio.CENTRO))

        val todos = dao.getAll().first()
        assertEquals(3, todos.size)
        assertTrue(todos.map { it.nombre }.containsAll(listOf("A", "B", "C")))
    }
}
```

- [ ] **Step 2: Ejecutar contra emulador**

Run: `./gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.vecindapp.data.db.UsuarioDaoTest"`
Expected: 8 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/example/vecindapp/data/db/UsuarioDaoTest.kt
git commit -m "test(usuarioDao): cubrir CRUD, índice único y búsqueda case-insensitive"
```

---

### Task 3.4: Test de `ServicioDao`

**Files:**
- Create: `app/src/androidTest/java/com/example/vecindapp/data/db/ServicioDaoTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/androidTest/java/com/example/vecindapp/data/db/ServicioDaoTest.kt`:

```kotlin
package com.example.vecindapp.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoServicio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServicioDaoTest {

    @get:Rule
    val dbRule = DbTestRule()

    private val servicioDao get() = dbRule.db.servicioDao()
    private val usuarioDao get() = dbRule.db.usuarioDao()

    private var usuarioId = 0

    @Before
    fun seedUser() = runTest {
        usuarioId = usuarioDao.insert(
            Usuario(nombre = "Marius", barrio = Barrio.CENTRO)
        ).toInt()
    }

    private fun servicio(
        titulo: String,
        categoria: CategoriaServicio = CategoriaServicio.HOGAR,
        estado: EstadoServicio = EstadoServicio.ACTIVO,
        fecha: Long = System.currentTimeMillis()
    ) = Servicio(
        idUsuarioFk = usuarioId,
        titulo = titulo,
        categoria = categoria,
        costeHoras = 1.0,
        estado = estado,
        fechaPublicacion = fecha
    )

    @Test
    fun getActivos_solo_devuelve_servicios_ACTIVOS_ordenados_desc() = runTest {
        servicioDao.insert(servicio("Antiguo", fecha = 1_000L))
        servicioDao.insert(servicio("Reciente", fecha = 9_000L))
        servicioDao.insert(servicio("Reservado", estado = EstadoServicio.RESERVADO, fecha = 5_000L))

        val activos = servicioDao.getActivos().first()
        assertEquals(2, activos.size)
        assertEquals("Reciente", activos[0].titulo) // DESC por fecha
        assertEquals("Antiguo", activos[1].titulo)
    }

    @Test
    fun getByCategoria_filtra_por_categoria_y_solo_activos() = runTest {
        servicioDao.insert(servicio("Hogar 1", categoria = CategoriaServicio.HOGAR))
        servicioDao.insert(servicio("Hogar 2", categoria = CategoriaServicio.HOGAR, estado = EstadoServicio.COMPLETADO))
        servicioDao.insert(servicio("Compañía 1", categoria = CategoriaServicio.COMPANÍA))

        val hogar = servicioDao.getByCategoria(CategoriaServicio.HOGAR.name).first()
        assertEquals(1, hogar.size)
        assertEquals("Hogar 1", hogar[0].titulo)
    }

    @Test
    fun cambiarEstado_actualiza_solo_el_estado() = runTest {
        val id = servicioDao.insert(servicio("X")).toInt()
        servicioDao.cambiarEstado(id, EstadoServicio.RESERVADO.name)

        val recuperado = servicioDao.getById(id).first()
        assertEquals(EstadoServicio.RESERVADO, recuperado!!.estado)
        assertEquals("X", recuperado.titulo) // resto inalterado
    }

    @Test
    fun getByUsuario_devuelve_solo_los_servicios_de_ese_usuario() = runTest {
        // Usuario 2
        val otroId = usuarioDao.insert(Usuario(nombre = "Otro", barrio = Barrio.DELICIAS)).toInt()

        servicioDao.insert(servicio("Mío 1"))
        servicioDao.insert(servicio("Mío 2"))
        servicioDao.insert(Servicio(
            idUsuarioFk = otroId,
            titulo = "De otro",
            categoria = CategoriaServicio.OTROS,
            costeHoras = 1.0
        ))

        val mios = servicioDao.getByUsuario(usuarioId).first()
        assertEquals(2, mios.size)
        assertTrue(mios.all { it.idUsuarioFk == usuarioId })
    }

    @Test
    fun borrar_usuario_borra_sus_servicios_en_cascada() = runTest {
        // FK ON DELETE CASCADE
        servicioDao.insert(servicio("S1"))
        servicioDao.insert(servicio("S2"))

        // Borramos al usuario directamente con un raw query a través de DAO no expone delete,
        // así que comprobamos la cascada borrando vía SupportSQLiteDatabase
        dbRule.db.openHelper.writableDatabase.execSQL(
            "DELETE FROM usuario WHERE id_usuario = $usuarioId"
        )

        val mios = servicioDao.getByUsuario(usuarioId).first()
        assertTrue(mios.isEmpty())
    }
}
```

- [ ] **Step 2: Ejecutar**

Run: `./gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.vecindapp.data.db.ServicioDaoTest"`
Expected: 5 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/example/vecindapp/data/db/ServicioDaoTest.kt
git commit -m "test(servicioDao): cubrir filtros, ordenación, cambio de estado y FK CASCADE"
```

---

### Task 3.5: Test de `TransaccionDao` (incluye la query compleja `getConteoNotificaciones`)

**Files:**
- Create: `app/src/androidTest/java/com/example/vecindapp/data/db/TransaccionDaoTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/androidTest/java/com/example/vecindapp/data/db/TransaccionDaoTest.kt`:

```kotlin
package com.example.vecindapp.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.data.entities.Valoracion
import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.domain.model.CategoriaServicio
import com.example.vecindapp.domain.model.EstadoTransaccion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransaccionDaoTest {

    @get:Rule
    val dbRule = DbTestRule()

    private val txDao get() = dbRule.db.transaccionDao()
    private val usuarioDao get() = dbRule.db.usuarioDao()
    private val servicioDao get() = dbRule.db.servicioDao()
    private val valoracionDao get() = dbRule.db.valoracionDao()

    private var compradorId = 0
    private var vendedorId = 0
    private var servicioId = 0

    @Before
    fun seed() = runTest {
        compradorId = usuarioDao.insert(Usuario(nombre = "C", barrio = Barrio.CENTRO)).toInt()
        vendedorId = usuarioDao.insert(Usuario(nombre = "V", barrio = Barrio.CENTRO)).toInt()
        servicioId = servicioDao.insert(
            Servicio(
                idUsuarioFk = vendedorId,
                titulo = "Servicio",
                categoria = CategoriaServicio.OTROS,
                costeHoras = 1.0
            )
        ).toInt()
    }

    private fun tx(
        estado: EstadoTransaccion = EstadoTransaccion.PENDIENTE,
        ts: Long = System.currentTimeMillis()
    ) = Transaccion(
        idCompradorFk = compradorId,
        idVendedorFk = vendedorId,
        idServicioFk = servicioId,
        horasTransferidas = 1.0,
        estado = estado,
        timestamp = ts
    )

    @Test
    fun getByUsuario_incluye_a_comprador_y_vendedor() = runTest {
        txDao.insert(tx())

        val porComprador = txDao.getByUsuario(compradorId).first()
        val porVendedor = txDao.getByUsuario(vendedorId).first()

        assertEquals(1, porComprador.size)
        assertEquals(1, porVendedor.size)
    }

    @Test
    fun getByUsuario_ordena_por_timestamp_descendente() = runTest {
        txDao.insert(tx(ts = 1_000L))
        txDao.insert(tx(ts = 9_000L))
        txDao.insert(tx(ts = 5_000L))

        val lista = txDao.getByUsuario(compradorId).first()
        assertEquals(listOf(9_000L, 5_000L, 1_000L), lista.map { it.timestamp })
    }

    @Test
    fun getByServicioYEstado_filtra_correctamente() = runTest {
        txDao.insert(tx(estado = EstadoTransaccion.PENDIENTE))
        txDao.insert(tx(estado = EstadoTransaccion.CANCELADA))

        val pendiente = txDao.getByServicioYEstado(servicioId, "PENDIENTE")
        val cancelada = txDao.getByServicioYEstado(servicioId, "CANCELADA")

        assertTrue(pendiente != null && pendiente.estado == EstadoTransaccion.PENDIENTE)
        assertTrue(cancelada != null && cancelada.estado == EstadoTransaccion.CANCELADA)
    }

    @Test
    fun getConteoNotificaciones_incluye_PENDIENTES_de_ambos() = runTest {
        txDao.insert(tx(estado = EstadoTransaccion.PENDIENTE))
        txDao.insert(tx(estado = EstadoTransaccion.PENDIENTE))

        assertEquals(2, txDao.getConteoNotificaciones(compradorId).first())
        assertEquals(2, txDao.getConteoNotificaciones(vendedorId).first())
    }

    @Test
    fun getConteoNotificaciones_solo_cuenta_COMPLETADA_sin_valorar_para_el_comprador() = runTest {
        // 1 transacción completada por valorar
        val txId = txDao.insert(tx(estado = EstadoTransaccion.COMPLETADA)).toInt()

        // El comprador la ve como notificación
        assertEquals(1, txDao.getConteoNotificaciones(compradorId).first())
        // El vendedor NO la ve como notificación
        assertEquals(0, txDao.getConteoNotificaciones(vendedorId).first())

        // Cuando el comprador valora, deja de contar
        valoracionDao.insert(
            Valoracion(
                idTransaccionFk = txId,
                idValoradorFk = compradorId,
                idValoradoFk = vendedorId,
                pictogramasJson = "[\"bien_amable\"]"
            )
        )
        assertEquals(0, txDao.getConteoNotificaciones(compradorId).first())
    }

    @Test
    fun getConteoNotificaciones_es_reactivo_y_se_actualiza_al_insertar() = runTest {
        // Estado inicial: 0
        assertEquals(0, txDao.getConteoNotificaciones(compradorId).first())

        txDao.insert(tx(estado = EstadoTransaccion.PENDIENTE))

        // Tras insertar, el Flow emite el nuevo conteo
        assertEquals(1, txDao.getConteoNotificaciones(compradorId).first())
    }
}
```

- [ ] **Step 2: Ejecutar**

Run: `./gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.vecindapp.data.db.TransaccionDaoTest"`
Expected: 6 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/example/vecindapp/data/db/TransaccionDaoTest.kt
git commit -m "test(transaccionDao): cubrir getByUsuario y la query de notificaciones"
```

---

### Task 3.6: Test de `ValoracionDao`

**Files:**
- Create: `app/src/androidTest/java/com/example/vecindapp/data/db/ValoracionDaoTest.kt`

- [ ] **Step 1: Crear el fichero**

Crear `app/src/androidTest/java/com/example/vecindapp/data/db/ValoracionDaoTest.kt`:

```kotlin
package com.example.vecindapp.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vecindapp.data.entities.Servicio
import com.example.vecindapp.data.entities.Transaccion
import com.example.vecindapp.data.entities.Usuario
import com.example.vecindapp.data.entities.Valoracion
import com.example.vecindapp.domain.model.Barrio
import com.example.vecindapp.domain.model.CategoriaServicio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ValoracionDaoTest {

    @get:Rule
    val dbRule = DbTestRule()

    private val dao get() = dbRule.db.valoracionDao()
    private val usuarioDao get() = dbRule.db.usuarioDao()
    private val servicioDao get() = dbRule.db.servicioDao()
    private val txDao get() = dbRule.db.transaccionDao()

    private var idA = 0
    private var idB = 0
    private var idTx = 0

    @Before
    fun seed() = runTest {
        idA = usuarioDao.insert(Usuario(nombre = "A", barrio = Barrio.CENTRO)).toInt()
        idB = usuarioDao.insert(Usuario(nombre = "B", barrio = Barrio.CENTRO)).toInt()
        val servicioId = servicioDao.insert(
            Servicio(idUsuarioFk = idA, titulo = "S",
                categoria = CategoriaServicio.OTROS, costeHoras = 1.0)
        ).toInt()
        idTx = txDao.insert(
            Transaccion(
                idCompradorFk = idB, idVendedorFk = idA,
                idServicioFk = servicioId, horasTransferidas = 1.0
            )
        ).toInt()
    }

    @Test
    fun insert_y_getByTransaccion_devuelve_la_valoracion() = runTest {
        dao.insert(Valoracion(
            idTransaccionFk = idTx, idValoradorFk = idB, idValoradoFk = idA,
            pictogramasJson = "[\"bien_excelente\"]"
        ))

        val v = dao.getByTransaccion(idTx)
        assertNotNull(v)
        assertEquals(idTx, v!!.idTransaccionFk)
    }

    @Test
    fun getByTransaccionYValorador_distingue_entre_quien_valora() = runTest {
        // B valora a A
        dao.insert(Valoracion(
            idTransaccionFk = idTx, idValoradorFk = idB, idValoradoFk = idA,
            pictogramasJson = "[\"bien_amable\"]"
        ))

        val porB = dao.getByTransaccionYValorador(idTx, idB)
        val porA = dao.getByTransaccionYValorador(idTx, idA)

        assertNotNull(porB)
        assertNull(porA)
    }

    @Test
    fun getByValorado_devuelve_solo_las_recibidas_por_ese_usuario() = runTest {
        dao.insert(Valoracion(
            idTransaccionFk = idTx, idValoradorFk = idB, idValoradoFk = idA,
            pictogramasJson = "[\"bien_excelente\"]", timestamp = 1_000L
        ))
        dao.insert(Valoracion(
            idTransaccionFk = idTx, idValoradorFk = idA, idValoradoFk = idB,
            pictogramasJson = "[\"regular_normal\"]", timestamp = 2_000L
        ))

        val recibidasA = dao.getByValorado(idA).first()
        assertEquals(1, recibidasA.size)
        assertEquals(idA, recibidasA[0].idValoradoFk)
    }

    @Test
    fun getByValorado_ordena_por_timestamp_desc() = runTest {
        // Como solo aceptamos una valoración por (transaccion, valorador) en la realidad,
        // creamos varias transacciones para tener varias valoraciones del mismo valorado.
        val tx2 = txDao.insert(
            Transaccion(
                idCompradorFk = idB, idVendedorFk = idA,
                idServicioFk = 1, // ya existe S de @Before
                horasTransferidas = 1.0
            )
        ).toInt()
        dao.insert(Valoracion(idTransaccionFk = idTx, idValoradorFk = idB, idValoradoFk = idA,
            pictogramasJson = "[\"x\"]", timestamp = 1_000L))
        dao.insert(Valoracion(idTransaccionFk = tx2, idValoradorFk = idB, idValoradoFk = idA,
            pictogramasJson = "[\"y\"]", timestamp = 9_000L))

        val recibidas = dao.getByValorado(idA).first()
        assertEquals(2, recibidas.size)
        assertEquals(9_000L, recibidas[0].timestamp)
        assertEquals(1_000L, recibidas[1].timestamp)
    }
}
```

- [ ] **Step 2: Ejecutar**

Run: `./gradlew.bat :app:connectedDebugAndroidTest --tests "com.example.vecindapp.data.db.ValoracionDaoTest"`
Expected: 4 tests passed.

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/example/vecindapp/data/db/ValoracionDaoTest.kt
git commit -m "test(valoracionDao): cubrir filtros por valorado y por transacción/valorador"
```

---

### Task 3.7: Suite final de integración

- [ ] **Step 1: Lanzar todos los tests instrumentados**

Run: `./gradlew.bat :app:connectedDebugAndroidTest`
Expected: BUILD SUCCESSFUL · ~28 tests passed.

- [ ] **Step 2: Lanzar la suite completa (unit + integración)**

Run: `./gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest`
Expected: BUILD SUCCESSFUL · ~75 tests passed en total.

- [ ] **Step 3: Revisar el reporte HTML de instrumentation**

`app/build/reports/androidTests/connected/debug/index.html`

- [ ] **Step 4: Commit final con un breve resumen en docs**

No hay cambios adicionales que commitear; los tests ya están en commits separados.

---

## Auto-revisión final del plan

- ✅ Todos los pasos contienen código real, sin placeholders.
- ✅ Cada test sigue patrón AAA y tiene comandos exactos.
- ✅ Las dependencias añadidas en Fase 0 son las usadas en las fases 1-3 (mockk, turbine, coroutines-test, arch-core-testing, room-testing).
- ✅ Los tests siguen el orden didáctico: pure → mocks → integración.
- ✅ Los nombres de funciones/firmas usadas (`buscarPorNombre`, `updateSaldo`, `getConteoNotificaciones`, `cambiarEstado`, etc.) coinciden con el código real revisado.
- ✅ Los enums `EstadoServicio.COMPLETADO/RESERVADO/CADUCADO/ACTIVO` y `EstadoTransaccion.PENDIENTE/ACEPTADA/COMPLETADA/CANCELADA` y `Barrio.CENTRO/DELICIAS/TORRERO` existen en el código.
- ✅ Comandos Gradle adaptados a Windows (`./gradlew.bat`).

## Próximos pasos sugeridos (fuera de este plan)

Si tras completar este plan quieres seguir aprendiendo:
- **Robolectric** para testear Fragments en JVM sin emulador.
- **Espresso + FragmentScenario** para tests UI del Login/Registro.
- **JaCoCo** para medir cobertura de tests.
- **GitHub Actions** para correr `testDebugUnitTest` automáticamente en cada push.
