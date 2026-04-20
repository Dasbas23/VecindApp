# Diagrama de Clases · VecindApp

> Generado a partir del código fuente en `app/src/main/java/com/example/vecindapp/`.
> Arquitectura: **MVVM + Clean Architecture** con inyección manual.

---

## Visión general por capas

| Capa | Paquetes | Responsabilidad |
|------|----------|-----------------|
| **Presentation** | `ui.*`, `MainActivity`, `MainViewModel` | Fragments, ViewModels, Adapters, modelos de presentación |
| **Domain** | `domain.model`, `domain.repository` | Enums de negocio y contratos de repositorio (interfaces) |
| **Data** | `data.entities`, `data.db`, `data.repository`, `data.SesionUsuario` | Entidades Room, DAOs, `AppDatabase`, implementaciones de repositorio, sesión |
| **Application** | `VecindAppApplication` | Contenedor singleton de DB y repositorios |

---

## Diagrama completo

```mermaid
---
title: Diagrama de Clases — VecindApp
---
classDiagram
    direction TB

    %% =========================================================
    %% CAPA PRESENTATION · ENTRY POINTS (app raíz)
    %% =========================================================
    namespace app {
        class VecindAppApplication {
            +AppDatabase database
            +UsuarioRepository usuarioRepository
            +ServicioRepository servicioRepository
            +TransaccionRepository transaccionRepository
            +ValoracionRepository valoracionRepository
        }
        class MainActivity {
            -MainViewModel mainViewModel
            -Boolean badgeIniciado
            -configurarNavegacion()
            -iniciarBadge(nav)
        }
        class MainViewModel {
            -TransaccionRepository transaccionRepository
            +StateFlow notificaciones
            +setUsuarioId(id)
        }
    }

    %% =========================================================
    %% CAPA PRESENTATION · UI AUTH
    %% =========================================================
    namespace ui_auth {
        class LoginFragment {
            -LoginViewModel viewModel
            -TtsHelper ttsHelper
        }
        class LoginViewModel {
            -UsuarioDao usuarioDao
            +StateFlow usuarioEncontrado
            +StateFlow error
            +iniciarSesion(nombre)
            +limpiarError()
        }
        class RegistroFragment {
            -RegistroViewModel viewModel
        }
        class RegistroViewModel {
            -UsuarioRepository usuarioRepository
            +StateFlow registrado
            +StateFlow error
            +registrar(nombre, barrio)
            +limpiarError()
        }
    }

    %% =========================================================
    %% CAPA PRESENTATION · UI ESCAPARATE
    %% =========================================================
    namespace ui_escaparate {
        class EscaparateFragment {
            -EscaparateViewModel viewModel
            -ServicioAdapter adapter
        }
        class EscaparateViewModel {
            -ServicioRepository servicioRepository
            +StateFlow servicios
            +StateFlow filtroActivo
            +filtrarPorCategoria(cat)
            +quitarFiltro()
        }
        class ServicioAdapter {
            +Lambda onServicioClick
            +Lambda onServicioLongClick
            -ServicioViewHolder
            -ServicioDiffCallback
        }
    }

    %% =========================================================
    %% CAPA PRESENTATION · UI SERVICIO
    %% =========================================================
    namespace ui_servicio {
        class CrearServicioFragment {
            -CrearServicioViewModel viewModel
        }
        class CrearServicioViewModel {
            -ServicioRepository servicioRepository
            +StateFlow guardado
            +StateFlow error
            +guardarServicio(titulo, desc, cat, coste, uid)
        }
        class DetalleServicioFragment {
            -DetalleServicioViewModel viewModel
        }
        class DetalleServicioViewModel {
            -ServicioRepository servicioRepository
            -TransaccionRepository transaccionRepository
            -UsuarioRepository usuarioRepository
            -ValoracionRepository valoracionRepository
            +StateFlow servicio
            +StateFlow valoracion
            +StateFlow eliminado
            +StateFlow solicitado
            +cargarServicio(id)
            +solicitarServicio(cid)
            +cancelarSolicitud()
            +eliminarServicio()
            +actualizarServicio(...)
        }
    }

    %% =========================================================
    %% CAPA PRESENTATION · UI TRANSACCION
    %% =========================================================
    namespace ui_transaccion {
        class TransaccionFragment {
            -TransaccionViewModel viewModel
            -TransaccionAdapter adapter
        }
        class TransaccionViewModel {
            -TransaccionRepository transaccionRepository
            -ServicioRepository servicioRepository
            -UsuarioRepository usuarioRepository
            -ValoracionRepository valoracionRepository
            -Int usuarioActualId
            +StateFlow transacciones
            +StateFlow mensaje
            +aceptarTransaccion(item)
            +completarTransaccion(item)
            +cancelarTransaccion(item)
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

    %% =========================================================
    %% CAPA PRESENTATION · UI PERFIL
    %% =========================================================
    namespace ui_perfil {
        class PerfilFragment {
            -PerfilViewModel viewModel
            -ServicioAdapter adapter
        }
        class PerfilViewModel {
            -UsuarioRepository usuarioRepository
            -ServicioRepository servicioRepository
            -Int usuarioActualId
            +StateFlow usuario
            +StateFlow misServicios
        }
    }

    %% =========================================================
    %% CAPA PRESENTATION · UI HISTORIAL
    %% =========================================================
    namespace ui_historial {
        class HistorialFragment {
            -HistorialViewModel viewModel
            -HistorialAdapter adapter
            -BarChart grafico
        }
        class HistorialViewModel {
            -TransaccionRepository transaccionRepository
            -ServicioRepository servicioRepository
            -ValoracionRepository valoracionRepository
            -Int usuarioActualId
            +StateFlow completadas
            +StateFlow canceladas
            +StateFlow datosGrafico
            +obtenerValoracion(tid) Valoracion
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

    %% =========================================================
    %% CAPA PRESENTATION · UI VALORACION
    %% =========================================================
    namespace ui_valoracion {
        class ValoracionBottomSheetFragment {
            -ValoracionViewModel viewModel
        }
        class DetalleValoracionBottomSheet
        class ValoracionViewModel {
            -ValoracionRepository valoracionRepository
            +StateFlow guardada
            +StateFlow error
            +guardarValoracion(tid, vid, vado, pictos, com)
        }
        class PictogramaMapper {
            <<object>>
            +obtenerDescripcion(ctx, tag) String
            +obtenerDrawable(tag) Int
        }
    }

    %% =========================================================
    %% CAPA PRESENTATION · UI COMMON (utilidades transversales)
    %% =========================================================
    namespace ui_common {
        class TtsHelper {
            -TextToSpeech tts
            -String pendingText
            -Boolean isReady
            +speak(text)
            +stop()
            +formatearCosteHumano(coste)$ String
            +formatearCosteConUnidad(coste)$ String
        }
        class CategoriaMapper {
            <<object>>
            +obtenerDrawable(categoria) Int
        }
        class SnackbarUtils {
            <<ext>>
            +Fragment mostrarSnackbar(msg)
        }
    }

    %% =========================================================
    %% CAPA DOMAIN · CONTRATOS DE REPOSITORIO
    %% =========================================================
    namespace domain_repository {
        class UsuarioRepository {
            <<interface>>
            +insert(usuario) Long
            +update(usuario)
            +getById(id) Flow~Usuario~
            +getAll() Flow~List~
            +getByIdOnce(id) Usuario
            +updateSaldo(id, saldo)
            +buscarPorNombre(nombre) Usuario
        }
        class ServicioRepository {
            <<interface>>
            +insert(servicio) Long
            +update(servicio)
            +delete(servicio)
            +getById(id) Flow~Servicio~
            +getActivos() Flow~List~
            +getByUsuario(id) Flow~List~
            +getByCategoria(cat) Flow~List~
            +cambiarEstado(id, estado)
        }
        class TransaccionRepository {
            <<interface>>
            +insert(transaccion) Long
            +update(transaccion)
            +getByUsuario(id) Flow~List~
            +getByServicioYEstado(sid, estado) Transaccion
            +getByIdOnce(id) Transaccion
            +getConteoNotificaciones(id) Flow~Int~
        }
        class ValoracionRepository {
            <<interface>>
            +insert(valoracion) Long
            +getByValorado(id) Flow~List~
            +getByTransaccion(tid) Valoracion
            +getByTransaccionYValorador(tid, vid) Valoracion
        }
    }

    %% =========================================================
    %% CAPA DOMAIN · ENUMS DE NEGOCIO
    %% =========================================================
    namespace domain_model {
        class Barrio {
            <<enumeration>>
            +String displayName
        }
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
    }

    %% =========================================================
    %% CAPA DATA · IMPLEMENTACIONES DE REPOSITORIO
    %% =========================================================
    namespace data_repository {
        class UsuarioRepositoryImpl {
            -UsuarioDao usuarioDao
        }
        class ServicioRepositoryImpl {
            -ServicioDao servicioDao
        }
        class TransaccionRepositoryImpl {
            -TransaccionDao transaccionDao
        }
        class ValoracionRepositoryImpl {
            -ValoracionDao valoracionDao
        }
    }

    %% =========================================================
    %% CAPA DATA · ENTIDADES ROOM
    %% =========================================================
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
            +calcularNivel() NivelVecino
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
            +estaActivo() Boolean
            +estaVencido() Boolean
            +estaCompletado() Boolean
        }
        class Transaccion {
            +Int idTransaccion
            +Int idCompradorFk
            +Int idVendedorFk
            +Int idServicioFk
            +Double horasTransferidas
            +EstadoTransaccion estado
            +Long timestamp
            +estaCompletada() Boolean
        }
        class Valoracion {
            +Int idValoracion
            +Int idTransaccionFk
            +Int idValoradorFk
            +Int idValoradoFk
            +String pictogramasJson
            +String comentario
            +Long timestamp
            +getPictogramasList() List
        }
    }

    %% =========================================================
    %% CAPA DATA · ROOM (AppDatabase + DAOs + utilidades)
    %% NOTA: Las firmas de los DAOs se omiten aquí porque son
    %%       idénticas a las de las interfaces *Repository.
    %% =========================================================
    namespace data_db {
        class AppDatabase {
            <<abstract>>
            +usuarioDao() UsuarioDao
            +servicioDao() ServicioDao
            +transaccionDao() TransaccionDao
            +valoracionDao() ValoracionDao
            +getInstance(context)$ AppDatabase
        }
        class UsuarioDao {
            <<interface>>
        }
        class ServicioDao {
            <<interface>>
        }
        class TransaccionDao {
            <<interface>>
        }
        class ValoracionDao {
            <<interface>>
        }
        class Converters {
            +fromBarrio(v) String
            +toBarrio(s) Barrio
            +fromNivelVecino(v) String
            +toNivelVecino(s) NivelVecino
            +fromCategoriaServicio(v) String
            +toCategoriaServicio(s) CategoriaServicio
            +fromEstadoServicio(v) String
            +toEstadoServicio(s) EstadoServicio
            +fromEstadoTransaccion(v) String
            +toEstadoTransaccion(s) EstadoTransaccion
        }
        class SeedDatabaseCallback {
            +onCreate(db)
        }
    }

    %% =========================================================
    %% CAPA DATA · SESIÓN DE USUARIO
    %% =========================================================
    namespace data_session {
        class SesionUsuario {
            -SharedPreferences prefs
            +guardarUsuarioId(id)
            +obtenerUsuarioId() Int
            +haySesion() Boolean
            +cerrarSesion()
            +SIN_SESION$ Int
        }
    }

    %% =========================================================
    %% RELACIONES · REALIZACIÓN (Data → Domain)
    %% =========================================================
    UsuarioRepositoryImpl ..|> UsuarioRepository
    ServicioRepositoryImpl ..|> ServicioRepository
    TransaccionRepositoryImpl ..|> TransaccionRepository
    ValoracionRepositoryImpl ..|> ValoracionRepository

    %% =========================================================
    %% RELACIONES · COMPOSICIÓN INTERNA DE LA CAPA DATA
    %% =========================================================
    UsuarioRepositoryImpl --> UsuarioDao
    ServicioRepositoryImpl --> ServicioDao
    TransaccionRepositoryImpl --> TransaccionDao
    ValoracionRepositoryImpl --> ValoracionDao

    AppDatabase *-- UsuarioDao
    AppDatabase *-- ServicioDao
    AppDatabase *-- TransaccionDao
    AppDatabase *-- ValoracionDao
    AppDatabase ..> Converters : @TypeConverters
    AppDatabase ..> SeedDatabaseCallback : callback

    UsuarioDao ..> Usuario
    ServicioDao ..> Servicio
    TransaccionDao ..> Transaccion
    ValoracionDao ..> Valoracion

    %% =========================================================
    %% RELACIONES · ENTIDADES ↔ ENUMS
    %% =========================================================
    Usuario --> Barrio : barrio
    Usuario --> NivelVecino : nivel
    Servicio --> CategoriaServicio : categoria
    Servicio --> EstadoServicio : estado
    Transaccion --> EstadoTransaccion : estado

    %% =========================================================
    %% RELACIONES · CLAVES FORÁNEAS (ON DELETE CASCADE)
    %% =========================================================
    Servicio "*" --> "1" Usuario : FK autor
    Transaccion "*" --> "1" Usuario : FK comprador
    Transaccion "*" --> "1" Usuario : FK vendedor
    Transaccion "*" --> "1" Servicio : FK servicio
    Valoracion "*" --> "1" Transaccion : FK transaccion
    Valoracion "*" --> "1" Usuario : FK valorador
    Valoracion "*" --> "1" Usuario : FK valorado

    %% =========================================================
    %% RELACIONES · CONTENEDOR DE DEPENDENCIAS (DI manual)
    %% =========================================================
    VecindAppApplication *-- AppDatabase
    VecindAppApplication *-- UsuarioRepositoryImpl
    VecindAppApplication *-- ServicioRepositoryImpl
    VecindAppApplication *-- TransaccionRepositoryImpl
    VecindAppApplication *-- ValoracionRepositoryImpl

    %% =========================================================
    %% RELACIONES · MAIN (entry point de la UI)
    %% =========================================================
    MainActivity --> MainViewModel
    MainActivity --> SesionUsuario
    MainViewModel --> TransaccionRepository

    %% =========================================================
    %% RELACIONES · FRAGMENT → VIEWMODEL / ADAPTER / UTILS
    %% =========================================================
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

    %% =========================================================
    %% RELACIONES · VIEWMODEL → REPOSITORY (Presentation → Domain)
    %% =========================================================
    LoginViewModel --> UsuarioDao
    RegistroViewModel --> UsuarioRepository
    EscaparateViewModel --> ServicioRepository
    CrearServicioViewModel --> ServicioRepository
    DetalleServicioViewModel --> ServicioRepository
    DetalleServicioViewModel --> TransaccionRepository
    DetalleServicioViewModel --> UsuarioRepository
    DetalleServicioViewModel --> ValoracionRepository
    TransaccionViewModel --> TransaccionRepository
    TransaccionViewModel --> ServicioRepository
    TransaccionViewModel --> UsuarioRepository
    TransaccionViewModel --> ValoracionRepository
    PerfilViewModel --> UsuarioRepository
    PerfilViewModel --> ServicioRepository
    HistorialViewModel --> TransaccionRepository
    HistorialViewModel --> ServicioRepository
    HistorialViewModel --> ValoracionRepository
    ValoracionViewModel --> ValoracionRepository

    %% =========================================================
    %% RELACIONES · ADAPTERS Y MODELOS DE PRESENTACIÓN
    %% =========================================================
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
```

---

## Notas de diseño

### Patrón de inyección manual
Todos los `ViewModel` exponen una clase anidada `Factory : ViewModelProvider.Factory` que se omite en el diagrama para evitar ruido visual. Los `Fragment` instancian su ViewModel con `by viewModels { XxxViewModel.Factory(repo) }` y obtienen el repositorio desde `VecindAppApplication`.

### Capa de dominio aislada
`domain.repository.*` contiene solo interfaces y no conoce Room ni SQLite. Las implementaciones en `data.repository.*` cumplen esos contratos (`..|>`). Esto permite sustituir el backend (p. ej. añadir un API remoto en el futuro) sin tocar los ViewModels.

### DAOs omitidos para reducir ruido visual
Las firmas de `UsuarioDao`, `ServicioDao`, `TransaccionDao` y `ValoracionDao` son idénticas a las de sus respectivas interfaces `*Repository` (las implementaciones `*RepositoryImpl` son meros pass-through). Para no duplicar ~30 firmas en el diagrama, los DAOs aparecen solo como `<<interface>>` sin cuerpo. El contrato real puede consultarse en las interfaces de la capa *Domain* o directamente en el código fuente en `data/db/*.kt`.

### Modelos de presentación
`TransaccionUI`, `HistorialItem` y `DatoMensual` son *data classes* que envuelven entidades Room con datos adicionales derivados (título de servicio, rol del usuario, agregados mensuales). Viven en la capa UI y no contaminan el dominio.

### Utilidades transversales
- `TtsHelper` — wrapper del motor TTS de Android ligado al `Lifecycle` del Fragment. Además expone formateadores estáticos de horas.
- `CategoriaMapper` / `PictogramaMapper` — `object` (singletons) que centralizan el mapeo de enums/tags a recursos drawable.
- `SnackbarUtils` — extensiones de `Fragment` para mostrar snackbars ancladas al `BottomNav`.

### Claves foráneas Room
Todas las FKs usan `ON DELETE CASCADE`, por eso se han representado como asociaciones con multiplicidad `*..1` hacia las entidades padre (`Usuario`, `Servicio`, `Transaccion`).

---

## Cambios respecto a la v3

| v3 | v4 (esta versión) | Motivo |
|----|------------------|--------|
| `direction LR` | `direction TB` | Encaja mejor en página A4 vertical del anexo y refleja el flujo top-down de Clean Architecture |
| Namespaces ordenados Domain → Data → Presentation | Orden Presentation → Domain → Data | Las flechas de dependencia bajan siempre en la misma dirección, evitando cruces |
| DAOs con sus 30+ firmas completas | DAOs como `<<interface>>` vacíos con nota explicativa | Elimina duplicación (idénticos a las interfaces de Domain) |
| Relaciones en 7 bloques temáticos | Relaciones en 8 bloques con cabeceras descriptivas | Cabeceras del tipo "Data → Domain" o "Presentation → Domain" dejan explícito el flujo |
| FK `Servicio → Usuario : FK idUsuarioFk` | FK `Servicio → Usuario : FK autor` | Etiquetas más legibles en el render final (el campo técnico ya queda en la entidad) |
