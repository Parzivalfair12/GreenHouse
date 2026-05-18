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
  } finally {
    await driver.quit();
  }
}

run();
