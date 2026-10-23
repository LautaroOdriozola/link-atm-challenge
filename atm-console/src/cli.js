'use strict';

const ExitCode = require('./exitCode');
const { ServiceUnavailableError } = require('./atmClient');
const { parseAmount, formatMoney, UsageError } = require('./money');

const APP_NAME = 'atm';

const COMMANDS = {
  login: {
    usage: 'login <tarjeta>',
    arity: 1,
    async execute(client, [cardNumber]) {
      const response = await client.login(cardNumber);
      return response.success
        ? success('Ingreso exitoso')
        : failure('Ingreso no exitoso');
    },
  },

  extraer: {
    usage: 'extraer <tarjeta> <cuenta> <importe>',
    arity: 3,
    async execute(client, [cardNumber, accountNumber, rawAmount]) {
      const amount = parseAmount(rawAmount);
      const response = await client.withdraw(cardNumber, accountNumber, amount);
      return response.success
        ? success('Retire su dinero')
        : failure(errorMessage(response));
    },
  },

  depositar: {
    usage: 'depositar <tarjeta> <cbu> <importe>',
    arity: 3,
    async execute(client, [cardNumber, cbu, rawAmount]) {
      const amount = parseAmount(rawAmount);
      const response = await client.deposit(cardNumber, cbu, amount);
      return response.success
        ? success('Depósito exitoso')
        : failure(errorMessage(response));
    },
  },

  saldo: {
    usage: 'saldo <tarjeta> <cuenta>',
    arity: 2,
    async execute(client, [cardNumber, accountNumber]) {
      const response = await client.getBalance(cardNumber, accountNumber);
      if (!response.success) {
        return failure(errorMessage(response));
      }
      if (response.balance === null) {
        return failure('Error: el servicio no informo el saldo');
      }
      return success(`Su saldo es $ ${formatMoney(response.balance)}`);
    },
  },
};

const HELP_ALIASES = new Set(['ayuda', 'help', '-h', '--help']);

async function run(args, { client, out, err }) {
  const [rawCommand, ...params] = args;

  if (!rawCommand) {
    writeUsage(err);
    return ExitCode.USAGE_ERROR;
  }

  const commandName = rawCommand.toLowerCase();

  if (HELP_ALIASES.has(commandName)) {
    writeUsage(out);
    return ExitCode.OK;
  }

  const command = COMMANDS[commandName];
  if (!command) {
    err.write(`Error: comando desconocido '${rawCommand}'\n`);
    writeUsage(err);
    return ExitCode.USAGE_ERROR;
  }

  if (params.length !== command.arity) {
    err.write(`Error: cantidad de parametros incorrecta\n`);
    err.write(`Uso: ${APP_NAME} ${command.usage}\n`);
    return ExitCode.USAGE_ERROR;
  }

  try {
    const result = await command.execute(client, params);
    (result.ok ? out : err).write(`${result.message}\n`);
    return result.ok ? ExitCode.OK : ExitCode.OPERATION_REJECTED;
  } catch (error) {
    if (error instanceof UsageError) {
      err.write(`Error: ${error.message}\n`);
      err.write(`Uso: ${APP_NAME} ${command.usage}\n`);
      return ExitCode.USAGE_ERROR;
    }
    if (error instanceof ServiceUnavailableError) {
      err.write(`Error: ${error.message}. Verifique que el servicio este en ejecucion.\n`);
      return ExitCode.SERVICE_UNAVAILABLE;
    }
    throw error;
  }
}

function success(message) {
  return { ok: true, message };
}

function failure(message) {
  return { ok: false, message };
}

function errorMessage(response) {
  return response.message
    ? `Error: ${response.message}`
    : `Error: la operacion no pudo completarse (HTTP ${response.httpStatus})`;
}

function writeUsage(stream) {
  stream.write('Uso:\n');
  for (const command of Object.values(COMMANDS)) {
    stream.write(`  ${APP_NAME} ${command.usage}\n`);
  }
}

module.exports = { run };
