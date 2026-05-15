# Guía Bash — Iteración 3 (scripting) — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ampliar `guia-bash.html` con el catálogo de scripting bash descrito en `spec-iter3.md`: 15 tarjetas `tipo: "sintaxis"` (10 gordas + 5 básicas) y 5 recetas. Actualizar header y filtro de categorías con 3 nuevas categorías (`variables`, `control-flujo`, `funciones-y-scripting`). Una nota sobre backticks en la tarjeta `op-cmd-substitution` de iter 1.

**Architecture:** Misma arquitectura que iter 1/2 — un único `guia-bash.html` autocontenido con Tailwind por CDN, JS vanilla, datos embebidos en `const COMANDOS = [...]`. **Ningún cambio de schema, helpers ni lógica.** Esta iteración añade entradas al array, 3 `<option>` al dropdown de categoría, ajusta el header y modifica un campo `gotchas` de una tarjeta existente. Los renderers ya cubren el material nuevo: `renderTipoBadge` mapea `'sintaxis'` a `bg-amber-100 text-amber-800` desde iter 1.

**Tech Stack:** HTML5 + Tailwind CSS (Play CDN) + JavaScript vanilla. Sin build, sin npm, sin frameworks. Apertura por `file://` con doble click.

---

## Pre-requisitos

- **Spec aprobado:** `C:\Users\Marius\Desktop\Programacion\VecindApp\guia-bash\spec-iter3.md`. Léelo antes de empezar — extiende `spec.md` y `spec-iter2.md`.
- **Iteración 2 finalizada y commiteada:** commit `74777ad` ("Añadir guía bash interactiva (iteraciones 1 y 2)") deja `guia-bash.html` con 49 tarjetas. Verificable abriendo el HTML: banner verde, 49 tarjetas, 5 categorías + Todas.
- **Repo:** `C:\Users\Marius\Desktop\Programacion\VecindApp\`, branch `desarrollo`.
- **Carpeta de trabajo:** todas las modificaciones ocurren en `guia-bash/guia-bash.html`. **No se crean archivos nuevos.**
- **Convención de mensajes de commit:** español, imperativo presente. **NO incluir línea `Co-Authored-By`.**

---

## File Structure

Modificado en este plan (único archivo):

| Ruta (relativa a `VecindApp/`) | Responsabilidad |
|---|---|
| `guia-bash/guia-bash.html` | Único artefacto. Las modificaciones tocan: `<title>` + `<h1>` + `<p>` del header, `<select id="filter-categoria">` (3 opciones nuevas), `const COMANDOS = [...]` (20 entradas nuevas) y una entrada existente (`op-cmd-substitution` recibe una nueva línea en `gotchas` sobre backticks). |

No se crean tests separados ni CSS externo. Mismo monolito.

---

## Estrategia de testing

- Los tests inline existentes (`applyFilters` con 8 asserts, `renderCard` con 4 asserts) cubren la lógica. Esta iteración **no añade lógica nueva** — solo datos. No hace falta ampliar la suite.
- Tras cada tarea de carga de datos, verificación visual recargando el HTML:
  - Banner verde "✓ N tests OK" sigue saliendo.
  - El conteo total de tarjetas (sin filtros) aumenta según lo añadido.
  - Filtros nuevos se comportan según `spec-iter3.md` §3.1.

**Cómo "correr los tests":** abrir `guia-bash.html` con doble click. Los tests corren automáticamente al cargar.

---

## Convenciones generales para todas las tareas

- **Cwd para git:** `C:\Users\Marius\Desktop\Programacion\VecindApp\` (usar `git -C` ahí). En Git Bash usar forward slashes en paths.
- **Tras cada cambio:** abrir/recargar el HTML y validar el "Esperado" del step.
- **Commit por tarea:** mensaje en español, sin `Co-Authored-By`. (Si el usuario prefiere un único commit final, se puede saltar el commit por tarea y commitear todo al final — pero por defecto el plan supone commit por tarea, igual que iter 1.)
- **Si una verificación falla:** no commitear. Diagnosticar, corregir, validar, entonces commitear.
- **Punto de inserción del array:** buscar `// ====== JS: DATA ======`. La sección termina con `    ];`. Las nuevas tarjetas se añaden justo antes del `];`, cada una prefijada con `,{` y manteniendo 6 espacios de indentación.
- **Convención del campo `compat_notes`:** siempre presente en cada `ejemplo` y cada `combo` (puede ser `null`).
- **Convención de IDs (spec-iter3 §6):**
  - Sintaxis: prefijo `sx-` (`sx-variables`, `sx-export`, `sx-read`, `sx-arrays`, `sx-if`, `sx-case`, `sx-for`, `sx-while`, `sx-until`, `sx-tests`, `sx-functions`, `sx-alias`, `sx-shebang`, `sx-special-vars`, `sx-heredoc`).
  - Recetas: prefijo `receta-` (`receta-leer-archivo-linea`, `receta-iterar-archivos`, `receta-validar-args`, `receta-funcion-return`, `receta-heredoc-config`).
- **Campo `gitbash`:** salvo notas explícitas en cada tarea, todo el material de iter 3 es `'ok'` (bash es bash; las features de bash 4+ como arrays asociativos están presentes en Git Bash).

---

## Tasks

### Task 1: Actualizar header y dropdown de categorías

**Files:**
- Modify: `guia-bash/guia-bash.html` (`<head>`, `<header>`, `<select id="filter-categoria">`)

- [ ] **Step 1: Cambiar el `<title>` del documento**

Buscar:

```html
  <title>Guía Bash — Búsqueda y manipulación</title>
```

Sustituir por:

```html
  <title>Guía Bash</title>
```

- [ ] **Step 2: Actualizar el `<h1>` y subtítulo del header**

Buscar:

```html
      <h1 class="text-2xl font-bold">Guía Bash — Búsqueda y manipulación</h1>
      <p class="text-sm text-slate-600">Iteraciones 1–2: búsqueda, manipulación, compresión, permisos, operadores y recetas.</p>
```

Sustituir por:

```html
      <h1 class="text-2xl font-bold">Guía Bash</h1>
      <p class="text-sm text-slate-600">Iteraciones 1–3: búsqueda, manipulación, scripting completo.</p>
```

- [ ] **Step 3: Añadir las 3 nuevas opciones al dropdown de categoría**

Buscar el bloque del select (acaba con las 5 categorías de iter 2):

```html
          <option value="manipulacion-archivos">Manipulación de archivos</option>
          <option value="compresion">Compresión</option>
          <option value="permisos">Permisos</option>
        </select>
```

Sustituir por:

```html
          <option value="manipulacion-archivos">Manipulación de archivos</option>
          <option value="compresion">Compresión</option>
          <option value="permisos">Permisos</option>
          <option value="variables">Variables</option>
          <option value="control-flujo">Control de flujo</option>
          <option value="funciones-y-scripting">Funciones y scripting</option>
        </select>
```

- [ ] **Step 4: Verificar visualmente**

Recargar `guia-bash.html`. **Esperado:**
- Pestaña del navegador con título "Guía Bash".
- Header: `Guía Bash` + subtítulo "Iteraciones 1–3: búsqueda, manipulación, scripting completo."
- Dropdown "Categoría" con 9 opciones (Todas + 8 categorías).
- Seleccionar "Variables" → grid vacío con empty state. Los 11 operadores transversales **sí** deben seguir apareciendo (regla "sin categoria pasan siempre").
- Banner verde de tests sigue saliendo.

- [ ] **Step 5: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Actualizar header y dropdown de categorías para iteración 3"
```

---

### Task 2: Cargar `sx-variables` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar la tarjeta de `sx-variables` antes del `];` del array**

```js
      ,{
        id: 'sx-variables',
        tipo: 'sintaxis',
        nombre: 'Variables y expansion',
        categoria: 'variables',
        dificultad: 'basico',
        descripcion: 'Asignar y leer variables en bash. Incluye parameter expansion (defaults, prefix/suffix strip, length, slicing, reemplazo).',
        gitbash: 'ok',
        patron: 'NOMBRE=valor    /    $NOMBRE    /    ${NOMBRE}    /    ${NOMBRE:-default}',
        tags: ['variable', 'expansion', 'parameter'],
        modelo_mental: 'Las variables en bash son SIEMPRE strings (no hay tipos). Se asignan SIN espacios alrededor del = (`a=hola`, NO `a = hola` — eso es un comando). Se leen con `$nombre` o `${nombre}`. La forma `${...}` desbloquea operaciones extra: defaults `${V:-x}`, prefix strip `${V##*/}`, suffix strip `${V%.*}`, length `${#V}`, slice `${V:0:3}`, reemplazo `${V/foo/bar}`. Variables sin definir expanden a string vacío (peligroso — usar `set -u` o `${V:?error}` para fallar).',
        ejemplos: [
          { comando: 'nombre="Marius"',                  explicacion: 'Asignación. Sin espacios alrededor de =. Comillas opcionales si no hay espacios en el valor, pero recomendadas siempre.', compat_notes: null },
          { comando: 'echo "Hola $nombre"',              explicacion: 'Expansión simple. Las comillas dobles permiten expansión; las simples NO.',                                                  compat_notes: null },
          { comando: 'echo "${nombre}_sufijo"',          explicacion: '${} delimita el nombre cuando va pegado a más texto. Sin las llaves, `$nombre_sufijo` se interpretaría como otra variable.',  compat_notes: null },
          { comando: 'echo "${NOMBRE:-anónimo}"',        explicacion: 'Si NOMBRE no está definida o está vacía, usa "anónimo". No asigna; solo expande.',                                            compat_notes: null },
          { comando: 'archivo="/home/m/foto.jpg"; echo "${archivo##*/}"', explicacion: 'Elimina prefijo más largo que matchee */. Resultado: "foto.jpg" (basename).',                                  compat_notes: null },
          { comando: 'archivo="/home/m/foto.jpg"; echo "${archivo%.*}"',  explicacion: 'Elimina sufijo más corto que matchee .*. Resultado: "/home/m/foto".',                                         compat_notes: null },
          { comando: 'cadena="hola mundo"; echo "${#cadena}"',           explicacion: 'Length de la cadena. Resultado: 10.',                                                                          compat_notes: null },
          { comando: 'cadena="HOLA"; echo "${cadena,,}"',                explicacion: 'Bash 4+: convierte a minúsculas. `${V^^}` a mayúsculas.',                                                       compat_notes: null }
        ],
        combos: [
          { comando: 'cp "$archivo" "${archivo}.bak"',                          explicacion: 'Backup con sufijo. Las comillas dobles preservan espacios en nombres.',                          compat_notes: null },
          { comando: ': "${PORT:=3000}"; echo "$PORT"',                          explicacion: '`:=` asigna el default si la variable no estaba definida. El `:` al principio es un no-op.',     compat_notes: null },
          { comando: 'for f in *.JPG; do mv "$f" "${f%.JPG}.jpg"; done',         explicacion: 'Renombra masivo: quita sufijo .JPG y añade .jpg.',                                                 compat_notes: null }
        ],
        gotchas: [
          'SIN espacios alrededor del =. `a=1` ✓. `a = 1` ✗ (lo interpreta como ejecutar el comando "a" con args "=" y "1").',
          'Variables no definidas expanden a "" sin error. `rm -rf $UNDEFINED/datos` se vuelve `rm -rf /datos` — catastrófico. Usar `set -u` (errror en variables no definidas) o `${V:?msg}` para abortar.',
          'Comillas dobles preservan expansión; comillas simples NO. `echo "$HOME"` → `/home/x`; `echo \'$HOME\'` → `$HOME` literal.',
          'SIEMPRE entrecomillar variables al usarlas como argumentos: `cp "$src" "$dst"` (espacios en nombres no rompen).',
          'Diferencia `:-` (usa default sin asignar) vs `:=` (usa default Y asigna) vs `:?` (error si no definida) vs `:+` (usa valor alternativo si SÍ está definida).',
          '`${V##patron}` (greedy) vs `${V#patron}` (no greedy). Igual con `%%` vs `%`.'
        ],
        relacionados: ['sx-export', 'sx-read', 'sx-arrays', 'sx-special-vars'],
        cuando_no_usar: 'Para manipulación de texto compleja (parsear JSON, CSV, regex sofisticada), las variables y parameter expansion se quedan cortos. Usar `jq` (JSON), `awk` (columnar), Python, o pasar a un lenguaje real.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 50 tarjetas. Filtrar Categoría = "Variables" → 1 tarjeta (`sx-variables`) + 11 operadores transversales = 12. Tarjeta con badge ámbar "sintaxis", punto verde gitbash, patrón visible, "Ver más ▾" (es gorda).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de variables y parameter expansion"
```

---

### Task 3: Cargar `sx-read` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-read` después de `sx-variables`**

```js
      ,{
        id: 'sx-read',
        tipo: 'sintaxis',
        nombre: 'read',
        categoria: 'variables',
        dificultad: 'intermedio',
        descripcion: 'Builtin para leer una línea de stdin y asignarla a una o varias variables.',
        gitbash: 'ok',
        patron: 'read [opciones] VAR1 [VAR2 ...]',
        tags: ['stdin', 'input', 'while'],
        modelo_mental: 'read lee UNA línea de stdin. Si das varios nombres de variable, hace split por IFS (espacios/tabs por defecto). Es la base de los scripts interactivos y del patrón canónico para procesar archivos: `while IFS= read -r line; do ... done < archivo`. Casi siempre quieres `-r` (no interpretar backslash) e `IFS=` (no trimear espacios) para procesar texto fielmente.',
        ejemplos: [
          { comando: 'read nombre',                          explicacion: 'Espera input del usuario y lo asigna a $nombre.',                                          compat_notes: null },
          { comando: 'read -p "Tu edad: " edad',             explicacion: 'Imprime el prompt antes de leer (-p).',                                                    compat_notes: null },
          { comando: 'read -s -p "Password: " pwd',          explicacion: '-s = silent: no muestra lo escrito (típico para contraseñas).',                            compat_notes: null },
          { comando: 'read -t 5 valor || echo "timeout"',    explicacion: '-t 5 = timeout 5s; si no hay input, read devuelve != 0.',                                  compat_notes: null },
          { comando: 'read -n 1 -p "¿Sí/No? " respuesta',     explicacion: '-n 1 = lee solo 1 carácter (sin esperar Enter).',                                          compat_notes: null },
          { comando: 'while IFS= read -r line; do echo ">> $line"; done < datos.txt', explicacion: 'Iteración canónica línea a línea. IFS= preserva espacios; -r preserva backslashes.', compat_notes: null }
        ],
        flags: [
          { flag: '-p PROMPT', descripcion: 'Imprime PROMPT antes de leer (más cómodo que `echo -n` previo).',                          compat_notes: null },
          { flag: '-r',         descripcion: 'No interpretar `\\` como escape (preserva backslashes literales).',                       compat_notes: null },
          { flag: '-s',         descripcion: 'Silent: no echo de los caracteres escritos (passwords).',                                  compat_notes: null },
          { flag: '-t N',       descripcion: 'Timeout en N segundos. read devuelve código != 0 si expira.',                              compat_notes: null },
          { flag: '-n N',       descripcion: 'Lee hasta N caracteres sin esperar Enter.',                                                 compat_notes: null },
          { flag: '-a ARR',     descripcion: 'Asigna las palabras (split por IFS) a un array indexed.',                                  compat_notes: null },
          { flag: '-d DELIM',   descripcion: 'Usa DELIM (en lugar de \\n) como terminador de la línea.',                                  compat_notes: null }
        ],
        combos: [
          { comando: 'cat archivo | while IFS= read -r line; do echo "$line"; done', explicacion: 'CUIDADO: como esto crea un subshell para el while, variables modificadas dentro NO sobreviven. Mejor: redirigir el archivo con `< archivo` después del done.', compat_notes: null },
          { comando: 'IFS=: read -r usuario _ uid _ <<< "alice:x:1001:1001:Alice:/home/alice:/bin/bash"',     explicacion: 'Split por : con IFS local a este read. `<<<` es here-string.',                  compat_notes: null }
        ],
        gotchas: [
          'Sin `-r`, backslashes se interpretan: `\\n` se convierte en `n` literal, `\\\\` en `\\`. Casi siempre quieres `-r`.',
          'Sin `IFS=` (vacío) delante, read recorta espacios y tabs del principio/final de la línea. Si quieres fidelidad, `IFS=` siempre.',
          'En el patrón `cmd | while read ...`, el while corre en un subshell — variables modificadas dentro del while NO se ven fuera. Usar `while read ... done < archivo` (redirección, sin pipe) para evitarlo.',
          'En Git Bash, `read -s` funciona pero a veces no oculta perfectamente si el terminal es raro (mintty suele bien). Probar antes de usar para passwords reales.',
          'read devuelve != 0 cuando alcanza EOF (sin saltos al final). Patrón típico: el bucle while termina solo cuando EOF.'
        ],
        relacionados: ['sx-variables', 'sx-while', 'sx-arrays'],
        cuando_no_usar: 'Para procesar archivos columnares grandes, `awk` es mucho más rápido y conciso que `while read` + split manual. Para parsing CSV/JSON real, `csvkit`/`jq` o un script Python.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. 51 tarjetas. `sx-read` con badge ámbar, "Ver más ▾". Flags visibles. Filtrar Categoría = "Variables" → 2 tarjetas + 11 operadores = 13.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de read con flags y patrón while read"
```

---

### Task 4: Cargar `sx-arrays` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-arrays` después de `sx-read`**

```js
      ,{
        id: 'sx-arrays',
        tipo: 'sintaxis',
        nombre: 'Arrays',
        categoria: 'variables',
        dificultad: 'intermedio',
        descripcion: 'Arrays indexed y asociativos en bash. Acceso a elementos, slicing, longitud, iteración.',
        gitbash: 'ok',
        patron: 'arr=(a b c)    /    arr[i]=x    /    "${arr[@]}"    /    "${#arr[@]}"',
        tags: ['array', 'lista', 'indexed', 'associative'],
        modelo_mental: 'Bash 4+ tiene dos tipos de array. Indexed (`declare -a` o asignación con paréntesis): índices son enteros desde 0. Associative (`declare -A`): índices son strings (mapa/diccionario). Acceso a un elemento: `${arr[i]}`. TODOS los elementos: `"${arr[@]}"` (palabras separadas, con comillas) o `"${arr[*]}"` (una sola palabra unida). Length: `${#arr[@]}`. La diferencia `@` vs `*` ENTRE COMILLAS es el gotcha clásico.',
        ejemplos: [
          { comando: 'arr=(uno dos tres)',                  explicacion: 'Declara array indexed con 3 elementos.',                                                       compat_notes: null },
          { comando: 'echo "${arr[1]}"',                    explicacion: 'Acceso por índice (base 0). Resultado: "dos".',                                                compat_notes: null },
          { comando: 'echo "${arr[@]}"',                    explicacion: 'Todos los elementos. Entre comillas: cada uno es una palabra separada (preserva espacios).',  compat_notes: null },
          { comando: 'echo "${#arr[@]}"',                   explicacion: 'Cantidad de elementos: 3.',                                                                    compat_notes: null },
          { comando: 'arr+=(cuatro)',                       explicacion: 'Append. arr ahora tiene 4 elementos.',                                                          compat_notes: null },
          { comando: 'unset arr[1]',                        explicacion: 'Borra el elemento de índice 1 (deja "hueco" — índices no se renumeran).',                       compat_notes: null },
          { comando: 'for x in "${arr[@]}"; do echo "$x"; done', explicacion: 'Itera correctamente sobre los elementos (incluso con espacios).',                          compat_notes: null },
          { comando: 'declare -A user=([nombre]="Marius" [edad]=42); echo "${user[nombre]}"', explicacion: 'Array asociativo (clave string). Requiere declare -A antes.', compat_notes: null }
        ],
        combos: [
          { comando: 'archivos=( *.txt ); echo "Total: ${#archivos[@]}"',   explicacion: 'Glob expandido en array. Cuidado: si no matchea nada, queda con 1 elemento literal "*.txt".', compat_notes: null },
          { comando: 'IFS=$\'\\n\' read -d \'\' -ra lineas < archivo',       explicacion: 'Lee todo el archivo en un array de líneas. Alternativa moderna: `mapfile -t lineas < archivo`.', compat_notes: null }
        ],
        gotchas: [
          '"${arr[@]}" (con comillas) → cada elemento como palabra separada. "${arr[*]}" (con comillas) → todos unidos por el primer char de IFS. SIN comillas: ambos hacen word splitting. La regla: casi siempre `"${arr[@]}"`.',
          'Los arrays asociativos requieren `declare -A nombre` ANTES de asignar. Si no, bash trata las claves string como aritmética → 0.',
          '`arr=(*.txt)` con glob sin matches: NO queda vacío, queda con un solo elemento literal `*.txt` (a menos que actives `shopt -s nullglob`).',
          '`unset arr[i]` borra el elemento pero NO renumera índices. `${#arr[@]}` da el conteo correcto, pero hay un "hueco" — iterar por índice numérico puede saltarse posiciones.',
          'En Git Bash, los arrays funcionan bien (bash 4+). Cuidado: copy-paste de scripts antiguos con sh puede fallar — los arrays son bash, no POSIX.'
        ],
        relacionados: ['sx-variables', 'sx-for', 'sx-special-vars'],
        cuando_no_usar: 'Para datos complejos (records con muchos campos, anidados), bash arrays se quedan cortos. Considerar Python, jq (JSON), o leer en variables individuales si son pocos.'
      }
```

- [ ] **Step 2: Verificar**

52 tarjetas. Categoría "Variables" → 3 tarjetas + 11 operadores = 14.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de arrays con indexed y asociativos"
```

---

### Task 5: Cargar `sx-if` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-if` después de `sx-arrays`**

```js
      ,{
        id: 'sx-if',
        tipo: 'sintaxis',
        nombre: 'if / elif / else',
        categoria: 'control-flujo',
        dificultad: 'basico',
        descripcion: 'Condicional. Ejecuta un bloque según el exit code de un comando (no solo de `[ ]`).',
        gitbash: 'ok',
        patron: 'if COMANDO; then ...; elif COMANDO; then ...; else ...; fi',
        tags: ['condicional', 'flujo', 'test'],
        modelo_mental: '`if COMANDO; then ...; fi` evalúa el EXIT CODE del comando: 0 = true (ejecuta el then), != 0 = false. COMANDO puede ser CUALQUIER cosa, no solo `[ ]` o `[[ ]]`: `if grep -q patron file; then ...` es idiomático. Esto es lo opuesto al instinto C/Python donde la condición es una expresión booleana. Aquí la condición es "¿salió bien el comando?".',
        ejemplos: [
          { comando: 'if [ "$#" -lt 1 ]; then echo "Faltan args"; exit 1; fi', explicacion: 'Comprueba número de argumentos. `[ -lt ]` es numérico.', compat_notes: null },
          { comando: 'if [[ "$nombre" == "alice" ]]; then echo "hola"; fi',    explicacion: 'Comparación string con `[[ ]]` (recomendado en bash).',     compat_notes: null },
          { comando: 'if grep -q ERROR app.log; then echo "hay errores"; fi',  explicacion: 'Condición = exit code de grep. `-q` silencia output, solo informa éxito/fallo.', compat_notes: null },
          { comando: 'if ! command -v jq >/dev/null; then echo "jq no instalado"; exit 1; fi', explicacion: 'Negación con `!`. Comprueba si un comando NO existe.', compat_notes: null },
          { comando: 'if [ -f config.yml ]; then echo "existe"; elif [ -f config.yaml ]; then echo "existe (yaml)"; else echo "no"; fi', explicacion: 'Cadena if/elif/else con tests de archivo.', compat_notes: null }
        ],
        combos: [
          { comando: '[ -d build ] || mkdir build',                            explicacion: 'Atajo "if no existe, crea" sin if explícito (operador ||).',                  compat_notes: null },
          { comando: 'systemctl is-active nginx >/dev/null && echo "activo" || echo "parado"', explicacion: 'Ternario tipo bash. CUIDADO: si el "true" falla, ejecuta el "false" también.',  compat_notes: null }
        ],
        gotchas: [
          'La condición de `if` NO es una expresión booleana — es un comando. `if $cond` ejecuta `$cond` como comando, no evalúa truthness.',
          'Espacios DENTRO de `[ ]`: `[ "$x" = "y" ]` ✓, `["$x"="y"]` ✗ (eso es un comando inexistente llamado `[$x=$y]`).',
          'Variables sin entrecomillar dentro de `[ ]` rompen si están vacías o tienen espacios. `[ $x = "y" ]` con x="" se convierte en `[ = y ]` → error.',
          '`[[ ]]` (bash) es más seguro que `[ ]` (POSIX): no necesitas comillas en variables, soporta `&&`/`||`, regex con `=~`. Salvo necesidad explícita de POSIX, usar `[[ ]]`.',
          'El patrón `cmd1 && cmd2 || cmd3` NO es un ternario fiable: si `cmd2` falla (exit != 0), también ejecuta `cmd3`. Para ternario real, usar if/else completo.'
        ],
        relacionados: ['sx-tests', 'sx-case', 'op-and', 'op-or'],
        cuando_no_usar: 'Para múltiples ramas con patrones (matching de strings, globs), `case` es más legible que cadenas largas de `if/elif`. Para condiciones que se aplican a muchos valores, considera `for` + `if` o un mapa con array asociativo.'
      }
```

- [ ] **Step 2: Verificar**

53 tarjetas. Filtrar Categoría = "Control de flujo" → 1 + 11 operadores = 12.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de if/elif/else con énfasis en exit code"
```

---

### Task 6: Cargar `sx-for` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-for` después de `sx-if`**

```js
      ,{
        id: 'sx-for',
        tipo: 'sintaxis',
        nombre: 'for',
        categoria: 'control-flujo',
        dificultad: 'basico',
        descripcion: 'Bucle: itera sobre una lista (glob, secuencia, args) o con índice numérico.',
        gitbash: 'ok',
        patron: 'for VAR in LISTA; do ...; done    /    for ((i=0; i<N; i++)); do ...; done',
        tags: ['bucle', 'iteracion', 'loop'],
        modelo_mental: 'Bash tiene 3 formas de `for`. (1) Sobre lista: `for x in a b c` — más usada, también con globs y `$(cmd)`. (2) Estilo C: `for ((i=0; i<10; i++))` — bashism, índice numérico. (3) Sobre secuencia: `for i in {1..10}` o `for i in $(seq 1 10)`. La forma "C-style" usa `(( ))` (aritmética bash), distinta de los `[ ]` de tests.',
        ejemplos: [
          { comando: 'for f in *.txt; do echo "$f"; done',                explicacion: 'Itera sobre archivos .txt del directorio actual.',                                           compat_notes: null },
          { comando: 'for arg in "$@"; do echo "arg: $arg"; done',         explicacion: 'Itera sobre los argumentos del script (con comillas para preservar espacios).',           compat_notes: null },
          { comando: 'for i in {1..5}; do echo "$i"; done',                explicacion: 'Brace expansion: imprime 1 2 3 4 5. Útil para secuencias fijas.',                          compat_notes: null },
          { comando: 'for ((i=0; i<10; i++)); do echo "$i"; done',          explicacion: 'Estilo C. Útil cuando necesitas un índice numérico real (no string).',                    compat_notes: null },
          { comando: 'for usuario in $(cut -d: -f1 /etc/passwd); do echo "$usuario"; done', explicacion: 'Itera sobre líneas de un comando. CUIDADO: rompe con espacios — preferible while read.', compat_notes: null }
        ],
        combos: [
          { comando: 'for f in *.JPG; do mv "$f" "${f%.JPG}.jpg"; done',    explicacion: 'Renombra masivo: el patrón canónico de bash sin instalar `rename`.',                       compat_notes: null },
          { comando: 'shopt -s nullglob; for f in *.log; do gzip "$f"; done; shopt -u nullglob', explicacion: 'Con nullglob, si no hay .log el bucle no se ejecuta (en lugar de iterar sobre el literal "*.log").', compat_notes: null }
        ],
        gotchas: [
          'Sin `shopt -s nullglob`, un glob sin matches expande al literal: `for f in *.txt` con cero archivos itera UNA VEZ con `f="*.txt"`. Activar nullglob o comprobar con `[ -e "$f" ]` dentro.',
          'NO entrecomillar la lista: `for x in "a b c"` itera una sola vez con `x="a b c"`. Sin comillas: 3 iteraciones.',
          '`for x in $(cmd)` hace word-splitting por IFS sobre la salida — rompe con nombres con espacios. Para procesar líneas, `while IFS= read -r line; do ... done < <(cmd)`.',
          'En Git Bash, `{1..10}` y `for ((;;))` funcionan (son bash, no POSIX). Si necesitas portabilidad a `sh`, usar `seq` o un while con contador.',
          'Brace expansion ocurre ANTES de la variable expansion: `for i in {1..$N}` NO funciona (queda literal). Usar `seq` o C-style for con $N.'
        ],
        relacionados: ['sx-while', 'sx-arrays', 'sx-special-vars'],
        cuando_no_usar: 'Para iterar líneas de un archivo o salida de comando, `while IFS= read -r line` es más robusto que `for x in $(cmd)` (maneja espacios y caracteres especiales). Para procesar muchísimos elementos, herramientas como `xargs -P` (paralelo) o awk pueden ser más eficientes.'
      }
```

- [ ] **Step 2: Verificar**

54 tarjetas. Categoría "Control de flujo" → 2 + 11 = 13.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de for con 3 formas (lista, C-style, secuencia)"
```

---

### Task 7: Cargar `sx-while` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-while` después de `sx-for`**

```js
      ,{
        id: 'sx-while',
        tipo: 'sintaxis',
        nombre: 'while',
        categoria: 'control-flujo',
        dificultad: 'basico',
        descripcion: 'Bucle mientras la condición sea verdadera (exit code 0). Patrón canónico para leer archivos línea a línea.',
        gitbash: 'ok',
        patron: 'while COMANDO; do ...; done',
        tags: ['bucle', 'iteracion', 'loop'],
        modelo_mental: 'Como `if`, la "condición" de `while` es el exit code de un comando — no una expresión booleana. Sigue ejecutando el cuerpo mientras COMANDO devuelva 0. Los dos patrones más usados: `while [ condicion ]` (loop con contador o flag) y `while read line; do ... done < archivo` (procesar archivo línea a línea, la forma robusta).',
        ejemplos: [
          { comando: 'i=0; while [ "$i" -lt 5 ]; do echo "$i"; i=$((i+1)); done', explicacion: 'Contador clásico. `$(( ))` es aritmética bash.',                          compat_notes: null },
          { comando: 'while IFS= read -r line; do echo ">> $line"; done < archivo.txt', explicacion: 'Patrón canónico para procesar archivo línea a línea, robusto frente a espacios.', compat_notes: null },
          { comando: 'while ! ping -c1 servidor.com; do echo "esperando..."; sleep 1; done', explicacion: 'Espera activa: itera mientras ping FALLE (con `!` que invierte).',  compat_notes: null },
          { comando: 'while true; do echo "loop"; sleep 5; done',           explicacion: 'Bucle infinito. Salir con Ctrl+C o `break` interno.',                              compat_notes: null }
        ],
        combos: [
          { comando: 'find . -name "*.log" | while IFS= read -r f; do echo "$f"; done', explicacion: 'CUIDADO: el while corre en subshell (por el pipe). Variables modificadas dentro NO sobreviven al done. Para conservarlas, usar process substitution: `while ...; done < <(find ...)`.', compat_notes: null },
          { comando: 'cuenta=0; while read -r l; do ((cuenta++)); done < f; echo "$cuenta"', explicacion: 'Sin pipe (redirección directa con `<`), las variables SÍ sobreviven al done. cuenta vale el número de líneas.', compat_notes: null }
        ],
        gotchas: [
          'Pipeline + while: `cmd | while ...; done` ejecuta el while en SUBSHELL. Variables modificadas dentro se pierden. Soluciones: process substitution `while ...; done < <(cmd)`, o no usar pipe.',
          'Si olvidas incrementar el contador dentro del while, bucle infinito. Ctrl+C para salir.',
          'Sin `IFS=` y `-r`, leyendo línea a línea pierdes espacios al inicio/final y backslashes. Casi siempre quieres ambos.',
          '`break` y `continue` funcionan dentro de while igual que en otros lenguajes. `break N` rompe N niveles de bucles anidados.'
        ],
        relacionados: ['sx-for', 'sx-until', 'sx-read', 'sx-if'],
        cuando_no_usar: 'Para iterar sobre archivos por patrón (glob), `for f in *.txt` es más directo. Para procesar grandes volúmenes columnares, `awk` suele superar a `while read` + split en velocidad y legibilidad.'
      }
```

- [ ] **Step 2: Verificar**

55 tarjetas. Categoría "Control de flujo" → 3 + 11 = 14.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de while con patrón while read y caveat de subshell"
```

---

### Task 8: Cargar `sx-tests` (gorda — comparativa `[ ]` vs `[[ ]]`)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-tests` después de `sx-while`**

```js
      ,{
        id: 'sx-tests',
        tipo: 'sintaxis',
        nombre: '[ ] vs [[ ]] (tests)',
        categoria: 'control-flujo',
        dificultad: 'intermedio',
        descripcion: 'Las dos sintaxis de test en bash: `[ ]` (POSIX, portable) y `[[ ]]` (bashism, más potente y seguro).',
        gitbash: 'ok',
        patron: '[ EXPR ]    o    [[ EXPR ]]',
        tags: ['test', 'condicional', 'comparacion'],
        modelo_mental: '`[ ]` es un comando real (`/usr/bin/[` o builtin) — necesita espacios alrededor de operandos, comillas para variables, y `\\&&` o `-a`/`-o` para combinar (deprecado). `[[ ]]` es palabra reservada del shell — parser especial: NO necesita comillas en variables, acepta `&&`/`||` directos, soporta regex con `=~` y glob matching. **Regla práctica:** usa `[[ ]]` salvo que necesites portabilidad POSIX explícita (scripts `sh`/`dash`).',
        ejemplos: [
          { comando: '[ "$x" = "hola" ]',           explicacion: 'POSIX: igualdad de string. `=` o `==`. Comillas obligatorias en $x.',           compat_notes: null },
          { comando: '[[ $x == "hola" ]]',          explicacion: 'Bash: lo mismo sin comillas en $x (parser bash lo entiende).',                  compat_notes: null },
          { comando: '[[ $email =~ ^[a-z]+@[a-z]+\\.[a-z]+$ ]]', explicacion: 'Bash exclusivo: regex con `=~`. La regex va sin comillas.',           compat_notes: null },
          { comando: '[[ $archivo == *.log ]]',     explicacion: 'Bash: glob matching dentro del test (no funciona con `[ ]`).',                  compat_notes: null },
          { comando: '[ -f archivo.txt ]',          explicacion: 'Test de archivo: -f existe Y es regular. -d dir, -L symlink, -e existe (cualquiera).', compat_notes: null },
          { comando: '[ "$n" -gt 10 ]',              explicacion: 'Comparación NUMÉRICA en POSIX: -eq, -ne, -lt, -le, -gt, -ge (NO `>` ni `<` aquí).', compat_notes: null },
          { comando: '[[ $n -gt 10 && $n -lt 100 ]]', explicacion: 'Bash: combinar con `&&` directo dentro del [[ ]].',                              compat_notes: null }
        ],
        flags: [
          { flag: '-eq / -ne / -lt / -le / -gt / -ge', descripcion: 'Comparación numérica (== != < <= > >=). Funciona en [ ] y [[ ]].',                compat_notes: null },
          { flag: '= / == / !=',                       descripcion: 'Comparación de string. En [ ] usar `=`. En [[ ]] `==` y `=` ambos válidos.',     compat_notes: null },
          { flag: '-f / -d / -e / -L / -r / -w / -x',  descripcion: 'Tests de archivo: -f regular, -d directorio, -e existe (cualquier tipo), -L symlink, -r/-w/-x permisos.', compat_notes: null },
          { flag: '-z / -n',                            descripcion: '-z STRING: cierto si string vacío. -n STRING: cierto si NO vacío.',              compat_notes: null },
          { flag: '=~',                                 descripcion: 'BASH ONLY (solo en [[ ]]): match con regex extendida (ERE).',                    compat_notes: null }
        ],
        combos: [
          { comando: '[[ -f "$f" && -r "$f" ]] && echo "legible"', explicacion: 'Combinar tests con `&&` directo dentro de [[ ]].',                           compat_notes: null },
          { comando: 'if [[ -z "$VAR" ]]; then echo "vacía o no definida"; fi', explicacion: 'Comprobar variable vacía O no definida.',                       compat_notes: null }
        ],
        gotchas: [
          'En `[ ]`: SIEMPRE entrecomillar variables. `[ $x = y ]` con x="" se convierte en `[ = y ]` → error de sintaxis. `[ "$x" = "y" ]` no rompe.',
          'En `[ ]`: usar `-eq` para números, `=` para strings. `[ "1" -eq "01" ]` → true (numéricos). `[ "1" = "01" ]` → false (strings).',
          'En `[ ]`: `<` y `>` NO son comparación numérica, son redirección de archivos. Usar `-lt`/`-gt`.',
          'En `[[ ]]`: `==` con globs hace matching, no igualdad: `[[ "abc" == a* ]]` → true. Para igualdad estricta de string, entrecomillar el patrón: `[[ "abc" == "a*" ]]` → false.',
          'En `[[ ]]`: regex `=~` debe ir SIN comillas en el patrón. `[[ "$s" =~ "^[0-9]+$" ]]` (con comillas) interpreta el patrón como literal — busca el texto `^[0-9]+$` dentro de $s.',
          '`[[ ]]` NO es POSIX. Scripts con `#!/bin/sh` que usen `[[ ]]` fallarán en sistemas donde sh = dash (Debian/Ubuntu).'
        ],
        relacionados: ['sx-if', 'sx-while'],
        cuando_no_usar: 'Para condiciones aritméticas puras (sin tests de archivo ni strings), `(( ))` es más limpio: `if (( n > 10 )); then ...`. Para matching complejo o múltiples ramas, `case` puede ser más legible que cadenas de `[[ ... ]]`.'
      }
```

- [ ] **Step 2: Verificar**

56 tarjetas. Tarjeta `sx-tests` con badge ámbar, "Ver más" expandible. Categoría "Control de flujo" → 4 + 11 = 15.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda comparativa de [ ] vs [[ ]]"
```

---

### Task 9: Cargar `sx-functions` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-functions` después de `sx-tests`**

```js
      ,{
        id: 'sx-functions',
        tipo: 'sintaxis',
        nombre: 'Funciones',
        categoria: 'funciones-y-scripting',
        dificultad: 'intermedio',
        descripcion: 'Definir y usar funciones bash. Argumentos, return, scope con local.',
        gitbash: 'ok',
        patron: 'nombre() { ... }    o    function nombre { ... }',
        tags: ['funcion', 'function', 'return'],
        modelo_mental: 'Una función es un bloque de comandos con nombre. Sus argumentos se acceden como `$1`, `$2`, `$@`, `$#` — exactamente igual que los argumentos de un script. `return N` solo define el EXIT CODE (0-255), no "devuelve" un valor. Para "devolver" texto/datos, hay que `echo`-arlos y capturar con `$(funcion)`. Variables creadas dentro son GLOBALES por defecto — usar `local var=...` para scoping.',
        ejemplos: [
          { comando: 'saludar() { echo "Hola, $1"; }; saludar "Marius"', explicacion: 'Definición + llamada. $1 es el primer argumento de la función.', compat_notes: null },
          { comando: 'cuadrado() { echo $(( $1 * $1 )); }; r=$(cuadrado 5); echo "$r"', explicacion: 'Devolver un valor: echo + $(cuadrado ...). Resultado: 25.', compat_notes: null },
          { comando: 'es_par() { (( $1 % 2 == 0 )); }; if es_par 4; then echo "sí"; fi', explicacion: 'Devolver éxito/fallo: el último exit code del cuerpo es el de la función. `(( ))` da 0 si la expresión es verdadera.', compat_notes: null },
          { comando: 'incrementar() { local i=$1; i=$((i+1)); echo "$i"; }; incrementar 10', explicacion: '`local` evita modificar variables del scope exterior. Output: 11.', compat_notes: null },
          { comando: 'log() { echo "[$(date +%H:%M:%S)] $*"; }; log "evento importante"', explicacion: '$* (o $@) accede a TODOS los argumentos. Útil para wrappers genéricos.', compat_notes: null }
        ],
        combos: [
          { comando: 'falla_o_sigue() { "$@" || { echo "Error en: $*"; exit 1; }; }; falla_o_sigue cp src dst', explicacion: 'Función wrapper que ejecuta lo que le pases y aborta con mensaje si falla.', compat_notes: null },
          { comando: 'usage() { cat <<EOF\nUso: $0 [-h] ARCHIVO\nEOF\nexit 1; }', explicacion: 'Función + here-doc para mostrar ayuda.', compat_notes: null }
        ],
        gotchas: [
          '`return N` define EXIT CODE (0-255). NO es como en otros lenguajes — no puedes "return $variable" para pasar un string. Para eso, `echo "$valor"` + capturar con $(funcion).',
          'Variables sin `local` son GLOBALES. Una función que hace `i=0` machaca cualquier `$i` del scope que la llama.',
          'Sin redefinir `$@` al entrar a una función, los `$1`, `$2`... DENTRO de la función son los ARGUMENTOS DE LA FUNCIÓN, no los del script. Para acceder a los del script desde dentro, pasarlos explícitamente.',
          'Funciones no exportan al entorno como variables: una función definida en un script NO es visible en un subshell salvo `export -f nombre`.',
          'El nombre `function nombre` (con keyword) es bashism. La forma `nombre() { }` es portable POSIX. Prefiere la segunda salvo necesidad explícita.'
        ],
        relacionados: ['sx-special-vars', 'sx-if'],
        cuando_no_usar: 'Para lógica compleja con estructuras de datos, mejor pasar a Python. Las funciones bash son útiles para encapsular comandos repetitivos, no para programar lógica de aplicación.'
      }
```

- [ ] **Step 2: Verificar**

57 tarjetas. Categoría "Funciones y scripting" → 1 + 11 = 12.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de funciones con return, local y patrones"
```

---

### Task 10: Cargar `sx-special-vars` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-special-vars` después de `sx-functions`**

```js
      ,{
        id: 'sx-special-vars',
        tipo: 'sintaxis',
        nombre: 'Variables especiales ($@, $#, $?, ...)',
        categoria: 'funciones-y-scripting',
        dificultad: 'intermedio',
        descripcion: 'Variables predefinidas en cualquier script: parámetros posicionales, exit code, PIDs, nombre del script.',
        gitbash: 'ok',
        patron: '$0  $1..$9  ${10}+  $@  $*  $#  $?  $$  $!',
        tags: ['posicionales', 'argumentos', 'exit-code'],
        modelo_mental: 'Bash define variables especiales automáticamente. **Posicionales:** `$1..$9` argumentos individuales, `${10}+` requiere llaves; `$@` todos como palabras separadas (con comillas dobles); `$*` todos como una sola palabra; `$#` cantidad; `$0` nombre del script. **Exit / proceso:** `$?` exit code del último comando (capturar inmediatamente o se pierde); `$$` PID del shell actual; `$!` PID del último comando en background. La trampa clásica es `$@` vs `$*` con/sin comillas.',
        ejemplos: [
          { comando: 'echo "Script: $0"',                  explicacion: '$0 es el nombre con el que se invocó el script (o "bash" si estás en shell interactivo).', compat_notes: null },
          { comando: 'echo "Argumentos: $#"',              explicacion: '$# = número de argumentos pasados.',                                                       compat_notes: null },
          { comando: 'echo "Primero: $1, segundo: $2"',     explicacion: 'Argumentos individuales (base 1, no 0).',                                                  compat_notes: null },
          { comando: 'for arg in "$@"; do echo "$arg"; done', explicacion: '"$@" expandido entre comillas = cada argumento como palabra separada (preserva espacios).', compat_notes: null },
          { comando: 'cmd_que_falla; echo "salió con $?"',  explicacion: '$? = exit code del último comando. Capturar INMEDIATAMENTE — cualquier comando posterior lo machaca.', compat_notes: null },
          { comando: 'sleep 100 &; echo "PID background: $!"; echo "Mi PID: $$"', explicacion: '$! = PID del último &. $$ = PID de esta shell.', compat_notes: null },
          { comando: 'echo "${10}"',                        explicacion: 'Argumentos 10+: requieren llaves obligatorias. `$10` se interpreta como `$1` + "0".',     compat_notes: null }
        ],
        combos: [
          { comando: 'set -- a "b c" d; echo "$@" | tr " " "\\n"; echo "---"; echo "$*"', explicacion: '`set -- ...` re-establece $1, $2... Vista contrastada: $@ preserva las palabras, $* las junta.', compat_notes: null },
          { comando: 'cmd; rc=$?; if [ "$rc" -ne 0 ]; then echo "falló con $rc"; fi', explicacion: 'Guardar $? inmediatamente en una variable propia para usarlo después de varios comandos.', compat_notes: null }
        ],
        gotchas: [
          'La trampa clásica: `"$@"` con comillas dobles = cada argumento como palabra separada (correcto casi siempre). Sin comillas o con `$*`, los argumentos con espacios se rompen.',
          '`$10`, `$11`... NO funcionan así. Bash los interpreta como `$1` seguido de "0". Usar `${10}`, `${11}`.',
          '`$?` solo es válido inmediatamente. `cmd_que_falla; echo "log"; if [ $? -ne 0 ]` — el $? aquí es de `echo`, no de cmd_que_falla. Capturar con `rc=$?` justo después.',
          '`$0` en una función NO es el nombre de la función — es el nombre del script. No hay variable estándar para "nombre de la función actual" en bash (algunos usan `${FUNCNAME[0]}`).',
          '`shift` mueve los posicionales: `$2` se convierte en `$1`, etc. Útil para procesar args en bucle. `shift 2` desplaza 2 posiciones.',
          'En Git Bash, `$$` es el PID del proceso MSYS, no del proceso Windows visible en Task Manager. Para scripts internos da igual.'
        ],
        relacionados: ['sx-functions', 'sx-shebang'],
        cuando_no_usar: 'Para parsing avanzado de flags (`-h --help`, `--port=8080`), `getopts` (builtin) o `getopt` (binario externo) son más limpios que parsear `$@` a mano. Para argumentos opcionales complejos, considerar python/argparse.'
      }
```

- [ ] **Step 2: Verificar**

58 tarjetas. Categoría "Funciones y scripting" → 2 + 11 = 13.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de variables especiales con énfasis en \$@ vs \$*"
```

---

### Task 11: Cargar `sx-heredoc` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar `sx-heredoc` después de `sx-special-vars`**

```js
      ,{
        id: 'sx-heredoc',
        tipo: 'sintaxis',
        nombre: 'Here-doc y here-string (<<EOF / <<<)',
        categoria: 'funciones-y-scripting',
        dificultad: 'intermedio',
        descripcion: 'Alimentar stdin con texto multilínea (<<EOF) o un string (<<<).',
        gitbash: 'ok',
        patron: 'cmd <<DELIM\\n...texto...\\nDELIM    /    cmd <<<"string"',
        tags: ['heredoc', 'herestring', 'stdin', 'multilinea'],
        modelo_mental: 'Un here-doc inyecta texto multilínea como stdin de un comando. `<<EOF` con variables expandidas dentro; `<<\'EOF\'` (delimitador entre comillas simples) hace el contenido LITERAL (sin expansion). `<<-EOF` permite indentar el contenido con TABS (los strip al pasar al comando — útil para que el código quede formateado). `<<<` es here-string: una sola línea como stdin, sin necesidad de delimitador.',
        ejemplos: [
          { comando: 'cat <<EOF\nHola $USER\nFecha: $(date +%Y-%m-%d)\nEOF', explicacion: 'Here-doc con expansión: $USER y $(date) se sustituyen. El delimitador EOF debe ir solo en su línea, sin indentar.', compat_notes: null },
          { comando: 'cat <<\'EOF\'\nHola $USER literal\nEOF',           explicacion: 'Delimitador entre comillas simples: el contenido es LITERAL, $USER no se expande.',                                  compat_notes: null },
          { comando: 'cat <<-EOF\n\tHola\n\tMundo\n\tEOF',                explicacion: '<<-EOF strip de tabs iniciales. PERMITE indentar el bloque dentro de un if/function con tabs.',                       compat_notes: null },
          { comando: 'tr a-z A-Z <<< "hola mundo"',                       explicacion: 'Here-string: una línea como stdin. Resultado: HOLA MUNDO. Más limpio que `echo "hola mundo" | tr a-z A-Z`.',     compat_notes: null },
          { comando: 'mysql -u root db <<EOF\nSELECT * FROM users;\nEOF',  explicacion: 'Patrón típico: alimentar SQL a mysql.',                                                                              compat_notes: null }
        ],
        combos: [
          { comando: 'ssh remoto <<EOF\ncd /var/log\ntail -n 50 app.log\nEOF', explicacion: 'Ejecutar varias órdenes en remoto vía here-doc.',                                                                  compat_notes: null },
          { comando: 'cat > config.yml <<EOF\nhost: $HOST\nport: $PORT\nEOF',   explicacion: 'Generar archivo de config con variables expandidas. Combina redirección > con here-doc.',                          compat_notes: null }
        ],
        gotchas: [
          'El delimitador (`EOF` o el que elijas) debe ir SOLO en su línea, sin espacios delante ni detrás. Si lo indentas con espacios, no termina el here-doc y bash espera hasta EOF real → cuelga.',
          '`<<-EOF` strip SOLO TABS, no espacios. Si tu editor convierte tabs a espacios, no funciona. Para indentar con espacios, no hay opción nativa.',
          'Comillas en el delimitador (`<<\'EOF\'` o `<<"EOF"`) desactivan TODA expansion: variables, command substitution, y backslash. Útil para snippets de código que tengan `$` literales (regex, SQL).',
          'Backslash-newline dentro de un here-doc SÍ se interpreta (continuación de línea) salvo que el delimitador esté entre comillas.',
          'En Git Bash, here-docs funcionan idéntico a Linux. Cuidado al copiar/pegar desde Windows: los finales de línea CRLF pueden romper.',
          'Variables del here-doc se expanden en el shell que llama, no en el comando destino. `ssh remoto <<EOF\\necho $USER\\nEOF` envía el $USER LOCAL al remoto. Para evaluar en remoto, usar `<<\'EOF\'`.'
        ],
        relacionados: ['op-redirect-stdin', 'op-cmd-substitution', 'sx-variables'],
        cuando_no_usar: 'Para archivos plantilla largos con muchas variables, una herramienta como `envsubst` o `m4`/`jinja2` da más control. Para inyectar JSON, `jq -n` permite construirlo sin escapado manual de comillas.'
      }
```

- [ ] **Step 2: Verificar**

59 tarjetas. Categoría "Funciones y scripting" → 3 + 11 = 14.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de here-doc y here-string (<<EOF, <<-EOF, <<<)"
```

---

### Task 12: Cargar 5 tarjetas básicas (export, case, until, alias, shebang)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar las 5 básicas después de `sx-heredoc`**

```js
      ,{
        id: 'sx-export',
        tipo: 'sintaxis', nombre: 'export', categoria: 'variables', dificultad: 'basico',
        descripcion: 'Marca una variable para que sea heredada por los procesos hijos (entorno).',
        gitbash: 'ok',
        patron: 'export NOMBRE=valor    o    export NOMBRE (si ya existe)',
        ejemplos: [
          { comando: 'export PATH="$PATH:/opt/bin"',         explicacion: 'Añade /opt/bin al PATH para esta sesión y todo proceso hijo.',                          compat_notes: null },
          { comando: 'API_KEY=abc123 ./script.sh',           explicacion: 'Atajo: define export TEMPORAL solo para esa invocación (no afecta al shell actual).', compat_notes: null },
          { comando: 'export -p',                            explicacion: 'Lista todas las variables exportadas en el entorno actual.',                            compat_notes: null }
        ],
        gotchas: [
          'Variables sin export son LOCALES al shell — un script hijo NO las ve. Esto es la causa típica de "mi variable funciona en bash pero no en el script".',
          'export afecta SOLO a procesos hijos lanzados DESPUÉS. Procesos ya en marcha conservan su entorno.',
          'Cambios con export NO persisten entre sesiones — para eso, ponerlo en `~/.bashrc` o `~/.profile`.'
        ]
      }
      ,{
        id: 'sx-case',
        tipo: 'sintaxis', nombre: 'case', categoria: 'control-flujo', dificultad: 'basico',
        descripcion: 'Switch/case sobre el valor de una variable usando patrones glob.',
        gitbash: 'ok',
        patron: 'case PALABRA in\\n  patron1) ... ;;\\n  patron2) ... ;;\\n  *) ... ;;\\nesac',
        ejemplos: [
          { comando: 'case "$1" in\n  start) echo "iniciando"; ;;\n  stop)  echo "parando"; ;;\n  *)     echo "uso: $0 {start|stop}"; exit 1; ;;\nesac', explicacion: 'Despacho por subcomando, patrón clásico.', compat_notes: null },
          { comando: 'case "$archivo" in\n  *.tar.gz) tar xzf "$archivo"; ;;\n  *.zip)    unzip "$archivo"; ;;\n  *)        echo "formato desconocido"; ;;\nesac', explicacion: 'Match por extensión usando globs.', compat_notes: null }
        ],
        gotchas: [
          'El terminador `;;` corta la ejecución (como `break` en switch C). `;&` (fall-through al siguiente bloque). `;;&` (sigue evaluando los siguientes patrones para ver si también matchean). El último patrón `*)` actúa como `default`.',
          'Los patrones son GLOB, no regex: `*` cualquier cosa, `?` un carácter, `[abc]` clase. Para regex usar `[[ =~ ]]`.',
          'Sin un patrón final `*)`, si nada matchea, case no hace nada (sin error). Suele querer un default explícito.'
        ]
      }
      ,{
        id: 'sx-until',
        tipo: 'sintaxis', nombre: 'until', categoria: 'control-flujo', dificultad: 'basico',
        descripcion: 'Bucle inverso de while: ejecuta hasta que la condición sea verdadera.',
        gitbash: 'ok',
        patron: 'until COMANDO; do ...; done',
        ejemplos: [
          { comando: 'until ping -c1 servidor.com >/dev/null 2>&1; do\n  echo "esperando..."\n  sleep 2\ndone', explicacion: 'Espera activa: itera mientras ping FALLE; sale cuando ping tenga éxito.', compat_notes: null },
          { comando: 'i=0; until [ "$i" -ge 5 ]; do echo "$i"; i=$((i+1)); done',          explicacion: 'Contador inverso: imprime 0..4 (sale al llegar a 5).',                            compat_notes: null }
        ],
        gotchas: [
          '`until COND` es equivalente a `while ! COND`. Muchos bashers prefieren `while !` por consistencia mental — `until` es menos común en código real.'
        ]
      }
      ,{
        id: 'sx-alias',
        tipo: 'sintaxis', nombre: 'alias', categoria: 'funciones-y-scripting', dificultad: 'basico',
        descripcion: 'Atajo de un comando bajo otro nombre. Se expande al inicio de la línea de comando.',
        gitbash: 'ok',
        patron: 'alias nombre="comando args"    /    unalias nombre    /    alias -p',
        ejemplos: [
          { comando: 'alias ll="ls -la"',           explicacion: 'Crea atajo `ll`. Se expande a `ls -la` al inicio de comando.',                compat_notes: null },
          { comando: 'alias gst="git status"',       explicacion: 'Atajo típico para git.',                                                      compat_notes: null },
          { comando: 'alias -p',                     explicacion: 'Lista todos los alias definidos en la sesión actual.',                       compat_notes: null },
          { comando: 'unalias ll',                   explicacion: 'Elimina un alias.',                                                            compat_notes: null }
        ],
        flags: [
          { flag: '-p', descripcion: 'Lista todos los alias en formato re-importable.', compat_notes: null }
        ],
        gotchas: [
          'Los alias NO se heredan en scripts ni subshells. Si necesitas funcionalidad portable, usa una función en su lugar.',
          'Solo se expanden al INICIO de un comando: `sudo ll` no usa el alias `ll` (a menos que crees `alias sudo="sudo "` con espacio final, truco clásico).',
          'Para que un alias persista entre sesiones, definirlo en `~/.bashrc` o `~/.bash_aliases`.',
          'En scripts, los alias están deshabilitados por defecto. Para activarlos: `shopt -s expand_aliases` (rara vez se quiere — usar funciones).'
        ]
      }
      ,{
        id: 'sx-shebang',
        tipo: 'sintaxis', nombre: 'Shebang (#!)', categoria: 'funciones-y-scripting', dificultad: 'basico',
        descripcion: 'Primera línea de un script que indica qué intérprete ejecutarlo.',
        gitbash: 'ok',
        patron: '#!/usr/bin/env bash    o    #!/bin/bash',
        ejemplos: [
          { comando: '#!/usr/bin/env bash',          explicacion: 'Recomendado: busca `bash` en $PATH. Más portable (algunos sistemas tienen bash en /usr/local/bin).', compat_notes: null },
          { comando: '#!/bin/bash',                  explicacion: 'Ruta fija al binario. Funciona en Linux estándar pero puede fallar en *BSD o macOS con bash homebrew.', compat_notes: null },
          { comando: '#!/usr/bin/env python3',       explicacion: 'Mismo patrón sirve para scripts python, ruby, etc.',                                                  compat_notes: null }
        ],
        gotchas: [
          '`#!/usr/bin/env bash` es preferible a `#!/bin/bash` por portabilidad: usa el bash del PATH (puede ser uno actualizado vía homebrew, etc.).',
          'El shebang debe estar EXACTAMENTE en la primera línea. Si hay un BOM o línea en blanco antes, el SO no lo reconoce.',
          'Sin shebang, el script se ejecuta con el shell que lo invocó (sh, dash...). No portable.',
          'En Git Bash el shebang sirve para `./script.sh` (necesita +x). Desde `cmd.exe`/PowerShell, hay que invocarlo explícitamente: `bash script.sh` o asociar .sh al intérprete.',
          'El intérprete recibe MÁXIMO un argumento adicional en la línea del shebang en Linux: `#!/usr/bin/env -S bash -e` requiere `env -S` (no portable a todos los sistemas).'
        ]
      }
```

- [ ] **Step 2: Verificar**

64 tarjetas (59 + 5). Filtrar Categoría = "Variables" → 4 + 11 = 15. "Control de flujo" → 6 + 11 = 17. "Funciones y scripting" → 5 + 11 = 16.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar 5 tarjetas básicas (export, case, until, alias, shebang)"
```

---

### Task 13: Añadir gotcha de backticks a `op-cmd-substitution`

**Files:**
- Modify: `guia-bash/guia-bash.html` (gotchas de la tarjeta existente `op-cmd-substitution` de iter 1)

- [ ] **Step 1: Localizar el array `gotchas` de `op-cmd-substitution`**

El bloque actual (de iter 1) es:

```js
        gotchas: [
          'Si la salida tiene espacios o saltos, puede romper el comando exterior — comillas dobles alrededor cuando lo usas como argumento individual.',
          'Para listas de archivos producidas por find, prefiere `find ... -exec` o `find ... | xargs` para manejar nombres con espacios.'
        ]
```

Reemplazar por:

```js
        gotchas: [
          'Si la salida tiene espacios o saltos, puede romper el comando exterior — comillas dobles alrededor cuando lo usas como argumento individual.',
          'Para listas de archivos producidas por find, prefiere `find ... -exec` o `find ... | xargs` para manejar nombres con espacios.',
          'Sintaxis vieja con backticks: `` `cmd` `` (acentos graves). Funciona pero NO se anida bien y los escapes son confusos. Para código nuevo, usar `$(cmd)` siempre. Si encuentras backticks en código existente y necesitas modificarlo, conviértelos.'
        ]
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** la tarjeta del operador `$( )` ahora muestra 3 gotchas (el nuevo es el de backticks). El conteo total sigue siendo 64 (esta task no añade tarjetas).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Añadir gotcha sobre backticks deprecados en op-cmd-substitution"
```

---

### Task 14: Cargar 5 recetas

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar las 5 recetas después de `sx-shebang` (antes del `];`)**

```js
      ,{
        id: 'receta-leer-archivo-linea',
        tipo: 'receta', nombre: 'Leer un archivo línea a línea (robusto)', categoria: 'variables', dificultad: 'intermedio',
        descripcion: 'Patrón canónico para procesar un archivo línea a línea preservando espacios, tabs y backslashes.',
        gitbash: 'ok',
        escenario: 'Tengo un archivo de configuración o de logs y quiero procesar cada línea en un script bash, sin que se rompa con espacios o caracteres especiales.',
        ejemplos: [
          { comando: 'while IFS= read -r line; do\n  echo ">> $line"\ndone < archivo.txt', explicacion: 'Lee archivo.txt línea a línea. Funciona aunque haya líneas con espacios iniciales, tabs, o backslashes.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'while ... do ... done',  hace: 'bucle que se ejecuta mientras la condición sea verdadera.' },
          { fragmento: 'IFS=',                    hace: 'IFS vacío SOLO para este read: no recorta espacios/tabs al principio/final de la línea.' },
          { fragmento: 'read -r',                 hace: 'no interpretar backslash (preserva `\\n`, `\\t` literales como dos caracteres).' },
          { fragmento: 'line',                    hace: 'variable que recibe cada línea leída.' },
          { fragmento: '< archivo.txt',           hace: 'redirección de entrada al while ENTERO. Importante: no usar pipe (`cat f | while...`) porque el while iría a subshell.' }
        ],
        relacionados: ['sx-read', 'sx-while'],
        gotchas: [
          'Alternativa moderna en bash 4+: `mapfile -t lineas < archivo` carga todas las líneas en un array. Útil si quieres iterar varias veces o conocer el total previamente.',
          'Si el archivo no termina en \\n, la última línea NO se procesa por defecto. Variante: `while IFS= read -r line || [ -n "$line" ]; do ...` procesa también la línea final sin \\n.'
        ]
      }
      ,{
        id: 'receta-iterar-archivos',
        tipo: 'receta', nombre: 'Iterar sobre archivos por patrón', categoria: 'control-flujo', dificultad: 'basico',
        descripcion: 'Procesar cada archivo de un patrón glob de forma segura ante globs vacíos y nombres con espacios.',
        gitbash: 'ok',
        escenario: 'Quiero hacer algo (convertir, renombrar, comprimir) con todos los archivos de un patrón en un directorio.',
        ejemplos: [
          { comando: 'shopt -s nullglob\nfor f in *.log; do\n  gzip "$f"\ndone\nshopt -u nullglob', explicacion: 'Itera sobre .log comprimiéndolos. shopt nullglob evita el caso "no hay archivos → itera con el literal *.log".', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'shopt -s nullglob',  hace: 'activa el modo en que globs sin matches expanden a NADA (en vez de al literal).' },
          { fragmento: 'for f in *.log',     hace: 'itera sobre cada archivo que matchee *.log en el directorio actual.' },
          { fragmento: 'gzip "$f"',          hace: 'comprime cada archivo. Comillas dobles preservan nombres con espacios.' },
          { fragmento: 'shopt -u nullglob',  hace: 'desactiva nullglob al terminar (cortesía para no cambiar el shell del usuario si el script se sourcea).' }
        ],
        relacionados: ['sx-for', 'sx-variables']
      }
      ,{
        id: 'receta-validar-args',
        tipo: 'receta', nombre: 'Validar argumentos de un script', categoria: 'funciones-y-scripting', dificultad: 'basico',
        descripcion: 'Comprueba que el script recibe el número de argumentos esperado y aborta con mensaje de uso si no.',
        gitbash: 'ok',
        escenario: 'Estoy escribiendo un script que necesita 2 argumentos (origen y destino) y quiero abortar con un mensaje claro si el usuario los olvida.',
        ejemplos: [
          { comando: '#!/usr/bin/env bash\nif [ "$#" -lt 2 ]; then\n  echo "Uso: $0 ORIGEN DESTINO" >&2\n  exit 1\nfi\norigen="$1"\ndestino="$2"\necho "Copiando $origen a $destino"', explicacion: 'Script tipo wrapper de cp con validación de argumentos.', compat_notes: null }
        ],
        desglose: [
          { fragmento: '#!/usr/bin/env bash', hace: 'shebang: indica que ejecute con bash del PATH.' },
          { fragmento: '"$#" -lt 2',          hace: 'comprueba si el número de argumentos es MENOR que 2.' },
          { fragmento: '"$0"',                hace: 'nombre con el que se invocó el script (útil para el mensaje de uso).' },
          { fragmento: '>&2',                 hace: 'redirige el echo a stderr (el flujo correcto para mensajes de error).' },
          { fragmento: 'exit 1',              hace: 'sale con código de error 1 (cualquier != 0 indica fallo).' }
        ],
        relacionados: ['sx-if', 'sx-special-vars', 'sx-shebang', 'sx-tests'],
        gotchas: [
          'Mejor con `[[ $# -lt 2 ]]` (bash) o `(( $# < 2 ))` (aritmético). El ejemplo usa `[ ]` por compatibilidad POSIX.',
          'Para parsing avanzado de flags (`-h`, `--option=value`), usar `getopts` (builtin POSIX) o `getopt` (GNU). Fuera de iter 3.'
        ]
      }
      ,{
        id: 'receta-funcion-return',
        tipo: 'receta', nombre: 'Función que devuelve éxito o fallo', categoria: 'funciones-y-scripting', dificultad: 'intermedio',
        descripcion: 'Función con return numérico usada como condición en un if.',
        gitbash: 'ok',
        escenario: 'Quiero encapsular una comprobación (¿es un email válido? ¿existe el servicio?) en una función y usarla naturalmente en `if`.',
        ejemplos: [
          { comando: 'es_email() {\n  [[ "$1" =~ ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$ ]]\n}\n\nif es_email "$entrada"; then\n  echo "ok"\nelse\n  echo "email inválido"\nfi', explicacion: 'La función devuelve el exit code de [[ ]] — 0 si matchea, 1 si no. El if lo usa directamente.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'es_email() { ... }',     hace: 'define una función llamada es_email.' },
          { fragmento: '[[ "$1" =~ regex ]]',    hace: 'el `[[ ]]` da exit 0 si la regex matchea, 1 si no. ESE es el "return" implícito de la función.' },
          { fragmento: 'if es_email "$entrada"', hace: 'usa la función como condición. Como cualquier comando: exit 0 = then, exit !=0 = else.' }
        ],
        relacionados: ['sx-functions', 'sx-if', 'sx-tests'],
        gotchas: [
          'El exit code de una función = exit code del ÚLTIMO comando del cuerpo. Si quieres explicitarlo, usa `return 0` o `return 1` al final.',
          'NO confundir con `echo` para devolver valor: `echo` imprime a stdout (capturable con $(funcion)). `return` solo define exit code.'
        ]
      }
      ,{
        id: 'receta-heredoc-config',
        tipo: 'receta', nombre: 'Generar archivo de configuración con variables', categoria: 'funciones-y-scripting', dificultad: 'intermedio',
        descripcion: 'Crear un archivo de configuración a partir de un here-doc con variables del script expandidas.',
        gitbash: 'ok',
        escenario: 'Mi script de despliegue necesita generar `nginx.conf` (o cualquier config) con valores variables (puerto, hostname, paths) que se conocen en runtime.',
        ejemplos: [
          { comando: 'HOST="example.com"\nPORT=8080\n\ncat > /tmp/nginx.conf <<EOF\nserver {\n  listen $PORT;\n  server_name $HOST;\n  root /var/www/$HOST;\n}\nEOF', explicacion: 'Genera /tmp/nginx.conf con $HOST y $PORT sustituidos por sus valores.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'HOST="..."; PORT=...',  hace: 'define las variables que se inyectarán.' },
          { fragmento: 'cat > /tmp/nginx.conf', hace: 'redirige stdout de cat al archivo destino (sobrescribe).' },
          { fragmento: '<<EOF',                 hace: 'inicio del here-doc. SIN comillas en EOF → expansion activada.' },
          { fragmento: '...contenido con $HOST y $PORT...', hace: 'plantilla. Las variables se sustituyen al expandir.' },
          { fragmento: 'EOF',                   hace: 'fin del here-doc. Debe ir SOLA en su línea, sin espacios delante.' }
        ],
        relacionados: ['sx-heredoc', 'sx-variables', 'op-redirect-stdout'],
        gotchas: [
          'Si el contenido tiene `$` literales (regex, comandos shell sin expandir), usar `<<\'EOF\'` (comillas) para desactivar expansion.',
          'Para plantillas grandes con lógica, considera `envsubst` (parte de gettext): `envsubst < plantilla.tmpl > config.conf` sustituye SOLO las variables del entorno y deja el resto literal — más limpio que mezclar bash y contenido.'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 69 tarjetas en total (64 + 5). Filtrar Tipo = "Receta" → 16 tarjetas (11 de iter 1+2 + 5 nuevas). Sin filtros → 69 tarjetas (total final previsto en `spec-iter3.md` §8: 27 cmd + 11 op + 16 recetas + 15 sintaxis = 69).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar 5 recetas de scripting (lectura, iteración, validación, función, heredoc-config)"
```

---

### Task 15: QA pass + checklist de aceptación de iter 3

**Files:**
- Manual: validación visual completa.
- Modify: `guia-bash/guia-bash.html` solo si surge algún fallo (en cuyo caso, commit final del ajuste).

- [ ] **Step 1: Recargar y validar el banner de tests**

Banner verde "✓ N tests OK" sigue saliendo (el conteo no cambia: 12 al menos). Sin errores en la consola (F12 → Console).

- [ ] **Step 2: Validar el conteo total**

Sin filtros aplicados, el grid tiene **69 tarjetas**:
- 27 comandos (sin cambios desde iter 2)
- 11 operadores (sin cambios)
- 16 recetas (11 anteriores + 5 nuevas)
- 15 sintaxis (15 nuevas: 4 variables + 6 control-flujo + 5 funciones-y-scripting)

- [ ] **Step 3: Casos de uso de filtros — verificación manual**

| Acción | Conteo esperado |
|--------|-----------------|
| Sin filtros | 69 |
| Categoría = Variables | 4 sintaxis + 11 operadores + 1 receta = 16 |
| Categoría = Control de flujo | 6 sintaxis + 11 operadores + 1 receta = 18 |
| Categoría = Funciones y scripting | 5 sintaxis + 11 operadores + 3 recetas = 19 |
| Categoría = Búsqueda de archivos | igual que iter 2 (no cambia): 8 cmd + 11 op + 3 recetas = 22 |
| Tipo = Sintaxis | 15 |
| Tipo = Receta | 16 |
| Tipo = Comando | 27 |
| Tipo = Operador | 11 |
| Compatible Git Bash (checkbox) | 67 (excluye solo whereis y locate; toda la sintaxis es `ok`) |
| Búsqueda libre = "while" | sx-while, sx-read (menciona while), recetas con while | verificar que no rompe |
| Búsqueda libre = "heredoc" o "EOF" | sx-heredoc + receta-heredoc-config + algún gotcha que mencione EOF |

- [ ] **Step 4: Casos de uso de las tarjetas nuevas**

- Tarjetas gordas nuevas (`sx-variables`, `sx-read`, `sx-arrays`, `sx-if`, `sx-for`, `sx-while`, `sx-tests`, `sx-functions`, `sx-special-vars`, `sx-heredoc`) → "Ver más ▾" funciona (expande/colapsa). Todas con badge ámbar "sintaxis".
- Tarjetas básicas (`sx-export`, `sx-case`, `sx-until`, `sx-alias`, `sx-shebang`) → badge ámbar, sin "Ver más" (no son heavy).
- Indicador gitbash: todas las sintaxis con punto verde.
- Botón copiar funciona en todos los `<pre>` (probar en `sx-heredoc` que tiene varios bloques multi-línea).
- Tarjeta `op-cmd-substitution` ahora tiene 3 gotchas (el último menciona backticks).
- Tarjeta `sx-tests` muestra correctamente el bloque comparativo `[ ]` vs `[[ ]]` con ambas formas en ejemplos.

- [ ] **Step 5: Casos límite de los filtros sintaxis**

- Filtrar Tipo = Sintaxis + Categoría = Variables → 4 tarjetas (sx-variables, sx-export, sx-read, sx-arrays).
- Filtrar Tipo = Sintaxis + Dificultad = básico → sx-variables, sx-export, sx-if, sx-for, sx-while, sx-case, sx-until, sx-alias, sx-shebang = 9.
- Filtrar Tipo = Sintaxis + Compatible Git Bash → 15 (todas las sintaxis son `ok`).

- [ ] **Step 6: Comprobar que el archivo abre sin servidor**

Cerrar el navegador. Doble click sobre `guia-bash.html`. Se abre, todo funciona, copy button funciona.

- [ ] **Step 7: Si todo OK y no se modificó nada en este Task 15**

No hace falta commit. Si se ajustó algo durante el QA:

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "QA pass iteración 3 — ajustes finales"
```

---

## Self-review tras escribir el plan

- **Cobertura del spec-iter3:**
  - §1 Objetivo (añadir scripting bash completo) → cubierto en Tasks 2–14.
  - §2 Alcance (15 sintaxis + 5 recetas = 20 tarjetas) → Tasks 2–14 entregan exactamente esto.
  - §3.1 Nuevas categorías (3) → Task 1.
  - §3.2 Header (título, h1, subtítulo) → Task 1.
  - §3.3 Resto UX sin cambios → respetado.
  - §4 Schema sin cambios estructurales → respetado.
  - §4.1 Flags en sintaxis cuando aplique → respetado en `sx-read` (Task 3) y `sx-alias` (Task 12).
  - §5.1 Gordas (10) → Tasks 2–11.
  - §5.2 Básicas (5) → Task 12.
  - §5.3 Recetas (5) → Task 14.
  - §6 ID conventions (prefijo `sx-` y `receta-`) → respetadas.
  - §7 Lo que NO incluye:
    - getopts → no aparece como tarjeta. ✓
    - set -e / trap → no aparecen. ✓
    - Backticks como gotcha en `op-cmd-substitution` (NO como tarjeta) → Task 13. ✓
    - `<<` / `<<<` viven dentro de `sx-heredoc`, no como operadores → Task 11. ✓
    - Globbing avanzado (nullglob) mencionado como gotcha en `sx-for` (Task 6) y `receta-iterar-archivos` (Task 14). ✓
    - mapfile mencionado como alternativa en gotcha de `receta-leer-archivo-linea` (Task 14). ✓
  - §8 Estado tras iter 3 (69 tarjetas: 27+11+16+15) → verificado en Task 15.
- **Placeholders:** ninguno. Cada tarea tiene su contenido completo.
- **Type consistency:** `tipo: 'sintaxis'` consistente en las 15 nuevas; `categoria` con los 3 valores nuevos; `gitbash: 'ok'` en todas las nuevas (no hay caveats de bash 4+ porque Git Bash lo cumple); ID prefix `sx-` y `receta-` consistente; relacionados apuntan a IDs reales (existentes o creados en este plan).
- **Coherencia de IDs:** los 20 IDs nuevos (`sx-variables`, `sx-export`, `sx-read`, `sx-arrays`, `sx-if`, `sx-case`, `sx-for`, `sx-while`, `sx-until`, `sx-tests`, `sx-functions`, `sx-alias`, `sx-shebang`, `sx-special-vars`, `sx-heredoc`, `receta-leer-archivo-linea`, `receta-iterar-archivos`, `receta-validar-args`, `receta-funcion-return`, `receta-heredoc-config`) son únicos y no chocan con iter 1+2.
- **Mensajes de commit:** todos en español, imperativo presente, sin `Co-Authored-By`.

---

## Plan complete and saved

Plan guardado en `C:\Users\Marius\Desktop\Programacion\VecindApp\guia-bash\plans\2026-05-09-iteracion-3-scripting.md`.

**Dos opciones de ejecución:**

1. **Subagent-Driven (recommended)** — dispatch de un subagent fresh por tarea, revisión entre tareas, iteración rápida.
2. **Inline Execution** — ejecutar las tareas en esta misma sesión usando `executing-plans`, ejecución por lotes con checkpoints.

Pendiente: elegir aproximación. (Si prefieres el flujo "pragmático con un único commit final" como en iter 2, también es válido — solo dilo.)
