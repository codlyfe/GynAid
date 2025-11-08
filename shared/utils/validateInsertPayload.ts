type InsertPayload = {
  table: string;
  columns: string[];
  values: unknown[];
  defaults?: Record<string, unknown>;
};

export function prepareSafeInsert({
  table,
  columns,
  values,
  defaults = {}
}: InsertPayload): { safeColumns: string[]; safeValues: unknown[]; log: string } {
  const colCount = columns.length;
  const valCount = values.length;
  const log: string[] = [];

  let safeColumns = [...columns];
  let safeValues = [...values];

  if (valCount < colCount) {
    const missingCount = colCount - valCount;
    log.push(`⚠️ [${table}] ${missingCount} value(s) missing. Attempting fallback.`);

    const missingCols = columns.slice(valCount);
    missingCols.forEach((col) => {
      const fallback = defaults[col] ?? null;
      safeValues.push(fallback);
      log.push(`🧩 Filled '${col}' with fallback: ${JSON.stringify(fallback)}`);
    });
  } else if (valCount > colCount) {
    log.push(`❌ [${table}] Value count (${valCount}) exceeds column count (${colCount}). Trimming excess.`);
    safeValues = safeValues.slice(0, colCount);
  } else {
    log.push(`✅ [${table}] Column and value counts match.`);
  }

  return {
    safeColumns,
    safeValues,
    log: log.join('\n')
  };
}