const TOKEN_KEY = 'lms_token'
const USER_KEY = 'lms_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function isAuthenticated() {
  return Boolean(getToken())
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function setSession(payload) {
  localStorage.setItem(TOKEN_KEY, payload.token)
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({ username: payload.username, roles: payload.roles || [] }),
  )
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
