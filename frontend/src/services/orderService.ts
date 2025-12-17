import { apiFetch } from './api';
import type { OrderDTO, CreateOrderRequest } from '../types/api';

/**
 * Create a new limit order
 * POST /api/orders
 */
export const createOrder = async (order: CreateOrderRequest): Promise<OrderDTO> => {
  return apiFetch<OrderDTO>('/api/orders', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify(order),
  });
};

/**
 * Get a specific order by ID
 * GET /api/orders/{orderId}
 */
export const getOrderById = async (orderId: string): Promise<OrderDTO> => {
  return apiFetch<OrderDTO>(`/api/orders/${orderId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get all orders for an account (all statuses)
 * GET /api/orders/account/{accountId}
 */
export const getAccountOrders = async (accountId: string): Promise<OrderDTO[]> => {
  return apiFetch<OrderDTO[]>(`/api/orders/account/${accountId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get all pending orders for an account
 * GET /api/orders/account/{accountId}/pending
 */
export const getPendingOrders = async (accountId: string): Promise<OrderDTO[]> => {
  return apiFetch<OrderDTO[]>(`/api/orders/account/${accountId}/pending`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Cancel a pending order
 * DELETE /api/orders/{orderId}
 */
export const cancelOrder = async (orderId: string): Promise<OrderDTO> => {
  return apiFetch<OrderDTO>(`/api/orders/${orderId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};
