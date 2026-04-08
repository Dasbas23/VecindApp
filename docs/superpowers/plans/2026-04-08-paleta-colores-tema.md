# Paleta de Colores VecindApp — Plan de Implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sustituir los colores genéricos de Android por la paleta VecindApp (verde/naranja) y configurar el tema Material Light con los atributos de color correctos.

**Architecture:** Solo se tocan recursos XML (`colors.xml`, `themes.xml`). No se modifican layouts, fragments, lógica de negocio ni TTS. El `AndroidManifest.xml` ya referencia `Theme.VecindApp`, así que no necesita cambios.

**Tech Stack:** Android XML resources, Material Components theme.

---

## Hallazgos del análisis previo

1. **`themes.xml`** ya tiene `Theme.VecindApp` con parent `Theme.MaterialComponents.Light.NoActionBar` pero **sin ningún atributo** — solo la línea del parent. Hay que añadir todos los mappings de color.
2. **`AndroidManifest.xml`** ya usa `@style/Theme.VecindApp` en `<application>` y `<activity>` — **no necesita cambios**.
3. **`colors.xml`** contiene los colores por defecto de Android (`purple_200`, `purple_500`, `purple_700`, `teal_200`, `teal_700`, `black`, `white`) + colores de roles (`rol_comprador*`, `rol_vendedor*`) y cancelada (`cancelada_fondo`, `cancelada_texto`) añadidos en sprint anterior.
4. **No existe `values-night/themes.xml`** — no hay tema oscuro, lo cual es correcto (el prompt pide Light, NO DayNight).
5. Los colores `black` y `white` se usan en otros archivos del proyecto (hardcodeados en adapters como `Color.WHITE`). Los nuevos colores de la paleta no reemplazan estos usos — solo se configura el tema.

---

## File Structure

| Archivo | Acción | Notas |
|---------|--------|-------|
| `app/src/main/res/values/colors.xml` | Modificar: sustituir colores default por paleta VecindApp, mantener colores de roles/cancelada | Los colores `purple_*` y `teal_*` ya no se referencian en ningún sitio |
| `app/src/main/res/values/themes.xml` | Modificar: añadir atributos de color al style existente | Parent ya es correcto, nombre ya es correcto |
| `app/src/main/AndroidManifest.xml` | Sin cambios | Ya referencia `Theme.VecindApp` |

---

### Task 1: Paleta de colores y tema Light

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/themes.xml`
- Verify: `app/src/main/AndroidManifest.xml` (sin cambios necesarios)

- [ ] **Step 1: Sustituir colores en colors.xml**

Reemplazar los colores por defecto de Android (`purple_200` a `white`) por la paleta VecindApp. **Mantener** los colores de roles y cancelada que se añadieron en tareas anteriores.

Estado actual (líneas 1-9):
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
```

Reemplazar por:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ═══ Paleta VecindApp ═══ -->
    <color name="verde_vecindapp_principal">#2E8B57</color>
    <color name="verde_vecindapp_oscuro">#1B5E38</color>
    <color name="verde_vecindapp_claro">#A8D5BA</color>
    <color name="naranja_acento">#E76F51</color>
    <color name="naranja_claro">#F4A261</color>
    <color name="fondo_app">#F4F7F5</color>
    <color name="blanco_tarjetas">#FFFFFF</color>
    <color name="texto_principal">#2D3436</color>
    <color name="texto_secundario">#636E72</color>

    <!-- Alias para compatibilidad -->
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
```

**Nota:** Se mantienen `black` y `white` porque podrían estar referenciados en otros XML del proyecto (ej: `@color/white`, `@color/black`). Se eliminan `purple_*` y `teal_*` que ya no se referencian en ningún sitio (el tema anterior estaba vacío y no los usaba).

**Verificación antes de eliminar:** Buscar referencias a `purple_` y `teal_` en el proyecto:
```bash
grep -r "purple_\|teal_" app/src/main/res/ --include="*.xml"
```
Si no hay referencias fuera de `colors.xml`, se pueden eliminar sin riesgo.

- [ ] **Step 2: Configurar atributos del tema en themes.xml**

Estado actual:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.VecindApp" parent="Theme.MaterialComponents.Light.NoActionBar" />
</resources>
```

Reemplazar por:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.VecindApp" parent="Theme.MaterialComponents.Light.NoActionBar">
        <!-- Colores primarios (verde) -->
        <item name="colorPrimary">@color/verde_vecindapp_principal</item>
        <item name="colorPrimaryVariant">@color/verde_vecindapp_oscuro</item>
        <item name="colorOnPrimary">@color/blanco_tarjetas</item>

        <!-- Colores secundarios (naranja) -->
        <item name="colorSecondary">@color/naranja_acento</item>
        <item name="colorSecondaryVariant">@color/naranja_claro</item>
        <item name="colorOnSecondary">@color/blanco_tarjetas</item>

        <!-- Fondos y superficies -->
        <item name="android:colorBackground">@color/fondo_app</item>
        <item name="colorSurface">@color/blanco_tarjetas</item>
        <item name="colorOnSurface">@color/texto_principal</item>
    </style>
</resources>
```

- [ ] **Step 3: Verificar AndroidManifest.xml**

Confirmar que `android:theme="@style/Theme.VecindApp"` aparece en:
- `<application>` (línea 14)
- `<activity>` (línea 19)

**No se necesitan cambios.** El nombre del tema ya es `Theme.VecindApp`.

- [ ] **Step 4: Build y commit**

```bash
./gradlew assembleDebug
git add app/src/main/res/values/colors.xml \
        app/src/main/res/values/themes.xml
git commit -m "feat: paleta de colores VecindApp verde/naranja con tema Light"
```

---

## Impacto visual esperado

Al aplicar estos cambios, el tema Material propagará automáticamente los colores a:
- **Botones MaterialButton**: fondo `colorPrimary` (verde), texto `colorOnPrimary` (blanco)
- **FABs**: fondo `colorSecondary` (naranja), icono `colorOnSecondary` (blanco)
- **Barras de estado**: `colorPrimaryVariant` (verde oscuro)
- **Fondo general**: `colorBackground` (gris verdoso claro #F4F7F5)
- **MaterialCardView**: `colorSurface` (blanco)
- **Textos sobre cards**: `colorOnSurface` (gris oscuro #2D3436)
- **Slider**: track/thumb en `colorPrimary` (verde)
- **TabLayout**: indicador en `colorPrimary` (verde)

**No se verán afectados:**
- Colores hardcodeados en adapters (0xFFF59E0B, 0xFF10B981, etc. en TransaccionAdapter/HistorialAdapter)
- Colores de roles (`rol_comprador*`, `rol_vendedor*`) y cancelada — se mantienen intactos
- Los `OutlinedButton` usarán stroke en `colorPrimary` (verde) automáticamente

## Riesgos

- **Ninguno significativo.** Los colores `purple_*` y `teal_*` no se referenciaban desde el tema (estaba vacío) ni desde layouts. La eliminación es segura.
- Los botones con `app:backgroundTint="#D32F2F"` hardcodeado (como `btnEliminar` en `fragment_detalle_servicio.xml`) mantendrán su color rojo — no se ven afectados por el tema.
