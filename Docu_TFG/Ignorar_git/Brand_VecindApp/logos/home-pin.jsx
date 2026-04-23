// ───────────────────────────────────────────────────────
// VecindApp — Marca Home-Pin
// Isotipo refinado construido sobre retícula 240×240
// ───────────────────────────────────────────────────────

const BRAND = {
  teal:     '#0f6b7a',
  tealDark: '#094a55',
  tealDeep: '#063840',
  coral:    '#e97a4d',
  coralDark:'#c25d33',
  sand:     '#f6f4ee',
  sandDark: '#ebe6d9',
  paper:    '#ffffff',
  ink:      '#0e1a2b',
  inkSoft:  '#5b6b82',
};

// ─── Isotipo refinado ─────────────────────────────────
//  Basado en retícula 240×240, radius 56 (28%)
//  Pin: curva compuesta, fondo teal
//  Casa: tejado con pendiente 1:1.2, puerta centrada
//  Punto: Ø 22, color coral, centrado en el "medio pin"
//  Incluye variante `flat` (sin tile) para favicon, y
//  variante `mono` para impresión a un color.
// ──────────────────────────────────────────────────────
function HomePin({
  size = 240,
  variant = 'tile',     // 'tile' | 'flat' | 'knockout' | 'mono'
  tileColor = BRAND.teal,
  pinColor  = BRAND.paper,
  houseColor = BRAND.teal,
  dotColor  = BRAND.coral,
  radius = 56,
  showGrid = false,
}) {
  // ── Colors per variant ──
  let tile = tileColor, pin = pinColor, house = houseColor, dot = dotColor;
  if (variant === 'flat') {
    tile = 'transparent'; pin = BRAND.teal; house = BRAND.paper; dot = BRAND.coral;
  } else if (variant === 'knockout') {
    tile = BRAND.teal; pin = BRAND.paper; house = BRAND.teal; dot = BRAND.coral;
  } else if (variant === 'mono') {
    tile = 'transparent'; pin = BRAND.ink; house = BRAND.paper; dot = BRAND.ink;
  }

  // Pin shape on 240 grid — teardrop; apex at 120,210; head circle-y around 120,108 r=64
  const pin_d = `
    M 120 40
    C 82 40, 56 66, 56 104
    C 56 124, 68 146, 84 166
    C 96 181, 110 197, 120 210
    C 130 197, 144 181, 156 166
    C 172 146, 184 124, 184 104
    C 184 66, 158 40, 120 40 Z
  `;

  // House (roof + body) centered at pin's head (120, 108)
  //   roof: wide ridge
  //   body: rectangle with a door
  const house_d = `
    M 86 116
    L 120 88
    L 154 116
    L 154 136
    L 130 136
    L 130 122
    L 110 122
    L 110 136
    L 86 136
    Z
  `;

  return (
    <svg
      viewBox="0 0 240 240"
      width={size}
      height={size}
      style={{ display: 'block' }}
      role="img"
      aria-label="VecindApp"
    >
      {/* Optional construction grid */}
      {showGrid && (
        <g>
          <rect x="0" y="0" width="240" height="240" fill="none" stroke="rgba(15,107,122,0.25)" strokeWidth="1"/>
          {Array.from({length: 11}).map((_, i) => (
            <g key={i}>
              <line x1={i*24} y1="0" x2={i*24} y2="240" stroke="rgba(15,107,122,0.12)" strokeWidth="0.6"/>
              <line x1="0" y1={i*24} x2="240" y2={i*24} stroke="rgba(15,107,122,0.12)" strokeWidth="0.6"/>
            </g>
          ))}
          {/* construction circles */}
          <circle cx="120" cy="104" r="64" fill="none" stroke="rgba(233,122,77,0.5)" strokeWidth="0.6"/>
          <line x1="120" y1="0" x2="120" y2="240" stroke="rgba(233,122,77,0.4)" strokeWidth="0.6" strokeDasharray="2 3"/>
        </g>
      )}

      {/* Tile */}
      {variant !== 'flat' && variant !== 'mono' && (
        <rect x="0" y="0" width="240" height="240" rx={radius} fill={tile}/>
      )}

      {/* Pin */}
      <path d={pin_d} fill={pin}/>

      {/* House */}
      <path d={house_d} fill={house}/>

      {/* Hour dot — the V of VecindApp */}
      <circle cx="120" cy="102" r="10" fill={dot}/>

      {/* Grid overlay on top of the shapes so it doesn't get obscured */}
      {showGrid && (
        <g>
          {/* key measurements */}
          <circle cx="120" cy="102" r="10" fill="none" stroke="rgba(233,122,77,0.7)" strokeWidth="0.6"/>
          <circle cx="120" cy="210" r="3" fill="rgba(233,122,77,0.7)"/>
          <circle cx="120" cy="40" r="3" fill="rgba(233,122,77,0.7)"/>
        </g>
      )}
    </svg>
  );
}

// ─── Wordmark ───────────────────────────────────────────
//  Lockup vertical centrado (inverso coral sobre sand):
//    • isotipo tile coral, casa sand, punto teal
//    • texto: "Vecind" sand · "App" teal · "." coral (NO coral on coral — aquí el punto es teal para armar contraste sobre fondo sand)
//  Para versión "clara" mantenemos: fondo sand, texto ink, App teal, . coral.
// ────────────────────────────────────────────────────────

function HomePinInverse({ size = 240 }) {
  return (
    <HomePin
      size={size}
      variant="tile"
      tileColor={BRAND.coral}
      pinColor={BRAND.sand}
      houseColor={BRAND.coral}
      dotColor={BRAND.teal}
    />
  );
}

function Wordmark({
  size = 40,
  style = 'light',      // 'light' (sand bg) | 'dark' (ink bg) | 'teal' (teal bg) | 'inverse' (coral bg)
  tagline,
}) {
  let vecind, app, dot, tag;
  if (style === 'light') {
    vecind = BRAND.ink;   app = BRAND.teal;  dot = BRAND.coral; tag = BRAND.inkSoft;
  } else if (style === 'dark') {
    vecind = BRAND.paper; app = '#7ec2cd';   dot = BRAND.coral; tag = 'rgba(255,255,255,0.55)';
  } else if (style === 'teal') {
    vecind = BRAND.paper; app = BRAND.sand;  dot = BRAND.coral; tag = 'rgba(255,255,255,0.7)';
  } else if (style === 'inverse') {
    vecind = BRAND.sand;  app = BRAND.teal;  dot = BRAND.tealDark; tag = 'rgba(255,255,255,0.7)';
  }
  return (
    <div style={{
      display:'flex', flexDirection:'column', alignItems:'center', gap: size*0.28,
      fontFamily: 'Inter, -apple-system, "Segoe UI", sans-serif',
    }}>
      <div style={{
        fontWeight: 700, letterSpacing: '-0.025em',
        fontSize: size, lineHeight: 1,
        color: vecind,
      }}>
        Vecind<span style={{color: app}}>App</span><span style={{color: dot}}>.</span>
      </div>
      {tagline && (
        <div style={{
          fontSize: size*0.22, fontWeight: 500,
          letterSpacing: '0.22em', textTransform:'uppercase',
          color: tag,
        }}>{tagline}</div>
      )}
    </div>
  );
}

Object.assign(window, { BRAND, HomePin, HomePinInverse, Wordmark });
