import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';

const routerFile = path.resolve(process.cwd(), 'src/router/router.js');
const content = fs.readFileSync(routerFile, 'utf8');

const requiredRoutes = ['/studio', '/repository', '/work'];
const missingRoutes = requiredRoutes.filter((routePath) => !content.includes(`path: '${routePath}'`));

if (missingRoutes.length > 0) {
  console.error(`[frontend-test] missing routes: ${missingRoutes.join(', ')}`);
  process.exit(1);
}

console.log('[frontend-test] route smoke pass');
