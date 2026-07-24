-- V10: configurable short-circuit on guardrail set evaluation (#55)
ALTER TABLE guardrail_sets
    ADD COLUMN short_circuit_on_block BOOLEAN NOT NULL DEFAULT TRUE;
