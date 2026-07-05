import type { Metadata, Viewport } from 'next'
import './globals.css'
import Header from '@/components/Header'
import { FavoritesProvider } from '@/lib/favorites'

export const metadata: Metadata = {
  title: '뚝딱밥',
  description: '장보기부터 레시피까지 한 번에',
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="ko">
      <body className="bg-gray-50 min-h-screen">
        <FavoritesProvider>
          <Header />
          <main className="max-w-5xl mx-auto px-4 py-6">
            {children}
          </main>
        </FavoritesProvider>
      </body>
    </html>
  )
}
