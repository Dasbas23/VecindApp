# Diagrama de Clases · VecindApp

> Generado a partir del código fuente en `app/src/main/java/com/example/vecindapp/`.
> Arquitectura: **MVVM + Clean Architecture** con inyección manual de dependencias.

---

## Visión general por capas

| Capa | Color | Paquetes | Responsabilidad |
|------|:-----:|----------|-----------------|
| **Presentation** | 🟪 Morado | `ui.*`, `MainActivity`, `MainViewModel` | Fragments, ViewModels, Adapters y modelos de presentación. |
| **Application** | 🟨 Ámbar | `VecindAppApplication` | Contenedor singleton de base de datos y repositorios. |
| **Domain** | 🟦 Azul | `domain.model`, `domain.repository` | Enums de negocio y contratos de repositorio (interfaces). |
| **Data** | 🟩 Verde | `data.entities`, `data.db`, `data.repository`, `data.SesionUsuario` | Entidades Room, DAOs, `AppDatabase`, implementaciones de repositorio y gestión de sesión. |
| **Utilidades** | ⬜ Gris | `ui.common` | Helpers transversales (TTS, mapeos de pictogramas, Snackbars). |

---

## Cómo leer el diagrama

**Notación de flechas (UML adaptada a Mermaid)**

| Símbolo | Significado |
|:-------:|-------------|
| `..|>` | Implementación de interfaz (`Impl` realiza el contrato). |
| `-->` | Asociación dirigida (el origen mantiene referencia al destino). |
| `..>` | Dependencia débil (el origen usa al destino pero no lo guarda). |
| `*--` | Composición (el contenedor posee el ciclo de vida del contenido). |
| `"*" --> "1"` | Multiplicidad (muchos → uno; se usa para representar FKs). |

**Estereotipos UML usados**

`<<interface>>` · `<<enumeration>>` · `<<abstract>>` · `<<object>>` (singleton Kotlin) · `<<ext>>` (funciones de extensión Kotlin).

**Tipos genéricos fusionados `<T>`**

Para reducir la densidad visual, los doce tipos que comparten forma (cuatro interfaces de repositorio, cuatro implementaciones y cuatro DAOs) se representan con un único genérico parametrizado por `T`. La correspondencia con el código real es:

| En el diagrama | En el código |
|----------------|--------------|
| `XxxRepository<T>` | `UsuarioRepository`, `ServicioRepository`, `TransaccionRepository`, `ValoracionRepository` |
| `XxxRepositoryImpl<T>` | `UsuarioRepositoryImpl`, `ServicioRepositoryImpl`, `TransaccionRepositoryImpl`, `ValoracionRepositoryImpl` |
| `XxxDao<T>` | `UsuarioDao`, `ServicioDao`, `TransaccionDao`, `ValoracionDao` |

Los métodos específicos de cada entidad (p. ej. `updateSaldo` en `UsuarioRepository`, `getByCategoria` en `ServicioRepository`, `getConteoNotificaciones` en `TransaccionRepository`, `getByTransaccionYValorador` en `ValoracionRepository`) se detallan en el cuerpo de la memoria (apartado 6.4) y no se reflejan aquí.

---

## Diagrama completo

```mermaid
classDiagram
    direction LR

    %% ═══════════════════════════════════════════════════════════════
    %%  PRESENTATION · RAÍZ (Application + Activity + ViewModel global)
    %% ═══════════════════════════════════════════════════════════════
    namespace root {
        class VecindAppApplication {
            +AppDatabase database
            +XxxRepository usuarioRepository
            +XxxRepository servicioRepository
            +XxxRepository transaccionRepository
            +XxxRepository valoracionRepository
        }
        class MainActivity {
            -MainViewModel mainViewModel
            -Boolean badgeIniciado
            -configurarNavegacion()
            -iniciarBadge()
        }
        class MainViewModel {
            +StateFlow notificaciones
            +setUsuarioId()
        }
    }

    %% ═══════════════════════════════════════════════════════════════
    %%  PRESENTATION · UI
    %% ═══════════════════════════════════════════════════════════════
    namespace ui_auth {
        class LoginFragment {
            -LoginViewModel viewModel
            -TtsHelper ttsHelper
        }
        class LoginViewModel {
            +StateFlow usuarioEncontrado
            +StateFlow error
            +iniciarSesion()
            +limpiarError()
        }
        class RegistroFragment {
            -RegistroViewModel viewModel
        }
        class RegistroViewModel {
            +StateFlow registrado
            +StateFlow error
            +registrar()
            +limpiarError()
        }
    }

    namespace ui_escaparate {
        class EscaparateFragment {
            -EscaparateViewModel viewModel
            -ServicioAdapter adapter
        }
        class EscaparateViewModel {
            +StateFlow servicios
            +StateFlow filtroActivo
            +filtrarPorCategoria()
            +quitarFiltro()
        }
        class ServicioAdapter {
            +Lambda onServicioClick
            +Lambda onServicioLongClick
            -ServicioViewHolder
            -ServicioDiffCallback
        }
    }

    namespace ui_servicio {
        class CrearServicioFragment {
            -CrearServicioViewModel viewModel
        }
        class CrearServicioViewModel {
            +StateFlow guardado
            +StateFlow error
            +guardarServicio()
        }
        class DetalleServicioFragment {
            -DetalleServicioViewModel viewModel
        }
        class DetalleServicioViewModel {
            +StateFlow servicio
            +StateFlow valoracion
            +StateFlow eliminado
            +StateFlow solicitado
            +cargarServicio()
            +solicitarServicio()
            +cancelarSolicitud()
            +eliminarServicio()
            +actualizarServicio()
        }
    }

    namespace ui_transaccion {
        class TransaccionFragment {
            -TransaccionViewModel viewModel
            -TransaccionAdapter adapter
        }
        class TransaccionViewModel {
            -Int usuarioActualId
            +StateFlow transacciones
            +StateFlow mensaje
            +aceptarTransaccion()
            +completarTransaccion()
            +cancelarTransaccion()
        }
        class TransaccionAdapter {
            +Lambda onAceptar
            +Lambda onCompletar
            +Lambda onCancelar
            +Lambda onValorar
            +Lambda onItemClick
        }
        class TransaccionUI {
            +Transaccion transaccion
            +String tituloServicio
            +String rol
            +Boolean puedeAceptar
            +Boolean puedeCompletar
            +Boolean puedeCancelar
            +Boolean puedeValorar
        }
    }

    namespace ui_perfil {
        class PerfilFragment {
            -PerfilViewModel viewModel
            -ServicioAdapter adapter
        }
        class PerfilViewModel {
            -Int usuarioActualId
            +StateFlow usuario
            +StateFlow misServicios
        }
    }

    namespace ui_historial {
        class HistorialFragment {
            -HistorialViewModel viewModel
            -HistorialAdapter adapter
            -BarChart grafico
        }
        class HistorialViewModel {
            -Int usuarioActualId
            +StateFlow completadas
            +StateFlow canceladas
            +StateFlow datosGrafico
            +obtenerValoracion()
        }
        class HistorialAdapter {
            -Int usuarioActualId
            +Lambda onItemClick
        }
        class HistorialItem {
            +Transaccion transaccion
            +String tituloServicio
            +Boolean esVendedor
        }
        class DatoMensual {
            +String mes
            +Double ganadas
            +Double gastadas
        }
    }

    namespace ui_valoracion {
        class ValoracionBottomSheetFragment {
            -ValoracionViewModel viewModel
        }
        class DetalleValoracionBottomSheet
        class ValoracionViewModel {
            +StateFlow guardada
            +StateFlow error
            +guardarValoracion()
        }
        class PictogramaMapper {
            <<object>>
            +obtenerDescripcion()
            +obtenerDrawable()
        }
    }

    namespace ui_common {
        class TtsHelper {
            -TextToSpeech tts
            -Boolean isReady
            +speak()
            +stop()
            +formatearCosteHumano()$
            +formatearCosteConUnidad()$
        }
        class CategoriaMapper {
            <<object>>
            +obtenerDrawable()
        }
        class SnackbarUtils {
            <<ext>>
            +mostrarSnackbar()
        }
    }

    %% ═══════════════════════════════════════════════════════════════
    %%  DOMAIN · INTERFAZ DE REPOSITORIO (genérica, fusiona 4)
    %% ═══════════════════════════════════════════════════════════════
    namespace domain_repository {
        class XxxRepository~T~ {
            <<interface>>
            +insert()
            +update()
            +delete()
            +getById()
            +getAll()
            +...específicos por entidad
        }
    }

    %% ═══════════════════════════════════════════════════════════════
    %%  DATA · IMPLEMENTACIÓN DE REPOSITORIO + SESIÓN
    %% ═══════════════════════════════════════════════════════════════
    namespace data_repository {
        class XxxRepositoryImpl~T~ {
            -XxxDao dao
        }
    }

    namespace data {
        class SesionUsuario {
            -SharedPreferences prefs
            +guardarUsuarioId()
            +obtenerUsuarioId()
            +haySesion()
            +cerrarSesion()
            +SIN_SESION$
        }
    }

    %% ═══════════════════════════════════════════════════════════════
    %%  DATA · ROOM (AppDatabase + DAO genérico)
    %% ═══════════════════════════════════════════════════════════════
    namespace data_db {
        class AppDatabase {
            <<abstract>>
            +usuarioDao()
            +servicioDao()
            +transaccionDao()
            +valoracionDao()
            +getInstance()$
        }
        class Converters {
            +fromBarrio() / toBarrio()
            +fromNivelVecino() / toNivelVecino()
            +fromCategoriaServicio() / toCategoriaServicio()
            +fromEstadoServicio() / toEstadoServicio()
            +fromEstadoTransaccion() / toEstadoTransaccion()
        }
        class SeedDatabaseCallback {
            +onCreate()
        }
        class XxxDao~T~ {
            <<interface>>
            +insert()
            +update()
            +delete()
            +getById()
            +getAll()
            +...específicos por entidad
        }
    }

    %% ═══════════════════════════════════════════════════════════════
    %%  DATA · ENTIDADES ROOM
    %% ═══════════════════════════════════════════════════════════════
    namespace data_entities {
        class Usuario {
            +Int idUsuario
            +String nombre
            +Barrio barrio
            +String avatarPath
            +Double saldoHoras
            +Int intercambiosTotal
            +NivelVecino nivel
            +Long fechaRegistro
            +calcularNivel()
        }
        class Servicio {
            +Int idServicio
            +Int idUsuarioFk
            +String titulo
            +String descripcion
            +CategoriaServicio categoria
            +String pictogramaId
            +Double costeHoras
            +EstadoServicio estado
            +Long fechaPublicacion
            +Long fechaCaducidad
            +estaActivo()
            +estaVencido()
            +estaCompletado()
        }
        class Transaccion {
            +Int idTransaccion
            +Int idCompradorFk
            +Int idVendedorFk
            +Int idServicioFk
            +Double horasTransferidas
            +EstadoTransaccion estado
            +Long timestamp
            +estaCompletada()
        }
        class Valoracion {
            +Int idValoracion
            +Int idTransaccionFk
            +Int idValoradorFk
            +Int idValoradoFk
            +String pictogramasJson
            +String comentario
            +Long timestamp
            +getPictogramasList()
        }
    }

    %% ═══════════════════════════════════════════════════════════════
    %%  DOMAIN · ENUMS (modelo de negocio)
    %% ═══════════════════════════════════════════════════════════════
    namespace domain_model {
        class NivelVecino {
            <<enumeration>>
            NOVATO
            ACTIVO
            VETERANO
            REFERENTE
        }
        class CategoriaServicio {
            <<enumeration>>
            HOGAR
            TECNOLOGIA
            EDUCACION
            COMPANIA
            RECADOS
            OTROS
        }
        class EstadoServicio {
            <<enumeration>>
            ACTIVO
            RESERVADO
            COMPLETADO
            CADUCADO
        }
        class EstadoTransaccion {
            <<enumeration>>
            PENDIENTE
            ACEPTADA
            COMPLETADA
            CANCELADA
        }
        class Barrio {
            <<enumeration>>
            +String displayName
        }
    }

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · ENTIDADES ↔ ENUMS
    %% ═══════════════════════════════════════════════════════════════
    Usuario --> Barrio : barrio
    Usuario --> NivelVecino : nivel
    Servicio --> CategoriaServicio : categoria
    Servicio --> EstadoServicio : estado
    Transaccion --> EstadoTransaccion : estado

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · FOREIGN KEYS (multiplicidad *..1)
    %% ═══════════════════════════════════════════════════════════════
    Servicio "*" --> "1" Usuario : FK idUsuarioFk
    Transaccion "*" --> "1" Usuario : FK comprador
    Transaccion "*" --> "1" Usuario : FK vendedor
    Transaccion "*" --> "1" Servicio : FK idServicioFk
    Valoracion "*" --> "1" Transaccion : FK idTransaccionFk
    Valoracion "*" --> "1" Usuario : FK valorador
    Valoracion "*" --> "1" Usuario : FK valorado

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · ROOM (se fusionan las 4 DAOs en XxxDao)
    %% ═══════════════════════════════════════════════════════════════
    AppDatabase "1" *-- "4" XxxDao
    AppDatabase ..> Converters : TypeConverters
    AppDatabase ..> SeedDatabaseCallback : callback

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · PATRÓN REPOSITORY
    %% ═══════════════════════════════════════════════════════════════
    XxxRepositoryImpl ..|> XxxRepository
    XxxRepositoryImpl --> XxxDao

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · CONTENEDOR DE DEPENDENCIAS
    %% ═══════════════════════════════════════════════════════════════
    VecindAppApplication *-- AppDatabase
    VecindAppApplication "1" *-- "4" XxxRepositoryImpl

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · MAIN
    %% ═══════════════════════════════════════════════════════════════
    MainActivity --> MainViewModel
    MainActivity --> SesionUsuario
    MainViewModel --> XxxRepository

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · FRAGMENT → VIEWMODEL / ADAPTER / HELPER
    %% ═══════════════════════════════════════════════════════════════
    LoginFragment --> LoginViewModel
    LoginFragment --> SesionUsuario
    LoginFragment --> TtsHelper
    RegistroFragment --> RegistroViewModel
    RegistroFragment --> SesionUsuario
    EscaparateFragment --> EscaparateViewModel
    EscaparateFragment --> ServicioAdapter
    CrearServicioFragment --> CrearServicioViewModel
    DetalleServicioFragment --> DetalleServicioViewModel
    TransaccionFragment --> TransaccionViewModel
    TransaccionFragment --> TransaccionAdapter
    PerfilFragment --> PerfilViewModel
    PerfilFragment --> ServicioAdapter
    HistorialFragment --> HistorialViewModel
    HistorialFragment --> HistorialAdapter
    ValoracionBottomSheetFragment --> ValoracionViewModel

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · VIEWMODEL → REPOSITORY (todos dependen del contrato)
    %% ═══════════════════════════════════════════════════════════════
    LoginViewModel --> XxxDao
    RegistroViewModel --> XxxRepository
    EscaparateViewModel --> XxxRepository
    CrearServicioViewModel --> XxxRepository
    DetalleServicioViewModel --> XxxRepository
    TransaccionViewModel --> XxxRepository
    PerfilViewModel --> XxxRepository
    HistorialViewModel --> XxxRepository
    ValoracionViewModel --> XxxRepository

    %% ═══════════════════════════════════════════════════════════════
    %%  RELACIONES · ADAPTERS Y MODELOS DE PRESENTACIÓN
    %% ═══════════════════════════════════════════════════════════════
    ServicioAdapter ..> Servicio
    ServicioAdapter ..> CategoriaMapper
    ServicioAdapter ..> TtsHelper
    TransaccionAdapter ..> TransaccionUI
    TransaccionAdapter ..> TtsHelper
    HistorialAdapter ..> HistorialItem
    HistorialAdapter ..> TtsHelper

    TransaccionUI --> Transaccion
    TransaccionUI --> EstadoTransaccion
    HistorialItem --> Transaccion
    TransaccionViewModel ..> TransaccionUI
    HistorialViewModel ..> HistorialItem
    HistorialViewModel ..> DatoMensual

    CategoriaMapper ..> CategoriaServicio
    ValoracionBottomSheetFragment ..> PictogramaMapper
    DetalleValoracionBottomSheet ..> PictogramaMapper

    %% ═══════════════════════════════════════════════════════════════
    %%  ESTILOS POR CAPA
    %% ═══════════════════════════════════════════════════════════════
    classDef capaDomain fill:#E3F2FD,stroke:#1565C0,stroke-width:1.5px,color:#0D47A1
    classDef capaData fill:#E8F5E9,stroke:#2E7D32,stroke-width:1.5px,color:#1B5E20
    classDef capaApp fill:#FFF8E1,stroke:#EF6C00,stroke-width:1.5px,color:#E65100
    classDef capaUI fill:#F3E5F5,stroke:#6A1B9A,stroke-width:1.5px,color:#4A148C
    classDef capaCommon fill:#ECEFF1,stroke:#455A64,stroke-width:1.5px,color:#263238

    %% --- DOMAIN · enums ---
    class NivelVecino capaDomain
    class CategoriaServicio capaDomain
    class EstadoServicio capaDomain
    class EstadoTransaccion capaDomain
    class Barrio capaDomain

    %% --- DOMAIN · repository interface genérica ---
    class XxxRepository capaDomain

    %% --- DATA · entidades Room ---
    class Usuario capaData
    class Servicio capaData
    class Transaccion capaData
    class Valoracion capaData

    %% --- DATA · Room DB ---
    class AppDatabase capaData
    class Converters capaData
    class SeedDatabaseCallback capaData
    class XxxDao capaData

    %% --- DATA · repository impl genérica ---
    class XxxRepositoryImpl capaData

    %% --- DATA · sesión ---
    class SesionUsuario capaData

    %% --- APPLICATION ---
    class VecindAppApplication capaApp
    class MainActivity capaApp
    class MainViewModel capaApp

    %% --- UI · auth ---
    class LoginFragment capaUI
    class LoginViewModel capaUI
    class RegistroFragment capaUI
    class RegistroViewModel capaUI

    %% --- UI · escaparate ---
    class EscaparateFragment capaUI
    class EscaparateViewModel capaUI
    class ServicioAdapter capaUI

    %% --- UI · servicio ---
    class CrearServicioFragment capaUI
    class CrearServicioViewModel capaUI
    class DetalleServicioFragment capaUI
    class DetalleServicioViewModel capaUI

    %% --- UI · transacción ---
    class TransaccionFragment capaUI
    class TransaccionViewModel capaUI
    class TransaccionAdapter capaUI
    class TransaccionUI capaUI

    %% --- UI · perfil ---
    class PerfilFragment capaUI
    class PerfilViewModel capaUI

    %% --- UI · historial ---
    class HistorialFragment capaUI
    class HistorialViewModel capaUI
    class HistorialAdapter capaUI
    class HistorialItem capaUI
    class DatoMensual capaUI

    %% --- UI · valoración ---
    class ValoracionBottomSheetFragment capaUI
    class DetalleValoracionBottomSheet capaUI
    class ValoracionViewModel capaUI
    class PictogramaMapper capaUI

    %% --- COMMON / utilidades ---
    class TtsHelper capaCommon
    class CategoriaMapper capaCommon
    class SnackbarUtils capaCommon
```

---

## Notas de diseño

### Fusión de tipos genéricos
El código real contiene **cuatro interfaces de repositorio**, **cuatro implementaciones** y **cuatro DAOs**, uno por cada entidad (`Usuario`, `Servicio`, `Transaccion`, `Valoracion`). Como los doce tipos comparten idéntica estructura, se representan con un único genérico `<T>` para evitar saturación visual. La cardinalidad real se refleja en las multiplicidades `"1" *-- "4"` de `AppDatabase` y `VecindAppApplication`. Los métodos específicos de cada entidad se detallan en el cuerpo de la memoria (apartado 6.4).

### Patrón de inyección manual
Todos los `ViewModel` exponen una clase anidada `Factory : ViewModelProvider.Factory` que se omite en el diagrama para evitar ruido visual. Los `Fragment` instancian su ViewModel con `by viewModels { XxxViewModel.Factory(repo) }` y obtienen el repositorio desde `VecindAppApplication`.

### Capa de dominio aislada
`domain.repository.*` contiene solo interfaces y no conoce Room ni SQLite. Las implementaciones en `data.repository.*` cumplen esos contratos (`..|>`). Esto permite sustituir el backend (p. ej. añadir un API remoto en el futuro) sin tocar los ViewModels.

### Modelos de presentación
`TransaccionUI`, `HistorialItem` y `DatoMensual` son *data classes* que envuelven entidades Room con datos adicionales derivados (título de servicio, rol del usuario, agregados mensuales). Viven en la capa UI y no contaminan el dominio.

### Utilidades transversales
- `TtsHelper` — wrapper del motor TTS de Android ligado al `Lifecycle` del Fragment. Además expone formateadores estáticos de horas.
- `CategoriaMapper` / `PictogramaMapper` — `object` (singletons Kotlin) que centralizan el mapeo de enums/tags a recursos drawable.
- `SnackbarUtils` — funciones de extensión Kotlin sobre `Fragment` para mostrar snackbars ancladas al `BottomNav`.

### Claves foráneas Room
Todas las FKs usan `ON DELETE CASCADE`, por eso se han representado como asociaciones con multiplicidad `*..1` hacia las entidades padre (`Usuario`, `Servicio`, `Transaccion`).

### Simplificaciones visuales aplicadas
- Los parámetros y tipos de retorno de los métodos se han omitido (se recuperan en el código y en KDoc).
- Los atributos privados de los `ViewModel` que guardan referencias a repositorios se representan únicamente como flechas de asociación hacia `XxxRepository`.
- Las asociaciones obvias `XxxDao ..> Entidad` se omiten: cada DAO opera sobre su entidad correspondiente por definición.
