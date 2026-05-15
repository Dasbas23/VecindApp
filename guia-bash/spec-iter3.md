# Spec — Iteración 3: Bash completo (scripting)

> Este spec extiende `spec.md` (commit `d5bead9`) y `spec-iter2.md`. Para
> arquitectura, stack, schema JSON, prohibiciones y reglas de UX, ver el spec
> original. Aquí solo se documenta lo NUEVO de la iteración 3.

---

## 1. Objetivo

Añadir al mismo `guia-bash.html` las tarjetas de `tipo: "sintaxis"` que cubren
el "bash de scripting": variables y expansion, control de flujo, funciones,
parámetros posicionales, tests, here-docs y arrays. Mismo formato monolítico,
mismo schema, mismo UX. Tras iter 3 la guía cubre el día a día completo de
quien escribe scripts en Git Bash / Linux.

## 2. Alcance

| Bloque | Constructos / builtins |
|---|---|
| Variables | `=` (asignación), `$VAR`, `${VAR}`, parameter expansion, `export`, `read`, arrays |
| Control de flujo | `if/elif/else`, `case`, `for`, `while`, `until`, tests `[ ]` vs `[[ ]]` |
| Funciones y scripting | funciones, `alias`, `#!` (shebang), variables especiales (`$@`, `$*`, `$#`, `$?`, `$$`, `$!`, `$0`, `$1..$9`, `${10}`), here-docs (`<<EOF`, `<<-EOF`, `<<<`) |
| Recetas | 5 escenarios combinando lo anterior |

**Total nuevo:** 15 tarjetas de `tipo: "sintaxis"` + 5 recetas = **20 tarjetas**.

## 3. Cambios en la UX

### 3.1 Nuevas categorías

Añadir **3 `<option>`** al `<select id="filter-categoria">`:

```html
<option value="variables">Variables</option>
<option value="control-flujo">Control de flujo</option>
<option value="funciones-y-scripting">Funciones y scripting</option>
```

Las 5 categorías existentes (`busqueda-archivos`, `busqueda-contenido`,
`manipulacion-archivos`, `compresion`, `permisos`) se mantienen.
**Total de categorías tras iter 3: 8** (más "Todas").

### 3.2 Header

- `<title>`: `Guía Bash`
- `<h1>`: `Guía Bash`
- Subtítulo: `Iteraciones 1–3: búsqueda, manipulación, scripting completo.`

### 3.3 Resto del UX

**Sin cambios.**
- Filtros `dificultad`, `tipo`, `gitbash`, `search` ya soportan los nuevos valores.
- Render por tipo (`comando`, `operador`, `receta`, `sintaxis`) ya cubre todos
  los casos. El badge `sintaxis` está mapeado a ámbar (`bg-amber-100
  text-amber-800`) en `renderTipoBadge` desde iter 1.
- Indicador `gitbash` (verde / ámbar / rojo) y botón copiar funcionan tal cual.
- La regla "transversales sin categoria pasan siempre" sigue aplicando — pero
  en iter 3 todas las tarjetas llevan `categoria` (no son transversales), así
  que esa regla no se ejercita aquí.

## 4. Schema — clarificación, sin cambios estructurales

El schema definido en `spec.md` §6 acomoda todo. **No se añaden campos.**

### 4.1 Clarificación sobre `flags` en `sintaxis`

`spec.md` §11 indicaba como "probable" que las tarjetas de `sintaxis` no
llevaran `flags`. Esta iteración relaja ese probable: **se permiten `flags`
en tarjetas `sintaxis` cuando aplique** (en concreto `read -p/-r/-s/-t/-n`,
`alias -p`, `declare -A` para arrays asociativos).

Razón: omitir flags en `read` o `alias` empobrece la guía sin ganancia
estructural. La inconsistencia con la nota original es deliberada.

Resto de campos opcionales (`modelo_mental`, `ejemplos`, `combos`, `gotchas`,
`relacionados`, `cuando_no_usar`) se usan según profundidad de tarjeta — mismo
criterio que comandos (§6.1).

## 5. Profundidad por tarjeta

### 5.1 Gordas (10)

Llevan `modelo_mental`, `ejemplos`, `flags` (cuando aplica), `combos`,
`gotchas`, `relacionados`, `cuando_no_usar`:

| ID | Categoría | Razón |
|---|---|---|
| `sx-variables` | variables | Parameter expansion es densa: `${V:-x}`, `${V##*/}`, `${V/foo/bar}`, length `${#V}`, slice `${V:0:3}` |
| `sx-read` | variables | Flags importantes (`-p/-r/-s/-t/-n`); el patrón `while read` es la fuente clásica de bugs |
| `sx-arrays` | variables | Sintaxis confusa, `[@]` vs `[*]` con quoting, asociativos requieren `declare -A` |
| `sx-if` | control-flujo | Combinación con `[ ]`/`[[ ]]`, exit codes, anidación, `elif` |
| `sx-for` | control-flujo | 3 formas (C-style, `in lista`, sequence `{1..10}`), comportamiento con globs |
| `sx-while` | control-flujo | Patrón `while [ ]`, patrón `while read` (con sus gotchas de stdin) |
| `sx-tests` | control-flujo | Comparativa `[ ]` vs `[[ ]]` — la decisión clave: cuándo usar cuál y por qué |
| `sx-functions` | funciones-y-scripting | Definición, args (`$1..$@`), `return` vs `echo`, `local`, scope |
| `sx-special-vars` | funciones-y-scripting | `$@` vs `$*` (con/sin comillas) es la trampa clásica; `$?`, `$$`, `$!` |
| `sx-heredoc` | funciones-y-scripting | `<<EOF`, `<<-EOF` (strip tabs), `<<<` here-string, `'EOF'` para literal |

### 5.2 Básicas (5)

Solo `descripcion` + `ejemplos` + `gotchas` mínimos. Sin `modelo_mental` ni
`cuando_no_usar`:

| ID | Categoría |
|---|---|
| `sx-export` | variables |
| `sx-case` | control-flujo |
| `sx-until` | control-flujo |
| `sx-alias` | funciones-y-scripting |
| `sx-shebang` | funciones-y-scripting |

Notas:
- `sx-case` mantiene profundidad básica aunque tenga matices (`;&`, `;;&`) —
  caben en `gotchas`.
- `sx-until` es espejo de `while` con condición invertida; tarjeta corta.
- `sx-shebang` cubre `#!/usr/bin/env bash` vs `#!/bin/bash` y por qué la
  primera es preferida; cabe como básica.

### 5.3 Recetas (5)

Todas con `tipo: "receta"` y `escenario` + `desglose`. Categoría según el
contexto principal:

| ID | Categoría | Escenario |
|---|---|---|
| `receta-leer-archivo-linea` | variables | Leer un archivo línea a línea robusto: `while IFS= read -r line; do ... done < f` |
| `receta-iterar-archivos` | control-flujo | `for f in *.txt; do ... done` con caveats (glob vacío, `nullglob`) |
| `receta-validar-args` | funciones-y-scripting | Comprobar `$#` y abortar con mensaje de uso |
| `receta-funcion-return` | funciones-y-scripting | Función que devuelve éxito/fallo y se usa en `if` |
| `receta-heredoc-config` | funciones-y-scripting | Generar archivo de configuración con variables expandidas |

## 6. ID conventions (para iter 3)

Coherente con `spec.md` §6 (sufijo del schema):

- **Sintaxis:** prefijo `sx-`:
  - `sx-variables`, `sx-export`, `sx-read`, `sx-arrays`
  - `sx-if`, `sx-case`, `sx-for`, `sx-while`, `sx-until`, `sx-tests`
  - `sx-functions`, `sx-alias`, `sx-shebang`, `sx-special-vars`, `sx-heredoc`

- **Recetas:** prefijo `receta-` (los IDs ya listados en §5.3).

## 7. Lo que NO incluye iter 3

- **`getopts`** (parsing avanzado de flags `-x`, `--long`): fuera de alcance.
  Queda como iteración futura si surge la necesidad.
- **`set -euo pipefail`, `trap`, `shopt`**: opciones de shell para
  safety/debugging. Quedan fuera; iter 3 cubre la sintaxis del lenguaje, no la
  configuración del shell.
- **Backticks `` `cmd` ``**: deprecados frente a `$()`. **NO** se crea tarjeta
  propia. Solo se mencionan como **gotcha** dentro de la tarjeta del operador
  `$( )` (`op-cmd-substitution`) ya existente: nota de que existen pero no se
  recomiendan para código nuevo.
- **Operadores nuevos**: `<<` y `<<<` (here-doc / here-string) NO entran como
  `tipo: "operador"`. Viven dentro de la tarjeta `sx-heredoc`, donde su
  contexto es más útil. Misma decisión que iter 2 con `tee`.
- **Globbing avanzado**: `shopt -s globstar` (`**`), `nullglob`, `dotglob`,
  `extglob`. Solo se mencionan como **gotchas** dentro de `sx-for` y
  `receta-iterar-archivos` cuando son relevantes.
- **`mapfile` / `readarray`**: builtins para leer archivos en arrays. Mencionar
  como alternativa moderna en `gotchas` de `receta-leer-archivo-linea`, sin
  tarjeta propia.
- **`select`** (menús interactivos en bash): poco usado, fuera de alcance.
- **Subshells `()` vs grupos `{ }`**: matiz interesante pero no central. Mencionar
  como gotcha en `sx-functions` o `sx-variables` si surge.

## 8. Estado del catálogo tras iter 3

| Tipo | Iter 1+2 | + Iter 3 | Total |
|---|---|---|---|
| Comandos | 27 | +0 | 27 |
| Operadores | 11 | +0 | 11 |
| Recetas | 11 | +5 | 16 |
| Sintaxis | 0 | +15 | 15 |
| **Total** | **49** | **+20** | **69** |

## 9. Decisiones tomadas en brainstorming (2026-05-09)

| # | Pregunta | Respuesta |
|---|---|---|
| Q1 | Granularidad de tarjetas (atómica / familias / lo más fino) | **Atómica con agrupaciones lógicas**. Una tarjeta por constructo principal, pero familias estrechas agrupadas (`$VAR`/`${VAR}`/expansion en una sola tarjeta `sx-variables`; `$@`/`$*`/`$1..`/`$#`/`$?` en una sola `sx-special-vars`; `[ ]` y `[[ ]]` comparados en una sola `sx-tests`). |
| Q2 | Clasificación de builtins reales (`export`, `read`, `alias`) | **Como `sintaxis`**. Coherente con la intención de iter 3 ("todo lo de scripting") y precedente de `tee` clasificado como operador en iter 2. Implica permitir `flags` en `sintaxis` cuando aplique. |
| Q3 | Categorías nuevas (3 / 1 / sin categoría) | **3 nuevas**: `variables`, `control-flujo`, `funciones-y-scripting`. Permite filtrar por bloque mental dentro de scripting. Total tras iter 3: 8 categorías. |

---

## 10. Fin

Este spec es la entrada para la skill `writing-plans`, que debe generar el plan
de implementación de iter 3 — un único archivo nuevo de plan en
`plans/2026-05-09-iteracion-3-scripting.md` (siguiendo la convención de iter 1
y iter 2).
