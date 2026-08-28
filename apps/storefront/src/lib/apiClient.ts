const API_BASE_URL =
	(import.meta.env.VITE_API_BASE_URL as string | undefined) ??
	'http://localhost:8080'

export class ApiError extends Error {
	readonly status: number
	readonly problem: unknown

	constructor(status: number, message: string, problem: unknown) {
		super(message)
		this.name = 'ApiError'
		this.status = status
		this.problem = problem
	}
}

async function request<T>(path: string): Promise<T> {
	const response = await fetch(`${API_BASE_URL}${path}`, {
		headers: { Accept: 'application/json' },
	})

	if (!response.ok) {
		const problem = await response.json().catch(() => null)
		const message =
			(problem && typeof problem === 'object' && 'detail' in problem
				? String((problem as { detail?: unknown }).detail)
				: undefined) ?? `Request to ${path} failed (${response.status})`
		throw new ApiError(response.status, message, problem)
	}

	return response.json() as Promise<T>
}

function query(params: Record<string, string | number | undefined>): string {
	const search = new URLSearchParams()
	for (const [key, value] of Object.entries(params)) {
		if (value !== undefined) search.set(key, String(value))
	}
	const qs = search.toString()
	return qs ? `?${qs}` : ''
}

export const api = {
	categories: () =>
		request<import('./types').Category[]>('/api/v1/catalog/categories'),

	products: (params: { category?: string; page?: number; size?: number }) =>
		request<import('./types').Page<import('./types').Product>>(
			`/api/v1/catalog/products${query(params)}`,
		),

	product: (slug: string) =>
		request<import('./types').Product>(
			`/api/v1/catalog/products/${encodeURIComponent(slug)}`,
		),

	serviceAreas: () =>
		request<import('./types').ServiceArea[]>('/api/v1/service-areas'),

	eligibility: (area: string, pincode: string) =>
		request<import('./types').Eligibility>(
			`/api/v1/service-areas/eligibility${query({ area, pincode })}`,
		),

	deliveryOptions: (areaCode: string) =>
		request<import('./types').DeliveryOptions>(
			`/api/v1/service-areas/${encodeURIComponent(areaCode)}/delivery-options`,
		),

	schedulePreview: (
		areaCode: string,
		params: { window: string; plan: string; startsOn: string },
	) =>
		request<import('./types').SchedulePreview>(
			`/api/v1/service-areas/${encodeURIComponent(areaCode)}/schedule-preview${query(
				params,
			)}`,
		),
}
