-- Preserva presupuestos históricos, pero retira el módulo del menú y perfiles.
UPDATE modules
SET erased = TRUE
WHERE LOWER(route) IN ('/budgets', 'budgets')
   OR LOWER(name) IN ('presupuestos', 'presupuesto', 'budgets', 'budget');
