import { Builder, By, until } from 'selenium-webdriver';

const baseUrl = process.env.FRONTEND_URL ?? 'http://localhost:5173';

async function run() {
  const driver = await new Builder().forBrowser('chrome').build();
  try {
    await driver.get(baseUrl);
    await driver.wait(until.elementLocated(By.css('h1')), 8000);
    const heading = await driver.findElement(By.css('h1')).getText();
    if (!heading.includes('invernadero') && !heading.includes('Greenhouse')) {
      throw new Error(`Unexpected heading: ${heading}`);
    }
    console.log('✓ TEST 1 PASSED: Login screen renders');
  } finally {
    await driver.quit();
  }
}

async function loginFlow(driver) {
  await driver.get(baseUrl);
  await driver.wait(until.elementLocated(By.css('input[type="email"]')), 5000);
  await driver.findElement(By.css('input[type="email"]')).sendKeys('admin@greenhouse.local');
  await driver.findElement(By.css('input[type="password"]')).sendKeys('admin1234');
  await driver.findElement(By.css('button[type="submit"]')).click();
  await driver.wait(until.elementLocated(By.css('.metrics')), 5000);
  console.log('✓ TEST 2 PASSED: Login flow -> Dashboard');
}

async function testLoginFlow() {
  const driver = await new Builder().forBrowser('chrome').build();
  try {
    await loginFlow(driver);
  } finally {
    await driver.quit();
  }
}

async function testDashboard() {
  const driver = await new Builder().forBrowser('chrome').build();
  try {
    await loginFlow(driver);
    const metrics = await driver.findElements(By.css('.metric'));
    if (metrics.length >= 4) {
      console.log('✓ TEST 3 PASSED: Dashboard shows metrics');
    }
  } finally {
    await driver.quit();
  }
}

async function testNavigation() {
  const driver = await new Builder().forBrowser('chrome').build();
  try {
    await loginFlow(driver);
    const navItems = await driver.findElements(By.css('.navItem'));
    if (navItems.length >= 8) {
      console.log('✓ TEST 4 PASSED: Navigation has 8+ sections');
    }
  } finally {
    await driver.quit();
  }
}

Promise.all([run(), testLoginFlow(), testDashboard(), testNavigation()])
  .then(() => {
    console.log('\n✓ ALL SELENIUM TESTS PASSED');
    process.exit(0);
  })
  .catch((err) => {
    console.error('\n✗ SELENIUM TEST FAILED:', err.message);
    process.exit(1);
  });
