import { getApiClient } from './client';

const client = getApiClient();

export interface CreatePaymentIntentRequest {
  auctionId: string;
  amount: number;
}

export interface PaymentIntent {
  id: string;
  clientSecret: string;
  amount: number;
  currency: string;
  status: string;
}

export interface ConfirmPaymentRequest {
  paymentIntentId: string;
}

export interface PaymentRecord {
  id: string;
  auctionId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;
}

export const paymentService = {
  async createPaymentIntent(request: CreatePaymentIntentRequest): Promise<PaymentIntent> {
    const { data } = await client.post<PaymentIntent>('/payments/create-payment-intent', request);
    return data;
  },

  async confirmPayment(request: ConfirmPaymentRequest): Promise<void> {
    await client.post('/payments/confirm-payment', request);
  },

  async getPaymentHistory(page = 1, pageSize = 20): Promise<{ data: PaymentRecord[]; total: number }> {
    const { data } = await client.get<{ data: PaymentRecord[]; total: number }>('/payments/history', {
      params: { page, pageSize },
    });
    return data;
  },
};
