import { Builder, By, until } from 'selenium-webdriver';

const baseUrl = process.env.FRONTEND_URL ?? 'http://localhost:5174';
const apiUrl = process.env.API_URL ?? 'http://localhost:8080';

async function login(driver) {
  await driver.get(`${baseUrl}/login`);
  await driver.wait(until.elementLocated(By.css('input[type="email"]')), 8000);
  await driver.findElement(By.css('input[type="email"]')).sendKeys('admin@greenhouse.local');
  await driver.findElement(By.css('input[type="password"]')).sendKeys('admin1234');
  await driver.findElement(By.css('button[type="submit"]')).click();
  await driver.wait(until.elementLocated(By.css('.metrics')), 8000);
}

async function runIotFlow() {
  const driver = await new Builder().forBrowser('chrome').build();
  try {
    console.log('1. Login...');
    await login(driver);
    console.log('  PASS: Login and dashboard loaded');

    console.log('2. Check simulator panel...');
    const simPanel = await driver.findElement(By.css('.simulatorPanel'));
    console.log('  PASS: Simulator panel visible');

    console.log('3. Start simulator...');
    const startBtn = await driver.findElement(By.css('.simulatorBtn.start'));
    await startBtn.click();
    await driver.sleep(2000);
    const statusActive = await driver.findElements(By.css('.pulseDot.active'));
    if (statusActive.length === 0) {
      throw new Error('Simulator did not show active status');
    }
    console.log('  PASS: Simulator started');

    console.log('4. Wait for readings to appear...');
    await driver.sleep(6000);
    const readingCards = await driver.findElements(By.css('.readingCard'));
    if (readingCards.length === 0) {
      throw new Error('No reading cards appeared after simulation');
    }
    console.log(`  PASS: ${readingCards.length} reading cards visible`);

    console.log('5. Navigate to Alerts...');
    const navAlerts = await driver.findElement(By.xpath("//a[contains(text(), 'Alertas')]"));
    await navAlerts.click();
    await driver.sleep(1000);
    const alertRows = await driver.findElements(By.css('.styledTable tbody tr'));
    console.log(`  PASS: Alerts section loaded with ${alertRows.length} rows`);

    console.log('6. Navigate to IA...');
    const navIa = await driver.findElement(By.xpath("//a[contains(text(), 'IA')]"));
    await navIa.click();
    await driver.sleep(2000);
    const iaValue = await driver.findElement(By.css('.iaValue'));
    const iaText = await iaValue.getText();
    if (!iaText && iaText !== '0') {
      throw new Error('IA prediction not visible');
    }
    console.log(`  PASS: IA prediction visible: ${iaText}`);

    console.log('7. Stop simulator...');
    const stopBtn = await driver.findElement(By.css('.simulatorBtn.stop'));
    await stopBtn.click();
    await driver.sleep(1000);
    const statusInactive = await driver.findElements(By.css('.pulseDot:not(.active)'));
    if (statusInactive.length === 0) {
      throw new Error('Simulator did not show inactive status');
    }
    console.log('  PASS: Simulator stopped');

    console.log('\n=== ALL SELENIUM IoT FLOW TESTS PASSED ===');
  } catch (err) {
    console.error('\n=== SELENIUM TEST FAILED ===');
    console.error(err.message);
    try {
      const screenshot = await driver.takeScreenshot();
      const fs = await import('fs');
      fs.writeFileSync('selenium-error.png', screenshot, 'base64');
      console.log('Screenshot saved to selenium-error.png');
    } catch (_) {}
    process.exit(1);
  } finally {
    await driver.quit();
  }
}

runIotFlow();
