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
			<Link className="back-link" to="/">
				← Home
			</Link>
			<h1>Check delivery in your area</h1>
			<p>Tell us your area and pincode to see delivery windows and plans.</p>

			<div className="delivery-card">
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
			</div>

			{eligibilityQuery.data && (
				<div
					className={`result-banner ${eligibilityQuery.data.serviceable ? 'ok' : 'no'}`}
				>
					{eligibilityQuery.data.serviceable
						? `We deliver to ${eligibilityQuery.data.pincode} in ${eligibilityQuery.data.serviceArea?.name.en}.`
						: `Sorry, ${eligibilityQuery.data.pincode} isn't in our delivery area yet.`}
				</div>
			)}

			{optionsQuery.data && (
				<>
					<h2>Delivery windows</h2>
					<ul className="info-list">
						{optionsQuery.data.windows.map((window) => (
							<li key={window.code}>
								<strong>{window.name.en}</strong>:{' '}
								{window.startsAt}–{window.endsAt} (order by{' '}
								{window.cutoffMinutesBefore} min before)
							</li>
						))}
					</ul>

					<h2>Subscription plans</h2>
					<ul className="info-list">
						{optionsQuery.data.plans.map((plan) => (
							<li key={plan.code}>
								<strong>{plan.name.en}</strong> —{' '}
								{plan.billingPeriod} ({plan.durationMonths} month
								{plan.durationMonths > 1 ? 's' : ''})
							</li>
						))}
					</ul>
				</>
			)}
		</section>
	)
}
