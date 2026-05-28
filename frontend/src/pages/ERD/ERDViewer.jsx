import { useMemo, useCallback, useState, useEffect, useRef } from 'react';
import {
  ReactFlow,
  ReactFlowProvider,
  MiniMap,
  Controls,
  Background,
  BackgroundVariant,
  useNodesState,
  useEdgesState,
  MarkerType,
  useReactFlow
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { ZoomIn, ZoomOut, Maximize2, RotateCcw, Focus } from 'lucide-react';
import EntityNode from './EntityNode.jsx';
import { ERDSidebar } from './ERDSidebar.jsx';
import { generateNodes, generateEdges, loadModel } from '../../config/modelParser.js';
import './erdStyles.css';

const nodeTypes = { entityNode: EntityNode };
const STORAGE_KEY = 'greenhouse-erd-positions';

function loadPositions() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved ? JSON.parse(saved) : null;
  } catch { return null; }
}

function savePositions(nodes) {
  try {
    const positions = {};
    for (const node of nodes) {
      positions[node.id] = { x: node.position.x, y: node.position.y };
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(positions));
  } catch { /* ignore */ }
}

function applySavedPositions(nodes) {
  const saved = loadPositions();
  if (!saved) return nodes;
  return nodes.map(node => ({
    ...node,
    position: saved[node.id] || node.position
  }));
}

function FlowCanvas({ nodes: rawNodes, edges: initialEdges, t }) {
  const positionedNodes = useMemo(() => applySavedPositions(rawNodes), [rawNodes]);
  const [nodes, setNodes, onNodesChange] = useNodesState(positionedNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [selectedEntity, setSelectedEntity] = useState(null);
  const [highlightedEntity, setHighlightedEntity] = useState(null);
  const reactFlowInstance = useReactFlow();
  const saveTimer = useRef(null);

  useEffect(() => {
    const handler = (event) => {
      if (event.key === 'Escape') {
        setHighlightedEntity(null);
        setNodes(nds => nds.map(n => ({ ...n, selected: false })));
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [setNodes]);

  useEffect(() => {
    if (saveTimer.current) clearTimeout(saveTimer.current);
    saveTimer.current = setTimeout(() => savePositions(nodes), 1000);
    return () => { if (saveTimer.current) clearTimeout(saveTimer.current); };
  }, [nodes]);

  const defaultEdgeOptions = useMemo(() => ({
    type: 'smoothstep',
    style: { strokeWidth: 2 },
    markerEnd: { type: MarkerType.ArrowClosed, width: 16, height: 16 }
  }), []);

  const onNodeClick = useCallback((_, node) => {
    setSelectedEntity(node.data.entity.name);
    setHighlightedEntity(node.data.entity.name);
    setNodes(nds => nds.map(n => ({
      ...n,
      selected: n.id === node.id
    })));
  }, [setNodes]);

  const onPaneClick = useCallback(() => {
    setHighlightedEntity(null);
  }, []);

  const fitView = useCallback(() => {
    reactFlowInstance.fitView({ padding: 0.2, duration: 300 });
  }, [reactFlowInstance]);

  const resetLayout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    window.location.reload();
  }, []);

  const focusEntity = useCallback((name) => {
    const node = nodes.find(n => n.id === name);
    if (node) {
      reactFlowInstance.setCenter(node.position.x + 130, node.position.y + 80, { zoom: 1.2, duration: 400 });
      setHighlightedEntity(name);
      setNodes(nds => nds.map(n => ({
        ...n,
        selected: n.id === name
      })));
    }
  }, [nodes, reactFlowInstance, setNodes]);

  const highlightedNodes = useMemo(() => {
    if (!highlightedEntity) return nodes;
    const connectedIds = new Set();
    connectedIds.add(highlightedEntity);
    for (const edge of edges) {
      if (edge.source === highlightedEntity) connectedIds.add(edge.target);
      if (edge.target === highlightedEntity) connectedIds.add(edge.source);
    }
    return nodes.map(node => ({
      ...node,
      data: { ...node.data, dimmed: !connectedIds.has(node.id) }
    }));
  }, [nodes, edges, highlightedEntity]);

  const highlightedEdges = useMemo(() => {
    if (!highlightedEntity) return edges;
    return edges.map(edge => ({
      ...edge,
      style: {
        ...edge.style,
        opacity: edge.source === highlightedEntity || edge.target === highlightedEntity ? 1 : 0.08,
        strokeWidth: edge.source === highlightedEntity || edge.target === highlightedEntity ? 3 : 1
      }
    }));
  }, [edges, highlightedEntity]);

  return (
    <div style={{ position: 'relative', flex: 1, height: '100%' }}>
      <ReactFlow
        nodes={highlightedNodes}
        edges={highlightedEdges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={onNodeClick}
        onPaneClick={onPaneClick}
        nodeTypes={nodeTypes}
        defaultEdgeOptions={defaultEdgeOptions}
        fitView
        attributionPosition="bottom-left"
        minZoom={0.15}
        maxZoom={2.5}
        className="erdCanvas"
        deleteKeyCode={null}
        snapToGrid
        snapGrid={[20, 20]}
      >
        <Background
          variant={BackgroundVariant.Dots}
          gap={24}
          size={1}
          color="rgba(255,255,255,0.06)"
        />
        <MiniMap
          nodeStrokeColor={(n) => n.selected ? '#00cc7a' : '#24282d'}
          nodeColor={(n) => n.selected ? 'rgba(0,204,122,0.3)' : '#0f1215'}
          nodeBorderRadius={6}
          maskColor="rgba(0,0,0,0.65)"
          style={{
            background: '#0b0d0f',
            border: '1px solid #24282d',
            borderRadius: 8,
            boxShadow: '0 4px 20px rgba(0,0,0,0.3)'
          }}
          pannable
          zoomable
        />
        <Controls
          showInteractive={false}
          style={{
            background: '#0f1215',
            border: '1px solid #24282d',
            borderRadius: 8,
            padding: 4,
            boxShadow: '0 4px 20px rgba(0,0,0,0.3)'
          }}
        />
      </ReactFlow>
      <div className="erdToolbar">
        <button className="erdToolbarBtn" onClick={() => reactFlowInstance.zoomIn({ duration: 200 })} title={t?.zoomIn || 'Zoom in'}>
          <ZoomIn size={16} />
        </button>
        <button className="erdToolbarBtn" onClick={() => reactFlowInstance.zoomOut({ duration: 200 })} title={t?.zoomOut || 'Zoom out'}>
          <ZoomOut size={16} />
        </button>
        <button className="erdToolbarBtn" onClick={fitView} title={t?.fitAll || 'Fit all'}>
          <Maximize2 size={16} />
        </button>
        <button className="erdToolbarBtn" onClick={resetLayout} title={t?.resetLayout || 'Reset layout'}>
          <RotateCcw size={16} />
        </button>
        {highlightedEntity && (
          <button className="erdToolbarBtn" onClick={() => focusEntity(highlightedEntity)} title={t?.focusEntity || 'Focus entity'} style={{ borderColor: 'var(--accent)', color: 'var(--accent)' }}>
            <Focus size={16} />
          </button>
        )}
      </div>
    </div>
  );
}

export function ERDViewer({ t }) {
  const [model, setModel] = useState(null);
  const [sidebarEntity, setSidebarEntity] = useState(null);

  useEffect(() => {
    loadModel().then(setModel);
  }, []);

  const nodes = useMemo(() => model ? generateNodes() : [], [model]);
  const edges = useMemo(() => model ? generateEdges() : [], [model]);

  const handleSelectEntity = useCallback((name) => {
    setSidebarEntity(name);
  }, []);

  if (!model) return <div style={{ padding: 40, textAlign: 'center', color: 'var(--muted)' }}>{t?.erdLoading || 'Loading ERD...'}</div>;

  return (
    <div className="erdLayout">
      <ReactFlowProvider>
        <ERDSidebar
          activeEntity={sidebarEntity}
          onSelectEntity={handleSelectEntity}
          t={t}
        />
        <FlowCanvas nodes={nodes} edges={edges} t={t} />
      </ReactFlowProvider>
    </div>
  );
}
