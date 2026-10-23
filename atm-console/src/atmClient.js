'use strict';

class ServiceUnavailableError extends Error {
  constructor(baseUrl, cause) {
    super(`No fue posible comunicarse con el servicio ATM en ${baseUrl}`);
    this.name = 'ServiceUnavailableError';
    this.baseUrl = baseUrl;
    this.cause = cause;
  }
}

function createAtmClient({ baseUrl, timeoutMs }) {
  async function request(method, path, body) {
    let response;
    try {
      response = await fetch(`${baseUrl}${path}`, {
        method,
        headers: body
          ? { Accept: 'application/json', 'Content-Type': 'application/json' }
          : { Accept: 'application/json' },
        body: body ? JSON.stringify(body) : undefined,
        signal: AbortSignal.timeout(timeoutMs),
      });
    } catch (error) {
      throw new ServiceUnavailableError(baseUrl, error);
    }

    let payload;
    try {
      payload = await response.json();
    } catch (error) {
      payload = null;
    }

    if (!payload || typeof payload !== 'object') {
      return { httpStatus: response.status, success: false, message: null, balance: null };
    }

    return {
      httpStatus: response.status,
      success: response.ok && payload.success === true,
      message: typeof payload.message === 'string' ? payload.message : null,
      balance: typeof payload.balance === 'number' ? payload.balance : null,
    };
  }

  return {
    login(cardNumber) {
      return request('POST', '/api/login', { cardNumber });
    },

    withdraw(cardNumber, accountNumber, amount) {
      return request('POST', '/api/withdraw', { cardNumber, accountNumber, amount });
    },

    deposit(cardNumber, cbu, amount) {
      return request('POST', '/api/deposit', { cardNumber, cbu, amount });
    },

    getBalance(cardNumber, accountNumber) {
      const query = new URLSearchParams({ cardNumber, accountNumber });
      return request('GET', `/api/balance?${query}`);
    },
  };
}

module.exports = { createAtmClient, ServiceUnavailableError };
