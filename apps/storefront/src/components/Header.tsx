import { Link } from 'react-router-dom'

export function Header() {
	return (
		<header className="site-header">
			<div className="site-header-inner">
				<Link to="/" className="brand">
					<span className="brand-mark" aria-hidden="true">
						<svg viewBox="0 0 32 32" width="28" height="28">
							<circle cx="16" cy="16" r="16" fill="#2e7d32" />
							<path
								d="M16 24c-4-1-7-5-6-10 3 0 6 2 7 5"
								fill="none"
								stroke="#fff"
								strokeWidth="2.2"
								strokeLinecap="round"
							/>
							<path
								d="M16 24c4-1 7-5 6-10-3 0-6 2-7 5"
								fill="none"
								stroke="#fff"
								strokeWidth="2.2"
								strokeLinecap="round"
							/>
							<line
								x1="16"
								y1="24"
								x2="16"
								y2="14"
								stroke="#fff"
								strokeWidth="2.2"
								strokeLinecap="round"
							/>
						</svg>
					</span>
					<span className="brand-name">MyFarm</span>
				</Link>
				<nav className="site-nav">
					<Link to="/">Shop</Link>
					<Link to="/delivery">Delivery</Link>
				</nav>
			</div>
		</header>
	)
}
