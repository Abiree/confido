// scripts/run-gradle.js
const { execSync } = require('child_process');
const os = require('os');
const path = require('path');

const args = process.argv.slice(2); // arguments like ["build", "-x", "test"]
const cwd = path.join(__dirname, '../apps/api');

// Ensure gradlew is executable on mac/linux
if (os.platform() !== 'win32') {
  try {
    execSync('chmod +x gradlew', { cwd });
  } catch (ex) {
    console.error('Failed to make gradlew executable', ex);
    process.exit(1);
  }
}

const command =
  os.platform() === 'win32'
    ? `gradlew.bat ${args.join(' ')}`
    : `./gradlew ${args.join(' ')}`;

console.log(`Running command: ${command} in ${cwd}`);

try {
  execSync(command, { stdio: 'inherit', cwd });
} catch (ex) {
  console.error('Gradle command failed', ex.message);
  process.exit(1);
}
