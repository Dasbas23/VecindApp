# VecindApp — Banco de Tiempo Vecinal Accesible

> Aplicación Android nativa para intercambiar servicios entre vecinos
> usando horas como moneda, con accesibilidad cognitiva mediante
> pictogramas ARASAAC y síntesis de voz (TTS).

**TFG · DAM · ILERNA · 2S2526**

---

## Descripción

VecindApp es una App Android que funciona como un banco de tiempo vecinal donde los usuarios 
publican servicios que ofrecen (pasear al perro, dar clases, hacer recados...)
y los "pagan" con horas en lugar de dinero. La app opera offline y guarda los datos en local con 
Room/SQLite. Incluye accesibilidad mediante Text-To-Speecch y una interfaz accesible. Prioriza la
accesibilidad cognitiva incorporando pictogramas del sistema ARASAAC
y lectura en voz alta mediante el motor TTS nativo de Android.

## Arquitectura

El proyecto sigue el patrón **MVVM + Clean Architecture** organizado
en 5 capas:

```
app/src/main/java/com/example/vecindapp/
├── data/                  ← Capa de datos
│   ├── db/                  AppDatabase, DAOs, TypeConverters
│   ├── entities/            Entidades Room (@Entity)
│   └── repository/          Implementación de repositorios
├── domain/                ← Capa de dominio (lógica de negocio)
│   ├── model/               Enums y modelos del dominio
│   ├── repository/          Interfaces de repositorios (contratos)
│   └── usecase/             Casos de uso
├── ui/                    ← Capa de presentación
│   ├── escaparate/          Catálogo de servicios (RecyclerView)
│   ├── servicio/            Detalle y publicación de servicio
│   ├── transaccion/         Aceptar y valorar transacciones
│   ├── perfil/              Perfil del vecino
│   └── historial/           Gráfico de intercambios (MPAndroidChart)
└── worker/                ← Tareas en segundo plano (WorkManager)
```

## Stack tecnológico

| Categoría       | Tecnología                          |
|-----------------|-------------------------------------|
| Lenguaje        | Kotlin                              |
| UI              | XML Layouts + Fragments             |
| Base de datos   | Room 2.8.4 / SQLite (offline-first) |
| Navegación      | Navigation Component 2.9.7          |
| Arquitectura    | ViewModel 2.10.0 + LiveData         |
| Gráficos        | MPAndroidChart v3.1.0               |
| Imágenes        | Glide 4.16.0                        |
| Background      | WorkManager 2.9.0                   |
| Accesibilidad   | Android TTS API + ARASAAC           |
| Procesador      | KSP (Kotlin Symbol Processing)      |

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36

## Base de datos

4 entidades Room con relaciones Foreign Key:

- **Usuario** — Vecinos registrados con saldo de horas y nivel.
- **Servicio** — Servicios publicados en el escaparate.
- **Transaccion** — Intercambios de horas entre comprador y vendedor.
- **Valoracion** — Reseñas con pictogramas ARASAAC tras cada transacción.

## Cómo compilar

```bash
# Compilar el proyecto
./gradlew build

# Instalar en dispositivo/emulador conectado
./gradlew installDebug

# Ejecutar tests
./gradlew test

# Limpiar build
./gradlew clean
```

## Convenciones del proyecto

- Interfaz gráfica en **XML**, nunca Jetpack Compose.
- Corrutinas con `viewModelScope` en los ViewModels.
- Patrón Repository: interfaz en `domain/`, implementación en `data/`.
- Inyección de dependencias manual con `ViewModelProvider.Factory`.
- Commits y documentación en **español**.
- Documentación KDoc en todas las clases públicas.

## Rama de desarrollo

| Rama                       | Contenido                              |
|----------------------------|----------------------------------------|
| `desarrollo`               | Rama principal de integración          |
| `sprint2/bbdd+escaparate`  | BBDD Room + CRUD servicios + escaparate|

## Autor

Marius Ion — ILERNA 2S2526

## Licencia

Proyecto académico (TFG). Todos los derechos reservados.
Pictogramas: [ARASAAC](http://www.arasaac.org) — Licencia Creative Commons BY-NC-SA.
