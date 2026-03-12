import { useState } from "react";

const TAB_STYLE = (active) => ({
  padding: "10px 22px",
  border: "none",
  borderBottom: active ? "3px solid #2563EB" : "3px solid transparent",
  background: "transparent",
  color: active ? "#2563EB" : "#6B7280",
  fontFamily: "'IBM Plex Mono', monospace",
  fontSize: 13,
  fontWeight: active ? 700 : 500,
  cursor: "pointer",
  letterSpacing: "0.04em",
  transition: "all 0.2s",
});

/* ─── USE CASE DIAGRAM ─── */
function UseCaseDiagram() {
  const cases = [
    { id: "UC01", text: "Crear perfil de usuario" },
    { id: "UC02", text: "Iniciar / cambiar sesión" },
    { id: "UC03", text: "Publicar servicio" },
    { id: "UC04", text: "Explorar Escaparate" },
    { id: "UC05", text: "Filtrar por categoría/horas" },
    { id: "UC06", text: "Ver detalle de servicio" },
    { id: "UC07", text: "Escuchar servicio (TTS)" },
    { id: "UC08", text: "Aceptar servicio" },
    { id: "UC09", text: "Valorar con pictogramas ARASAAC" },
    { id: "UC10", text: "Ver perfil y nivel" },
    { id: "UC11", text: "Ver historial gráfico" },
    { id: "UC12", text: "Editar / cancelar servicio" },
    { id: "UC13", text: "Recibir notificación local" },
  ];

  const extended = [
    { from: "UC06", to: "UC07", label: "«extend»" },
    { from: "UC08", to: "UC09", label: "«include»" },
    { from: "UC04", to: "UC05", label: "«extend»" },
  ];

  const W = 860, H = 560;
  const actorX = 72, sysX1 = 160, sysX2 = W - 20;
  const rows = [0,1,2,3,4,5,6,7,8,9,10,11,12];
  const rowH = (H - 80) / cases.length;

  return (
    <div style={{ overflowX: "auto" }}>
      <svg width={W} height={H} style={{ fontFamily: "'IBM Plex Mono', monospace", display: "block", margin: "0 auto" }}>
        {/* System boundary */}
        <rect x={sysX1} y={30} width={sysX2 - sysX1} height={H - 50} rx={6}
          fill="#F0F7FF" stroke="#2563EB" strokeWidth={1.5} strokeDasharray="6,3" />
        <text x={sysX1 + 12} y={50} fontSize={11} fill="#2563EB" fontWeight={700}>
          VecindApp — Sistema
        </text>

        {/* Actor */}
        {/* Stick figure */}
        <circle cx={actorX} cy={H/2 - 10} r={14} fill="none" stroke="#1E3A5F" strokeWidth={2} />
        <line x1={actorX} y1={H/2+4} x2={actorX} y2={H/2+40} stroke="#1E3A5F" strokeWidth={2} />
        <line x1={actorX-18} y1={H/2+20} x2={actorX+18} y2={H/2+20} stroke="#1E3A5F" strokeWidth={2} />
        <line x1={actorX} y1={H/2+40} x2={actorX-14} y2={H/2+64} stroke="#1E3A5F" strokeWidth={2} />
        <line x1={actorX} y1={H/2+40} x2={actorX+14} y2={H/2+64} stroke="#1E3A5F" strokeWidth={2} />
        <text x={actorX} y={H/2+80} textAnchor="middle" fontSize={11} fill="#1E3A5F" fontWeight={700}>Usuario</text>
        <text x={actorX} y={H/2+93} textAnchor="middle" fontSize={9} fill="#6B7280">(Vecino)</text>

        {/* Use case ellipses */}
        {cases.map((uc, i) => {
          const cy = 65 + i * rowH + rowH / 2;
          const cx = sysX1 + 310;
          return (
            <g key={uc.id}>
              <ellipse cx={cx} cy={cy} rx={175} ry={16} fill="white" stroke="#3B82F6" strokeWidth={1.5} />
              <text x={cx} y={cy + 4} textAnchor="middle" fontSize={10} fill="#1E3A5F" fontWeight={600}>
                {uc.id} — {uc.text}
              </text>
              {/* Line from actor */}
              <line x1={actorX + 20} y1={H/2 - 10} x2={cx - 175} y2={cy} stroke="#94A3B8" strokeWidth={1} strokeDasharray="3,2" />
            </g>
          );
        })}

        {/* Extend/include arcs */}
        {/* UC06→UC07 extend */}
        <path d={`M ${sysX1+310+175} ${65+5*rowH+rowH/2} C ${sysX1+560} ${65+5*rowH+rowH/2-10} ${sysX1+560} ${65+6*rowH+rowH/2+10} ${sysX1+310+175} ${65+6*rowH+rowH/2}`}
          fill="none" stroke="#F59E0B" strokeWidth={1.5} strokeDasharray="4,2"
          markerEnd="url(#arrow-extend)" />
        <text x={sysX1 + 540} y={65 + 5.5 * rowH + rowH / 2} fontSize={8} fill="#F59E0B" fontWeight={700}>«extend»</text>

        {/* UC08→UC09 include */}
        <path d={`M ${sysX1+310+175} ${65+7*rowH+rowH/2} C ${sysX1+570} ${65+7*rowH+rowH/2-10} ${sysX1+570} ${65+8*rowH+rowH/2+10} ${sysX1+310+175} ${65+8*rowH+rowH/2}`}
          fill="none" stroke="#10B981" strokeWidth={1.5} strokeDasharray="4,2"
          markerEnd="url(#arrow-include)" />
        <text x={sysX1 + 548} y={65 + 7.5 * rowH + rowH / 2} fontSize={8} fill="#10B981" fontWeight={700}>«include»</text>

        {/* UC04→UC05 extend */}
        <path d={`M ${sysX1+310+175} ${65+3*rowH+rowH/2} C ${sysX1+565} ${65+3*rowH+rowH/2-10} ${sysX1+565} ${65+4*rowH+rowH/2+10} ${sysX1+310+175} ${65+4*rowH+rowH/2}`}
          fill="none" stroke="#F59E0B" strokeWidth={1.5} strokeDasharray="4,2" />
        <text x={sysX1 + 543} y={65 + 3.5 * rowH + rowH / 2} fontSize={8} fill="#F59E0B" fontWeight={700}>«extend»</text>

        {/* Legend */}
        <rect x={W - 175} y={H - 95} width={165} height={80} rx={4} fill="white" stroke="#E5E7EB" strokeWidth={1} />
        <text x={W - 165} y={H - 78} fontSize={9} fill="#374151" fontWeight={700}>LEYE  NDA</text>
        <line x1={W-165} y1={H-66} x2={W-140} y2={H-66} stroke="#F59E0B" strokeWidth={1.5} strokeDasharray="4,2" />
        <text x={W-135} y={H-62} fontSize={8} fill="#374151">«extend» (opcional)</text>
        <line x1={W-165} y1={H-50} x2={W-140} y2={H-50} stroke="#10B981" strokeWidth={1.5} strokeDasharray="4,2" />
        <text x={W-135} y={H-46} fontSize={8} fill="#374151">«include» (obligatorio)</text>
        <line x1={W-165} y1={H-34} x2={W-140} y2={H-34} stroke="#94A3B8" strokeWidth={1} strokeDasharray="3,2" />
        <text x={W-135} y={H-30} fontSize={8} fill="#374151">asociación actor→UC</text>

        <defs>
          <marker id="arrow-extend" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
            <path d="M0,0 L0,6 L8,3 z" fill="#F59E0B" />
          </marker>
          <marker id="arrow-include" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
            <path d="M0,0 L0,6 L8,3 z" fill="#10B981" />
          </marker>
        </defs>
      </svg>
    </div>
  );
}

/* ─── ER DIAGRAM ─── */
function ERDiagram() {
  const entities = [
    {
      name: "USUARIO", x: 60, y: 50, w: 210, color: "#1E3A5F", light: "#EFF6FF",
      pk: "id_usuario",
      fields: ["nombre", "barrio", "avatar_path", "saldo_horas", "intercambios_total", "nivel", "fecha_registro"],
    },
    {
      name: "SERVICIO", x: 340, y: 50, w: 230, color: "#1D4ED8", light: "#DBEAFE",
      pk: "id_servicio",
      fields: ["id_usuario_fk", "titulo", "descripcion", "categoria", "pictograma_id", "coste_horas", "estado", "fecha_publicacion", "fecha_caducidad"],
    },
    {
      name: "TRANSACCION", x: 60, y: 380, w: 230, color: "#0F766E", light: "#F0FDFA",
      pk: "id_transaccion",
      fields: ["id_comprador_fk", "id_vendedor_fk", "id_servicio_fk", "horas_transferidas", "estado", "timestamp"],
    },
    {
      name: "VALORACION", x: 340, y: 380, w: 230, color: "#7C3AED", light: "#F5F3FF",
      pk: "id_valoracion",
      fields: ["id_transaccion_fk", "id_valorador_fk", "id_valorado_fk", "pictogramas_json", "comentario", "timestamp"],
    },
  ];

  const rowH = 18, headerH = 32, pkH = 22;

  const entityHeight = (e) => headerH + pkH + e.fields.length * rowH + 10;

  const relations = [
    { label: "publica\n1..N", x1: 270, y1: 130, x2: 340, y2: 130, card1: "1", card2: "N" },
    { label: "compra/vende\n1..N", x1: 165, y1: 50+entityHeight(entities[0]), x2: 165, y2: 380, card1: "1", card2: "N", vert: true },
    { label: "objeto de\n1..N", x1: 340+115, y1: 380+40, x2: 340+115, y2: 50+entityHeight(entities[1]), card1: "N", card2: "1", vert: true },
    { label: "genera\n1..N", x1: 290, y1: 430, x2: 340, y2: 430, card1: "1", card2: "N" },
    { label: "recibe\n1..N", x1: 270, y1: 180, x2: 340, y2: 450, card1: "1", card2: "N", curve: true },
  ];

  return (
    <div style={{ overflowX: "auto" }}>
      <svg width={640} height={700} style={{ fontFamily: "'IBM Plex Mono', monospace", display: "block", margin: "0 auto" }}>
        {/* Relations */}
        {/* USUARIO → SERVICIO */}
        <line x1={270} y1={115} x2={340} y2={115} stroke="#64748B" strokeWidth={1.5} />
        <text x={294} y={108} fontSize={8} fill="#64748B" textAnchor="middle">publica</text>
        <text x={275} y={126} fontSize={9} fill="#1D4ED8" fontWeight={700}>1</text>
        <text x={330} y={126} fontSize={9} fill="#1D4ED8" fontWeight={700}>N</text>

        {/* USUARIO → TRANSACCION */}
        <line x1={165} y1={50+entityHeight(entities[0])} x2={165} y2={380} stroke="#64748B" strokeWidth={1.5} />
        <text x={130} y={320} fontSize={8} fill="#64748B">compra/vende</text>
        <text x={148} y={50+entityHeight(entities[0])+10} fontSize={9} fill="#0F766E" fontWeight={700}>1</text>
        <text x={148} y={375} fontSize={9} fill="#0F766E" fontWeight={700}>N</text>

        {/* SERVICIO → TRANSACCION */}
        <line x1={455} y1={50+entityHeight(entities[1])} x2={455} y2={380} stroke="#64748B" strokeWidth={1.5} />
        <text x={462} y={320} fontSize={8} fill="#64748B">en transacción</text>
        <text x={440} y={50+entityHeight(entities[1])+10} fontSize={9} fill="#0F766E" fontWeight={700}>1</text>
        <text x={440} y={375} fontSize={9} fill="#0F766E" fontWeight={700}>N</text>

        {/* TRANSACCION → VALORACION */}
        <line x1={290} y1={430} x2={340} y2={430} stroke="#64748B" strokeWidth={1.5} />
        <text x={304} y={424} fontSize={8} fill="#64748B">genera</text>
        <text x={273} y={440} fontSize={9} fill="#7C3AED" fontWeight={700}>1</text>
        <text x={330} y={440} fontSize={9} fill="#7C3AED" fontWeight={700}>N</text>

        {/* USUARIO → VALORACION (recibe) */}
        <path d="M 270 220 Q 320 260 340 470" fill="none" stroke="#64748B" strokeWidth={1.5} strokeDasharray="4,3" />
        <text x={296} y={358} fontSize={8} fill="#64748B">recibe</text>
        <text x={265} y={232} fontSize={9} fill="#7C3AED" fontWeight={700}>1</text>
        <text x={330} y={482} fontSize={9} fill="#7C3AED" fontWeight={700}>N</text>

        {/* Entities */}
        {entities.map((e) => {
          const h = entityHeight(e);
          return (
            <g key={e.name}>
              {/* Shadow */}
              <rect x={e.x+3} y={e.y+3} width={e.w} height={h} rx={6} fill="#00000015" />
              {/* Body */}
              <rect x={e.x} y={e.y} width={e.w} height={h} rx={6} fill={e.light} stroke={e.color} strokeWidth={2} />
              {/* Header */}
              <rect x={e.x} y={e.y} width={e.w} height={headerH} rx={6} fill={e.color} />
              <rect x={e.x} y={e.y + headerH - 6} width={e.w} height={6} fill={e.color} />
              <text x={e.x + e.w / 2} y={e.y + 21} textAnchor="middle" fontSize={12} fill="white" fontWeight={700} letterSpacing="0.05em">
                {e.name}
              </text>
              {/* PK */}
              <rect x={e.x} y={e.y + headerH} width={e.w} height={pkH} fill={e.color + "22"} />
              <text x={e.x + 10} y={e.y + headerH + 15} fontSize={10} fill={e.color} fontWeight={700}>
                🔑 {e.pk}
              </text>
              <line x1={e.x} y1={e.y + headerH + pkH} x2={e.x + e.w} y2={e.y + headerH + pkH} stroke={e.color} strokeWidth={0.5} />
              {/* Fields */}
              {e.fields.map((f, i) => (
                <g key={f}>
                  <text
                    x={e.x + 10}
                    y={e.y + headerH + pkH + 14 + i * rowH}
                    fontSize={9.5}
                    fill={f.includes("_fk") ? "#7C3AED" : "#374151"}
                    fontWeight={f.includes("_fk") ? 700 : 400}
                  >
                    {f.includes("_fk") ? "⤷ " : "  "}{f}
                  </text>
                </g>
              ))}
            </g>
          );
        })}

        {/* Legend */}
        <rect x={10} y={640} width={620} height={48} rx={4} fill="#F8FAFC" stroke="#E2E8F0" strokeWidth={1} />
        <text x={22} y={657} fontSize={9} fill="#374151" fontWeight={700}>LEYENDA: </text>
        <text x={22} y={672} fontSize={9} fill="#1E3A5F" fontWeight={700}>🔑 PK</text>
        <text x={75} y={672} fontSize={9} fill="#374151">Clave primaria · </text>
        <text x={75} y={657} fontSize={9} fill="#7C3AED" fontWeight={700}>⤷ FK</text>
        <text x={115} y={657} fontSize={9} fill="#374151">Clave foránea · </text>
        <text x={205} y={657} fontSize={9} fill="#374151">— Relación 1:N ·</text>
        <text x={290} y={657} fontSize={9} fill="#374151">- - Relación derivada</text>
        <text x={205} y={672} fontSize={9} fill="#374151">Cardinalidades marcadas en cada extremo de la relación</text>
      </svg>
    </div>
  );
}

/* ─── CLASS DIAGRAM ─── */
function ClassDiagram() {
  const classes = [
    {
      name: "Usuario", layer: "Entity", x: 20, y: 20, w: 200,
      color: "#1E3A5F", light: "#EFF6FF",
      attrs: ["- idUsuario: Int", "- nombre: String", "- barrio: String", "- avatarPath: String?", "- saldoHoras: Double", "- intercambiosTotal: Int", "- nivel: NivelVecino", "- fechaRegistro: Long"],
      methods: ["+ calcularNivel(): NivelVecino"],
    },
    {
      name: "Servicio", layer: "Entity", x: 250, y: 20, w: 215,
      color: "#1D4ED8", light: "#DBEAFE",
      attrs: ["- idServicio: Int", "- idUsuarioFk: Int", "- titulo: String", "- descripcion: String?", "- categoria: CategoriaServicio", "- pictogramaId: String", "- costeHoras: Double", "- estado: EstadoServicio", "- fechaPublicacion: Long", "- fechaCaducidad: Long?"],
      methods: ["+ estaActivo(): Boolean", "+ estaVencido(): Boolean"],
    },
    {
      name: "Transaccion", layer: "Entity", x: 20, y: 360, w: 220,
      color: "#0F766E", light: "#F0FDFA",
      attrs: ["- idTransaccion: Int", "- idCompradorFk: Int", "- idVendedorFk: Int", "- idServicioFk: Int", "- horasTransferidas: Double", "- estado: EstadoTransaccion", "- timestamp: Long"],
      methods: ["+ estaCompletada(): Boolean"],
    },
    {
      name: "Valoracion", layer: "Entity", x: 270, y: 360, w: 215,
      color: "#7C3AED", light: "#F5F3FF",
      attrs: ["- idValoracion: Int", "- idTransaccionFk: Int", "- idValoradorFk: Int", "- idValoradoFk: Int", "- pictogramasJson: String", "- comentario: String?", "- timestamp: Long"],
      methods: ["+ getPictogramasList(): List<String>"],
    },
    {
      name: "UsuarioRepository", layer: "Repository", x: 510, y: 20, w: 230,
      color: "#374151", light: "#F9FAFB",
      attrs: [],
      methods: ["+ getAll(): Flow<List<Usuario>>", "+ getById(id: Int): Usuario?", "+ insert(u: Usuario): Long", "+ update(u: Usuario)", "+ updateSaldo(id: Int, saldo: Double)"],
    },
    {
      name: "ServicioRepository", layer: "Repository", x: 510, y: 220, w: 230,
      color: "#374151", light: "#F9FAFB",
      attrs: [],
      methods: ["+ getActivos(): Flow<List<Servicio>>", "+ getByCategoria(cat): Flow<..>", "+ insert(s: Servicio): Long", "+ update(s: Servicio)", "+ cambiarEstado(id, estado)"],
    },
    {
      name: "TransaccionUseCase", layer: "UseCase", x: 510, y: 420, w: 240,
      color: "#92400E", light: "#FFFBEB",
      attrs: [],
      methods: ["+ ejecutar(compradorId, vendedorId,", "  servicioId): Result<Unit>", "  // valida saldo, debita, acredita", "  // @Transaction atómico"],
    },
    {
      name: "EscaparateViewModel", layer: "ViewModel", x: 20, y: 590, w: 215,
      color: "#BE185D", light: "#FFF1F2",
      attrs: ["- _servicios: MutableStateFlow", "- _filtroCategoria: StateFlow"],
      methods: ["+ cargarServicios()", "+ filtrar(categoria)", "+ aceptarServicio(sId, uId)"],
    },
    {
      name: "PerfilViewModel", layer: "ViewModel", x: 260, y: 590, w: 200,
      color: "#BE185D", light: "#FFF1F2",
      attrs: ["- _usuario: MutableStateFlow", "- _nivel: StateFlow"],
      methods: ["+ cargarPerfil(userId)", "+ actualizarNivel()"],
    },
    {
      name: "CaducidadWorker", layer: "Worker", x: 510, y: 620, w: 225,
      color: "#065F46", light: "#ECFDF5",
      attrs: ["- context: Context", "- params: WorkerParameters"],
      methods: ["+ doWork(): Result", "  // revisa caducidades", "  // lanza notificación local"],
    },
  ];

  const rowH = 16;
  const hdrH = 28;
  const secH = 14;
  const entityHeight = (c) =>
    hdrH + secH + c.attrs.length * rowH + secH + c.methods.length * rowH + 10;

  const layerColors = {
    Entity: "#1E3A5F",
    Repository: "#374151",
    UseCase: "#92400E",
    ViewModel: "#BE185D",
    Worker: "#065F46",
  };

  return (
    <div style={{ overflowX: "auto" }}>
      <svg width={760} height={820} style={{ fontFamily: "'IBM Plex Mono', monospace", display: "block", margin: "0 auto" }}>

        {/* Layer labels */}
        {[
          { label: "«entity»", x: 20, y: 10, color: "#1E3A5F" },
          { label: "«repository»", x: 510, y: 10, color: "#374151" },
          { label: "«use case»", x: 510, y: 410, color: "#92400E" },
          { label: "«view model»", x: 20, y: 580, color: "#BE185D" },
          { label: "«worker»", x: 510, y: 610, color: "#065F46" },
        ].map((l) => (
          <text key={l.label} x={l.x} y={l.y} fontSize={8} fill={l.color} fontWeight={700} fontStyle="italic">
            {l.label}
          </text>
        ))}

        {/* Relations */}
        {/* Entity → Repository */}
        <line x1={220} y1={80} x2={510} y2={80} stroke="#9CA3AF" strokeWidth={1} strokeDasharray="4,3" />
        <text x={340} y={74} fontSize={7} fill="#9CA3AF" textAnchor="middle">uses</text>
        <line x1={465} y1={160} x2={510} y2={280} stroke="#9CA3AF" strokeWidth={1} strokeDasharray="4,3" />
        <text x={490} y={225} fontSize={7} fill="#9CA3AF">uses</text>

        {/* UseCase → Repositories */}
        <line x1={625} y1={420} x2={625} y2={380} stroke="#92400E" strokeWidth={1.5} strokeDasharray="4,2" />
        <text x={632} y={405} fontSize={7} fill="#92400E">calls</text>

        {/* ViewModel → UseCase */}
        <line x1={127} y1={590} x2={580} y2={590} stroke="#BE185D" strokeWidth={1} strokeDasharray="4,2" />
        <line x1={580} y1={590} x2={580} y2={505} stroke="#BE185D" strokeWidth={1} strokeDasharray="4,2" />
        <text x={350} y={585} fontSize={7} fill="#BE185D" textAnchor="middle">calls</text>

        {/* Entity relations (composition) */}
        <line x1={165} y1={360} x2={165} y2={220} stroke="#64748B" strokeWidth={1.5} />
        <polygon points="165,220 158,234 172,234" fill="#1E3A5F" />
        <text x={172} y={296} fontSize={7} fill="#64748B">1..N</text>

        <line x1={357} y1={360} x2={357} y2={220} stroke="#64748B" strokeWidth={1.5} />
        <polygon points="357,220 350,234 364,234" fill="#1E3A5F" />
        <text x={364} y={296} fontSize={7} fill="#64748B">1..N</text>

        {/* Classes */}
        {classes.map((c) => {
          const h = entityHeight(c);
          return (
            <g key={c.name}>
              <rect x={c.x + 2} y={c.y + 2} width={c.w} height={h} rx={4} fill="#00000010" />
              <rect x={c.x} y={c.y} width={c.w} height={h} rx={4} fill={c.light} stroke={c.color} strokeWidth={1.5} />

              {/* Header */}
              <rect x={c.x} y={c.y} width={c.w} height={hdrH} rx={4} fill={c.color} />
              <rect x={c.x} y={c.y + hdrH - 4} width={c.w} height={4} fill={c.color} />
              <text x={c.x + c.w / 2} y={c.y + 18} textAnchor="middle" fontSize={11} fill="white" fontWeight={700}>
                {c.name}
              </text>

              {/* Attributes section */}
              <rect x={c.x} y={c.y + hdrH} width={c.w} height={secH} fill={c.color + "18"} />
              <text x={c.x + 6} y={c.y + hdrH + 10} fontSize={7.5} fill={c.color} fontWeight={600} fontStyle="italic">
                — atributos —
              </text>
              {c.attrs.map((a, i) => (
                <text key={i} x={c.x + 6} y={c.y + hdrH + secH + 11 + i * rowH} fontSize={8} fill="#374151">
                  {a}
                </text>
              ))}

              {/* Methods section */}
              <line x1={c.x} y1={c.y + hdrH + secH + c.attrs.length * rowH + 2}
                x2={c.x + c.w} y2={c.y + hdrH + secH + c.attrs.length * rowH + 2}
                stroke={c.color} strokeWidth={0.5} />
              <rect x={c.x} y={c.y + hdrH + secH + c.attrs.length * rowH + 2}
                width={c.w} height={secH} fill={c.color + "18"} />
              <text x={c.x + 6} y={c.y + hdrH + secH + c.attrs.length * rowH + 12}
                fontSize={7.5} fill={c.color} fontWeight={600} fontStyle="italic">
                — métodos —
              </text>
              {c.methods.map((m, i) => (
                <text key={i} x={c.x + 6}
                  y={c.y + hdrH + secH + c.attrs.length * rowH + secH + 13 + i * rowH}
                  fontSize={8} fill="#0F172A">
                  {m}
                </text>
              ))}
            </g>
          );
        })}

        {/* Legend */}
        <rect x={10} y={790} width={740} height={24} rx={4} fill="#F8FAFC" stroke="#E2E8F0" />
        {Object.entries(layerColors).map(([layer, color], i) => (
          <g key={layer}>
            <rect x={20 + i * 148} y={799} width={10} height={10} fill={color} rx={2} />
            <text x={34 + i * 148} y={808} fontSize={8} fill={color} fontWeight={700}>«{layer}»</text>
          </g>
        ))}
      </svg>
    </div>
  );
}

/* ─── SPECIFICATIONS TABLE ─── */
function SpecTable() {
  const specs = [
    { id: "UC01", name: "Crear perfil de usuario", actor: "Usuario", pre: "No existe perfil aún", flujo: "Introduce nombre, barrio y elige avatar → Sistema guarda en Room con saldo 5.0h", alt: "Nombre vacío → Error de validación", post: "Perfil creado, sesión iniciada" },
    { id: "UC02", name: "Iniciar / cambiar sesión", actor: "Usuario", pre: "Existen perfiles en BD local", flujo: "Selecciona perfil de la lista → Sistema carga su sesión en SharedPreferences", alt: "Sin perfiles → Redirige a UC01", post: "Usuario en sesión activo" },
    { id: "UC03", name: "Publicar servicio", actor: "Usuario", pre: "Usuario en sesión", flujo: "Rellena título, categoría ARASAAC, coste y caducidad → Sistema guarda con estado ACTIVO", alt: "Coste fuera de rango → Error validación", post: "Servicio visible en Escaparate" },
    { id: "UC08", name: "Aceptar servicio", actor: "Usuario", pre: "Saldo ≥ coste del servicio", flujo: "Pulsa Aceptar → Confirma → Sistema ejecuta @Transaction: débito comprador, crédito vendedor", alt: "Saldo insuficiente → Snackbar error", post: "Saldo actualizado, servicio RESERVADO, se lanza UC09" },
    { id: "UC09", name: "Valorar con ARASAAC", actor: "Usuario", pre: "Transacción completada", flujo: "Selecciona 1-5 pictogramas ARASAAC → Opcionalmente escribe comentario → Guarda valoración", alt: "Sin selección → Permite saltar", post: "Valoración guardada, pictogramas visibles en perfil" },
    { id: "UC13", name: "Recibir notificación", actor: "Sistema (Worker)", pre: "WorkManager registrado", flujo: "CaducidadWorker revisa cada 24h → Si fecha_caducidad < 24h → Lanza NotificationManager", alt: "Sin servicios próximos → No notifica", post: "Notificación local visible en bandeja" },
  ];

  return (
    <div style={{ overflowX: "auto" }}>
      <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 11, fontFamily: "'IBM Plex Mono', monospace" }}>
        <thead>
          <tr style={{ background: "#1E3A5F", color: "white" }}>
            {["ID", "Nombre", "Actor", "Precondición", "Flujo Principal", "Alternativa", "Postcondición"].map(h => (
              <th key={h} style={{ padding: "8px 10px", textAlign: "left", fontSize: 10, fontWeight: 700, letterSpacing: "0.04em" }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {specs.map((s, i) => (
            <tr key={s.id} style={{ background: i % 2 === 0 ? "#F8FAFC" : "white", borderBottom: "1px solid #E2E8F0" }}>
              <td style={{ padding: "7px 10px", color: "#2563EB", fontWeight: 700, whiteSpace: "nowrap" }}>{s.id}</td>
              <td style={{ padding: "7px 10px", fontWeight: 600, color: "#1E3A5F" }}>{s.name}</td>
              <td style={{ padding: "7px 10px", color: "#374151" }}>{s.actor}</td>
              <td style={{ padding: "7px 10px", color: "#374151", fontSize: 10 }}>{s.pre}</td>
              <td style={{ padding: "7px 10px", color: "#374151", fontSize: 10, maxWidth: 200 }}>{s.flujo}</td>
              <td style={{ padding: "7px 10px", color: "#DC2626", fontSize: 10 }}>{s.alt}</td>
              <td style={{ padding: "7px 10px", color: "#059669", fontSize: 10 }}>{s.post}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

/* ─── MAIN APP ─── */
export default function VecindAppDiagramas() {
  const [tab, setTab] = useState(0);
  const tabs = ["Casos de Uso", "Especificaciones UC", "Entidad-Relación", "Diagrama de Clases"];

  return (
    <div style={{
      minHeight: "100vh",
      background: "#F0F4F8",
      fontFamily: "'IBM Plex Mono', monospace",
      padding: "0 0 40px 0",
    }}>
      {/* Header */}
      <div style={{
        background: "#1E3A5F",
        padding: "20px 32px 0 32px",
        boxShadow: "0 4px 24px #00000022",
      }}>
        <div style={{ display: "flex", alignItems: "baseline", gap: 16 }}>
          <span style={{ color: "white", fontSize: 22, fontWeight: 800, letterSpacing: "-0.02em" }}>VecindApp</span>
          <span style={{ color: "#93C5FD", fontSize: 12 }}>Banco de Tiempo Vecinal · TFG DAM · ILERNA 2S2526</span>
        </div>
        <p style={{ color: "#BFDBFE", fontSize: 10, margin: "4px 0 12px 0" }}>
          Diagramas UML para entrega opcional 13–16 marzo 2026
        </p>
        <div style={{ display: "flex", gap: 0 }}>
          {tabs.map((t, i) => (
            <button key={t} onClick={() => setTab(i)} style={TAB_STYLE(tab === i)}>
              {["①","②","③","④"][i]} {t}
            </button>
          ))}
        </div>
      </div>

      {/* Content */}
      <div style={{ background: "white", margin: "24px 20px", borderRadius: 8, padding: "24px", boxShadow: "0 2px 12px #00000010" }}>
        {tab === 0 && (
          <>
            <h2 style={{ color: "#1E3A5F", fontSize: 15, marginBottom: 6, marginTop: 0 }}>
              Diagrama de Casos de Uso — VecindApp
            </h2>
            <p style={{ color: "#6B7280", fontSize: 10, marginBottom: 16 }}>
              Actor único: <strong>Usuario (Vecino)</strong> · 13 casos de uso · Relaciones «extend» e «include» marcadas
            </p>
            <UseCaseDiagram />
          </>
        )}
        {tab === 1 && (
          <>
            <h2 style={{ color: "#1E3A5F", fontSize: 15, marginBottom: 6, marginTop: 0 }}>
              Especificaciones de Casos de Uso
            </h2>
            <p style={{ color: "#6B7280", fontSize: 10, marginBottom: 16 }}>
              6 casos de uso clave con flujo principal, alternativo, pre y postcondiciones
            </p>
            <SpecTable />
          </>
        )}
        {tab === 2 && (
          <>
            <h2 style={{ color: "#1E3A5F", fontSize: 15, marginBottom: 6, marginTop: 0 }}>
              Diagrama Entidad-Relación — VecindApp
            </h2>
            <p style={{ color: "#6B7280", fontSize: 10, marginBottom: 16 }}>
              4 entidades · Cumple requisito mínimo normativa ILERNA (≥ 3 tablas) · Todas las FK marcadas en morado
            </p>
            <ERDiagram />
          </>
        )}
        {tab === 3 && (
          <>
            <h2 style={{ color: "#1E3A5F", fontSize: 15, marginBottom: 6, marginTop: 0 }}>
              Diagrama de Clases — VecindApp (MVVM + Clean Architecture)
            </h2>
            <p style={{ color: "#6B7280", fontSize: 10, marginBottom: 16 }}>
              10 clases organizadas en 5 capas · Entidades Room · Repositorios · UseCase · ViewModels · Worker
            </p>
            <ClassDiagram />
          </>
        )}
      </div>

      {/* Footer notice */}
      <div style={{ margin: "0 20px", padding: "12px 20px", background: "#FEF3C7", borderRadius: 6, border: "1px solid #F59E0B", fontSize: 10, color: "#92400E" }}>
        ⚠️ <strong>Recuerda:</strong> En la memoria, los diagramas deben exportarse como imagen (PNG/SVG) e incluirse con pie de foto y fuente. No se puede incluir código. Extensión objetivo del apartado Análisis: <strong>8–16 páginas</strong>.
      </div>
    </div>
  );
}
