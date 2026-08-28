import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/apiClient'

export function CategoryPage() {
	const { categoryCode } = useParams<{ categoryCode: string }>()
	const [page, setPage] = useState(0)

	const categoriesQuery = useQuery({
		queryKey: ['categories'],
		queryFn: api.categories,
	})
	const category = categoriesQuery.data?.find(
		(item) => item.code === categoryCode,
	)

	const productsQuery = useQuery({
		queryKey: ['products', categoryCode, page],
		queryFn: () =>
			api.products({ category: categoryCode, page, size: 20 }),
		enabled: Boolean(categoryCode),
	})

	return (
		<section>
			<Link className="back-link" to="/">
				← All categories
			</Link>
			<h1>{category?.name.en ?? categoryCode}</h1>

			{productsQuery.isPending && <p>Loading products…</p>}
			{productsQuery.isError && (
				<p role="alert">Couldn't load products.</p>
			)}
			{productsQuery.data && (
				<>
					{productsQuery.data.items.length === 0 ? (
						<p className="empty-state">
							No products in this category yet.
						</p>
					) : (
						<ul className="product-grid">
							{productsQuery.data.items.map((product) => (
								<li key={product.code}>
									<Link
										className="product-card"
										to={`/products/${product.slug}`}
									>
										{product.variants[0] && (
											<img
												src={`/products/${product.variants[0].imageKey}.svg`}
												alt=""
												width={110}
												height={110}
											/>
										)}
										<h3>{product.name.en}</h3>
										{product.variants[0] && (
											<p className="product-price">
												₹{product.variants[0].price.amountInr}{' '}
												<small>
													/ {product.variants[0].quantity}{' '}
													{product.variants[0].unit}
												</small>
											</p>
										)}
									</Link>
								</li>
							))}
						</ul>
					)}
					{productsQuery.data.totalPages > 1 && (
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
								{productsQuery.data.totalPages}
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
					)}
				</>
			)}
		</section>
	)
}
