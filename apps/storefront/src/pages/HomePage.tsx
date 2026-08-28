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
			<div className="hero">
				<h1>Fresh produce, delivered on your schedule.</h1>
				<p>
					Farm-fresh vegetables, fruits, dairy and staples from
					MyFarm &mdash; picked, packed, and delivered to your
					door.
				</p>
				<Link className="hero-cta" to="/delivery">
					Check delivery in your area →
				</Link>
			</div>

			<h2 className="section-title">Shop by category</h2>
			{categoriesQuery.isPending && <p>Loading categories…</p>}
			{categoriesQuery.isError && (
				<p role="alert">Couldn't load categories. Is the API running?</p>
			)}
			{categoriesQuery.data && (
				<ul className="category-grid">
					{categoriesQuery.data.map((category) => (
						<li key={category.code}>
							<Link
								className="category-card"
								to={`/categories/${category.code}`}
							>
								<img
									src={`/categories/${category.code}.svg`}
									alt=""
									width={72}
									height={72}
								/>
								<span>{category.name.en}</span>
							</Link>
						</li>
					))}
				</ul>
			)}
		</section>
	)
}
