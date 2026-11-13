const { chromium } = require('playwright');
const fs = require('fs');
(async () => {
  const { size, jobs } = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: size, height: size } });
  for (const [input, output] of jobs) {
    const svg = fs.readFileSync(input, 'utf8').replace('<svg ', `<svg width="${size}" height="${size}" `);
    await page.setContent(`<html><body style="margin:0;background:#000">${svg}</body></html>`);
    await page.screenshot({ path: output });
  }
  await browser.close();
})();
