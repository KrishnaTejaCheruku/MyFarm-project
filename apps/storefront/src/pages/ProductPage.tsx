import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/apiClient'

export function ProductPage() {
	const { slug } = useParams<{ slug: string }>()

	const productQuery = useQuery({
		queryKey: ['product', slug],
		queryFn: () => api.product(slug as string),
		enabled: Boolean(slug),
	})

	if (productQuery.isPending) return <p>Loading…</p>
	if (productQuery.isError || !productQuery.data) {
		return <p role="alert">Product not found.</p>
	}

	const product = productQuery.data

	return (
		<section>
			<Link to={`/categories/${product.categoryCode}`}>
				← Back
			</Link>
			{product.variants[0] && (
				<img
					src={`/products/${product.variants[0].imageKey}.svg`}
					alt=""
					width={160}
					height={160}
				/>
			)}
			<h1>{product.name.en}</h1>
			<p>{product.description.en}</p>

			<h2>Variants</h2>
			<ul className="variant-list">
				{product.variants.map((variant) => (
					<li key={variant.sku}>
						<img
							src={`/products/${variant.imageKey}.svg`}
							alt=""
							width={40}
							height={40}
						/>
						<span>
							{variant.quantity} {variant.unit}
						</span>
						<span>₹{variant.price.amountInr}</span>
						{variant.subscriptionAllowed && (
							<span className="badge">Subscribable</span>
						)}
					</li>
				))}
			</ul>
		</section>
	)
}
