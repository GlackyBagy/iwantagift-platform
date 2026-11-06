import '@fontsource/onest/latin-400.css'
import '@fontsource/onest/cyrillic-400.css'
import '@fontsource/onest/latin-500.css'
import '@fontsource/onest/cyrillic-500.css'
import '@fontsource/onest/latin-600.css'
import '@fontsource/onest/cyrillic-600.css'
import '@fontsource/unbounded/latin-600.css'
import '@fontsource/unbounded/cyrillic-600.css'
import '@fontsource/unbounded/latin-700.css'
import '@fontsource/unbounded/cyrillic-700.css'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
