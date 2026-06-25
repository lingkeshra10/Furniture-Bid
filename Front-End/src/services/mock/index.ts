/**
 * Mock mode detection and initialization.
 *
 * When VITE_USE_MOCKS=true, this module registers a mock Axios client
 * that intercepts requests and routes them to mock handlers.
 *
 * Requirement: 14.4
 */

import axios from 'axios';
import { registerMockClient } from '@/services/api/client';
import { handleMockRequest } from './mockApiHandlers';

/**
 * Returns true when the app is running in mock mode.
 */
export function isMockMode(): boolean {
  return import.meta.env.VITE_USE_MOCKS === 'true';
}

/**
 * Initializes the mock layer by creating a fake Axios instance
 * that delegates all requests to mock handlers.
 * Call this once during app bootstrap when `isMockMode()` is true.
 */
export function initMocks(): void {
  if (!isMockMode()) return;

  const mockClient = axios.create();

  // Override the adapter to route requests through mock handlers
  mockClient.defaults.adapter = async (config) => {
    const method = (config.method ?? 'get').toLowerCase();
    const url = config.url ?? '';
    const data = config.data ? (typeof config.data === 'string' ? JSON.parse(config.data) : config.data) : undefined;
    const params = config.params;

    const response = await handleMockRequest(method, url, data, params);

    return {
      data: response.data,
      status: response.status,
      statusText: response.status === 200 ? 'OK' : 'Error',
      headers: { 'content-type': 'application/json' },
      config,
    };
  };

  registerMockClient(mockClient);

  console.info('[Mock] Mock API layer initialized');
}
