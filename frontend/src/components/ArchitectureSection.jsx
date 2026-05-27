import { useEffect, useState } from 'react';
import { Box, Cpu, Database, FlaskConical, GitBranch, Server, Shield, Wifi } from 'lucide-react';
import { Metric, Panel, Section } from './shared.jsx';

const DIAGRAMS = [
  {
    id: 'architecture',
    title: 'Arquitectura General',
    icon: Box,
    definition: `graph TB
      subgraph Frontend["Frontend (React + Vite)"]
        REACT["React SPA"]
        AUTH["JWT + OAuth2"]
      end
      subgraph Backend["Backend (Spring Boot 3)"]
        REST["REST API"]
        JWT["JWT Security"]
        FLYWAY["Flyway"]
      end
      subgraph DB["PostgreSQL"]
        PG[("Database")]
      end
      subgraph IA["Flask IA"]
        FI[("Predicciones")]
      end
      subgraph IOT["IoT"]
        SIM[("Sensores")]
      end
      REACT -->|HTTP + JWT| REST
      SIM -->|Readings| REST
      REST --> FLYWAY
      FLYWAY --> PG
      REST -->|HTTP| FI`
  },
  {
    id: 'ci-cd',
    title: 'Pipeline CI/CD',
    icon: GitBranch,
    definition: `graph LR
      PUSH[Push] --> J1[Backend JUnit]
      PUSH --> J2[Frontend Vitest]
      PUSH --> J3[Python JSON]
      PUSH --> J4[Python IA]
      J1 --> G[✅ All Green]
      J2 --> G
      J3 --> G
      J4 --> G`
  },
  {
    id: 'auth',
    title: 'Flujo de Autenticacion',
    icon: Shield,
    definition: `sequenceDiagram
      participant U as Usuario
      participant FE as Frontend
      participant BE as Backend
      U->>FE: Login
      FE->>BE: POST /api/auth/login
      BE-->>FE: JWT Token
      FE->>FE: localStorage`
  },
  {
    id: 'docker',
    title: 'Infraestructura Docker',
    icon: Server,
    definition: `graph TB
      FE[Frontend<br/>Nginx :80]
      BE[Backend<br/>Spring :8080]
      PG[PostgreSQL<br/>:5432]
      FL[Flask IA<br/>:5000]
      FE -->|/api/| BE
      BE -->|JDBC| PG
      BE -->|HTTP| FL`
  },
  {
    id: 'iot',
    title: 'Simulacion IoT',
    icon: Wifi,
    definition: `sequenceDiagram
      loop Cada N segundos
        SIM[Simulador]->>BE[Backend]: POST /api/readings
        BE->>BE: Evalua umbrales
        alt Fuera de rango
          BE->>BE: Crea alerta
        end
      end`
  },
  {
    id: 'ia',
    title: 'IA Predictiva',
    icon: FlaskConical,
    definition: `sequenceDiagram
      FE[Frontend]->>BE[Backend]: POST /ia/predict
      BE->>FL[Flask]: /ia/predict
      FL->>FL: Modelo ML
      FL-->>BE: Prediccion
      BE-->>FE: Riesgo + Recomendacion`
  }
];

function MermaidDiagram({ definition, id }) {
  const [svg, setSvg] = useState('');

  useEffect(() => {
    async function renderMermaid() {
      try {
        const resp = await fetch(`https://mermaid.ink/img/${btoa(definition)}?theme=dark&scale=2`);
        if (resp.ok) {
          setSvg(resp.url);
        }
      } catch {}
    }
    renderMermaid();
  }, [definition]);

  if (svg) {
    return (
      <div className="mermaidContainer">
        <img src={svg} alt={`Diagrama ${id}`} className="mermaidImg" loading="lazy" />
      </div>
    );
  }

  return (
    <div className="mermaidContainer">
      <pre className="mermaidFallback">{definition}</pre>
    </div>
  );
}

export function ArchitectureSection({ t, ghStatus }) {
  const [selectedDiagram, setSelectedDiagram] = useState('architecture');

  const current = DIAGRAMS.find((d) => d.id === selectedDiagram) ?? DIAGRAMS[0];
  const Icon = current.icon;

  return (
    <Section title={t.architectureTitle ?? 'Arquitectura del Sistema'} subtitle={t.architectureSubtitle ?? 'Diagramas de componentes, flujos e infraestructura'}>
      <div className="metrics">
        <Metric icon={<Box />} label={t.modules ?? 'Modulos'} value="12" />
        <Metric icon={<Database />} label={t.entities ?? 'Entidades'} value="19" />
        <Metric icon={<Server />} label={t.endpoints ?? 'Endpoints'} value="30+" />
        <Metric icon={<GitBranch />} label={t.cicd ?? 'Jobs CI/CD'} value="4" />
        <Metric icon={<Shield />} label={t.securityLayers ?? 'Capas seguridad'} value="2" />
        <Metric icon={<Cpu />} label={t.technologies ?? 'Tecnologias'} value="15" />
      </div>

      <div className="archNav">
        {DIAGRAMS.map((d) => (
          <button
            key={d.id}
            className={`archBtn ${selectedDiagram === d.id ? 'active' : ''}`}
            type="button"
            onClick={() => setSelectedDiagram(d.id)}
          >
            <d.icon size={18} />
            <span>{d.title}</span>
          </button>
        ))}
      </div>

      <Panel title={`${current.icon === Box ? '🏗' : current.icon === GitBranch ? '🔄' : current.icon === Shield ? '🔒' : current.icon === Server ? '🐳' : current.icon === Wifi ? '📡' : '🤖'} ${current.title}`}>
        <MermaidDiagram definition={current.definition} id={current.id} />
      </Panel>

      <div className="dashboardGrid">
        <Panel title={t.techStack ?? 'Stack Tecnologico'}>
          <div className="techGrid">
            <div className="techItem"><strong>Frontend:</strong> React 18 + Vite 8</div>
            <div className="techItem"><strong>Backend:</strong> Spring Boot 3.3.5 / Java 21</div>
            <div className="techItem"><strong>Database:</strong> PostgreSQL 16 + Flyway</div>
            <div className="techItem"><strong>Auth:</strong> JWT + OAuth2 Google + BCrypt</div>
            <div className="techItem"><strong>Testing:</strong> JUnit 5 + Vitest + Selenium + pytest</div>
            <div className="techItem"><strong>CI/CD:</strong> GitHub Actions (4 jobs)</div>
            <div className="techItem"><strong>Container:</strong> Docker + Docker Compose</div>
            <div className="techItem"><strong>Documentacion:</strong> Swagger/OpenAPI</div>
          </div>
        </Panel>

        <Panel title={t.metrics ?? 'Metricas del Proyecto'}>
          <div className="techGrid">
            <div className="techItem"><strong>Archivos:</strong> 200+ backend, 25+ frontend</div>
            <div className="techItem"><strong>Tests:</strong> 28 (16 JUnit + 4 Vitest + 8 pytest)</div>
            <div className="techItem"><strong>DTOs:</strong> 22 separados Request/Response</div>
            <div className="techItem"><strong>Cobertura:</strong> Con fallback offline IA</div>
            <div className="techItem"><strong>i18n:</strong> 140+ claves ES/EN frontend</div>
            <div className="techItem"><strong>Docker:</strong> 3 servicios + volumen</div>
          </div>
        </Panel>
      </div>
    </Section>
  );
}
