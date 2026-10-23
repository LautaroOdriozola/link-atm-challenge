'use strict';

const DEFAULT_BASE_URL = 'http://localhost:8080';
const DEFAULT_TIMEOUT_MS = 10000;

function loadConfig(env) {
  const rawBaseUrl = (env.ATM_SERVICE_URL || '').trim() || DEFAULT_BASE_URL;
  const rawTimeout = (env.ATM_TIMEOUT_MS || '').trim();

  return {
    baseUrl: normalizeBaseUrl(rawBaseUrl),
    timeoutMs: rawTimeout ? parseTimeout(rawTimeout) : DEFAULT_TIMEOUT_MS,
  };
}

function normalizeBaseUrl(rawBaseUrl) {
  let url;
  try {
    url = new URL(rawBaseUrl);
  } catch (error) {
    throw new Error(`la URL del servicio no es valida: ${rawBaseUrl}`);
  }
  if (url.protocol !== 'http:' && url.protocol !== 'https:') {
    throw new Error(`la URL del servicio debe ser http o https: ${rawBaseUrl}`);
  }
  return rawBaseUrl.replace(/\/+$/, '');
}

function parseTimeout(rawTimeout) {
  const timeout = Number(rawTimeout);
  if (!Number.isInteger(timeout) || timeout <= 0) {
    throw new Error(`ATM_TIMEOUT_MS debe ser un entero positivo: ${rawTimeout}`);
  }
  return timeout;
}

module.exports = { loadConfig, DEFAULT_BASE_URL, DEFAULT_TIMEOUT_MS };
