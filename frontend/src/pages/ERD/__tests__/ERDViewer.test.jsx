import { describe, it, expect, beforeAll } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ERDViewer } from '../ERDViewer.jsx';

describe('ERDViewer', () => {
  beforeAll(() => {
    document.body.innerHTML = '<div id="root"></div>';
  });

  it('renders without crashing', async () => {
    const { container } = render(
      <MemoryRouter>
        <ERDViewer />
      </MemoryRouter>
    );
    await waitFor(() => expect(container.querySelector('.erdLayout')).toBeDefined());
  });

  it('renders entity sidebar with entity list', async () => {
    render(
      <MemoryRouter>
        <ERDViewer />
      </MemoryRouter>
    );
    const greenhouses = await screen.findAllByText('Greenhouse');
    expect(greenhouses.length).toBeGreaterThanOrEqual(1);
    const sensors = await screen.findAllByText('Sensor');
    expect(sensors.length).toBeGreaterThanOrEqual(1);
  });

  it('shows entity field count badges', async () => {
    render(
      <MemoryRouter>
        <ERDViewer />
      </MemoryRouter>
    );
    await waitFor(() => {
      const badges = document.querySelectorAll('.erdEntityItemBadge');
      expect(badges.length).toBeGreaterThan(0);
    });
  });

  it('renders export buttons', async () => {
    render(
      <MemoryRouter>
        <ERDViewer />
      </MemoryRouter>
    );
    const jsonBtns = await screen.findAllByText('Export JSON');
    expect(jsonBtns.length).toBeGreaterThanOrEqual(1);
    const sqlBtns = await screen.findAllByText('Export SQL');
    expect(sqlBtns.length).toBeGreaterThanOrEqual(1);
  });

  it('renders search input', async () => {
    render(
      <MemoryRouter>
        <ERDViewer />
      </MemoryRouter>
    );
    const inputs = await screen.findAllByPlaceholderText('Search entities...');
    expect(inputs.length).toBeGreaterThanOrEqual(1);
  });

  it('shows entity statistics', async () => {
    render(
      <MemoryRouter>
        <ERDViewer />
      </MemoryRouter>
    );
    const entities = await screen.findAllByText(/entities/);
    expect(entities.length).toBeGreaterThanOrEqual(1);
  });
});
