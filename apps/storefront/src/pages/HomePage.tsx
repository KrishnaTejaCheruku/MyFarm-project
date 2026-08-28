import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/apiClient'

export function HomePage() {
	const categoriesQuery = useQuery({
		queryKey: ['categories'],
		queryFn: api.categories,
	})

	return (
		<section>
			<h1>MyFarm</h1>
			<p>Fresh produce, delivered on your schedule.</p>

			<Link className="delivery-check-link" to="/delivery">
				Check delivery in your area →
			</Link>

			<h2>Categories</h2>
			{categoriesQuery.isPending && <p>Loading categories…</p>}
			{categoriesQuery.isError && (
				<p role="alert">Couldn't load categories. Is the API running?</p>
			)}
			{categoriesQuery.data && (
				<ul className="category-grid">
					{categoriesQuery.data.map((category) => (
						<li key={category.code}>
							<Link to={`/categories/${category.code}`}>
								{category.name.en}
							</Link>
						</li>
					))}
				</ul>
			)}
		</section>
	)
}
