import { memo } from 'react';
import { Handle, Position } from '@xyflow/react';
import { Key, Link2, Check } from 'lucide-react';

function EntityNode({ data, selected }) {
  const { entity } = data;
  const hasFks = entity.fields.some(f => f.fk);

  return (
    <div className={`entityNode ${selected ? 'selected' : ''}`}>
      <div className="entityNodeHeader">
        <span className="entityNodeName">{entity.name}</span>
        <span className="entityNodeTable">{entity.table}</span>
      </div>
      <div className="entityNodeFields">
        {entity.fields.map((field) => (
          <div
            key={field.name}
            className={`entityNodeField ${field.pk ? 'pk' : ''} ${field.fk ? 'fk' : ''} ${field.unique ? 'unique' : ''}`}
          >
            <span className="entityNodeFieldIcon">
              {field.pk && <Key size={11} />}
              {field.fk && <Link2 size={11} />}
              {field.unique && !field.pk && <Check size={11} />}
            </span>
            <span className="entityNodeFieldName">{field.name}</span>
            <span className="entityNodeFieldType">{field.type}</span>
          </div>
        ))}
      </div>
      {hasFks && (
        <div className="entityNodeHandles">
          {entity.fields.filter(f => f.fk).map((field) => (
            <Handle
              key={`handle-${entity.name}-${field.name}`}
              type="target"
              position={Position.Left}
              id={`handle-${entity.name}-${field.name}`}
              style={{ background: 'var(--accent)', width: 8, height: 8, border: '2px solid #0b0d0f' }}
            />
          ))}
        </div>
      )}
      <Handle
        type="source"
        position={Position.Right}
        id={`handle-${entity.name}-source`}
        style={{ background: 'var(--cyan)', width: 8, height: 8, border: '2px solid #0b0d0f' }}
      />
    </div>
  );
}

export default memo(EntityNode);
