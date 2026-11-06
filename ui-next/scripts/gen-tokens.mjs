// Generates src/styles/tokens.css from design/tokens.json.
// Run: npm run tokens
import { readFileSync, writeFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const tokens = JSON.parse(readFileSync(resolve(root, 'design/tokens.json'), 'utf8'))
const g = tokens.global

const rem = (px) => `${+(Number(px) / 16).toFixed(4)}rem`
const percentToEm = (v) => (String(v).endsWith('%') ? `${+(parseFloat(v) / 100).toFixed(4)}em` : '0')

// "{color.ink.950}" -> "var(--color-ink-950)"
const ref = (v) =>
  v.replace(/\{color\.([^}]+)\}/g, (_, path) => `var(--color-${path.replaceAll('.', '-')})`)

// Semantic token names: text* -> fg*, to avoid a "text-text" class
const semName = (key) => key.replace(/^text/, 'fg')

const lines = []
const out = (s = '') => lines.push(s)

out('/* GENERATED from design/tokens.json by `npm run tokens`. Do not edit by hand. */')
out()
out('/* The theme is switched by the data-theme attribute on <html>. Dark by default. */')
out("@custom-variant dark (&:where([data-theme='dark'], [data-theme='dark'] *));")
out()

// ── Primitives ─────────────────────────────────────────────
out('/* static: emit every variable, even if no class uses it */')
out('@theme static {')
out('  --color-*: initial; /* drop the default Tailwind palette */')
out()
for (const [group, shades] of Object.entries(g.color)) {
  for (const [shade, t] of Object.entries(shades)) {
    out(`  --color-${group}-${shade}: ${t.value.toLowerCase()};`)
  }
  out()
}

out('  --font-display: "Unbounded", "Onest", sans-serif;')
out('  --font-body: "Onest", system-ui, sans-serif;')
out()

out('  --radius-*: initial;')
for (const [k, t] of Object.entries(g.radius)) {
  out(`  --radius-${k}: ${t.value}px;`)
}
out()

for (const [k, t] of Object.entries(g.typography)) {
  const v = t.value
  out(`  --text-${k}: ${rem(v.fontSize)};`)
  out(`  --text-${k}--line-height: ${rem(v.lineHeight)};`)
  out(`  --text-${k}--font-weight: ${v.fontWeight};`)
  out(`  --text-${k}--letter-spacing: ${percentToEm(v.letterSpacing)};`)
}
out()
out('  --breakpoint-md: 768px;')
out('  --breakpoint-lg: 1024px;')
out('  --breakpoint-xl: 1440px;')
out('}')
out()

// ── Control heights (h-(--control-h-md)) ───────────────────
out(':root {')
for (const [k, t] of Object.entries(g.control)) {
  out(`  --control-h-${k}: ${t.value}px;`)
}
out('}')
out()

// ── Themes ─────────────────────────────────────────────────
const themeBlock = (selector, set, scheme) => {
  out(`${selector} {`)
  out(`  color-scheme: ${scheme};`)
  for (const [key, t] of Object.entries(set.color)) {
    out(`  --${semName(key)}: ${ref(t.value).toLowerCase()};`)
  }
  out('}')
  out()
}
themeBlock(":root,\n[data-theme='dark']", tokens['theme/dark'], 'dark')
themeBlock("[data-theme='light']", tokens['theme/light'], 'light')

// ── Semantic colors exposed as Tailwind classes ────────────
out('/* inline: the class references var(--name), so the color follows the theme */')
out('@theme inline {')
for (const key of Object.keys(tokens['theme/dark'].color)) {
  const n = semName(key)
  out(`  --color-${n}: var(--${n});`)
}
out('}')

writeFileSync(resolve(root, 'src/styles/tokens.css'), lines.join('\n') + '\n')
console.log('src/styles/tokens.css generated')
