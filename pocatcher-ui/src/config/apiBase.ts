export function getApiOrigin(): string {
  const raw = import.meta.env.VITE_API_ORIGIN as string | undefined
  if (raw === undefined || raw === "") {
    return ""
  }
  return raw.replace(/\/$/, "")
}

export function apiUrl(path: string): string {
  const origin = getApiOrigin()
  const normalized = path.startsWith("/") ? path : `/${path}`
  return origin ? `${origin}${normalized}` : normalized
}
