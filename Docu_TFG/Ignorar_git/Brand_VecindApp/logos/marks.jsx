// All VecindApp logo marks — pure SVG components
// Palette: teal primary + warm coral accent
const P = {
  teal: '#0f6b7a',
  tealDark: '#094a55',
  tealLight: '#3a95a3',
  coral: '#e97a4d',
  coralDark: '#c25d33',
  sand: '#f6f4ee',
  ink: '#0e1a2b',
  paper: '#ffffff',
};

// helper: make a rounded-square tile (Android app-icon adaptive mask)
function Tile({ bg, children, size = 200, radius = 46 }) {
  return (
    <svg viewBox="0 0 200 200" width={size} height={size} style={{ display: 'block' }}>
      <rect x="0" y="0" width="200" height="200" rx={radius} fill={bg} />
      {children}
    </svg>
  );
}

// ──────────────────────────────────────────────────────────
// 01 — "HOME-PIN"  casa + pin de ubicación fusionados
//     punto cálido = saldo de horas
// ──────────────────────────────────────────────────────────
function Mark01({ size = 200 }) {
  return (
    <Tile bg={P.teal} size={size}>
      {/* pin silhouette */}
      <path
        d="M100 36c-30 0-52 22-52 50 0 30 36 68 52 84 16-16 52-54 52-84 0-28-22-50-52-50z"
        fill="#fff"
      />
      {/* house cut into pin */}
      <path
        d="M100 60 L72 82 L72 112 L90 112 L90 96 L110 96 L110 112 L128 112 L128 82 Z"
        fill={P.teal}
      />
      {/* warm dot — hour */}
      <circle cx="100" cy="88" r="7" fill={P.coral} />
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 02 — "CLOCK-HOUSE"  reloj con tejado (banco de tiempo + casa)
// ──────────────────────────────────────────────────────────
function Mark02({ size = 200 }) {
  return (
    <Tile bg={P.sand} size={size}>
      {/* roof */}
      <path d="M40 80 L100 38 L160 80 Z" fill={P.coral} />
      {/* clock body */}
      <circle cx="100" cy="118" r="48" fill={P.teal} />
      <circle cx="100" cy="118" r="38" fill={P.paper} />
      {/* hands */}
      <line x1="100" y1="118" x2="100" y2="90" stroke={P.teal} strokeWidth="5" strokeLinecap="round" />
      <line x1="100" y1="118" x2="120" y2="118" stroke={P.coral} strokeWidth="5" strokeLinecap="round" />
      <circle cx="100" cy="118" r="4" fill={P.tealDark} />
      {/* 12 marks */}
      {[0,3,6,9].map(i=>{
        const a = (i/12)*Math.PI*2 - Math.PI/2;
        const x1 = 100 + Math.cos(a)*33, y1 = 118 + Math.sin(a)*33;
        const x2 = 100 + Math.cos(a)*38, y2 = 118 + Math.sin(a)*38;
        return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke={P.teal} strokeWidth="3" strokeLinecap="round" />;
      })}
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 03 — "HAND-HEART-ROOF"  tejado + corazón + mano (acogida)
// ──────────────────────────────────────────────────────────
function Mark03({ size = 200 }) {
  return (
    <Tile bg={P.paper} size={size}>
      {/* teal house block */}
      <rect x="36" y="84" width="128" height="92" rx="8" fill={P.teal}/>
      {/* roof */}
      <path d="M28 92 L100 40 L172 92 Z" fill={P.tealDark}/>
      {/* heart inside */}
      <path
        d="M100 160
           C 70 138, 58 120, 68 104
           C 76 92, 90 94, 100 108
           C 110 94, 124 92, 132 104
           C 142 120, 130 138, 100 160 Z"
        fill={P.coral}
      />
      {/* chimney */}
      <rect x="130" y="58" width="14" height="20" fill={P.coral}/>
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 04 — "V-NETWORK"  letra V formada por nodos/red de personas
// ──────────────────────────────────────────────────────────
function Mark04({ size = 200 }) {
  const nodes = [
    [48, 50], [88, 50], [128, 50], [168, 50],
    [68, 96], [108, 96], [148, 96],
    [88, 142], [128, 142],
    [108, 178],
  ];
  const lines = [
    [0,4],[1,4],[1,5],[2,5],[2,6],[3,6],
    [4,7],[5,7],[5,8],[6,8],
    [7,9],[8,9],
  ];
  return (
    <Tile bg={P.tealDark} size={size}>
      {lines.map(([a,b],i)=>(
        <line key={i}
          x1={nodes[a][0]} y1={nodes[a][1]}
          x2={nodes[b][0]} y2={nodes[b][1]}
          stroke="rgba(255,255,255,0.25)" strokeWidth="2"
        />
      ))}
      {nodes.map(([x,y],i)=>{
        const isAccent = i===0 || i===3 || i===9;
        return <circle key={i} cx={x} cy={y} r={i===9?10:8}
          fill={isAccent ? P.coral : '#fff'} />;
      })}
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 05 — "SPEECH-HOUSE"  burbuja de diálogo con tejado
// ──────────────────────────────────────────────────────────
function Mark05({ size = 200 }) {
  return (
    <Tile bg={P.coral} size={size}>
      {/* speech bubble */}
      <path
        d="M50 56 H150 a16 16 0 0 1 16 16 v60 a16 16 0 0 1 -16 16 H120 L96 170 L98 148 H50 a16 16 0 0 1 -16 -16 V72 a16 16 0 0 1 16 -16 z"
        fill={P.paper}
      />
      {/* roof inside bubble */}
      <path d="M58 90 L100 62 L142 90 Z" fill={P.teal}/>
      {/* door suggesting hour "I" */}
      <rect x="94" y="100" width="12" height="30" rx="2" fill={P.teal}/>
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 06 — "GEO ABSTRACT"  hexágono modular (barrio + red)
// ──────────────────────────────────────────────────────────
function Mark06({ size = 200 }) {
  // seven hexagon grid, centered hex cut as a house/V
  const hex = (cx, cy, r=22) => {
    const pts = [];
    for (let i=0;i<6;i++){
      const a = (i/6)*Math.PI*2 + Math.PI/6;
      pts.push([cx + Math.cos(a)*r, cy + Math.sin(a)*r]);
    }
    return pts.map(p=>p.join(',')).join(' ');
  };
  const r = 26;
  const dx = r * Math.sqrt(3);
  const dy = r * 1.5;
  const centers = [
    [100, 100],
    [100 - dx, 100 - dy], [100 + dx, 100 - dy],
    [100 - dx, 100 + dy], [100 + dx, 100 + dy],
    [100, 100 - dy*2], [100, 100 + dy*2],
  ];
  return (
    <Tile bg={P.paper} size={size}>
      {centers.map((c,i)=>(
        <polygon key={i} points={hex(c[0],c[1],22)}
          fill={i===0 ? P.coral : (i%2===0 ? P.teal : P.tealLight)}/>
      ))}
      {/* house cut in center */}
      <path d="M88 106 L100 94 L112 106 L112 114 L104 114 L104 108 L96 108 L96 114 L88 114 Z" fill="#fff"/>
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 07 — "V MONOGRAM"  V tipográfica con tejado incorporado
// ──────────────────────────────────────────────────────────
function Mark07({ size = 200 }) {
  return (
    <Tile bg={P.paper} size={size}>
      {/* teal V */}
      <path
        d="M44 52 H78 L100 132 L122 52 H156 L118 172 H82 Z"
        fill={P.teal}
      />
      {/* little roof above apex — forms tiny house */}
      <path d="M86 42 L100 28 L114 42 Z" fill={P.coral}/>
      {/* hour dot in V valley */}
      <circle cx="100" cy="150" r="8" fill={P.coral}/>
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 08 — "HANDS-ROOF"  dos manos formando tejado (apoyo mutuo)
// ──────────────────────────────────────────────────────────
function Mark08({ size = 200 }) {
  return (
    <Tile bg={P.teal} size={size}>
      {/* left hand */}
      <path
        d="M40 140 L40 108 Q40 96 52 92 L86 80 Q94 78 100 86 L100 118 Z"
        fill="#fff"
      />
      {/* right hand */}
      <path
        d="M160 140 L160 108 Q160 96 148 92 L114 80 Q106 78 100 86 L100 118 Z"
        fill="#fff"
      />
      {/* heart cradled */}
      <path
        d="M100 146
           C 82 130, 72 118, 80 106
           C 86 98, 96 100, 100 110
           C 104 100, 114 98, 120 106
           C 128 118, 118 130, 100 146 Z"
        fill={P.coral}
      />
      {/* roofline accent */}
      <path d="M38 88 L100 44 L162 88" stroke={P.coral} strokeWidth="6" fill="none" strokeLinecap="round" strokeLinejoin="round"/>
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 09 — "PICTO-STYLE"  ARASAAC-inspired: vecinos y casa
// ──────────────────────────────────────────────────────────
function Mark09({ size = 200 }) {
  return (
    <Tile bg="#fff9f0" size={size}>
      {/* ground */}
      <rect x="0" y="160" width="200" height="40" fill={P.sand}/>
      {/* house */}
      <rect x="62" y="72" width="76" height="88" fill={P.coral}/>
      <path d="M52 78 L100 40 L148 78 Z" fill={P.tealDark}/>
      <rect x="90" y="110" width="20" height="50" fill={P.tealDark}/>
      <rect x="74" y="92" width="14" height="14" fill="#fff"/>
      <rect x="112" y="92" width="14" height="14" fill="#fff"/>
      {/* neighbor left */}
      <circle cx="34" cy="124" r="10" fill={P.tealDark} stroke="#000" strokeWidth="2"/>
      <path d="M20 160 Q20 138 34 138 Q48 138 48 160 Z" fill={P.teal} stroke="#000" strokeWidth="2"/>
      {/* neighbor right */}
      <circle cx="166" cy="124" r="10" fill={P.tealDark} stroke="#000" strokeWidth="2"/>
      <path d="M152 160 Q152 138 166 138 Q180 138 180 160 Z" fill={P.teal} stroke="#000" strokeWidth="2"/>
      {/* hard outlines on house */}
      <path d="M52 78 L100 40 L148 78 M62 72 V160 H138 V72" fill="none" stroke="#000" strokeWidth="2.5"/>
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 10 — "TIME-ROOF"  reloj estilizado con aguja = tejado
// ──────────────────────────────────────────────────────────
function Mark10({ size = 200 }) {
  return (
    <Tile bg={P.paper} size={size}>
      <circle cx="100" cy="100" r="70" fill={P.teal}/>
      <circle cx="100" cy="100" r="58" fill={P.paper}/>
      {/* roof-shaped hand from center toward 12 */}
      <path d="M100 100 L78 62 L100 48 L122 62 Z" fill={P.coral}/>
      {/* short hand toward 4 */}
      <line x1="100" y1="100" x2="130" y2="118" stroke={P.teal} strokeWidth="7" strokeLinecap="round"/>
      <circle cx="100" cy="100" r="6" fill={P.tealDark}/>
      {/* ticks */}
      {Array.from({length:12}).map((_,i)=>{
        const a = (i/12)*Math.PI*2 - Math.PI/2;
        const x1 = 100 + Math.cos(a)*54, y1 = 100 + Math.sin(a)*54;
        const x2 = 100 + Math.cos(a)*50, y2 = 100 + Math.sin(a)*50;
        return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke={P.teal} strokeWidth="3" strokeLinecap="round"/>;
      })}
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 11 — "BARRIO BLOCK"  tres casas / bloque vecinal
// ──────────────────────────────────────────────────────────
function Mark11({ size = 200 }) {
  return (
    <Tile bg={P.teal} size={size}>
      {/* three roofs */}
      <path d="M30 100 L60 66 L90 100 Z" fill={P.coral}/>
      <path d="M75 100 L100 58 L125 100 Z" fill="#fff"/>
      <path d="M110 100 L140 66 L170 100 Z" fill={P.coral}/>
      {/* house bodies */}
      <rect x="38" y="100" width="44" height="58" fill="#fff"/>
      <rect x="80" y="100" width="40" height="58" fill={P.coral}/>
      <rect x="118" y="100" width="44" height="58" fill="#fff"/>
      {/* doors */}
      <rect x="56" y="128" width="10" height="30" fill={P.teal}/>
      <rect x="95" y="128" width="10" height="30" fill={P.teal}/>
      <rect x="134" y="128" width="10" height="30" fill={P.teal}/>
    </Tile>
  );
}

// ──────────────────────────────────────────────────────────
// 12 — "CIRCLE V"  círculo con V estilizada  (minimalista)
// ──────────────────────────────────────────────────────────
function Mark12({ size = 200 }) {
  return (
    <Tile bg={P.paper} size={size}>
      <circle cx="100" cy="100" r="78" fill={P.teal}/>
      {/* V with soft corners */}
      <path
        d="M56 64 Q56 58 62 58 L78 58 Q84 58 86 64 L100 118 L114 64 Q116 58 122 58 L138 58 Q144 58 144 64 L114 154 Q110 164 100 164 Q90 164 86 154 Z"
        fill="#fff"
      />
      <circle cx="100" cy="150" r="8" fill={P.coral}/>
    </Tile>
  );
}

Object.assign(window, {
  Mark01, Mark02, Mark03, Mark04, Mark05, Mark06,
  Mark07, Mark08, Mark09, Mark10, Mark11, Mark12,
  LOGO_PALETTE: P,
});
