# Guía Bash — Iteración 2 (manipulación) — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ampliar `guia-bash.html` con el catálogo de manipulación de archivos descrito en `spec-iter2.md`: 14 comandos nuevos (5 gordos + 9 básicos), 3 operadores nuevos (`<`, `tee`, `&>`) y 5 recetas. Actualizar header y filtro de categorías para reflejar las 3 nuevas categorías (`manipulacion-archivos`, `compresion`, `permisos`).

**Architecture:** Misma arquitectura que iter 1 — un único `guia-bash.html` autocontenido con Tailwind por CDN, JS vanilla, datos embebidos en `const COMANDOS = [...]`. **Ningún cambio de schema, helpers ni lógica.** Esta iteración es esencialmente añadir entradas al array de datos + 3 `<option>` al dropdown de categoría + actualizar dos textos del header. Los renderers de iter 1 (`renderCard`, `renderEjemplos`, `renderFlags`, `renderCombos`, `renderGotchas`, `renderModeloMental`, `renderCuandoNoUsar`, `renderEscenario`, `renderDesglose`, `renderGitbashDot`, `renderTipoBadge`, copy button con fallback) cubren todo el material nuevo sin tocar.

**Tech Stack:** HTML5 + Tailwind CSS (Play CDN) + JavaScript vanilla. Sin build, sin npm, sin frameworks. Apertura por `file://` con doble click.

---

## Pre-requisitos

- **Spec aprobado:** `C:\Users\Marius\Desktop\Programacion\VecindApp\guia-bash\spec-iter2.md`. Léelo antes de empezar — esta iteración solo extiende `spec.md` (commit `d5bead9`); para schema, prohibiciones, reglas de filtros, etc., consulta el spec original.
- **Iteración 1 finalizada y mergeada:** el HTML ya contiene todo lo de iter 1 (catálogo de búsqueda + render multi-tipo + filtros + copy button). Verificable abriendo `guia-bash.html`: banner verde de tests, 27 tarjetas, filtros funcionando.
- **Repo:** `C:\Users\Marius\Desktop\Programacion\VecindApp\` es repo git, branch actual `desarrollo`.
- **Carpeta de trabajo:** todas las modificaciones ocurren en `guia-bash/guia-bash.html`. **No se crean archivos nuevos.**
- **Convención de mensajes de commit:** español, imperativo presente. **NO incluir línea `Co-Authored-By`.**

---

## File Structure

Modificado en este plan (único archivo):

| Ruta (relativa a `VecindApp/`) | Responsabilidad |
|---|---|
| `guia-bash/guia-bash.html` | Único artefacto. Las modificaciones tocan: `<title>` + `<h1>` + `<p>` del header (texto de iteración), `<select id="filter-categoria">` (3 nuevas opciones) y `const COMANDOS = [...]` (22 entradas nuevas). |

No se crean tests separados, ni CSS externo, ni JSON de datos (igual que iter 1; spec §3 lo prohíbe).

---

## Estrategia de testing

- Los tests inline existentes de `applyFilters` y `renderCard` (creados en iter 1) cubren la lógica. Esta iteración **no añade lógica nueva** — solo datos. No hace falta ampliar la suite.
- Tras cada tarea de carga de datos, verificación visual recargando el HTML:
  - Banner verde "✓ N tests OK" sigue saliendo (los tests no dependen del catálogo cargado).
  - El conteo de tarjetas sin filtros aumenta según lo añadido.
  - Filtros nuevos se comportan según `spec-iter2.md` §3.1 y §4.

**Cómo "correr los tests":** abrir `guia-bash.html` con doble click. Los tests corren automáticamente al cargar.

---

## Convenciones generales para todas las tareas

- **Cwd para git:** `C:\Users\Marius\Desktop\Programacion\VecindApp\` (usar `git -C` ahí; en Git Bash usar forward slashes en paths).
- **Tras cada cambio:** abrir/recargar el HTML en navegador y validar el "Esperado" del step.
- **Commit por tarea:** mensaje en español, sin `Co-Authored-By`.
- **Si una verificación falla:** no commitear. Diagnosticar, corregir, validar, y entonces commitear.
- **Patrón de inserción en el array `COMANDOS`:** siempre **antes** del `]` de cierre del array, en el orden que dicta cada tarea. La línea inmediatamente anterior debe terminar con `,` separando del último elemento existente. (En iter 1 cada nueva tarjeta empieza por `,{` siguiendo este patrón — mantenlo.)
- **Punto de inserción del array:** abrir `guia-bash.html`, buscar `// ====== JS: DATA ======`. La sección comienza con `const COMANDOS = [` y termina con `];`. Las nuevas tarjetas se añaden justo antes del `];`.
- **Convención del campo `compat_notes`:** siempre presente en cada `ejemplo` y cada `combo` (puede ser `null` si no aplica). En `flags` el campo es opcional pero por consistencia con iter 1 se incluye con `null` cuando no haya nota.
- **Convención de IDs (spec-iter2 §6):**
  - Comandos: `cp`, `mv`, `rm`, `mkdir`, `rmdir`, `touch`, `ln`, `tar`, `gzip`, `gunzip`, `zip`, `unzip`, `chmod`, `chown`.
  - Operadores: `op-redirect-stdin`, `op-tee`, `op-redirect-all`.
  - Recetas: `receta-backup-antes-sobrescribir`, `receta-comprimir-excluyendo`, `receta-script-ejecutable`, `receta-renombrar-masivo`, `receta-borrar-antiguos-confirmacion`.

---

## Tasks

### Task 1: Actualizar header y dropdown de categorías

**Files:**
- Modify: `guia-bash/guia-bash.html` (sección `<head>`, `<header>`, y `<select id="filter-categoria">`)

- [ ] **Step 1: Cambiar el `<title>` para reflejar las dos iteraciones**

Buscar:

```html
  <title>Guía Bash — Búsqueda</title>
```

Sustituir por:

```html
  <title>Guía Bash — Búsqueda y manipulación</title>
```

- [ ] **Step 2: Actualizar el `<h1>` y subtítulo del header**

Buscar:

```html
      <h1 class="text-2xl font-bold">Guía Bash — Búsqueda</h1>
      <p class="text-sm text-slate-600">Iteración 1: archivos, contenido, operadores, recetas.</p>
```

Sustituir por:

```html
      <h1 class="text-2xl font-bold">Guía Bash — Búsqueda y manipulación</h1>
      <p class="text-sm text-slate-600">Iteraciones 1–2: búsqueda, manipulación, compresión, permisos, operadores y recetas.</p>
```

- [ ] **Step 3: Añadir 3 nuevas opciones al dropdown de categoría**

Buscar el bloque del select:

```html
        <select id="filter-categoria" class="ml-1 border border-slate-300 rounded px-2 py-1 text-sm">
          <option value="all">Todas</option>
          <option value="busqueda-archivos">Búsqueda de archivos</option>
          <option value="busqueda-contenido">Búsqueda de contenido</option>
        </select>
```

Sustituir por:

```html
        <select id="filter-categoria" class="ml-1 border border-slate-300 rounded px-2 py-1 text-sm">
          <option value="all">Todas</option>
          <option value="busqueda-archivos">Búsqueda de archivos</option>
          <option value="busqueda-contenido">Búsqueda de contenido</option>
          <option value="manipulacion-archivos">Manipulación de archivos</option>
          <option value="compresion">Compresión</option>
          <option value="permisos">Permisos</option>
        </select>
```

- [ ] **Step 4: Verificar visualmente**

Recargar `guia-bash.html`. **Esperado:**
- Pestaña del navegador con título "Guía Bash — Búsqueda y manipulación".
- Header: `Guía Bash — Búsqueda y manipulación` + subtítulo "Iteraciones 1–2: búsqueda, manipulación, compresión, permisos, operadores y recetas."
- Dropdown "Categoría" muestra 6 opciones (Todas, Búsqueda de archivos, Búsqueda de contenido, Manipulación de archivos, Compresión, Permisos).
- Seleccionar "Manipulación de archivos" → grid vacío con empty state ("Ningún comando coincide…") porque aún no hay datos en esa categoría. Los 8 operadores transversales de iter 1 **sí** deben seguir apareciendo (la regla "sin categoria pasan siempre" se mantiene).
- Banner verde de tests sigue saliendo.

- [ ] **Step 5: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Actualizar header y dropdown de categorías para iteración 2"
```

---

### Task 2: Cargar `cp` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Añadir la tarjeta de `cp` antes del `]` del array**

Localizar el cierre del array `COMANDOS`:

```js
        ]
      }
    ];
```

(El `]` interior cierra los `gotchas` de la última receta de iter 1; el `]` siguiente al `}` cierra el array.)

Insertar entre `}` (cierre del último elemento existente) y `];`, prefijado con `,`:

```js
      ,{
        id: 'cp',
        tipo: 'comando',
        nombre: 'cp',
        categoria: 'manipulacion-archivos',
        dificultad: 'basico',
        descripcion: 'Copia archivos y/o directorios.',
        gitbash: 'ok',
        patron: 'cp [opciones] ORIGEN... DESTINO',
        tags: ['copiar', 'archivos', 'recursivo'],
        modelo_mental: 'cp copia ORIGEN(es) a DESTINO. Si DESTINO existe y es un directorio, ORIGEN se copia DENTRO; si DESTINO es un archivo (existente o no), se crea/sobrescribe. Por defecto NO copia directorios — necesitas -r. Y por defecto SOBRESCRIBE el destino sin avisar.',
        ejemplos: [
          { comando: 'cp original.txt copia.txt',           explicacion: 'Copia un archivo a un nombre nuevo en el mismo directorio.', compat_notes: null },
          { comando: 'cp archivo.txt /tmp/',                explicacion: 'Copia archivo.txt DENTRO de /tmp/ (mantiene el nombre).',     compat_notes: null },
          { comando: 'cp -r src/ dst/',                     explicacion: 'Copia el directorio src/ y todo su contenido como dst/.',      compat_notes: null },
          { comando: 'cp -p config.yml backup/',            explicacion: 'Preserva permisos y timestamps al copiar.',                    compat_notes: null },
          { comando: 'cp -i a.txt b.txt',                   explicacion: 'Pide confirmación si b.txt existe.',                            compat_notes: null }
        ],
        flags: [
          { flag: '-r / -R',         descripcion: 'Recursivo: necesario para copiar directorios.',                                       compat_notes: null },
          { flag: '-a',              descripcion: 'Archive: equivale a -dR --preserve=all (recursivo + preserva todo + sigue symlinks como tales).', compat_notes: null },
          { flag: '-p',              descripcion: 'Preserva modo, dueño y timestamps.',                                                  compat_notes: 'En Git Bash dueño/grupo se ajustan a Windows (semi-decorativo).' },
          { flag: '-i',              descripcion: 'Interactive: pide confirmación antes de sobrescribir.',                              compat_notes: null },
          { flag: '-n',              descripcion: 'No-clobber: nunca sobrescribe (opuesto a -i sin pregunta).',                         compat_notes: null },
          { flag: '-u',              descripcion: 'Update: solo copia si ORIGEN es más nuevo que DESTINO o DESTINO no existe.',        compat_notes: null },
          { flag: '-v',              descripcion: 'Verbose: imprime cada copia realizada.',                                              compat_notes: null },
          { flag: '--backup[=TIPO]', descripcion: 'Hace copia de seguridad del DESTINO antes de sobrescribir (TIPO: numbered, simple).', compat_notes: null }
        ],
        combos: [
          { comando: 'cp -r src/* dst/',          explicacion: 'Copia el CONTENIDO de src/ dentro de dst/ (no crea dst/src/).',                              compat_notes: null },
          { comando: 'cp -av src/ dst/',          explicacion: 'Backup completo preservando metadatos, mostrando cada archivo copiado.',                     compat_notes: null },
          { comando: 'cp --backup=numbered f g',  explicacion: 'Si g existe, se renombra a g.~1~ antes de sobrescribir; si ya existía g.~1~, será g.~2~.', compat_notes: null }
        ],
        gotchas: [
          'Sobrescribe el destino SIN AVISAR. Usa -i (preguntar), -n (no sobrescribir nunca) o --backup según necesites.',
          'Sin -r, copiar un directorio falla con "omitting directory". -r recursivo es la solución; -a recursivo + preserva metadatos.',
          'Diferencia clave: `cp -r src/ dst/` crea dst/ como copia de src/. `cp -r src/* dst/` copia el contenido de src/ dentro de dst/. La barra final NO siempre cambia el comportamiento — depende de existir o no el destino.',
          'cp NO copia archivos ocultos (`.algo`) cuando usas `src/*` salvo que actives shopt dotglob o uses `src/.* src/*`.',
          'En FS distintos cp duplica datos; mv en FS distintos también copia y borra (no es atómico).'
        ],
        relacionados: ['mv', 'rm', 'ln'],
        cuando_no_usar: 'Para sincronizar árboles grandes con cambios incrementales, `rsync -av` es mucho más rápido (transfiere solo diferencias) y soporta exclusiones, dry-run y borrado del destino. Para clonar discos completos, `dd`.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:**
- Banner verde de tests.
- 28 tarjetas en total.
- Filtrar Categoría = "Manipulación de archivos" → 1 tarjeta (`cp`) + los 8 operadores transversales = 9 tarjetas.
- La tarjeta `cp` muestra punto verde gitbash, badge sky "comando", patrón en gris, "Ver más ▾" (es heavy por modelo_mental + gotchas + 8 flags). Al expandir: modelo mental, 5 ejemplos, 8 flags (uno con compat_notes amarillo en `-p`), 3 combos, 5 gotchas, "Cuándo NO usarlo".

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de cp con flags, combos, gotchas y cuándo no usar"
```

---

### Task 3: Cargar `mv` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar la tarjeta de `mv` justo después de `cp` (antes del `];` final)**

```js
      ,{
        id: 'mv',
        tipo: 'comando',
        nombre: 'mv',
        categoria: 'manipulacion-archivos',
        dificultad: 'basico',
        descripcion: 'Mueve y/o renombra archivos y directorios.',
        gitbash: 'ok',
        patron: 'mv [opciones] ORIGEN... DESTINO',
        tags: ['mover', 'renombrar', 'archivos'],
        modelo_mental: 'mv hace dos cosas según el caso: si ORIGEN y DESTINO están en el mismo filesystem es un rename atómico (cambia la entrada en el directorio, no copia datos). Si están en filesystems distintos, hace cp + rm. No tiene -r porque siempre mueve directorios completos. Sobrescribe el destino sin avisar.',
        ejemplos: [
          { comando: 'mv viejo.txt nuevo.txt',     explicacion: 'Renombra (mismo directorio, mismo FS).',                                 compat_notes: null },
          { comando: 'mv archivo.txt carpeta/',    explicacion: 'Mueve archivo.txt DENTRO de carpeta/.',                                  compat_notes: null },
          { comando: 'mv carpeta1/ carpeta2/',     explicacion: 'Renombra carpeta1 a carpeta2 (si carpeta2 no existía).',                 compat_notes: null },
          { comando: 'mv -i a.txt b.txt',           explicacion: 'Pide confirmación si b.txt existe.',                                     compat_notes: null },
          { comando: 'mv -n source/* dest/',        explicacion: 'No sobrescribe ningún archivo en dest/ que ya exista.',                  compat_notes: null }
        ],
        flags: [
          { flag: '-i',              descripcion: 'Interactive: pide confirmación antes de sobrescribir.',          compat_notes: null },
          { flag: '-n',              descripcion: 'No-clobber: nunca sobrescribe.',                                  compat_notes: null },
          { flag: '-u',              descripcion: 'Update: solo mueve si ORIGEN es más nuevo o DESTINO no existe.', compat_notes: null },
          { flag: '-v',              descripcion: 'Verbose: imprime cada movimiento.',                                compat_notes: null },
          { flag: '--backup[=TIPO]', descripcion: 'Hace copia de seguridad del DESTINO si lo va a sobrescribir.',     compat_notes: null }
        ],
        combos: [
          { comando: 'mv *.log archivo/',                       explicacion: 'Mueve todos los .log al directorio archivo/.',                       compat_notes: null },
          { comando: 'mv -- -archivo-raro.txt /tmp/',           explicacion: 'El `--` evita que mv interprete -archivo-raro.txt como flag.',         compat_notes: null }
        ],
        gotchas: [
          'Sobrescribe el destino SIN AVISAR (igual que cp). Usa -i o -n.',
          'No hay -r: mv mueve directorios sin opción extra (porque "mover" un dir es solo cambiar su entrada en el FS).',
          'Entre filesystems distintos NO es atómico — internamente hace cp + rm. Si la copia falla a la mitad, puedes quedarte con datos a medias en destino y origen aún presente.',
          'Para nombres que empiezan con `-`, anteponer `--` o usar `./` (`mv ./-raro destino/`).',
          'En Git Bash mover entre unidades (C: → D:) cuenta como filesystems distintos.'
        ],
        relacionados: ['cp', 'rm'],
        cuando_no_usar: 'Para renombrar muchos archivos con un patrón (cambiar extensión, prefijar...) un solo `mv` no basta — usa un bucle `for f in *.txt; do mv "$f" "${f%.txt}.md"; done` o `find ... | xargs -I {} mv {} {}.bak`. Existen también `rename` (Perl) y `mmv`.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 29 tarjetas. Categoría "Manipulación de archivos" → 2 tarjetas + 8 operadores = 10. La tarjeta `mv` con punto verde, "Ver más" expandible.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de mv con caveat de filesystems y renombrado masivo"
```

---

### Task 4: Cargar `rm` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar la tarjeta de `rm` después de `mv`**

```js
      ,{
        id: 'rm',
        tipo: 'comando',
        nombre: 'rm',
        categoria: 'manipulacion-archivos',
        dificultad: 'intermedio',
        descripcion: 'Borra archivos. Con -r, también directorios.',
        gitbash: 'ok',
        patron: 'rm [opciones] ARCHIVO...',
        tags: ['borrar', 'destructivo', 'recursivo'],
        modelo_mental: 'rm desliga (unlink) entradas del filesystem. NO HAY PAPELERA: lo borrado se considera perdido. Sin -r no borra directorios. Sin -f, los archivos protegidos contra escritura piden confirmación. La combinación -rf es la versión "fuerza bruta" — y la fuente clásica de desastres cuando se aplica a la ruta equivocada.',
        ejemplos: [
          { comando: 'rm archivo.txt',           explicacion: 'Borra un archivo.',                                                       compat_notes: null },
          { comando: 'rm -r carpeta/',           explicacion: 'Borra un directorio y todo lo que contenga (recursivo).',                 compat_notes: null },
          { comando: 'rm -i *.tmp',              explicacion: 'Pide confirmación por cada archivo .tmp.',                                 compat_notes: null },
          { comando: 'rm -- -archivo.txt',       explicacion: 'El `--` evita interpretar -archivo.txt como flag.',                       compat_notes: null }
        ],
        flags: [
          { flag: '-r / -R',  descripcion: 'Recursivo: necesario para borrar directorios.',                                                              compat_notes: null },
          { flag: '-f',       descripcion: 'Force: no pregunta y no falla si el archivo no existe.',                                                       compat_notes: null },
          { flag: '-i',       descripcion: 'Interactive: confirma cada borrado.',                                                                          compat_notes: null },
          { flag: '-I',       descripcion: 'Confirma una sola vez si vas a borrar > 3 archivos o de forma recursiva (más cómodo que -i para purgas masivas).', compat_notes: null },
          { flag: '-v',       descripcion: 'Verbose: imprime qué borra.',                                                                                  compat_notes: null },
          { flag: '-d',       descripcion: 'Borra directorios vacíos sin necesitar -r (alternativa a `rmdir`).',                                            compat_notes: null }
        ],
        combos: [
          { comando: "find . -name '*.tmp' -exec rm {} +",     explicacion: 'Borra todos los .tmp encontrados (alternativa más segura: rm -i con find).', compat_notes: null },
          { comando: "find . -name '*.bak' -delete",            explicacion: 'find tiene -delete propio: más eficiente y seguro que combinar con rm.',     compat_notes: null }
        ],
        gotchas: [
          'NO TIENE PAPELERA. Lo borrado solo se recupera con un backup previo o con utilidades de recuperación de bajo nivel.',
          '`rm -rf /` o variantes accidentales son catastróficas. Versiones modernas de rm (GNU coreutils ≥ 6.4) bloquean `--no-preserve-root` por defecto.',
          'Variables vacías: `rm -rf $UNSET/*` se expande a `rm -rf /*`. Usar siempre comillas (`rm -rf "$DIR"/...`) y/o `set -u`.',
          'Globbing inesperado: `rm *` borra solo lo que el shell expandió — no archivos ocultos salvo dotglob. Si el directorio está vacío o el glob no matchea, sin -f rm falla con "no such file".',
          'Archivos cuyo nombre empieza por `-`: usar `--` o `./` para evitar que rm los interprete como flags.',
          '`rm` no borra directorios sin -r (excepto vacíos con -d). Para limpiar directorios vacíos sueltos, `rmdir` es la alternativa explícita.'
        ],
        relacionados: ['rmdir', 'find', 'mv'],
        cuando_no_usar: 'Si quieres papelera real para poder revertir, usa `gio trash FILE` (GNOME) o `trash-cli` (`trash-put FILE`). En entornos compartidos o productivos, prefiere `mv FILE /ruta/cuarentena/` antes de rm — solo borra cuando hayas confirmado que nadie lo necesita.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 30 tarjetas. Tarjeta `rm` con punto verde, dificultad "intermedio". Filtrar Dificultad = "Intermedio" → ahora hay más (find, grep, xargs, head/tail no, op-cmd-substitution, op-stderr-stdout, recetas intermedias, y rm).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de rm con énfasis en gotchas destructivos"
```

---

### Task 5: Cargar `chmod` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar la tarjeta de `chmod` después de `rm`**

```js
      ,{
        id: 'chmod',
        tipo: 'comando',
        nombre: 'chmod',
        categoria: 'permisos',
        dificultad: 'intermedio',
        descripcion: 'Cambia los permisos de un archivo o directorio.',
        gitbash: 'caveats',
        patron: 'chmod [opciones] MODO ARCHIVO...',
        tags: ['permisos', 'octal', 'simbolico'],
        modelo_mental: 'Los permisos POSIX son tres triadas — dueño / grupo / otros — cada una con tres bits: r (4), w (2), x (1). chmod acepta DOS notaciones: octal (sumas de 4/2/1 por triada, p.ej. 755 = rwxr-xr-x) y simbólica (`u+x`, `g-w`, `o=r`, `a+rw` donde u=user, g=group, o=others, a=all). Para directorios, x significa "permiso para entrar" (no ejecutar).',
        ejemplos: [
          { comando: 'chmod 755 script.sh',       explicacion: 'rwx para dueño, r-x para grupo y otros (típico de ejecutables).',          compat_notes: null },
          { comando: 'chmod 644 archivo.txt',     explicacion: 'rw- dueño, r-- resto (típico de archivos de datos).',                       compat_notes: null },
          { comando: 'chmod +x script.sh',        explicacion: 'Añade permiso de ejecución para todos (forma simbólica).',                  compat_notes: null },
          { comando: 'chmod u+x,g-w archivo',     explicacion: 'Dueño gana ejecución; grupo pierde escritura.',                              compat_notes: null },
          { comando: 'chmod -R 755 dir/',         explicacion: 'Aplica recursivamente a todo el árbol (cuidado: aplica el mismo modo a archivos Y directorios).', compat_notes: null }
        ],
        flags: [
          { flag: '-R',              descripcion: 'Recursivo: aplica a todo el árbol bajo el directorio.',                          compat_notes: null },
          { flag: '-v',              descripcion: 'Verbose: imprime cada cambio.',                                                  compat_notes: null },
          { flag: '-c',              descripcion: 'Como -v pero solo imprime los archivos efectivamente modificados.',             compat_notes: null },
          { flag: '--reference=REF', descripcion: 'Copia los permisos de REF al archivo destino.',                                  compat_notes: null }
        ],
        combos: [
          { comando: 'chmod -R u+rwX,go+rX,go-w dir/', explicacion: 'X mayúscula: aplica x SOLO a directorios y archivos ya ejecutables. Truco clásico para chmod -R en árboles mixtos.', compat_notes: null },
          { comando: 'chmod --reference=plantilla.txt nuevo.txt', explicacion: 'Copia permisos de un archivo modelo al nuevo.',                  compat_notes: null }
        ],
        gotchas: [
          'En la triada octal: r=4, w=2, x=1. 755 = 4+2+1 / 4+0+1 / 4+0+1 = rwxr-xr-x. 644 = 6/4/4 = rw-r--r--. 700 = solo dueño.',
          'Para directorios: x significa "puedo entrar (cd)"; r significa "puedo listar (ls)"; w significa "puedo crear/borrar archivos dentro". Un dir con r sin x permite ver nombres pero no acceder.',
          'En Git Bash y Windows: NTFS no usa el modelo POSIX exacto. chmod parece funcionar pero el sistema operativo respeta sus propios ACLs. El bit +x sobre un .sh sí permite "./script.sh" desde Git Bash.',
          'chmod -R 755 sobre un árbol mixto pone x en archivos de datos también (no deseable). Usar `chmod -R u+rwX,go+rX,go-w dir/` (X mayúscula) para aplicar x solo donde tiene sentido.',
          'El bit setuid (4xxx), setgid (2xxx) y sticky (1xxx) son una cuarta triada que precede a las tres habituales (chmod 4755 → rwsr-xr-x). Útil casi solo para sysadmin.'
        ],
        relacionados: ['chown', 'ls'],
        cuando_no_usar: 'Para gestión avanzada de permisos por usuario/grupo (más allá del modelo POSIX 3-triadas), usa ACLs con `setfacl`/`getfacl`. Para cambiar el dueño, no chmod sino `chown`.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 31 tarjetas. Tarjeta `chmod` con punto **amarillo** (caveats), badge sky, "Ver más" expandible. Filtrar Categoría = "Permisos" → 1 tarjeta (chmod) + 8 operadores = 9.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de chmod con doble notación octal y simbólica"
```

---

### Task 6: Cargar `tar` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar la tarjeta de `tar` después de `chmod`**

```js
      ,{
        id: 'tar',
        tipo: 'comando',
        nombre: 'tar',
        categoria: 'compresion',
        dificultad: 'intermedio',
        descripcion: 'Empaqueta y/o comprime archivos en un solo fichero (tarball).',
        gitbash: 'ok',
        patron: 'tar [czxtjJv]f ARCHIVO.tar[.gz] [archivos...]',
        tags: ['comprimir', 'archivar', 'backup'],
        modelo_mental: 'tar combina dos cosas: empaquetar un árbol de archivos en uno solo (.tar) y comprimirlo. Letras clave: c=create, x=extract, t=list, f=archivo (siempre va con su nombre justo después), v=verbose. Para compresión: z=gzip (.tar.gz), j=bzip2 (.tar.bz2), J=xz (.tar.xz). Memoriza tres atajos: `czf` crear comprimido, `xzf` extraer comprimido, `tvf` listar contenido.',
        ejemplos: [
          { comando: 'tar czf backup.tar.gz proyecto/',          explicacion: 'Crea backup.tar.gz comprimido con gzip a partir del directorio proyecto/.',  compat_notes: null },
          { comando: 'tar xzf backup.tar.gz',                    explicacion: 'Extrae backup.tar.gz en el directorio actual.',                              compat_notes: null },
          { comando: 'tar tzf backup.tar.gz',                    explicacion: 'Lista (sin extraer) el contenido del tarball.',                              compat_notes: null },
          { comando: 'tar xzf backup.tar.gz -C /tmp/',           explicacion: 'Extrae en /tmp/ en lugar del directorio actual.',                            compat_notes: null },
          { comando: 'tar czf out.tar.gz --exclude=node_modules proyecto/', explicacion: 'Crea el tarball excluyendo node_modules.',                       compat_notes: null }
        ],
        flags: [
          { flag: 'c',           descripcion: 'Create: crea un nuevo archivo.',                                                                          compat_notes: null },
          { flag: 'x',           descripcion: 'Extract: extrae el contenido.',                                                                            compat_notes: null },
          { flag: 't',           descripcion: 'List: muestra el contenido sin extraer.',                                                                  compat_notes: null },
          { flag: 'f ARCHIVO',   descripcion: 'File: usa ARCHIVO (debe ir inmediatamente antes del nombre del fichero).',                                compat_notes: null },
          { flag: 'z',           descripcion: 'gzip: comprime/descomprime con gzip (.tar.gz).',                                                          compat_notes: null },
          { flag: 'j',           descripcion: 'bzip2: comprime/descomprime con bzip2 (.tar.bz2). Mejor compresión, más lento.',                          compat_notes: null },
          { flag: 'J',           descripcion: 'xz: comprime/descomprime con xz (.tar.xz). Mejor compresión que bzip2, más lento aún.',                   compat_notes: null },
          { flag: 'v',           descripcion: 'Verbose: imprime cada archivo procesado.',                                                                compat_notes: null },
          { flag: '-C DIR',      descripcion: 'Cambia a DIR antes de operar (útil al extraer sin contaminar el cwd).',                                   compat_notes: null },
          { flag: '--exclude=PATRON', descripcion: 'Excluye archivos/directorios que matcheen el patrón.',                                                compat_notes: null }
        ],
        combos: [
          { comando: 'tar czf - dir/ | ssh remoto "cd /backup && tar xzf -"',      explicacion: 'Pipe: comprime localmente, descomprime en remoto sin archivo intermedio.', compat_notes: null },
          { comando: 'tar czf out.tar.gz --exclude=.git --exclude=node_modules .', explicacion: 'Múltiples exclusiones para empaquetar el proyecto actual sin basura.',     compat_notes: null }
        ],
        gotchas: [
          'El orden de los flags importa: `f` debe ir inmediatamente antes del nombre del archivo. `tar czf out.tar.gz dir` ✓, `tar fcz out.tar.gz dir` ✗.',
          'Las extensiones (.tar, .tar.gz, .tgz, .tar.bz2, .tar.xz) son convencionales — tar no las infiere. Tienes que indicarle el modo de compresión con z/j/J explícitamente (versiones modernas detectan al extraer con `tar xf`, pero por costumbre se especifica).',
          'Sin -C, `tar xzf` extrae en el cwd. Si el tarball no tiene un directorio raíz único, te llena el cwd de archivos sueltos. Antes de extraer algo desconocido, listar con `tar tzf` y/o usar `-C /tmp/dir`.',
          '--exclude debe ir ANTES del directorio que se empaqueta (`tar czf out --exclude=node_modules proyecto/`). Después del directorio, no surte efecto en algunas versiones.',
          'Tarballs grandes con xz consumen mucha CPU; si necesitas velocidad usa gzip; si el tamaño manda y tienes tiempo, xz.'
        ],
        relacionados: ['gzip', 'gunzip', 'zip', 'unzip'],
        cuando_no_usar: 'Para intercambio con usuarios de Windows que abrirán el archivo desde el explorador, `zip` es más portable (Windows extrae .zip nativamente). Para sincronización incremental de árboles, `rsync`. Para compresión single-file ya empaquetado por otra vía, `gzip`/`xz` directos.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 32 tarjetas. Tarjeta `tar` con punto verde, "Ver más" expandible. Filtrar Categoría = "Compresión" → 1 tarjeta (tar) + 8 operadores = 9.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar tarjeta gorda de tar con sintaxis críptica de flags y modos de compresión"
```

---

### Task 7: Cargar comandos básicos de manipulación (mkdir, rmdir, touch, ln)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar las 4 tarjetas básicas tras `tar`**

```js
      ,{
        id: 'mkdir',
        tipo: 'comando', nombre: 'mkdir', categoria: 'manipulacion-archivos', dificultad: 'basico',
        descripcion: 'Crea uno o varios directorios.',
        gitbash: 'ok',
        patron: 'mkdir [opciones] DIRECTORIO...',
        ejemplos: [
          { comando: 'mkdir nuevo',           explicacion: 'Crea el directorio "nuevo" en el cwd.',                            compat_notes: null },
          { comando: 'mkdir -p a/b/c',         explicacion: 'Crea toda la jerarquía a/b/c (no falla si algún tramo ya existe).', compat_notes: null },
          { comando: 'mkdir d1 d2 d3',         explicacion: 'Crea tres directorios de un golpe.',                               compat_notes: null }
        ],
        flags: [
          { flag: '-p', descripcion: 'Crea directorios padres si no existen y no falla si el directorio ya existe.', compat_notes: null },
          { flag: '-v', descripcion: 'Verbose: imprime cada directorio creado.',                                      compat_notes: null },
          { flag: '-m MODO', descripcion: 'Asigna el modo (permisos) al crear, sin pasar por el umask.',             compat_notes: null }
        ]
      }
      ,{
        id: 'rmdir',
        tipo: 'comando', nombre: 'rmdir', categoria: 'manipulacion-archivos', dificultad: 'basico',
        descripcion: 'Borra directorios SOLO si están vacíos.',
        gitbash: 'ok',
        patron: 'rmdir [opciones] DIRECTORIO...',
        ejemplos: [
          { comando: 'rmdir vacio',         explicacion: 'Borra el directorio si no contiene nada.',                  compat_notes: null },
          { comando: 'rmdir -p a/b/c',       explicacion: 'Borra c, luego b si quedó vacío, luego a si quedó vacío.',   compat_notes: null }
        ],
        flags: [
          { flag: '-p', descripcion: 'Borra también los padres si quedan vacíos tras el borrado.', compat_notes: null }
        ],
        gotchas: [
          'rmdir falla con "Directory not empty" si hay algo dentro. Para borrar dir + contenido, `rm -r DIR`.'
        ]
      }
      ,{
        id: 'touch',
        tipo: 'comando', nombre: 'touch', categoria: 'manipulacion-archivos', dificultad: 'basico',
        descripcion: 'Crea un archivo vacío o actualiza la fecha de acceso/modificación de uno existente.',
        gitbash: 'ok',
        patron: 'touch [opciones] ARCHIVO...',
        ejemplos: [
          { comando: 'touch nuevo.txt',          explicacion: 'Si no existe, crea un archivo vacío. Si existe, actualiza su mtime/atime a ahora.', compat_notes: null },
          { comando: 'touch -t 202404150830 f',   explicacion: 'Pone mtime/atime al timestamp dado (YYYYMMDDhhmm).',                                  compat_notes: null },
          { comando: 'touch -r ref.txt nuevo.txt', explicacion: 'Copia los timestamps de ref.txt a nuevo.txt.',                                       compat_notes: null }
        ],
        flags: [
          { flag: '-c',         descripcion: 'No crea el archivo si no existe (solo actualiza si está).',     compat_notes: null },
          { flag: '-t TIMESTAMP', descripcion: 'Asigna timestamp explícito (formato YYYYMMDDhhmm[.ss]).',     compat_notes: null },
          { flag: '-r REF',     descripcion: 'Usa los timestamps de REF como modelo.',                         compat_notes: null }
        ]
      }
      ,{
        id: 'ln',
        tipo: 'comando', nombre: 'ln', categoria: 'manipulacion-archivos', dificultad: 'basico',
        descripcion: 'Crea enlaces (links) hard o simbólicos a un archivo o directorio.',
        gitbash: 'caveats',
        patron: 'ln [opciones] ORIGEN [DESTINO]',
        ejemplos: [
          { comando: 'ln -s /ruta/larga corta',        explicacion: 'Crea un enlace simbólico "corta" que apunta a /ruta/larga.', compat_notes: null },
          { comando: 'ln archivo.txt copia',           explicacion: 'Crea un hard link "copia" — misma inode, mismo contenido.',  compat_notes: 'Hard links a directorios prohibidos en la mayoría de FS.' },
          { comando: 'ln -sf /nuevo destino',          explicacion: 'Reemplaza un symlink existente apuntando a /nuevo.',          compat_notes: null }
        ],
        flags: [
          { flag: '-s', descripcion: 'Symbolic: crea enlace simbólico (soft link). Sin -s, ln crea hard link.', compat_notes: null },
          { flag: '-f', descripcion: 'Force: sobrescribe DESTINO si existe.',                                    compat_notes: null },
          { flag: '-n', descripcion: 'Si DESTINO es un symlink a un directorio, no entrar ahí — tratar el symlink como archivo (útil con -f para reemplazarlo).', compat_notes: null },
          { flag: '-v', descripcion: 'Verbose.',                                                                  compat_notes: null }
        ],
        gotchas: [
          'Hard link: dos entradas de directorio apuntan al mismo inode (mismo archivo físico). Borrar una no borra el contenido. Solo dentro del MISMO filesystem.',
          'Symlink (-s): un archivo especial que contiene una ruta. Si borras el origen, el symlink queda "colgado" (broken).',
          'En Git Bash sobre Windows, los symlinks requieren privilegios o modo desarrollador activado en Windows; si no, ln -s suele copiar el archivo en lugar de crear el symlink.'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 36 tarjetas. Filtrar Categoría = "Manipulación de archivos" → 7 tarjetas (cp, mv, rm, mkdir, rmdir, touch, ln) + 8 operadores = 15. `ln` con punto amarillo (caveats), el resto verde.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar comandos básicos de manipulación (mkdir, rmdir, touch, ln)"
```

---

### Task 8: Cargar `chown` (básico) y comandos básicos de compresión (gzip, gunzip, zip, unzip)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar las 5 tarjetas básicas tras `ln`**

```js
      ,{
        id: 'chown',
        tipo: 'comando', nombre: 'chown', categoria: 'permisos', dificultad: 'basico',
        descripcion: 'Cambia el dueño y/o grupo de un archivo o directorio.',
        gitbash: 'caveats',
        patron: 'chown [opciones] DUENO[:GRUPO] ARCHIVO...',
        ejemplos: [
          { comando: 'chown alice archivo.txt',       explicacion: 'Cambia el dueño a alice (grupo sin tocar).',                       compat_notes: null },
          { comando: 'chown alice:devs archivo.txt',  explicacion: 'Cambia dueño Y grupo en una sola operación.',                      compat_notes: null },
          { comando: 'chown :devs archivo.txt',       explicacion: 'Cambia solo el grupo (deja el dueño).',                            compat_notes: null },
          { comando: 'sudo chown -R www-data:www-data /var/www/sitio', explicacion: 'Cambia recursivamente todo el árbol del sitio.', compat_notes: 'Cambiar a otro dueño suele requerir sudo.' }
        ],
        flags: [
          { flag: '-R',              descripcion: 'Recursivo en el árbol.',                                  compat_notes: null },
          { flag: '-v',              descripcion: 'Verbose.',                                                 compat_notes: null },
          { flag: '--reference=REF', descripcion: 'Copia dueño/grupo de REF al destino.',                     compat_notes: null }
        ],
        gotchas: [
          'Cambiar el dueño a otro usuario distinto del actual normalmente requiere root (sudo). Cambiar solo el grupo a uno al que perteneces es suficiente con permisos normales.',
          'En Git Bash el modelo POSIX no se mapea exactamente a Windows; chown a usuarios Windows funciona pero el efecto práctico es limitado.'
        ]
      }
      ,{
        id: 'gzip',
        tipo: 'comando', nombre: 'gzip', categoria: 'compresion', dificultad: 'basico',
        descripcion: 'Comprime un archivo con el algoritmo gzip (lo reemplaza por archivo.gz).',
        gitbash: 'ok',
        patron: 'gzip [opciones] ARCHIVO...',
        ejemplos: [
          { comando: 'gzip log.txt',          explicacion: 'Crea log.txt.gz y BORRA log.txt original.',          compat_notes: null },
          { comando: 'gzip -k log.txt',       explicacion: 'Mantiene también el original (-k = keep).',          compat_notes: null },
          { comando: 'gzip -9 grande.bin',    explicacion: 'Máxima compresión (más lento). -1 es más rápida.',  compat_notes: null }
        ],
        flags: [
          { flag: '-k',     descripcion: 'Keep: no borra el archivo original.',                        compat_notes: null },
          { flag: '-d',     descripcion: 'Decompress: descomprime (equivalente a `gunzip`).',         compat_notes: null },
          { flag: '-1..-9', descripcion: 'Nivel de compresión (1 rápido / 9 máximo). Por defecto 6.', compat_notes: null },
          { flag: '-r',     descripcion: 'Recursivo: comprime cada archivo dentro de un árbol.',      compat_notes: null }
        ],
        gotchas: [
          'gzip por defecto BORRA el original tras comprimir. Usa -k para conservarlo.',
          'gzip comprime UN archivo por invocación; no empaqueta múltiples — para eso usa tar (`tar czf` = tar + gzip).'
        ]
      }
      ,{
        id: 'gunzip',
        tipo: 'comando', nombre: 'gunzip', categoria: 'compresion', dificultad: 'basico',
        descripcion: 'Descomprime un archivo .gz (alias de `gzip -d`).',
        gitbash: 'ok',
        patron: 'gunzip [opciones] ARCHIVO.gz...',
        ejemplos: [
          { comando: 'gunzip log.txt.gz',       explicacion: 'Recupera log.txt y borra log.txt.gz.',           compat_notes: null },
          { comando: 'gunzip -k log.txt.gz',    explicacion: 'Descomprime manteniendo el .gz.',                compat_notes: null },
          { comando: 'gunzip -c log.txt.gz | head', explicacion: '-c imprime a stdout sin tocar el archivo.', compat_notes: null }
        ],
        flags: [
          { flag: '-k', descripcion: 'Keep: mantiene el .gz original.',                  compat_notes: null },
          { flag: '-c', descripcion: 'Salida a stdout (no toca el archivo).',           compat_notes: null }
        ]
      }
      ,{
        id: 'zip',
        tipo: 'comando', nombre: 'zip', categoria: 'compresion', dificultad: 'basico',
        descripcion: 'Empaqueta y comprime archivos en formato ZIP (portable a Windows/Mac).',
        gitbash: 'ok',
        patron: 'zip [opciones] ARCHIVO.zip ARCHIVOS...',
        ejemplos: [
          { comando: 'zip backup.zip a.txt b.txt',         explicacion: 'Crea backup.zip con dos archivos.',                                          compat_notes: null },
          { comando: 'zip -r proyecto.zip proyecto/',       explicacion: 'Empaqueta recursivamente todo un directorio.',                              compat_notes: null },
          { comando: "zip -r out.zip src/ -x 'src/node_modules/*'", explicacion: 'Empaqueta src/ excluyendo node_modules.',                            compat_notes: null }
        ],
        flags: [
          { flag: '-r',         descripcion: 'Recursivo en directorios.',             compat_notes: null },
          { flag: '-x PATRON',  descripcion: 'Excluye archivos que matcheen PATRON.', compat_notes: null },
          { flag: '-9',         descripcion: 'Máxima compresión.',                     compat_notes: null },
          { flag: '-e',         descripcion: 'Cifra el ZIP con contraseña (preguntará).', compat_notes: null }
        ]
      }
      ,{
        id: 'unzip',
        tipo: 'comando', nombre: 'unzip', categoria: 'compresion', dificultad: 'basico',
        descripcion: 'Extrae el contenido de un archivo ZIP.',
        gitbash: 'ok',
        patron: 'unzip [opciones] ARCHIVO.zip [-d DIR]',
        ejemplos: [
          { comando: 'unzip backup.zip',           explicacion: 'Extrae backup.zip en el cwd.',                                            compat_notes: null },
          { comando: 'unzip backup.zip -d /tmp/',  explicacion: 'Extrae en /tmp/ en lugar del cwd.',                                       compat_notes: null },
          { comando: 'unzip -l backup.zip',        explicacion: 'Lista el contenido sin extraer.',                                         compat_notes: null }
        ],
        flags: [
          { flag: '-d DIR', descripcion: 'Directorio destino para la extracción.',     compat_notes: null },
          { flag: '-l',     descripcion: 'Lista el contenido sin extraer.',            compat_notes: null },
          { flag: '-o',     descripcion: 'Sobrescribe sin preguntar.',                  compat_notes: null },
          { flag: '-n',     descripcion: 'Nunca sobrescribe (omite si existe).',        compat_notes: null }
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 41 tarjetas. Filtrar Categoría = "Permisos" → chmod + chown + 8 operadores = 10. Filtrar Categoría = "Compresión" → tar + gzip + gunzip + zip + unzip + 8 operadores = 13. `chown` con punto amarillo (caveats); el resto verdes.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar comandos básicos de permisos (chown) y compresión (gzip, gunzip, zip, unzip)"
```

---

### Task 9: Cargar 3 operadores nuevos (`<`, `tee`, `&>`)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar las 3 tarjetas de operadores tras `unzip` (sin `categoria` — son transversales)**

```js
      ,{
        id: 'op-redirect-stdin',
        tipo: 'operador', nombre: '<', dificultad: 'basico',
        descripcion: 'Input redirection: alimenta stdin de un comando desde un archivo.',
        gitbash: 'ok',
        modelo_mental: 'Pareja conceptual de `>`. Toma el contenido del archivo de la derecha y lo entrega como stdin al comando de la izquierda. Equivalente a `cat archivo | cmd`, pero más eficiente y sin "useless cat". Útil con comandos que leen de stdin pero no aceptan un archivo como argumento.',
        ejemplos: [
          { comando: 'tr a-z A-Z < notas.txt',         explicacion: 'Pasa el contenido del archivo a tr, que pasa todo a mayúsculas.',     compat_notes: null },
          { comando: 'mysql -u root mydb < dump.sql',  explicacion: 'Ejecuta el script SQL en mysql leyendo desde el archivo.',           compat_notes: null },
          { comando: "while read line; do echo \"-> $line\"; done < input.txt", explicacion: 'Bucle leyendo línea a línea desde un archivo.', compat_notes: null }
        ],
        gotchas: [
          'Distinto de `>`: `<` lee, `>` escribe. La flecha indica la dirección del flujo (entra al comando vs sale del comando).',
          'No funciona con todos los comandos: solo los que leen de stdin (no los que esperan un nombre de archivo como argumento).',
          'Heredoc `<<` y here-string `<<<` son parientes: `cmd <<< "string"` pasa "string" como stdin sin necesidad de archivo.'
        ]
      }
      ,{
        id: 'op-tee',
        tipo: 'operador', nombre: 'tee', dificultad: 'intermedio',
        descripcion: 'Bifurca stdin: lo escribe a uno o más archivos Y lo pasa también a stdout.',
        gitbash: 'ok',
        modelo_mental: 'tee es un comando real, pero su uso primario es transversal en pipelines: `cmd | tee archivo | siguiente`. La salida de cmd se guarda en `archivo` y se sigue propagando por el pipe a `siguiente`. Letra "T" porque parte el flujo en dos. Útil para inspeccionar/loggear pasos intermedios sin romper la cadena.',
        ejemplos: [
          { comando: "ls | tee listado.txt",                  explicacion: 'Imprime ls por pantalla Y guarda el listado en listado.txt.', compat_notes: null },
          { comando: "make 2>&1 | tee build.log",             explicacion: 'Compila viendo la salida en tiempo real y guardándola.',     compat_notes: null },
          { comando: "echo 'datos' | sudo tee /etc/conf",     explicacion: 'Truco para escribir en archivo protegido (sudo no afecta a la redirección directa).', compat_notes: null },
          { comando: "ls | tee -a log.txt | grep '\\.txt'",   explicacion: '-a (append): añade al log en lugar de sobrescribir.',          compat_notes: null }
        ],
        gotchas: [
          'tee SOBRESCRIBE el archivo destino salvo que uses -a (append).',
          'Es la solución canónica para `sudo cmd > /etc/archivo` que falla porque la redirección la hace el shell sin sudo: `cmd | sudo tee /etc/archivo`.',
          'Diferencia con xargs: xargs TRANSFORMA stdin en argumentos del comando; tee BIFURCA el stream sin tocarlo.'
        ]
      }
      ,{
        id: 'op-redirect-all',
        tipo: 'operador', nombre: '&>', dificultad: 'intermedio',
        descripcion: 'Atajo de bash para `> archivo 2>&1` — redirige stdout y stderr al mismo archivo.',
        gitbash: 'ok',
        modelo_mental: 'Es **sintaxis de bash**, no un binario. Equivale exactamente a `> ARCHIVO 2>&1` pero más corto. Si el archivo existe, lo sobrescribe; usa `&>>` para append. Solo funciona en bash/zsh — sh y dash no lo entienden.',
        ejemplos: [
          { comando: 'cmd &> all.log',           explicacion: 'Stdout y stderr al archivo all.log (sobrescribe).',         compat_notes: null },
          { comando: 'cmd &>> all.log',           explicacion: 'Append: stdout y stderr al final de all.log.',              compat_notes: null },
          { comando: 'cmd &> /dev/null',          explicacion: 'Descarta toda la salida (silencio total).',                  compat_notes: null }
        ],
        gotchas: [
          '`&>` es bashism: en sh/dash falla. Para portabilidad POSIX usar `> archivo 2>&1` (en ese orden).',
          'Distinto de `>&`: `>&` redirige a un descriptor (`2>&1` = "stderr al destino actual de stdout"). `&>` redirige todo a un archivo.',
          'En Windows/Git Bash funciona porque Git Bash usa bash; en `cmd.exe` o PowerShell la sintaxis es distinta.'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 44 tarjetas. Filtrar Tipo = "Operador" → 11 tarjetas (8 de iter 1 + 3 nuevos). Filtrar Categoría = "Manipulación de archivos" → 7 comandos manipulación + 11 operadores transversales + recetas = al menos 18 cuando todo esté cargado, ahora mismo solo 18 si las recetas aún no están (7 comandos + 11 operadores = 18). Los nuevos operadores aparecen en TODAS las categorías (regla §5.2 mantenida).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar operadores nuevos (input redirect, tee, redirect-all bash)"
```

---

### Task 10: Cargar 5 recetas

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Insertar las 5 recetas tras los operadores nuevos**

```js
      ,{
        id: 'receta-backup-antes-sobrescribir',
        tipo: 'receta', nombre: 'Copiar/mover sin perder el destino existente', categoria: 'manipulacion-archivos', dificultad: 'basico',
        descripcion: 'Hace backup automático del destino antes de sobrescribirlo.',
        gitbash: 'ok',
        escenario: 'Voy a sobrescribir un archivo de configuración pero quiero conservar la versión anterior por si rompo algo.',
        ejemplos: [
          { comando: 'cp --backup=numbered nueva.conf vieja.conf', explicacion: 'Si vieja.conf existe, se renombra a vieja.conf.~1~ antes de la sobrescritura.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'cp',                  hace: 'comando de copiar.' },
          { fragmento: '--backup=numbered',   hace: 'antes de sobrescribir, renombra el destino con sufijo .~N~ (1, 2, 3...).' },
          { fragmento: 'nueva.conf',          hace: 'archivo origen.' },
          { fragmento: 'vieja.conf',          hace: 'destino (será sobrescrito; su versión actual queda como vieja.conf.~1~).' }
        ],
        relacionados: ['cp', 'mv']
      }
      ,{
        id: 'receta-comprimir-excluyendo',
        tipo: 'receta', nombre: 'Comprimir un proyecto excluyendo basura', categoria: 'compresion', dificultad: 'intermedio',
        descripcion: 'Crea un tarball de un proyecto saltándose node_modules, .git y otros directorios pesados.',
        gitbash: 'ok',
        escenario: 'Quiero enviar mi proyecto por email pero sin las dependencias ni el historial de git.',
        ejemplos: [
          { comando: "tar czf proyecto.tar.gz --exclude='node_modules' --exclude='.git' --exclude='dist' proyecto/", explicacion: 'Tarball comprimido del proyecto/ excluyendo 3 directorios.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'tar czf proyecto.tar.gz', hace: 'crea (c) tarball comprimido (z) en el archivo (f) proyecto.tar.gz.' },
          { fragmento: "--exclude='node_modules'", hace: 'salta cualquier directorio llamado node_modules.' },
          { fragmento: "--exclude='.git'",         hace: 'salta el directorio .git.' },
          { fragmento: "--exclude='dist'",         hace: 'salta el directorio dist.' },
          { fragmento: 'proyecto/',                hace: 'directorio raíz a empaquetar (las exclusiones DEBEN ir antes de él).' }
        ],
        relacionados: ['tar'],
        gotchas: [
          'Las --exclude deben ir antes del directorio para que tar las aplique.',
          'Si el destino debe ser portable a Windows, usa `zip -r out.zip proyecto/ -x "proyecto/node_modules/*"` en lugar de tar.'
        ]
      }
      ,{
        id: 'receta-script-ejecutable',
        tipo: 'receta', nombre: 'Volver ejecutable un script .sh', categoria: 'permisos', dificultad: 'basico',
        descripcion: 'Da permiso de ejecución al usuario para poder lanzar un script con ./',
        gitbash: 'ok',
        escenario: 'He escrito un script.sh y al hacer ./script.sh el shell dice "Permission denied".',
        ejemplos: [
          { comando: 'chmod u+x script.sh && ./script.sh', explicacion: 'Añade ejecución para el dueño y lanza el script.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'chmod',     hace: 'cambia permisos.' },
          { fragmento: 'u+x',        hace: 'al usuario (u) le añade (+) ejecución (x). Sin afectar grupo/otros.' },
          { fragmento: 'script.sh',  hace: 'archivo a modificar.' },
          { fragmento: '&&',         hace: 'solo continúa si chmod tuvo éxito.' },
          { fragmento: './script.sh', hace: 'ejecuta el script (./ es necesario porque "." no suele estar en PATH).' }
        ],
        relacionados: ['chmod', 'op-and'],
        gotchas: [
          'Si el script empieza con un shebang correcto (`#!/usr/bin/env bash`) pero no tiene +x, también puedes lanzarlo con `bash script.sh` (no requiere permisos de ejecución).',
          'En Git Bash sobre Windows, +x permite el ./ en Git Bash, pero no convierte el .sh en ejecutable nativo de Windows.'
        ]
      }
      ,{
        id: 'receta-renombrar-masivo',
        tipo: 'receta', nombre: 'Renombrar archivos en masa con un patrón', categoria: 'manipulacion-archivos', dificultad: 'intermedio',
        descripcion: 'Aplica un patrón de renombrado a todos los matches de find usando xargs y sustitución de placeholder.',
        gitbash: 'ok',
        escenario: 'Tengo muchos archivos con extensión .bak y quiero archivarlos cambiando la extensión a .old.',
        ejemplos: [
          { comando: "find . -name '*.bak' | xargs -I {} mv {} {}.old", explicacion: 'Por cada .bak encontrado, lo renombra añadiéndole .old al final.', compat_notes: null }
        ],
        desglose: [
          { fragmento: "find . -name '*.bak'", hace: 'lista todos los archivos .bak desde el directorio actual.' },
          { fragmento: '|',                    hace: 'pasa la lista por stdin a xargs.' },
          { fragmento: 'xargs -I {}',          hace: 'por cada línea, sustituye {} por la línea recibida.' },
          { fragmento: 'mv {} {}.old',         hace: 'mueve cada archivo a su mismo nombre con .old añadido.' }
        ],
        relacionados: ['find', 'xargs', 'mv', 'op-pipe'],
        gotchas: [
          'No funciona bien con nombres de archivo con espacios. Para robustez: `find . -name "*.bak" -print0 | xargs -0 -I {} mv {} {}.old`.',
          'Para sustituir partes del nombre (ej. cambiar .JPG por .jpg), `mv {} {}.old` no sirve — usa un bucle: `for f in *.JPG; do mv "$f" "${f%.JPG}.jpg"; done`. O instala el comando `rename` (Perl).'
        ]
      }
      ,{
        id: 'receta-borrar-antiguos-confirmacion',
        tipo: 'receta', nombre: 'Borrar archivos antiguos con confirmación', categoria: 'manipulacion-archivos', dificultad: 'intermedio',
        descripcion: 'Localiza archivos modificados hace más de N días y los borra interactivamente.',
        gitbash: 'ok',
        escenario: 'Quiero limpiar logs antiguos pero quiero ver cada uno antes de borrarlo (por si hay algo que no debería ir).',
        ejemplos: [
          { comando: 'find /var/log -type f -mtime +30 -exec rm -i {} \\;', explicacion: 'Por cada archivo regular en /var/log con mtime > 30 días, ejecuta rm -i (pide confirmación).', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'find /var/log',        hace: 'busca desde /var/log.' },
          { fragmento: '-type f',              hace: 'solo archivos regulares (excluye directorios).' },
          { fragmento: '-mtime +30',           hace: 'modificados hace MÁS de 30 días (signo + = más antiguos que N).' },
          { fragmento: '-exec rm -i {} \\;',   hace: 'por cada match, ejecuta rm -i con la ruta sustituida en {}. \\; cierra la acción exec.' }
        ],
        relacionados: ['find', 'rm'],
        gotchas: [
          '`-exec ... {} \\;` ejecuta rm UNA vez por archivo (lento si hay muchos). Para agrupar: `-exec rm -i {} +` invoca rm con varios archivos por llamada — pero pierde el aspecto interactivo si lo combinas con muchas confirmaciones.',
          'Si te equivocaste con +30 (debería ser +N días), prueba antes con `-print` en lugar de `-exec rm` para ver qué va a borrar.',
          'Para una alternativa moderna sin riesgo de tipos: `find /var/log -type f -mtime +30 -delete` (sin confirmación; revisar antes con -print).'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 49 tarjetas en total. Filtrar Tipo = "Receta" → 11 tarjetas (6 de iter 1 + 5 nuevas). Sin filtros → 49 tarjetas (igual que el total previsto en spec-iter2 §8: 27 cmd + 11 op + 11 recetas = 49).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "Cargar 5 recetas de manipulación, compresión y permisos (§4.4 spec-iter2)"
```

---

### Task 11: QA pass + checklist de aceptación de iter 2

**Files:**
- Manual: validación visual completa.
- Modify: `guia-bash/guia-bash.html` solo si surge algún fallo (en cuyo caso, commit final del ajuste).

- [ ] **Step 1: Recargar y validar el banner de tests**

Banner verde "✓ N tests OK" sigue saliendo (el conteo no cambia respecto a iter 1: 12 al menos). Sin errores rojos en la consola del navegador (F12 → Console).

- [ ] **Step 2: Validar el conteo total**

Sin filtros aplicados, el grid tiene **49 tarjetas**:
- 27 comandos: 13 de iter 1 (find, grep, xargs, ls, pwd, tree, file, which, whereis, type, locate, head, tail) + 14 nuevos (cp, mv, rm, mkdir, rmdir, touch, ln, tar, gzip, gunzip, zip, unzip, chmod, chown).
- 11 operadores: 8 de iter 1 + 3 nuevos.
- 11 recetas: 6 de iter 1 + 5 nuevas.

- [ ] **Step 3: Casos de uso de filtros — verificación manual**

Probar cada combinación y observar el conteo:

| Acción | Conteo esperado | Notas |
|--------|-----------------|-------|
| Sin filtros | 49 | |
| Categoría = Búsqueda de archivos | 8 cmd + 11 op + 3 recetas = 22 | igual que iter 1 ya que no añadimos cmd a esta categoría |
| Categoría = Búsqueda de contenido | 3 cmd + 11 op + 3 recetas = 17 | |
| Categoría = Manipulación de archivos | 7 cmd (cp, mv, rm, mkdir, rmdir, touch, ln) + 11 op + 3 recetas (backup, renombrado, borrar antiguos) = 21 | |
| Categoría = Compresión | 5 cmd (tar, gzip, gunzip, zip, unzip) + 11 op + 1 receta (comprimir excluyendo) = 17 | |
| Categoría = Permisos | 2 cmd (chmod, chown) + 11 op + 1 receta (script ejecutable) = 14 | |
| Tipo = Comando | 27 | |
| Tipo = Operador | 11 | |
| Tipo = Receta | 11 | |
| Compatible Git Bash (checkbox) | 47 | excluye solo whereis y locate (los únicos `gitbash: 'no-disponible'`) |
| Tipo = Comando + Compatible Git Bash | 25 | quita whereis y locate |
| Búsqueda libre = "tar" | varios (tar, receta-comprimir-excluyendo, etc.) | verificar que ninguno revienta y que el botón copiar funciona en todos |
| Búsqueda libre = "permis" | chmod, chown, recetas que mencionan permisos | |
| Búsqueda + Categoría + Tipo combinados | depende | aplica AND |

- [ ] **Step 4: Casos de uso de las tarjetas nuevas**

- Tarjetas gordas nuevas (`cp`, `mv`, `rm`, `chmod`, `tar`) → "Ver más ▾" funciona (expande/colapsa).
- Tarjeta `chmod` con punto **amarillo** (caveats) — hover sobre el punto muestra tooltip "Git Bash: existe con caveats…".
- Tarjeta `ln` con punto amarillo, ejemplo de hard link con compat_notes amarillo.
- Tarjeta `chown` con punto amarillo.
- Tarjetas `cp -p` flag con compat_notes amarillo (Windows semi-decorativo).
- Hover sobre cualquier `<pre>` de las tarjetas nuevas → aparece botón "📋"; click → "✓ copiado" verde 1.5s; pegar en notepad/terminal → comando se pega correctamente.
- Operador `tee` aparece con badge violeta "operador", **no** badge "comando" (clasificación deliberada del spec-iter2 §4.3).

- [ ] **Step 5: Comprobar que el archivo abre sin servidor**

Cerrar el navegador. Doble click sobre `guia-bash.html`. **Esperado:** se abre, todo funciona, copy button funciona (con fallback si Firefox).

- [ ] **Step 6: Si todo OK y no se modificó nada en este Task 11**

No hace falta commit. Si sí se ajustó algo durante el QA:

```bash
git -C C:/Users/Marius/Desktop/Programacion/VecindApp add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/VecindApp commit -m "QA pass iteración 2 — ajustes finales"
```

---

## Self-review tras escribir el plan

- **Cobertura del spec-iter2:**
  - §1 Objetivo (añadir manipulación/compresión/permisos) → cubierto en Tasks 2–10.
  - §2 Alcance (14 cmd + 3 op + 5 recetas = 22 tarjetas) → Tasks 2–10 entregan exactamente esto.
  - §3.1 Nuevas categorías (3 nuevas options) → Task 1.
  - §3.2 Resto del UX sin cambios → respetado (no se tocan filtros distintos al de categoría, ni la lógica de `applyFilters`).
  - §4.1 Gordas (cp, mv, rm, chmod, tar) → Tasks 2–6, cada una con `modelo_mental` + flags densos + `combos` + `gotchas` + `cuando_no_usar`.
  - §4.2 Básicas (mkdir, rmdir, touch, ln, chown, gzip, gunzip, zip, unzip) → Tasks 7–8, sin `modelo_mental` ni `cuando_no_usar` (campos opcionales, omitidos por convención de "básica").
  - §4.3 Operadores transversales (sin categoria) → Task 9, cada uno con `modelo_mental` y `gotchas`. `tee` clasificado como operador deliberadamente.
  - §4.4 Recetas (5) → Task 10, cada una con `escenario` + `desglose` + categoría según tabla del spec.
  - §5 Schema sin cambios → respetado.
  - §6 ID conventions → respetadas (`cp`, `mv`, ..., `op-redirect-stdin`, `op-tee`, `op-redirect-all`, `receta-...`).
  - §7 NO incluye → respetado (no se añaden rsync, dd, sudo, stat, sed, awk, etc.).
  - §8 Estado tras iter 2 (49 tarjetas: 27+11+11) → verificado en Task 11 step 2.
- **Placeholders:** ninguno. Cada tarea tiene su contenido completo (no "TODO", no "implement later", no "similar a Task N").
- **Type consistency:** `gitbash` siempre uno de los 3 valores (`ok`, `caveats`, `no-disponible`); `tipo` siempre `comando` / `operador` / `receta`; `categoria` siempre uno de los 5 valores nuevos o ausente para operadores; estructura de `ejemplos` y `flags` con los mismos campos que iter 1.
- **Coherencia de IDs:** todos los IDs usados en este plan (`cp`, `mv`, `rm`, `mkdir`, `rmdir`, `touch`, `ln`, `tar`, `gzip`, `gunzip`, `zip`, `unzip`, `chmod`, `chown`, `op-redirect-stdin`, `op-tee`, `op-redirect-all`, `receta-backup-antes-sobrescribir`, `receta-comprimir-excluyendo`, `receta-script-ejecutable`, `receta-renombrar-masivo`, `receta-borrar-antiguos-confirmacion`) son únicos y no chocan con los de iter 1.
- **Mensajes de commit:** todos en español, imperativo presente, sin línea `Co-Authored-By` (cumple memoria del usuario).

---

## Plan complete and saved

Plan guardado en `C:\Users\Marius\Desktop\Programacion\VecindApp\guia-bash\plans\2026-04-29-iteracion-2-manipulacion.md`.

**Dos opciones de ejecución:**

1. **Subagent-Driven (recommended)** — dispatch de un subagent fresh por tarea, revisión entre tareas, iteración rápida.
2. **Inline Execution** — ejecutar las tareas en esta misma sesión usando `executing-plans`, ejecución por lotes con checkpoints de revisión.

Pendiente: elegir aproximación.
