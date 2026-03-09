# VecindApp — Esquema de Desarrollo
**Banco de Tiempo Vecinal · TFG DAM · ILERNA · 2S2526**

---

## FASE 1 — Diseño Funcional y Visual

### Casos de uso
Lista de lo que el usuario puede hacer en la app:
- Crear/seleccionar perfil de usuario local
- Publicar una oferta de servicio con categoría y coste en horas
- Explorar el Escaparate del Barrio (catálogo de servicios activos)
- Filtrar servicios por categoría (pictograma ARASAAC) y rango de horas
- Aceptar un servicio de otro vecino (transacción automática de horas)
- Valorar un servicio recibido mediante pictogramas ARASAAC
- Ver su saldo de horas actual y su nivel de vecino
- Consultar el historial de transacciones con gráfico de barras
- Escuchar la descripción de un servicio en voz alta (TTS)
- Recibir notificaciones locales de caducidad de servicios

### Boceto
Dibujar en papel cómo serán las pantallas principales y dónde van los botones y demás funcionalidades:
- Splash / Login (selector de perfil)
- Escaparate del Barrio (pantalla principal con RecyclerView)
- Detalle de Servicio (con botón Aceptar y activación TTS)
- Publicar Servicio (formulario + selector de pictograma ARASAAC)
- Valorar Servicio (grid de pictogramas seleccionables)
- Mi Perfil (saldo, badge de nivel, pictogramas de reputación)
- Historial / Gráfico (BarChart MPAndroidChart)

### Flujo de navegación
- **Inicio:** Splash → Login (selección de perfil)
- **Pantalla principal:** Escaparate del Barrio
  - FAB "+" → Publicar Servicio
  - Tap en tarjeta → Detalle de Servicio → (si acepta) → Valorar Servicio
  - Menú inferior → Mi Perfil → Historial / Gráfico
- **Navegación:** Navigation Component con Bottom Navigation Bar

---

## FASE 2 — Diseño Técnico

### Modelo de datos
Diagrama E-R con 4 entidades (cumple mínimo normativo de 3 tablas relacionadas):

```
USUARIO         SERVICIO            TRANSACCION         VALORACION
─────────       ──────────────      ──────────────────  ─────────────────
id_usuario PK   id_servicio PK      id_transaccion PK   id_valoracion PK
nombre          id_usuario_fk FK→U  id_comprador_fk FK→U id_transaccion_fk FK→T
barrio          titulo              id_vendedor_fk FK→U  id_valorador_fk FK→U
avatar_path     descripcion         id_servicio_fk FK→S  id_valorado_fk FK→U
saldo_horas     categoria           horas_transferidas  pictogramas_json
intercambios    pictograma_id       estado              comentario
nivel           coste_horas         timestamp           timestamp
fecha_registro  estado
                fecha_publicacion
                fecha_caducidad
```

**Relaciones:**
- USUARIO (1) → (N) SERVICIO
- USUARIO (1) → (N) TRANSACCION (como comprador y como vendedor)
- TRANSACCION (1) → (N) VALORACION
- USUARIO (1) → (N) VALORACION (recibidas)

### Elección de arquitectura
**MVVM + Clean Architecture** (estándar profesional Android recomendado por Google)

```
app/
├── data/
│   ├── db/              → AppDatabase, DAOs (Room)
│   ├── entities/        → Entidades Room (Usuario, Servicio, Transaccion, Valoracion)
│   └── repository/      → Implementaciones de repositorios
├── domain/
│   ├── model/           → Modelos de dominio
│   ├── repository/      → Interfaces de repositorios
│   └── usecase/         → Casos de uso (lógica de negocio)
├── ui/
│   ├── escaparate/      → Fragment + ViewModel del catálogo
│   ├── servicio/        → Fragment + ViewModel de detalle/publicación
│   ├── transaccion/     → Fragment + ViewModel de aceptar/valorar
│   ├── perfil/          → Fragment + ViewModel del perfil
│   └── historial/       → Fragment + ViewModel del gráfico
└── worker/              → WorkManager (notificaciones locales)
```

---

## FASE 3 — Setup y Control de Versiones

### Repositorio Git
- Crear repositorio en GitHub/GitLab: `vecindapp`
- Configurar `.gitignore` para Android Studio
- Estrategia de ramas: `main` (estable) + `develop` (trabajo) + `feature/xxx` (por iteración)
- Commit al final de cada sesión de trabajo como mínimo

### Esqueleto del proyecto *(IMPORTANTE)*
1. Crear proyecto en Android Studio (Empty Activity, Kotlin, minSdk 26)
2. Configurar estructura de carpetas según MVVM (ver Fase 2)
3. Añadir dependencias en `build.gradle` desde el primer día:

```kotlin
// Room + KTX
implementation "androidx.room:room-runtime:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"

// Navigation Component
implementation "androidx.navigation:navigation-fragment-ktx:2.7.7"
implementation "androidx.navigation:navigation-ui-ktx:2.7.7"

// Hilt (inyección de dependencias)
implementation "com.google.dagger:hilt-android:2.50"
kapt "com.google.dagger:hilt-android-compiler:2.50"

// ViewModel + LiveData
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"

// MPAndroidChart (terceros - justificado en propuesta)
implementation "com.github.PhilJay:MPAndroidChart:v3.1.0"

// WorkManager
implementation "androidx.work:work-runtime-ktx:2.9.0"

// Glide (imágenes de avatar)
implementation "com.github.bumptech.glide:glide:4.16.0"
```

---

## FASE 4 — Desarrollo por Sprints (MVP → Features)

> **Objetivo del MVP:** App funcional con login local, catálogo de servicios y transacciones de horas. Todo lo demás son capas de valor añadido.

---

### Iter_1 → UI Básica y Navegación (Semana 1)
**Objetivo:** Que se pueda navegar entre todas las pantallas con datos hardcodeados o falsos. La idea es validar el flujo antes de conectar la base de datos.

**Tareas:**
- Crear todos los Fragments: EscaparateFragment, DetalleServicioFragment, PublicarServicioFragment, ValorarServicioFragment, PerfilFragment, HistorialFragment
- Configurar Navigation Graph (nav_graph.xml) con todas las rutas y acciones
- Implementar Bottom Navigation Bar con iconos
- Maquetar layouts XML de cada pantalla (sin lógica, datos hardcodeados)
- Configurar RecyclerView con Adapter en el Escaparate (datos de prueba)
- Verificar que la navegación entre pantallas funciona sin crashes

**Entregable:** App navegable de principio a fin con datos de prueba.

---

### Iter_2 → Base de Datos Room (Semana 2)
**Objetivo:** Implementar las entidades Room, DAOs y verificar que el CRUD funciona correctamente.

**Tareas:**
- Crear las 4 entidades Room con anotaciones: `@Entity`, `@PrimaryKey`, `@ForeignKey`
- Crear los 4 DAOs con operaciones CRUD: `@Insert`, `@Update`, `@Delete`, `@Query`
- Configurar `AppDatabase` con `Room.databaseBuilder()` y migrations
- Implementar los Repositorios (interfaz en domain + implementación en data)
- Poblar la BD con datos de prueba mediante `RoomDatabase.Callback` para el desarrollo
- Verificar con pruebas manuales que los datos persisten entre sesiones

**Entregable:** Base de datos funcional con todas las tablas y relaciones verificadas.

---

### Iter_3 → Login Local y Gestión de Perfiles (Semana 2-3)
**Objetivo:** Implementar el sistema de autenticación local con múltiples perfiles, que es la base para poder simular la demo multiusuario (Pepe y María).

**Tareas:**
- Crear `LoginFragment` con RecyclerView de perfiles existentes + FAB para crear nuevo
- Implementar `CrearPerfilFragment`: campos nombre, barrio y selección de avatar (galería)
- Guardar el perfil activo en `SharedPreferences` (id del usuario en sesión)
- Implementar `SessionManager` (singleton) para recuperar el usuario activo en cualquier ViewModel
- Asignar saldo inicial de 5.0 horas al crear un perfil nuevo
- Cargar 2 perfiles de prueba precargados (Pepe y María) en `AppDatabase.Callback`
- Verificar el flujo completo: crear perfil → login → logout → cambiar a otro perfil

**Entregable:** Sistema de login local funcional. Demo multiusuario verificada en emulador.

---

### Iter_4 → Módulo de Servicios CRUD (Semana 3)
**Objetivo:** El usuario puede publicar, editar, cancelar y visualizar servicios en el Escaparate.

**Tareas:**
- Conectar `EscaparateViewModel` con el repositorio de servicios (LiveData/StateFlow)
- Implementar `PublicarServicioFragment` con formulario completo: título, descripción, categoría, coste en horas y fecha de caducidad
- Integrar los assets ARASAAC en `/res/drawable`: descargar ~15 pictogramas de categorías (hogar, cuidados, transporte, idiomas, deportes, etc.)
- Crear selector de pictograma ARASAAC (GridView o RecyclerView horizontal)
- Implementar filtros en el Escaparate: ChipGroup por categoría + slider de horas
- Permitir editar y cancelar servicios propios (estados: ACTIVO / CANCELADO)
- Ocultar el botón "Aceptar" en los servicios propios del usuario en sesión

**Entregable:** CRUD completo de servicios con pictogramas ARASAAC en el Escaparate.

---

### Iter_5 → Motor de Transacciones Atómico (Semana 3-4)
**Objetivo:** Implementar el flujo de aceptar un servicio con transferencia automática de horas usando `@Transaction` de Room.

**Tareas:**
- Crear `TransaccionUseCase` con la lógica: validar saldo ≥ coste → débito comprador → crédito vendedor → cambiar estado del servicio a RESERVADO
- Anotar la operación con `@Transaction` en el DAO para atomicidad
- Implementar la UI del botón "Aceptar Servicio" con diálogo de confirmación (muestra coste y saldo resultante)
- Mostrar error con Snackbar si saldo insuficiente
- Actualizar automáticamente el badge de saldo en la pantalla de Perfil tras cada transacción
- Incrementar `intercambios_total` del vendedor al completar
- Recalcular el nivel del vecino automáticamente (0-4 → NUEVO, 5-14 → CONFIANZA, 15+ → PILAR)

**Entregable:** Transacciones funcionando. El saldo se actualiza en tiempo real. Los niveles se calculan automáticamente.

---

### Iter_6 → Valoraciones con Pictogramas ARASAAC (Semana 4)
**Objetivo:** Tras completar una transacción, el comprador puede valorar al vendedor con pictogramas ARASAAC emocionales.

**Tareas:**
- Crear `ValorarServicioFragment` que se lanza automáticamente tras aceptar un servicio
- Mostrar grid de 8-10 pictogramas ARASAAC seleccionables (puntual, amable, cumple lo prometido, muy recomendable, volvería a contactar, eficiente, puntual, genial...)
- Implementar selección múltiple de pictogramas con feedback visual (borde resaltado al seleccionar)
- Guardar la valoración en `VALORACION` con el JSON array de pictogramas seleccionados
- Mostrar en `PerfilFragment` los 3 pictogramas más recibidos del usuario con su frecuencia
- Añadir campo de comentario libre opcional

**Entregable:** Sistema de valoraciones con ARASAAC funcional. El perfil muestra la reputación visual del vecino.

---

### Iter_7 → Accesibilidad: Text-to-Speech Nativo (Semana 5)
**Objetivo:** Implementar lectura en voz alta de servicios con el SDK nativo de Android, sin internet.

**Tareas:**
- Inicializar `TextToSpeech` en `DetalleServicioFragment` con locale español (`Locale("es", "ES")`)
- Activar TTS al pulsar la tarjeta del servicio: leer título + descripción
- Añadir botón de altavoz visible en la tarjeta para activación manual opcional
- Gestionar correctamente el ciclo de vida: pausar TTS en `onPause()`, liberar en `onDestroy()`
- Añadir `contentDescription` en todos los elementos interactivos para compatibilidad con TalkBack
- Verificar que el contraste de colores cumple WCAG AA (≥ 4.5:1)
- Verificar que todos los botones tienen al menos 48dp × 48dp

**Entregable:** TTS funcional. Demo verificada con TalkBack activado.

---

### Iter_8 → Historial Visual y Gráfico MPAndroidChart (Semana 5-6)
**Objetivo:** Mostrar el historial de saldo de horas con un gráfico de barras agrupadas.

**Tareas:**
- Añadir dependencia MPAndroidChart y configurar repositorio JitPack en `settings.gradle`
- Crear `HistorialFragment` con `BarChart` de MPAndroidChart
- Consulta Room: agrupar transacciones por semana → calcular horas ganadas vs. gastadas
- Configurar el gráfico: dos barras por semana (azul = ganadas, naranja = gastadas), leyenda, sin líneas de cuadrícula excesivas
- Mostrar debajo del gráfico una lista de las últimas 10 transacciones con RecyclerView
- Añadir datos de prueba precargados para que el gráfico sea visualmente impactante en la demo

**Entregable:** Gráfico de historial funcional con datos reales de la BD.

---

### Iter_9 → Sistema de Niveles, Badges y Gamificación (Semana 6)
**Objetivo:** Completar el módulo de gamificación con badges visuales y el Escaparate del Barrio visualmente terminado.

**Tareas:**
- Crear los 3 badges de nivel como drawables vectoriales: 🏠 Vecino Nuevo, ⭐ Vecino de Confianza, 🏆 Pilar del Barrio
- Mostrar badge en `PerfilFragment` con animación simple (fade-in al actualizar nivel)
- Mostrar badge pequeño junto al nombre del publicante en cada tarjeta del Escaparate
- Añadir la pantalla "Escaparate del Barrio" con header visual de bienvenida mostrando el saldo del usuario en sesión
- Implementar el "modo Pepe/María" de demo: datos precargados con 3-4 servicios por usuario, transacciones previas y valoraciones para que el gráfico tenga datos desde el arranque

**Entregable:** Gamificación completa. La app tiene aspecto terminado y listo para demo.

---

### Iter_10 → WorkManager y Notificaciones Locales (Semana 6)
**Objetivo:** Implementar notificaciones locales en background sin internet usando WorkManager.

**Tareas:**
- Crear `CaducidadWorker`: Worker periódico (cada 24h) que consulta servicios con `fecha_caducidad` próxima (< 24h) y lanza notificación local
- Crear `SaldoInactivoWorker`: Worker semanal que detecta usuarios con horas sin gastar durante 30+ días y notifica
- Configurar `NotificationChannel` para Android 8.0+
- Solicitar permiso `POST_NOTIFICATIONS` en Android 13+ con diálogo explicativo
- Registrar los Workers en `Application.onCreate()` con `WorkManager.enqueueUniquePeriodicWork()`
- Verificar en emulador que las notificaciones se lanzan correctamente

**Entregable:** Notificaciones locales funcionales. WorkManager en background verificado.

---

### Iter_11 → Pulido, Testing y Preparación Demo (Semana 7)
**Objetivo:** App terminada, estable, con datos de demo preparados y lista para grabar el vídeo.

**Tareas:**

**Testing (caja negra según normativa):**
- Prueba RF-01: Crear perfil Pepe con avatar → verificar login
- Prueba RF-10: Publicar servicio "Clases de guitarra" → verificar en Escaparate
- Prueba RF-20/21: María acepta servicio → verificar descuento de horas en María y abono en Pepe
- Prueba RF-12: Filtrar por categoría HOGAR → verificar que solo aparecen servicios de ese tipo
- Prueba RF-13: Pulsar servicio → verificar que TTS lee título y descripción
- Prueba RF-30: Completar valoración con pictogramas → verificar en perfil de Pepe
- Prueba RNF-12: Intentar aceptar servicio con saldo 0 → verificar mensaje de error
- Prueba RNF-11: Simular fallo en transacción → verificar que el saldo no queda en estado inconsistente

**Preparación demo:**
- Precarga de datos: 2 usuarios (Pepe, María), 5 servicios activos, 3 transacciones completadas, valoraciones con pictogramas, historial de 4 semanas para el gráfico
- Guión de demo (15-20 min): Login Pepe → Explorar Escaparate → TTS en servicio → Cambio a María → Aceptar servicio → Valorar con ARASAAC → Ver perfil con nivel y pictogramas → Ver gráfico historial
- Verificar que el emulador está configurado y estable antes de grabar
- Generar APK de debug para incluir en la entrega

**Entregable:** APK funcional + guión de demo + app lista para grabar el vídeo de defensa.

---

## RESUMEN DE ITERACIONES

| Iter | Semana | Funcionalidad | RF/RNF cubiertos |
|------|--------|---------------|-------------------|
| 1 | S1 | UI básica + navegación | — |
| 2 | S1-2 | Base de datos Room + CRUD | RF-01 parcial |
| 3 | S2-3 | Login local + perfiles | RF-01, RF-02, RF-03 |
| 4 | S3 | Módulo servicios CRUD + ARASAAC catálogo | RF-10, RF-11, RF-12, RF-14 |
| 5 | S3-4 | Motor de transacciones atómico | RF-20, RF-21, RF-22, RF-41 |
| 6 | S4 | Valoraciones con pictogramas ARASAAC | RF-23, RF-30, RF-31, RF-32 |
| 7 | S5 | Text-to-Speech nativo | RF-13, RNF-20, RNF-21, RNF-22, RNF-23 |
| 8 | S5-6 | Historial gráfico MPAndroidChart | RF-42 |
| 9 | S6 | Niveles, badges y gamificación | RF-40, RF-41 |
| 10 | S6 | WorkManager + notificaciones locales | RF-44, RNF-32 |
| 11 | S7 | Testing, pulido y demo | Pruebas caja negra |

---

## CHECKLIST NORMATIVA ILERNA

### Entrega final (24 abril 2026)
- [ ] Trabajo escrito (.docx + .pdf) · 30-60 páginas · Arial 11 · interlineado 1.5
- [ ] Vídeo de defensa (.mp4) · 15-25 min · sin cortes · emulador Android Studio
- [ ] Presentación diapositivas (.pptx) · 20-25 diapositivas · letra 24-28pt
- [ ] Código fuente comprimido (.zip)
- [ ] APK ejecutable (.apk)

### Secciones obligatorias de la memoria
- [ ] Portada (título, autor, tutor, semestre, logo ILERNA, enlace Google Drive)
- [ ] Índice (1 página, sin modificar plantilla)
- [ ] Introducción: Motivación + Abstract (inglés) + Objetivos SMART
- [ ] **Estado del Arte** *(suspense directo si falta)*
- [ ] Metodología (ciclo de vida, fases)
- [ ] Tecnologías y herramientas (con justificación de terceros: ARASAAC, MPAndroidChart, Firebase)
- [ ] Planificación: Diagrama de Gantt (estimado vs. real) + Análisis DAFO
- [ ] Análisis 8-16 pág.: RF + RNF + **Diagrama E-R** + **Casos de Uso** + **Diagrama de Clases**
- [ ] Diseño 6-14 pág.: Mockups/Wireframes + explicación de clases y funciones clave
- [ ] Despliegue y pruebas: pruebas de caja negra sobre casos de uso
- [ ] Conclusiones (objetivos alcanzados vs. propuestos)
- [ ] Vías futuras (API REST, chat, geolocalización, exportación PDF, PWA)
- [ ] **Bibliografía/Webgrafía** *(suspense directo si falta)* · normas APA
- [ ] Anexos obligatorios: Manual de instalación + Manual de usuario

### Diagramas obligatorios (entrega opcional 13-16 marzo)
- [ ] Diagrama de Casos de Uso (con especificaciones de cada caso)
- [ ] Diagrama Entidad-Relación (mínimo 3 tablas relacionadas → tenemos 4 ✅)
- [ ] Diagrama de Clases

---

*VecindApp · TFG DAM · ILERNA · Versión 2.0 · Marzo 2026*
