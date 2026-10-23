'use strict';

const AMOUNT_PATTERN = /^\d+([.,]\d{1,2})?$/;

class UsageError extends Error {
  constructor(message) {
    super(message);
    this.name = 'UsageError';
  }
}

function parseAmount(rawAmount) {
  const value = String(rawAmount ?? '').trim();
  if (!AMOUNT_PATTERN.test(value)) {
    throw new UsageError(
      `Importe invalido: '${rawAmount}'. Use un numero positivo con hasta 2 decimales (ej. 1500 o 1500,50)`
    );
  }
  const amount = Number(value.replace(',', '.'));
  if (amount <= 0) {
    throw new UsageError('Importe invalido: debe ser mayor a cero');
  }
  return amount;
}

function formatMoney(amount) {
  return amount.toFixed(2).replace('.', ',');
}

module.exports = { parseAmount, formatMoney, UsageError };
