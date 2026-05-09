# Spec — Guía interactiva de comandos bash

- **Fecha:** 2026-04-26
- **Ubicación del proyecto:** `C:\Users\Marius\Desktop\Programacion\Aprendizaje\guia-bash\`
- **Archivo final (artefacto):** `guia-bash.html` en la misma carpeta
- **Estado:** spec aprobado — pendiente plan de implementación

---

## 1. Objetivo

Construir una **guía HTML autocontenida** de comandos bash para uso personal del autor.

Caso de uso principal: consultar rápido qué comando usar (y con qué flags) según la necesidad — *"quiero buscar archivos por X"*, *"quiero buscar texto dentro de archivos"*, *"cómo encadeno este comando con otro"*. La guía sustituye el reflejo de preguntar a una IA en cada duda; cuando algo no esté en la guía, entonces sí se consulta a la IA.

Objetivo secundario: que el autor **aprenda bash** mientras la usa. Esto matiza las tarjetas más importantes (ver §6.1).

---

## 2. Alcance e iteraciones

La guía crece por iteraciones. **No se debe intentar cubrir todo de golpe.**

| Iteración | Tema                              | Estado    |
|-----------|-----------------------------------|-----------|
| 1         | Búsqueda (archivos y contenido)   | ⬅ ahora  |
| 2         | Manipulación de archivos          | después  |
| 3         | Bash completo (variables, scripting, control de flujo, alias) | futuro |

Cada iteración añade tarjetas al mismo archivo HTML. **No se rehace la estructura entre iteraciones**: el esquema de tarjeta (§6) y el sistema de filtros (§5) deben aguantar las tres iteraciones desde el día uno. Por eso se fija ahora aunque solo cargues búsqueda.

---

## 3. Stack técnico

- **Un solo archivo HTML** (`.html`). Monolito asumido y aceptado, no se va a partir.
- **CSS:** Tailwind por CDN (`<script src="https://cdn.tailwindcss.com"></script>` o equivalente Play CDN).
- **JavaScript:** vanilla, embebido en `<script>` dentro del mismo archivo. Sin frameworks (no React/Vue/Svelte). Sin build step.
- **Datos:** un `const COMANDOS = [ … ]` embebido en el mismo `<script>`. Sin fetch, sin JSON externo.
- **Sin backend, sin tracking, sin dependencias adicionales.** Requiere internet para cargar Tailwind la primera vez; se asume que el usuario lo tendrá.

---

## 4. Entorno objetivo

Prioridad explícita: **Git Bash en Windows**. Es el shell que el usuario utiliza día a día.

Preparado para **Linux/WSL** como segunda fase (uso futuro en ciberseguridad). Esto significa:

- Los ejemplos cargados ahora deben funcionar en Git Bash (o marcar claramente cuando no).
- El esquema de datos (§6) ya contempla notas de compatibilidad por flag/ejemplo desde el día uno, para no rehacer datos al añadir comandos solo-Linux más adelante.
- **NO** se debe omitir información solo porque sea Linux-only; se carga y se anota.

### 4.1 Compatibilidad — granularidad fina, no por tarjeta

❌ **NO hacer:** un badge global "Git Bash OK" o "Linux only" en la cabecera de la tarjeta.

Razón: las incompatibilidades entre Git Bash (MSYS2/MinGW) y Linux casi nunca son a nivel de comando. `find` "funciona" en Git Bash, pero `find -printf` no. `grep` "funciona", pero `grep -P` puede no estar compilado. Un badge a nivel de tarjeta crea modelos mentales falsos.

✅ **Hacer:** notas de compatibilidad en el campo concreto que la requiere — un flag, un ejemplo, una receta. Ver el campo `compat_notes?` en el esquema (§6).

A nivel de tarjeta, un campo `gitbash` con **tres estados** distingue los casos reales:

- `ok` — 100% portable. Funciona idéntico en Git Bash y Linux.
- `caveats` — el comando existe en Git Bash pero algún flag/ejemplo no aplica. Las notas finas (`compat_notes`) explican qué.
- `no-disponible` — el comando entero no existe en Git Bash (ej: `locate`). Se carga igualmente, con alternativa documentada.

El filtro "Compatible con Git Bash" incluye `ok` + `caveats`, excluye solo `no-disponible`. Así no se ocultan herramientas usables (como `find`) por culpa de un flag puntual. El render puede pintar un indicador visual (verde / amarillo / rojo) a la izquierda del nombre — pero esto sigue siendo coarse-grained; la verdad fina sigue viviendo en `compat_notes` por flag/ejemplo.

---

## 5. Experiencia de usuario

### 5.1 Layout general

```
┌──────────────────────────────────────────────────────┐
│  Título + búsqueda libre (input)                     │
├──────────────────────────────────────────────────────┤
│  Filtros: [categoría ▾] [dificultad ▾] [tipo ▾] [☐] │
├──────────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │ tarjeta  │  │ tarjeta  │  │ tarjeta  │           │
│  └──────────┘  └──────────┘  └──────────┘           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │ tarjeta  │  │ tarjeta  │  │ tarjeta  │           │
│  └──────────┘  └──────────┘  └──────────┘           │
└──────────────────────────────────────────────────────┘
```

### 5.2 Filtros

Filtrado **en vivo** (sin botón "aplicar"), todo combinable (AND entre filtros, dentro de cada filtro multi-valor es OR).

Ejes de filtrado:
- **Categoría** (`categoria`) — siempre **temática**: `busqueda-archivos`, `busqueda-contenido`. Más adelante: `manipulacion`, `redireccion`, `permisos`, `proceso`, `red`, etc. Aplica a `tipo: "comando"` (obligatorio) y `tipo: "receta"` (obligatorio, para que la receta aparezca al filtrar por su tema). **NO** se usa para distinguir comando/operador/receta — eso es trabajo de `tipo`.
  - **Comportamiento del filtro con tarjetas sin `categoria`** (transversales — operadores generales como `|`, `&&`, `;`, y futuras tarjetas de sintaxis sin tema): se muestran **siempre**, ignorando el filtro de categoría activo. Razón: un pipe sirve para todos los temas, ocultarlo al filtrar por `busqueda-archivos` esconde justo la pieza que une los comandos del tema. El filtro temático no las descarta; solo las descartan los filtros `tipo` o `dificultad` o la búsqueda libre.
- **Dificultad** (`dificultad`): `basico`, `intermedio`, `avanzado`.
- **Tipo** (`tipo`): `comando`, `operador`, `receta`, `sintaxis`. Este último (variables, control de flujo, funciones, here-docs…) se cargará en la iteración 3, pero el enum se fija ahora para no romper la promesa de §2.
- **Compatible con Git Bash** (checkbox que filtra `gitbash !== "no-disponible"` — incluye `ok` y `caveats`, oculta solo lo que no existe en Git Bash). Ver §4.1.
- **Búsqueda libre** (input texto): match en `nombre`, `descripcion`, y opcionalmente `tags`.

### 5.3 Estado vacío

Si los filtros no devuelven nada, mostrar mensaje claro: *"Ningún comando coincide con estos filtros."* + botón "Limpiar filtros".

### 5.4 Interacción con la tarjeta

- Cada tarjeta es plegable o tiene "ver más" si el contenido es largo (las tarjetas gordas tipo `find`/`grep` lo van a ser).
- Botón **copiar** (📋) en cada bloque de código — para que los ejemplos se peguen al terminal sin trabajo manual.
  - **Implementación obligatoria con fallback**: el HTML se va a abrir con doble click desde el explorador (`file://`), donde `navigator.clipboard.writeText` no es fiable (Chrome lo permite, Firefox lo bloquea). El botón debe usar `navigator.clipboard` cuando esté disponible y caer a `document.execCommand('copy')` con un `<textarea>` temporal en caso contrario. Siempre debe haber **feedback visual del resultado** (toast breve o cambio de icono "✓ copiado" / "✗ no se pudo copiar") para no dejar al usuario adivinando.
- No usar `localStorage` ni `sessionStorage`: estado en memoria de la sesión.

### 5.5 Modo oscuro

No decidido. Implementación a criterio de quien construye, pero si se añade que sea con toggle visible, no auto-detectado en silencio.

---

## 6. Esquema de tarjeta — contrato JSON

**Esto es lo que cada elemento de `const COMANDOS = [...]` debe respetar.** El esquema soporta los tres tipos (comando, operador, receta) con campos opcionales. Definirlo ahora — antes de cargar tarjetas — es la principal lección de la revisión previa.

```js
{
  // ─── OBLIGATORIOS (todos los tipos) ───────────────────────────
  id: "find",                      // slug único, kebab-case
                                   // Convenciones por tipo:
                                   //   comando  → nombre tal cual.   Ej: "find", "grep", "xargs".
                                   //   operador → prefijo "op-".     Ej: "op-pipe", "op-and", "op-or",
                                   //                                     "op-redirect-stdout", "op-redirect-append",
                                   //                                     "op-cmd-substitution", "op-stderr-stdout".
                                   //   receta   → prefijo "receta-". Ej: "receta-buscar-todo-en-proyecto".
                                   //   sintaxis → prefijo "sx-".     Ej: "sx-if", "sx-for", "sx-heredoc". (futuro)
  tipo: "comando",                 // "comando" | "operador" | "receta" | "sintaxis"
  nombre: "find",                  // display name
  categoria: "busqueda-archivos",  // temática (ver §5.2). Obligatoria en "comando" y "receta".
                                   // Opcional en "operador" y "sintaxis" (omitir si no encaja en una temática concreta).
  dificultad: "intermedio",        // "basico" | "intermedio" | "avanzado"
  descripcion: "Recorre un árbol de directorios buscando archivos según criterios.",
  gitbash: "caveats",              // "ok" | "caveats" | "no-disponible"  (ver §4.1)
                                   //   ok           = portable 100%
                                   //   caveats      = comando existe; algún flag/ejemplo no aplica
                                   //   no-disponible = el comando entero no está en Git Bash

  // ─── COMUNES OPCIONALES ────────────────────────────────────────
  tags: ["recursivo", "filtros"],  // para búsqueda libre, opcional
  patron: "find [ruta] [tests] [acción]",    // patrón de uso general, opcional.
                                             // Renombrado desde "sintaxis" para no chocar con tipo: "sintaxis".

  // ─── PARA TARJETAS GORDAS (find, grep, etc.) ───────────────────
  modelo_mental: "find recorre un árbol de directorios y aplica un test a cada entrada. Los flags como -name, -type, -mtime son tests; se combinan con AND implícito o -o para OR. -exec y -delete son acciones que se aplican a las coincidencias.",
  // 2-3 frases. CLAVE para el objetivo "aprender bash".
  // Solo en tarjetas importantes (find, grep, xargs, sed, awk...). NO en tarjetas básicas (pwd, cd, ls).

  // ─── EJEMPLOS (recomendado en casi todas las tarjetas) ─────────
  ejemplos: [
    {
      comando: "find . -name '*.ts'",
      explicacion: "Busca todos los archivos .ts a partir del directorio actual.",
      compat_notes: null            // null o string. Ej: "Requiere comillas en Git Bash para evitar glob expansion del shell."
    },
    {
      comando: "find . -type f -mtime -7",
      explicacion: "Archivos (no directorios) modificados en los últimos 7 días.",
      compat_notes: null
    }
  ],

  // ─── FLAGS (en tarjetas medias y gordas) ───────────────────────
  flags: [
    { flag: "-name PATRON",  descripcion: "Filtra por nombre (glob).",       compat_notes: null },
    { flag: "-type f|d|l",   descripcion: "Filtra por tipo (file/dir/link).",compat_notes: null },
    { flag: "-mtime ±N",     descripcion: "Modificados hace N días.",         compat_notes: null },
    { flag: "-printf FMT",   descripcion: "Formato de salida personalizado.", compat_notes: "No disponible en Git Bash. Alternativa: -exec stat o procesar con awk." }
  ],

  // ─── COMBOS (cómo se enchufa con otros comandos) ───────────────
  combos: [
    {
      comando: "find . -name '*.log' | xargs grep ERROR",
      explicacion: "Busca 'ERROR' en todos los .log del árbol.",
      compat_notes: null
    },
    {
      comando: "find . -name '*.tmp' -delete",
      explicacion: "Equivalente a `find ... -exec rm`, más eficiente.",
      compat_notes: null
    }
  ],

  // ─── EXTRAS (solo tarjetas gordas) ─────────────────────────────
  gotchas: [
    "Sin -type f, también devuelve directorios.",
    "-name es case-sensitive; usar -iname para case-insensitive.",
    "Las acciones (-exec, -delete) se aplican a TODOS los matches: probar antes con -print."
  ],
  relacionados: ["grep", "xargs", "ls", "locate"],   // ids de otras tarjetas
  cuando_no_usar: "Para búsquedas frecuentes en árboles enormes (node_modules, /usr), considerar `fd` o `ripgrep` por velocidad. `locate` si solo necesitas nombre y tienes la BD actualizada (no en Git Bash).",

  // ─── SOLO PARA tipo: "receta" ──────────────────────────────────
  escenario: "Buscar archivos modificados en los últimos 7 días que contengan la cadena 'TODO'",
  desglose: [
    { fragmento: "find . -mtime -7 -type f", hace: "Lista archivos modificados últimos 7 días." },
    { fragmento: "| xargs grep -l 'TODO'",   hace: "Pasa esa lista a grep, que devuelve solo los que contienen 'TODO'." }
  ]
}
```

### 6.1 Reglas de uso del esquema

- **Tarjetas básicas** (ej: `pwd`, `cd`, `ls` simple): solo obligatorios + `ejemplos` + opcionalmente `flags`. No llevan `modelo_mental`, `gotchas`, `cuando_no_usar`. `tipo: "comando"`. `categoria` obligatoria.
- **Tarjetas gordas** (ej: `find`, `grep`, `xargs`, `sed`, `awk`): obligatorios + `modelo_mental` + `ejemplos` + `flags` + `combos` + `gotchas` + `relacionados` + `cuando_no_usar`. `tipo: "comando"`. `categoria` obligatoria. **Nota:** `xargs` es un comando (`/usr/bin/xargs`), por tanto va aquí, no en operadores.
- **Tarjetas de operador** (operadores reales del shell: `|`, `>`, `>>`, `2>&1`, `&&`, `||`, `;`, `<`, `$()`): `tipo: "operador"`. Suelen llevar `modelo_mental` (son conceptos), `ejemplos`, `combos`, `gotchas`. No tienen `flags`. `categoria` opcional (un pipe se usa en todos los temas; omitir antes que forzar).
- **Tarjetas de receta**: `tipo: "receta"`, llevan `escenario` + `desglose` + un único bloque en `ejemplos` con el comando completo. `relacionados` apunta a los comandos involucrados. `categoria` obligatoria y debe coincidir con el tema del problema que resuelve la receta (ej: receta "buscar TODO en proyecto" → `categoria: "busqueda-contenido"`), para que aparezca al filtrar por ese tema.
- **Tarjetas de sintaxis** (futuro, iteración 3 — `if`, `for`, `while`, funciones, `<<EOF`, arrays, etc.): `tipo: "sintaxis"`. Estructura por definir cuando llegue su iteración; el enum queda reservado ya. Probable: `modelo_mental` + `ejemplos` + `gotchas`. `categoria` opcional.

`compat_notes` siempre presente con `null` cuando no aplica — facilita el render condicional sin tener que comprobar `undefined`.

---

## 7. Tipos de tarjeta — desglose

### 7.1 Comandos individuales
Una tarjeta por comando. La mayoría del catálogo.

### 7.2 Operadores como ciudadanos de primera clase

Solo **operadores reales del shell** (símbolos parseados por bash). Cada uno tiene tarjeta propia con `tipo: "operador"`. Para esta iteración (búsqueda), priorizar:

- `|` (pipe)
- `$( )` (command substitution)
- `>`, `>>`, `2>&1` (relevantes para guardar resultados de búsqueda)
- `&&`, `||`, `;` — encadenado, no específicos de búsqueda pero entran sin problema.
- `<` (redirección de entrada) — opcional en esta iteración, encaja mejor en la 2.

**No** son operadores y por tanto **no** entran aquí:
- `xargs` — es un **comando** (`/usr/bin/xargs`). Va como tarjeta de comando gorda en §8 (búsqueda de archivos / encadenado), no como operador.
- `find -exec` — es un **flag** de `find`. Su contenido vive dentro de la tarjeta de `find` (en sus `flags` y `combos`), nunca como tarjeta separada.

### 7.3 Recetas — casos compuestos reales
Recetas mínimas para la primera iteración:
- Buscar texto en un proyecto: `grep -rn "patrón" .`
- Archivos modificados en X días: `find . -mtime -N`
- Archivos modificados que contengan texto: `find . -mtime -7 | xargs grep "TODO"`
- Top 10 ficheros más grandes: `find . -type f -printf '%s %p\n' | sort -rn | head -10` (anotar incompatibilidad Git Bash y dar alternativa)
- Excluir node_modules en una búsqueda: `find . -path ./node_modules -prune -o -type f -print`
- Contar líneas de código: `find . -name '*.js' | xargs wc -l`

---

## 8. Catálogo inicial sugerido — iteración 1 (búsqueda)

Lista mínima a cargar. **No exhaustiva**: si tiene sentido añadir alguno más, adelante.

**Comandos — búsqueda de archivos**
- `find` (gorda)
- `ls` (media)
- `tree` (básica, anotar que requiere instalación en Git Bash)
- `which`, `whereis`, `type` (básicas, agrupables o separadas)
- `locate` (mencionar; no disponible en Git Bash, alternativas)
- `file` (básica, identifica tipo de archivo)
- `pwd` (básica, contexto)

**Comandos — búsqueda de contenido**
- `grep` (gorda)
- `egrep` / `fgrep` (mencionar como variantes; modernamente `grep -E` y `grep -F`)
- `head`, `tail` (básicas — relevantes para inspeccionar resultados de búsqueda)

**Comandos — encadenado / procesamiento de listas (relevantes en búsqueda)**
- `xargs` (gorda; categoria: `busqueda-archivos` por su uso típico tras `find`. Es un comando, no un operador — ver §7.2).

**Operadores**
- Los listados en §7.2.

**Recetas**
- Las listadas en §7.3.

---

## 9. Lo que NO debe hacer la implementación

- **No** introducir frameworks JS (React, Vue, Alpine, etc.).
- **No** introducir un build step ni dependencias npm.
- **No** dividir el archivo en varios (HTML/CSS/JS/JSON separados). Monolito.
- **No** usar `localStorage` o `sessionStorage`.
- **No** poner badges de compatibilidad globales por tarjeta como sustituto de las notas finas. El indicador visual derivado de `gitbash` (§4.1) está permitido como ayuda, pero las notas de incompatibilidad concretas van **siempre** en `compat_notes` del flag/ejemplo afectado.
- **No** fundir `tipo` y `categoria`: `tipo` distingue comando/operador/receta/sintaxis, `categoria` es el tema (búsqueda-archivos, búsqueda-contenido…). Una tarjeta de receta sobre búsqueda de contenido tiene `tipo: "receta"` Y `categoria: "busqueda-contenido"`, NO `categoria: "receta"`.
- **No** crear tarjetas para flags concretos (`find -exec`, `grep -P`…). Los flags viven dentro de la tarjeta de su comando (§7.2).
- **No** ocultar gotchas para "simplificar" — son parte del valor de la guía.
- **No** inventar comandos o flags que el implementador no esté seguro de que existen tal cual; en caso de duda, preguntar o marcar claramente.
- **No** asumir colores ni tipografía sin Tailwind — todo el styling pasa por clases utility.
- **No** añadir analytics, tracking ni llamadas externas más allá del CDN de Tailwind.

---

## 10. Decisiones cerradas (resumen)

| Tema                          | Decisión                                                 |
|-------------------------------|----------------------------------------------------------|
| Alcance iteración 1           | Búsqueda (archivos y contenido)                          |
| Formato                       | Filtros + tarjetas, filtrado en vivo                     |
| Stack                         | Single-file HTML + Tailwind por CDN + JS vanilla         |
| Datos                         | `const COMANDOS = [...]` embebido                        |
| Entorno prioritario           | Git Bash; Linux/WSL preparado                            |
| Compatibilidad                | Tres estados (`gitbash: ok`/`caveats`/`no-disponible`) + notas finas en `compat_notes` por flag/ejemplo |
| Filtros (cómo NO mezclar)     | `tipo` separa comando/operador/receta/sintaxis; `categoria` es siempre temática |
| Profundidad de tarjeta        | Variable: básica para `pwd`/`ls`, gorda para `find`/`grep` |
| Combinaciones                 | Tarjetas individuales + tarjetas de operadores + recetas |
| Esquema JSON                  | Fijado en §6, todas las tarjetas lo respetan             |
| Modo oscuro                   | A decidir al construir; con toggle si se añade           |
| Ubicación del proyecto        | `C:\Users\Marius\Desktop\Programacion\Aprendizaje\guia-bash\` |
| Archivo final                 | `guia-bash.html` en la misma carpeta                     |

---

## 11. Roadmap de iteraciones futuras (referencia, no implementar ahora)

**Iteración 2 — Manipulación de archivos**
`cp`, `mv`, `rm`, `mkdir`, `rmdir`, `touch`, `ln`, `tar`, `gzip`/`gunzip`, `zip`/`unzip`, `chmod`, `chown`, redirecciones (`>`, `>>`, `<`, `2>`, `&>`, `tee`).

**Iteración 3 — Bash completo**
Variables, `export`, `$VAR` vs `${VAR}`, `read`, control de flujo (`if`, `case`, `for`, `while`, `until`), funciones, `alias`, scripting (`#!`, parámetros posicionales, `$@`, `$#`, `$?`), tests (`[ ]`, `[[ ]]`), command substitution avanzada, here-docs (`<<EOF`), arrays.

Estos van como `tipo: "sintaxis"` (enum ya reservado en §5.2 y §6). Cuando se aborde la iteración, se concreta qué subset del esquema usan las tarjetas de sintaxis (probable: `modelo_mental` + `ejemplos` + `gotchas`, sin `flags`).

Cada iteración añade comandos al mismo archivo siguiendo el esquema de §6. Las nuevas categorías temáticas se añaden al filtro de §5.2 sin romper las existentes.
