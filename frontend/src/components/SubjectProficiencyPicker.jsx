const PROFICIENCY_LEVELS = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

const proficiencyLabel = (level) =>
  level.charAt(0) + level.slice(1).toLowerCase();

/**
 * Lets a mentor build their { subjectId, proficiencyLevel } list from the
 * shared subject catalog. Each subject can only be picked once.
 *
 * Props:
 *  - subjects: catalog from GET /api/subjects  [{ id, name, description }]
 *  - value: [{ subjectId, proficiencyLevel }]
 *  - onChange(nextValue)
 */
export default function SubjectProficiencyPicker({ subjects, value, onChange }) {
  const subjectById = new Map(subjects.map((s) => [s.id, s]));
  const pickedIds = new Set(value.map((row) => row.subjectId));
  const availableForNewRow = subjects.filter((s) => !pickedIds.has(s.id));

  const updateRow = (index, patch) => {
    const next = value.map((row, i) => (i === index ? { ...row, ...patch } : row));
    onChange(next);
  };

  const removeRow = (index) => {
    onChange(value.filter((_, i) => i !== index));
  };

  const addRow = () => {
    if (availableForNewRow.length === 0) return;
    onChange([
      ...value,
      { subjectId: availableForNewRow[0].id, proficiencyLevel: 'BEGINNER' },
    ]);
  };

  return (
    <div className="subject-picker">
      {value.length === 0 && (
        <div className="empty-state" style={{ padding: '16px 0' }}>
          No subjects added yet. Add at least one to save your profile.
        </div>
      )}

      {value.map((row, index) => {
        // Options for this row = subjects not picked elsewhere, plus its own current pick.
        const rowOptions = subjects.filter(
          (s) => s.id === row.subjectId || !pickedIds.has(s.id)
        );
        const unknownSubject = !subjectById.has(row.subjectId);

        return (
          <div
            key={index}
            className="subject-picker-row"
            style={{ display: 'flex', gap: 10, alignItems: 'flex-start', marginBottom: 12 }}
          >
            <div className="form-field" style={{ flex: 2, marginBottom: 0 }}>
              {index === 0 && <label>Subject</label>}
              <select
                value={row.subjectId}
                onChange={(e) => updateRow(index, { subjectId: Number(e.target.value) })}
              >
                {unknownSubject && (
                  <option value={row.subjectId} disabled>
                    Unknown subject
                  </option>
                )}
                {rowOptions.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-field" style={{ flex: 1, marginBottom: 0 }}>
              {index === 0 && <label>Proficiency</label>}
              <select
                value={row.proficiencyLevel}
                onChange={(e) => updateRow(index, { proficiencyLevel: e.target.value })}
              >
                {PROFICIENCY_LEVELS.map((level) => (
                  <option key={level} value={level}>
                    {proficiencyLabel(level)}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="button"
              className="btn btn-ghost"
              style={{
                color: 'var(--color-danger)',
                border: '1px solid var(--color-border)',
                padding: '9px 12px',
                marginTop: index === 0 ? 22 : 0,
              }}
              onClick={() => removeRow(index)}
              aria-label="Remove subject"
            >
              Remove
            </button>
          </div>
        );
      })}

      <button
        type="button"
        className="btn btn-ghost"
        style={{ color: 'var(--color-primary)', border: '1px solid var(--color-border)', padding: '8px 14px' }}
        onClick={addRow}
        disabled={availableForNewRow.length === 0}
      >
        + Add subject
      </button>
      {availableForNewRow.length === 0 && subjects.length > 0 && (
        <span style={{ marginLeft: 10, fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
          All catalog subjects added.
        </span>
      )}
    </div>
  );
}
