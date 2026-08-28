import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api } from '../lib/apiClient'

export function DeliveryCheckPage() {
	const [areaCode, setAreaCode] = useState('')
	const [pincode, setPincode] = useState('')
	const [checkedArea, setCheckedArea] = useState<string | null>(null)
	const [checkedPincode, setCheckedPincode] = useState<string | null>(null)

	const areasQuery = useQuery({
		queryKey: ['service-areas'],
		queryFn: api.serviceAreas,
	})

	const eligibilityQuery = useQuery({
		queryKey: ['eligibility', checkedArea, checkedPincode],
		queryFn: () =>
			api.eligibility(checkedArea as string, checkedPincode as string),
		enabled: Boolean(checkedArea && checkedPincode),
	})

	const optionsQuery = useQuery({
		queryKey: ['delivery-options', checkedArea],
		queryFn: () => api.deliveryOptions(checkedArea as string),
		enabled: Boolean(
			checkedArea && eligibilityQuery.data?.serviceable,
		),
	})

	return (
		<section>
			<Link to="/">← Home</Link>
			<h1>Check delivery in your area</h1>

			<form
				onSubmit={(event) => {
					event.preventDefault()
					setCheckedArea(areaCode)
					setCheckedPincode(pincode)
				}}
			>
				<label>
					Service area
					<select
						value={areaCode}
						onChange={(event) => setAreaCode(event.target.value)}
						required
					>
						<option value="" disabled>
							Select an area
						</option>
						{areasQuery.data?.map((area) => (
							<option key={area.code} value={area.code}>
								{area.name.en} ({area.city})
							</option>
						))}
					</select>
				</label>
				<label>
					Pincode
					<input
						value={pincode}
						onChange={(event) => setPincode(event.target.value)}
						pattern="[0-9]{6}"
						maxLength={6}
						placeholder="530013"
						required
					/>
				</label>
				<button type="submit">Check</button>
			</form>

			{eligibilityQuery.data && (
				<p>
					{eligibilityQuery.data.serviceable
						? `We deliver to ${eligibilityQuery.data.pincode} in ${eligibilityQuery.data.serviceArea?.name.en}.`
						: `Sorry, ${eligibilityQuery.data.pincode} isn't in our delivery area yet.`}
				</p>
			)}

			{optionsQuery.data && (
				<>
					<h2>Delivery windows</h2>
					<ul>
						{optionsQuery.data.windows.map((window) => (
							<li key={window.code}>
								{window.name.en}: {window.startsAt}–
								{window.endsAt} (order by{' '}
								{window.cutoffMinutesBefore} min before)
							</li>
						))}
					</ul>

					<h2>Subscription plans</h2>
					<ul>
						{optionsQuery.data.plans.map((plan) => (
							<li key={plan.code}>
								{plan.name.en} — {plan.billingPeriod} (
								{plan.durationMonths} month
								{plan.durationMonths > 1 ? 's' : ''})
							</li>
						))}
					</ul>
				</>
			)}
		</section>
	)
}
