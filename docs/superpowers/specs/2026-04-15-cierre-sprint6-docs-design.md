# Diseño: Cierre Sprint 6 — Sincronización de documentación

**Fecha:** 2026-04-15  
**Rama:** desarrollo  
**Alcance:** CLAUDE.md + README.md

---

## Contexto

Sprint 6 fue mergeado a `desarrollo` con los siguientes entregables reales:
- Badge de notificaciones reactivo (MainViewModel + LiveData)
- Formateo de horas en la UI
- Refactor UI/UX general
- Mejoras en el sistema de valoración (DetalleValoracionBottomSheet)

WorkManager **no se implementó**. No existe ningún fichero `.kt` en `worker/`. Queda documentado como deuda técnica justificada por tiempo.

---

## Cambios en CLAUDE.md

### 1. Tabla de sprints
- Sprint 6: `In progress` → `Done`
- Descripción: reflejar entregables reales (badge reactivo, formateo horas, refactor UI/UX, mejoras valoración)

### 2. Project Structure
Añadir los ficheros que existen en el código pero no aparecían:
- `MainViewModel.kt` → Badge de notificaciones reactivo (LiveData)
- `data/SesionUsuario.kt` → Singleton para gestionar la sesión activa
- `data/db/` → listar DAOs individuales (UsuarioDao, ServicioDao, TransaccionDao, ValoracionDao)
- `ui/common/` → TtsHelper.kt, CategoriaMapper.kt, SnackbarUtils.kt
- `ui/valoracion/` → añadir DetalleValoracionBottomSheet
- `worker/` → marcar como deuda técnica (no implementado)

### 3. Known TODOs
Reescribir la entrada de WorkManager como deuda técnica justificada por tiempo, no como tarea pendiente del sprint activo.

---

## Cambios en README.md

### 1. Tabla de sprints — Sprint 6
Ajustar descripción para reflejar lo entregado realmente en lugar del plan original.

### 2. Árbol de carpetas — worker/
Cambiar `[pendiente]` por nota de deuda técnica.

### 3. Nueva sección: Deuda técnica
Documentar WorkManager como funcionalidad planificada no implementada, con justificación breve.

---

## Lo que NO se toca

- Tabla de ramas
- Aliases Git
- Stack tecnológico
- Sección autor/licencia
- Comandos de build
- Convenciones del proyecto
