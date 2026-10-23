#!/usr/bin/env node
'use strict';

const { run } = require('../src/cli');
const { createAtmClient } = require('../src/atmClient');
const { loadConfig } = require('../src/config');
const ExitCode = require('../src/exitCode');

async function main() {
  let config;
  try {
    config = loadConfig(process.env);
  } catch (error) {
    process.stderr.write(`Error de configuracion: ${error.message}\n`);
    return ExitCode.CONFIGURATION_ERROR;
  }

  const client = createAtmClient(config);
  return run(process.argv.slice(2), { client, out: process.stdout, err: process.stderr });
}

main().then((exitCode) => {
  process.exitCode = exitCode;
});
