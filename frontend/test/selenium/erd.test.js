import { Builder, By, until } from 'selenium-webdriver';

const baseUrl = process.env.FRONTEND_URL ?? 'http://localhost:5173';

async function run() {
  const driver = await new Builder().forBrowser('chrome').build();
  try {
    // Test ERD viewer page
    await driver.get(`${baseUrl}/erd`);
    await driver.wait(until.elementLocated(By.css('.erdSidebar')), 8000);

    const sidebar = await driver.findElement(By.css('.erdSidebar'));
    const sidebarText = await sidebar.getText();
    if (!sidebarText.includes('Entities')) {
      throw new Error('ERD sidebar missing Entities header');
    }

    // Verify entity items are rendered
    const entityItems = await driver.findElements(By.css('.erdEntityItem'));
    if (entityItems.length === 0) {
      throw new Error('No entity items found in ERD sidebar');
    }

    // Test search input exists
    const searchInput = await driver.findElement(By.css('.erdSearch'));
    if (!searchInput) {
      throw new Error('Search input not found');
    }

    // Test export buttons
    const exportJSON = await driver.findElement(By.xpath("//button[contains(text(), 'Export JSON')]"));
    const exportSQL = await driver.findElement(By.xpath("//button[contains(text(), 'Export SQL')]"));
    if (!exportJSON || !exportSQL) {
      throw new Error('Export buttons not found');
    }

    // Test React Flow canvas is rendered
    const reactFlow = await driver.findElement(By.css('.react-flow'));
    if (!reactFlow) {
      throw new Error('React Flow canvas not found');
    }

    console.log('All ERD Selenium tests passed!');
  } finally {
    await driver.quit();
  }
}

run().catch((err) => {
  console.error('ERD Selenium test failed:', err.message);
  process.exit(1);
});
