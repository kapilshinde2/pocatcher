import { getTypeColor } from './utils'

const KNOWN_TYPES = [
  'normal',
  'fire',
  'water',
  'electric',
  'grass',
  'ice',
  'fighting',
  'poison',
  'ground',
  'flying',
  'psychic',
  'bug',
  'rock',
  'ghost',
  'dragon',
  'dark',
  'steel',
  'fairy',
] as const

describe('getTypeColor', () => {
  describe('known types return a non-empty bg- class', () => {
    it.each(KNOWN_TYPES)('%s returns a non-empty string starting with bg-', (type) => {
      const result = getTypeColor(type)
      expect(result).toBeTruthy()
      expect(result.startsWith('bg-')).toBe(true)
    })
  })

  describe('unknown type fallback', () => {
    it('returns bg-slate-400 for an unknown type', () => {
      expect(getTypeColor('unknown')).toBe('bg-slate-400')
    })

    it('returns bg-slate-400 for an empty string', () => {
      expect(getTypeColor('')).toBe('bg-slate-400')
    })

    it('returns bg-slate-400 for a random nonsense type', () => {
      expect(getTypeColor('notavalidtype')).toBe('bg-slate-400')
    })
  })

  describe('purity — same input always returns same output', () => {
    it.each(KNOWN_TYPES)('%s returns the same value on repeated calls', (type) => {
      const first = getTypeColor(type)
      const second = getTypeColor(type)
      expect(first).toBe(second)
    })

    it('unknown type returns the same fallback on repeated calls', () => {
      const first = getTypeColor('unknown')
      const second = getTypeColor('unknown')
      expect(first).toBe(second)
    })
  })
})
