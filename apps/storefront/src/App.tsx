import { Route, Routes } from 'react-router-dom'
import { Header } from './components/Header'
import { HomePage } from './pages/HomePage'
import { CategoryPage } from './pages/CategoryPage'
import { ProductPage } from './pages/ProductPage'
import { DeliveryCheckPage } from './pages/DeliveryCheckPage'
import './App.css'

function App() {
	return (
		<div id="app">
			<Header />
			<main className="page">
				<Routes>
					<Route path="/" element={<HomePage />} />
					<Route
						path="/categories/:categoryCode"
						element={<CategoryPage />}
					/>
					<Route path="/products/:slug" element={<ProductPage />} />
					<Route path="/delivery" element={<DeliveryCheckPage />} />
				</Routes>
			</main>
			<footer className="site-footer">
				<p>MyFarm &mdash; fresh produce, delivered on your schedule.</p>
			</footer>
		</div>
	)
}

export default App
