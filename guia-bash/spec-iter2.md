# Spec — Iteración 2: Manipulación de archivos

> Este spec extiende `spec.md` (commit `d5bead9`). Para arquitectura, stack,
> schema JSON, prohibiciones y reglas de UX, ver el spec original. Aquí solo
> se documenta lo NUEVO de la iteración 2.

---

## 1. Objetivo

Añadir al mismo `guia-bash.html` las tarjetas de comandos que **manipulan**
archivos (crear, copiar, mover, borrar, comprimir, cambiar permisos), más los
operadores y recetas asociados. Mismo formato monolítico, mismo schema, mismo UX.

## 2. Alcance

| Bloque | Comandos / operadores |
|---|---|
| Manipulación | `cp`, `mv`, `rm`, `mkdir`, `rmdir`, `touch`, `ln` |
| Compresión   | `tar`, `gzip`, `gunzip`, `zip`, `unzip` |
| Permisos     | `chmod`, `chown` |
| Operadores nuevos | `<` (input), `&>` (bash all-redirect) |
| Comando especial | `tee` — clasificado como `tipo: "operador"` transversal (ver §6) |
| Recetas | 5 escenarios combinando lo anterior |

**Total nuevo:** 14 comandos + 3 operadores + 5 recetas = **22 tarjetas**.

## 3. Cambios en la UX

### 3.1 Nuevas categorías

Añadir **3 `<option>`** al `<select id="filter-categoria">`:

```html
<option value="manipulacion-archivos">Manipulación de archivos</option>
<option value="compresion">Compresión</option>
<option value="permisos">Permisos</option>
```

Las 2 opciones existentes (`busqueda-archivos`, `busqueda-contenido`) se mantienen.
Total de categorías tras iter 2: **5** (más "Todas").

### 3.2 Resto del UX

Sin cambios:
- Filtros (dificultad, tipo, gitbash, search) ya soportan los nuevos valores.
- Render por tipo (`comando`, `operador`, `receta`) ya cubre todos los casos.
- Indicador `gitbash` (verde/ámbar/rojo) y botón copiar funcionan tal cual.
- La regla "transversales sin categoria pasan siempre" sigue aplicando a los
  3 operadores nuevos.

## 4. Profundidad por tarjeta

### 4.1 Gordas (5)

Estos comandos justifican `modelo_mental`, lista exhaustiva de `flags`, `combos`,
`gotchas` y `cuando_no_usar` por su superficie y/o riesgo destructivo:

| Comando | Categoría | Razón |
|---|---|---|
| `cp` | manipulacion-archivos | flags densos (-r, -p, -a, -i, -u, --backup), riesgo de sobrescritura |
| `mv` | manipulacion-archivos | mismo riesgo + uso para renombrar |
| `rm` | manipulacion-archivos | DESTRUCTIVO sin papelera; -rf es la gotcha clásica |
| `chmod` | permisos | doble notación (octal `755` vs simbólica `u+x`) |
| `tar` | compresion | sintaxis críptica (-czf, -xzf, -tvf), múltiples modos |

### 4.2 Básicas (9)

Solo `descripcion` + `ejemplos` + flags principales. Nada de `modelo_mental`
ni `cuando_no_usar`:

`mkdir`, `rmdir`, `touch`, `ln`, `chown`, `gzip`, `gunzip`, `zip`, `unzip`.

Notas:
- `gzip` y `gunzip` son tarjetas **separadas** (consistencia con `head`/`tail` en iter 1).
- `zip` y `unzip` igualmente separadas.
- `ln` cubre tanto hard como simbólico (`-s`); el modelo mental cabe en básico.

### 4.3 Operadores transversales (3)

Sin `categoria`, mismo patrón que los 8 operadores de iter 1:

| Operador | Descripción breve |
|---|---|
| `<` | Input redirection: alimenta stdin desde un archivo. Pareja conceptual de `>`. |
| `tee` | Comando real, pero clasificado aquí como operador porque su uso primario es fan-out en pipelines (`cmd | tee out.txt | next`). Inconsistencia deliberada con `xargs` (que se clasificó como comando): la diferencia es que `xargs` **transforma** la entrada, mientras que `tee` solo la **bifurca**. |
| `&>` | Bash shortcut para `> file 2>&1`. Es sintaxis del shell, no un binario. |

### 4.4 Recetas (5)

Todas con `tipo: "receta"` y `escenario` + `desglose`. Categoría según el
contexto principal:

| ID | Categoría | Escenario |
|---|---|---|
| `receta-backup-antes-sobrescribir` | manipulacion-archivos | Copiar/mover sin perder el destino existente |
| `receta-comprimir-excluyendo` | compresion | `tar czf` excluyendo `node_modules` |
| `receta-script-ejecutable` | permisos | Volver ejecutable un `.sh` para el usuario |
| `receta-renombrar-masivo` | manipulacion-archivos | `find` + `xargs -I {} mv {}` para renombrar muchos |
| `receta-borrar-antiguos-confirmacion` | manipulacion-archivos | `find -mtime +N -exec rm -i {} \;` |

## 5. Schema JSON

**Sin cambios.** El schema definido en `spec.md` §6 acomoda todo:
- Comandos destructivos van vía `gotchas` + `cuando_no_usar`, sin nuevo campo.
- Doble notación de `chmod` cabe en `modelo_mental`.
- Ningún campo nuevo. Ningún tipo nuevo. (Tipo `sintaxis` queda reservado para iter 3.)

## 6. ID conventions (para iter 2)

Coherente con `spec.md` §6 (sufijo del schema):

- **Comandos:** id = nombre del comando tal cual.
  Ejemplos: `cp`, `mv`, `rm`, `mkdir`, `rmdir`, `touch`, `ln`, `tar`, `gzip`,
  `gunzip`, `zip`, `unzip`, `chmod`, `chown`.

- **Operadores:** prefijo `op-`:
  - `<` → `op-redirect-stdin`
  - `tee` → `op-tee`
  - `&>` → `op-redirect-all`

- **Recetas:** prefijo `receta-` (los IDs ya listados en §4.4).

## 7. Lo que NO incluye iter 2

- **Redirecciones cubiertas en iter 1**: `>`, `>>`, `2>&1`. No se tocan ni amplían.
- **Compresión avanzada**: `cpio`, `dd`, `rsync`, `7z`, `bzip2`/`xz`. Fuera de
  alcance — queda como iteración futura si surge la necesidad.
- **Privilegios**: `sudo`, `su` — no son manipulación de archivos.
- **Inspección de permisos**: `stat`, `getfacl` — quedan fuera; iter 2 cubre
  modificación de permisos, no su inspección.
- **Edición**: `nano`, `vim`, `sed`, `awk`. Iter 2 mueve/copia/borra archivos,
  no edita su contenido.

## 8. Estado del catálogo tras iter 2

| Tipo | Iter 1 | + Iter 2 | Total |
|---|---|---|---|
| Comandos | 13 | +14 | 27 |
| Operadores | 8 | +3 | 11 |
| Recetas | 6 | +5 | 11 |
| **Total** | **27** | **+22** | **49** |

## 9. Decisiones tomadas en brainstorming (2026-04-29)

| # | Pregunta | Respuesta |
|---|---|---|
| Q1 | ¿Una sola categoría nueva, dos, o tres? | **Tres**: `manipulacion-archivos`, `compresion`, `permisos`. Justificación: separar por intención mental clara. |
| Q2 | ¿Profundidad uniforme o mixta? | **Mixta**: gordas para los 5 más complejos/destructivos (`cp`, `mv`, `rm`, `chmod`, `tar`); básicas para los 9 simples. Mismo patrón que iter 1 (`find`/`grep`/`xargs` gordas vs `ls`/`pwd`/etc. básicas). |
| Q3 | Solapamiento de redirecciones | Mantener intactas las de iter 1. Añadir solo `<`, `tee`, `&>` como operadores nuevos. |

---

## 10. Fin

Este spec es la entrada para la skill `writing-plans`, que debe generar el plan
de implementación de iter 2 — un único archivo nuevo de plan en
`plans/2026-04-29-iteracion-2-manipulacion.md` (siguiendo la convención de iter 1).
