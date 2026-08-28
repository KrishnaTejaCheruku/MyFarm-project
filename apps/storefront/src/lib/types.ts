export interface LocalizedText {
	en: string
	te: string
}

export interface Category {
	code: string
	slug: string
	name: LocalizedText
}

export interface Money {
	currency: string
	amountInr: number
	taxInclusive: boolean
}

export interface Variant {
	sku: string
	quantity: string
	unit: string
	price: Money
	gstBasisPoints: number
	subscriptionAllowed: boolean
	imageKey: string
}

export interface Product {
	code: string
	slug: string
	categoryCode: string
	name: LocalizedText
	description: LocalizedText
	variants: Variant[]
}

export interface Page<T> {
	items: T[]
	page: number
	size: number
	totalElements: number
	totalPages: number
}

export interface ServiceArea {
	code: string
	name: LocalizedText
	city: string
	state: string
	timezone: string
	subscriptionRequired: boolean
}

export interface Eligibility {
	requestedAreaCode: string
	pincode: string
	serviceable: boolean
	serviceArea: ServiceArea | null
}

export interface DeliveryWindow {
	code: string
	name: LocalizedText
	startsAt: string
	endsAt: string
	cutoffMinutesBefore: number
}

export interface SubscriptionPlan {
	code: string
	name: LocalizedText
	billingPeriod: 'MONTHLY' | 'YEARLY'
	durationMonths: number
	deliveryFrequency: string
}

export interface DeliveryOptions {
	serviceAreaCode: string
	timezone: string
	windows: DeliveryWindow[]
	plans: SubscriptionPlan[]
}

export interface SchedulePreview {
	serviceAreaCode: string
	timezone: string
	windowCode: string
	planCode: string
	startsOn: string
	endsOn: string
	deliveryCount: number
	operatesEveryCalendarDay: boolean
	firstOrderCutoff: string
}

export interface ProblemDetail {
	type: string
	title: string
	status: number
	detail: string
	[key: string]: unknown
}
