# Guía Bash — Iteración 1 (búsqueda) — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir `guia-bash.html` — guía interactiva monolítica de comandos bash con filtros + tarjetas, cargada con el catálogo de búsqueda (archivos, contenido, operadores, recetas) descrito en el spec §8.

**Architecture:** Un único archivo `.html` autocontenido (Tailwind por CDN, JS vanilla embebido, datos `const COMANDOS = [...]` embebido). Render funcional sin frameworks: `state` → `applyFilters(COMANDOS, state)` → `renderAll()`. Las tarjetas se generan a partir de objetos JSON que respetan el contrato fijado en spec §6. Los tests son aserciones inline (`assert()` custom) que corren al cargar la página y muestran fallos como banners rojos + `console.error`.

**Tech Stack:** HTML5 + Tailwind CSS (Play CDN) + JavaScript vanilla (sin build, sin npm, sin frameworks). Todo en un archivo. Compatible con apertura `file://` por doble click.

---

## Pre-requisitos

- **Spec aprobado:** `C:\Users\Marius\Desktop\Programacion\Aprendizaje\guia-bash\spec.md` (commit `d5bead9` en `main`).
- **Repo:** `C:\Users\Marius\Desktop\Programacion\Aprendizaje\` ya es repo git en `main` (origin: `origin/main`).
- **Carpeta de trabajo de cada paso:** `C:\Users\Marius\Desktop\Programacion\Aprendizaje\guia-bash\`.
- **Convención de mensajes de commit:** en español, descriptivos. Sin línea `Co-Authored-By`.
- **El implementador debe leer el spec antes de empezar.** Las tareas de carga de datos referencian las reglas de §6.1 (campos por tipo de tarjeta) y los textos de §7.3 / §8.

---

## File Structure

Creado en este plan:

| Ruta (relativa a `Aprendizaje/`) | Responsabilidad |
|----------------------------------|-----------------|
| `guia-bash/guia-bash.html`       | Único artefacto. UI + lógica + datos + tests. Monolito por diseño (spec §3, §9). |

No se crean tests separados, ni CSS externo, ni JSON de datos. Spec §3 lo prohíbe explícitamente.

### Estructura interna del HTML (sectionado para que tareas posteriores referencien líneas/secciones)

El archivo se organiza con **comentarios de sección** que cada tarea usa como anclaje. Tras Task 1, el archivo tiene este esqueleto y todas las modificaciones posteriores se describen como "añadir dentro de `<!-- ====== JS: RENDER ====== -->`" o equivalente.

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <!-- ====== HEAD ====== -->
</head>
<body>
  <!-- ====== MARKUP: HEADER ====== -->
  <!-- ====== MARKUP: FILTERS ====== -->
  <!-- ====== MARKUP: GRID ====== -->
  <!-- ====== MARKUP: EMPTY STATE ====== -->
  <!-- ====== MARKUP: TEST BANNER (debug) ====== -->

  <script>
    // ====== JS: TEST INFRA ======
    // ====== JS: DATA ======
    // ====== JS: STATE ======
    // ====== JS: FILTER LOGIC ======
    // ====== JS: RENDER ======
    // ====== JS: COPY ======
    // ====== JS: WIRE-UP ======
    // ====== JS: TESTS ======
  </script>
</body>
</html>
```

---

## Estrategia de testing (no hay framework de tests)

- **Aserciones inline** en `// ====== JS: TESTS ======` con un helper `assert(cond, msg)` definido en `// ====== JS: TEST INFRA ======`.
- Cuando una aserción falla:
  - `console.error('FAIL:', msg)` — visible al abrir DevTools.
  - Banner rojo añadido al `<div id="test-banner">` (`<!-- ====== MARKUP: TEST BANNER ====== -->`) — visible incluso sin DevTools.
- Cuando todas pasan, el banner muestra `✓ N tests OK` en verde.
- Cada tarea que introduce lógica añade su bloque de aserciones (IIFE o función con nombre llamada al final del script).

**Cómo "correr los tests":** abrir `guia-bash.html` con doble click en el explorador. Los tests corren automáticamente al cargar.

---

## Convenciones generales para todas las tareas

- **Cwd para ejecutar comandos git:** `C:\Users\Marius\Desktop\Programacion\Aprendizaje\` (usar `git -C` o `cd` ahí). Usar siempre forward slashes en paths para git en Git Bash.
- **Verificación visual:** después de cada cambio que toca UI, abrir el HTML con doble click (o `start guia-bash.html` desde CMD, o `explorer guia-bash.html` desde Git Bash) y comprobar el resultado descrito en el step "Verify".
- **Commit por tarea:** cada tarea termina con un commit. Mensaje en español, en imperativo presente ("Añadir tal cosa", "Implementar tal otra"). Sin línea `Co-Authored-By`.
- **Si una tarea se rompe a mitad:** no se hace commit, se diagnostica el fallo, se corrige y entonces se commitea. Nunca commitear código con tests rojos.

---

## Tasks

### Task 1: Esqueleto HTML + Tailwind + secciones marcadas

**Files:**
- Create: `guia-bash/guia-bash.html`

- [ ] **Step 1: Crear el archivo con el esqueleto base, Tailwind por CDN y todos los marcadores de sección**

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <!-- ====== HEAD ====== -->
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Guía Bash — Búsqueda</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 text-slate-900 min-h-screen">

  <!-- ====== MARKUP: HEADER ====== -->
  <header class="bg-white border-b border-slate-200 sticky top-0 z-10 shadow-sm">
    <div class="max-w-6xl mx-auto px-4 py-4">
      <h1 class="text-2xl font-bold">Guía Bash — Búsqueda</h1>
      <p class="text-sm text-slate-600">Iteración 1: archivos, contenido, operadores, recetas.</p>
    </div>
  </header>

  <!-- ====== MARKUP: FILTERS ====== -->
  <section class="max-w-6xl mx-auto px-4 py-4"></section>

  <!-- ====== MARKUP: GRID ====== -->
  <main id="grid" class="max-w-6xl mx-auto px-4 pb-12 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"></main>

  <!-- ====== MARKUP: EMPTY STATE ====== -->
  <div id="empty-state" class="max-w-6xl mx-auto px-4 py-12 text-center text-slate-500 hidden"></div>

  <!-- ====== MARKUP: TEST BANNER (debug) ====== -->
  <div id="test-banner" class="max-w-6xl mx-auto px-4 py-2 text-sm font-mono"></div>

  <script>
    // ====== JS: TEST INFRA ======

    // ====== JS: DATA ======
    const COMANDOS = [];

    // ====== JS: STATE ======

    // ====== JS: FILTER LOGIC ======

    // ====== JS: RENDER ======

    // ====== JS: COPY ======

    // ====== JS: WIRE-UP ======

    // ====== JS: TESTS ======
  </script>
</body>
</html>
```

- [ ] **Step 2: Verificar visualmente**

Abrir `guia-bash/guia-bash.html` con doble click. **Esperado:** página en blanco con header "Guía Bash — Búsqueda", subtítulo gris debajo, fondo gris claro. Sin errores en consola (F12 → Console).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Crear esqueleto HTML de la guía bash con Tailwind y secciones marcadas"
```

---

### Task 2: Infraestructura de tests (assert + banner)

**Files:**
- Modify: `guia-bash/guia-bash.html` (sección `// ====== JS: TEST INFRA ======`)

- [ ] **Step 1: Añadir el helper `assert` y el banner inicializador**

Insertar dentro del `<script>`, dentro de `// ====== JS: TEST INFRA ======`:

```js
// ====== JS: TEST INFRA ======
const __TEST_RESULTS = { passed: 0, failed: [] };

function assert(cond, msg) {
  if (!cond) {
    __TEST_RESULTS.failed.push(msg);
    console.error('FAIL:', msg);
  } else {
    __TEST_RESULTS.passed++;
  }
}

function renderTestBanner() {
  const banner = document.getElementById('test-banner');
  if (__TEST_RESULTS.failed.length === 0) {
    banner.className = 'max-w-6xl mx-auto px-4 py-2 text-sm font-mono bg-emerald-50 text-emerald-800 border border-emerald-200 rounded mt-2';
    banner.textContent = `✓ ${__TEST_RESULTS.passed} tests OK`;
  } else {
    banner.className = 'max-w-6xl mx-auto px-4 py-2 text-sm font-mono bg-rose-50 text-rose-800 border border-rose-300 rounded mt-2';
    banner.innerHTML = `✗ ${__TEST_RESULTS.failed.length} test(s) FALLAN (${__TEST_RESULTS.passed} OK):<br>` +
      __TEST_RESULTS.failed.map(f => `&nbsp;&nbsp;• ${f}`).join('<br>');
  }
}
```

- [ ] **Step 2: Añadir un test trivial dummy en `// ====== JS: TESTS ======` y la llamada a `renderTestBanner`**

Insertar dentro de `// ====== JS: TESTS ======`:

```js
// ====== JS: TESTS ======
(function runTests() {
  assert(true, 'meta — assert true pasa');
  // Las siguientes tareas añadirán más aserciones aquí.
  renderTestBanner();
})();
```

- [ ] **Step 3: Verificar visualmente**

Recargar `guia-bash.html`. **Esperado:** debajo del grid vacío aparece banner verde "✓ 1 tests OK". Sin errores en consola.

- [ ] **Step 4: Verificar que el banner detecta fallos**

Editar temporalmente el assert a `assert(false, 'comprobando rojo')`. Recargar. **Esperado:** banner rojo "✗ 1 test(s) FALLAN..." con el mensaje. Restaurar a `assert(true, ...)`. Recargar. Verde de nuevo.

- [ ] **Step 5: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Añadir helper assert y banner de tests"
```

---

### Task 3: Markup de filtros (sin lógica)

**Files:**
- Modify: `guia-bash/guia-bash.html` (sección `<!-- ====== MARKUP: FILTERS ====== -->`)

- [ ] **Step 1: Sustituir el `<section>` vacío de FILTERS por los controles**

Reemplazar:

```html
  <!-- ====== MARKUP: FILTERS ====== -->
  <section class="max-w-6xl mx-auto px-4 py-4"></section>
```

por:

```html
  <!-- ====== MARKUP: FILTERS ====== -->
  <section class="max-w-6xl mx-auto px-4 py-4 space-y-3">
    <input
      id="search-input"
      type="search"
      placeholder="Buscar (nombre, descripción, tags)..."
      class="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-sky-500"
    />
    <div class="flex flex-wrap gap-3 items-center">
      <label class="text-sm">
        Categoría:
        <select id="filter-categoria" class="ml-1 border border-slate-300 rounded px-2 py-1 text-sm">
          <option value="all">Todas</option>
          <option value="busqueda-archivos">Búsqueda de archivos</option>
          <option value="busqueda-contenido">Búsqueda de contenido</option>
        </select>
      </label>
      <label class="text-sm">
        Dificultad:
        <select id="filter-dificultad" class="ml-1 border border-slate-300 rounded px-2 py-1 text-sm">
          <option value="all">Todas</option>
          <option value="basico">Básico</option>
          <option value="intermedio">Intermedio</option>
          <option value="avanzado">Avanzado</option>
        </select>
      </label>
      <label class="text-sm">
        Tipo:
        <select id="filter-tipo" class="ml-1 border border-slate-300 rounded px-2 py-1 text-sm">
          <option value="all">Todos</option>
          <option value="comando">Comando</option>
          <option value="operador">Operador</option>
          <option value="receta">Receta</option>
        </select>
      </label>
      <label class="text-sm flex items-center gap-1">
        <input id="filter-gitbash" type="checkbox" class="rounded border-slate-300">
        Compatible con Git Bash
      </label>
      <button id="filter-clear" class="ml-auto text-sm text-sky-700 underline hover:text-sky-900">
        Limpiar filtros
      </button>
    </div>
  </section>
```

- [ ] **Step 2: Verificar visualmente**

Recargar. **Esperado:** input de búsqueda en la primera fila; debajo, dropdowns "Categoría", "Dificultad", "Tipo" + checkbox "Compatible con Git Bash" + botón "Limpiar filtros" alineado a la derecha. Nada hace nada todavía.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Añadir markup de filtros (sin lógica todavía)"
```

---

### Task 4: Estado + función `applyFilters` con tests

**Files:**
- Modify: `guia-bash/guia-bash.html` (`// ====== JS: STATE ======`, `// ====== JS: FILTER LOGIC ======`, `// ====== JS: TESTS ======`)

- [ ] **Step 1: Añadir el state inicial**

Insertar en `// ====== JS: STATE ======`:

```js
// ====== JS: STATE ======
const state = {
  search: '',
  categoria: 'all',
  dificultad: 'all',
  tipo: 'all',
  gitbashOnly: false
};
```

- [ ] **Step 2: Escribir tests de `applyFilters` ANTES de implementarla**

Insertar en `// ====== JS: TESTS ======`, dentro del IIFE, antes de `renderTestBanner()`:

```js
  // --- applyFilters ---
  const SAMPLE = [
    { id: 'a', tipo: 'comando',  nombre: 'a', categoria: 'busqueda-archivos', dificultad: 'basico',     descripcion: 'Hola archivo', gitbash: 'ok',            tags: ['arch'] },
    { id: 'b', tipo: 'comando',  nombre: 'b', categoria: 'busqueda-contenido', dificultad: 'intermedio', descripcion: 'busca dentro', gitbash: 'caveats',       tags: ['cont'] },
    { id: 'c', tipo: 'receta',   nombre: 'c', categoria: 'busqueda-contenido', dificultad: 'avanzado',   descripcion: 'una receta',   gitbash: 'no-disponible', tags: [] },
    { id: 'd', tipo: 'operador', nombre: '|', /* sin categoria */              dificultad: 'basico',     descripcion: 'pipe',         gitbash: 'ok',            tags: ['compose'] },
  ];

  assert(applyFilters(SAMPLE, { search:'', categoria:'all', dificultad:'all', tipo:'all', gitbashOnly:false }).length === 4,
         'applyFilters — sin filtros devuelve todo');
  assert(applyFilters(SAMPLE, { search:'', categoria:'busqueda-archivos', dificultad:'all', tipo:'all', gitbashOnly:false }).length === 2,
         'applyFilters — categoria busqueda-archivos incluye operador transversal');
  assert(applyFilters(SAMPLE, { search:'', categoria:'all', dificultad:'all', tipo:'comando', gitbashOnly:false }).length === 2,
         'applyFilters — tipo comando');
  assert(applyFilters(SAMPLE, { search:'', categoria:'all', dificultad:'basico', tipo:'all', gitbashOnly:false }).length === 2,
         'applyFilters — dificultad basico');
  assert(applyFilters(SAMPLE, { search:'', categoria:'all', dificultad:'all', tipo:'all', gitbashOnly:true }).length === 3,
         'applyFilters — gitbashOnly excluye solo no-disponible');
  assert(applyFilters(SAMPLE, { search:'pipe', categoria:'all', dificultad:'all', tipo:'all', gitbashOnly:false }).length === 1,
         'applyFilters — search en descripcion');
  assert(applyFilters(SAMPLE, { search:'cont', categoria:'all', dificultad:'all', tipo:'all', gitbashOnly:false }).length === 1,
         'applyFilters — search en tags');
  assert(applyFilters(SAMPLE, { search:'', categoria:'busqueda-contenido', dificultad:'all', tipo:'all', gitbashOnly:false }).length === 3,
         'applyFilters — categoria filtra Y mantiene operador transversal');
```

- [ ] **Step 3: Verificar que los tests fallan (la función no existe aún)**

Recargar. **Esperado:** banner rojo con varios fallos del tipo "applyFilters is not defined" en consola. Es lo esperado en este punto.

- [ ] **Step 4: Implementar `applyFilters`**

Insertar en `// ====== JS: FILTER LOGIC ======`:

```js
// ====== JS: FILTER LOGIC ======

/**
 * Aplica el conjunto de filtros del state al array de comandos.
 * Reglas (spec §5.2):
 * - categoria: las tarjetas SIN categoria (transversales, p.ej. operadores) se muestran SIEMPRE.
 * - tipo: filtra exactamente.
 * - dificultad: filtra exactamente.
 * - gitbashOnly: incluye 'ok' y 'caveats', excluye solo 'no-disponible'.
 * - search: match case-insensitive en nombre, descripcion, tags (si existen).
 */
function applyFilters(comandos, s) {
  const q = (s.search || '').trim().toLowerCase();
  return comandos.filter(c => {
    if (s.categoria !== 'all') {
      // Las tarjetas SIN categoria son transversales: pasan siempre el filtro temático.
      if (c.categoria != null && c.categoria !== s.categoria) return false;
    }
    if (s.tipo !== 'all' && c.tipo !== s.tipo) return false;
    if (s.dificultad !== 'all' && c.dificultad !== s.dificultad) return false;
    if (s.gitbashOnly && c.gitbash === 'no-disponible') return false;
    if (q) {
      const hay = (c.nombre || '').toLowerCase().includes(q)
               || (c.descripcion || '').toLowerCase().includes(q)
               || (c.tags || []).some(t => String(t).toLowerCase().includes(q));
      if (!hay) return false;
    }
    return true;
  });
}
```

- [ ] **Step 5: Verificar que los tests pasan**

Recargar. **Esperado:** banner verde "✓ 9 tests OK" (1 trivial + 8 de applyFilters). Sin errores en consola.

- [ ] **Step 6: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Implementar applyFilters con state y tests inline (transversales sin categoria)"
```

---

### Task 5: Render de tarjeta básica (tipo "comando")

**Files:**
- Modify: `guia-bash/guia-bash.html` (`// ====== JS: RENDER ======`, `// ====== JS: TESTS ======`)

- [ ] **Step 1: Implementar `renderCard` para tipo "comando" + helper `escapeHtml`**

Insertar en `// ====== JS: RENDER ======`:

```js
// ====== JS: RENDER ======

function escapeHtml(str) {
  return String(str ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function renderCodeBlock(code, compatNotes) {
  const safe = escapeHtml(code);
  const note = compatNotes
    ? `<p class="text-xs text-amber-700 mt-1">⚠ ${escapeHtml(compatNotes)}</p>`
    : '';
  return `<div class="my-1">
    <pre class="bg-slate-900 text-slate-100 text-xs rounded px-2 py-1 overflow-x-auto"><code>${safe}</code></pre>
    ${note}
  </div>`;
}

function renderEjemplos(ejemplos) {
  if (!ejemplos || ejemplos.length === 0) return '';
  return `<div class="mt-3">
    <h4 class="text-xs font-semibold uppercase text-slate-500 mb-1">Ejemplos</h4>
    ${ejemplos.map(e => `
      <div class="text-sm">
        ${renderCodeBlock(e.comando, e.compat_notes)}
        <p class="text-slate-700">${escapeHtml(e.explicacion)}</p>
      </div>
    `).join('')}
  </div>`;
}

function renderFlags(flags) {
  if (!flags || flags.length === 0) return '';
  return `<div class="mt-3">
    <h4 class="text-xs font-semibold uppercase text-slate-500 mb-1">Flags</h4>
    <ul class="text-sm space-y-1">
      ${flags.map(f => `
        <li>
          <code class="bg-slate-100 px-1 rounded">${escapeHtml(f.flag)}</code>
          — ${escapeHtml(f.descripcion)}
          ${f.compat_notes ? `<span class="block text-xs text-amber-700">⚠ ${escapeHtml(f.compat_notes)}</span>` : ''}
        </li>
      `).join('')}
    </ul>
  </div>`;
}

function renderModeloMental(text) {
  if (!text) return '';
  return `<div class="mt-3 bg-sky-50 border-l-4 border-sky-300 p-2 text-sm">
    <strong class="text-sky-900">Modelo mental:</strong>
    <span class="text-slate-700">${escapeHtml(text)}</span>
  </div>`;
}

function renderCombos(combos) {
  if (!combos || combos.length === 0) return '';
  return `<div class="mt-3">
    <h4 class="text-xs font-semibold uppercase text-slate-500 mb-1">Combinaciones</h4>
    ${combos.map(c => `
      <div class="text-sm">
        ${renderCodeBlock(c.comando, c.compat_notes)}
        <p class="text-slate-700">${escapeHtml(c.explicacion)}</p>
      </div>
    `).join('')}
  </div>`;
}

function renderGotchas(gotchas) {
  if (!gotchas || gotchas.length === 0) return '';
  return `<div class="mt-3">
    <h4 class="text-xs font-semibold uppercase text-slate-500 mb-1">⚠ Gotchas</h4>
    <ul class="text-sm list-disc list-inside text-slate-700 space-y-0.5">
      ${gotchas.map(g => `<li>${escapeHtml(g)}</li>`).join('')}
    </ul>
  </div>`;
}

function renderCuandoNoUsar(text) {
  if (!text) return '';
  return `<div class="mt-3 text-sm text-slate-600">
    <strong>Cuándo NO usarlo:</strong> ${escapeHtml(text)}
  </div>`;
}

function renderCard(c) {
  return `
    <article class="bg-white border border-slate-200 rounded-lg p-4 shadow-sm" data-id="${escapeHtml(c.id)}">
      <header class="flex items-start gap-2">
        <h3 class="text-lg font-bold flex-1"><code>${escapeHtml(c.nombre)}</code></h3>
        <span class="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">${escapeHtml(c.tipo)}</span>
      </header>
      ${c.patron ? `<p class="text-xs text-slate-500 font-mono mt-1">${escapeHtml(c.patron)}</p>` : ''}
      <p class="mt-2 text-sm text-slate-700">${escapeHtml(c.descripcion)}</p>
      ${renderModeloMental(c.modelo_mental)}
      ${renderEjemplos(c.ejemplos)}
      ${renderFlags(c.flags)}
      ${renderCombos(c.combos)}
      ${renderGotchas(c.gotchas)}
      ${renderCuandoNoUsar(c.cuando_no_usar)}
    </article>
  `;
}

function renderAll() {
  const grid = document.getElementById('grid');
  const empty = document.getElementById('empty-state');
  const filtered = applyFilters(COMANDOS, state);
  if (filtered.length === 0) {
    grid.innerHTML = '';
    empty.classList.remove('hidden');
  } else {
    empty.classList.add('hidden');
    grid.innerHTML = filtered.map(renderCard).join('');
  }
}
```

- [ ] **Step 2: Cargar una tarjeta de prueba en `COMANDOS` y llamar `renderAll`**

Reemplazar `const COMANDOS = [];` por:

```js
    const COMANDOS = [
      {
        id: 'pwd',
        tipo: 'comando',
        nombre: 'pwd',
        categoria: 'busqueda-archivos',
        dificultad: 'basico',
        descripcion: 'Imprime la ruta absoluta del directorio actual.',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'pwd', explicacion: 'Muestra dónde estás.', compat_notes: null }
        ]
      }
    ];
```

Y al final del bloque `// ====== JS: TESTS ======`, justo antes de `renderTestBanner();`, añadir:

```js
  // --- render initial ---
  renderAll();
```

- [ ] **Step 3: Añadir tests de `renderCard`**

En el mismo IIFE de tests, antes de `renderAll()`:

```js
  // --- renderCard ---
  const cardHtml = renderCard(SAMPLE[0]);
  assert(cardHtml.includes('<article'), 'renderCard — devuelve un article');
  assert(cardHtml.includes('data-id="a"'), 'renderCard — incluye data-id');
  assert(cardHtml.includes('Hola archivo'), 'renderCard — incluye descripcion');
  // No debe romperse con campos opcionales ausentes
  assert(typeof renderCard({ id:'x', tipo:'comando', nombre:'x', categoria:'busqueda-archivos', dificultad:'basico', descripcion:'min', gitbash:'ok' }) === 'string',
         'renderCard — funciona con solo obligatorios');
```

- [ ] **Step 4: Verificar visualmente**

Recargar. **Esperado:** banner verde "✓ 12 tests OK". En el grid, una tarjeta blanca con título `pwd`, badge "comando", descripción, sección "Ejemplos" con bloque de código `pwd` y "Muestra dónde estás.".

- [ ] **Step 5: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Implementar renderCard básico para tipo comando con sub-renderers por sección"
```

---

### Task 6: Wire-up de filtros (eventos → state → renderAll)

**Files:**
- Modify: `guia-bash/guia-bash.html` (`// ====== JS: WIRE-UP ======`)

- [ ] **Step 1: Implementar el wire-up**

Insertar en `// ====== JS: WIRE-UP ======`:

```js
// ====== JS: WIRE-UP ======

function wireUp() {
  const search = document.getElementById('search-input');
  const cat    = document.getElementById('filter-categoria');
  const dif    = document.getElementById('filter-dificultad');
  const tip    = document.getElementById('filter-tipo');
  const gb     = document.getElementById('filter-gitbash');
  const clear  = document.getElementById('filter-clear');

  search.addEventListener('input', () => { state.search = search.value; renderAll(); });
  cat.addEventListener('change',   () => { state.categoria   = cat.value; renderAll(); });
  dif.addEventListener('change',   () => { state.dificultad  = dif.value; renderAll(); });
  tip.addEventListener('change',   () => { state.tipo        = tip.value; renderAll(); });
  gb.addEventListener('change',    () => { state.gitbashOnly = gb.checked; renderAll(); });

  clear.addEventListener('click', () => {
    search.value = ''; cat.value = 'all'; dif.value = 'all'; tip.value = 'all'; gb.checked = false;
    state.search = ''; state.categoria = 'all'; state.dificultad = 'all'; state.tipo = 'all'; state.gitbashOnly = false;
    renderAll();
  });
}

wireUp();
```

- [ ] **Step 2: Cargar varias tarjetas de prueba para tener algo que filtrar**

Sustituir el `const COMANDOS = [...]` actual por:

```js
    const COMANDOS = [
      {
        id: 'pwd',
        tipo: 'comando',
        nombre: 'pwd',
        categoria: 'busqueda-archivos',
        dificultad: 'basico',
        descripcion: 'Imprime la ruta absoluta del directorio actual.',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'pwd', explicacion: 'Muestra dónde estás.', compat_notes: null }
        ]
      },
      {
        id: 'locate-stub',
        tipo: 'comando',
        nombre: 'locate',
        categoria: 'busqueda-archivos',
        dificultad: 'basico',
        descripcion: 'Stub temporal para probar filtro Git Bash.',
        gitbash: 'no-disponible',
        ejemplos: [
          { comando: 'locate config', explicacion: 'No disponible en Git Bash.', compat_notes: 'No instalado en MSYS2/MinGW.' }
        ]
      }
    ];
```

(Nota: `locate-stub` se sustituirá por la tarjeta real en una tarea posterior.)

- [ ] **Step 3: Verificar interacción**

Recargar. **Esperado:**
- Se ven 2 tarjetas (`pwd` y `locate`).
- Escribir "ruta" en el input → solo queda `pwd`.
- Limpiar input, marcar checkbox "Compatible con Git Bash" → solo queda `pwd` (locate desaparece).
- Limpiar checkbox, dropdown Tipo = "Operador" → grid vacío (todavía no hay empty state visible — se verá en Task 7).
- Botón "Limpiar filtros" → restablece todo y vuelven las 2 tarjetas.

- [ ] **Step 4: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Conectar filtros del DOM con state y renderAll"
```

---

### Task 7: Empty state

**Files:**
- Modify: `guia-bash/guia-bash.html` (`<!-- ====== MARKUP: EMPTY STATE ====== -->`)

- [ ] **Step 1: Reemplazar el div vacío del empty state por contenido**

Reemplazar:

```html
  <!-- ====== MARKUP: EMPTY STATE ====== -->
  <div id="empty-state" class="max-w-6xl mx-auto px-4 py-12 text-center text-slate-500 hidden"></div>
```

por:

```html
  <!-- ====== MARKUP: EMPTY STATE ====== -->
  <div id="empty-state" class="max-w-6xl mx-auto px-4 py-12 text-center text-slate-500 hidden">
    <p class="text-lg mb-2">Ningún comando coincide con estos filtros.</p>
    <button id="empty-state-clear" class="text-sky-700 underline hover:text-sky-900">Limpiar filtros</button>
  </div>
```

- [ ] **Step 2: Conectar el botón del empty state al mismo handler de "Limpiar filtros"**

Modificar `wireUp()` añadiendo, justo antes del cierre `}`, el wiring del botón duplicado:

```js
  document.getElementById('empty-state-clear').addEventListener('click', () => {
    document.getElementById('filter-clear').click();
  });
```

- [ ] **Step 3: Verificar**

Recargar. Filtrar Tipo = "Operador". **Esperado:** "Ningún comando coincide con estos filtros." + botón "Limpiar filtros". Click → todo vuelve.

- [ ] **Step 4: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Implementar empty state cuando los filtros no devuelven nada"
```

---

### Task 8: Indicador visual del estado de compatibilidad (`gitbash`)

**Files:**
- Modify: `guia-bash/guia-bash.html` (función `renderCard`)

- [ ] **Step 1: Añadir indicador de color en la cabecera de la tarjeta**

En `renderCard`, sustituir el bloque `<header>` actual por:

```js
      <header class="flex items-start gap-2">
        ${renderGitbashDot(c.gitbash)}
        <h3 class="text-lg font-bold flex-1"><code>${escapeHtml(c.nombre)}</code></h3>
        <span class="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">${escapeHtml(c.tipo)}</span>
      </header>
```

Y añadir `renderGitbashDot` justo encima de `renderCard` (dentro de `// ====== JS: RENDER ======`):

```js
function renderGitbashDot(estado) {
  const map = {
    'ok':            { color: 'bg-emerald-500', title: 'Git Bash: portable 100%' },
    'caveats':       { color: 'bg-amber-500',   title: 'Git Bash: existe con caveats (ver compat_notes en flags/ejemplos)' },
    'no-disponible': { color: 'bg-rose-500',    title: 'Git Bash: no disponible' }
  };
  const m = map[estado] || { color: 'bg-slate-400', title: 'Git Bash: desconocido' };
  return `<span class="inline-block w-3 h-3 rounded-full ${m.color} mt-1.5" title="${m.title}"></span>`;
}
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:**
- `pwd` muestra punto verde.
- `locate` muestra punto rojo.
- Hover sobre el punto muestra tooltip con la explicación.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Añadir indicador visual de compatibilidad Git Bash en cabecera de tarjeta"
```

---

### Task 9: "Ver más" / colapsado para tarjetas largas

**Files:**
- Modify: `guia-bash/guia-bash.html` (`renderCard` + delegación de eventos en `renderAll` o en `wireUp`)

- [ ] **Step 1: Modificar `renderCard` para envolver el contenido posterior a la descripción en un bloque colapsable cuando la tarjeta tenga muchas secciones**

En `renderCard`, sustituir el bloque desde `${renderModeloMental(...)}` hasta `${renderCuandoNoUsar(...)}` por:

```js
      ${renderCardBody(c)}
```

Y añadir `renderCardBody` arriba (dentro de `// ====== JS: RENDER ======`):

```js
function renderCardBody(c) {
  const inner = [
    renderModeloMental(c.modelo_mental),
    renderEjemplos(c.ejemplos),
    renderFlags(c.flags),
    renderCombos(c.combos),
    renderGotchas(c.gotchas),
    renderCuandoNoUsar(c.cuando_no_usar)
  ].join('');

  // Heurística: si la tarjeta es "gorda" (tiene modelo_mental o gotchas o más de 4 flags), se colapsa.
  const isHeavy = !!c.modelo_mental || (c.gotchas && c.gotchas.length > 0) || (c.flags && c.flags.length > 4);
  if (!isHeavy) return inner;

  return `
    <details class="mt-2">
      <summary class="cursor-pointer text-sm text-sky-700 hover:text-sky-900 select-none">Ver más ▾</summary>
      <div class="mt-2">${inner}</div>
    </details>
  `;
}
```

- [ ] **Step 2: Cargar una tarjeta gorda de prueba para validar el colapsado**

Añadir al final del array `COMANDOS` (entre la última tarjeta y `]`):

```js
      ,{
        id: 'find-stub',
        tipo: 'comando',
        nombre: 'find-stub',
        categoria: 'busqueda-archivos',
        dificultad: 'intermedio',
        descripcion: 'Stub para probar el colapsado.',
        gitbash: 'caveats',
        modelo_mental: 'find recorre un árbol y aplica tests a cada entrada.',
        ejemplos: [{ comando: 'find . -name "*.ts"', explicacion: 'ejemplo', compat_notes: null }],
        flags: [
          { flag: '-name X', descripcion: 'a', compat_notes: null },
          { flag: '-type f', descripcion: 'b', compat_notes: null },
          { flag: '-mtime -7', descripcion: 'c', compat_notes: null },
          { flag: '-size +10M', descripcion: 'd', compat_notes: null },
          { flag: '-printf', descripcion: 'e', compat_notes: 'No en Git Bash.' }
        ],
        gotchas: ['Sin -type f, también devuelve directorios.']
      }
```

- [ ] **Step 3: Verificar**

Recargar. **Esperado:**
- `pwd` y `locate` se ven completos (no colapsan, no son heavy).
- `find-stub` muestra título + descripción + "Ver más ▾". Click → expande modelo mental, ejemplos, flags, gotchas. Click de nuevo → colapsa.

- [ ] **Step 4: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Añadir colapsado 'Ver más' para tarjetas gordas mediante details/summary"
```

---

### Task 10: Botón copiar con fallback `execCommand` y feedback visual

**Files:**
- Modify: `guia-bash/guia-bash.html` (`// ====== JS: COPY ======`, función `renderCodeBlock`, delegación de eventos)

- [ ] **Step 1: Implementar `copyToClipboard` con fallback**

Insertar en `// ====== JS: COPY ======`:

```js
// ====== JS: COPY ======

/**
 * Copia texto al portapapeles. Intenta navigator.clipboard (secure context),
 * cae a document.execCommand('copy') con un <textarea> temporal para entornos file://
 * donde Firefox bloquea la API moderna.
 * Devuelve una Promise<boolean>.
 */
function copyToClipboard(text) {
  if (navigator.clipboard && window.isSecureContext) {
    return navigator.clipboard.writeText(text).then(() => true).catch(() => fallbackCopy(text));
  }
  return Promise.resolve(fallbackCopy(text));
}

function fallbackCopy(text) {
  const ta = document.createElement('textarea');
  ta.value = text;
  ta.style.position = 'fixed';
  ta.style.left = '-9999px';
  document.body.appendChild(ta);
  ta.focus();
  ta.select();
  let ok = false;
  try { ok = document.execCommand('copy'); } catch (_) { ok = false; }
  document.body.removeChild(ta);
  return ok;
}

function flashCopyButton(btn, ok) {
  const original = btn.dataset.original || btn.textContent;
  btn.dataset.original = original;
  btn.textContent = ok ? '✓ copiado' : '✗ no se pudo copiar';
  btn.classList.toggle('text-emerald-700', ok);
  btn.classList.toggle('text-rose-700',  !ok);
  setTimeout(() => {
    btn.textContent = original;
    btn.classList.remove('text-emerald-700', 'text-rose-700');
  }, 1500);
}
```

- [ ] **Step 2: Modificar `renderCodeBlock` para incluir botón copiar**

Sustituir la función `renderCodeBlock` por:

```js
function renderCodeBlock(code, compatNotes) {
  const safe = escapeHtml(code);
  const note = compatNotes
    ? `<p class="text-xs text-amber-700 mt-1">⚠ ${escapeHtml(compatNotes)}</p>`
    : '';
  return `<div class="my-1 relative group">
    <pre class="bg-slate-900 text-slate-100 text-xs rounded px-2 py-1 overflow-x-auto"><code>${safe}</code></pre>
    <button type="button" class="copy-btn absolute top-1 right-1 text-xs bg-slate-700 text-slate-100 px-2 py-0.5 rounded opacity-0 group-hover:opacity-100 transition" data-copy="${safe}">📋</button>
    ${note}
  </div>`;
}
```

- [ ] **Step 3: Añadir delegación de evento al body en `wireUp()` para los botones de copiar**

Al final de `wireUp()`, antes del cierre `}`, añadir:

```js
  document.body.addEventListener('click', (ev) => {
    const btn = ev.target.closest('.copy-btn');
    if (!btn) return;
    const text = btn.getAttribute('data-copy') || '';
    // Decodificar entidades HTML que escapamos al render.
    const decoded = new DOMParser().parseFromString(text, 'text/html').documentElement.textContent;
    copyToClipboard(decoded).then(ok => flashCopyButton(btn, ok));
  });
```

- [ ] **Step 4: Verificar**

Recargar. **Esperado:**
- En cualquier tarjeta, hover sobre un bloque de código muestra un botón "📋" en la esquina superior derecha.
- Click → cambia a "✓ copiado" en verde durante 1.5s, después vuelve a "📋".
- Pegar en otro sitio (notepad, terminal) — debe pegarse el comando.
- Si falla (poco probable en Chrome), debe mostrar "✗ no se pudo copiar" en rojo.

- [ ] **Step 5: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Añadir botón copiar con fallback execCommand y feedback visual"
```

---

### Task 11: Render para tipo "operador" (sin `categoria`, sin `flags`)

**Files:**
- Modify: `guia-bash/guia-bash.html` (`renderCard` o nueva variante)

- [ ] **Step 1: Verificar que `renderCard` actual no falla con operadores sin `categoria` ni `flags`**

`renderCard` ya gestiona campos opcionales (`renderFlags` retorna '' si no hay flags). Solo hay que confirmar que el badge del tipo se muestra correctamente. Sustituir el `<span class="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">` por una versión que use color por tipo:

```js
function renderTipoBadge(tipo) {
  const map = {
    'comando':  'bg-sky-100 text-sky-800',
    'operador': 'bg-violet-100 text-violet-800',
    'receta':   'bg-emerald-100 text-emerald-800',
    'sintaxis': 'bg-amber-100 text-amber-800'
  };
  const cls = map[tipo] || 'bg-slate-100 text-slate-700';
  return `<span class="text-xs px-2 py-0.5 rounded-full ${cls}">${escapeHtml(tipo)}</span>`;
}
```

Y en `renderCard`, sustituir la línea `<span class="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">${escapeHtml(c.tipo)}</span>` por `${renderTipoBadge(c.tipo)}`.

- [ ] **Step 2: Cargar un operador de prueba**

Añadir al final del array `COMANDOS`:

```js
      ,{
        id: 'op-pipe-stub',
        tipo: 'operador',
        nombre: '|',
        // sin categoria — transversal
        dificultad: 'basico',
        descripcion: 'Pipe stub.',
        gitbash: 'ok',
        modelo_mental: 'Conecta stdout de un comando a stdin del siguiente.',
        ejemplos: [
          { comando: 'ls | head', explicacion: 'Stub.', compat_notes: null }
        ]
      }
```

- [ ] **Step 3: Verificar**

Recargar. **Esperado:**
- Aparece tarjeta `|` con badge violeta "operador", punto verde gitbash.
- No tiene sección de "Flags" (no rompe).
- Filtrar Categoría = "Búsqueda de archivos" → la tarjeta del pipe sigue apareciendo (transversal). Filtrar Categoría = "Búsqueda de contenido" → idem. Es la regla de §5.2.

- [ ] **Step 4: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Soportar render de tipo operador con badges de color por tipo"
```

---

### Task 12: Render para tipo "receta" (`escenario` + `desglose`)

**Files:**
- Modify: `guia-bash/guia-bash.html` (`renderCard` o sub-renderers)

- [ ] **Step 1: Añadir sub-renderers para receta y modificar `renderCardBody` para incluirlos cuando `tipo === 'receta'`**

Insertar en `// ====== JS: RENDER ======`, antes de `renderCardBody`:

```js
function renderEscenario(text) {
  if (!text) return '';
  return `<div class="mt-2 bg-emerald-50 border-l-4 border-emerald-300 p-2 text-sm">
    <strong class="text-emerald-900">Escenario:</strong>
    <span class="text-slate-700">${escapeHtml(text)}</span>
  </div>`;
}

function renderDesglose(items) {
  if (!items || items.length === 0) return '';
  return `<div class="mt-3">
    <h4 class="text-xs font-semibold uppercase text-slate-500 mb-1">Desglose</h4>
    <ol class="text-sm space-y-1 list-decimal list-inside">
      ${items.map(i => `
        <li>
          <code class="bg-slate-100 px-1 rounded">${escapeHtml(i.fragmento)}</code>
          — ${escapeHtml(i.hace)}
        </li>
      `).join('')}
    </ol>
  </div>`;
}
```

Modificar `renderCardBody` para incluir escenario y desglose al inicio cuando aplica:

```js
function renderCardBody(c) {
  const inner = [
    renderEscenario(c.escenario),
    renderModeloMental(c.modelo_mental),
    renderEjemplos(c.ejemplos),
    renderDesglose(c.desglose),
    renderFlags(c.flags),
    renderCombos(c.combos),
    renderGotchas(c.gotchas),
    renderCuandoNoUsar(c.cuando_no_usar)
  ].join('');

  const isHeavy = !!c.modelo_mental || (c.gotchas && c.gotchas.length > 0) || (c.flags && c.flags.length > 4) || c.tipo === 'receta';
  if (!isHeavy) return inner;

  return `
    <details class="mt-2">
      <summary class="cursor-pointer text-sm text-sky-700 hover:text-sky-900 select-none">Ver más ▾</summary>
      <div class="mt-2">${inner}</div>
    </details>
  `;
}
```

- [ ] **Step 2: Cargar una receta de prueba**

Añadir al final del array `COMANDOS`:

```js
      ,{
        id: 'receta-stub',
        tipo: 'receta',
        nombre: 'Buscar TODO en proyecto',
        categoria: 'busqueda-contenido',
        dificultad: 'basico',
        descripcion: 'Receta stub.',
        gitbash: 'ok',
        escenario: 'Quiero localizar todos los TODO pendientes en el código.',
        ejemplos: [
          { comando: "grep -rn 'TODO' .", explicacion: 'Comando completo.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'grep', hace: 'busca patrón.' },
          { fragmento: '-r', hace: 'recursivamente.' },
          { fragmento: '-n', hace: 'mostrando número de línea.' }
        ],
        relacionados: ['grep']
      }
```

- [ ] **Step 3: Verificar**

Recargar. **Esperado:**
- Tarjeta "Buscar TODO en proyecto" con badge verde "receta", punto verde gitbash.
- "Ver más" expande a: bloque verde "Escenario: ...", ejemplos con el comando completo, lista numerada del desglose.
- Filtrar Tipo = "Receta" → solo aparece esta. Filtrar Categoría = "Búsqueda de contenido" → esta tarjeta aparece (porque su categoria coincide).

- [ ] **Step 4: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Soportar render de tipo receta con escenario y desglose"
```

---

### Task 13: Cargar datos reales — `find` (gorda)

A partir de aquí cargamos el catálogo real. Sustituimos los stubs introducidos en tareas anteriores.

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Eliminar los stubs (`pwd`, `locate-stub`, `find-stub`, `op-pipe-stub`, `receta-stub`) y reemplazar el array por el primer comando real**

Sustituir todo el `const COMANDOS = [...]` por:

```js
    const COMANDOS = [
      {
        id: 'find',
        tipo: 'comando',
        nombre: 'find',
        categoria: 'busqueda-archivos',
        dificultad: 'intermedio',
        descripcion: 'Recorre un árbol de directorios buscando archivos según criterios.',
        gitbash: 'caveats',
        patron: 'find [ruta] [tests] [acción]',
        tags: ['recursivo', 'filtros', 'arbol'],
        modelo_mental: 'find recorre un árbol de directorios y aplica un test a cada entrada. Los flags como -name, -type, -mtime son tests; se combinan con AND implícito o con -o para OR. -exec, -delete y -print son acciones que se aplican a las coincidencias.',
        ejemplos: [
          { comando: "find . -name '*.ts'", explicacion: 'Busca todos los archivos .ts a partir del directorio actual.', compat_notes: 'Comillas alrededor del patrón obligatorias para evitar que el shell expanda el glob antes de pasarlo a find.' },
          { comando: 'find . -type f -mtime -7', explicacion: 'Archivos (no directorios) modificados en los últimos 7 días.', compat_notes: null },
          { comando: "find . -iname '*.LOG'", explicacion: 'Búsqueda por nombre, ignorando mayúsculas/minúsculas.', compat_notes: null },
          { comando: 'find . -size +10M', explicacion: 'Archivos de más de 10 megabytes.', compat_notes: null }
        ],
        flags: [
          { flag: '-name PATRON',          descripcion: 'Filtra por nombre (glob, case-sensitive).', compat_notes: null },
          { flag: '-iname PATRON',         descripcion: 'Como -name pero case-insensitive.',         compat_notes: null },
          { flag: '-type f|d|l',           descripcion: 'Filtra por tipo (file/dir/link).',           compat_notes: null },
          { flag: '-mtime ±N',             descripcion: 'Modificados hace N días. -7 = últimos 7 días, +7 = hace más de 7.', compat_notes: null },
          { flag: '-size ±N[ckMG]',        descripcion: 'Filtra por tamaño. +10M = más de 10MB, -1k = menos de 1KB.',       compat_notes: null },
          { flag: '-path PATRON',          descripcion: 'Filtra por ruta completa (útil con -prune para excluir).',          compat_notes: null },
          { flag: '-prune',                descripcion: 'No descender en el directorio actual (usado para excluir subárboles).', compat_notes: null },
          { flag: '-exec CMD {} \\;',      descripcion: 'Ejecuta CMD por cada match. {} se sustituye por la ruta encontrada.',  compat_notes: null },
          { flag: '-exec CMD {} +',        descripcion: 'Como -exec ; pero agrupa varios matches en una invocación (más eficiente).', compat_notes: null },
          { flag: '-delete',               descripcion: 'Borra los matches. Equivalente a -exec rm pero más eficiente.',     compat_notes: null },
          { flag: '-print',                descripcion: 'Imprime la ruta. Acción por defecto.',                                compat_notes: null },
          { flag: '-printf FORMATO',       descripcion: 'Salida con formato personalizado (%p ruta, %s tamaño, %T@ epoch).',  compat_notes: 'No disponible en Git Bash. Alternativa: usar -exec stat o procesar con awk.' }
        ],
        combos: [
          { comando: "find . -name '*.log' | xargs grep ERROR",                     explicacion: "Lista todos los .log y busca 'ERROR' en su contenido.", compat_notes: null },
          { comando: "find . -name '*.tmp' -delete",                                explicacion: 'Borra todos los .tmp del árbol.',                       compat_notes: null },
          { comando: 'find . -type f -exec wc -l {} +',                             explicacion: 'Cuenta líneas de cada archivo regular.',                compat_notes: null },
          { comando: "find . -path './node_modules' -prune -o -type f -print",      explicacion: 'Lista todos los archivos excluyendo node_modules.',     compat_notes: null }
        ],
        gotchas: [
          'Sin -type f también devuelve directorios. La mayoría de las veces quieres -type f.',
          '-name es case-sensitive; usar -iname para ignorar mayúsculas.',
          'Las acciones (-exec, -delete) se aplican a TODOS los matches: probar antes con -print para ver qué afectará.',
          'Comillas en el patrón de -name son obligatorias si contiene wildcards (*, ?, [...]) — sin ellas el shell expande el glob antes de que find lo vea.',
          'El orden de los argumentos importa: filtros antes de acciones, -prune antes de -o.'
        ],
        relacionados: ['grep', 'xargs', 'ls', 'locate'],
        cuando_no_usar: 'Para búsquedas frecuentes en árboles enormes (node_modules, /usr), considera `fd` (más rápido y sintaxis moderna). Si solo necesitas localizar por nombre y la base de datos está actualizada, `locate` es instantáneo (no disponible en Git Bash).'
      }
    ];
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:**
- Banner verde de tests (los tests no dependen de COMANDOS).
- Una sola tarjeta `find` con punto amarillo (caveats), badge sky "comando", patrón en gris claro, descripción, "Ver más ▾" (es heavy). Al expandir: modelo mental sky, 4 ejemplos (uno con compat_notes amarillo), 12 flags (uno con compat_notes), 4 combos, 5 gotchas, "Cuándo NO usarlo".
- Hover sobre cualquier `<pre>` muestra botón copiar.
- Filtro Categoría = "Búsqueda de archivos" la deja. Filtro = "Búsqueda de contenido" la oculta.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Cargar tarjeta gorda de find con flags, combos, gotchas y cuándo no usar"
```

---

### Task 14: Cargar `grep` (gorda) — incluye nota sobre egrep/fgrep

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Añadir tarjeta de `grep` al array**

Insertar como segundo elemento del array (entre `find` y `]`):

```js
      ,{
        id: 'grep',
        tipo: 'comando',
        nombre: 'grep',
        categoria: 'busqueda-contenido',
        dificultad: 'intermedio',
        descripcion: 'Busca patrones (cadenas o regex) dentro del contenido de archivos.',
        gitbash: 'caveats',
        patron: 'grep [opciones] PATRON [archivos...]',
        tags: ['regex', 'texto', 'recursivo'],
        modelo_mental: 'grep lee texto línea a línea (de archivos o stdin) y emite las que matchean el patrón. Por defecto usa regex básicas; -E activa regex extendidas, -F desactiva regex y trata el patrón como literal. -r recorre directorios recursivamente.',
        ejemplos: [
          { comando: "grep 'error' app.log",                explicacion: "Líneas con 'error' (case-sensitive) en app.log.", compat_notes: null },
          { comando: "grep -rni 'todo' .",                  explicacion: 'Recursivo, ignorando mayúsculas, mostrando número de línea.', compat_notes: null },
          { comando: "grep -E 'WARN|ERROR' app.log",        explicacion: 'Regex extendida: WARN o ERROR.',                  compat_notes: null },
          { comando: "grep -v 'DEBUG' app.log",             explicacion: 'Líneas que NO contienen DEBUG (invertido).',       compat_notes: null },
          { comando: "grep -c 'ERROR' app.log",             explicacion: 'Solo cuenta cuántas líneas matchean.',             compat_notes: null }
        ],
        flags: [
          { flag: '-r / -R',          descripcion: 'Recursivo en directorios.',                                  compat_notes: null },
          { flag: '-n',               descripcion: 'Muestra el número de línea de cada coincidencia.',          compat_notes: null },
          { flag: '-i',               descripcion: 'Case-insensitive.',                                          compat_notes: null },
          { flag: '-v',               descripcion: 'Invierte: muestra líneas que NO matchean.',                 compat_notes: null },
          { flag: '-l',               descripcion: 'Solo nombres de archivos con coincidencias (no las líneas).', compat_notes: null },
          { flag: '-c',               descripcion: 'Solo cuenta coincidencias por archivo.',                    compat_notes: null },
          { flag: '-w',               descripcion: 'Match de palabra entera (no substring).',                   compat_notes: null },
          { flag: '-E',               descripcion: 'Regex extendidas (alternativa a egrep).',                   compat_notes: null },
          { flag: '-F',               descripcion: 'Patrón literal, sin regex (alternativa a fgrep, más rápido).', compat_notes: null },
          { flag: '-P',               descripcion: 'Regex tipo Perl (PCRE).',                                    compat_notes: 'En Git Bash el binario suele estar compilado SIN soporte PCRE — `grep -P` falla. Usar -E como alternativa cuando sea posible.' },
          { flag: '--include=GLOB',   descripcion: 'Solo busca en archivos cuyo nombre matchea GLOB.',          compat_notes: null },
          { flag: '--exclude-dir=DIR', descripcion: 'Excluye directorios completos (ej. node_modules).',         compat_notes: null },
          { flag: '-A N / -B N / -C N', descripcion: 'Contexto: N líneas después / antes / ambos.',              compat_notes: null }
        ],
        combos: [
          { comando: "grep -rn 'TODO' . --include='*.js'",                explicacion: 'TODOs solo en archivos .js, recursivo.',         compat_notes: null },
          { comando: "ps aux | grep nginx",                               explicacion: 'Filtra procesos que contienen nginx.',            compat_notes: null },
          { comando: "find . -name '*.log' | xargs grep 'ERROR'",        explicacion: 'Combina con find para buscar en una lista de archivos concreta.', compat_notes: null }
        ],
        gotchas: [
          "egrep y fgrep están deprecados como binarios separados; el equivalente moderno es `grep -E` y `grep -F` respectivamente. Pueden seguir existiendo como wrappers.",
          'Sin comillas alrededor del patrón, el shell puede interpretar caracteres especiales (* ? $ etc.) antes de que llegue a grep.',
          "`ps aux | grep nginx` también matchea el propio grep. Usar `pgrep nginx` o `ps aux | grep [n]ginx` (truco del corchete).",
          'La salida con --include / --exclude-dir es preferible a `find ... | xargs grep` para excluir node_modules.',
          'En Git Bash, -P (PCRE) frecuentemente no funciona — usar -E.'
        ],
        relacionados: ['find', 'xargs', 'head', 'tail'],
        cuando_no_usar: 'Para búsquedas en proyectos grandes, considera `ripgrep (rg)` — respeta .gitignore por defecto, es mucho más rápido y tiene PCRE de serie. Para extracción/transformación de texto más allá de filtrar líneas, usa `sed` o `awk`.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** ahora hay 2 tarjetas. `grep` con punto amarillo, badge sky, "Ver más" expandible. Flag `-P` muestra warning amarillo de Git Bash.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Cargar tarjeta gorda de grep con caveat de -P en Git Bash y mención a egrep/fgrep"
```

---

### Task 15: Cargar `xargs` (gorda)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Añadir tarjeta de `xargs`**

Insertar después de la tarjeta de grep:

```js
      ,{
        id: 'xargs',
        tipo: 'comando',
        nombre: 'xargs',
        categoria: 'busqueda-archivos',
        dificultad: 'intermedio',
        descripcion: 'Construye y ejecuta un comando a partir de las líneas que recibe por stdin.',
        gitbash: 'ok',
        patron: '... | xargs [opciones] COMANDO [args...]',
        tags: ['stdin', 'compose', 'pipeline'],
        modelo_mental: 'xargs lee líneas de stdin y las inyecta como argumentos del COMANDO que le indiques. Es lo que une `find ...` (que produce nombres de archivo) con `grep` (que necesita nombres de archivo como argumentos). Por defecto agrupa todas las líneas en una sola invocación; -n y -I cambian esto.',
        ejemplos: [
          { comando: "find . -name '*.log' | xargs grep ERROR",  explicacion: "Pasa la lista de .log a grep para buscar 'ERROR'.", compat_notes: null },
          { comando: "find . -name '*.tmp' | xargs rm",          explicacion: 'Borra todos los .tmp encontrados.',                  compat_notes: null },
          { comando: "echo 'a b c' | xargs -n 1 echo",           explicacion: 'Procesa cada palabra individualmente.',              compat_notes: null }
        ],
        flags: [
          { flag: '-n N',         descripcion: 'Pasa N argumentos por invocación del comando.',                              compat_notes: null },
          { flag: '-I {}',        descripcion: 'Sustituye {} por el argumento. Útil cuando el argumento NO va al final.',     compat_notes: null },
          { flag: '-0',           descripcion: 'Lee entradas separadas por NUL (\\0) en vez de espacios. Pareja natural de `find ... -print0`.', compat_notes: null },
          { flag: '-p',           descripcion: 'Pide confirmación antes de cada ejecución (útil para -delete inseguros).',   compat_notes: null },
          { flag: '-t',           descripcion: 'Verbose: imprime el comando antes de ejecutarlo.',                             compat_notes: null },
          { flag: '--no-run-if-empty', descripcion: 'No ejecuta si stdin está vacío (evita ejecutar el comando con 0 args).', compat_notes: null }
        ],
        combos: [
          { comando: "find . -name '*.log' -print0 | xargs -0 grep -l 'ERROR'", explicacion: 'Robusto frente a nombres con espacios.',     compat_notes: null },
          { comando: "find . -name '*.bak' | xargs -I {} mv {} archivo/",       explicacion: 'Mueve cada match a una carpeta usando placeholder.', compat_notes: null }
        ],
        gotchas: [
          'Por defecto, xargs trata espacios y saltos de línea como separadores — nombres con espacios rompen. Usar -0 + find -print0.',
          'Con stdin vacío, sin --no-run-if-empty, xargs ejecuta el comando con cero argumentos (puede ser destructivo, p.ej. `xargs rm`).',
          'find . -exec CMD {} + suele ser preferible a find ... | xargs CMD por evitar problemas de cuoting; usa xargs cuando necesites -I o -n.'
        ],
        relacionados: ['find', 'grep'],
        cuando_no_usar: 'Si find ya tiene -exec ... {} +, xargs aporta poco salvo para -I (placeholders en posiciones distintas a la final) o cuando la fuente de stdin no es find.'
      }
```

- [ ] **Step 2: Verificar**

Recargar. 3 tarjetas. `xargs` con punto verde (ok), badge sky.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Cargar tarjeta gorda de xargs como comando (no operador)"
```

---

### Task 16: Cargar comandos básicos de búsqueda de archivos

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Añadir las tarjetas básicas (ls, pwd, tree, file, which, whereis, type, locate)**

Insertar después de la tarjeta de xargs:

```js
      ,{
        id: 'ls',
        tipo: 'comando', nombre: 'ls', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Lista el contenido de un directorio.',
        gitbash: 'ok',
        patron: 'ls [opciones] [ruta]',
        ejemplos: [
          { comando: 'ls',           explicacion: 'Lista el directorio actual.',           compat_notes: null },
          { comando: 'ls -la',       explicacion: 'Listado largo + ocultos (empezando por .).', compat_notes: null },
          { comando: 'ls -lh',       explicacion: 'Listado largo con tamaños humanos (K, M, G).', compat_notes: null },
          { comando: 'ls -lt',       explicacion: 'Ordena por fecha de modificación (más reciente primero).', compat_notes: null },
          { comando: 'ls -lS',       explicacion: 'Ordena por tamaño descendente.',         compat_notes: null }
        ],
        flags: [
          { flag: '-l', descripcion: 'Listado largo (permisos, tamaño, fecha).', compat_notes: null },
          { flag: '-a', descripcion: 'Incluye archivos ocultos.',                 compat_notes: null },
          { flag: '-h', descripcion: 'Tamaños en formato humano (combinado con -l).', compat_notes: null },
          { flag: '-t', descripcion: 'Ordena por fecha de modificación.',         compat_notes: null },
          { flag: '-S', descripcion: 'Ordena por tamaño.',                         compat_notes: null },
          { flag: '-R', descripcion: 'Recursivo en subdirectorios.',               compat_notes: null }
        ]
      }
      ,{
        id: 'pwd',
        tipo: 'comando', nombre: 'pwd', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Imprime la ruta absoluta del directorio actual ("print working directory").',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'pwd', explicacion: 'Ruta absoluta del directorio actual.', compat_notes: 'En Git Bash devuelve estilo /c/Users/... no C:\\Users\\...' }
        ]
      }
      ,{
        id: 'tree',
        tipo: 'comando', nombre: 'tree', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Muestra la estructura del directorio como árbol ASCII.',
        gitbash: 'caveats',
        ejemplos: [
          { comando: 'tree',                  explicacion: 'Árbol del directorio actual.',                          compat_notes: null },
          { comando: 'tree -L 2',             explicacion: 'Limita la profundidad a 2 niveles.',                    compat_notes: null },
          { comando: 'tree -I node_modules',  explicacion: 'Ignora node_modules.',                                  compat_notes: null }
        ],
        flags: [
          { flag: '-L N',     descripcion: 'Profundidad máxima.',                  compat_notes: null },
          { flag: '-I PATRON', descripcion: 'Ignora rutas que matcheen el patrón.', compat_notes: null },
          { flag: '-d',       descripcion: 'Solo directorios.',                     compat_notes: null },
          { flag: '-a',       descripcion: 'Incluye ocultos.',                       compat_notes: null }
        ],
        gotchas: [
          'No viene de serie en Git Bash; instalable con pacman: `pacman -S tree` (en Git Bash con MSYS2 nuevo). Si no, alternativa: `find . -type d` o `ls -R`.'
        ]
      }
      ,{
        id: 'file',
        tipo: 'comando', nombre: 'file', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Identifica el tipo de un archivo inspeccionando su contenido (no su extensión).',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'file imagen.png',     explicacion: 'Detecta que es PNG aunque le quites la extensión.', compat_notes: null },
          { comando: 'file *',              explicacion: 'Tipos de todos los archivos del directorio.',        compat_notes: null }
        ]
      }
      ,{
        id: 'which',
        tipo: 'comando', nombre: 'which', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Muestra la ruta del ejecutable que se invoca al escribir un comando (busca en $PATH).',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'which git',     explicacion: 'Ruta del binario de git.',         compat_notes: null },
          { comando: 'which python',  explicacion: 'Útil para diagnosticar versiones.', compat_notes: null }
        ]
      }
      ,{
        id: 'whereis',
        tipo: 'comando', nombre: 'whereis', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Muestra ubicación del binario, fuente y manual de un comando.',
        gitbash: 'no-disponible',
        ejemplos: [
          { comando: 'whereis git', explicacion: 'Binario, source, man pages.', compat_notes: 'No disponible en Git Bash. En su lugar, usar `which git`.' }
        ]
      }
      ,{
        id: 'type',
        tipo: 'comando', nombre: 'type', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Indica si un nombre es un alias, función, builtin o ejecutable externo.',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'type cd',        explicacion: 'Te dirá que es un builtin del shell.',         compat_notes: null },
          { comando: 'type -a python', explicacion: 'Lista TODAS las definiciones (alias + binarios) en orden de prioridad.', compat_notes: null }
        ],
        gotchas: [
          'Más informativo que `which` para diagnosticar por qué un comando se comporta raro: te dice si lo está secuestrando un alias.'
        ]
      }
      ,{
        id: 'locate',
        tipo: 'comando', nombre: 'locate', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Búsqueda instantánea en una base de datos pre-indexada de rutas.',
        gitbash: 'no-disponible',
        ejemplos: [
          { comando: 'locate config.yml', explicacion: 'Devuelve todas las rutas que contienen "config.yml".', compat_notes: null }
        ],
        gotchas: [
          'Lee de una BD generada por `updatedb` — puede estar desactualizada.',
          'No disponible en Git Bash. Alternativas en Git Bash: `find / -name PATRON 2>/dev/null` (lento) o instalar `mlocate` en WSL/Linux.'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 11 tarjetas en total. `whereis` y `locate` con punto rojo. Marcar checkbox "Compatible con Git Bash" → desaparecen ambas, quedan 9 tarjetas.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Cargar comandos básicos de búsqueda de archivos (ls, pwd, tree, file, which, whereis, type, locate)"
```

---

### Task 17: Cargar comandos básicos de búsqueda de contenido (head, tail)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Añadir tarjetas de head y tail**

Insertar después de la tarjeta de locate:

```js
      ,{
        id: 'head',
        tipo: 'comando', nombre: 'head', categoria: 'busqueda-contenido', dificultad: 'basico',
        descripcion: 'Muestra las primeras líneas de un archivo o stream.',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'head app.log',      explicacion: 'Primeras 10 líneas (por defecto).',     compat_notes: null },
          { comando: 'head -n 50 app.log', explicacion: 'Primeras 50 líneas.',                    compat_notes: null },
          { comando: "ls | head -n 20",    explicacion: 'Primeros 20 elementos de un listado.',  compat_notes: null }
        ],
        flags: [
          { flag: '-n N', descripcion: 'Número de líneas (por defecto 10).', compat_notes: null },
          { flag: '-c N', descripcion: 'Número de bytes en lugar de líneas.', compat_notes: null }
        ]
      }
      ,{
        id: 'tail',
        tipo: 'comando', nombre: 'tail', categoria: 'busqueda-contenido', dificultad: 'basico',
        descripcion: 'Muestra las últimas líneas de un archivo o stream.',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'tail app.log',          explicacion: 'Últimas 10 líneas.',                                  compat_notes: null },
          { comando: 'tail -n 100 app.log',   explicacion: 'Últimas 100 líneas.',                                 compat_notes: null },
          { comando: 'tail -f app.log',       explicacion: 'Sigue el archivo en tiempo real (logs en vivo).',     compat_notes: null },
          { comando: 'tail -F app.log',       explicacion: 'Como -f pero reabre el archivo si se rota (logrotate).', compat_notes: null }
        ],
        flags: [
          { flag: '-n N',  descripcion: 'Número de líneas.',                                            compat_notes: null },
          { flag: '-n +N', descripcion: 'Empieza en la línea N (no las últimas N).',                    compat_notes: null },
          { flag: '-f',    descripcion: 'Follow: muestra nuevas líneas a medida que se escriben.',      compat_notes: null },
          { flag: '-F',    descripcion: 'Como -f pero soporta rotación de archivos.',                   compat_notes: null }
        ],
        gotchas: [
          'Ctrl+C para salir del modo -f / -F.'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. 13 tarjetas en total. Filtrar Categoría = "Búsqueda de contenido" → solo `grep`, `head`, `tail` (3) — los operadores no se cargan aún.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Cargar comandos básicos de búsqueda de contenido (head, tail)"
```

---

### Task 18: Cargar operadores del shell

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Añadir las 8 tarjetas de operadores (sin `categoria` — son transversales)**

Insertar después de la tarjeta de tail:

```js
      ,{
        id: 'op-pipe',
        tipo: 'operador', nombre: '|', dificultad: 'basico',
        descripcion: 'Pipe: conecta stdout de un comando a stdin del siguiente.',
        gitbash: 'ok',
        modelo_mental: 'Forma una cadena: la salida del comando de la izquierda se convierte en la entrada del de la derecha. Cada etapa procesa el stream línea a línea (en general). Es el pegamento básico del shell.',
        ejemplos: [
          { comando: 'ls | head',            explicacion: 'Primeros 10 elementos del listado.',                       compat_notes: null },
          { comando: "cat app.log | grep ERROR | wc -l", explicacion: 'Cuenta líneas con ERROR en app.log.',          compat_notes: null }
        ],
        gotchas: [
          'El último comando del pipeline determina el código de salida (por defecto). Para detectar fallos en cualquier eslabón: `set -o pipefail`.',
          'No usar `cat archivo | grep patron` — `grep patron archivo` es directo y más eficiente (anti-patrón "useless cat").'
        ]
      }
      ,{
        id: 'op-cmd-substitution',
        tipo: 'operador', nombre: '$( )', dificultad: 'intermedio',
        descripcion: 'Command substitution: ejecuta un comando y sustituye su salida en el lugar.',
        gitbash: 'ok',
        modelo_mental: '`$(cmd)` ejecuta cmd y reemplaza la expresión por su stdout (sin salto final). Permite construir comandos a partir del resultado de otros. Es preferible a la forma antigua con backticks (`cmd`) porque anida bien.',
        ejemplos: [
          { comando: 'echo "Hoy es $(date +%Y-%m-%d)"',           explicacion: 'Inyecta la salida de `date` en la cadena.',  compat_notes: null },
          { comando: 'cd $(dirname "$0")',                         explicacion: 'Va al directorio del script.',              compat_notes: null },
          { comando: "grep ERROR $(find . -name '*.log')",         explicacion: 'Busca en los archivos que find devuelve.',  compat_notes: null }
        ],
        gotchas: [
          'Si la salida tiene espacios o saltos, puede romper el comando exterior — comillas dobles alrededor cuando lo usas como argumento individual.',
          'Para listas de archivos producidas por find, prefiere `find ... -exec` o `find ... | xargs` para manejar nombres con espacios.'
        ]
      }
      ,{
        id: 'op-redirect-stdout',
        tipo: 'operador', nombre: '>', dificultad: 'basico',
        descripcion: 'Redirige stdout a un archivo (sobrescribe).',
        gitbash: 'ok',
        modelo_mental: 'Toma la salida estándar del comando de la izquierda y la escribe en el archivo de la derecha, **sobrescribiendo** lo que hubiera. Errores (stderr) NO se redirigen — siguen yendo al terminal.',
        ejemplos: [
          { comando: "find . -name '*.log' > logs.txt",  explicacion: 'Guarda el listado en logs.txt (sobrescribe).',  compat_notes: null },
          { comando: 'date > timestamp.txt',              explicacion: 'Escribe la fecha actual en el archivo.',        compat_notes: null }
        ],
        gotchas: [
          '> sobrescribe sin avisar. Para añadir, usar >>.',
          'No redirige stderr. Para todo: `cmd > out 2>&1` o `cmd &> out` (bash).'
        ]
      }
      ,{
        id: 'op-redirect-append',
        tipo: 'operador', nombre: '>>', dificultad: 'basico',
        descripcion: 'Redirige stdout a un archivo, añadiendo al final (no sobrescribe).',
        gitbash: 'ok',
        modelo_mental: 'Como `>` pero **append**: si el archivo existe, escribe al final; si no, lo crea.',
        ejemplos: [
          { comando: "echo 'línea' >> notas.txt", explicacion: 'Añade una línea al final.',          compat_notes: null },
          { comando: 'date >> log.txt',           explicacion: 'Cada ejecución suma una línea.',     compat_notes: null }
        ]
      }
      ,{
        id: 'op-stderr-stdout',
        tipo: 'operador', nombre: '2>&1', dificultad: 'intermedio',
        descripcion: 'Redirige stderr (descriptor 2) al mismo destino que stdout (descriptor 1).',
        gitbash: 'ok',
        modelo_mental: 'Cada proceso tiene 3 descriptores: 0 stdin, 1 stdout, 2 stderr. `2>&1` indica "envía stderr a donde esté yendo stdout en este momento". Importa el orden: `> out 2>&1` mezcla todo en out, pero `2>&1 > out` redirige stderr al terminal y luego stdout al archivo.',
        ejemplos: [
          { comando: 'cmd > out.txt 2>&1',  explicacion: 'Stdout y stderr al archivo.',           compat_notes: null },
          { comando: 'cmd 2> errors.log',   explicacion: 'Solo stderr al archivo.',                compat_notes: null },
          { comando: 'cmd &> out.txt',      explicacion: 'Atajo bash para `> out 2>&1`.',          compat_notes: null }
        ],
        gotchas: [
          'El orden importa: `> out 2>&1` ≠ `2>&1 > out`. La regla: 2>&1 hace una copia del descriptor 1 EN ESE MOMENTO, no lo sigue.',
          '`&> archivo` es atajo de bash; sh/dash no lo soportan.'
        ]
      }
      ,{
        id: 'op-and',
        tipo: 'operador', nombre: '&&', dificultad: 'basico',
        descripcion: 'AND lógico: ejecuta el segundo comando solo si el primero termina con éxito (exit code 0).',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'mkdir build && cd build', explicacion: 'Solo entra en build si se creó.',                          compat_notes: null },
          { comando: 'tests && deploy.sh',       explicacion: 'Solo despliega si los tests pasan.',                       compat_notes: null }
        ]
      }
      ,{
        id: 'op-or',
        tipo: 'operador', nombre: '||', dificultad: 'basico',
        descripcion: 'OR lógico: ejecuta el segundo comando solo si el primero falla (exit code != 0).',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'cmd || echo "fallo"',          explicacion: 'Imprime "fallo" si cmd salió con error.', compat_notes: null },
          { comando: '[ -d build ] || mkdir build',  explicacion: 'Crea build si no existe.',                  compat_notes: null }
        ]
      }
      ,{
        id: 'op-semicolon',
        tipo: 'operador', nombre: ';', dificultad: 'basico',
        descripcion: 'Separador secuencial: ejecuta los comandos en orden, independientemente del éxito o fallo.',
        gitbash: 'ok',
        ejemplos: [
          { comando: 'cd /tmp; ls; echo "fin"', explicacion: 'Ejecuta los tres en orden, pase lo que pase.', compat_notes: null }
        ],
        gotchas: [
          'Si quieres parar al primer fallo, usar && entre comandos en lugar de ;.'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:** 21 tarjetas. Operadores con badge violeta. Filtrar Tipo = "Operador" → 8 tarjetas. Filtrar Categoría = "Búsqueda de archivos" → comandos de búsqueda-archivos + los 8 operadores transversales (regla §5.2).

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Cargar operadores del shell (pipe, command substitution, redirecciones, encadenado)"
```

---

### Task 19: Cargar recetas (§7.3)

**Files:**
- Modify: `guia-bash/guia-bash.html` (array `COMANDOS`)

- [ ] **Step 1: Añadir las 6 recetas del spec**

Insertar después del operador `;`:

```js
      ,{
        id: 'receta-grep-en-proyecto',
        tipo: 'receta', nombre: 'Buscar texto en un proyecto', categoria: 'busqueda-contenido', dificultad: 'basico',
        descripcion: 'Localiza una cadena/regex en todo el árbol de un proyecto.',
        gitbash: 'ok',
        escenario: 'Quiero saber dónde se usa una cadena (función, variable, mensaje) en mi código.',
        ejemplos: [
          { comando: "grep -rn 'patron' .", explicacion: 'Recursivo, mostrando número de línea.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'grep',          hace: 'busca patrón en archivos.' },
          { fragmento: '-r',            hace: 'recursivamente desde el directorio dado.' },
          { fragmento: '-n',            hace: 'incluye número de línea de cada match.' },
          { fragmento: "'patron'",      hace: 'la cadena/regex a buscar (comillas evitan que el shell la interprete).' },
          { fragmento: '.',             hace: 'directorio raíz desde el que buscar.' }
        ],
        relacionados: ['grep']
      }
      ,{
        id: 'receta-archivos-modificados',
        tipo: 'receta', nombre: 'Archivos modificados en los últimos N días', categoria: 'busqueda-archivos', dificultad: 'basico',
        descripcion: 'Localiza archivos cuyo contenido cambió en los últimos N días.',
        gitbash: 'ok',
        escenario: 'Quiero ver qué se ha tocado esta semana, sin depender del control de versiones.',
        ejemplos: [
          { comando: 'find . -type f -mtime -7', explicacion: 'Archivos modificados en los últimos 7 días.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'find .',     hace: 'recorre desde el directorio actual.' },
          { fragmento: '-type f',    hace: 'solo archivos regulares (excluye directorios).' },
          { fragmento: '-mtime -7',  hace: 'modificados hace MENOS de 7 días (signo - = antigüedad menor).' }
        ],
        relacionados: ['find']
      }
      ,{
        id: 'receta-archivos-modif-con-texto',
        tipo: 'receta', nombre: 'Archivos modificados que contengan texto', categoria: 'busqueda-contenido', dificultad: 'intermedio',
        descripcion: 'Combina filtro temporal (find) con búsqueda de contenido (grep).',
        gitbash: 'ok',
        escenario: 'Quiero ver dónde he dejado TODO esta semana (en código que he tocado).',
        ejemplos: [
          { comando: "find . -type f -mtime -7 | xargs grep -l 'TODO'", explicacion: 'Lista archivos recientes que contienen TODO.', compat_notes: null }
        ],
        desglose: [
          { fragmento: 'find . -type f -mtime -7', hace: 'archivos modificados últimos 7 días.' },
          { fragmento: '|',                         hace: 'pasa la lista por stdin.' },
          { fragmento: 'xargs grep -l',             hace: "invoca grep -l (solo nombres) con esa lista." },
          { fragmento: "'TODO'",                    hace: 'patrón a buscar.' }
        ],
        relacionados: ['find', 'xargs', 'grep', 'op-pipe']
      }
      ,{
        id: 'receta-top-grandes',
        tipo: 'receta', nombre: 'Top 10 archivos más grandes', categoria: 'busqueda-archivos', dificultad: 'intermedio',
        descripcion: 'Identifica los archivos que más espacio ocupan en un árbol.',
        gitbash: 'caveats',
        escenario: 'El proyecto pesa mucho y quiero saber qué archivos lo inflan.',
        ejemplos: [
          { comando: "find . -type f -printf '%s %p\\n' | sort -rn | head -10", explicacion: 'Tamaño + ruta, ordenado descendente, primeros 10.', compat_notes: 'find -printf NO está en Git Bash. Alternativa portable: `find . -type f -exec stat -c "%s %n" {} + | sort -rn | head -10` (también Linux). En Git Bash más limitado: `find . -type f -exec wc -c {} + | sort -rn | head -10` (incluye totales falsos al final, ignorar la última línea).' }
        ],
        desglose: [
          { fragmento: 'find . -type f',           hace: 'lista archivos regulares.' },
          { fragmento: "-printf '%s %p\\n'",       hace: 'formato: tamaño en bytes + ruta + salto.' },
          { fragmento: '| sort -rn',                hace: 'ordena numéricamente descendente.' },
          { fragmento: '| head -10',                hace: 'se queda con los 10 primeros.' }
        ],
        relacionados: ['find', 'op-pipe', 'head']
      }
      ,{
        id: 'receta-excluir-node-modules',
        tipo: 'receta', nombre: 'Excluir node_modules en una búsqueda', categoria: 'busqueda-archivos', dificultad: 'intermedio',
        descripcion: 'Recorrido del árbol que ignora node_modules (o cualquier subdirectorio).',
        gitbash: 'ok',
        escenario: "Quiero buscar archivos de mi código sin que find se meta en node_modules y tarde una eternidad.",
        ejemplos: [
          { comando: "find . -path './node_modules' -prune -o -type f -print", explicacion: 'Lista archivos excluyendo node_modules.', compat_notes: null }
        ],
        desglose: [
          { fragmento: "-path './node_modules'", hace: 'condición: rutas que matcheen ./node_modules.' },
          { fragmento: '-prune',                 hace: 'no descender ahí.' },
          { fragmento: '-o',                     hace: 'OR.' },
          { fragmento: '-type f -print',         hace: 'para el resto, si es archivo regular, imprimirlo.' }
        ],
        relacionados: ['find']
      }
      ,{
        id: 'receta-contar-lineas',
        tipo: 'receta', nombre: 'Contar líneas de código', categoria: 'busqueda-contenido', dificultad: 'basico',
        descripcion: 'Cuenta líneas totales de archivos por extensión.',
        gitbash: 'ok',
        escenario: 'Quiero saber cuántas líneas .js hay en mi proyecto.',
        ejemplos: [
          { comando: "find . -name '*.js' | xargs wc -l", explicacion: 'Cuenta líneas de todos los .js. La última línea es el total.', compat_notes: null }
        ],
        desglose: [
          { fragmento: "find . -name '*.js'", hace: 'lista archivos .js.' },
          { fragmento: '|',                    hace: 'pipe a xargs.' },
          { fragmento: 'xargs wc -l',          hace: 'invoca wc -l (líneas) con la lista.' }
        ],
        relacionados: ['find', 'xargs', 'op-pipe'],
        gotchas: [
          'wc -l incluye una línea "total" al final cuando recibe varios archivos.',
          'Si hay archivos con espacios en el nombre, usar `find ... -print0 | xargs -0 wc -l`.'
        ]
      }
```

- [ ] **Step 2: Verificar**

Recargar. **Esperado:**
- 27 tarjetas en total.
- Filtrar Tipo = "Receta" → 6 tarjetas.
- Filtrar Categoría = "Búsqueda de contenido" → grep, head, tail, los 8 operadores (transversales) y las 3 recetas de búsqueda-contenido = 14 tarjetas.

- [ ] **Step 3: Commit**

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "Cargar 6 recetas de búsqueda (§7.3 del spec)"
```

---

### Task 20: QA pass + checklist de aceptación + commit final

**Files:**
- Manual: validación visual completa.
- Modify: `guia-bash/guia-bash.html` solo si surge algún fallo.

- [ ] **Step 1: Recargar y validar el banner de tests**

Banner verde "✓ N tests OK" (al menos 12: 1 trivial + 8 applyFilters + 3 renderCard). Sin errores en consola.

- [ ] **Step 2: Validar el catálogo cargado**

Conteo esperado de tarjetas con filtros sin aplicar: 27.
- 11 comandos (find, grep, xargs, ls, pwd, tree, file, which, whereis, type, locate)
- 2 comandos básicos de contenido (head, tail) — total comandos: 13
- 8 operadores
- 6 recetas

Total: 13 + 8 + 6 = 27 tarjetas.

- [ ] **Step 3: Casos de uso de filtros (verificación manual)**

Probar cada uno y observar el conteo:

| Acción | Conteo esperado | Notas |
|--------|-----------------|-------|
| Sin filtros | 27 | |
| Categoría = Búsqueda de archivos | 8 comandos + 8 operadores transversales + 3 recetas categoría arch = 19 | locate y whereis cuentan; los operadores aparecen siempre |
| Categoría = Búsqueda de contenido | 3 comandos (grep, head, tail) + 8 operadores + 3 recetas = 14 | |
| Tipo = Comando | 13 | |
| Tipo = Operador | 8 | |
| Tipo = Receta | 6 | |
| Compatible Git Bash (checkbox) | 25 | excluye solo whereis y locate (`gitbash: "no-disponible"`) |
| Búsqueda libre = "log" | varios (find, grep, head, tail, op-redirect... tienen "log" en algún lado) | verificar que ninguno revienta |
| Búsqueda + Categoría + Tipo combinados | depende | aplica AND |
| Tipo = Comando + Compatible Git Bash | 11 | quita whereis y locate |
| Sin resultados (Tipo = Sintaxis no existe en dropdown actual; Búsqueda = "xzqzqz") | 0 → Empty state | |

- [ ] **Step 4: Casos de uso de la tarjeta**

- Hover sobre bloque de código → aparece botón "📋".
- Click → cambia a "✓ copiado" verde durante 1.5s.
- Pegar el comando en otro sitio → se pega correctamente.
- Tarjetas gordas (find, grep, xargs, recetas) tienen "Ver más ▾" — abrir y cerrar.
- Tarjetas con `gitbash: 'caveats'` (find, grep, tree) → punto amarillo + warnings amarillos en flags/ejemplos correspondientes.
- Tarjetas con `gitbash: 'no-disponible'` (whereis, locate) → punto rojo.

- [ ] **Step 5: Comprobar que el archivo abre sin servidor**

Cerrar todos los navegadores. Hacer doble click sobre `guia-bash.html` en el explorador de Windows. **Esperado:** se abre, todo funciona, copy button funciona (con fallback si Firefox).

- [ ] **Step 6: Si todo OK, commit final de cierre**

Si no se ha cambiado nada en este Task 20, no hace falta commit. Si se ha tenido que ajustar algo durante el QA:

```bash
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje add guia-bash/guia-bash.html
git -C C:/Users/Marius/Desktop/Programacion/Aprendizaje commit -m "QA pass iteración 1 — ajustes finales"
```

---

## Self-review tras escribir el plan

Hecho por la skill antes de entregarlo:

- **Cobertura del spec:**
  - §1 Objetivo (consulta rápida + aprender) → cubierto por contenido de tarjetas (modelo_mental, gotchas) en T13–T15.
  - §2 Iteraciones — solo iteración 1 → ✓.
  - §3 Stack — single-file, Tailwind CDN, vanilla JS, datos embebidos → T1.
  - §4 Entorno + §4.1 compatibilidad fina → T8 (indicador), T13–T18 (compat_notes en flags).
  - §5.1 Layout → T1, T3.
  - §5.2 Filtros (categoría, dificultad, tipo, gitbash, search) + transversales → T3, T4 (lógica + tests específicos), T6 (wire-up).
  - §5.3 Empty state → T7.
  - §5.4 Plegable + copiar con fallback + feedback → T9, T10.
  - §5.5 Modo oscuro → diferido al implementador (sin tarea explícita; aceptado por el spec).
  - §6 Esquema JSON (con `tipo`, `categoria`, `gitbash` 3-state, `patron`, ID conventions) → T4 tests, T13+ datos.
  - §6.1 Reglas por tipo (básica/gorda/operador/receta) → T13–T19 cubren cada tipo.
  - §7.2 Operadores reales del shell → T18.
  - §7.3 Recetas → T19.
  - §8 Catálogo inicial → T13–T19. egrep/fgrep mencionados en `gotchas` de grep (T14).
  - §9 Lo que NO debe hacer → respetado en plan (no frameworks, no build, no localStorage, no badges globales sustitutos, no flags como tarjetas, no fundir tipo y categoría).
- **Placeholders:** ninguno (`assert(...)` con mensaje real, datos completos en cada `COMANDOS`).
- **Type consistency:** `gitbash` siempre uno de los 3 valores; `tipo` siempre uno de los 4 (sintaxis no se usa en data, pero el badge sí lo conoce); `categoria` solo `busqueda-archivos`/`busqueda-contenido` o ausente; `state` mismas keys en T4, T6, T7.
- **Convención de IDs:** comandos con nombre tal cual (`find`, `grep`); operadores con prefijo `op-` (`op-pipe`, `op-cmd-substitution`, `op-redirect-stdout`, `op-redirect-append`, `op-stderr-stdout`, `op-and`, `op-or`, `op-semicolon`); recetas con prefijo `receta-`. Coherente con spec §6.

---

## Plan complete and saved

Plan guardado en `C:\Users\Marius\Desktop\Programacion\Aprendizaje\guia-bash\plans\2026-04-26-iteracion-1-busqueda.md`.

**Dos opciones de ejecución:**

1. **Subagent-Driven (recommended)** — dispatch de un subagent fresh por tarea, revisión entre tareas, iteración rápida.
2. **Inline Execution** — ejecutar las tareas en esta misma sesión usando `executing-plans`, ejecución por lotes con checkpoints de revisión.

Pendiente: elegir aproximación.
