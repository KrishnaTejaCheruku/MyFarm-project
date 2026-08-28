import { Route, Routes } from 'react-router-dom'
import { HomePage } from './pages/HomePage'
import { CategoryPage } from './pages/CategoryPage'
import { ProductPage } from './pages/ProductPage'
import { DeliveryCheckPage } from './pages/DeliveryCheckPage'
import './App.css'

function App() {
	return (
		<main id="app">
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
	)
}

export default App
