import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/apiClient'

export function CategoryPage() {
	const { categoryCode } = useParams<{ categoryCode: string }>()
	const [page, setPage] = useState(0)

	const productsQuery = useQuery({
		queryKey: ['products', categoryCode, page],
		queryFn: () =>
			api.products({ category: categoryCode, page, size: 20 }),
		enabled: Boolean(categoryCode),
	})

	return (
		<section>
			<Link to="/">← All categories</Link>
			<h1>{categoryCode}</h1>

			{productsQuery.isPending && <p>Loading products…</p>}
			{productsQuery.isError && (
				<p role="alert">Couldn't load products.</p>
			)}
			{productsQuery.data && (
				<>
					<ul className="product-grid">
						{productsQuery.data.items.map((product) => (
							<li key={product.code}>
								<Link to={`/products/${product.slug}`}>
									{product.variants[0] && (
										<img
											src={`/products/${product.variants[0].imageKey}.svg`}
											alt=""
											width={96}
											height={96}
										/>
									)}
									<h3>{product.name.en}</h3>
									{product.variants[0] && (
										<p>
											₹{product.variants[0].price.amountInr}{' '}
											/ {product.variants[0].quantity}{' '}
											{product.variants[0].unit}
										</p>
									)}
								</Link>
							</li>
						))}
					</ul>
					{productsQuery.data.items.length === 0 && (
						<p>No products in this category yet.</p>
					)}
					<div className="pager">
						<button
							type="button"
							disabled={page === 0}
							onClick={() => setPage((p) => p - 1)}
						>
							Previous
						</button>
						<span>
							Page {productsQuery.data.page + 1} of{' '}
							{Math.max(productsQuery.data.totalPages, 1)}
						</span>
						<button
							type="button"
							disabled={
								page + 1 >= productsQuery.data.totalPages
							}
							onClick={() => setPage((p) => p + 1)}
						>
							Next
						</button>
					</div>
				</>
			)}
		</section>
	)
}
